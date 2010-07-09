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

import java.util.Map;


import android.smartcard.CardException;
import android.smartcard.CardTerminal;
import android.smartcard.libraries.smartcard.mcex.McexException;
import android.smartcard.libraries.smartcard.mcex.McexJni;

/**
 * Native SEEK card terminal collection implementation which supports a single MCEX device.
 */
final class McexCardTerminals extends SeekCardTerminals {

	/**
	 * Additional MCEX card terminal information.
	 */
	static class McexTerminalInfo extends TerminalInfo {

		/** <code>true</code> if the device is present, <code>false</code> if it is absent. */
		private boolean mcexState = false;

		/**
		 * Returns the current state of the MCEX device.
		 * @return <code>true</code> if the device is present, <code>false</code> if it is absent.
		 */
		boolean getMcexState() {
			return mcexState;
		}

		/**
		 * Updates the current state of the MCEX device.
		 * @param mcexState
		 *           <code>true</code> if the device is present, <code>false</code> if it is absent.
		 */
		void updateMcexState(boolean mcexState) {
			boolean wasPresent = this.mcexState;
			boolean isPresent = mcexState;
			if (isPresent != wasPresent) {
				this.cardState = isPresent ? State.CARD_INSERTION : State.CARD_REMOVAL;
			} else {
				this.cardState = isPresent ? State.CARD_PRESENT : State.CARD_ABSENT;
			}
			this.mcexState = mcexState;
		}
	}

	/** The friendly name of the single MCEX card terminal. */
	private static final String TERMINAL_NAME = "MCEX 0";
	
	/** The list of MCEX card terminal names, containing the single MCEX card terminal name. */
	private static final String[] TERMINAL_LIST = new String[] { TERMINAL_NAME };

	/** The single MCEX card terminal instance. */
	private static final CardTerminal mcexTerminal = new McexCardTerminal(TERMINAL_NAME);

	/**
	 * Constructs a new MCEX SEEK card terminal collection instance.
	 */
	McexCardTerminals() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	CardTerminal internalGetTerminal(String name) {
		return mcexTerminal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	String[] internalListReaders() throws CardException {
		return TERMINAL_LIST;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean internalWaitForChange(long timeout, Map<String, TerminalInfo> infoMap) throws CardException {
		try {
			McexTerminalInfo info = (McexTerminalInfo) infoMap.get(TERMINAL_NAME);
			if (info == null) {
				info = new McexTerminalInfo();
				infoMap.put(TERMINAL_NAME, info);
			}
			boolean currentState = info.getMcexState();
			boolean eventState = false;
			boolean eventRaised = false;

			long currentTime = System.currentTimeMillis();
			long exitTime = (timeout == 0) ? Long.MAX_VALUE : (timeout + currentTime);
			while (currentTime < exitTime) {
				eventState = (McexJni.stat() == 0);
				if (currentState != eventState) {
					eventRaised = true;
					break;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
				currentTime = System.currentTimeMillis();
			}

			info.updateMcexState(eventRaised ? eventState : currentState);

			return eventRaised;

		} catch (McexException e) {
			throw new CardException("waitForChange() exception", e);
		}
	}
}
