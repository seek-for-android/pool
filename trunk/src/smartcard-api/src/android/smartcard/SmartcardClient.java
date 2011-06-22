/*
 * Copyright (C) 2011, The Android Open Source Project
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
/*
 * Contributed by: Giesecke & Devrient GmbH.
 */

package android.smartcard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.security.AccessControlException;
import java.util.HashSet;
import java.util.Set;

/**
 * Adapter class to be used by clients of the smartcard service.
 * <p>
 * Encapsulates smartcard service binding and unbinding and provides a higher
 * level proxy for the smartcard interface.<br>
 * When done using the smartcard adapter, call the {@link #shutdown()} method to
 * release the acquired smartcard channel.
 */
public class SmartcardClient {

    /**
     * Interface definition of smartcard listener to be informed about smartcard
     * service connect and disconnect events.
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
    private final Set<CardChannel> mChannels = new HashSet<CardChannel>();

    /** The client context (e.g. activity). */
    private final Context mContext;

    /**
     * Listener to be informed about smartcard service connect and disconnect
     * events.
     */
    private final ISmartcardConnectionListener mConnectionListener;

    /** The smartcard service binder. */
    private volatile ISmartcardService mSmartcardService;

    /**
     * This implementation is used to receive callbacks from the smartcard
     * service.
     */
    private final ISmartcardServiceCallback mCallback = new ISmartcardServiceCallback.Stub() {
    };

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection;

    /**
     * Constructs a new smartcard adapter to be used by clients.
     * <p>
     * Note that connecting to the smartcard service is an asynchronous
     * operation. Clients should specify a listener during construction of this
     * instance to be informed on completion of the connect operation.
     * 
     * @param context the context (activity) which connects to the smartcard
     *            service. Must not be <code>null</code>.
     * @param listener a listener to be informed about smartcard service connect
     *            and disconnect events. May be <code>null</code>.
     * @throws SecurityException the client does not have the permission to bind
     *             the smartcard service.
     */
    public SmartcardClient(Context context, ISmartcardConnectionListener listener) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        this.mContext = context;
        this.mConnectionListener = listener;

        mConnection = new ServiceConnection() {
            public synchronized void onServiceConnected(ComponentName className, IBinder service) {
                mSmartcardService = ISmartcardService.Stub.asInterface(service);
                if (mConnectionListener != null) {
                    mConnectionListener.serviceConnected();
                }
                Log.v(SMARTCARD_TAG, "Smartcard service onServiceConnected");
            }

            public void onServiceDisconnected(ComponentName className) {
                synchronized (mChannels) {
                    for (CardChannel channel : mChannels) {
                        try {
                            channel.invalidate();
                        } catch (Exception ignore) {
                        }
                    }
                    mChannels.clear();
                }
                mSmartcardService = null;
                if (mConnectionListener != null) {
                    mConnectionListener.serviceDisconnected();
                }
                Log.v(SMARTCARD_TAG, "Smartcard service onServiceDisconnected");
            }
        };

