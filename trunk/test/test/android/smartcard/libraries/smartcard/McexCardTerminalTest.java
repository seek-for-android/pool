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
import android.smartcard.TerminalFactory;

import junit.framework.TestCase;

/**
 * JUnit tests for the class 'android.smartcard.libraries.smartcard.McexCardTerminal'.
 */
public class McexCardTerminalTest extends TestCase {

	CardTerminal terminal;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		terminal = TerminalFactory.getInstance(TerminalFactory.NATIVE_TYPE, null).terminals().list().get(0);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCardTerminal#connect(java.lang.String)}.
	 * @throws CardException 
	 */
	public final void testConnect() throws CardException {
		Card card = terminal.connect("T=0");
		assertNotNull(card);
		Card card2 = terminal.connect("*");
		assertSame(card, card2);
		try {
			terminal.connect("T=1");
			fail("connect() did not raise an exection for incompatible protocol");
		} catch (CardException expected) {} 
		card.disconnect(false);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCardTerminal#getName()}.
	 */
	public final void testGetName() {
		String name = terminal.getName();
		assertTrue(name.length() > 0);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCardTerminal#isCardPresent()}.
	 * @throws CardException 
	 */
	public final void testIsCardPresent() throws CardException {
		assertTrue(terminal.isCardPresent());
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCardTerminal#waitForCardAbsent(long)}.
	 */
	public final void testWaitForCardAbsent() throws CardException {
		assertFalse(terminal.waitForCardAbsent(100));
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCardTerminal#waitForCardPresent(long)}.
	 * @throws CardException 
	 */
	public final void testWaitForCardPresent() throws CardException {
		assertTrue(terminal.waitForCardPresent(0));
	}
}
