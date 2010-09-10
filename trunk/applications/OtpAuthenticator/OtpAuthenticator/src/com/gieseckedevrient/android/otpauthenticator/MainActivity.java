/*
 * Copyright 2010 Giesecke & Devrient GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gieseckedevrient.android.otpauthenticator;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.smartcard.ICardChannel;
import android.smartcard.SmartcardClient;
import android.smartcard.SmartcardClient.ISmartcardConnectionListener;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String OTP_DEMO_TAG = "OtpCalculator";

	private Context mContext = null;
	private Timer mTimer = null;
	private String cardReader = "Mobile Security Card 00 00";
	public static String otp = "";

	ISmartcardConnectionListener connectionListener = new ISmartcardConnectionListener() {
		public void serviceConnected() {
			// ensure we deal with the correct card reader
	        try {
	        	String[] reader = smartcard.getReaders(); 
				if (reader.length > 0 && reader[0] != cardReader)
					cardReader = reader[0];
			} catch (Exception e) {
				Toast.makeText(mContext, "Exception while accessing the card reader", Toast.LENGTH_LONG).show();
				Log.e(OTP_DEMO_TAG, "Exception.1: " + e.getMessage());
				finish();
				return;
			}
			
			// ensure a card is present
	        try {
				if (smartcard.isCardPresent(cardReader) == false) {
					Toast.makeText(mContext, "No MicroSD card in reader", Toast.LENGTH_LONG).show();
					finish();
					return;
				}
			} catch (Exception e) {
				Toast.makeText(mContext, "Exception while accessing the card reader", Toast.LENGTH_LONG).show();
				Log.e(OTP_DEMO_TAG, "Exception.2: " + e.getMessage());
				finish();
				return;
			}

			refresh();
		}

		public void serviceDisconnected() {
			// SmartcardService was killed - restart the binding...
		}
	};
	SmartcardClient smartcard;

	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "About").setIcon(R.drawable.menu_about);
		menu.add(0, 1, 0, "Refresh").setIcon(R.drawable.menu_refresh);
		menu.add(0, 2, 0, "Personalize").setIcon(R.drawable.menu_personalize);
		menu.add(0, 3, 0, "Set Counter").setIcon(R.drawable.menu_set_cntr);
		menu.add(0, 4, 0, "Show Counter").setIcon(R.drawable.menu_get_cntr);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		mTimer.cancel();
		
		switch(item.getItemId()) {
		case 0:		// ABOUT 
			this.startActivity(new Intent(this, AboutActivity.class));
			break;
			
		case 1:		// REFRESH 
			refresh();
			break;
			
		case 2:		// PERSONALIZE
			final EditText seed = new EditText(this);
			new AlertDialog.Builder(MainActivity.this).setTitle("Personalize").setMessage("Set the OATH seed (20 hex bytes, e.g. 31ab9f7b...) and reset the counter to 0x00").setView(seed).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
		        	byte[] command = new byte[] {0x00, 0x12, 0x06, 0x00, 0x14, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		        	byte[] hexSeed;
		        	try {
		        		hexSeed = new BigInteger(seed.getText().toString(), 16).toByteArray();
		        	} catch (Exception e) {
						Toast.makeText(mContext, "Seed is not a valid hex string", Toast.LENGTH_LONG).show();
						Log.e(OTP_DEMO_TAG, "Exception: " + e.getMessage());
						setTimer();
						return;
		        	}
					System.arraycopy(hexSeed, 0, command, 5, (hexSeed.length > 20) ? 20 : hexSeed.length);


					ICardChannel cardChannel = null;
			        try {
						cardChannel = smartcard.openLogicalChannel(cardReader, new byte[] {(byte)0xD2, 0x76, 0x00, 0x01, 0x18, 0x00, 0x03, (byte)0xFF, 0x49, 0x10, 0x00, (byte)0x89, 0x00, 0x00, 0x02, 0x01});
			        	if (cardChannel == null)
			        		throw new Exception("No basic card channel available");
			        	
			        	
			        	// P1=06 is OTP length
						byte[] response = cardChannel.transmit(command);
						cardChannel.close();

						if (response.length != 2 || response[response.length-2] != (byte)0x90 || response[response.length-1] != 0x00)
								throw new Exception("Cannot communicate with OTP applet");
						
						refresh();
					} catch (Exception e) {
						Toast.makeText(mContext, "Exception while accessing the card reader", Toast.LENGTH_LONG).show();
						Log.e(OTP_DEMO_TAG, "Exception.3: " + e.getMessage());
						try {
							cardChannel.close();
						} catch (Exception ex) {
						}
						finish();
					}
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				setTimer();
			}
			}).show();
			break;
			
		case 3:		// SET COUNTER
			final EditText counter = new EditText(this);
			new AlertDialog.Builder(MainActivity.this).setTitle("Set Counter").setMessage("Set the OATH counter (decimal).\n0 < counter < 2^64").setView(counter).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
		        	byte[] command = new byte[] {0x00, 0x10, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		        	byte[] cntr;
		        	try {
		        		cntr = new BigInteger(counter.getText().toString()).toByteArray();
		        	} catch (Exception e) {
						Toast.makeText(mContext, "Counter is not a valid decimal number", Toast.LENGTH_LONG).show();
						Log.e(OTP_DEMO_TAG, "Exception: " + e.getMessage());
						setTimer();
						return;
		        	}
					System.arraycopy(cntr, 0, command, command.length-cntr.length, cntr.length);

					ICardChannel cardChannel = null;
			        try {
			        	cardChannel = smartcard.openLogicalChannel(cardReader, new byte[] {(byte)0xD2, 0x76, 0x00, 0x01, 0x18, 0x00, 0x03, (byte)0xFF, 0x49, 0x10, 0x00, (byte)0x89, 0x00, 0x00, 0x02, 0x01});
			        	if (cardChannel == null)
			        		throw new Exception("No basic card channel available");
			        	
						byte[] response = cardChannel.transmit(command);
						cardChannel.close();

						if (response.length != 2 || response[response.length-2] != (byte)0x90 || response[response.length-1] != 0x00)
								throw new Exception("Cannot communicate with OTP applet");
						
						refresh();
					} catch (Exception e) {
						Toast.makeText(mContext, "Exception while accessing the card reader", Toast.LENGTH_LONG).show();
						Log.e(OTP_DEMO_TAG, "Exception: " + e.getMessage());
						try {
							cardChannel.close();
						} catch (Exception ex) {
						}
						finish();
					}
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				setTimer();
			}
			}).show();
			break;

		case 4:		// GET COUNTER
			ICardChannel cardChannel = null;
	        try {
	        	cardChannel = smartcard.openLogicalChannel(cardReader, new byte[] {(byte)0xD2, 0x76, 0x00, 0x01, 0x18, 0x00, 0x03, (byte)0xFF, 0x49, 0x10, 0x00, (byte)0x89, 0x00, 0x00, 0x02, 0x01});
	        	if (cardChannel == null)
	        		throw new Exception("No basic card channel available");
	        	
	        	byte[] command = new byte[] {0x00, 0x11, 0x00, 0x00, 0x00};
	        	
				byte[] response = cardChannel.transmit(command);
				cardChannel.close();
				
				if (response.length != 10 || response[response.length-2] != (byte)0x90 || response[response.length-1] != 0x00)
						throw new Exception("Cannot communicate with OTP applet");
				
				byte[] data = new byte[8];
				System.arraycopy(response, 0, data, 0, 8);
	        	BigInteger cntr = new BigInteger(data);
				Toast.makeText(mContext, "OATH Counter: " + cntr.longValue(), Toast.LENGTH_LONG).show();
				Log.e(OTP_DEMO_TAG, "OATH Counter: " + cntr.longValue());
					
			} catch (Exception e) {
				Toast.makeText(mContext, "Exception while accessing the card reader", Toast.LENGTH_LONG).show();
				Log.e(OTP_DEMO_TAG, "Exception: " + e.getMessage());
				try {
					cardChannel.close();
				} catch (Exception ex) {
				}
			} finally {
				setTimer();
			}
			break;
		}
		
		return true;
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
		finish();
		
		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode != KeyEvent.KEYCODE_MENU) {
			((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
			finish();
		}
		
		return false;
	}

	protected void onCreate(Bundle savedInstanceState) {
		// Initialize the layout
		super.onCreate(savedInstanceState);

		// remove title
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
		mContext = this;
		try {
			smartcard = new SmartcardClient(this, connectionListener);
		} catch (SecurityException e) {
			Log.e(OTP_DEMO_TAG, "Smartcard service binding not allowed");
		} catch (Exception e) {
			Log.e(OTP_DEMO_TAG, "Exception: " + e.getMessage());
		}
	}

    @Override
	protected void onDestroy() {
		if (smartcard != null) {
			smartcard.shutdown();
		}
		super.onDestroy();
	}
    
    private void refresh() {	
		otp = "";
		
		// access the applet and retrieve the OTP
		ICardChannel cardChannel = null;
        try {
        	cardChannel = smartcard.openLogicalChannel(cardReader, new byte[] {(byte)0xD2, 0x76, 0x00, 0x01, 0x18, 0x00, 0x03, (byte)0xFF, 0x49, 0x10, 0x00, (byte)0x89, 0x00, 0x00, 0x02, 0x01});
        	if (cardChannel == null)
        		throw new Exception("No basic card channel available");
        	
			byte[] response = cardChannel.transmit(new byte[] {0x00, 0x13, 0x00, 0x00, 0x00});
			cardChannel.close();

			if (response.length == 2 && response[response.length-2] == (byte)0x69 && response[response.length-1] == (byte)0x85) {
				// oath applet is not personalized yet
				otp = "n/a";
			} else {
				if (response.length != 8 || response[response.length-2] != (byte)0x90 || response[response.length-1] != 0x00)
					throw new Exception("Cannot communicate with OTP applet");

				byte[] data = new byte[6];
				System.arraycopy(response, 0, data, 0, 6);
				otp = new String(data);
				
				ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				ClipMan.setText(otp);
			}
			

			// Set the OTP
			setContentView(R.layout.main);
			TextView tv = (TextView) findViewById(R.id.otp);
			tv.setText(otp);			
			Log.v(OTP_DEMO_TAG, "OTP: " + otp);
			
			setTimer();
		} catch (Exception e) {
			Toast.makeText(mContext, "Exception while accessing the card reader", Toast.LENGTH_LONG).show();
			Log.e(OTP_DEMO_TAG, "Exception.3: " + e.getMessage());
			try {
				cardChannel.close();
			} catch (Exception ex) {
			}
			finish();
		}
    }
    
    private void setTimer() {
    	mTimer = new Timer();
    	mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				finish();
			}
		}, 30000);    	
    }
}