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

package android.smartcard.cts;

import java.io.IOException;
import java.util.Arrays;
import junit.framework.TestCase;

import android.smartcard.CardException;
import android.smartcard.ICardChannel;
import android.smartcard.cts.Util;
import android.util.Log;

public class TestMultiChannel extends SmartcardTestCase {

	private static final boolean LOG_VERBOSE = true;
	private static final String TAG = "CTS";

	/**
	 * number of channels this test will exercise
	 */
	public static final int N_CHANNELS = 10;

	public TestMultiChannel() {
	} // constructor

	@Override
	public void setUp() throws Exception {
		super.setUp();
	} // setUp
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	} // tearDown
	
	/**
	 * tests opening and closing on N_CHANNELS channels.
	 * @throws IOException
	 */
	public void testOpenClose() throws CardException {
		if (LOG_VERBOSE) Log.v(TAG, "testOpenClose()");

		ICardChannel[] channels = new ICardChannel[N_CHANNELS];
		for (int i=0; i<channels.length; i++) {
			if (LOG_VERBOSE) Log.v(TAG, "  open channel #"+i+((i==0)? " (basic channel)": ""));
			
			if (i==0) 
				channels[i] = mSmartcardClient.openBasicChannel(mReader);
			else
				channels[i] = mSmartcardClient.openLogicalChannel(mReader, AID_APDU_TESTER);
			TestCase.assertNotNull(channels[i]);
		}
		
		// close all open non-basic channels:
		for (int i=1; i<channels.length; i++) 
			if (channels[i] != null) {
				channels[i].close();
				channels[i] = null;
				if (LOG_VERBOSE) Log.v(TAG, "  closed channel #"+i);
			}
	} // testOpenClose

	/**
	 * tests transmitting a case-4 command (sending 3 data bytes, receiving 5 data bytes) 
	 * on N_CHANNELS channels.
	 * @throws IOException
	 */
	public void testTransmit() throws CardException {
		if (LOG_VERBOSE) Log.v(TAG, "testOpenClose()");
		
		ICardChannel[] channels = new ICardChannel[N_CHANNELS];
		
		// (1) open channels:
		for (int i=0; i<channels.length; i++) {
			if (LOG_VERBOSE) Log.v(TAG, "  open channel #"+i+((i==0)? " (basic channel)": ""));
			
			if (i==0) 
				channels[i] = mSmartcardClient.openBasicChannel(mReader, AID_APDU_TESTER);
			else
				channels[i] = mSmartcardClient.openLogicalChannel(mReader, AID_APDU_TESTER);
			TestCase.assertNotNull(channels[i]);
		}

		// (2) transmit on all open channels:
		for (int i=0; i<channels.length; i++) {
			byte[] command = new byte[]{(byte)0x00, (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x05 };
			byte[] expectedResponse = Util.expectedResponseOfTestApplet(command);
			if (LOG_VERBOSE) Log.v(TAG, "  transmit on channel #"+i+":");
			if (LOG_VERBOSE) Log.v(TAG, "  --> "+ Util.bytesToHexString(command));
			byte[] response = channels[i].transmit(command);
			if (LOG_VERBOSE) Log.v(TAG, "  <-- " + Util.bytesToHexString(response));
			TestCase.assertTrue(Arrays.equals(response, expectedResponse));
		}
		
		// (3) close all open non-basic channels:
		for (int i=1; i<channels.length; i++) 
			if (channels[i] != null) {
				channels[i].close();
				channels[i] = null;
				if (LOG_VERBOSE) Log.v(TAG, "  closed channel #"+i);
			}

	} // testTransmit

} // class
