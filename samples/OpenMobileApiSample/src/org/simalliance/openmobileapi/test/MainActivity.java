package org.simalliance.openmobileapi.test;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;
import org.simalliance.openmobileapi.SEService.CallBack;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * This sample demonstrates how a Secure Element (SE) based android application
 * can be realized via the G&D Smart Card API .<br>
 * 
 * Smart Card API implements the simalliance Open Mobile API ({@link http://www.simalliance.org}).
 * 
 */
public class MainActivity extends Activity {

	/** Open Mobile API service. */
	SEService _service = null;
	/** GUI elements on the screen. */
	TextView _textview = null;
	ScrollView _scrollview = null;
	/** AID of the issuer security domain. */
	private static final byte[] ISD_AID = new byte[] { (byte) 0xA0, 0x00, 0x00,
			0x00, 0x03, 0x00, 0x00, 0x00 };

	private static String bytesToString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02x ", b & 0xFF));
		}
		return sb.toString();
	}

	private void logText(String message) {
		_scrollview.post(new Runnable() {
			public void run() {
				_scrollview.fullScroll(ScrollView.FOCUS_DOWN);
			}

		});
		_textview.append(message);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);

		_scrollview = new ScrollView(this);

		_textview = new TextView(this);
		_textview.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		_scrollview.addView(_textview);

		layout.addView(_scrollview);

		setContentView(layout);

		SEServiceCallback callback = new SEServiceCallback();
		new SEService(this, callback);
	}

	/**
	 * Callback interface if informs that this SEService is connected to the
	 * backend system and it's resources.
	 */
	public class SEServiceCallback implements CallBack {

		/**
		 * This method will be called if this SEService is connected
		 */
		public void serviceConnected(SEService service) {
			try {
				performsOperations(service);
			} catch (Exception e) {
				printException(e);
			}
		}
	}

	void performsOperations(SEService service) throws Exception {

		_service = service;
		Reader[] readers = service.getReaders();
		logText("All available readers:  \n");
		for (Reader reader : readers)
			logText("	" + reader.getName() + "\n");

		if (readers.length == 0) {
			logText("No reader available \n");
			service.shutdown();
			return;
		}

		for (Reader reader : readers) {
			logText("Selected Reader:" + reader.getName() + "\n");

			logText("SecureElement:");
			boolean isPresent = reader.isSecureElementPresent();
			logText(isPresent ? " present\n" : " absent\n");
			logText("\n");
			if (!isPresent)
				continue;
			
			Session session = reader.openSession();
			
			byte[] atr = session.getATR();
			if(atr != null)
			{
				logText("ATR: " + bytesToString(atr));
				logText("\n");
			}

			logText("************************\n");
			logText("basic channel test\n");
			logText("************************\n");

			logText("\n");
			logText("OpenBasicChannel\n");
			Channel basicChannel = session.openBasicChannel(null);
			logText("\n");

			logText("basicChannel.transmit() \n");
			byte[] cmd = new byte[] { (byte) 0x80, (byte) 0xCA, (byte) 0x9F,
					0x7F, 0x00 };
			logText(" Command: " + bytesToString(cmd) + "\n");
			byte[] response = basicChannel.transmit(cmd);
			logText(" Response: " + bytesToString(response) + "\n");
			logText("\n");

			logText("************************\n");
			logText("logical channel test\n");
			logText("************************\n");

			logText("\n");
			logText("OpenLogicalChannel\n");
			Channel logicalChannel = session.openLogicalChannel(null);
			logText("\n");

			logText("logicalChannel.transmit() \n");
			cmd = new byte[] { (byte) 0x80, (byte) 0xCA, (byte) 0x9F, 0x7F,
					0x00 };
			logText(" Command: " + bytesToString(cmd) + "\n");
			response = logicalChannel.transmit(cmd);
			logText(" Response: " + bytesToString(response) + "\n");

			logText("\n");
			logText("basicChannel.close()\n");
			basicChannel.close();
			logText("logicalChannel.close()\n");
			logicalChannel.close();
			logText("\n");

			logText("************************\n");
			logText("basic channel test (AID)\n");
			logText("************************\n");

			logText("\n");
			logText("OpenBasicChannel(AID=" + bytesToString(ISD_AID) + ")\n");
			basicChannel = session.openBasicChannel(ISD_AID);
			logText("\n");

			logText("basicChannel.transmit() \n");
			cmd = new byte[] { (byte) 0x80, (byte) 0xCA, (byte) 0x9F, 0x7F,
					0x00 };
			logText(" Command: " + bytesToString(cmd) + "\n");
			response = basicChannel.transmit(cmd);
			logText(" Response: " + bytesToString(response) + "\n");

			logText("************************\n");
			logText("logical channel test (AID)\n");
			logText("************************\n");

			logText("\n");
			logText("OpenLogicalChannel(AID=" + bytesToString(ISD_AID) + ")\n");
			logicalChannel = session.openLogicalChannel(ISD_AID);
			logText("\n");

			logText("logicalChannel.transmit() \n");
			cmd = new byte[] { (byte) 0x80, (byte) 0xCA, (byte) 0x9F, 0x7F,
					0x00 };
			logText(" Command: " + bytesToString(cmd) + "\n");
			response = logicalChannel.transmit(cmd);
			logText(" Response: " + bytesToString(response) + "\n");

			logText("\n");
			logText("basicChannel.close()\n");
			basicChannel.close();
			logText("logicalChannel.close()\n");
			logicalChannel.close();
			logText("\n");

		}

	}

	@Override
	protected void onDestroy() {
		if (_service != null) {
			_service.shutdown();
		}
		super.onDestroy();
	}

	public void printException(Exception e) {
		String msg = e.toString();
		logText("Exception: " + msg);
		Log.e("OpenMobileApiSample", msg);
	}

}