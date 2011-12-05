/*
 * Copyright (C) 2009 The Android Open Source Project
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

package org.simalliance.openmobileapi.cts;

import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

import mockcard.MockCard;



import android.content.Context;
import android.util.Log;

public class PluginTerminal {

	private static final boolean LOG_VERBOSE = false;
    static final String TAG = "CTS";

	private static final byte[] command_MANAGE_CHANNEL_open  = {(byte)0x00, (byte)0x70, (byte)0x00, (byte)0x00, (byte)0x01};
	private static final byte[] command_MANAGE_CHANNEL_close = {(byte)0x00, (byte)0x70, (byte)0x80, (byte)0x00};
	private static final byte[] commandHeader_SELECT         = {(byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00};

	byte[] mSelectResponse;
	
    MockCard mockCard;
    
	public PluginTerminal(Context context){
		// this plug-in terminal will have
		// a mock card that has an APDUTester applet installed on it:
	    mockCard = new MockCard();
	    mockCard.installApplet(OMAPITestCase.AID_APDU_TESTER, new APDUTester());
	} // constructor
	
	public byte[] getAtr()            { return mockCard.getATR(); }
	public String getName()           { return "CTSMock"; }
	public boolean isCardPresent()    { return true; }
	public void internalConnect()     { }
	public void internalDisconnect()  { }
    public byte[] getSelectResponse() { return mSelectResponse; }

	public byte[] internalTransmit(byte[] command){ 
		return mockCard.process(command); 
	} // internalTransmit

	public int internalOpenLogicalChannel(){ 
		byte[] response;
		if (LOG_VERBOSE) Log.v(TAG, "internalOpenLogicalChannel()");
		response = mockCard.process(command_MANAGE_CHANNEL_open);
		int sw = ((0xff & response[response.length-2])<<8) | 
		          (0xff & response[response.length-1]);
		if (sw==0x6A81) throw new MissingResourceException("MANAGE_CHANNEL: open failed; no free channel available", "", "");
		if (sw!=0x9000 || response.length!=3) throw new MissingResourceException("MANAGE_CHANNEL: open failed", "", "");
		return 0xff & response[0];
	} // internalOpenLogicalChannel
	
	public int internalOpenLogicalChannel(byte[] aid){ 
		if (LOG_VERBOSE) Log.v(TAG, "internalOpenLogicalChannel(aid)");
		int iChannel = internalOpenLogicalChannel();
		byte[] commandSelect;
		commandSelect = Arrays.copyOfRange(commandHeader_SELECT, 0, 5+aid.length);
		commandSelect[0] = (byte)((iChannel<4)? iChannel: 0x40 | (iChannel-4));
		commandSelect[4] = (byte)aid.length;
		System.arraycopy(aid, 0, commandSelect, 5, aid.length);
		mSelectResponse = mockCard.process(commandSelect);
		if (LOG_VERBOSE) Log.v(TAG, "mSelectResponse.length="+mSelectResponse.length);
		int sw = ((0xff & mSelectResponse[mSelectResponse.length-2])<<8) | 
                  (0xff & mSelectResponse[mSelectResponse.length-1]);
        if (sw!=0x9000) throw new NoSuchElementException("SELECT: applet not found");
		return iChannel;
	} // internalOpenLogicalChannel
	
	public void internalCloseLogicalChannel(int iChannel){ 
		byte[] response;
		if (LOG_VERBOSE) Log.v(TAG, "internalCloseLogicalChannel()");
		command_MANAGE_CHANNEL_close[0] = (byte)((iChannel<4)? iChannel: 0x40 | (iChannel-4));
		command_MANAGE_CHANNEL_close[3] = (byte)iChannel;
		response = mockCard.process(command_MANAGE_CHANNEL_close);
		int sw = ((0xff & response[response.length-2])<<8) | 
                  (0xff & response[response.length-1]);
		if (sw!=0x9000) throw new NoSuchElementException("MANAGE_CHANNEL: close failed");
	} // internalCloseLogicalChannel

} // class
