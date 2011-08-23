package android.smartcard.test;

import android.app.Activity;
import android.os.Bundle;

// Use new API
import org.simalliance.openmobileapi.*;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity implements SEService.CallBack {

	private static final byte[] ISD_AID = new byte[] { (byte) 0xA0, 0x00, 0x00,
			0x00, 0x03, 0x00, 0x00, 0x00 };

	TextView tv = null;
	ScrollView sv = null;

	SEService seService;

	private void logText(String message) {
		sv.post(new Runnable() {
			public void run() {
				sv.fullScroll(ScrollView.FOCUS_DOWN);
			}

		});
		tv.append(message);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ViewGroup.LayoutParams layoutParams = new ScrollView.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		sv = new ScrollView(this);

		LinearLayout ll = new LinearLayout(this);
		ll.setLayoutParams(layoutParams);
		sv.addView(ll);

		tv = new TextView(this);
		tv.setLayoutParams(layoutParams);
		ll.addView(tv);

		setContentView(sv);

		try {
			seService = new SEService(this, this);
		} catch (SecurityException e) {
			logText("Smartcard binding not allowed");
			Log.w("SmartCardSample", "Exception: " + e.getLocalizedMessage());
		} catch (Exception e) {
			logText("Exception: " + e.getMessage());
		}
	}

	@Override
	protected void onDestroy() {
		if (seService != null) {
			seService.shutdown();
		}
		super.onDestroy();
	}

	public void serviceConnected(SEService service) {
		logText("\nSmartcard interface available\n");
		Reader cardReader = null;

		logText("\ngetReaders()\n");
		try {
			for (Reader reader : seService.getReaders()) {
				logText(" " + reader.getName() + "\n");
			}
			cardReader = seService.getReaders()[0];
		} catch (Exception e) {
			logText(e.getMessage());
			return;
		}

		logText("\nisCardPresent()\n");
		try {
			boolean isPresent = cardReader.isSecureElementPresent();
			logText(isPresent ? " present\n" : " absent\n");
		} catch (Exception e) {
			logText(e.getMessage());
			return;
		}
		logText("\nopenBasicChannel()\n");
		Channel basicChannel;
		try {
			Session session = cardReader.openSession();
			basicChannel = session.openBasicChannel(null);
		} catch (Exception e) {
			logText(e.getMessage());
			return;
		}

		logText("\ntransmit() (GET CPLC)\n");
		try {
			byte[] response = basicChannel.transmit(new byte[] { (byte) 0x80,
					(byte) 0xCA, (byte) 0x9F, 0x7F, 0x00 });
			logText(" Response: " + bytesToString(response) + "\n");
		} catch (Exception e) {
			logText(e.getMessage());
			return;
		}

		logText("\nopenLogicalChannel() (ISD)\n");
		Channel logicalChannel;
		try {
			Session session = cardReader.openSession();
			logicalChannel = session.openLogicalChannel(ISD_AID);
		} catch (Exception e) {
			logText(e.getMessage());
			return;
		}

		logText("\ntransmit() (GET CPLC)\n");
		try {
			byte[] response = logicalChannel.transmit(new byte[] { (byte) 0x80,
					(byte) 0xCA, (byte) 0x9F, 0x7F, 0x00 });
			logText(" Response: " + bytesToString(response) + "\n");
		} catch (Exception e) {
			logText(e.getMessage());
			return;
		}

		logText("\nclose()\n");
		try {
			basicChannel.close();
			logText(" basic channel is closed");
		} catch (Exception e) {
			logText(e.getMessage());
			return;
		}

		logText("\nclose()\n");
		try {
			logicalChannel.close();
			logText(" logical channel is closed");
		} catch (Exception e) {
			logText(e.getMessage());
			return;
		}

	}

	private static String bytesToString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02x ", b & 0xFF));
		}
		return sb.toString();
	}
}
