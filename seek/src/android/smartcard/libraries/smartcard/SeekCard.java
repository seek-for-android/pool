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
import android.smartcard.Card;
import android.smartcard.CardChannel;
import android.smartcard.CardException;
import android.smartcard.CardNotPresentException;

/**
 * Base class implementation of SEEK card. Concrete
 * cards must implement the abstract <code>internalXxx</code> methods.
 */
abstract class SeekCard extends Card {

	/**
	 * Returns a concatenated response.
	 * @param r1
	 *         the first part of the response.
	 * @param r2
	 *         the second part of the response.
	 * @param length
	 *         the number of bytes of the second part to be appended.
	 * @return a concatenated response.
	 */
	static byte[] appendResponse(byte[] r1, byte[] r2, int length) {
		byte[] rsp = new byte[r1.length + length];
		System.arraycopy(r1, 0, rsp, 0, r1.length);
		System.arraycopy(r2, 0, rsp, r1.length, length);
		return rsp;
	}

	/**
	 * Creates a formatted exception message.
	 * @param commandName
	 *          the name of the command. <code>null</code> if not specified.
	 * @param sw
	 *          the response status word.
	 * @return a formatted exception message.
	 */
	static String createMessage(String commandName, int sw) {
		StringBuffer message = new StringBuffer();
		if (commandName != null)
			message.append(commandName).append(" ");
		message.append("SW1/2 error: ");
		message.append(Integer.toHexString(sw | 0x10000).substring(1));
		return message.toString();
	}

	/**
	 * Creates a formatted exception message.
	 * @param commandName
	 *          the name of the command. <code>null</code> if not specified.
	 * @param message
	 *          the message to be formatted.
	 * @return a formatted exception message.
	 */
	static String createMessage(String commandName, String message) {
		if (commandName == null)
			return message;
		return commandName + " " + message;
	}

	/**
	 * Returns <code>true</code> if the specified command is a short CASE4 APDU, <code>false</code> otherwise.
	 * @param cmd
	 *          the command APDU to be checked.
	 * @return <code>true</code> if the specified command is a short CASE4 APDU, <code>false</code> otherwise.
	 */
	static boolean isCase4(byte[] cmd) {
		if (cmd.length < 7)
			return false;
		int lc = cmd[4] & 0xFF;
		return (lc + 5 < cmd.length);
	}

	/** The ATR of the card. */
	protected final ATR atr;
	/** The basic channel of the card, which cannot be closed. */
	protected final SeekCardChannel basicChannel;

	/** The thread which has exclusive access, or <code>null</code> if the card can be used concurrently. */
	protected volatile Thread transactionThread;
	/** <code>true</code> if the card is connected, <code>false</code> if it is disconnected. */
	protected volatile boolean isConnected = true;

	/**
	 * Constructs a new SEEK card instance.
	 * @param atr
	 *         the ATR of the card.
	 */
	protected SeekCard(ATR atr) {
		this.atr = atr;
		this.basicChannel = new SeekCardChannel(this, 0);
	}

	/**
	 * Asserts that the card is connected.
	 * @throws IllegalStateException
	 *           if the card is not connected.
	 */
	protected void assertCardConnected() {
		if (!isConnected) {
			throw new IllegalStateException("card is disposed");
		}
	}

	/**
	 * Ensures that exclusive access is released.
	 */
	protected void assertEndExclusive() {
		if (transactionThread == null)
			return;

		try {
			internalEndExclusive();
		} catch (Exception ignore) {
		} finally {
			transactionThread = null;
		}
	}

