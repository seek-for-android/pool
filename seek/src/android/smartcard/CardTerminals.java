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

package android.smartcard;

import java.util.List;

public abstract class CardTerminals {

	public static enum State {
		ALL, CARD_PRESENT, CARD_ABSENT, CARD_INSERTION, CARD_REMOVAL,
	}

	protected CardTerminals() {

	}

	public CardTerminal getTerminal(String name) {
		if (name == null) {
			throw new NullPointerException();
		}

		try {
			List<CardTerminal> terminals = list();
			for (CardTerminal terminal : terminals) {
				if (terminal.getName().equals(name)) {
					return terminal;
				}
			}
		} catch (Exception ignore) {
		}

		return null;
	}

	public List<CardTerminal> list() throws CardException {
		return list(State.ALL);
	}

	public abstract List<CardTerminal> list(State state) throws CardException;

	public void waitForChange() throws CardException {
		waitForChange(0);
	}

	public abstract boolean waitForChange(long timeout) throws CardException;

}
