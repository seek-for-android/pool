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

import java.util.HashSet;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.smartcard.ISmartcardService;
import android.smartcard.ISmartcardServiceCallback;
import android.util.Log;

/**
 * Adapter class to be used by clients of the smartcard service.
 * <p>Encapsulates smartcard service binding and unbinding and provides
 * a higher level proxy for the smartcard interface.<br>
 * When done using the smartcard adapter, call the {@link #shutdown()} method
 * to release the acquired smartcard channel.
 */
public class SmartcardClient {
	
	/**
	 * Interface definition of smartcard listener to be informed about
	 * smartcard service connect and disconnect events.
	 */
	public interface ISmartcardConnectionListener {
	
		/**
		 * Called when the smartcard service is connected or reconnected.
		 */
		void serviceConnected();
		
		/**
		 * Called when the smartcard service is disconnected (because it died).
		 */
		void serviceDisconnected();
	}
	
	private static final String SMARTCARD_TAG = "SmartcardApi";
	
	/** List of open channels in use of by this client. */
	private final Set<CardChannel> channels = new HashSet<CardChannel>();
	
	/** The client context (e.g. activity). */
	private final Context context;
	
	/** Listener to be informed about smartcard service connect and disconnect events. */
	private final ISmartcardConnectionListener connectionListener;
	
	/** The smartcard service binder. */
	private volatile ISmartcardService smartcardService;

    /**
     * This implementation is used to receive callbacks from the smartcard service.
     */
    private final ISmartcardServiceCallback callback = new ISmartcardServiceCallback.Stub() {};

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection connection;

