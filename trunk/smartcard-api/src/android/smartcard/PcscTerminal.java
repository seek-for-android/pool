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

import android.smartcard.pcsc.PcscException;
import android.smartcard.pcsc.PcscJni;
import android.util.Log;

/**
 * PC/SC daemon channel implementation.
 */
final class PcscTerminal extends Terminal {
	
	private long contextHandle = 0;
	private long cardHandle = 0;
	private int protocolId = 0;
	
	PcscTerminal(String name,  long contextHandle) {
		super(name);
		this.contextHandle = contextHandle; 
	}
	
	@Override
	protected void internalConnect() throws CardException {
		//TODO: protocol handler - share mode - ...
		try {
			int[] protocolIdParam = new int[] { getProtocolId("*") };
			cardHandle = PcscJni.connect(contextHandle, name, PcscJni.ShareMode.Shared, protocolIdParam);
			protocolId = protocolIdParam[0];
		} catch (PcscException e) {
			internalDisconnect();
			throw new CardException("card connect failed");
		}

		try {
			atr = PcscJni.status(cardHandle, null, null);
			Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " PCSC connected");
		} catch (PcscException e) {
			internalDisconnect();
			throw new CardException("ATR not available");
		}
		isConnected = true;
	}
	
	@Override
	protected void internalDisconnect() throws CardException {
		try {
			PcscJni.disconnect(cardHandle, PcscJni.Disposition.Leave);
		} catch (Exception ignore) {
		} finally {
			cardHandle = 0;
			atr = null;
			isConnected = false;
			Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " PCSC disconnected");
		}
	}
	
	@Override
	protected Channel createChannel(int channelNumber, ISmartcardServiceCallback callback) {
		return new PcscChannel(this, channelNumber, callback);
	}

	public boolean isCardPresent() throws CardException {
		try {
			int[] status = new int[] { PcscJni.ReaderState.Unaware, 0 };
			PcscJni.getStatusChange(contextHandle, 0, name, status);
			return ((status[0] & PcscJni.ReaderState.Present) == PcscJni.ReaderState.Present);
		} catch (Exception e) {
			throw new CardException(e);
		}
	}
	
	@Override
	protected byte[] internalTransmit(byte[] command) throws CardException {
		try {
			byte[] response = PcscJni.transmit(cardHandle, protocolId, command);
			return response;
		} catch (PcscException e) {
			throw new CardException("transmit failed", e);
		}
	}

	/**
	 * Returns the PC/SC protocol identifier for the specified protocol name.
	 * @param protocol
	 *          the protocol name.
	 * @return the PC/SC protocol identifier for the specified protocol name.
	 */
	private int getProtocolId(String protocol) {
		if (protocol.equals("*"))
			return PcscJni.Protocol.T0 | PcscJni.Protocol.T1;
		if (protocol.equals("T=0"))
			return PcscJni.Protocol.T0;
		if (protocol.equals("T=1"))
			return PcscJni.Protocol.T1;
		throw new IllegalArgumentException("protocol " + protocol + " invalid or not supported");
	}
}
