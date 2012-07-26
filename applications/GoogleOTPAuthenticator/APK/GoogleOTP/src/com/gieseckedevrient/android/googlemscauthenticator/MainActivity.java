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

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
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
	public static final String PREFS_NAME = "OtpPreferences";
	public static final String PREFS_ACCOUNT = "SelectedAccount";

	private static final int ACCOUNTS_INTENT_ID = 0x57f2c9a2;

	private Context mContext = null;
	private Timer mTimer = null;
	public String otp = "";
	//private String account; 
	
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh").setIcon(R.drawable.menu_refresh);
		menu.add(0, 1, 1, "Accounts").setIcon(R.drawable.menu_accounts);
		menu.add(0, 2, 2, "About").setIcon(R.drawable.menu_about);

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mTimer != null)
			mTimer.cancel();
		
		switch(item.getItemId()) {
		case 0:		// REFRESH 
			refresh();
			break;
			
		case 1:		// ACCOUNTS
			startActivityForResult(new Intent(this, AccountsActivity.class), ACCOUNTS_INTENT_ID);
			break;
			
/*
 * 		case 2:		// RESET
 
			

			break;
*/
		case 2:		// ABOUT 
			this.startActivity(new Intent(this, AboutActivity.class));
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
		
		setContentView(R.layout.main);
		TextView tv = (TextView) findViewById(R.id.otp);
		tv.setText("n/a");	
	    
	    // Restore preferences
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
	    ChannelUtils.setSelectedAccount(settings.getString(PREFS_ACCOUNT, ""));	    

	    tv = (TextView) findViewById(R.id.account);
	    tv.setText(ChannelUtils.getSelectedAccount());	
		
		mContext = this;
		ChannelUtils.init(this);
	}

    @Override
	protected void onDestroy() {
    	ChannelUtils.shutdown();
		SharedPreferences.Editor settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
		settings.putString(PREFS_ACCOUNT, ChannelUtils.getSelectedAccount());
		settings.commit();
    	
		super.onDestroy();
	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ACCOUNTS_INTENT_ID ) { 
				if( resultCode == RESULT_OK) {
				String account = data.getStringExtra("account");
				if (account != null && account.length() > 0) {
					//this.account = account;
					ChannelUtils.setSelectedAccount(account);
				}
			}
			refresh();
		}
	}
    
    void refresh() {
    	
    	if (!ChannelUtils.ensureConnected()) return;
    	
		otp = "";		
		String account = ChannelUtils.getSelectedAccount();
		// access the applet and retrieve the OTP
		Channel cardChannel = null;
        try {
        	if( ChannelUtils.getOtpReader() == null || ChannelUtils.getOtpReader().length == 0 ){
        		throw new AppletNotAvailableException("");
        	}
        	
        	cardChannel = ChannelUtils.openChannelAndSelect(ChannelUtils.getReader(account));
        	
        	if( cardChannel == null ){
        		throw new Exception ("Your secure element is either not personalised yet or you don't have selected an account. "+
						"Please go to \"Accounts\" and \"Scan code\"." );
        	}

        	boolean result = ChannelUtils.selectAccount(cardChannel, account);
    		if (!result) {
    			throw new Exception( "Could not select account. " + account );
    		}
        	
        	boolean hotp = ChannelUtils.getHOTP(cardChannel);
        	
        	if (!hotp) {
        		// Calculate TOTP interval
        		long interval = ChannelUtils.getCurrentInterval();
        		ChannelUtils.setCounter(cardChannel, interval);
        	}
        	
        	// Send the command to get a PIN
			byte[] response = cardChannel.transmit(new byte[] {0x00, 0x13, 0x00, 0x00, 0x00});

			// Check if a "no account selected" error has occurred
			if (response.length == 2 && response[response.length-2] == (byte)0x69 && 
					response[response.length-1] == (byte)0xC0) {
				otp = "n/a";
				Toast.makeText(mContext, "Your secure element is not personalised yet. "+
						"Please go to \"Accounts\" and \"Scan code\".", Toast.LENGTH_LONG).show();
			} else {
				// Check for any other error codes
				if (response.length != 8 || response[response.length-2] != (byte)0x90 || 
						response[response.length-1] != 0x00)
					throw new Exception("Cannot communicate with OTP applet");

				// No errors. Read the pin from the result.
				otp = new String(response, 0, ChannelUtils.N_DIGITS);
				
				// Copy the PIN to the clipboard
				ClipboardManager clipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipMan.setText(otp);
			}
			
        } catch (AppletNotAvailableException e) {
        	Toast.makeText(mContext, "The Google Authenticator applet is not installed on the secure element.", 
        			Toast.LENGTH_LONG).show();
        	account = "";
			otp = "n/a";
		} catch (Exception e) {
			Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
			Log.e(OTP_DEMO_TAG, "Exception.3: " + e.getMessage());
			otp = "n/a";
			e.printStackTrace();
		} finally {
			try {
				if( cardChannel != null ){
					cardChannel.close();
				}
			} catch (Exception ex) {
			}
		}
        
		// Show the pin on the TextView
		setContentView(R.layout.main);
		TextView tv = (TextView) findViewById(R.id.otp);
		tv.setText(otp);	
		Log.v(OTP_DEMO_TAG, "OTP: " + otp);
		
		tv = (TextView) findViewById(R.id.account);
		tv.setText(account == null ? "" : account);	

		setTimer();
        
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