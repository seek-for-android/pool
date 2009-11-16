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
import android.smartcard.CardNotPresentException;
import android.smartcard.libraries.smartcard.mcex.McexException;
import android.smartcard.libraries.smartcard.mcex.McexJni;
import android.smartcard.libraries.smartcard.mcex.McexNotPresentException;
import android.smartcard.libraries.smartcard.mcex.McexJni.OpenMode;

/**
 * Native SEEK card implementation which supports a single MCEX device.
 */
final class McexCard extends SeekCard {

	/**
	 * Creates a specific CardException instance for the specified MCEX
	 * exception.
	 * 
	 * @param e
	 *            the cause of the CardException to be created.
	 * @param message
	 *            the message of the created CardException.
	 * @return a specific CardException instance for the specified MCEX
	 *         exception.
	 */
	static CardException createCardException(McexException e, String message) {
		if (e instanceof McexNotPresentException)
			return new CardNotPresentException(e);
		else
			return new CardException(message, e);
	}

	/** The current protocol. */
	private final String protocol;
	
	/**
	 * The file descriptor of the MCEX device file.
	 * Non-zero only during temporary exclusive access.
	 */
	private volatile int fd;

	/**
	 * Constructs a new MCEX SEEK card instance.
	 * @param fd
	 *         the file descriptor of the MCEX device file.
	 * @param protocol
	 *         the transmission protocol.
	 * @param atr
	 *         the ATR of the card.
	 */
	protected McexCard(int fd, String protocol, ATR atr) {
		super(atr);
		this.fd = fd;
		this.protocol = protocol;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void internalBeginExclusive() throws CardException {
		if (fd != 0)
			return;
		try {
			fd = McexJni.open(OpenMode.BeginExclusive);
		} catch (McexException e) {
			throw new CardException("begin exclusive operation failed", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	byte[] internalControl(int controlCode, byte[] command) throws CardException {
		throw new CardException("control command not supported");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void internalDisconnect(boolean reset) throws CardException {
		try {
			McexJni.close(fd);
		} catch (Exception e) {
			throw new CardException("card disconnect operation failed, card assumed to be disconnected", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void internalEndExclusive() throws CardException {
		if (fd == 0)
			return;
		try {
			McexJni.close(fd);
			fd = 0;
		} catch (McexException e) {
			throw new CardException("end exclusive operation failed", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void internalFinalize() throws Throwable {
		McexJni.close(fd);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String internalGetProtocol() {
		return protocol;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalIsConnected() {
		try {
			if (McexJni.stat() != 0)
				return false;
		} catch (McexException e) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	byte[] internalTransmit(byte[] command) throws CardException {
		try {
			byte[] response = McexJni.transmit(fd, command);
			return response;
		} catch (McexException e) {
			throw new CardException("transmit failed", e);
		}
	}

}
