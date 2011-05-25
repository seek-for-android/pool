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
 * 
 *
 * This code is partially based on code from the GoogleAuthenticator,
 * (http://code.google.com/p/google-authenticator/) which is licenced
 * under the Apache Licence 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
 * 
 */

package com.gieseckedevrient.android.googlemscauthenticator;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String OTP_DEMO_TAG = "OtpCalculator";

	private static final int SCAN_INTENT_ID = 0x6034f621;
	private static final int INTERVAL_LENGTH = 30;
	private static final int N_DIGITS = 6;
	private static final int TOTP = 0;
	private static final int HOTP = 1;
	
	private Context mContext = null;
	private Timer mTimer = null;
	private String cardReader = "Mobile Security Card";
	public static String otp = "";
	
	private int disconnects = 0;
	private boolean connected = false;
	
	ISmartcardConnectionListener connectionListener = new ISmartcardConnectionListener() {
		public void serviceConnected() {
	        try {
	        	String[] reader = smartcard.getReaders(); 
				cardReader = reader[0];
			} catch (Exception e) {
				Toast.makeText(mContext, "Could not access the card reader: " + e, Toast.LENGTH_LONG).show();
				finish();
				return;
			}
			
			// ensure a card is present
	        try {
				if (!smartcard.isCardPresent(cardReader)) {
					Toast.makeText(mContext, "No smartcard in reader or card in use.", Toast.LENGTH_LONG).show();
					finish();
					return;
				}
			} catch (Exception e) {
				Toast.makeText(mContext, "Could not access the card reader: " + e, Toast.LENGTH_LONG).show();
				Log.e(OTP_DEMO_TAG, "Exception.2: " + e.getMessage());
				finish();
				return;
			}
			
			connected = true;

			refresh();
		}

		public void serviceDisconnected() {
			disconnects++;
			connected = false;
			if (disconnects <= 5)
				smartcard = new SmartcardClient(mContext, connectionListener);
		}
	};
	SmartcardClient smartcard;

	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh").setIcon(R.drawable.menu_refresh);
		menu.add(0, 1, 1, "Scan code").setIcon(R.drawable.menu_scan);
		menu.add(0, 2, 2, "Reset").setIcon(R.drawable.menu_personalize);
		menu.add(0, 3, 3, "About").setIcon(R.drawable.menu_about);

		return true;
	}
	
	private void askYesNo(String message, DialogInterface.OnClickListener yesListener, 
			DialogInterface.OnClickListener noListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("Yes", yesListener);
		builder.setNegativeButton("No", noListener);
		builder.show();
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		mTimer.cancel();
		
		switch(item.getItemId()) {
		case 0:		// REFRESH 
			refresh();
			break;
			
		case 1: 	// SCAN CODE
			scanCode();			
			break;
			
		case 2:		// RESET
			
			if (!ensureConnected()) return true;
			
			DialogInterface.OnClickListener yesListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					reset();
				}
			};
			
			askYesNo("Are you sure you want to reset your smartcard personalisation for Google OTP?",
					yesListener, null);
			break;

		case 3:		// ABOUT 
			this.startActivity(new Intent(this, AboutActivity.class));
			break;
		}
		
		return true;
	}
	
	// Deletes card personalisation (secret key and counter)
	private void reset() {
		try {
			
			ICardChannel channel = openChannelAndSelect();
			
			byte[] cmd = new byte[] {0x00, 0x14, 0x00, 0x00, 0x00};
			byte[] response = channel.transmit(cmd);	
			channel.close();
			
			if (response.length != 2 || response[0] != (byte) 0x90 || response[1] != 0x00)
				throw new Exception("Error during reset.");
			
			ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipMan.setText("n/a");
			setContentView(R.layout.main);
			TextView tv = (TextView) findViewById(R.id.otp);
			tv.setText("n/a");	
			
		} catch (Exception e) {
			Log.w(OTP_DEMO_TAG, "Error during reset: " + e);
    		Toast.makeText(mContext, "Could not reset: " + e, Toast.LENGTH_LONG).show();
    		e.printStackTrace();
		}
	}
	
	// Opens the barcode scanner in order to scan the personalisation code
	private void scanCode() {
		if (!ensureConnected()) return;
    	Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        intent.putExtra("SAVE_HISTORY", false);
        startActivityForResult(intent, SCAN_INTENT_ID);
	}
	
	
	// This is called when the barcode scanner has finished. If it was successful,
	// the secret will be read from the URI in the barcode.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	if (requestCode == SCAN_INTENT_ID && resultCode == Activity.RESULT_OK) {
    		String result = intent.getStringExtra("SCAN_RESULT");
    		try {
    			Uri uri = Uri.parse(result);
    			parseAndSaveSecret(uri);
    		} catch (IllegalArgumentException e) {
    			Toast.makeText(mContext, "Invalid QR code. Please ensure the code you scanned is a valid OTP secret code.", 
    					Toast.LENGTH_LONG).show();
    		}
    	}
    }
	
	// Parses a personalisation URI and saves the secret (and the counter,  if HOTP is used)
    // to the smartcard.
	private void parseAndSaveSecret(Uri uri) {
    	
    	String scheme = uri.getScheme();
    	
    	if (scheme == null) {
    		Log.w(OTP_DEMO_TAG, "QR code is not a URI.");
    		throw new IllegalArgumentException("Invalid QR code: Code is not a URI");
    	}
    	
    	String authority = uri.getAuthority();
    	String user = null, secret = null;
    	int type = TOTP;
    	long counter = 0;
    	
    	if (scheme.equalsIgnoreCase("otpauth")) {
    		
    		if (authority != null && authority.equalsIgnoreCase("totp")) {
    	        type = TOTP; // TOTP
    	      } else if (authority != null && authority.equalsIgnoreCase("hotp")) {
    	        type = HOTP; // HOTP
    	        String counterParameter = uri.getQueryParameter("counter");
    	        if (counterParameter != null) {
    	          counter = Long.parseLong(counterParameter);
    	        }
    	      }
    	      
    		  user = uri.getPath();
    	      if (user != null && user.length() > 1) {
    	        user = user.substring(1); // path is "/user", so remove leading /
    	      }
    	      
    	      secret = uri.getQueryParameter("secret");
    		
    	} else if (scheme.equalsIgnoreCase("totp")) {
    		
    		if (authority != null) {
    	        user = authority;
    		}
    	    secret = uri.getFragment();
    		
    	} else {
    		
    		user = uri.getQueryParameter("user");
    	    secret = uri.getFragment();
    	    
    	}
    	
    	if (user == null) user = "Default account";
    	
    	if (secret == null || secret.length() == 0) {
    		Log.w(OTP_DEMO_TAG, "Secret not found in URI.");
    		throw new IllegalArgumentException("Invalid QR code: No secret in code.");  
    	}
    	
    	handleSecret(Base32Utils.base32ToByteArray(secret, 20), counter, type);
    		
    }
	
	private boolean getHOTP(ICardChannel channel) throws Exception {
		
		byte[] cmd = new byte[] {0x00, 0x15, 0x00, 0x00, 0x00};
		byte[] response = channel.transmit(cmd);
		
		if (response.length != 3 || response[1] != (byte) 0x90 || response[2] != 0x00)
			return false;
		
		return response[0] != 0;
	}
	
	// Sets the counter on the smartcard to the specified value.
	private void setCounter(ICardChannel channel, long counter) throws Exception {
		
		// The APDU to be sent
		byte[] cmd = new byte[] {0x00, 0x10, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
		
		// Write counter to APDU
		for (int i = 0; i < 8; i++) {
			cmd[12 - i] = (byte) (counter & 0xFF);
			counter >>= 8;
		}
		
		// Transmit APDU and check if the card returns success (0x9000)
		byte[] response = channel.transmit(cmd);
		if (response.length != 2 || response[0] != (byte) 0x90 || response[1] != 0x00)
			throw new Exception("Could not set counter.");
		
	}
	
	// Writes a secret to the smartcard
	private void personalize(ICardChannel channel, byte[] secret, int type) throws Exception {
		
		byte hotp = (type == HOTP) ? (byte) 1 : (byte) 0;
		
		byte[] cmd = new byte[] {0x00, 0x12, (byte) (N_DIGITS & 0xFF), hotp, 0x14, 0x00, 0x00, 0x00, 0x00, 0x00,   
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

		for (int i = 0; i < 20; i++) {
			cmd[5 + i] = secret[i];
		}
		
		byte[] response = channel.transmit(cmd);
		if (response.length != 2 || response[0] != (byte) 0x90 || response[1] != 0x00)
			throw new Exception("Could not set secret.");
		
	}
	
	// Writes a secret and (when using HOTP) the counter to the smartcard
	private void handleSecret(byte[] secret, long counter, int type) {
		try {
		
			if (!ensureConnected()) return;
			
			ICardChannel channel = openChannelAndSelect();
			if (type == HOTP) setCounter(channel, counter);
			
			personalize(channel, secret, type);
			channel.close();
			refresh();
			
		} catch (Exception e) {
			Log.w(OTP_DEMO_TAG, "Exception during handleSecret()");
			e.printStackTrace();
    		Toast.makeText(mContext, "Could not save secret: " + e, Toast.LENGTH_LONG).show();
		}
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
		
		setContentView(R.layout.main);
		TextView tv = (TextView) findViewById(R.id.otp);
		tv.setText("n/a");	
	    
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
    
    private long getCurrentInterval() {
    	long currentTimeSeconds = System.currentTimeMillis() / 1000;
    	return currentTimeSeconds / INTERVAL_LENGTH;
    }
    
    private boolean ensureConnected() {
    	if (!connected) 
    		Toast.makeText(mContext, "Could not establish connection to the smartcard service. "+
    				"Please try again.", Toast.LENGTH_LONG).show();
    	return connected;
    }
    
    private void refresh() {
    	
    	if (!ensureConnected()) return;
    	
		otp = "";
		
		// access the applet and retrieve the OTP
		ICardChannel cardChannel = null;
        try {
        	cardChannel = openChannelAndSelect();
        	
        	boolean hotp = getHOTP(cardChannel);
        	
        	if (!hotp) {
        		// Calculate TOTP interval
        		long interval = getCurrentInterval();
        		setCounter(cardChannel, interval);
        	}
        	
        	// Send the command to get a PIN
			byte[] response = cardChannel.transmit(new byte[] {0x00, 0x13, 0x00, 0x00, 0x00});
			cardChannel.close();

			// Check if the applet returned an error code saying it was not personalised yet
			if (response.length == 2 && response[response.length-2] == (byte)0x69 && 
					response[response.length-1] == (byte)0x85) {
				// Yes. There is no secret stored on the card yet.
				otp = "n/a";
				Toast.makeText(mContext, "Your smartcard is not personalised yet. "+
						"Please select \"Scan code\" from the menu.", Toast.LENGTH_LONG).show();
			} else {
				// Check for any other error codes
				if (response.length != 8 || response[response.length-2] != (byte)0x90 || 
						response[response.length-1] != 0x00)
					throw new Exception("Cannot communicate with OTP applet");

				// No errors. Read the pin from the result.
				byte[] data = new byte[N_DIGITS];
				System.arraycopy(response, 0, data, 0, N_DIGITS);
				otp = new String(data);
				
				// Copy the PIN to the clipboard
				ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				ClipMan.setText(otp);
			}
			

			// Show the pin on the TextView
			setContentView(R.layout.main);
			TextView tv = (TextView) findViewById(R.id.otp);
			tv.setText(otp);			
			Log.v(OTP_DEMO_TAG, "OTP: " + otp);
			
			setTimer();
			
        } catch (AppletNotAvailableException e) {
        	Toast.makeText(mContext, "The Google MSC Authenticator applet is not installed on the smartcard.", 
        			Toast.LENGTH_LONG).show();
			try {
				cardChannel.close();
			} catch (Exception ex) {
			}
        	finish();
		} catch (Exception e) {
			Toast.makeText(mContext, "Could not access card reader: " + e.getMessage(), Toast.LENGTH_LONG).show();
			Log.e(OTP_DEMO_TAG, "Exception.3: " + e.getMessage());
			e.printStackTrace();
			try {
				cardChannel.close();
			} catch (Exception ex) {
			}
			finish();
		}
    }
    
    private ICardChannel openChannelAndSelect() throws Exception {
    	ICardChannel cardChannel = smartcard.openBasicChannel(cardReader, new byte[] {(byte)0xD2, 0x76, 0x00, 0x01, 0x18, 0x00, 0x03, (byte)0xFF, 0x49, 0x10, 0x00, (byte)0x89, 0x00, 0x00, 0x02, 0x01});
    	
    	if (cardChannel == null)
    		throw new Exception("No basic card channel available");
    	
    	return cardChannel;
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