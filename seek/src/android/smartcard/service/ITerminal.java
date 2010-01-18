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

package android.smartcard.service;

/**
 * SEEK service interface for terminal resources.
 */
interface ITerminal {

	/**
	 * Closes all open channels of this terminal.
	 */
	void closeChannels();
	
	/**
	 * Returns the channel for the specified handle or <code>null</code> if this handle is not registered.
	 * @param hChannel
	 *          the channel handle.
	 * @return the channel for the specified handle or <code>null</code> if this handle is not registered.
	 */
	IChannel getChannel(long hChannel);
	
	/**
	 * Returns the reader name.
	 * @return the reader name.
	 */
	String getName();
	
	/**
	 * Opens the basic channel to the card.
	 * @param callback
	 *          the callback used to react on the death of the client.
	 * @return a handle for the basic channel.
	 * @throws CardException
	 *          if opening the basic channel failed or the basic channel is in use.
	 */
	long openBasicChannel(ISeekServiceCallback callback) throws CardException;
	
	/**
	 * Opens a logical channel to the card.
	 * @param aid
	 *          the AID of the applet to be selected.
	 * @param callback
	 *          the callback used to react on the death of the client.
	 * @return a handle for the logical channel.
	 * @throws CardException
	 *          if opening the logical channel failed.
	 */
	long openLogicalChannel(byte[] aid, ISeekServiceCallback callback) throws CardException;
	
	/**
	 * Returns <code>true</code> if a card is present; <code>false</code> otherwise.
	 * @return <code>true</code> if a card is present; <code>false</code> otherwise.
	 * @throws CardException
	 *          if card presence information is not available.
	 */
	boolean isCardPresent() throws CardException;
}
