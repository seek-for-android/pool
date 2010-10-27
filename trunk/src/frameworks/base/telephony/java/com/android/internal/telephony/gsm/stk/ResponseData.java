/*
 * Copyright (C) 2006-2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.internal.telephony.gsm.stk;

import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

abstract class ResponseData {
    /**
     * Format the data appropriate for TERMINAL RESPONSE and write it into
     * the ByteArrayOutputStream object.
     */
    public abstract void format(ByteArrayOutputStream buf);
}

class SelectItemResponseData extends ResponseData {
    // members
    private int id;

    public SelectItemResponseData(int id) {
        super();
        this.id = id;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        // Item identifier object
        int tag = 0x80 | ComprehensionTlvTag.ITEM_ID.value();
        buf.write(tag); // tag
        buf.write(1); // length
        buf.write(id); // identifier of item chosen
    }
}

class GetInkeyInputResponseData extends ResponseData {
    // members
    private boolean mIsUcs2;
    private boolean mIsPacked;
    private boolean mIsYesNo;
    private boolean mYesNoResponse;
    public String mInData;

    // GetInKey Yes/No response characters constants.
    protected static final byte GET_INKEY_YES = 0x01;
    protected static final byte GET_INKEY_NO = 0x00;

    public GetInkeyInputResponseData(String inData, boolean ucs2, boolean packed) {
        super();
        this.mIsUcs2 = ucs2;
        this.mIsPacked = packed;
        this.mInData = inData;
        this.mIsYesNo = false;
    }

    public GetInkeyInputResponseData(boolean yesNoResponse) {
        super();
        this.mIsUcs2 = false;
        this.mIsPacked = false;
        this.mInData = "";
        this.mIsYesNo = true;
        this.mYesNoResponse = yesNoResponse;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        if (buf == null) {
            return;
        }

        // Text string object
        int tag = 0x80 | ComprehensionTlvTag.TEXT_STRING.value();
        buf.write(tag); // tag

        byte[] data;

        if (mIsYesNo) {
            data = new byte[1];
            data[0] = mYesNoResponse ? GET_INKEY_YES : GET_INKEY_NO;
        } else if (mInData != null && mInData.length() > 0) {
            try {
                if (mIsUcs2) {
                    data = mInData.getBytes("UTF-16");
                } else if (mIsPacked) {
                    int size = mInData.length();

                    byte[] tempData = GsmAlphabet
                            .stringToGsm7BitPacked(mInData);
                    data = new byte[size];
                    // Since stringToGsm7BitPacked() set byte 0 in the
                    // returned byte array to the count of septets used...
                    // copy to a new array without byte 0.
                    System.arraycopy(tempData, 1, data, 0, size);
                } else {
                    data = GsmAlphabet.stringToGsm8BitPacked(mInData);
                }
            } catch (UnsupportedEncodingException e) {
                data = new byte[0];
            } catch (EncodeException e) {
                data = new byte[0];
            }
        } else {
            data = new byte[0];
        }

        // length - one more for data coding scheme.
        buf.write(data.length + 1);

        // data coding scheme
        if (mIsUcs2) {
            buf.write(0x08); // UCS2
        } else if (mIsPacked) {
            buf.write(0x00); // 7 bit packed
        } else {
            buf.write(0x04); // 8 bit unpacked
        }

        for (byte b : data) {
            buf.write(b);
        }
    }
}

class OpenChannelResponseData extends ResponseData {
    // members
    private int bufSize;
    private int channelStatus;
    private int bearerType;

    public OpenChannelResponseData(int bufSize, int channelStatus, int bearerType) {
        super();
        this.bufSize = bufSize;
        this.channelStatus = channelStatus;
        this.bearerType = bearerType;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        // Item identifier object
        int tag = ComprehensionTlvTag.BUFFER_SIZE.value();
        buf.write(tag);
        buf.write(2);
        buf.write((bufSize >> 8) & 0xff);
        buf.write(bufSize & 0xff);
        tag = ComprehensionTlvTag.CHANNEL_STATUS.value();
        buf.write(tag);
        buf.write(2);
        buf.write((channelStatus >> 8) & 0xff);
        buf.write(channelStatus & 0xff);
        tag = ComprehensionTlvTag.BEARER_DESC.value();
        buf.write(tag);
        buf.write(1);
        buf.write(bearerType & 0xff);
    }
}

class ReceiveDataResponseData extends ResponseData {
    // members
    private byte[] mData = null;
    private int mLength = 0;

    public ReceiveDataResponseData(byte[] data, int len) {
        super();
        mData = data;
        mLength = len;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        int tag = 0x80 | ComprehensionTlvTag.CHANNEL_DATA.value();
        buf.write(tag);
        if (mData.length > 0x7f)
            buf.write(0x81);
        buf.write(mData.length & 0xff);

        for (byte b : mData) {
            buf.write(b);
        }

        tag = 0x80 | ComprehensionTlvTag.CHANNEL_DATA_LENGTH.value();
        buf.write(tag);
        buf.write(1);
        buf.write(mLength & 0xff);
    }
}

class SendDataResponseData extends ResponseData {
    // members
    private int mLength = 0;

    public SendDataResponseData(int len) {
        super();
        mLength = len;
    }

    @Override
    public void format(ByteArrayOutputStream buf) {
        int tag = ComprehensionTlvTag.CHANNEL_DATA_LENGTH.value();
        buf.write(tag);
        buf.write(1);
        buf.write(mLength & 0xff);
    }
}
