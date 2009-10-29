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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.smartcard.CardException;
import android.smartcard.CardTerminal;
import android.smartcard.CardTerminals;


/**
 * Base class implementation of SEEK card terminal collections. Concrete card
 * terminal collections must implement the abstract <code>internalXxx</code> methods.
 */
abstract class SeekCardTerminals extends CardTerminals {

	/**
	 * Base class implementation of additional SEEK card terminal information.
	 */
	abstract static class TerminalInfo {

		/** The current card state. */
		protected State cardState = State.CARD_ABSENT;

		/**
		 * Returns the current card state.
		 * @return the current card state.
		 */
		State getCardState() {
			return cardState;
		}
	}

	/**
	 * Map of additional terminal info. Due to the specification, each terminal collection
	 * must have its own terminal info.
	 */
	private volatile Map<String, TerminalInfo> terminalInfoMap = new HashMap<String, TerminalInfo>();
	
	/** <code>true</code> if the terminal info map is initialized, <code>false</code> if not. */
	private volatile boolean isInitialized;

	/**
	 * Constructs a new SEEK card terminal collection.
	 */
	SeekCardTerminals() {
		super();
	}

	/**
	 * Returns a new terminal information map for the specified reader names.
	 * @param readerNames
	 *           the reader names for which to provide terminal information.
	 * @return a new terminal information map for the specified reader names.
	 */
	private Map<String, TerminalInfo> createTerminalInfoMap(String[] readerNames) {
		Map<String, TerminalInfo> infoMap = new HashMap<String, TerminalInfo>(readerNames.length);
		for (String readerName : readerNames) {
			infoMap.put(readerName, terminalInfoMap.get(readerName));
		}
		return infoMap;
	}
	
	/**
	 * Returns the SEEK card terminal with the specified name.
	 * @param name
	 *         the name of the SEEK card terminal to be returned.
	 * @return the SEEK card terminal with the specified name.
	 */
	abstract CardTerminal internalGetTerminal(String name);

	/**
	 * Implements the list readers operation for this card terminal collection.
	 * @return an array of reader names contained in this card terminal collection.
	 * @throws CardException
	 *           if the list readers operation failed.
	 */
	abstract String[] internalListReaders() throws CardException;

	/**
	 * Implements the wait for change operation for this card terminal collection.
	 * 
	 * @param timeout
	 *          the maximum waiting time for a state change to occur.
	 *          If 0, the method will block forever until a state change occurs.
	 * @param infoMap
	 *          terminal information map which contains the information
	 *          known by the application to be updated by the method.
	 * @return <code>false</code> if no state change occurred within the
	 *         maximum timeout, <code>true</code> otherwise.
	 * @throws CardException
	 */
	abstract boolean internalWaitForChange(long timeout, Map<String, TerminalInfo> infoMap)
			throws CardException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized List<CardTerminal> list(State state) throws CardException {
		if (state == null) {
			throw new NullPointerException();
		}

		State listState = state;
		if (!isInitialized) {
			if (state == State.CARD_INSERTION) {
				listState = State.CARD_PRESENT;
			} else if (state == State.CARD_REMOVAL) {
				listState = State.CARD_ABSENT;
			}
		}

		String[] readerNames = internalListReaders();

		List<CardTerminal> cardTerminalList = new ArrayList<CardTerminal>(readerNames.length);

		for (String readerName : readerNames) {
			CardTerminal cardTerminal = internalGetTerminal(readerName);

			if (listState == State.ALL) {
				cardTerminalList.add(cardTerminal);
			} else if ((listState == State.CARD_PRESENT) || (listState == State.CARD_ABSENT)) {
				boolean isPresent = cardTerminal.isCardPresent();
				if (isPresent && (listState == State.CARD_PRESENT)) {
					cardTerminalList.add(cardTerminal);
				} else if (!isPresent && (listState == State.CARD_ABSENT)) {
					cardTerminalList.add(cardTerminal);
				}
			} else if ((listState == State.CARD_INSERTION) || (listState == State.CARD_REMOVAL)) {
				TerminalInfo terminalInfo = terminalInfoMap.get(readerName);
				if ((terminalInfo != null) && terminalInfo.getCardState() == listState) {
					cardTerminalList.add(cardTerminal);
				}
			}
		}
		return Collections.unmodifiableList(cardTerminalList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean waitForChange(long timeout) throws CardException {
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout must not be negative");
		}
		String[] readerNames = internalListReaders();
		if (readerNames.length == 0) {
			terminalInfoMap.clear();
			throw new IllegalStateException("no card terminals available");
		}

		if (!isInitialized) {
			Map<String, TerminalInfo> infoMap = createTerminalInfoMap(readerNames);
			internalWaitForChange(1, infoMap);
			terminalInfoMap = infoMap;
			isInitialized = true;
		}

		Map<String, TerminalInfo> infoMap = createTerminalInfoMap(readerNames);
		boolean eventRaised = internalWaitForChange(timeout, infoMap);

		terminalInfoMap = infoMap;

		return eventRaised;
	}
}
