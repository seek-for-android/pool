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

import java.io.IOException;

import junit.framework.TestCase;
import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Session;
import android.util.Log;

/**
 * 
 *
 */
public class TestBasic extends OMAPITestCase {

	private static final boolean LOG_VERBOSE = true;
	private static final String TAG = "CTS";

	public TestBasic() {
	} // constructor

	/**
	 * tests opening a session and reading the ATR
	 * 
	 * @throws IOException
	 */
	public void testATR() throws IOException {
		if (LOG_VERBOSE) Log.v(TAG, "testATR()");

		Session session = mReader.openSession();
		byte[] atr = session.getATR();
		Log.v(TAG, "ATR: "+Util.bytesToHexString(atr));
		boolean atrOk = (atr==null) || (atr.length>2 && (atr[0]==(byte)0x3B || atr[0]==(byte)0x3F));
		assertTrue("Error: wrong ATR", atrOk);
		session.close();
	} // testATR

	/**
	 * tests opening a basic channel
	 * 
	 * @throws IOException
	 */
	public void testOpen() throws IOException {
		if (LOG_VERBOSE) Log.v(TAG, "testBasicChannelOpening()");
		
		Session session = mReader.openSession();
		Channel basicChannel = session.openBasicChannel(null);
		TestCase.assertNotNull(basicChannel);
		basicChannel.close();
		session.close();
	} // testBasicChannelOpening
	
	/**
	 * tests transmitting on the basic channel
	 * 
	 * @throws IOException
	 */
	public void testTransmit() throws IOException {
		if (LOG_VERBOSE) Log.v(TAG, "testBasicChannelOpening()");
		
		Session session = mReader.openSession();
		Channel basicChannel = session.openBasicChannel(null);
		TestCase.assertNotNull(basicChannel);
		byte[] r = basicChannel.transmit(new byte[]{(byte)0x10,(byte)0x20,(byte)0x30,(byte)0x40});
		TestCase.assertTrue(r.length>=2);
		basicChannel.close();
		session.close();
	} // testBasicChannelTransmit
	
} // class
