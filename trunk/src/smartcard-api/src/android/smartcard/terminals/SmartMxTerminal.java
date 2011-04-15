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

package android.smartcard.terminals;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

import android.content.Context;
import android.smartcard.CardException;
import android.smartcard.Terminal;
import android.os.ServiceManager;
import android.os.SystemProperties;

import android.nfc.NfcAdapter;
import android.nfc.NfcSecureElement;

public class SmartMxTerminal extends Terminal {
	
	private NfcSecureElement se;
	private int handle = 0; 


	public SmartMxTerminal(Context context) {
		super("SmartMX", context);
	}

	public boolean isCardPresent() throws CardException {
//		int nfcState = Settings.System.getInt(context.getContentResolver(),
//				Settings.System.NFC_ON, 0);
//		if (nfcState != 0)
//			return true;
//		return false;
		return true;
	}
	
	@Override
	protected void internalConnect() throws CardException {
		
		se = NfcAdapter.getDefaultAdapter()
		        .createNfcSecureElementConnection();

		try {
			handle = se.openSecureElementConnection("SmartMX");
		} catch (IOException e) {
			handle = 0;
		}

		if(handle == 0)
			throw new CardException("open SE failed");

		isConnected = true;
	}
	
	@Override
	protected void internalDisconnect() throws CardException {

		try {
			se.closeSecureElementConnection(handle);
		} catch (IOException e) {
			throw new CardException("close SE failed");
		}
	}
	
	@Override
	protected byte[] internalTransmit(byte[] command) throws CardException {
		try {
			return se.exchangeAPDU(handle, command);
		} catch (IOException e) {
			throw new CardException("exchange APDU failed");
		}
	}

	@Override
	protected int internalOpenLogicalChannel() throws Exception {

		byte[] manageChannelCommand = new byte[] { 0x00, 0x70, 0x00, 0x00, 0x01 };
		byte[] rsp = transmit(manageChannelCommand, 2, 0x9000, 0, "MANAGE CHANNEL");
                if ((rsp.length == 2) && ((rsp[0] == (byte)0x68) && (rsp[1] == (byte)0x81)))
                        throw new NoSuchElementException("logical channels not supported");
		if (rsp.length == 2 && (rsp[0] == (byte)0x6A && rsp[1] == (byte)0x81))
			throw new MissingResourceException("no free channel available", "", "");
		if (rsp.length != 3)
			throw new MissingResourceException("unsupported MANAGE CHANNEL response data", "", "");
		int channelNumber = rsp[0] & 0xFF;
		if (channelNumber == 0 || channelNumber > 19)
			throw new MissingResourceException("invalid logical channel number returned", "", "");

		return channelNumber;
	}
	
	@Override
	protected int internalOpenLogicalChannel(byte[] aid) throws Exception
	{	
		if(aid == null)
			throw new NullPointerException("aid must not be null");

		byte[] manageChannelCommand = new byte[] { 0x00, 0x70, 0x00, 0x00, 0x01 };
		byte[] rsp = transmit(manageChannelCommand, 2, 0x9000, 0, "MANAGE CHANNEL");
                if ((rsp.length == 2) && ((rsp[0] == (byte)0x68) && (rsp[1] == (byte)0x81)))
                        throw new NoSuchElementException("logical channels not supported");
		if (rsp.length == 2 && (rsp[0] == (byte)0x6A && rsp[1] == (byte)0x81))
			throw new MissingResourceException("no free channel available", "", "");
		if (rsp.length != 3)
			throw new MissingResourceException("unsupported MANAGE CHANNEL response data", "", "");
		int channelNumber = rsp[0] & 0xFF;
		if (channelNumber == 0 || channelNumber > 19)
			throw new MissingResourceException("invalid logical channel number returned", "", "");

		byte[] selectCommand = new byte[aid.length + 6];
		selectCommand[0] = (byte) channelNumber;
		if (channelNumber > 3)
			selectCommand[0] |= 0x40;
		selectCommand[1] = (byte) 0xA4;
		selectCommand[2] = 0x04;
		selectCommand[4] = (byte) aid.length;
		System.arraycopy(aid, 0, selectCommand, 5, aid.length);
		try
		{
			transmit(selectCommand, 2, 0x9000, 0xFFFF, "SELECT");
		}
		catch(CardException exp)
		{
			internalCloseLogicalChannel(channelNumber);
			throw new NoSuchElementException(exp.getMessage());
		}

		return channelNumber;
	}
	
	@Override
	protected void internalCloseLogicalChannel(int channelNumber) throws CardException {
		if (channelNumber > 0) {
			byte cla = (byte) channelNumber;
			if (channelNumber > 3) {
				cla |= 0x40;
			}
			byte[] manageChannelClose = new byte[] { cla, 0x70, (byte) 0x80, (byte) channelNumber };
			transmit(manageChannelClose, 2, 0x9000, 0xFFFF, "MANAGE CHANNEL");
		}
	}
}
