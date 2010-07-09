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
import android.smartcard.libraries.smartcard.mcex.McexException;
import android.smartcard.libraries.smartcard.mcex.McexJni;
import android.smartcard.libraries.smartcard.mcex.McexJni.OpenMode;

/**
 * Native SEEK card terminal implementation which supports a single MCEX device.
 */
final class McexCardTerminal extends SeekCardTerminal {

	/**
	 * Constructs a new MCEX SEEK card terminal instance.
	 * @param name
	 *         the friendly name of the terminal.
	 */
	McexCardTerminal(String name) {
		super(name);
	}

	/**
	 * Returns the name of the assigned protocol.
	 * @param protocol
	 *         the requested protocol.
	 * @return the name of the assigned protocol.
	 */
	private String assignProtocol(String protocol) {
		if (protocol.equals("*"))
			return "T=1";
		if (protocol.equals("T=0"))
			return "T=0";
		if (protocol.equals("T=1"))
			return "T=1";
		throw new IllegalArgumentException("protocol " + protocol + " invalid or not supported");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	SeekCard internalConnect(String protocol) throws CardException {
		String flashProtocol = assignProtocol(protocol);
		int fd = 0;
		try {
			fd = McexJni.open(OpenMode.Shared);
		} catch (McexException e) {
			throw McexCard.createCardException(e, "card connect failed");
		}

		try {
			byte[] response = McexJni.transmit(fd, new byte[] { 0x20, 0x12, 0x01, 0x01, 0x00 });
			if (response.length < 2) {
				try {
					McexJni.close(fd);
				} catch (Exception ignore) {
				}
				throw new CardException("ATR not available");
			}
			byte[] atrBytes = new byte[response.length - 2];
			System.arraycopy(response, 0, atrBytes, 0, atrBytes.length);
			ATR atr = new ATR(atrBytes);
			return new McexCard(fd, flashProtocol, atr);
		} catch (McexException e) {
			try {
				McexJni.close(fd);
			} catch (Exception ignore) {
			}
			throw McexCard.createCardException(e, "ATR not available");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalWaitForState(long timeout, boolean present) throws CardException {
		try {
			if (timeout < 0) {
				int stat = McexJni.stat();
				return ((stat == 0) == present);
			}

			long currentTime = System.currentTimeMillis();
			long exitTime = (timeout == 0) ? Long.MAX_VALUE : (timeout + currentTime);
			while (currentTime < exitTime) {
				boolean isPresent = (McexJni.stat() == 0);
				if (isPresent == present)
					return true;

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				currentTime = System.currentTimeMillis();
			}

			return false;
		} catch (McexException e) {
			throw new CardException("card status operation failed", e);
		}
	}

}