	/**
	 * Asserts that the current thread has access to the card.
	 * @throws IllegalStateException
	 *          if another thread has been granted temporary exclusive access.
	 */
	protected void assertTransaction() {
		Thread currentThread = Thread.currentThread();
		if ((transactionThread != null) && (transactionThread != currentThread)) {
			throw new IllegalStateException("card in exclusive usage by another thread");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beginExclusive() throws CardException {
		assertCardConnected();
		synchronized (this) {
			Thread currentThread = Thread.currentThread();
			if (transactionThread == currentThread)
				return;
			if (transactionThread != null)
				throw new CardException("card in exclusive usage by another thread");

			internalBeginExclusive();

			transactionThread = currentThread;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect(boolean reset) throws CardException {
		if (!isConnected)
			return;
		try {
			synchronized (this) {
				assertTransaction();
				assertEndExclusive();
				internalDisconnect(reset);
			}
		} finally {
			isConnected = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endExclusive() throws CardException {
		assertCardConnected();
		synchronized (this) {
			if (transactionThread != Thread.currentThread())
				throw new IllegalStateException("active thread does not have exclusive access");

			try {
				internalEndExclusive();
			} finally {
				transactionThread = null;
			}
		}
	}

	/**
	 * Releases resources allocated by this SEEK card terminal.
	 */
	protected void finalize() throws Throwable {
		try {
			assertEndExclusive();
			internalFinalize();
		} finally {
			super.finalize();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ATR getATR() {
		return atr;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CardChannel getBasicChannel() {
		assertCardConnected();
		return basicChannel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getProtocol() {
		assertCardConnected();
		return internalGetProtocol();
	}

	/**
	 * Implements the SEEK card specific begin exclusive operation.
	 * @throws CardException
	 *           if the begin exclusive operation failed.
	 */
	abstract void internalBeginExclusive() throws CardException;

	/**
	 * Implements the SEEK card specific control operation.
	 * @param controlCode
	 *          the control code of the command.
	 * @param command
	 *          the control command to be transmitted.
	 * @return the control response received.
	 * @throws CardException
	 *          if the control operation failed.
	 */
	abstract byte[] internalControl(int controlCode, byte[] command) throws CardException;

	/**
	 * Implements the SEEK card specific disconnect operation.
	 * 
	 * @param reset
	 *          <code>true</code> to reset the card, <code>false</code> to leave the card in the current state.
	 * @throws CardException
	 *           if the disconnect operation failed.
	 */
	abstract void internalDisconnect(boolean reset) throws CardException;

	/**
	 * Implements the SEEK card specific end exclusive operation.
	 * @throws CardException
	 *           if the end exclusive operation failed.
	 */
	abstract void internalEndExclusive() throws CardException;

	/**
	 * Implements the SEEK card specific finalize operation.
	 * @throws Throwable
	 *           if the finalize operation failed.
	 */
	abstract void internalFinalize() throws Throwable;

	/**
	 * Implements the SEEK card specific get protocol operation.
	 * @return the current protocol.
	 */
	abstract String internalGetProtocol();

	/**
	 * Implements the SEEK card specific connection check.
	 */
	abstract void internalIsConnected();

	/**
	 * Implements the SEEK card specific transmit operation.
	 * @param command
	 *           the command to be transmitted.
	 * @return the response received.
	 * @throws CardException
	 *           if the transmit operation failed.
	 */
	abstract byte[] internalTransmit(byte[] command) throws CardException;

	/**
	 * Asserts that the current protocol is compatible the specified protocol.
	 * May be overridden by derived classes.
	 * The default implementation asserts that the specified protocol equals
	 * '*' or the current protocol.
	 * 
	 * @param newProtocol
	 *         the requested protocol to be checked.
	 * @throws CardException
	 *         if the current protocol is not compatible to the specified protocol.
	 */
	protected void isCompatibleProtocol(String newProtocol) throws CardException {
		String protocol = internalGetProtocol();
		if (!newProtocol.equals("*")) {
			if (!newProtocol.equals(protocol))
				throw new CardException("card already connected with incompatible protocol " + protocol);
		}
	}

	/**
	 * Returns <code>true</code> if the card is connected, <code>false</code> otherwise.
	 * @param validate
	 *           <code>true</code>, if the connection state shall be actively polled;
	 *           <code>false</code>, if cached connection state information shall be returned.
	 * @return <code>true</code> if the card is connected, <code>false</code> otherwise.
	 */
	boolean isConnected(boolean validate) {
		if (validate) {
			internalIsConnected();
		}
		return isConnected;
	}

	/**
	 * Returns <code>true</code> if the card is connected in a protocol compatible to the specified one,
	 * or <code>false</code> if the card is not connected.
	 * @param protocol
	 *         the requested protocol to be checked.
	 * @return <code>true</code> if the card is connected in a protocol compatible to the specified one,
	 * or <code>false</code> if the card is not connected.
	 * @throws CardException
	 *         if the current protocol is not compatible to the specified protocol.
	 */
	boolean isConnected(String protocol) throws CardException {
		if (isConnected(true)) {
			isCompatibleProtocol(protocol);
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CardChannel openLogicalChannel() throws CardException {
		assertCardConnected();
		byte[] manageChannelCommand = new byte[] { 0x00, 0x70, 0x00, 0x00, 0x01 };
		byte[] rsp = transmit(manageChannelCommand, 3, 0x9000, 0xFFFF, "MANAGE CHANNEL");
		if (rsp.length != 3)
			throw new CardException("unsupported MANAGE CHANNEL response data");
		int channelNumber = rsp[0] & 0xFF;
		if (channelNumber == 0 || channelNumber > 19)
			throw new CardException("invalid logical channel number returned");

		SeekCardChannel channel = new SeekCardChannel(this, channelNumber);
		return channel;
	}

	/**
	 * Protocol specific implementation of the transmit operation.
	 * 
	 * @param cmd
	 *         the command to be transmitted.
	 * @return the response received.
	 * @throws CardException
	 *          if the transmit operation failed.
	 */
	private byte[] protocolTransmit(byte[] cmd) throws CardException {
		final boolean isT0 = ("T=0".equals(internalGetProtocol()));

		byte[] command = cmd;
		if (isT0 && isCase4(cmd)) {
			// Cut LE byte
			command = new byte[cmd.length - 1];
			System.arraycopy(cmd, 0, command, 0, cmd.length - 1);
		}

		byte[] rsp = internalTransmit(command);

		if (isT0 && rsp.length >= 2) {
			int sw1 = rsp[rsp.length - 2] & 0xFF;
			if (sw1 == 0x6C) {
				command[cmd.length - 1] = rsp[rsp.length - 1];
				rsp = internalTransmit(command);
			} else if (sw1 == 0x61) {
				byte[] getResponseCmd = new byte[] { command[0], (byte) 0xC0, 0x00, 0x00, 0x00 };
				byte[] response = new byte[rsp.length - 2];
				System.arraycopy(rsp, 0, response, 0, rsp.length - 2);
				while (true) {
					getResponseCmd[4] = rsp[rsp.length - 1];
					rsp = internalTransmit(getResponseCmd);
					if (rsp.length >= 2 && rsp[rsp.length - 2] == 0x61) {
						response = appendResponse(response, rsp, rsp.length - 2);
					} else {
						response = appendResponse(response, rsp, rsp.length);
						break;
					}
				}
				rsp = response;
			}
		}
		return rsp;
	}

	/**
	 * Transmits the specified command and returns the response.
	 * @param cmd
	 *         the command APDU to be transmitted.
	 * @return the response received.
	 * @throws CardException
	 *          if the transmit operation failed.
	 */
	byte[] transmit(byte[] cmd) throws CardException {
		return transmit(cmd, 0, 0, 0, null);
	}

	/**
	 * Transmits the specified command and returns the response.
	 * Optionally checks the response length and the response status word.
	 * The status word check is implemented as follows (sw = status word of the response):
	 * <p>
	 * if ((sw & swMask) != (swExpected & swMask)) throw new CardException();
	 * </p>
	 * 
	 * @param cmd
	 *         the command APDU to be transmitted.
	 * @param minRspLength
	 *         the minimum length of received response to be checked.
	 * @param swExpected
	 *         the response status word to be checked.
	 * @param swMask
	 *         the mask to be used for response status word comparison.
	 * @return the response received.
	 * @throws CardException
	 *          if the transmit operation or
	 *          the minimum response length check or the status word check failed.
	 */
	byte[] transmit(byte[] cmd, int minRspLength, int swExpected, int swMask) throws CardException {
		return transmit(cmd, minRspLength, swExpected, swMask, null);
	}

	/**
	 * Transmits the specified command and returns the response.
	 * Optionally checks the response length and the response status word.
	 * The status word check is implemented as follows (sw = status word of the response):
	 * <p>
	 * if ((sw & swMask) != (swExpected & swMask)) throw new CardException();
	 * </p>
	 * 
	 * @param cmd
	 *         the command APDU to be transmitted.
	 * @param minRspLength
	 *         the minimum length of received response to be checked.
	 * @param swExpected
	 *         the response status word to be checked.
	 * @param swMask
	 *         the mask to be used for response status word comparison.
	 * @param commandName
	 *          the name of the smart card command for logging purposes. May be <code>null</code>.
	 * @return the response received.
	 * @throws CardException
	 *          if the transmit operation or
	 *          the minimum response length check or the status word check failed.
	 */
	byte[] transmit(byte[] cmd, int minRspLength, int swExpected, int swMask, String commandName)
			throws CardException {
		byte[] rsp = null;
		try {
			synchronized (this) {
				assertTransaction();
				rsp = protocolTransmit(cmd);
			}
		} catch (CardNotPresentException e) {
			throw e;
		} catch (CardException e) {
			if (commandName == null)
				throw e;
			else
				throw new CardException(createMessage(commandName, "transmit failed"), e);
		}
		if (minRspLength > 0) {
			if (rsp == null || rsp.length < minRspLength)
				throw new CardException(createMessage(commandName, "response too small"));
		}
		if (swMask != 0) {
			if (rsp == null || rsp.length < 2)
				throw new CardException(createMessage(commandName, "SW1/2 not available"));
			int sw1 = rsp[rsp.length - 2] & 0xFF;
			int sw2 = rsp[rsp.length - 1] & 0xFF;
			int sw = (sw1 << 8) | sw2;
			if ((sw & swMask) != (swExpected & swMask))
				throw new CardException(createMessage(commandName, sw));
		}
		return rsp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardException {
		assertCardConnected();
		synchronized (this) {
			assertTransaction();
			byte[] response = internalControl(controlCode, command);
			return response;
		}
	}
}
