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
import android.smartcard.CardChannel;
import android.smartcard.CardException;
import android.smartcard.CardTerminal;
import android.smartcard.CommandAPDU;
import android.smartcard.ResponseAPDU;
import android.smartcard.TerminalFactory;
import android.smartcard.libraries.smartcard.McexCard;

import junit.framework.TestCase;

/**
 * JUnit tests for the class 'android.smartcard.libraries.smartcard.McexCard'.
 */
public class McexCardTest extends TestCase {

	static final CommandAPDU GET_CPLC_DATA_APDU = new CommandAPDU(new byte[] { (byte) 0x80, (byte) 0xCA, (byte) 0x9F, 0x7F, 0x00 });

	McexCard card;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		CardTerminal terminal = TerminalFactory.getInstance(TerminalFactory.NATIVE_TYPE, null).terminals().list().get(0);
		card = (McexCard) terminal.connect("*");
	}

	protected void tearDown() throws Exception {
		if (card.isConnected(false))
			card.disconnect(false);
		super.tearDown();
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCard#beginExclusive()}.
	 * @throws CardException 
	 * @throws InterruptedException 
	 */
	public final void testBeginExclusive() throws CardException, InterruptedException {
		card.beginExclusive();
		final CardChannel channel = card.getBasicChannel();
		ResponseAPDU rsp = channel.transmit(GET_CPLC_DATA_APDU);
		assertTrue(rsp.getBytes().length > 0);
		
		final boolean[] check = new boolean[1];
		Thread worker1 = new Thread() {
			@Override
			public void run() {
				try {
					channel.transmit(GET_CPLC_DATA_APDU);
					check[0] = false;
				} catch (IllegalStateException e) {
					check[0] = true;
				} catch (Exception e) {
					check[0] = false;
				}
			}
		};
		worker1.start();
		worker1.join();
		assertTrue(check[0]);
		
		card.endExclusive();
		
		Thread worker2 = new Thread() {
			@Override
			public void run() {
				try {
					channel.transmit(GET_CPLC_DATA_APDU);
					check[0] = false;
				} catch (IllegalStateException e) {
					check[0] = true;
				} catch (Exception e) {
					check[0] = false;
				}
			}
		};
		worker2.start();
		worker2.join();
		assertFalse(check[0]);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCard#disconnect(boolean)}.
	 * @throws CardException 
	 */
	public final void testDisconnect() throws CardException {
		card.disconnect(false);
		assertFalse(card.isConnected(false));
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCard#endExclusive()}.
	 * @throws CardException 
	 */
	public final void testEndExclusive() throws CardException {
		card.beginExclusive();
		card.endExclusive();
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCard#getATR()}.
	 */
	public final void testGetATR() {
		ATR atr = card.getATR();
		assertTrue(atr.getBytes().length > 0);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCard#getBasicChannel()}.
	 */
	public final void testGetBasicChannel() {
		CardChannel channel = card.getBasicChannel();
		assertEquals(0, channel.getChannelNumber());
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCard#getProtocol()}.
	 */
	public final void testGetProtocol() {
		assertEquals("T=1", card.getProtocol());
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCard#openLogicalChannel()}.
	 * @throws CardException 
	 */
	public final void testOpenLogicalChannel() throws CardException {
		CardChannel channel = card.openLogicalChannel();
		assertTrue(channel.getChannelNumber() > 0);
		channel.close();
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.McexCard#transmitControlCommand(int, byte[])}.
	 */
	public final void testTransmitControlCommand() {
		// TODO fail("Not yet implemented");
	}
}
