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

package android.smartcard.libraries.smartcard;


import android.smartcard.ATR;
import android.smartcard.CardException;
import android.smartcard.libraries.smartcard.pcsc.PcscException;
import android.smartcard.libraries.smartcard.pcsc.PcscJni;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.Disposition;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.Protocol;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.ReaderState;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.ShareMode;

/**
 * PC/SC SEEK card terminal implementation.
 */
final class PcscCardTerminal extends SeekCardTerminal {

	private final long contextHandle;

	/**
	 * Constructs a new PC/SC SEEK card terminal instance.
	 * @param contextHandle
	 *         the PC/SC context handle.
	 * @param name
	 *         the friendly name of the terminal.
	 */
	PcscCardTerminal(long contextHandle, String name) {
		super(name);
		this.contextHandle = contextHandle;
	}

	/**
	 * Returns the PC/SC protocol identifier for the specified protocol name.
	 * @param protocol
	 *          the protocol name.
	 * @return the PC/SC protocol identifier for the specified protocol name.
	 */
	private int getProtocolId(String protocol) {
		if (protocol.equals("*"))
			return Protocol.T0 | Protocol.T1;
		if (protocol.equals("T=0"))
			return Protocol.T0;
		if (protocol.equals("T=1"))
			return Protocol.T1;
		throw new IllegalArgumentException("protocol " + protocol + " invalid or not supported");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	SeekCard internalConnect(String protocol) throws CardException {
		int protocolId = getProtocolId(protocol);
		long cardHandle = 0;
		int currentProtocolId = 0;
		try {
			int[] protocolIdParam = new int[] { protocolId };
			cardHandle = PcscJni.connect(contextHandle, name, ShareMode.Shared, protocolIdParam);
			currentProtocolId = protocolIdParam[0];
		} catch (PcscException e) {
			throw PcscCard.createCardException(e, "card connect failed");
		}

		try {
			ATR atr = new ATR(PcscJni.status(cardHandle, null, null));
			return new PcscCard(cardHandle, currentProtocolId, atr);
		} catch (PcscException e) {
			try {
				PcscJni.disconnect(cardHandle, Disposition.Leave);
			} catch (Exception ignore) {
			}
			throw PcscCard.createCardException(e, "ATR not available");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalWaitForState(long timeout, boolean present) throws CardException {
		try {
			int[] status = new int[] { ReaderState.Unaware, 0 };
			PcscJni.getStatusChange(contextHandle, 0, name, status);
			boolean isPresent = ((status[0] & ReaderState.Present) == ReaderState.Present);
			if (timeout < 0)
				return (isPresent == present);
			if (isPresent == present)
				return true;

			long currentTime = System.currentTimeMillis();
			long exitTime = timeout + currentTime;
			long pcscTimeout = -1;
			while ((currentTime < exitTime) || (timeout == 0)) {
				if (timeout != 0)
					pcscTimeout = exitTime - currentTime;
				PcscJni.getStatusChange(contextHandle, pcscTimeout, name, status);
				isPresent = ((status[0] & ReaderState.Present) == ReaderState.Present);
				if (isPresent == present)
					return true;
				currentTime = System.currentTimeMillis();
			}

			return false;
		} catch (PcscException e) {
			throw new CardException("card status operation failed", e);
		}
	}

}
