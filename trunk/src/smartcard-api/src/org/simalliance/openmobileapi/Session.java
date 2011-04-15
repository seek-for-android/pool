/*
 * Copyright 2011 Giesecke & Devrient GmbH.
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

package org.simalliance.openmobileapi;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.HashSet;
import java.util.Set;

/**
 * Instances of this class represent a connection session to one of the secure
 * elements available on the device. These objects can be used to get a
 * communication channel with an application in the secure element. This channel
 * can be the basic channel or a logical channel.
 */
public class Session {

	Reader _reader;
	String _name;
	boolean _isClosed;
	byte[] _atr;

	/** List of open channels in use of by this client. */
	private final Set<Channel> _channels = new HashSet<Channel>();

	Session(String name, Reader reader) {
		_atr = reader.getSEService().getAtr(reader);
		_reader = reader;
		_name = name;
		_isClosed = false;
	}

	/**
	 * Get an access to the basic channel, as defined in the ISO7816-4
	 * specification (the one that has number 0). The obtained object is an
	 * instance of the Channel class. The AID can be null, which means no SE
	 * application is to be selected on this channel, the default SE application
	 * is used. If it's not, then the corresponding SE application is selected.
	 * Once this channel has been opened by a device application, it is
	 * considered as "locked" by this device application, and other calls to
	 * this method will return null, until the channel is closed. Some secure
	 * elements (like the UICC) might always return null to applications, to
	 * prevent access to the basic channel, while some other might return a
	 * channel object implementing some kind of filtering on the commands,
	 * restricting the set of accepted command to a smaller set. There is no way
	 * for the application to retrieve the select response. Recommendation for
	 * an implementation: Any select response has to be fetched but should be
	 * discarded internally.
	 * <p>
	 * 
	 * @throws IOException
	 *             - if something goes wrong with the communication to the
	 *             reader or the secure element.
	 * @throws IllegalStateException
	 *             - if the secure element session is used after being closed.
	 * @throws IllegalArgumentException
	 *             - if the aid's length is not within 5 to 16 (inclusive).
	 * @throws SecurityException
	 *             - if the calling application cannot be granted access to this
	 *             AID or the default application on this session.
	 * 
	 * @param aid
	 *            of the applet which shall be selected on the secure element or
	 *            null for using the default selected applet.
	 * 
	 * @return an instance of Channel.
	 */
	public Channel openBasicChannel(byte[] aid) throws IOException {

		synchronized (_channels) {

			Channel basicChannel = _reader.getSEService().openBasicChannel(
					this, aid);
			_channels.add(basicChannel);
			return basicChannel;
		}
	}

	/**
	 * Open a logical channel with the secure element, selecting the application
	 * represented by the given AID. The AID can be null, which means no
	 * application is to be selected on this channel, the default application is
	 * used. It's up to the secure element to choose which logical channel will
	 * be used. There is no way for the application to retrieve the select
	 * response. Recommendation for an implementation: Any select response has
	 * to be fetched but should be discarded internally.
	 * 
	 * @param aid
	 *            the AID of the application to be selected on this channel, as
	 *            a byte array.
	 * 
	 * @throws IOException
	 *             if something goes wrong with the communication to the reader
	 *             or the secure element.
	 * @throws IllegalStateException
	 *             if the secure element is used after being closed.
	 * @throws IllegalArgumentException
	 *             if the aid's length is not within 5 to 16 (inclusive).
	 * @throws SecurityException
	 *             if the calling application cannot be granted access to this
	 *             AID or the default application on this session.
	 * 
	 * @param aid
	 *            the AID of the application to be selected on this channel, as
	 *            a byte array.
	 * 
	 * @return an instance of Channel.
	 */
	public Channel openLogicalChannel(byte[] aid) throws IOException {

		synchronized (_channels) {

			Channel logicalChannel = _reader.getSEService().openLogicalChannel(
					this, aid);
			_channels.add(logicalChannel);
			return logicalChannel;

		}
	}

	/**
	 * Close the connection with the secure element. This will close any
	 * channels opened by this application with this secure element.
	 */
	public void close() {

		closeChannels();
		_reader.closeSession(this);
	}

	/**
	 * Tells if this session is closed.
	 * 
	 * @return <code>true</code> if the session is closed.
	 */
	public boolean isClosed() {
		return _isClosed;
	}

	/**
	 * Get the Answer to Reset of this secure element. <br>
	 * The returned byte array can be null, which means the reader cannot
	 * provide the ATR for this Secure Element.
	 * 
	 * @return the ATR as a byte array.
	 */
	public byte[] getATR() {
		return _atr;
	}

	/**
	 * Get the reader that provides this session.
	 * 
	 * @return the Reader object.
	 */
	public Reader getReader() {
		return _reader;
	}

	/**
	 * Close any channel opened on this session.
	 */
	public void closeChannels() {

		synchronized (_channels) {
			for (Channel channel : _channels)
				closeChannel(channel);
		}
	}

	// ******************************************************************
	// package private methods
	// ******************************************************************

	/**
	 * Closes the specified channel. <br>
	 * After calling this method the session can not be used for the
	 * communication with the secure element any more.
	 * 
	 * @param hChannel
	 *            the channel handle obtained by an open channel command.
	 */
	void closeChannel(Channel channel) {

			synchronized (_channels) {

				if (!channel.isClosed()) {
					try {
						_reader.getSEService().closeChannel(channel);
					} catch (Exception ignore) {}
					
					channel.setClosed();
				}
				_channels.remove(channel);
			}
	}

	void setClosed() {
		_isClosed = true;
	}

}
