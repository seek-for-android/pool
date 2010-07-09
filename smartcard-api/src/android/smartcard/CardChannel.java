/*
 * Copyright 2010 Giesecke & Devrient GmbH.
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

/**
 * Implementation of a basic or logical card channel abstraction
 * managed by a <code>SmartcardClient</code> instance, connected to the smartcard service.
 */
final class CardChannel implements ICardChannel {

	private final SmartcardClient client;
	private final long hChannel;
	private final boolean isLogicalChannel;
	private volatile boolean isClosed;

	/**
	 * Constructs a new abstraction of a smartcard based card channel.
	 * @param client
	 *          the smartcard client instance to which this channel is associated.
	 * @param hChannel
	 *          the handle associated to this card channel by the smartcard service.
	 * @param isLogicalChannel
	 *          <code>true</code> if this is a logical channel, <code>false</code> if this is a basic channel.
	 */
	CardChannel(SmartcardClient client, long hChannel, boolean isLogicalChannel) {
		this.client = client;
		this.hChannel = hChannel;
		this.isLogicalChannel = isLogicalChannel;
	}

	/**
	 * Closes the card channel.
     * @throws IllegalStateException
     *           if the smartcard service is not connected.
     * @throws IllegalArgumentException
     *           if the channel handle is unknown.
	 * @throws CardException
	 *           if closing the channel failed.
	 */
	public void close() throws CardException {
		if (isClosed)
			return;
		try {
			client.closeChannel(this);
		} catch (IllegalArgumentException ignore) {
			// cannot happen unless uses modifies the card handle manually
		} catch (IllegalStateException ignore) {
			// nothing we can do here as the smartcard service died
		}
	}

	/**
	 * Returns the ATR of the card connected with this channel.
	 * @return the ATR of the card connected with this channel.
     * @throws IllegalStateException
     *           if the smartcard service is not connected.
     * @throws IllegalArgumentException
     *           if the channel handle is unknown.
	 * @throws CardException
	 *           if a smartcard service communication error occurred.
	 */
	public byte[] getAtr() throws CardException {
		assertOpen();
		try {
			return client.getAtr(hChannel);
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("channel is closed");
		}
	}

	/**
     * Returns <code>true</code> if the card channel was closed previously.
     * @return <code>true</code> if the card channel was closed previously.
     *           <code>false</code> if the card channel is still valid.
     */
	public boolean isClosed() {
		return isClosed;
	}

	/**
     * Returns <code>true</code> if the card channel is a logical channel.
     * @return <code>true</code> if the card channel is a logical channel.
     *           <code>false</code> the card channel is a basic channel.
     */
	public boolean isLogicalChannel() {
		return isLogicalChannel;
	}

	/**
	 * Transmits the specified command APDU and returns the response APDU.
	 * MANAGE channel commands are not allowed.
	 * Applet selection commands are not allowed if this is a logical channel. 
	 * @param command
	 *          the command APDU to be transmitted.
	 * @return the response APDU.
	 * @throws NullPointerException
	 *           if command is <code>null</code>.
	 * @throws IllegalArgumentException
	 *           if the channel handle is unknown or command is shorter than 4 bytes.
	 * @throws IllegalStateException
     *           if the smartcard service is not connected.
	 * @throws CardException
	 *           if command transmission failed or the command is not allowed.
	 */
	public byte[] transmit(byte[] command) throws CardException {
		assertOpen();
		try {
			return client.transmit(hChannel, command);
		} catch (IllegalArgumentException e) {
			if (e.getMessage().toLowerCase().startsWith("invalid handle")) {
				throw new IllegalStateException("channel is closed");
			} else {
				throw e;
			}
		}
	}

	/**
	 * Returns the handle associated to this card channel.
	 * @return the handle associated to this card channel.
	 */
	long getHandle() {
		return hChannel;
	}
	
	/**
	 * Invalidates this card channel.
	 */
	void invalidate() {
		isClosed = true;
	}

	/**
	 * Asserts that this channel is open.
	 */
	private void assertOpen() {
		if (isClosed) {
			throw new IllegalStateException("channel is closed");
		}
	}
}
