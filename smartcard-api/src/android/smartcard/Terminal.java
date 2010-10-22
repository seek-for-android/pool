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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.Context;

/**
 * Smartcard service base class for terminal resources.
 */
abstract class Terminal implements ITerminal {
	
	/** Random number generator used for handle creation. */
	static Random random = new Random();

	protected Context context;
	
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
    
    private final Map<Long, IChannel> channels = new HashMap<Long, IChannel>();
    
	protected final String name;
	
	protected volatile boolean isConnected;
	
	Terminal(String name, Context context) {
		this.context = context;
		this.name = name;
	}
	
	public void closeChannels() {
		synchronized (channels) {
			Collection<IChannel> col = channels.values();
			IChannel[] channelList = col.toArray(new IChannel[col.size()]);
			for (IChannel channel : channelList) {
				try {
					channel.close();
				} catch (Exception ignore) {
				}
			}
		}
	}
	
	/**
	 * Closes the specified channel.
	 * @param channel
	 *           the channel to be closed.
	 * @throws CardException
	 *           if closing the channel failed.
	 */
	void closeChannel(Channel channel) throws CardException {
		synchronized (channels) {
			try {
				internalCloseLogicalChannel(channel.getChannelNumber());
			} finally {
				channels.remove(channel.getHandle());
				if (isConnected && channels.isEmpty()) {
					try {
						internalDisconnect();
					} catch (Exception ignore) {
					}
				}
			}
		}
	}
		
	/**
	 * Creates a channel instance.
	 * @param channelNumber
	 *           the channel number according to ISO 7816-4.
	 * @param callback
	 *           the callback used to detect the death of the client.
	 * @return a channel instance.
	 */
	protected Channel createChannel(int channelNumber, ISmartcardServiceCallback callback) {
		return new Channel(this, channelNumber, callback);
	}
	
	private IChannel getBasicChannel() {
		for (IChannel channel : channels.values()) {
			if (channel.getChannelNumber() == 0)
				return channel;
		}
		return null;
	}
	
	public IChannel getChannel(long hChannel) {
		synchronized (channels) {
			return channels.get(hChannel);
		}
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Implements the terminal specific connect operation.
	 * @throws CardException
	 *           if connecting the card failed.
	 */
	protected abstract void internalConnect() throws CardException;

	/**
	 * Implements the terminal specific disconnect operation.
	 * @throws CardException
	 *           if disconnecting from the card failed.
	 */
	protected abstract void internalDisconnect() throws CardException;
	
	/**
	 * Implementation of the MANAGE CHANNEL open command.
	 * @return the number of the logical channel according to ISO 7816-4.
	 * @throws CardException
	 */
	abstract protected int internalOpenLogicalChannel(byte[] aid) throws CardException;
	
	/**
	 * Implementation of the MANAGE CHANNEL close command.
	 * @param channelNumber
	 * @throws CardException
	 */
	abstract protected void internalCloseLogicalChannel(int channelNumber) throws CardException;

	/**
	 * Implements the terminal specific transmit operation.
	 * @param command
	 *            the command APDU to be transmitted.
	 * @return the response APDU received.
	 * @throws CardException
	 *           if the transmit operation failed.
	 */
	protected abstract byte[] internalTransmit(byte[] command) throws CardException;

	
	public long openBasicChannel(ISmartcardServiceCallback callback) throws CardException {
		if (callback == null)
			throw new NullPointerException("callback must not be null");
		
		synchronized (channels) {
			if (getBasicChannel() != null) {
				throw new CardException("basic channel in use");
			}
			if (channels.isEmpty()) {
				internalConnect();
			}
			
			Channel basicChannel = createChannel(0, callback);
			long hChannel = registerChannel(basicChannel);	
			return hChannel;
		}
	}
		
	public long openLogicalChannel(byte[] aid, ISmartcardServiceCallback callback) throws CardException {
		if (callback == null)
			throw new NullPointerException("callback must not be null");
		
		synchronized (channels) {
			if (channels.isEmpty()) {
				internalConnect();
			}
			
			int channelNumber;
			try {
				channelNumber = internalOpenLogicalChannel(aid);
			} catch (CardException e) {
				if (isConnected && channels.isEmpty()) {
					internalDisconnect();
				}
				throw e;
			}
								
			Channel logicalChannel = createChannel(channelNumber, callback);
			long hChannel = registerChannel(logicalChannel);	
			return hChannel;				
		}
	}
	
	/**
	 * Protocol specific implementation of the transmit operation.
	 * This method is synchronized in order to handle GET RESPONSE and
	 * command repetition without interruption by other commands.
	 * @param cmd
	 *         the command to be transmitted.
	 * @return the response received.
	 * @throws CardException
	 *          if the transmit operation failed.
	 */
	protected synchronized byte[] protocolTransmit(byte[] cmd) throws CardException {
		byte[] command = cmd;
		byte[] rsp = internalTransmit(command);

		if (rsp.length >= 2) {
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
	 * Creates a handle for the specified channel instances and adds
	 * the channel instance to the channel list.
	 * @param channel
	 * @return the channel handle.
	 */
	private long registerChannel(Channel channel) {
		long hChannel = random.nextInt();
		hChannel <<= 32;
		hChannel |= (((long) channel.hashCode()) & 0xFFFFFFFFL);
		
		channel.setHandle(hChannel);
		
		channels.put(hChannel, channel);
		
		return hChannel;
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
	byte[] transmit(byte[] cmd, int minRspLength, int swExpected, int swMask, String commandName) throws CardException {
		byte[] rsp = null;
		try {
			rsp = protocolTransmit(cmd);
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
}
