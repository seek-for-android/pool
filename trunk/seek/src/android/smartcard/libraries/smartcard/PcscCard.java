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
import android.smartcard.libraries.smartcard.pcsc.PcscException;
import android.smartcard.libraries.smartcard.pcsc.PcscJni;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.Disposition;

/**
 * PC/SC SEEK card implementation.
 */
final class PcscCard extends SeekCard {

	/**
	 * Creates a specific CardException instance for the specified PC/SC
	 * exception. A CardNotPresentException instance will be returned if the
	 * PC/SC error code signals that a smart card is not available.
	 * 
	 * @param e
	 *            the cause of the CardException to be created.
	 * @param message
	 *            the message of the created CardException.
	 * @return a specific CardException instance for the specified PC/SC
	 *         exception.
	 */
	static CardException createCardException(PcscException e, String message) {
		if (e.noSmartCard())
			return new CardNotPresentException(e);
		else
			return new CardException(message, e);
	}

	/** The PC/SC card handle. */
	private final long cardHandle;
	/** The PC/SC protocol identifier. */
	private final int protocolId;
	/** The protocol name. */
	private final String protocol;

	/**
	 * Constructs a new PC/SC SEEK card instance.
	 * @param cardHandle
	 *         the PC/SC card handle.
	 * @param protocol
	 *         the transmission protocol.
	 * @param atr
	 *         the ATR of the card.
	 */
	protected PcscCard(long cardHandle, int protocolId, ATR atr) {
		super(atr);
		this.cardHandle = cardHandle;
		this.protocolId = protocolId;

		switch (protocolId) {
		case PcscJni.Protocol.T0:
			this.protocol = "T=0";
			break;
		case PcscJni.Protocol.T1:
			this.protocol = "T=1";
			break;
		default:
			this.protocol = "unknown protocol";
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void internalBeginExclusive() throws CardException {
		try {
			PcscJni.beginTransaction(cardHandle);
		} catch (PcscException e) {
			throw new CardException("begin PC/SC transaction failed", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void internalDisconnect(boolean reset) throws CardException {
		try {
			PcscJni.disconnect(cardHandle, reset ? Disposition.Reset : Disposition.Leave);
		} catch (PcscException e) {
			throw new CardException("card disconnect operation failed, card assumed to be disconnected", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void internalEndExclusive() throws CardException {
		try {
			PcscJni.endTransaction(cardHandle, Disposition.Leave);
		} catch (PcscException e) {
			throw new CardException("end PC/SC transaction failed" ,e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	void internalFinalize() throws Throwable {
		PcscJni.disconnect(cardHandle, Disposition.Leave);
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
			PcscJni.status(cardHandle, null, null);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	byte[] internalControl(int controlCode, byte[] command) throws CardException {
		try {
			byte[] response = PcscJni.control(cardHandle, controlCode, command);
			return response;
		} catch (PcscException e) {
			throw new CardException("control command failed", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	byte[] internalTransmit(byte[] command) throws CardException {
		try {
			byte[] response = PcscJni.transmit(cardHandle, protocolId, command);
			return response;
		} catch (PcscException e) {
			throw new CardException("transmit failed", e);
		}
	}

}
