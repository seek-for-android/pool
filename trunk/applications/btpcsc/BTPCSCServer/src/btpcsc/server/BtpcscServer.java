package btpcsc.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.smartcard.CardException;
import android.smartcard.ICardChannel;
import android.smartcard.SmartcardClient;
import android.smartcard.SmartcardClient.ISmartcardConnectionListener;
import android.widget.TextView;

public class BtpcscServer extends Activity {

	private static final int REQUEST_ENABLE_BT = 227339847;
	private static final int HANDLER_REFRESH_LOG = 706713485;
	private static final String appName = "btpcsc_server";

	private static final UUID appUuid = new UUID(0x42219abb16154486l,
			0xbd50496bd50496d8l);
	private static final byte[] BT_PCSC_ACK_CONNECTION = new byte[] { 0x00,
			0x00, 0x30, (byte) 0xF8 };

	private static final int BT_PCSC_CMD_ACK = 1;
	private static final int BT_PCSC_CMD_DISCONNECT = 2;
	private static final int BT_PCSC_CMD_SEND_APDU = 16;
	private static final int BT_PCSC_CMD_RECV_APDU = 17;
	private static final int BT_PCSC_CMD_GET_PRESENT = 24;
	private static final int BT_PCSC_CMD_GET_PRESENT_RESULT = 25;
	private static final int BT_PCSC_CMD_GET_SLOTS = 32;
	private static final int BT_PCSC_CMD_GET_SLOTS_RESULT = 33;
	private static final int BT_PCSC_CMD_SET_SLOT = 34;
	private static final int BT_PCSC_CMD_NOT_SUPPORTED = 254;
	private static final int BT_PCSC_CMD_ERROR = 255;

	private BluetoothAdapter adapter;
	private StringBuffer log = new StringBuffer();
	private AcceptThread acceptThread;
	private ClientCommunicationThread clientThread;
	private Handler handler;
	private boolean pausedForBluetoothEnabling, enablingBluetooth,
			smartcardServiceConnected;
	private SmartcardClient smartcardClient;
	private ISmartcardConnectionListener smartcardListener;
	private String smartcardReader;

	private class SmartcardListener implements
			SmartcardClient.ISmartcardConnectionListener {

		@Override
		public void serviceConnected() {
			smartcardServiceConnected = true;
			log("Connected to smartcard service.");
			try {
				String[] readers = smartcardClient.getReaders();

				if (readers == null || readers.length < 1) {
					log("No readers available.");
					return;
				}

				smartcardReader = readers[0];
				log("Using default slot: " + smartcardReader + ".");
			} catch (Exception e) {
				log("Could not get list of readers: " + e);
				return;
			}
		}

		@Override
		public void serviceDisconnected() {
			log("Disconnected from smartcard service.");
			smartcardServiceConnected = false;
			smartcardReader = null;
		}

	}

	private class AcceptThread extends Thread {
		private final BluetoothServerSocket serverSocket;
		private boolean cancel;

		public AcceptThread() {
			BluetoothServerSocket tmp = null;
			try {
				tmp = adapter.listenUsingRfcommWithServiceRecord(appName,
						appUuid);
			} catch (IOException e) {
			}
			serverSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			while (!cancel) {
				try {
					socket = serverSocket.accept();
				} catch (IOException e) {
					break;
				}
				if (socket != null) {
					manageConnectedSocket(socket);
				}
			}
		}

		public void cancel() {
			cancel = true;
			stopListening();
		}
	}

	private class ClientCommunicationThread extends Thread {

		private BluetoothSocket socket;
		private InputStream is;
		private OutputStream os;
		private boolean cancel, active;
		private ICardChannel channel;

		public ClientCommunicationThread(BluetoothSocket socket) {
			this.socket = socket;
		}

