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

package android.smartcard.service;

import android.smartcard.libraries.smartcard.mcex.McexJni;
import android.smartcard.libraries.smartcard.mcex.McexJni.OpenMode;
import android.util.Log;

/**
 * SEEK service MCEX terminal implementation.
 */
final class NativeTerminal extends Terminal {
	
	private volatile int fd;
	
	NativeTerminal(String name) {
		super(name);
	}
	
	@Override
	protected void internalConnect() throws CardException {
		try {
			fd = McexJni.open(OpenMode.Exclusive);
		} catch (Exception e) {
			throw new CardException("card connect failed");
		}

		try {
			byte[] response = McexJni.transmit(fd, new byte[] { 0x20, 0x12, 0x01, 0x01, 0x00 });
			if (response.length < 2) {
				internalDisconnect();
				throw new CardException("ATR not available");
			}
			atr = new byte[response.length - 2];
			System.arraycopy(response, 0, atr, 0, atr.length);
			Log.v(SeekService.SEEK_SERVICE_TAG, Thread.currentThread().getName() + " MCEX 0 connected");
		} catch (Exception e) {
			internalDisconnect();
			throw new CardException("ATR not available");
		}
		isConnected = true;
	}
	
	@Override
	protected void internalDisconnect() throws CardException {
		try {
			McexJni.close(fd);
		} catch (Exception ignore) {
		} finally {
			fd = 0;
			atr = null;
			isConnected = false;
			Log.v(SeekService.SEEK_SERVICE_TAG, Thread.currentThread().getName() + " MCEX 0 disconnected");
		}
	}
	
	@Override
	protected Channel createChannel(int channelNumber, ISeekServiceCallback callback) {
		return new NativeChannel(this, channelNumber, callback);
	}

	@Override
	public boolean isCardPresent() throws CardException {
		try {
			int stat = McexJni.stat();
			return (stat == 0);
		} catch (Exception e) {
			throw new CardException(e);
		}
	}
	
	@Override
	protected byte[] internalTransmit(byte[] command) throws CardException {
		try {
			byte[] response = McexJni.transmit(fd, command);
			return response;
		} catch (Exception e) {
			throw new CardException(e);
		}
	}
}
