package android.smartcard.test;

import android.app.Activity;
import android.os.Bundle;
import android.smartcard.CardException;
import android.smartcard.ICardChannel;
import android.smartcard.SmartcardClient;
import android.smartcard.SmartcardClient.ISmartcardConnectionListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity implements ISmartcardConnectionListener {
	private static final byte[] ISD_AID = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00};

	private static String bytesToString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02x ", b & 0xFF));
		}
		return sb.toString();
	}
	
	TextView tv = null;
	ScrollView sv = null;
	SmartcardClient smartcard;
	
	
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
    		smartcard = new SmartcardClient(this, this);
		} catch (SecurityException e) {
			logText("Smartcard binding not allowed");
		} catch (Exception e) {
			logText("Exception: " + e.getMessage());
		}
 	}
	
    @Override
	protected void onDestroy() {
		if (smartcard != null) {
			smartcard.shutdown();
		}
		super.onDestroy();
	}
    

	public void serviceConnected() {
		logText("\nSmartcard interface available\n");
		String cardReader = "";
		
		logText("\ngetReaders()\n");
        try {
			for (String reader : smartcard.getReaders()) {
				logText(" " + reader + "\n");
			}
			cardReader = smartcard.getReaders()[0];
		} catch (CardException e) {
			logText(e.getMessage());
			return;
		} catch (Exception e) {
			logText(e.getMessage());
			return;
		}
		
		logText("\nisCardPresent()\n");
        try {
			boolean isPresent = smartcard.isCardPresent(cardReader);
			logText(isPresent ? " present\n" : " absent\n");
		} catch (CardException e) {
			logText(e.getMessage());
			return;
		} catch (Exception e) {
			logText(e.getMessage());
			return;
		}
		
		logText("\nopenBasicChannel()\n");
		ICardChannel basicChannel;
        try {
        	basicChannel = smartcard.openBasicChannel(cardReader);
		} catch (CardException e) {
			logText(e.getMessage());
			return;
		}
		
		logText("\ntransmit() (GET CPLC)\n");
        try {
			byte[] response = basicChannel.transmit(new byte[] {(byte) 0x80, (byte) 0xCA, (byte) 0x9F, 0x7F, 0x00});
			logText(" Response: " + bytesToString(response) + "\n");
		} catch (CardException e) {
			logText(e.getMessage());
			return;
		}
		
		logText("\nopenLogicalChannel() (ISD)\n");
		ICardChannel logicalChannel;
        try {
        	logicalChannel = smartcard.openLogicalChannel(cardReader, ISD_AID);
		} catch (CardException e) {
			logText(e.getMessage());
			return;
		}
		
		logText("\ntransmit() (GET CPLC)\n");
        try {
			byte[] response = logicalChannel.transmit(new byte[] {(byte) 0x80, (byte) 0xCA, (byte) 0x9F, 0x7F, 0x00});
			logText(" Response: " + bytesToString(response) + "\n");
		} catch (CardException e) {
			logText(e.getMessage());
			return;
		}
		
		logText("\nclose()\n");
        try {
        	basicChannel.close();
			logText(" basic channel is closed");
		} catch (CardException e) {
			logText(e.getMessage());
			return;
		}
		
		logText("\nclose()\n");
        try {
        	logicalChannel.close();
			logText(" logical channel is closed");
		} catch (CardException e) {
			logText(e.getMessage());
			return;
		}		
	}

	public void serviceDisconnected() {
	}
}