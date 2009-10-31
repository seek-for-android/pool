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

package com.gieseckedevrient.android.apps.smartcardsample;

import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.smartcard.Card;
import android.smartcard.CardChannel;
import android.smartcard.CardException;
import android.smartcard.CardTerminal;
import android.smartcard.CardTerminals;
import android.smartcard.CommandAPDU;
import android.smartcard.ResponseAPDU;
import android.smartcard.TerminalFactory;
import android.smartcard.CardTerminals.State;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView tv = null;
	ScrollView sv = null;

   	
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

		

		logText("TerminalFactory");
        TerminalFactory terminalFactory = TerminalFactory.getDefault();
        logText("\n default factory: " + terminalFactory.getType());
        try {
			terminalFactory = TerminalFactory.getInstance(TerminalFactory.NATIVE_TYPE, null);
		} catch (NoSuchAlgorithmException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
        logText("\n native factory: " + terminalFactory.getType());

        
        CardTerminals cardTerminals = terminalFactory.terminals();
        logText("\n\nTerminals (all):\n");
        try {
			for (CardTerminal string: cardTerminals.list()) {
				logText(" " + string.getName() + "\n");
			}
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}

		logText("Terminals (card present):\n");
        try {
			for (CardTerminal string: cardTerminals.list(State.CARD_PRESENT)) {
				logText(" " + string.getName() + "\n");
			}
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}

		logText("Terminals (card absent):\n");
        try {
			for (CardTerminal string: cardTerminals.list(State.CARD_ABSENT)) {
				logText(" " + string.getName() + "\n");
			}
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
		
		
		logText("\nCardTerminal");
		CardTerminal cardTerminal = null;
		try {
			cardTerminal = cardTerminals.list().get(0);
			if (cardTerminal.isCardPresent() == false) {
				showErrorAlert("Card not present - please insert and restart the application");
				return;
			}
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
		logText(" - ok\n");
		
		
		Card card;
		logText("\nCard - connect\n");
		try {
			card = cardTerminal.connect("*");
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
		logText(" protocol: " + card.getProtocol() + "\n ATR: " + card.getATR() + "\n");
		
		
		logText("\nCardChannel - transmit");
		CardChannel channel = card.getBasicChannel();
		try {
			CommandAPDU cmd = new CommandAPDU(new byte[] { 0x00, (byte) 0xA4, 0x04, 0x00, 0x00 });
			logText("\n channel number: " + channel.getChannelNumber());
			logText("\n command APDU:  " + cmd.toString());
			ResponseAPDU resp = channel.transmit(cmd);
			logText("\n response APDU: " + resp.toString());
			if (resp.getSW() != 0x9000) {
				showErrorAlert("Response APDU status word not 90:00 - no Java Card, no ISD selectable?");
			}
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
		}
		
		
		logText("\nCard - disconnect - ");
		try {
			card.disconnect(false);
		} catch (CardException e) {
			showErrorAlert(e.getMessage());
			logText(e.getMessage());
			return;
		}
		logText("ok");
 	}
}