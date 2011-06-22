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

package org.simalliance.openmobileapi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.smartcard.CardException;
import android.smartcard.ISmartcardService;
import android.smartcard.ISmartcardServiceCallback;
import android.smartcard.SmartcardError;
import android.util.Log;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.MissingResourceException;

/**
 * The SEService realizes the communication to available Secure Elements on the
 * device. This is the entry point of this API. It is used to connect to the
 * infrastructure and get access to a list of Secure Element Readers.
 */
public class SEService {

    private static final String SERVICE_TAG = "SEService";

    /** The client context (e.g. activity). */
    private final Context mContext;

    /** The backend system. */
    private volatile ISmartcardService mSmartcardService;

    /**
     * Class for interacting with the main interface of the backend.
     */
    private ServiceConnection mConnection;

    /**
     * Collection of available readers
     */
    private Reader[] mReaders;

    /**
     * This implementation is used to receive callbacks from backend.
     */
    private final ISmartcardServiceCallback mCallback = new ISmartcardServiceCallback.Stub() {
    };

    /**
     * Callback object that allows the notification of the caller if this
     * SEService could be bound to the backend.
     */
    private CallBack mCallerCallback;

    /**
     * Interface to receive call-backs when the service is connected. If the
     * target language and environment allows it, then this shall be an inner
     * interface of the SEService class.
     */
    public interface CallBack {

        /**
         * Called by the framework when the service is connected.
         * 
         * @param service the connected service.
         */
        void serviceConnected(SEService service);
    }

    /**
     * Establishes a new connection that can be used to connect to all the
     * Secure Elements available in the system. The connection process can be
     * quite long, so it happens in an asynchronous way. It is usable only if
     * the specified listener is called or if isConnected() returns
     * <code>true</code>. <br>
     * The call-back object passed as a parameter will have its
     * serviceConnected() method called when the connection actually happen..
     * 
     * @param context the context of the calling application. Cannot be
     *            <code>null</code>.
     * @param listener a SEService.CallBack object. Can be <code>null</code>.
     */
    public SEService(Context context, SEService.CallBack listener) {

        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        if (listener == null) {
            throw new NullPointerException("listener must not be null");
        }

        mContext = context;
        mCallerCallback = listener;

        mConnection = new ServiceConnection() {

            public synchronized void onServiceConnected(ComponentName className, IBinder service) {

                mSmartcardService = ISmartcardService.Stub.asInterface(service);
                if (mCallerCallback != null) {
                    mCallerCallback.serviceConnected(SEService.this);
                }
                Log.v(SERVICE_TAG, "Service onServiceConnected");
            }

            public void onServiceDisconnected(ComponentName className) {

                synchronized (mReaders) {

                    for (Reader reader : mReaders) {
                        try {
                            reader.closeSessions();
                        } catch (Exception ignore) {
                        }
                    }
                }
                mSmartcardService = null;
                Log.v(SERVICE_TAG, "Service onServiceDisconnected");
            }
        };

        boolean bindingSuccessful = mContext.bindService(new Intent(ISmartcardService.class
                .getName()), mConnection, Context.BIND_AUTO_CREATE);
        if (bindingSuccessful) {
            Log.v(SERVICE_TAG, "bindService successful");
        }
    }

    /**
     * Tells whether or not the service is connected.
     * 
     * @return <code>true</code> if the service is connected.
     */
    public boolean isConnected() {
        if (mSmartcardService == null) {
            return false;
        }
        return true;
    }