    /**
     * Constructs a new smartcard adapter to be used by clients.
	 * <p>Note that connecting to the smartcard service is an asynchronous operation.
	 * Clients should specify a listener during construction of this instance to
	 * be informed on completion of the connect operation.
     * @param context
     *           the context (activity) which connects to the smartcard service. Must not be <code>null</code>.
     * @param listener
     *           a listener to be informed about smartcard service connect and disconnect events. May be <code>null</code>.
	 * @throws SecurityException
	 *           the client does not have the permission to bind the smartcard service.
     */
	public SmartcardClient(Context context, ISmartcardConnectionListener listener) {
		if (context == null)
			throw new NullPointerException("context must not be null");
		this.context = context;
		this.connectionListener = listener;
		
		connection = new ServiceConnection() {    	
		    public synchronized void onServiceConnected(ComponentName className, IBinder service) {
		    	smartcardService = ISmartcardService.Stub.asInterface(service);
		    	if (connectionListener != null) { 
		    		connectionListener.serviceConnected();
		    	}
		    	Log.v(SMARTCARD_TAG, "Smartcard service onServiceConnected");
		    }
		    
		    public void onServiceDisconnected(ComponentName className) {
				synchronized (channels) {
					for (CardChannel channel : channels) {
						try {
							channel.invalidate();
						} catch (Exception ignore) {
						}
					}
					channels.clear();
				}
				smartcardService = null;
		    	if (connectionListener != null) { 
		    		connectionListener.serviceDisconnected();
		    	}
		    	Log.v(SMARTCARD_TAG, "Smartcard service onServiceDisconnected");
		    }
		};

	    context.bindService(new Intent(ISmartcardService.class.getName()), connection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * Disconnect from the smartcard service and release all acquired
	 * smartcard channels.
	 * It is recommended to call this method in the onDestroy() method
	 * of the activity.
	 */
	public void shutdown() {
		synchronized (connection) {
			if (smartcardService != null) {
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
			try {
				context.unbindService(connection);
			} catch (IllegalArgumentException e) {
				// Do nothing and fail silently since an error here indicates that
	            // binding never succeeded in the first place.
			}
		}
	}	

	/**
     * Returns the friendly names of available smart card readers.
     * @throws IllegalStateException
     *           if the smartcard service not connected to.
     * @throws CardException
     *           if a communication error with the smartcard service occurred.
     */
	public String[] getReaders() throws CardException {
		if (smartcardService == null)
			throw new IllegalStateException("smartcard service not connected");
		
		SmartcardError error = new SmartcardError();
		String[] readers;
		try {
			readers = smartcardService.getReaders(error);
		} catch (Exception e) {
			throw new CardException(e);
		}
		error.throwException();
		
		return readers;
	}
	
	/**
     * Returns <code>true</code> if a smartcard is present in the specified reader.
     * @return <code>true</code> if a smartcard is present
     *           <code>false</code> if a smartcard is absent
     * @param reader
     *           the friendly name of the reader to be checked for card presence.
     * @throws NullPointerException
     *           if the reader name is <code>null</code>.
     * @throws IllegalStateException
     *           if the smartcard service is not connected.
     * @throws CardException
     *           if a communication error with the smartcard service occurred.
     */
	public boolean isCardPresent(String reader) throws CardException {
		if (reader == null)
			throw new NullPointerException("reader must not be null");
		if (smartcardService == null)
			throw new IllegalStateException("smartcard service not connected");
		
		SmartcardError error = new SmartcardError();
		boolean isPresent;
		try {
			isPresent = smartcardService.isCardPresent(reader, error);
		} catch (Exception e) {
			throw new CardException(e);
		}
		error.throwException();
		
		return isPresent;
	}
	
	/**
	 * Opens a basic channel to the card in the specified reader.
	 * <p>Note that not all smartcards support communication on the basic channel.
	 * E.g. the USIM card is already occupied by the GSM modem so only logical
	 * channels can be used for communication.
	 * @param reader
	 *         the friendly name of the reader to be connected.
	 * @return a basic channel to the card in the specified reader.
	 * @throws NullPointerException
	 *           if the reader name is <code>null</code>.
     * @throws IllegalStateException
     *           if the smartcard service is not connected.
	 * @throws IllegalArgumentException
	 *           if the reader name is unknown.
	 * @throws CardException
	 *           if opening the basic channel failed.
	 */
	public ICardChannel openBasicChannel(String reader) throws CardException {
		if (reader == null)
			throw new NullPointerException("reader must not be null");
		if (smartcardService == null)
			throw new IllegalStateException("smartcard service not connected");
		
		synchronized (channels) {
			SmartcardError error = new SmartcardError();
			long hChannel;
			try {
				hChannel = smartcardService.openBasicChannel(reader, callback, error);
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
     *           if the smartcard service is not connected.
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
		if (smartcardService == null)
			throw new IllegalStateException("smartcard service not connected");
		
		synchronized (channels) {
			SmartcardError error = new SmartcardError();
			long hChannel;
			try {
				hChannel = smartcardService.openLogicalChannel(reader, aid, callback, error);
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
	 * Closes the specified channel.
	 */
	void closeChannel(CardChannel channel) throws CardException {
		if (smartcardService == null)
			throw new IllegalStateException("smartcard service not connected");
		
		SmartcardError error = new SmartcardError();
		synchronized (channels) {
			try {
				smartcardService.closeChannel(channel.getHandle(), error);
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
	 * Transmits the specified command APDU and returns the response APDU.
	 * MANAGE channel commands are not allowed.
	 * Applet selection commands are not allowed if this is a logical channel. 
	 */
	byte[] transmit(long hChannel, byte[] command) throws CardException {
		if (command == null)
			throw new NullPointerException("command must not be null");
		if (command.length < 4)
			throw new IllegalArgumentException("command must have at least 4 bytes");
		if (smartcardService == null)
			throw new IllegalStateException("smartcard service not connected");
		
		SmartcardError error = new SmartcardError();
		byte[] response;
		try {
			response = smartcardService.transmit(hChannel, command, error);
		} catch (Exception e) {
			throw new CardException(e);
		}
		error.throwException();
		
		return response;
	}
}
