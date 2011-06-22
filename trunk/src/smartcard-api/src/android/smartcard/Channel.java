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

import android.os.IBinder;
import android.os.RemoteException; 
import android.smartcard.security.ChannelAccess; 
import android.util.Log;

/**
 * Smartcard service base class for channel resources.
 */
class Channel implements IChannel, IBinder.DeathRecipient {

    protected final int mChannelNumber;

    protected long mHandle;

    protected Terminal mTerminal;

    protected final IBinder mBinder;

    
    protected ChannelAccess mChannelAccess = null;

    

    protected ISmartcardServiceCallback mCallback;

    protected boolean mHasSelectedAid = false;

    Channel(Terminal terminal, int channelNumber, ISmartcardServiceCallback callback) {
        this.mChannelNumber = channelNumber;
        this.mTerminal = terminal;
        this.mCallback = callback;
        this.mBinder = callback.asBinder();
        try {
            mBinder.linkToDeath(this, 0);
        } catch (RemoteException e) {
            Log.e(SmartcardService.SMARTCARD_SERVICE_TAG, "Failed to register client callback");
        }
    }

    public void binderDied() {
        // Close this channel if the client died.
        try {
            Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, Thread.currentThread().getName()
                    + " Client " + mBinder.toString() + " died");
            close();
        } catch (Exception ignore) {
        }
    }

    public void close() throws CardException {
        try {
            getTerminal().closeChannel(this);
        } finally {
            mBinder.unlinkToDeath(this, 0);
        }
    }

    public int getChannelNumber() {
        return mChannelNumber;
    }

    /**
     * Returns if this channel is a basic channel
     * 
     * @return true if this channel is a basic channel
     */
    public boolean isBasicChannel() {
        return (mChannelNumber == 0) ? true : false;
    }

    public ISmartcardServiceCallback getCallback() {
        return mCallback;
    }

    /**
     * Returns the handle assigned to this channel.
     * 
     * @return the handle assigned to this channel.
     */
    long getHandle() {
        return mHandle;
    }

    /**
     * Returns the associated terminal.
     * 
     * @return the associated terminal.
     */
    public Terminal getTerminal() {
        return mTerminal;
    }

    /**
     * Assigns the channel handle.
     * 
     * @param handle the channel handle to be assigned.
     */
    void setHandle(long handle) {
        this.mHandle = handle;
    }

    public byte[] transmit(byte[] command) throws CardException {
        if (command.length < 4) {
            throw new IllegalArgumentException("command must not be smaller than 4 bytes");
        }
        if (((command[0] & (byte) 0x80) == 0) && ((byte) (command[0] & (byte) 0x60) != (byte) 0x20)) {
            // ISO command
            if (command[1] == (byte) 0x70) {
                throw new IllegalArgumentException("MANAGE CHANNEL command not allowed");
            }
            if ((command[1] == (byte) 0xA4) && (command[2] == (byte) 0x04)) {
                throw new IllegalArgumentException("SELECT command not allowed");
            }
            int cla = command[0] & (byte) 0x7F;
            if (mChannelNumber < 4) {
                cla = (cla & 0x1C) | mChannelNumber;
            } else {
                cla = (cla & 0x30) | 0x40 | (mChannelNumber - 4);
            }
            command[0] = (byte) cla;
        } else {
            command[0] |= mChannelNumber;
        }
        byte[] rsp = getTerminal().transmit(command, 2, 0, 0, null);
        return rsp;
    }

    
    public void setChannelAccess(ChannelAccess channelAccess) {
        this.mChannelAccess = channelAccess;
    }

    public ChannelAccess getChannelAccess() {
        return mChannelAccess;
    }

    

    /**
     * @return
     */
    public boolean hasSelectedAid() {
        return mHasSelectedAid;
    }

    /**
     * @return
     */
    public void hasSelectedAid(boolean has) {
        mHasSelectedAid = has;
    }

}
