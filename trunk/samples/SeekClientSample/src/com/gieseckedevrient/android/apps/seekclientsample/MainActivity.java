/*
 * Copyright 2009 Giesecke & Devrient GmbH.
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

package com.gieseckedevrient.android.apps.seekclientsample;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.smartcard.service.CardException;
import android.smartcard.service.ICardChannel;
import android.smartcard.service.SeekClient;
import android.smartcard.service.SeekClient.ISeekConnectionListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final int BUMP_MSG = 1;
	
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
	
	ISeekConnectionListener connectionListener = new ISeekConnectionListener() {
		public void seekConnected() {
			messageHandler.sendMessage(messageHandler.obtainMessage(BUMP_MSG));
		}

		public void seekDisconnected() {
		}
	};
	SeekClient seek;
	
	private Handler messageHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case BUMP_MSG:
                	testSeek();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
        
    };
	
    /*
    private OnClickListener unbindListener = new OnClickListener() {
        public void onClick(View v) {
            seek.unbindService();
        }
    };
    */

	/* log content and scroll to bottom */
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

		/*
        Button button = new Button(this);
        button.setLayoutParams(new ScrollView.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setText(R.string.unbind_service);
        button.setOnClickListener(unbindListener);
        ll.addView(button);
        */
		
		tv = new TextView(this);
		tv.setLayoutParams(layoutParams);
		ll.addView(tv);

		setContentView(sv);		
        
		seek = new SeekClient(this, connectionListener);
        try {
    		seek.bindService();
		} catch (SecurityException e) {
			logText("SEEK binding not allowed");
		}
 	}
	
    @Override
	protected void onDestroy() {
		if (seek.isBound()) {
			seek.unbindService();
		}
		super.onDestroy();
	}
    
    /* Display an error dialog */
	private void showErrorAlert(String errorMessage) {
		new AlertDialog.Builder(this)
			.setTitle("Error")
			.setMessage(errorMessage)
			.setNeutralButton("Close", null)
			.show();
	}
	
	private void testSeek() {
		logText("\nSEEK is connected\n");
		
		logText("\ngetReaders()\n");
        try {
			for (String reader : seek.getReaders()) {
				logText(" " + reader + "\n");
			}
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
		
		logText("\nisCardPresent()\n");
        try {
			boolean isPresent = seek.isCardPresent("Native 0");
			logText(isPresent ? " present\n" : " absent\n");
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
		
		logText("\nopenBasicChannel()\n");
		ICardChannel basicChannel;
        try {
        	basicChannel = seek.openBasicChannel("Native 0");
			logText(" ATR: " + bytesToString(basicChannel.getAtr()) + "\n");
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
		
		logText("\ntransmit() (SELECT ISD)\n");
        try {
			byte[] response = basicChannel.transmit(new byte[] {0x00, (byte) 0xA4, 0x04, 0x00, 0x00});
			logText(" Response: " + bytesToString(response) + "\n");
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
		
		logText("\nopenLogicalChannel() (ISD)\n");
		ICardChannel logicalChannel;
        try {
        	logicalChannel = seek.openLogicalChannel("Native 0", ISD_AID);
			logText(" ATR: " + bytesToString(logicalChannel.getAtr()) + "\n");
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
		
		logText("\ntransmit() (GET CPLC)\n");
        try {
			byte[] response = logicalChannel.transmit(new byte[] {(byte) 0x80, (byte) 0xCA, (byte) 0x9F, 0x7F, 0x00});
			logText(" Response: " + bytesToString(response) + "\n");
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
		
		logText("\nclose()\n");
        try {
        	basicChannel.close();
			logText(" basic channel is closed");
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
		
		logText("\nclose()\n");
        try {
        	logicalChannel.close();
			logText(" logical channel is closed");
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
	}
}