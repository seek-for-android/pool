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

package android.smartcard.security;

import android.smartcard.Util;

public class AccessCondition {
    protected byte[] mApdu;

    protected byte[] mMask;

    public static final int LENGTH = 8;

    public AccessCondition(byte[] apdu, byte[] mask) {
        if (apdu.length != 4) {
            throw new IllegalArgumentException("apdu length must be 4 bytes");
        }
        if (mask.length != 4) {
            throw new IllegalArgumentException("mask length must be 4 bytes");
        }

        mApdu = apdu;
        mMask = mask;
    }

    public AccessCondition(byte[] apduAndMask) {
        if (apduAndMask.length != 8) {
            throw new IllegalArgumentException("mask length must be 8 bytes");
        }

        mApdu = Util.getMid(apduAndMask, 0, 4);
        mMask = Util.getMid(apduAndMask, 4, 4);
    }

    public byte[] getApdu() {
        return mApdu;
    }

    public void setApdu(byte[] apdu) {
        if (apdu.length != 4) {
            throw new IllegalArgumentException("apdu length must be 4 bytes");
        }
        mApdu = apdu;
    }

    public byte[] getMask() {
        return mMask;
    }

    public void setMask(byte[] mask) {
        if (mask.length != 4) {
            throw new IllegalArgumentException("mask length must be 4 bytes");
        }
        mMask = mask;
    }

    public byte[] toBytes() {
        return Util.mergeBytes(mApdu, mMask);
    }

    @Override
    public String toString() {
        return "AccessCondition [apdu=" + Util.bytesToString(mApdu) + ", mask="
                + Util.bytesToString(mMask) + "]";
    }

    public int getLength() {
        return 8;
    }

}
