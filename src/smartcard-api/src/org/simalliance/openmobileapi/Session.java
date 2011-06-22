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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Instances of this class represent a connection session to one of the secure
 * elements available on the device. These objects can be used to get a
 * communication channel with an application in the secure element. This channel
 * can be the basic channel or a logical channel.
 */
public class Session {

    private Reader mReader;

    private String mName;

    private boolean mIsClosed;

    private byte[] mAtr;

    /** List of open channels in use of by this client. */
    private final Set<Channel> mChannels = new HashSet<Channel>();

    Session(String name, Reader reader) {
        mAtr = reader.getSEService().getAtr(reader);
        mReader = reader;
        mName = name;
        mIsClosed = false;
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
     * elements (like the UICC) might always keep the basic channel locked (i.e.
     * return null to applications), to prevent access to the basic channel,
     * while some other might return a channel object implementing some kind of
     * filtering on the commands, restricting the set of accepted command to a
     * smaller set. There is no way for the application to retrieve the select
     * response. Recommendation for an implementation: Any select response has
     * to be fetched but should be discarded internally.
     * <p>
     * 
     * @throws IOException - if something goes wrong with the communication to
     *             the reader or the secure element (e.g. if the AID is not
     *             available).
     * @throws IllegalStateException - if the secure element session is used
     *             after being closed.
     * @throws IllegalArgumentException - if the aid's length is not within 5 to
     *             16 (inclusive).
     * @throws SecurityException - if the calling application cannot be granted
     *             access to this AID or the default application on this
     *             session.
     * @param aid of the applet which shall be selected on the secure element or
     *            null for using the default selected applet.
     * @return an instance of Channel if available or null.
     */
    public Channel openBasicChannel(byte[] aid) throws IOException {

        synchronized (mChannels) {

            Channel basicChannel = mReader.getSEService().openBasicChannel(this, aid);
            mChannels.add(basicChannel);
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
     * @param aid the AID of the application to be selected on this channel, as
     *            a byte array.
     * @throws IOException if something goes wrong with the communication to the
     *             reader or the secure element. (e.g. if the AID is not
     *             available)
     * @throws IllegalStateException if the secure element is used after being
     *             closed.
     * @throws IllegalArgumentException if the aid's length is not within 5 to
     *             16 (inclusive).
     * @throws SecurityException if the calling application cannot be granted
     *             access to this AID or the default application on this
     *             session.
     * @param aid the AID of the application to be selected on this channel, as
     *            a byte array.
     * @return an instance of Channel. Null if the secure element is unable to
     *         provide a new logical channel.
     */
    public Channel openLogicalChannel(byte[] aid) throws IOException {

        synchronized (mChannels) {

            Channel logicalChannel = mReader.getSEService().openLogicalChannel(this, aid);
            mChannels.add(logicalChannel);
            return logicalChannel;

        }
    }

    /**
     * Close the connection with the secure element. This will close any
     * channels opened by this application with this secure element.
     */
    public void close() {

        mReader.closeSession(this);
    }

    /**
     * Tells if this session is closed.
     * 
     * @return <code>true</code> if the session is closed.
     */
    public boolean isClosed() {
        return mIsClosed;
    }

    /**
     * Get the Answer to Reset of this secure element. <br>
     * The returned byte array can be null, if the ATR for this Secure Element
     * is not available.
     * 
     * @return the ATR as a byte array.
     */
    public byte[] getATR() {
        return mAtr;
    }

    /**
     * Returns the list of available secure element readers. There must be no
     * duplicated objects in the returned list.
     * 
     * @return the readers list, as an array of Readers. If there are no readers
     *         the returned array is of length 0.
     */
    public Reader getReader() {
        return mReader;
    }

    /**
     * Close any channel opened on this session.
     */
    public void closeChannels() {

        synchronized (mChannels) {
            for (Channel channel : mChannels) {
                if (channel != null && !channel.isClosed()) {
                    try {
                        mReader.getSEService().closeChannel(channel);
                    } catch (Exception ignore) {
                    }
                    channel.setClosed();
                }
            }
            mChannels.clear();
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
     * @param hChannel the channel handle obtained by an open channel command.
     */
    void closeChannel(Channel channel) {
        if (channel == null) {
            throw new NullPointerException("channel is null");
        }

        synchronized (mChannels) {

            if (!channel.isClosed()) {
                try {
                    mReader.getSEService().closeChannel(channel);
                } catch (Exception ignore) {
                }

                channel.setClosed();
            }
            mChannels.remove(channel);
        }
    }

    void setClosed() {
        mIsClosed = true;
    }

}
