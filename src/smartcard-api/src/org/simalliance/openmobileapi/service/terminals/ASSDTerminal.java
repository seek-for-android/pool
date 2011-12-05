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

package org.simalliance.openmobileapi.service.terminals;

import android.content.Context;


import java.util.MissingResourceException;
import java.util.NoSuchElementException;

import org.simalliance.openmobileapi.service.CardException;
import org.simalliance.openmobileapi.service.Terminal;

final class ASSDTerminal extends Terminal {

    private static boolean JNILoaded = false;

    public ASSDTerminal(Context context) {
        super("SD: Secure SD Card", context);
    }

    @Override
    protected void internalConnect() throws CardException {
        if (JNILoaded == false) {
            throw new CardException("JNI failed");
        }

        try {
            if (Open() == false) {
                throw new CardException("open SE failed");
            }
        } catch (Exception e) {
            throw new CardException("open SE failed");
        }
        mDefaultApplicationSelectedOnBasicChannel = true;
        mIsConnected = true;
    }

    @Override
    protected void internalDisconnect() throws CardException {
        if (JNILoaded == false) {
            throw new CardException("JNI failed");
        }

        try {
            Close();
        } catch (Exception e) {
        } finally {
            mIsConnected = false;
        }
    }

    public boolean isCardPresent() throws CardException {
        if (JNILoaded == false) {
            return false;
        }

        try {
            return IsPresent();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected byte[] internalTransmit(byte[] command) throws CardException {
        if (JNILoaded == false) {
            throw new CardException("JNI failed");
        }

        try {
            byte[] response = Transmit(command);
            if (response == null) {
                throw new CardException("transmit failed");
            }
            return response;
        } catch (Exception e) {
            throw new CardException("transmit failed");
        }
    }

    @Override
    protected void internalCloseLogicalChannel(int channelNumber) throws CardException {
        if (channelNumber > 0) {
            byte cla = (byte) channelNumber;
            if (channelNumber > 3) {
                cla |= 0x40;
            }
            byte[] manageChannelClose = new byte[] {
                    cla, 0x70, (byte) 0x80, (byte) channelNumber
            };
            transmit(manageChannelClose, 2, 0x9000, 0xFFFF, "MANAGE CHANNEL");
        }
    }

    @Override
    protected int internalOpenLogicalChannel() throws Exception {
    	
    	mSelectResponse = null;
        byte[] manageChannelCommand = new byte[] {
                0x00, 0x70, 0x00, 0x00, 0x01
        };
        byte[] rsp = transmit(manageChannelCommand, 3, 0x9000, 0xFFFF, "MANAGE CHANNEL");
        if (rsp.length != 3) {
            throw new MissingResourceException("unsupported MANAGE CHANNEL response data", "", "");
        }
        int channelNumber = rsp[0] & 0xFF;
        if (channelNumber == 0 || channelNumber > 19) {
            throw new MissingResourceException("invalid logical channel number returned", "", "");
        }

        return channelNumber;
    }

    @Override
    protected int internalOpenLogicalChannel(byte[] aid) throws Exception {
        int channelNumber = internalOpenLogicalChannel();

        if (aid == null) {
            throw new NullPointerException("aid must not be null");
        }
        mSelectResponse = null;
        byte[] selectCommand = new byte[aid.length + 6];
        selectCommand[0] = (byte) channelNumber;
        if (channelNumber > 3) {
            selectCommand[0] |= 0x40;
        }
        selectCommand[1] = (byte) 0xA4;
        selectCommand[2] = 0x04;
        selectCommand[4] = (byte) aid.length;
        System.arraycopy(aid, 0, selectCommand, 5, aid.length);
        try {
        	mSelectResponse = transmit(selectCommand, 2, 0x9000, 0xFFFF, "SELECT");
        } catch (CardException e) {
            internalCloseLogicalChannel(channelNumber);
            throw new NoSuchElementException(e.getMessage());
        }

        return channelNumber;
    }

    static {
        try {
            Runtime.getRuntime().loadLibrary("assd");
            JNILoaded = true;
        } catch (Throwable t) {
        }
    }

    private native void Close() throws Exception;

    private native boolean Open() throws Exception;

    private native boolean IsPresent() throws Exception;

    private native byte[] Transmit(byte[] command) throws Exception;
}