		public void run() {
			String address = socket.getRemoteDevice().getAddress();

			try {

				log("New client " + socket.getRemoteDevice().getAddress() + ".");

				BluetoothSocket socket = this.socket;
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				this.is = is;
				this.os = os;

				active = true;

				os.write(BT_PCSC_ACK_CONNECTION);
				os.flush();

				while (!cancel) {
					int cmd = is.read();

					switch (cmd) {
					case BT_PCSC_CMD_DISCONNECT:
						cancel = true;
						log("Client disconnected gracefully.");
						break;

					case BT_PCSC_CMD_GET_PRESENT:
						getCardPresentAndReply();
						break;

					case BT_PCSC_CMD_SEND_APDU:
						getApduAndReply();
						break;

					case BT_PCSC_CMD_GET_SLOTS:
						getSlots();
						break;

					case BT_PCSC_CMD_SET_SLOT:
						getAndSetSlot();
						break;

					default:
						os.write(BT_PCSC_CMD_NOT_SUPPORTED);
						log("Received unknown command: " + cmd);
					}

					if (!cancel)
						Thread.yield();
				}

				is.close();
				os.close();
				socket.close();

			} catch (IOException e) {
				log("Lost connection to client " + address + ".");

				try {
					this.socket.close();
				} catch (IOException e1) {
				}
			} catch (CardException e) {
				log("Could not open smartcard channel: " + e);
				try {
					this.socket.close();
				} catch (IOException e1) {
				}
			}

			if (channel != null)
				try {
					channel.close();
				} catch (CardException e) {
				}
			clientThread = null;

		}

		private void getSlots() throws IOException {

			log("Client asks for slot list.");

			String[] slots = null;
			try {
				slots = smartcardClient.getReaders();
			} catch (CardException e) {
				os.write(BT_PCSC_CMD_ERROR);
				return;
			}

			if (slots == null)
				return;

			int maxSlots = is.read();

			int nslots = Math.min(slots.length, maxSlots);
			os.write(BT_PCSC_CMD_GET_SLOTS_RESULT);
			os.write(nslots);

			for (int i = 0; i < nslots; i++) {
				int strlen = Math.min(slots[i].length(), 255);
				os.write(strlen);
				byte[] buffer = slots[i].getBytes();
				os.write(buffer, 0, strlen);
			}

		}

		private void getAndSetSlot() throws IOException {

			int length = is.read();
			byte[] buffer = new byte[length];
			is.read(buffer);

			String slot = new String(buffer);
			String[] slots = null;
			try {
				slots = smartcardClient.getReaders();
			} catch (CardException e) {
				os.write(BT_PCSC_CMD_ERROR);
				return;
			}
			if (slots == null)
				return;

			boolean contained = false;
			for (int i = 0; i < slots.length; i++) {
				if (slots[i].equals(slot)) {
					contained = true;
					break;
				}
			}

			if (!contained) {
				os.write(BT_PCSC_CMD_ERROR);
				log("Client requested nonexistent slot "+slot+".");
				return;
			}

			log("Setting slot to " + slot + ".");

			try {
				smartcardReader = slot;
				if (channel != null)
					channel.close();
				openChannel();
			} catch (CardException e) {
				os.write(BT_PCSC_CMD_ERROR);
				return;
			}

			os.write(BT_PCSC_CMD_ACK);

		}

		private void getCardPresentAndReply() throws CardException, IOException {

			byte state;
			if (smartcardReader != null) {
				state = (smartcardClient.isCardPresent(smartcardReader)) ? (byte) 1
						: (byte) 0;
			} else {
				state = 0;
			}

			byte[] buffer = new byte[] { BT_PCSC_CMD_GET_PRESENT_RESULT, state };
			os.write(buffer);

		}

		private void openChannel() throws CardException {
			try {
				channel = smartcardClient.openBasicChannel(smartcardReader);
			} catch (Exception e) {
				throw new CardException(e);
			}

			if (channel == null)
				throw new CardException("Could not open channel.");
		}

		private boolean apduEquals(byte[] buffer, byte[] apdu) {

			if (buffer.length < apdu.length)
				return false;

			for (int i = 0; i < apdu.length; i++)
				if (buffer[i] != apdu[i])
					return false;

			return true;

		}

		private boolean catchSpecialApdu(byte[] buffer) throws IOException,
				CardException {

			return false;

		}

