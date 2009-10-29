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

import android.smartcard.Card;
import android.smartcard.CardException;
import android.smartcard.CardTerminal;

/**
 * Base class implementation of SEEK card terminals. Concrete card
 * terminals must implement the abstract <code>internalXxx</code> methods.
 */
abstract class SeekCardTerminal extends CardTerminal {

	/** The friendly name of the card terminal. */
	protected final String name;
	/**
	 * The associated card instance or <code>null</code> if a card is not
	 * connected.
	 */
	protected volatile SeekCard card;

	/**
	 * Constructs a new SEEK card terminal instance.
	 * 
	 * @param name
	 *            the friendly name of the card terminal.
	 */
	SeekCardTerminal(String name) {
		super();
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Card connect(String protocol) throws CardException {
		if (protocol == null)
			throw new NullPointerException("protocol must not be null");

		synchronized (this) {
			if (card != null) {
				// check if a current card with compatible protocol can be used
				if (card.isConnected(protocol)) {
					return card;
				} else {
					card = null;
				}
			}
	
			card = internalConnect(protocol);
			return card;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Implements the connect card operation for this SEEK card terminal
	 * using the specified transmission protocol.
	 * The supported protocol types depend on the
	 * concrete SEEK card terminal type.
	 * 
	 * @param protocol
	 *           the transmission protocol to be used.
	 * @return the connected card instance.
	 * @throws IllegalArgumentException
	 *           if the specified protocol is invalid or not supported.
	 * @throws CardNotPresentException
	 *           if no card is present in this terminal
	 * @throws CardException
	 *           if the connect operation for the specified protocol failed.
	 */
	abstract SeekCard internalConnect(String protocol) throws CardException;

	/**
	 * Implements the wait for state change operation for this
	 * SEEK card terminal.
	 * 
	 * @param timeout
	 *          the maximum waiting time for a state change to occur.
	 *          If 0, the method will block forever until a state change occurs.
	 *          If -1, the method will immediately return the current presence state.
	 * @param present
	 *          <code>true</code> to wait for card presence, <code>false</code> to wait for card absence.
	 * @return if timeout is -1, the method returns (current presence state == present).
	 *         If timeout is non-negative the method returns <code>false</code> if no state change occurred within the
	 *         maximum timeout, otherwise the method returns <code>true</code>.
	 * @throws CardException
	 */
	abstract boolean internalWaitForState(long timeout, boolean present) throws CardException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCardPresent() throws CardException {
		return internalWaitForState(-1, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean waitForCardAbsent(long timeout) throws CardException {
		if (timeout < 0)
			throw new IllegalArgumentException("timeout must not be negative");
		return internalWaitForState(timeout, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean waitForCardPresent(long timeout) throws CardException {
		if (timeout < 0)
			throw new IllegalArgumentException("timeout must not be negative");
		return internalWaitForState(timeout, true);
	}
}
