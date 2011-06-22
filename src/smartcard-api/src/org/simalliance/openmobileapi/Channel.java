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

/**
 * Instances of this class represent an ISO7816-4 channel opened to a secure
 * element. It can be either a logical channel or the default channel. They can
 * be used to send APDUs to the secure element. Channels are opened by calling
 * the Session.openBasicChannel(byte[]) or Session.openLogicalChannel(byte[])
 * methods.
 */
public class Channel {

    private Session mSession;

    private long mHChannel;

    private boolean mIsLogicalChannel;

    private boolean mIsClosed;

    Channel(Session session, long hChannel, boolean isLogicalChannel) {
        mSession = session;
        mHChannel = hChannel;
        mIsLogicalChannel = isLogicalChannel;
        mIsClosed = false;
    }

    /**
     * Close this channel to the secure element. If the channel is used after
     * being closed, an IllegalStateError will be raised. If this channel is the
     * basic channel, it can't be closed this method will be ignored.
     */
    public void close() {

        if (isClosed()) {
            if (!isBasicChannel()) {
                throw new IllegalStateException("channel is already closed");
            }
        }
        mSession.closeChannel(this);
    }

    /**
     * Tells if this channel is closed.
     * 
     * @return <code>true</code> if the channel is closed.
     */
    public boolean isClosed() {
        return mIsClosed;
    }

    /**
     * Returns a boolean telling if this channel is the basic channel..
     * 
     * @return <code>true</code> if this channel is a basic channel. false if
     *         this channel is a logical channel.
     */
    public boolean isBasicChannel() {
        return !mIsLogicalChannel;
    }

    /**
     * Transmit an APDU command (as per ISO7816-4) to the secure element and
     * wait for the response. The underlying layers might generate as much TPDUs
     * as necessary to transport this APDU. The transport part is invisible from
     * the application. <br>
     * The system ensures the synchronization between all the concurrent calls
     * to this method, and that only one APDU will be sent at a time,
     * irrespective of the number of TPDUs that might be required to transport
     * it to the SE. <br>
     * The channel information in the class byte in the APDU will be completely
     * ignored. The underlying system will add any required information to
     * ensure the APDU is transported on this channel. There are restrictions on
     * the set of commands that can be sent: <br>
     * MANAGE_CHANNEL commands are not allowed.
     * 
     * <pre>
     * <lu> 
     * <li>MANAGE_CHANNEL commands are not allowed.</li> 
     * <li>SELECT by DF Name (p1=04) are not allowed.</li>
     * <li>CLA bytes with channel numbers are de-masked.</li> 
     * </lu>
     * </pre>
     * 
     * @param command the APDU command to be transmitted, as a byte array.
     * @return the response received, as a byte array
     * @throws IOException if something goes wrong with the communication to the
     *             reader or to the secure element.
     * @throws IllegalStateException if the channel is used after being closed.
     * @throws IllegalArgumentException if the command is not correctly
     *             formatted or is not complaint to the restrictions mentioned
     *             above
     * @throws SecurityException if the command is filtered by the security
     *             policy
     */
    public byte[] transmit(byte[] command) throws IOException {
        if (isClosed()) {
            throw new IllegalStateException("channel is closed");
        }
        return mSession.getReader().getSEService().transmit(this, command);
    }

    /**
     * Get the session that has opened this channel.
     * 
     * @return the session object this channel is bound to.
     */
    public Session getSession() {
        return mSession;
    }

    // ******************************************************************
    // package private methods
    // ******************************************************************

    long getHandle() {
        return mHChannel;
    }

    void setClosed() {
        mIsClosed = true;
    }
}
