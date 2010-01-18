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

import java.util.HashSet;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Adapter class to be used by clients of the SEEK service.
 * Encapsulates SEEK service binding and unbinding and provides
 * a higher level proxy for the flat SEEK service interface. 
 */
public class SeekClient {
	
	/**
	 * Listener to be informed about SEEK service connect and disconnect events.
	 */
	public interface ISeekConnectionListener {
	
		/**
		 * Called when the SEEK service is connected or reconnected.
		 */
		void seekConnected();
		
		/**
		 * Called when the SEEK service is disconnected (because it died).
		 */
		void seekDisconnected();
	}
	
	public static final String SEEK_CLIENT_TAG = "SeekClient";
	
	/** List of open channels in use of by this client. */
	private final Set<CardChannel> channels = new HashSet<CardChannel>();
	
	/** The client context (e.g. activity). */
	private final Context context;
	
	/** Listener to be informed about SEEK service connect and disconnect events. */
	private final ISeekConnectionListener connectionListener;
	
	/** The SEEK service binder. */
	private volatile ISeekService seekService;
	
	/** <code>true</code> if the SEEK service is bound, <code>false</code> if it is not bound. */
	private volatile boolean isBound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private final ServiceConnection connection = new ServiceConnection() {
    	
    	@Override
        public synchronized void onServiceConnected(ComponentName className, IBinder service) {
        	seekService = ISeekService.Stub.asInterface(service);
        	isBound = true;
        	if (connectionListener != null) { 
        		connectionListener.seekConnected();
        	}
        	Log.v(SEEK_CLIENT_TAG, Thread.currentThread().getName() + " SEEK service onServiceConnected");
        }
        
    	@Override
        public void onServiceDisconnected(ComponentName className) {
        	Log.v(SEEK_CLIENT_TAG, Thread.currentThread().getName() + " SEEK service onServiceDisconnected ...");
			synchronized (channels) {
				for (CardChannel channel : channels) {
					try {
						channel.invalidate();
					} catch (Exception ignore) {
					}
				}
				channels.clear();
			}
        	seekService = null;
        	if (connectionListener != null) { 
        		connectionListener.seekDisconnected();
        	}
        	Log.v(SEEK_CLIENT_TAG, Thread.currentThread().getName() + " ... SEEK service onServiceDisconnected");
        }
    };

    /**
     * This implementation is used to receive callbacks from the SEEK service.
     */
    private final ISeekServiceCallback callback = new ISeekServiceCallback.Stub() {};

    /**
     * Constructs a new adapter to be used by clients of the SEEK service.
     * @param context
     *           the context (activity) which connects to the SEEK service. Must not be <code>null</code>.
     * @param listener
     *           a listener to be informed about SEEK service connect and disconnect events. May be <code>null</code>.
     */
	public SeekClient(Context context, ISeekConnectionListener listener) {
		if (context == null)
			throw new NullPointerException("context must not be null");
		this.context = context;
		this.connectionListener = listener;
	}
	
	/**
	 * Connects to the SEEK service, creating it if needed.
	 * Note that connecting to the SEEK service is an asynchronous operation.
	 * Clients should specify a listener during construction of this instance to
	 * be informed on completion of the connect operation.
	 * Methods of this instance throw an <code>IllegalStateException</code>
	 * if the SEEK service is not connected. Clients may use the <code>isBound</code>
	 * method to verify that the connection is established.
	 * @throws SecurityException
	 *           the client does not have the permission to bind the SEEK service.
	 */
	public void bindService() {
		context.bindService(new Intent(ISeekService.class.getName()), connection, Context.BIND_AUTO_CREATE);
	}
	
	/**
	 * Closes the specified channel.
	 * @param channel
	 *           the channel abstraction created with one of the open methods.
     * @throws IllegalStateException
     *           if the SEEK service is not connected.
     * @throws IllegalArgumentException
     *           if the channel handle is unknown.
	 * @throws CardException
	 *           if closing the channel failed.
	 */
	void closeChannel(CardChannel channel) throws CardException {
		if (seekService == null)
			throw new IllegalStateException("SEEK service not connected");
		
		SeekError error = new SeekError();
		synchronized (channels) {
			try {
				seekService.closeChannel(channel.getHandle(), error);
			} catch (Exception e) {
				throw new CardException(e);
			} finally {
				channels.remove(channel);
				channel.invalidate();
			}
		}
		error.throwException();
	}
	
	/**
	 * Returns the ATR of the card connected with this channel.
	 * @return the ATR of the card connected with this channel.
     * @throws IllegalStateException
     *           if the SEEK service is not connected.
     * @throws IllegalArgumentException
     *           if the channel handle is unknown.
	 * @throws CardException
	 *           if a SEEK service communication error occurred.
	 */
	byte[] getAtr(long hChannel) throws CardException {
		if (seekService == null)
			throw new IllegalStateException("SEEK service not connected");
		
		SeekError error = new SeekError();
		byte[] atr;
		try {
			atr = seekService.getAtr(hChannel, error);
		} catch (Exception e) {
			throw new CardException(e);
		}
		error.throwException();
		
		return atr;
	}