		private void getApduAndReply() throws IOException, CardException {

			int apduLength = is.read();
			byte[] buffer = new byte[apduLength];
			is.read(buffer);

			log("Client transmitted APDU: " + apduToString(buffer));

			// This is not a regular APDU to be sent to the card. The handler
			// has taken care of it, so don't bother.
			// if (catchSpecialApdu(buffer))
			// return;

			if (channel == null)
				openChannel();

			byte[] receivedApdu = null;
			try {
				receivedApdu = channel.transmit(buffer);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (receivedApdu != null)
				log("Card returned APDU: " + apduToString(receivedApdu));

			sendApdu(receivedApdu);

		}

		private void sendApdu(byte[] apdu) throws IOException {

			if (apdu == null) {

				os.write(0);
				os.flush();

			} else {

				byte[] buffer = new byte[apdu.length + 2];
				buffer[0] = (byte) BT_PCSC_CMD_RECV_APDU;
				buffer[1] = (byte) apdu.length;
				for (int i = 0; i < apdu.length; i++)
					buffer[i + 2] = apdu[i];
				os.write(buffer);
				os.flush();

			}
		}

		public void cancel() {
			cancel = true;
			if (active) {
				try {
					is.close();
				} catch (IOException e) {
				}
				try {
					os.close();
				} catch (IOException e) {
				}
				try {
					socket.close();
				} catch (IOException e) {
				}
				active = false;
			}
		}

	}

	protected void manageConnectedSocket(BluetoothSocket socket) {

		// We want only one client at a time.
		if (clientThread != null)
			return;

		clientThread = new ClientCommunicationThread(socket);
		clientThread.start();
	}

	private static String apduToString(byte[] buffer) {
		final char[] HEXMAP = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
				'9', 'A', 'B', 'C', 'D', 'E', 'F' };

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < buffer.length; i++) {
			int b = (int) buffer[i];
			char c1 = HEXMAP[(b >> 4) & 0xF];
			char c2 = HEXMAP[b & 0xF];
			if (i > 0)
				sb.append(' ');
			sb.append(c1);
			sb.append(c2);
		}

		return sb.toString();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		smartcardListener = new SmartcardListener();
		try {
			smartcardClient = new SmartcardClient(this, smartcardListener);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HANDLER_REFRESH_LOG:
					doRefreshLog();
					break;
				}
			}

		};

	}

	@Override
	protected void onResume() {
		super.onResume();

		clearLog();
		log("Application started. Launching Server.");
		if (smartcardReader != null)
			log("Using reader " + smartcardReader + ".");

		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

		if (adapter == null) {
			log("No bluetooth device detected.");
		} else {
			this.adapter = adapter;
			if (!adapter.isEnabled()) {
				if (!pausedForBluetoothEnabling) {
					Intent enableBtIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					pausedForBluetoothEnabling = enablingBluetooth = true;
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
			} else {
				startListening();
			}
		}

	}

	protected void onDestroy() {
		if (smartcardClient != null)
			smartcardClient.shutdown();
		super.onDestroy();
	}

	protected void startListening() {
		if (acceptThread != null)
			return;
		acceptThread = new AcceptThread();
		acceptThread.start();
	}

	protected void stopListening() {
		if (acceptThread == null)
			return;
		try {
			acceptThread.serverSocket.close();
		} catch (IOException e) {
		}
		acceptThread = null;
	}

	protected void killConnections() {
		if (acceptThread != null) {
			acceptThread.cancel();
			acceptThread = null;
		}
		if (clientThread != null) {
			clientThread.cancel();
			clientThread = null;
		}
	}

	@Override
	protected void onPause() {
		pausedForBluetoothEnabling = enablingBluetooth;
		super.onPause();
		killConnections();
	}

	protected void clearLog() {
		log = new StringBuffer();
		fireRefreshLog();
	}

	protected void log(String s) {
		log(s, true);
	}

	protected void log(String s, boolean addNewLine) {
		log.append(s);
		System.out.println(s);
		if (addNewLine)
			log.append("\n");
		fireRefreshLog();
	}

	protected void doRefreshLog() {
		try {
			((TextView) findViewById(R.id.text)).setText(log);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void fireRefreshLog() {
		handler.sendEmptyMessage(HANDLER_REFRESH_LOG);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		enablingBluetooth = false;

		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == RESULT_OK) {
				startListening();
			} else {
				log("Cannot start server - Bluetooth is disabled.");
			}
		}

	}

	protected void onStop() {
		if (acceptThread != null) {
			acceptThread.cancel();
			acceptThread = null;
		}
		super.onStop();
	}

}