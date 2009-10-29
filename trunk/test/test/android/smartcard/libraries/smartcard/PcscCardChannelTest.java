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

import java.nio.ByteBuffer;

import android.smartcard.Card;
import android.smartcard.CardChannel;
import android.smartcard.CardException;
import android.smartcard.CardTerminal;
import android.smartcard.CommandAPDU;
import android.smartcard.ResponseAPDU;
import android.smartcard.TerminalFactory;
import android.smartcard.libraries.smartcard.PcscCard;


import junit.framework.TestCase;

/**
 * JUnit tests for the class 'android.smartcard.libraries.smartcard.PcscCardChannel'.
 */
public class PcscCardChannelTest extends TestCase {

	static final byte[] SELECT_BYTES = new byte[] { 0x00, (byte) 0xA4, 0x04, 0x00, 0x08, (byte) 0xA0, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00 };
	static final CommandAPDU SELECT_APDU = new CommandAPDU(SELECT_BYTES);

	PcscCard card;
	CardChannel basicChannel;
	CardChannel logicalChannel;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		CardTerminal terminal = TerminalFactory.getInstance(TerminalFactory.PCSC_TYPE, null).terminals().list().get(0);
		card = (PcscCard) terminal.connect("*");
		basicChannel = card.getBasicChannel();
		logicalChannel = card.openLogicalChannel(); 
	}

	protected void tearDown() throws Exception {
		if (card.isConnected(false)) {
			try {
				logicalChannel.close();
			} catch (Exception ignore) {}
			card.disconnect(false);
		}
		super.tearDown();
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.PcscCardChannel#close()}.
	 * @throws CardException 
	 */
	public final void testClose() throws CardException {
		logicalChannel.close();
		try {
			logicalChannel.getChannelNumber();
			fail("close() did not close logical");
		} catch (IllegalStateException expected) {}
		try {
			basicChannel.close();
			fail("close() did not raise an exception for basic channel");
		} catch (IllegalStateException expected) {}
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.PcscCardChannel#getCard()}.
	 */
	public final void testGetCard() {
		Card basicCard = basicChannel.getCard();
		assertSame(card, basicCard);
		Card logicalCard = basicChannel.getCard();
		assertSame(card, logicalCard);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.PcscCardChannel#getChannelNumber()}.
	 */
	public final void testGetChannelNumber() {
		assertEquals(0, basicChannel.getChannelNumber());
		assertTrue(logicalChannel.getChannelNumber() > 0);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.PcscCardChannel#transmit(java.nio.ByteBuffer, java.nio.ByteBuffer)}.
	 * @throws CardException 
	 */
	public final void testTransmitByteBufferByteBuffer() throws CardException {
		ByteBuffer cmd = ByteBuffer.wrap(SELECT_BYTES);
		cmd.position(0);
		ByteBuffer rsp = ByteBuffer.allocate(258);
		int rspLen = basicChannel.transmit(cmd, rsp);
		assertEquals(SELECT_BYTES.length, cmd.position());
		assertEquals(rspLen, rsp.position());
		
		cmd = ByteBuffer.wrap(SELECT_BYTES);
		cmd.position(0);
		rsp = ByteBuffer.allocate(258);
		rspLen = logicalChannel.transmit(cmd, rsp);
		assertEquals(SELECT_BYTES.length, cmd.position());
		assertEquals(rspLen, rsp.position());
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.PcscCardChannel#transmit(android.smartcard.CommandAPDU)}.
	 * @throws CardException 
	 */
	public final void testTransmitCommandAPDU() throws CardException {
		ResponseAPDU rsp = basicChannel.transmit(SELECT_APDU);
		assertTrue(rsp.getBytes().length > 0);
		rsp = logicalChannel.transmit(SELECT_APDU);
		assertTrue(rsp.getBytes().length > 0);
	}

}
