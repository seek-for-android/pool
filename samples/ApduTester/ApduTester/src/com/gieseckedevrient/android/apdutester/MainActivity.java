package com.gieseckedevrient.android.apdutester;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.smartcard.CardException;
import android.smartcard.ICardChannel;
import android.smartcard.SmartcardClient;
import android.smartcard.SmartcardClient.ISmartcardConnectionListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends Activity {

	private static final byte[] APPLET_AID = new byte[] { (byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x01, 0x01 };	
	private static String READER = "UICC";
	
	SmartcardClient smartcard;
	
	Thread thread = null;
	
	TextView textview = null;
	ScrollView scrollview = null;
	Button button1 = null;
	Button button2 = null;
	Button button3 = null;
	Button button4 = null;
	Button button5 = null;

	List<String> message = new ArrayList(0);
	
	final Handler mHandler = new Handler();
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
    		scrollview.post(new Runnable() {
    			public void run() {
    				scrollview.fullScroll(ScrollView.FOCUS_DOWN);
    			}

    		});
    		if (message.isEmpty() == false) {
    			String msg = message.remove(0);
    			Log.v("APDUSTRESS", msg);
    			textview.append(msg);
    		}
        }
    };

	
	ISmartcardConnectionListener connectionListener = new ISmartcardConnectionListener() {
		public void serviceConnected() {
			logText("Smart card service connected\n");
			button1.setEnabled(true);
			button2.setEnabled(true);
			button3.setEnabled(true);
			button4.setEnabled(true);
			button5.setEnabled(true);
		}

		public void serviceDisconnected() {
			logText("Smart card service disconnected\n");
			button1.setEnabled(false);
			button2.setEnabled(false);
			button4.setEnabled(false);
			button5.setEnabled(false);
			
			connectToService();
		}
	};

	
	private void logText(String message) {
		this.message.add(message);
		mHandler.post(mUpdateResults);
	}
	
	private static String bytesToString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02x ", b & 0xFF));
		}
		return sb.toString();
	}
	

	private void connectToService() {
		logText("Connecting to smart card service...\n");
		try {
			smartcard = new SmartcardClient(this, connectionListener);
		} catch (SecurityException e) {
			logText("Binding not allowed, uses-permission SMARTCARD?");
		} catch (Exception e) {
			logText("Exception: " + e.getMessage());
		}
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);

		button1 = new Button(this);
		button1.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		button1.setText("LogicalChannel");
		button1.setEnabled(false);
		button1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				runTestLogicalChannel();
			}
		});
		button2 = new Button(this);
		button2.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		button2.setText("BasicChannel");
		button2.setEnabled(false);
		button2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				runTestBasicChannel();
			}
		});
		button3 = new Button(this);
		button3.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		button3.setText("LargeAPDU");
		button3.setEnabled(false);
		button3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				runTestLargeAPDU();
			}
		});
		button4 = new Button(this);
		button4.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		button4.setText("IncreaseAPDU");
		button4.setEnabled(false);
		button4.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				runTestIncreaseAPDU();
			}
		});
		button5 = new Button(this);
		button5.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		button5.setText("DelayAPDU");
		button5.setEnabled(false);
		button5.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				runTestDelayAPDU();
			}
		});

		scrollview = new ScrollView(this);

		textview = new TextView(this);
		textview.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		scrollview.addView(textview);

		layout.addView(button1);
		layout.addView(button2);
		layout.addView(button3);
		layout.addView(button4);
		layout.addView(button5);
		layout.addView(scrollview);

		setContentView(layout);

		connectToService();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Mobile Security Card");
		menu.add(0, 1, 0, "UICC");
		menu.add(0, 2, 0, "SmartMX");
		menu.add(0, 3, 0, "Exit");

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case 0: READER = "Mobile Security Card"; break;
		case 1: READER = "UICC"; break;
		case 2: READER = "SmartMX"; break;
		case 3: finish(); break;
		}
		
		return true;
	}
	
	@Override
	protected void onDestroy() {
		if (smartcard != null) {
			logText("Disconnecting from smart card service\n");
			smartcard.shutdown();
		}
		
		super.onDestroy();
	}

    
	private void runApduCaseTest(ICardChannel cardChannel) {
		try {
			String[] readers = smartcard.getReaders();
			logText("Testcase CASE-1 APDU\n");
			byte[] cmdApdu = new byte[] { (byte) 0x00, 0x01, 0x00, 0x00, 0x00 };
			logText(" ->: " + bytesToString(cmdApdu) + "\n");
			byte[] rspApdu = cardChannel.transmit(cmdApdu);
			logText(" <-: " + bytesToString(rspApdu) + "\n");			
			if (rspApdu.length != 2 || rspApdu[0] != (byte) 0x90 || rspApdu[1] != 0x00) {
				logText("Error: test failed (expected response 90:00)\n");
			} else {
				logText("Ok: test succeeded\n");				
			}
			logText("Testcase CASE-2 APDU\n");
			cmdApdu = new byte[] { (byte) 0x00, 0x02, 0x00, 0x00, 0x10 };
			logText(" ->: " + bytesToString(cmdApdu) + "\n");
			rspApdu = cardChannel.transmit(cmdApdu);
			logText(" <-: " + bytesToString(rspApdu) + "\n");			
			if (rspApdu.length != 0x12 || rspApdu[rspApdu.length-2] != (byte) 0x90 || rspApdu[rspApdu.length-1] != 0x00 || Util.isEqual(new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f }, rspApdu) == false) {
				logText("Error: test failed (expected response 00:01:02:03:04:05:06:07:08:09:0a:0b:0c:0d:0e:0f:90:00)\n");
			} else {
				logText("Ok: test succeeded\n");				
			}
			logText("Testcase CASE-3 APDU\n");
			cmdApdu = new byte[] { (byte) 0x00, 0x03, 0x00, 0x00, 0x10, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
			logText(" ->: " + bytesToString(cmdApdu) + "\n");
			rspApdu = cardChannel.transmit(cmdApdu);
			logText(" <-: " + bytesToString(rspApdu) + "\n");			
			if (rspApdu.length != 2 || rspApdu[rspApdu.length-2] != (byte) 0x90 || rspApdu[rspApdu.length-1] != 0x00) {
				logText("Error: test failed (expected response 90:00)\n");
			} else {
				logText("Ok: test succeeded\n");				
			}
			logText("Testcase CASE-4 APDU\n");
			cmdApdu = new byte[] { (byte) 0x00, 0x04, 0x00, 0x00, 0x10, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
			logText(" ->: " + bytesToString(cmdApdu) + "\n");
			rspApdu = cardChannel.transmit(cmdApdu);
			logText(" <-: " + bytesToString(rspApdu) + "\n");			
			if (rspApdu.length != 0x12 || rspApdu[rspApdu.length-2] != (byte) 0x90 || rspApdu[rspApdu.length-1] != 0x00 || Util.isEqual(new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f }, rspApdu) == false) {
				logText("Error: test failed (expected response 00:01:02:03:04:05:06:07:08:09:0a:0b:0c:0d:0e:0f:90:00)\n");
			} else {
				logText("Ok: test succeeded\n");				
			}
		} catch (CardException e) {
			logText("CardException: " + e.getMessage());
			return;
		}
	}

	private void runTestLogicalChannel() {
		textview.setText("");
		logText("\nExecuting runTestLogicalChannel()...\n");
		ICardChannel cardChannel = null;

		
		logText("List card readers:\n");
		try {
			String[] readers = smartcard.getReaders();
			for (String reader : readers) {
				logText(" " + reader + "\n");
			}
		} catch (CardException e) {
			logText("CardException: " + e.getMessage());
			return;
		}

		
		logText("Smart card status in " + READER + ":\n");
		try {
			boolean isPresent = smartcard.isCardPresent(READER);
			logText(" " + (isPresent ? "present\n" : "absent\n"));
			if (isPresent == false) {
				return;
			}
		} catch (CardException e) {
			logText("CardException: " + e.getMessage());
			return;
		}

		
		logText("Open Logical Channel to Applet " + bytesToString(APPLET_AID) + "\n");
		try {
			cardChannel = smartcard.openLogicalChannel(READER, APPLET_AID);
			runApduCaseTest(cardChannel);
		} catch (CardException e) {
			logText("CardException: " + e.getMessage());
			return;
		}
		
		
		logText("Closing logical channel channel\n");
		try {
			cardChannel.close();
		} catch (CardException e) {
			logText("CardException: " + e.getMessage());
			return;
		}		
	}


	private void runTestBasicChannel() {
		textview.setText("");
		logText("\nExecuting runTestBasicChannel()...\n");
		ICardChannel cardChannel = null;

		
		logText("List card readers:\n");
		try {
			String[] readers = smartcard.getReaders();
			for (String reader : readers) {
				logText(" " + reader + "\n");
			}
		} catch (CardException e) {
			logText("CardException: " + e.getMessage());
			return;
		}

		
		logText("Smart card status:\n");
		try {
			boolean isPresent = smartcard.isCardPresent(READER);
			logText(" " + (isPresent ? "present\n" : "absent\n"));
			if (isPresent == false) {
				return;
			}
		} catch (CardException e) {
			logText("CardException: " + e.getMessage());
			return;
		}

		
		logText("Open Basic Channel to Applet " + bytesToString(APPLET_AID) + "\n");
		try {
			cardChannel = smartcard.openBasicChannel(READER);
			byte[] cmdApdu = new byte[] { 0x00, (byte)0xA4, 0x04, 0x00, 0x07, (byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x01, 0x01 };
			logText(" ->: " + bytesToString(cmdApdu) + "\n");
			byte[] rspApdu = cardChannel.transmit(cmdApdu);
			logText(" <-: " + bytesToString(rspApdu) + "\n");
			runApduCaseTest(cardChannel);
		} catch (CardException e) {
			logText("CardException: " + e.getMessage());
			return;
		}
		
		
		logText("Closing basic channel channel\n");
		try {
			cardChannel.close();
		} catch (CardException e) {
			logText("CardException: " + e.getMessage());
			return;
		}		
	}


	private void runTestLargeAPDU() {
		textview.setText("");
		thread = new Thread() {
            public void run() {
                logText("\nExecuting runTestLargeAPDU()...\n");
        		ICardChannel cardChannel = null;
        		
        		try {
        			if (READER == "SmartMX") {
        				cardChannel = smartcard.openBasicChannel(READER);
        				byte[] cmdapdu = new byte[] { 0x00, (byte)0xA4, 0x04, 0x00, 0x07, (byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x01, 0x01 };
        				logText(" ->: " + bytesToString(cmdapdu) + "\n");
        				byte[] rspapdu = cardChannel.transmit(cmdapdu);
        				logText(" <-: " + bytesToString(rspapdu) + "\n");
        			} else {
        				cardChannel = smartcard.openLogicalChannel(READER, APPLET_AID);
        			}

        			for (int isoCase=1; isoCase<=4; isoCase++) {
	        			for (int i=0; i<25; i++) {
	        				byte[] cmdApdu;
	        				if (isoCase == 2) {
	        					// no command data
	        					cmdApdu = new byte[5];
	        				} else {
	        					cmdApdu = new byte[250];	        					
	        				}
	        				String random = Util.randomString(cmdApdu.length);
	        				for (int t=1; t<cmdApdu.length; t++) {
	        					cmdApdu[t] = (byte)random.getBytes()[t];
	        				}
	        				cmdApdu[1] = (byte) isoCase;
	        				if (isoCase == 2) {
	        					cmdApdu[4] = 0x10;
	        				} else {
		        				cmdApdu[4] = (byte) (cmdApdu.length - 5);
	        				}
	        				
	        				logText(i + " ->: " + bytesToString(cmdApdu) + "\n");
	        				byte[] rspApdu = cardChannel.transmit(cmdApdu);
	        				logText(i + " <-: " + bytesToString(rspApdu) + "\n");
	
	        				if (isoCase == 1 || isoCase == 3) {
	        					// only 90:00
	        					if (rspApdu.length != 2 || rspApdu[rspApdu.length-2] != (byte) 0x90 || rspApdu[rspApdu.length-1] != 0x00) {
	        						logText("Error: test failed (expected response: 90:00\n");
		        					try {
		        						if (cardChannel != null)
		        							cardChannel.close();
		        					} catch (CardException ex) {
		        					}		
		        					return;
	        					}
	        				} else if (isoCase == 2) {
	        					if (rspApdu.length != 0x12 || rspApdu[rspApdu.length-2] != (byte) 0x90 || rspApdu[rspApdu.length-1] != 0x00 || Util.isEqual(new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f }, rspApdu) == false) {
	        						logText("Error: test failed (response APDU does not contain expected data)\n");
		        					try {
		        						if (cardChannel != null)
		        							cardChannel.close();
		        					} catch (CardException ex) {
		        					}		
		        					return;			
	        					}
	        				} else {
	        					// data and 90:00
	        					byte[] newCmd = new byte[cmdApdu.length-5];
	        					System.arraycopy(cmdApdu, 5, newCmd, 0, cmdApdu.length-5);
		        				if (Util.isEqual(newCmd, rspApdu) == false) {
		        					logText("Error: test failed (response APDU does not contain expected data)\n");
		        					try {
		        						if (cardChannel != null)
		        							cardChannel.close();
		        					} catch (CardException ex) {
		        					}		
		        					return;
		        				}
	        				}
	        			}
        			}
        			logText("Ok: test succeeded\n");
        		} catch (CardException e) {
        			logText("CardException: " + e.getMessage());
        			return;
        		} finally {
        			try {
        				if (cardChannel != null)
        					cardChannel.close();
        			} catch (CardException e) {
        				logText("CardException: " + e.getMessage());
        				return;
        			}		
        		}
            }
        };
        thread.start();
	}

	
	private void runTestIncreaseAPDU() {
		textview.setText("");
		thread = new Thread() {
            public void run() {
                logText("\nExecuting runTestAPDUIncrease()...\n");
        		ICardChannel cardChannel = null;
        		
        		try {
        			if (READER == "SmartMX") {
        				cardChannel = smartcard.openBasicChannel(READER);
        				byte[] cmdapdu = new byte[] { 0x00, (byte)0xA4, 0x04, 0x00, 0x07, (byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x01, 0x01 };
        				logText(" ->: " + bytesToString(cmdapdu) + "\n");
        				byte[] rspapdu = cardChannel.transmit(cmdapdu);
        				logText(" <-: " + bytesToString(rspapdu) + "\n");
        			} else {
        				cardChannel = smartcard.openLogicalChannel(READER, APPLET_AID);
        			}
       			
        			for (int isoCase=3; isoCase<=4; isoCase++) {
 	        			for (int i=5; i<255; i++) {
	        				byte[] cmdApdu = new byte[i];
	        				String random = Util.randomString(cmdApdu.length);
	        				for (int t=1; t<cmdApdu.length; t++) {
	        					cmdApdu[t] = (byte)random.getBytes()[t];
	        				}
	        				cmdApdu[4] = (byte) (cmdApdu.length - 5);
	        				cmdApdu[1] = (byte) isoCase;
	        				
	        				logText(i + " ->: " + bytesToString(cmdApdu) + "\n");
	        				byte[] rspApdu = cardChannel.transmit(cmdApdu);
	        				logText(i + " <-: " + bytesToString(rspApdu) + "\n");
	
	        				if (isoCase == 1 || isoCase == 3) {
	        					// only 90:00
	        					if (rspApdu.length != 2 || rspApdu[rspApdu.length-2] != (byte) 0x90 || rspApdu[rspApdu.length-1] != 0x00) {
	        						logText("Error: test failed (expected response: 90:00\n");
		        					try {
		        						if (cardChannel != null)
		        							cardChannel.close();
		        					} catch (CardException ex) {
		        					}		
		        					return;
	        					}
	        				} else {
	        					// data and 90:00
	        					byte[] newCmd = new byte[cmdApdu.length-5];
	        					System.arraycopy(cmdApdu, 5, newCmd, 0, cmdApdu.length-5);
		        				if (Util.isEqual(newCmd, rspApdu) == false) {
		        					logText("Error: test failed (response APDU does not contain expected data)\n");
		        					try {
		        						if (cardChannel != null)
		        							cardChannel.close();
		        					} catch (CardException ex) {
		        					}		
		        					return;
		        				}
	        				}
	        			}
        			}
        			logText("Ok: test succeeded\n");
        		} catch (CardException e) {
        			logText("CardException: " + e.getMessage());
        			return;
        		} finally {
        			try {
        				if (cardChannel != null)
        					cardChannel.close();
        			} catch (CardException e) {
        				logText("CardException: " + e.getMessage());
        				return;
        			}		
        		}
            }
        };
        thread.start();
	}


	private void runTestDelayAPDU() {
		textview.setText("");
		thread = new Thread() {
            public void run() {
                logText("\nExecuting runTestDelayAPDU()...\n");
        		ICardChannel cardChannel = null;
        		
        		try {
        			if (READER == "SmartMX") {
        				cardChannel = smartcard.openBasicChannel(READER);
        				byte[] cmdapdu = new byte[] { 0x00, (byte)0xA4, 0x04, 0x00, 0x07, (byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x01, 0x01 };
        				logText(" ->: " + bytesToString(cmdapdu) + "\n");
        				byte[] rspapdu = cardChannel.transmit(cmdapdu);
        				logText(" <-: " + bytesToString(rspapdu) + "\n");
        			} else {
        				cardChannel = smartcard.openLogicalChannel(READER, APPLET_AID);
        			}
        			
        			for (int i=0; i<300; i++) {
        				runApduCaseTest(cardChannel);        				
        				Thread.sleep(i*100);
        			}
        		} catch (CardException e) {
        			logText("CardException: " + e.getMessage());
        			return;
        		} catch (InterruptedException e) {
       				logText("CardException: " + e.getMessage());
				} finally {
        			try {
        				if (cardChannel != null)
        					cardChannel.close();
        			} catch (CardException e) {
        				logText("CardException: " + e.getMessage());
        				return;
        			}		
        		}
            }
        };
        thread.start();
	}
}