    /**
     * Returns the list of available secure element readers. More precisely it
     * returns the list of readers that the calling application has the
     * permission to connect to.
     * 
     * @return the readers list, as an array of Readers.
     */
    public Reader[] getReaders() {
        if (mSmartcardService == null) {
            throw new IllegalStateException("service not connected to system");
        }

        SmartcardError error = new SmartcardError();
        String[] readerNames;
        try {
            readerNames = mSmartcardService.getReaders(error);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        checkForException(error);

        mReaders = new Reader[readerNames.length];
        int i = 0;
        for (String readerName : readerNames) {
            mReaders[i] = new Reader(readerName, this);
            i++;
        }
        return mReaders;
    }

    /**
     * Releases all Secure Elements resources allocated by this SEService. It is
     * recommended to call this method in the termination method of the calling
     * application (or part of this application) which is bound to this
     * SEService.
     */
    public void shutdown() {
        synchronized (mConnection) {
            if (mSmartcardService != null) {
                synchronized (mReaders) {

                    for (Reader reader : mReaders) {
                        try {
                            reader.closeSessions();
                        } catch (Exception ignore) {
                        }
                    }
                }
            }
            try {
                mContext.unbindService(mConnection);
            } catch (IllegalArgumentException e) {
                // Do nothing and fail silently since an error here indicates
                // that binding never succeeded in the first place.
            }
        }
    }

    // ******************************************************************
    // package private methods
    // ******************************************************************

    /**
     * Returns <code>true</code> if a smartcard is present in the specified
     * reader.
     * 
     * @param reader the friendly name of the reader to be checked for card
     *            presence.
     * @return <code>true</code> if a smartcard is present <code>false</code> if
     *         a smartcard is absent
     * @throws NullPointerException if the reader name is <code>null</code>.
     * @throws IllegalStateException if this service is not connected.
     */
    boolean isSecureElementPresent(Reader reader) {
        if (reader == null) {
            throw new NullPointerException("reader must not be null");
        }
        if (mSmartcardService == null) {
            throw new IllegalStateException("service not connected to system");
        }

        SmartcardError error = new SmartcardError();
        boolean isPresent;
        try {
            isPresent = mSmartcardService.isCardPresent(reader.getName(), error);
            checkForException(error);
        } catch (Exception e) {
            return false;
        }

        return isPresent;
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
     * @throws IllegalStateException if this service is not connected.
     * @throws IllegalArgumentException if the reader name is unknown or the AID
     *             length is illegal.
     * @throws SecurityException if the access control conditions are not
     *             fullfilled.
     * @throws IOException if applet selection failed.
     */
    Channel openBasicChannel(Session session, byte[] aid) throws IOException {
        if (session == null) {
            throw new NullPointerException("session must not be null");
        }
        if (session.getReader() == null) {
            throw new NullPointerException("reader must not be null");
        }

        if (mSmartcardService == null) {
            throw new IllegalStateException("service not connected to system");
        }
        if (session.isClosed()) {
            throw new IllegalStateException("session is closed");
        }

        SmartcardError error = new SmartcardError();
        long hChannel;
        try {
            hChannel = mSmartcardService.openBasicChannelAid(session.getReader().getName(), aid,
                    mCallback, error);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        if (allChannelsInUse(error)) {
            return null;
        }
        checkForException(error);

        Channel basicChannel = new Channel(session, hChannel, false);
        return basicChannel;
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
     * @throws IllegalStateException if this service is not connected.
     * @throws IllegalArgumentException if the reader name is unknown or the AID
     *             length is illegal.
     * @throws SecurityException if the access control conditions are not
     *             fullfilled.
     * @throws IOException if opening the logical channel or applet selection
     *             failed.
     */
    Channel openLogicalChannel(Session session, byte[] aid) throws IOException {
        if (session == null) {
            throw new NullPointerException("session must not be null");
        }
        if (session.getReader() == null) {
            throw new NullPointerException("reader must not be null");
        }

        if (mSmartcardService == null) {
            throw new IllegalStateException("service not connected to system");
        }
        if (session.isClosed()) {
            throw new IllegalStateException("session is closed");
        }

        SmartcardError error = new SmartcardError();
        long hChannel;
        try {
            hChannel = mSmartcardService.openLogicalChannel(session.getReader().getName(), aid,
                    mCallback, error);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        if (allChannelsInUse(error)) {
            return null;
        }
        checkForException(error);

        Channel logicalChannel = new Channel(session, hChannel, true);
        return logicalChannel;
    }

    /**
     * Closes the specified channel.
     * 
     * @param hChannel the channel handle obtained by an open channel command.
     * @throws IllegalStateException if this service is not connected.
     */
    void closeChannel(Channel channel) throws IOException {
        if (mSmartcardService == null) {
            throw new IllegalStateException("service not connected to system");
        }
        if (channel == null) {
            throw new NullPointerException("channel must not be null");
        }
        if (channel.isClosed()) {
            return;
        }

        SmartcardError error = new SmartcardError();
        try {
            mSmartcardService.closeChannel(channel.getHandle(), error);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        checkForException(error);
    }

    /**
     * Transmits the specified command APDU and returns the response APDU. <br>
     * The commands MANAGE CHANNEL and SELECT are not allowed. Use the
     * openChannel methods therefore.
     * 
     * @param hChannel the channel handle obtained by an open channel command.
     * @param command the command apdu which should be sent
     * @throws IllegalStateException if this service is not connected.
     * @throws IOException if a communication error occurs
     * @throws NullPointerException if command is null
     * @throws IllegalArgumentException if command is to short (must have a
     *             length of at least 4 bytes)
     * @throws SecurityException if the access control conditions are not
     *             fullfilled.
     * @throws IllegalStateException if this service is not connected.
     */
    byte[] transmit(Channel channel, byte[] command) throws IOException {
        if (command == null) {
            throw new NullPointerException("command must not be null");
        }
        if (command.length < 4) {
            throw new IllegalArgumentException("command must have at least 4 bytes");
        }
        if (mSmartcardService == null) {
            throw new IllegalStateException("service not connected to system");
        }
        if (channel == null) {
            throw new NullPointerException("channel must not be null");
        }
        if (channel.isClosed()) {
            throw new IllegalStateException("channel is closed");
        }

        SmartcardError error = new SmartcardError();
        byte[] response;
        try {
            response = mSmartcardService.transmit(channel.getHandle(), command, error);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        checkForException(error);

        return response;
    }

    byte[] getAtr(Reader reader) {

        if (reader == null) {
            throw new NullPointerException("reader must not be null");
        }
        if (mSmartcardService == null) {
            throw new IllegalStateException("service not connected to system");
        }

        SmartcardError error = new SmartcardError();
        byte[] atr;
        try {
            atr = mSmartcardService.getAtr(reader.getName(), error);
            checkForException(error);
        } catch (Exception e) {
            return null;
        }
        return atr;
    }

    private boolean allChannelsInUse(SmartcardError error) {
        Exception exp = error.createException();
        if (exp != null) {
            if (exp instanceof MissingResourceException) {
                return true;
            }
            String msg = exp.getMessage();
            if (msg != null) {
                if (msg.contains("channel in use")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkForException(SmartcardError error) {
        try {
            error.throwException();
        } catch (CardException exp) {
            throw new IllegalStateException(exp.getMessage());
        } catch (AccessControlException exp) {
            throw new SecurityException(exp.getMessage());
        }
    }
}
