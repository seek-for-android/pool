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

import java.util.HashMap;
import java.util.Map;


import android.smartcard.CardException;
import android.smartcard.CardTerminal;
import android.smartcard.libraries.smartcard.pcsc.PcscException;
import android.smartcard.libraries.smartcard.pcsc.PcscJni;
import android.smartcard.libraries.smartcard.pcsc.PcscJni.ReaderState;

/**
 * PC/SC SEEK card terminal collection implementation.
 */
class PcscCardTerminals extends SeekCardTerminals {

	/**
	 * Additional PC/SC card terminal information.
	 */
	private static class PcscTerminalInfo extends TerminalInfo {

		/** The current PC/SC reader state. */
		private int pcscState = ReaderState.Unaware;

		/**
		 * Returns the current PC/SC reader state.
		 * @return the current PC/SC reader state.
		 */
		int getPcscState() {
			return pcscState;
		}

		/**
		 * Updates the current PC/SC reader state.
		 * @param pcscState
		 *         the current PC/SC reader state to be updated.
		 */
		void updatePcscState(int pcscState) {
			boolean wasPresent = (this.pcscState & ReaderState.Present) == ReaderState.Present;
			boolean isPresent = (pcscState & ReaderState.Present) == ReaderState.Present;
			if (isPresent != wasPresent) {
				this.cardState = isPresent ? State.CARD_INSERTION : State.CARD_REMOVAL;
			} else {
				this.cardState = isPresent ? State.CARD_PRESENT : State.CARD_ABSENT;
			}
			this.pcscState = pcscState;
		}
	}

	/** The PC/SC context handle. */
	private static long contextHandle;

	/** Cache of PC/SC card terminal instances. */
	private static final Map<String, CardTerminal> terminals = new HashMap<String, CardTerminal>();

	/**
	 * Constructs a new PC/SC SEEK card terminal collection instance.
	 */
	PcscCardTerminals() {
		super();
		try {
			if (contextHandle == 0) {
				contextHandle = PcscJni.establishContext(PcscJni.Scope.User);
			}
		} catch (PcscException e) {
			// Log.v("SEEK", e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	CardTerminal internalGetTerminal(String name) {
		synchronized (PcscCardTerminals.class) {
			// check if we already have an object for this terminal
			CardTerminal terminal = terminals.get(name);
			if (terminal == null) {
				// new terminal - create the CardTerminal and insert into hash map
				terminal = new PcscCardTerminal(contextHandle, name);
				terminals.put(name, terminal);
			}
			return terminal;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String[] internalListReaders() throws CardException {
		try {
			String[] readerNames = PcscJni.listReaders(contextHandle, null);
			return readerNames;
		} catch (PcscException e) {
			throw new CardException("PC/SC list readers failed", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalWaitForChange(long timeout, Map<String, TerminalInfo> infoMap) throws CardException {
		try {
			String[] readerNames = infoMap.keySet().toArray(new String[infoMap.size()]);
			int[] currentStates = new int[readerNames.length];
			int[] eventStates = new int[readerNames.length];

			for (int i = 0; i < readerNames.length; i++) {
				PcscTerminalInfo info = (PcscTerminalInfo) infoMap.get(readerNames[i]);
				if (info == null) {
					info = new PcscTerminalInfo();
					infoMap.put(readerNames[i], info);
				}
				currentStates[i] = info.getPcscState();
				eventStates[i] = ReaderState.Unaware;
			}

			int pcscTimeout = (timeout == 0) ? -1 : (int) timeout;
			boolean eventRaised = PcscJni.getStatus(contextHandle, pcscTimeout, readerNames, currentStates,
					eventStates);

			for (int i = 0; i < readerNames.length; i++) {
				PcscTerminalInfo info = (PcscTerminalInfo) infoMap.get(readerNames[i]);
				info.updatePcscState(eventRaised ? eventStates[i] : currentStates[i]);
			}

			return eventRaised;

		} catch (PcscException e) {
			throw new CardException("PC/SC get status change failed", e);
		}
	}
}