        context.bindService(new Intent(ISmartcardService.class.getName()), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * Disconnect from the smartcard service and release all acquired smartcard
     * channels. It is recommended to call this method in the onDestroy() method
     * of the activity.
     */
    public void shutdown() {
        synchronized (mConnection) {
            if (mSmartcardService != null) {
                synchronized (mChannels) {
                    CardChannel[] channelList = mChannels
                            .toArray(new CardChannel[mChannels.size()]);
                    for (CardChannel channel : channelList) {
                        try {
                            channel.close();
                        } catch (Exception ignore) {
                        }
                    }
                }
            }
            try {
                mContext.unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                // Do nothing and fail silently since an error here indicates
                // that
                // binding never succeeded in the first place.
            }
        }
    }

    /**
     * Returns the friendly names of available smart card readers.
     * 
     * @return a list of available readers
     * @throws IllegalStateException if the smartcard service not connected to.
     * @throws CardException if a communication error with the smartcard service
     *             occurred.
     */
    public String[] getReaders() throws CardException {
        if (mSmartcardService == null) {
            throw new IllegalStateException("smartcard service not connected");
        }

        SmartcardError error = new SmartcardError();
        String[] readers;
        try {
            readers = mSmartcardService.getReaders(error);
        } catch (Exception e) {
            throw new CardException(e);
        }
        error.throwException();

        return readers;
    }

    /**
     * Returns <code>true</code> if a smartcard is present in the specified
     * reader.
     * 
     * @param reader the friendly name of the reader to be checked for card
     *            presence.
     * @return <code>true</code> if a smartcard is present <code>false</code> if
     *         a smartcard is absent
     * @throws NullPointerException if the reader name is <code>null</code>.
     * @throws IllegalStateException if the smartcard service is not connected.
     * @throws CardException if a communication error with the smartcard service
     *             occurred.
     */
    public boolean isCardPresent(String reader) throws CardException {
        if (reader == null) {
            throw new NullPointerException("reader must not be null");
        }
        if (mSmartcardService == null) {
            throw new IllegalStateException("smartcard service not connected");
        }

        SmartcardError error = new SmartcardError();
        boolean isPresent;
        try {
            isPresent = mSmartcardService.isCardPresent(reader, error);
        } catch (Exception e) {
            throw new CardException(e);
        }
        error.throwException();

        return isPresent;
    }

    /**
     * Opens a basic channel to the card in the specified reader.
     * <p>
     * Note that not all smartcards support communication on the basic channel.
     * E.g. the USIM card is already occupied by the GSM modem so only logical
     * channels can be used for communication.
     * 
     * @param reader the friendly name of the reader to be connected.
     * @return a basic channel to the card in the specified reader.
     * @throws NullPointerException if the reader name is <code>null</code>.
     * @throws IllegalStateException if the smartcard service is not connected.
     * @throws IllegalArgumentException if the reader name is unknown.
     * @throws AccessControlException if the access control conditions are not
     *             fullfilled.
     * @throws CardException if opening the basic channel failed.
     */
    public ICardChannel openBasicChannel(String reader) throws CardException {
        if (reader == null) {
            throw new NullPointerException("reader must not be null");
        }
        if (mSmartcardService == null) {
            throw new IllegalStateException("smartcard service not connected");
        }

        synchronized (mChannels) {
            SmartcardError error = new SmartcardError();
            long hChannel;
            try {
                hChannel = mSmartcardService.openBasicChannel(reader, mCallback, error);
            } catch (Exception e) {
                throw new CardException(e);
            }
            error.throwException();

            CardChannel basicChannel = new CardChannel(this, hChannel, false);
            mChannels.add(basicChannel);
            return basicChannel;
        }
    }

    /**
     * Opens a basic channel to the card in the specified reader and selects the
     * applet with the specified AID.
     * <p>
     * Note that not all smartcards support communication on the basic channel.
     * E.g. the USIM card is already occupied by the GSM modem so only logical
     * channels can be used for communication.
     * 
     * @param reader the friendly name of the reader to be connected.
     * @param aid the AID of the applet to be selected. Can be omitted by empty
     *            value or null: Then no selection will be performed (same as
     *            openBasicChannel(String reader) ).
     * @return a basic channel to the card in the specified reader.
     * @throws NullPointerException if the reader name or aid are
     *             <code>null</code>.
     * @throws IllegalStateException if the smartcard service is not connected.
     * @throws IllegalArgumentException if the reader name is unknown or the AID
     *             length is illegal.
     * @throws AccessControlException if the access control conditions are not
     *             fullfilled.
     * @throws CardException if applet selection failed.
     */
    public ICardChannel openBasicChannel(String reader, byte[] aid) throws CardException {
        if (reader == null) {
            throw new NullPointerException("reader must not be null");
        }
        if (aid == null) {
            throw new NullPointerException("AID must not be null");
        }
        if (aid.length < 5 || aid.length > 16) {
            throw new IllegalArgumentException("AID out of range");
        }
        if (mSmartcardService == null) {
            throw new IllegalStateException("smartcard service not connected");
        }

        synchronized (mChannels) {
            SmartcardError error = new SmartcardError();
            long hChannel;
            try {
                hChannel = mSmartcardService.openBasicChannelAid(reader, aid, mCallback, error);
            } catch (Exception e) {
                throw new CardException(e);
            }
            error.throwException();

            CardChannel logicalChannel = new CardChannel(this, hChannel, true);
            mChannels.add(logicalChannel);
            return logicalChannel;
        }
    }

    /**
     * Opens a new logical channel to the card in the specified reader and
     * selects the applet with the specified AID.
     * 
     * @param reader the friendly name of the reader to be connected.
     * @param aid the AID of the applet to be selected.
     * @return a new logical channel to the card in the specified reader.
     * @throws NullPointerException if the reader name or aid are
     *             <code>null</code>.
     * @throws IllegalStateException if the smartcard service is not connected.
     * @throws IllegalArgumentException if the reader name is unknown or the AID
     *             length is illegal.
     * @throws AccessControlException if the access control conditions are not
     *             fullfilled.
     * @throws CardException if opening the logical channel or applet selection
     *             failed.
     */
    public ICardChannel openLogicalChannel(String reader, byte[] aid) throws CardException {
        if (reader == null) {
            throw new NullPointerException("reader must not be null");
        }
        if (aid == null) {
            throw new NullPointerException("AID must not be null");
        }
        if (aid.length < 5 || aid.length > 16) {
            throw new IllegalArgumentException("AID out of range");
        }
        if (mSmartcardService == null) {
            throw new IllegalStateException("smartcard service not connected");
        }

        synchronized (mChannels) {
            SmartcardError error = new SmartcardError();
            long hChannel;
            try {
                hChannel = mSmartcardService.openLogicalChannel(reader, aid, mCallback, error);
            } catch (Exception e) {
                throw new CardException(e);
            }
            error.throwException();

            CardChannel logicalChannel = new CardChannel(this, hChannel, true);
            mChannels.add(logicalChannel);
            return logicalChannel;
        }
    }

    /**
     * Closes the specified channel.
     * 
     * @param hChannel the channel handle obtained by an open channel command.
     * @throws IllegalStateException if the smartcard service is not connected.
     */
    void closeChannel(CardChannel channel) throws CardException {
        if (mSmartcardService == null) {
            throw new IllegalStateException("smartcard service not connected");
        }

        SmartcardError error = new SmartcardError();
        synchronized (mChannels) {
            try {
                mSmartcardService.closeChannel(channel.getHandle(), error);
            } catch (Exception e) {
                throw new CardException(e);
            } finally {
                mChannels.remove(channel);
                channel.invalidate();
            }
        }
        error.throwException();
    }

    /**
     * Transmits the specified command APDU and returns the response APDU. <br>
     * The commands MANAGE CHANNEL and SELECT are not allowed. Use the
     * openChannel methods therefore.
     * 
     * @param hChannel the channel handle obtained by an open channel command.
     * @param command the command apdu which should be sent
     * @throws CardException if a communication error occurs
     * @throws NullPointerException if command is null
     * @throws IllegalArgumentException if command is to short (must have a
     *             length of at least 4 bytes)
     * @throws AccessControlException if the access control conditions are not
     *             fullfilled.
     * @throws IllegalStateException if the smartcard service is not connected.
     */
    byte[] transmit(long hChannel, byte[] command) throws CardException {
        if (command == null) {
            throw new NullPointerException("command must not be null");
        }
        if (command.length < 4) {
            throw new IllegalArgumentException("command must have at least 4 bytes");
        }
        if (mSmartcardService == null) {
            throw new IllegalStateException("smartcard service not connected");
        }

        SmartcardError error = new SmartcardError();
        byte[] response;
        try {
            response = mSmartcardService.transmit(hChannel, command, error);
        } catch (Exception e) {
            throw new CardException(e);
        }
        error.throwException();

        return response;
    }
}
