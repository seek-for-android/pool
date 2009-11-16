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

import java.util.List;

import junit.framework.TestCase;
import android.smartcard.CardException;
import android.smartcard.CardTerminal;
import android.smartcard.CardTerminals;
import android.smartcard.TerminalFactory;
import android.smartcard.CardTerminals.State;
import android.util.Log;

/**
 * JUnit tests for the class 'android.smartcard.libraries.smartcard.McexCardTerminals'.
 */
public class McexCardTerminalsTest extends TestCase {

	CardTerminals terminals;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		terminals = TerminalFactory.getInstance(TerminalFactory.NATIVE_TYPE, null).terminals();
	}

	
	/**
	 * Test method for {@link android.smartcard.CardTerminals#getTerminal(java.lang.String)}.
	 * @throws CardException 
	 */
	public final void testGetTerminal() throws CardException {
		List<CardTerminal> list = terminals.list();
		CardTerminal terminal0 = list.get(0);
		CardTerminal terminal = terminals.getTerminal(terminal0.getName());
		assertSame(terminal0, terminal);
	}

	/**
	 * Test method for {@link android.smartcard.CardTerminals#list()}.
	 * @throws CardException 
	 */
	public final void testList() throws CardException {
		List<CardTerminal> list = terminals.list();
		assertTrue(list.size() > 0);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCardTerminals#list(android.smartcard.CardTerminals.State)}.
	 * @throws CardException 
	 */
	public final void testListState() throws CardException {
		List<CardTerminal> list = terminals.list(State.CARD_INSERTION);
		assertTrue(list.size() > 0);
		list = terminals.list(State.CARD_PRESENT);
		assertTrue(list.size() > 0);
		list = terminals.list(State.ALL);
		assertTrue(list.size() > 0);
		
		assertFalse(terminals.waitForChange(1));
		
		list = terminals.list(State.CARD_INSERTION);
		assertTrue(list.size() == 0);
		list = terminals.list(State.CARD_PRESENT);
		assertTrue(list.size() > 0);
		list = terminals.list(State.ALL);
		assertTrue(list.size() > 0);
	}

	/**
	 * Test method for {@link android.smartcard.CardTerminals#waitForChange()}.
	 * @throws CardException 
	 */
	public final void testWaitForChange() throws CardException {
		
		Log.v("TestRunner", "EJECT CARD");
		terminals.waitForChange();
		List<CardTerminal> list = terminals.list(State.CARD_REMOVAL);
		assertTrue(list.size() == 1);
		
		Log.v("TestRunner", "INSERT CARD");
		terminals.waitForChange();
		list = terminals.list(State.CARD_INSERTION);
		assertTrue(list.size() == 1);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCardTerminals#waitForChange(long)}.
	 * @throws CardException 
	 */
	public final void testWaitForChangeLong() throws CardException {
		
		assertFalse(terminals.waitForChange(1));
		
		Log.v("TestRunner", "EJECT CARD");
		assertTrue(terminals.waitForChange(0));
		List<CardTerminal> list = terminals.list(State.CARD_REMOVAL);
		assertTrue(list.size() == 1);
		assertFalse(terminals.waitForChange(1));
		
		Log.v("TestRunner", "INSERT CARD");
		assertTrue(terminals.waitForChange(0));
		list = terminals.list(State.CARD_INSERTION);
		assertTrue(list.size() == 1);
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
	}

}
