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
import java.nio.ReadOnlyBufferException;

import android.smartcard.Card;
import android.smartcard.CardChannel;
import android.smartcard.CardException;
import android.smartcard.CardNotPresentException;
import android.smartcard.CommandAPDU;
import android.smartcard.ResponseAPDU;


/**
 * Implementation of a card channel associated with a
 * <code>SeekCard</code> based card instance.
 */
final class SeekCardChannel extends CardChannel {

	/** The corresponding card instance. */
	private final SeekCard card;
	
	/**
	 * <code>true</code> if this channel is open; <code>false</code> if it is
	 * closed.
	 */
	private volatile boolean isOpen = true;
	
	/**
	 * The channel number: 0 for the basic channel, 1 .. 19 for logical
	 * channels.
	 */
	private final int channelNumber;

	/**
	 * Constructs a new SEEK card channel instance.
	 * 
	 * @param card
	 *            the card instance to which this channel is associated to.
	 * @param channelNumber
	 *            the channel number: 0 for the basic channel, 1 .. 19 for
	 *            logical channels.
	 */
	SeekCardChannel(SeekCard card, int channelNumber) {
		super();
		this.card = card;
		this.channelNumber = channelNumber;
	}

	/**
	 * Asserts that this channel is open and that the corresponding card is
	 * connected.
	 * 
	 * @throws IllegalStateException
	 *             if this channel is closed, or the corresponding card is
	 *             disconnected.
	 */
	private void assertChannelOpenAndCardConnected() {
		if (!isOpen) {
			throw new IllegalStateException("channel is closed");
		}
		if (!card.isConnected(false)) {
			throw new IllegalStateException("card is disconnected");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws CardException {
		if (!isOpen)
			return;
		if (channelNumber == 0) {
			throw new IllegalStateException("basic logical channel cannot be closed");
		}
		byte cla = (byte) channelNumber;
		if (channelNumber > 3) {
			cla |= 0x40;
		}
		byte[] manageChannelClose = new byte[] { cla, 0x70, (byte) 0x80,
				(byte) channelNumber };
		card.transmit(manageChannelClose, 2, 0x9000, 0xFFFF, "MANAGE CHANNEL");
		isOpen = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Card getCard() {
		return card;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getChannelNumber() {
		assertChannelOpenAndCardConnected();
		return channelNumber;
	}

	/**
	 * Transmits the specified command APDU and returns the response APDU.
	 * 
	 * @param cmd
	 *            the command APDU to be transmitted.
	 * @return the response APDU received.
	 * @throws IllegalArgumentException
	 *             if the command length is smaller than 4 or an ISO MANAGE
	 *             CHANNEL command is specified.
	 * @throws CardNotPresentException
	 *             if the transmit operation failed because the card has been
	 *             removed.
	 * @throws CardException
	 *             if the transmit operation failed
	 */
	private byte[] internalTransmit(byte[] cmd) throws CardException {
		if (cmd.length < 4)
			throw new IllegalArgumentException("command must not be smaller than 4 bytes");
		if (((cmd[0] & 0x80) == 0) && ((cmd[0] & 0x60) != 0x20)) {
			// ISO command
			if (cmd[1] == 0x70)
				throw new IllegalArgumentException("MANAGE CHANNEL command not allowed");
			int cla = cmd[0] & 0x7F;
			if (channelNumber < 4) {
				cla = (cla & 0x1C) | channelNumber;
			} else {
				cla = (cla & 0x30) | 0x40 | channelNumber;
			}
			cmd[0] = (byte) cla;
		}
		byte[] rsp = card.transmit(cmd, 2, 0, 0);
		return rsp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int transmit(ByteBuffer command, ByteBuffer response) throws CardException {
		assertChannelOpenAndCardConnected();
		if (command == null)
			throw new NullPointerException("command buffer must not be null");
		if (response == null)
			throw new NullPointerException("response buffer must not be null");
		if (response.isReadOnly())
			throw new ReadOnlyBufferException();
		if (command == response)
			throw new IllegalArgumentException("command and response buffer must not be identical");
		if (response.remaining() < 258)
			throw new IllegalArgumentException("free space of response buffer must not be smaller than 258");

		byte[] cmd = new byte[command.remaining()];
		command.get(cmd);
		byte[] rsp = internalTransmit(cmd);
		response.put(rsp);
		return rsp.length;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseAPDU transmit(CommandAPDU command) throws CardException {
		assertChannelOpenAndCardConnected();
		byte[] rsp = internalTransmit(command.getBytes());
		return new ResponseAPDU(rsp);
	}
}
