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
import java.util.NoSuchElementException;

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

	public static final byte[] AID_APDU_TESTER = {(byte)0xD2, (byte)0x76, (byte)0x00, (byte)0x01, (byte)0x18, (byte)0x00, (byte)0x00};

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
		Channel channel;
		try { 
			channel = session.openBasicChannel(null); 
		}
		catch (NoSuchElementException nsee) { 
			channel = session.openLogicalChannel(AID_APDU_TESTER); 
		}
		
		TestCase.assertNotNull(channel);
		channel.close();
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
		Channel channel;
		try { 
			channel = session.openBasicChannel(null); 
		}
		catch (NoSuchElementException nsee) { 
			channel = session.openLogicalChannel(AID_APDU_TESTER); 
		}
		
		TestCase.assertNotNull(channel);
		byte[] r = channel.transmit(new byte[]{(byte)0x10,(byte)0x20,(byte)0x30,(byte)0x40});
		TestCase.assertTrue(r.length>=2);
		channel.close();
		session.close();
	} // testBasicChannelTransmit
	
} // class
