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

import junit.framework.TestCase;
import android.smartcard.CardException;
import android.smartcard.ICardChannel;
import android.util.Log;

public class TestBasic extends SmartcardTestCase {

	private static final boolean LOG_VERBOSE = true;
	private static final String TAG = "CTS";

	public TestBasic() {
	} // constructor

	/**
	 * tests opening a session and reading the ATR
	 * 
	 * @throws IOException
	 */
	public void testATR() throws CardException {
		if (LOG_VERBOSE) Log.v(TAG, "testATR()");
		Log.v(TAG, "testATR() ***not implemented***");
	} // testATR

	/**
	 * tests opening a basic channel
	 * 
	 * @throws IOException
	 * @throws CardException 
	 */
	public void testOpen() throws CardException {
		if (LOG_VERBOSE) Log.v(TAG, "testBasicChannelOpening()");

		ICardChannel basicChannel = mSmartcardClient.openBasicChannel(mReader);
		TestCase.assertNotNull(basicChannel);
		basicChannel.close();
	} // testBasicChannelOpening
	
	/**
	 * tests transmitting on the basic channel
	 * 
	 * @throws IOException
	 */
	public void testTransmit() throws CardException {
		if (LOG_VERBOSE) Log.v(TAG, "testBasicChannelOpening()");
		
		ICardChannel basicChannel = mSmartcardClient.openBasicChannel(mReader);
		TestCase.assertNotNull(basicChannel);
		byte[] r = basicChannel.transmit(new byte[]{(byte)0x10,(byte)0x20,(byte)0x30,(byte)0x40});
		TestCase.assertTrue(r.length>=2);
		basicChannel.close();
	} // testBasicChannelTransmit
	
} // class
