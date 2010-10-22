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

package android.smartcard;

import android.content.Context;
import android.smartcard.msc.MscJni;
import android.smartcard.msc.MscJni.OpenMode;

/**
 * MSC based channel implementation.
 */
final class MscTerminal extends Terminal {
	
	private volatile int fd;

	MscTerminal(Context context) {
		super("Mobile Security Card", context);
	}
	
	@Override
	protected void internalConnect() throws CardException {
		if (fd == 0) {
			try {
				fd = MscJni.open(OpenMode.Exclusive);
			} catch (Exception e) {
				throw new CardException("Card connect failed");
			}
		}

		try {
			byte[] response = MscJni.transmit(fd, new byte[] { 0x20, 0x12, 0x01, 0x01, 0x00 });
			if (response.length < 2) {
				internalDisconnect();
				throw new CardException("Card ATR not available");
			}
			//Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " MSC connected");
		} catch (Exception e) {
			internalDisconnect();
			throw new CardException("Card ATR not available");
		}
		isConnected = true;
	}
	
	@Override
	protected void internalDisconnect() throws CardException {
		try {
			MscJni.close(fd);
		} catch (Exception ignore) {
		} finally {
			fd = 0;
			isConnected = false;
			//Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " MSC disconnected");
		}
	}
	
	public boolean isCardPresent() throws CardException {
		if (fd == 0) {
			try {
				fd = MscJni.open(OpenMode.Exclusive);
			} catch (Exception e) {
				return false;
			}
		}
		
		try {
			byte[] response = MscJni.transmit(fd, new byte[] { 0x20, 0x13, 0x00, (byte)0x80, 0x00 });
			if (response.length == 0x05 && response[response.length - 2] == (byte)0x90 && response[response.length - 1] == 0x00) {
				return true;
			}
		} catch (Exception e) {
		}
		
		return false;
	}
	
	@Override
	protected byte[] internalTransmit(byte[] command) throws CardException {
		try {
			byte[] response = MscJni.transmit(fd, command);
			return response;
		} catch (Exception e) {
			throw new CardException(e);
		}
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

	@Override
	protected int internalOpenLogicalChannel(byte[] aid) throws CardException {
		byte[] manageChannelCommand = new byte[] { 0x00, 0x70, 0x00, 0x00, 0x01 };
		byte[] rsp = transmit(manageChannelCommand, 3, 0x9000, 0xFFFF, "MANAGE CHANNEL");
		if (rsp.length != 3)
			throw new CardException("unsupported MANAGE CHANNEL response data");
		int channelNumber = rsp[0] & 0xFF;
		if (channelNumber == 0 || channelNumber > 19)
			throw new CardException("invalid logical channel number returned");

		byte[] selectCommand = new byte[aid.length + 6];
		selectCommand[0] = (byte) channelNumber;
		if (channelNumber > 3)
			selectCommand[0] |= 0x40;
		selectCommand[1] = (byte) 0xA4;
		selectCommand[2] = 0x04;
		selectCommand[4] = (byte) aid.length;
		System.arraycopy(aid, 0, selectCommand, 5, aid.length);
		transmit(selectCommand, 2, 0x9000, 0xFFFF, "SELECT");
	
		return channelNumber;
	}
}
