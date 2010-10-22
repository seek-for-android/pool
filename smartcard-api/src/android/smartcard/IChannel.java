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
 * Smartcard service interface for channel resources.
 */
interface IChannel {

	/**
	 * Closes this channel.
	 * @throws CardException
	 *           if closing the channel failed.
	 */
	void close() throws CardException;
	
	/**
	 * Returns the channel number according to ISO 7816-4.
	 * @return the channel number according to ISO 7816-4.
	 */
	int getChannelNumber();
	
	/**
	 * Transmits the specified command APDU and returns the response APDU.
	 * MANAGE channel commands are not allowed.
	 * Applet selection commands are not allowed if this is a logical channel. 
	 * @param command
	 *          the command APDU to be transmitted.
	 * @return the response APDU.
	 * @throws CardException
	 *           if command transmission failed or the command is not allowed.
	 */
	byte[] transmit(byte[] command) throws CardException;
}
