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

package org.simalliance.openmobileapi.service.security;

import java.security.AccessControlException;

public class ResponseApdu {

    protected int mSw1 = 0x00;

    protected int mSw2 = 0x00;

    protected byte[] mData = new byte[0];

    public ResponseApdu(byte[] respApdu) {
        if (respApdu.length < 2) {
            return;
        }
        if (respApdu.length > 2) {
            mData = new byte[respApdu.length - 2];
            System.arraycopy(respApdu, 0, mData, 0, respApdu.length - 2);
        }
        mSw1 = 0x00FF & respApdu[respApdu.length - 2];
        mSw2 = 0x00FF & respApdu[respApdu.length - 1];

    }

    public int getSW1() {
        return mSw1;
    }

    public int getSW2() {
        return mSw2;
    }

    public int getSW1SW2() {
        return (mSw1 << 8) | mSw2;
    }

    public byte[] getData() {
        return mData;
    }

    public void checkLengthAndStatus(int length, int sw1sw2, String message)
            throws AccessControlException {
        if (getSW1SW2() != sw1sw2 || mData.length != length) {
            throw new AccessControlException("ResponseApdu is wrong at " + message);
        }
    }

    public void checkLengthAndStatus(int length, int[] sw1sw2List, String message)
            throws AccessControlException {
        if (mData.length != length) {
            throw new AccessControlException("ResponseApdu is wrong at " + message);
        }
        for (int sw1sw2 : sw1sw2List) {
            if (getSW1SW2() == sw1sw2) {
                return; // sw1sw2 matches => return
            }
        }
        throw new AccessControlException("ResponseApdu is wrong at " + message);
    }

    public void checkStatus(int[] sw1sw2List, String message) throws AccessControlException {
        for (int sw1sw2 : sw1sw2List) {
            if (getSW1SW2() == sw1sw2) {
                return; // sw1sw2 matches => return
            }
        }
        throw new AccessControlException("ResponseApdu is wrong at " + message);
    }

    public void checkStatus(int sw1sw2, String message) throws AccessControlException {
        if (getSW1SW2() != sw1sw2) {
            throw new AccessControlException("ResponseApdu is wrong at " + message);
        }
    }

    public boolean isStatus(int sw1sw2) {
        if (getSW1SW2() == sw1sw2) {
            return true;
        }
        else {
            return false;
        }
    }

}