	/**
     * Returns the friendly names of available smart card readers.
     * @throws IllegalStateException
     *           if the SEEK service not connected to.
     * @throws CardException
     *           if a communication error with the SEEK service occurred.
     */
	public String[] getReaders() throws CardException {
		if (seekService == null)
			throw new IllegalStateException("SEEK service not connected");
		
		SeekError error = new SeekError();
		String[] readers;
		try {
			readers = seekService.getReaders(error);
		} catch (Exception e) {
			throw new CardException(e);
		}
		error.throwException();
		
		return readers;
	}
	
	/**
	 * Returns <code>true</code> if the service is bound.
	 * @return
	 */
	public boolean isBound() {
		return isBound;
	}
	
	/**
     * Returns the friendly names of available smart card readers.
     * @param reader
     *           the friendly name of the reader to be checked for card presence.
     * @throws NullPointerException
     *           if the reader name is <code>null</code>.
     * @throws IllegalStateException
     *           if the SEEK service is not connected.
	 * @throws IllegalArgumentException
	 *           if the reader name is unknown.
     * @throws CardException
     *           if a communication error with the SEEK service occurred.
     */
	public boolean isCardPresent(String reader) throws CardException {
		if (reader == null)
			throw new NullPointerException("reader must not be null");
		if (seekService == null)
			throw new IllegalStateException("SEEK service not connected");
		
		SeekError error = new SeekError();
		boolean isPresent;
		try {
			isPresent = seekService.isCardPresent(reader, error);
		} catch (Exception e) {
			throw new CardException(e);
		}
		error.throwException();
		
		return isPresent;
	}
	
	/**
	 * Opens a basic channel to the card in the specified reader.
	 * @param reader
	 *         the friendly name of the reader to be connected.
	 * @return a basic channel to the card in the specified reader.
	 * @throws NullPointerException
	 *           if the reader name is <code>null</code>.
     * @throws IllegalStateException
     *           if the SEEK service is not connected.
	 * @throws IllegalArgumentException
	 *           if the reader name is unknown.
	 * @throws CardException
	 *           if opening the basic channel failed.
	 */
	public ICardChannel openBasicChannel(String reader) throws CardException {
		if (reader == null)
			throw new NullPointerException("reader must not be null");
		if (seekService == null)
			throw new IllegalStateException("SEEK service not connected");
		
		synchronized (channels) {
			SeekError error = new SeekError();
			long hChannel;
			try {
				hChannel = seekService.openBasicChannel(reader, callback, error);
			} catch (Exception e) {
				throw new CardException(e);
			}
			error.throwException();
			
			CardChannel basicChannel = new CardChannel(this, hChannel, false);
			channels.add(basicChannel);
			return basicChannel;
		}
	}

	/**
	 * Opens a new logical channel to the card in the specified reader
	 * and selects the applet with the specified AID.
	 * @param reader
	 *         the friendly name of the reader to be connected.
	 * @param aid
	 *         the AID of the applet to be selected.
	 * @return a new logical channel to the card in the specified reader.
	 * @throws NullPointerException
	 *           if the reader name or aid are <code>null</code>.
     * @throws IllegalStateException
     *           if the SEEK service is not connected.
	 * @throws IllegalArgumentException
	 *           if the reader name is unknown or the AID length is illegal.
	 * @throws CardException
	 *           if opening the logical channel or applet selection failed.
	 */
	public ICardChannel openLogicalChannel(String reader, byte[] aid) throws CardException {
		if (reader == null)
			throw new NullPointerException("reader must not be null");
		if (aid == null)
			throw new NullPointerException("AID must not be null");
		if (aid.length < 5 || aid.length > 16)
			throw new IllegalArgumentException("AID out of range");
		if (seekService == null)
			throw new IllegalStateException("SEEK service not connected");
		
		synchronized (channels) {
			SeekError error = new SeekError();
			long hChannel;
			try {
				hChannel = seekService.openLogicalChannel(reader, aid, callback, error);
			} catch (Exception e) {
				throw new CardException(e);
			}
			error.throwException();
			
			CardChannel logicalChannel = new CardChannel(this, hChannel, true);
			channels.add(logicalChannel);
			return logicalChannel;
		}
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
     *           if the SEEK service is not connected.
	 * @throws CardException
	 *           if command transmission failed or the command is not allowed.
	 */
	byte[] transmit(long hChannel, byte[] command) throws CardException {
		if (command == null)
			throw new NullPointerException("command must not be null");
		if (command.length < 4)
			throw new IllegalArgumentException("command must have at least 4 bytes");
		if (seekService == null)
			throw new IllegalStateException("SEEK service not connected");
		
		SeekError error = new SeekError();
		byte[] response;
		try {
			response = seekService.transmit(hChannel, command, error);
		} catch (Exception e) {
			throw new CardException(e);
		}
		error.throwException();
		
		return response;
	}
	
	/**
	 * Disconnect from the SEEK service.
	 * You will no longer receive calls as the SEEK service is restarted,
	 * and the SEEK service is now allowed to stop at any time.
	 */
	public void unbindService() {
		synchronized (connection) {
			if (isBound) {
				if (seekService != null) {
					synchronized (channels) {
						CardChannel[] channelList = channels.toArray(new CardChannel[channels.size()]);
						for (CardChannel channel : channelList) {
							try {
								channel.close();
							} catch (Exception ignore) {
							}
						}
					}
				}
				context.unbindService(connection);
				isBound = false;
			}
		}
	}
	
}
