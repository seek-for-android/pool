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

package com.gieseckedevrient.android.apps.pcscsample;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.smartcard.libraries.smartcard.pcsc.PcscException;
import android.smartcard.libraries.smartcard.pcsc.PcscJni;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.Disposition;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.Protocol;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.ReaderState;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.Scope;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.ShareMode;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.Status;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView tv = null;
	ScrollView sv = null;
	
   	long context = 0;
	String reader = "";

   	
	/* Display an error dialog */
	private void showErrorAlert(String errorMessage) {
		new AlertDialog.Builder(this)
			.setTitle("Error")
			.setMessage(errorMessage)
			.setNeutralButton("Close", null)
			.show();
	}
	
	/* log content and scroll to bottom */
	private void logText(String message) {
		sv.post(new Runnable() {
			public void run() {
				sv.fullScroll(ScrollView.FOCUS_DOWN);
			}

		});
		tv.append(message);
	}

	private void shutdown(Exception ex) {
		logText("PcscException: " + ex.getMessage() + "\n");
		showErrorAlert(ex.getMessage());
		try {
			PcscJni.releaseContext(context);
		} catch (PcscException e) {
			e.printStackTrace();
		}		
	}
	
	private String status(int state) {
		if ((state & ReaderState.Empty) == ReaderState.Empty)
			return "absent";
			
		if ((state & ReaderState.Present) == ReaderState.Present)
			return "present";
			
		if ((state & ReaderState.Exclusive) == ReaderState.Exclusive)
			return "exclusive";
			
		return "unknown";
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

		
        logText("PC/SC Test Application\n---\n");

    	/* SCardEstablishContext */
    	logText("\nSCardEstablishContext: ");
     	try {
    		context = PcscJni.establishContext(Scope.User);
        	logText("ok: " + context + "\n");
    	} catch (PcscException ex) {
    		logText("PcscException: " + ex.getMessage() + "\n");
    		showErrorAlert(ex.getMessage());
    		return;
    	}
    	
    	
       	/* SCardListReaders */
    	logText("\nSCardListReaders: ");
    	try {
            String[] terminals = PcscJni.listReaders(context, null);
            for (String terminal: terminals)
            {
            	logText("\n - " + terminal);
            }
    		logText("\n");
    		if (terminals.length > 0) {
    			reader = terminals[0];
    		}
    	} catch (PcscException ex) {
    		shutdown(ex);
    		return;
    	}

    	
		/* SCardGetStatusChange */
       	logText("\nSCardGetStatusChange:\n");
        int[] status = new int[] { Status.Unknown, 0 };
        byte[] atr;
		try {
			atr = PcscJni.getStatusChange(context, 0, reader, status);
		} catch (PcscException ex) {
			shutdown(ex);
			return;
		}
		StringBuffer string = new StringBuffer();
		for (int i = 0; atr != null && i < atr.length; ++i)
			string.append(Integer.toHexString(0x0100 + (atr[i] & 0x00FF)).substring(1));
		logText("ATR: " + string.toString() + "\n");
		logText("Status: " + status(status[0]) + "\n");

		
        /* SCardConnect */
		logText("\nSCardConnect: ");
		long card = 0;
        int[] protocol = new int[] { Protocol.T0 | Protocol.T1 };
		try {
			card = PcscJni.connect(context, reader, ShareMode.Shared, protocol);
			logText("ok - protocol: " + ((protocol[0] == Protocol.T0) ? "T=0" : "T=1") + "\n");
		} catch (PcscException ex) {
			shutdown(ex);
			return;
		}

    	
		/* SCardTransmit */
		logText("\nSCardTransmit: ");
		try {
	        byte[] response = PcscJni.transmit(card, protocol[0], new byte[] { 0x00, (byte) 0xA4, 0x04, 0x00, 0x00 });

			logText("\n-> a0a4040000\n");
			string = new StringBuffer();
			for (int i = 0; response != null && i < response.length; ++i)
				string.append(Integer.toHexString(0x0100 + (response[i] & 0x00FF)).substring(1));
			logText("<- " + string.toString() + "\n");
		} catch (PcscException ex) {
			shutdown(ex);
			return;
		}

		
		/* SCardDisconnect */
		logText("\nSCardDisconnect()\n"); 
		try {
			// don't do Disposition.Reset or card is unpowered!
			PcscJni.disconnect(card, Disposition.Leave);
		} catch (Exception ex) {
			showErrorAlert("Exception - " + ex.getMessage() + "\n");
			return;
		}

		
		/* SCardReleaseContext */
		logText("\nSCardReleaseContext: ");
		try {
    	PcscJni.releaseContext(context);
			logText("ok\n");
		} catch (PcscException ex) {
			logText("PcscException: " + ex.getMessage() + "\n");
			showErrorAlert(ex.getMessage());
		}
	}
}