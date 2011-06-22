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
import org.simalliance.openmobileapi.cts.Util;
import android.util.Log;

public class TestAPDUCases extends OMAPITestCase {

	private static final boolean LOG_VERBOSE = true;
	private static final String TAG = "CTS";

	private static final byte[] cmdCase1={(byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00};
	private static final byte[] cmdCase2={(byte)0x00, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00};
	private static final byte[] cmdCase3={(byte)0x00, (byte)0x03, (byte)0x00, (byte)0x00};
	private static final byte[] cmdCase4={(byte)0x00, (byte)0x04, (byte)0x00, (byte)0x00};

	protected Session mSession;
	
	public TestAPDUCases() {
	} // constructor

	@Override
	public void setUp() throws Exception {
		super.setUp();
		mSession = mReader.openSession();
	} // setUp
	
	@Override
	public void tearDown() throws Exception {
		mSession.close();
		super.tearDown();
	} // tearDown
	
	/**
	 * tests case-1 APDU (without command data, without response data).
	 * @throws IOException 
	 */
	public void testAPDUCase1() throws IOException {
		if (LOG_VERBOSE) Log.v(TAG, "testAPDUCase1()");

		Channel channel = mSession.openBasicChannel(AID_APDU_TESTER);
		
		byte[] cmd;
		byte[] response;
		cmd=cmdCase1.clone();

		if (LOG_VERBOSE) Log.v(TAG, "  --> "+ Util.bytesToMaxHexString(cmd));
		response = channel.transmit(cmd);
		if (LOG_VERBOSE) Log.v(TAG, "  <-- " + Util.bytesToMaxHexString(response));
		int lr=response.length;
		TestCase.assertTrue("Error: SW!=0x9000", lr>=2 && response[lr-2]==(byte)0x90 && response[lr-1]==(byte)0x00);
		channel.close();
	} // testAPDUCase1

	/**
	 * tests case-2 APDUs (without command data, with response data)
	 * with various lengths.
	 * @throws IOException
	 */
	public void testAPDUCase2() throws IOException {
		if (LOG_VERBOSE) Log.v(TAG, "testAPDUCase2()");

		Channel channel = mSession.openBasicChannel(AID_APDU_TESTER);

		byte[] cmd=cmdCase2.clone();
		for(int le=0; le<255; le++) {
			if (le>10 && le<250 && le%13>2) continue; // skip most cases
			cmd[4]=(byte)le;
			if (LOG_VERBOSE) Log.v(TAG, "  --> "+ Util.bytesToMaxHexString(cmd));
			byte[] response = channel.transmit(cmd);
			if (LOG_VERBOSE) Log.v(TAG, "  <-- " + Util.bytesToMaxHexString(response, 30));
			int lr=response.length;
			TestCase.assertTrue("Error: SW!=0x9000", lr>=2 && response[lr-2]==(byte)0x90 && response[lr-1]==(byte)0x00);
			int i;
			for(i=0; i<lr-2; i++) TestCase.assertTrue("Error: wrong response data", response[i]==(byte)i);                    
			TestCase.assertTrue("Error: response has wrong length", lr-2==le);                    
		}
		channel.close();
	} // testAPDUCase2

	/**
	 * tests case-3 APDUs (with command data, without response data)
	 * with various lengths.
	 * @throws IOException
	 */
	public void testAPDUCase3() throws IOException {
		if (LOG_VERBOSE) Log.v(TAG, "testAPDUCase3()");

		Channel channel = mSession.openBasicChannel(AID_APDU_TESTER);

		for(int lc=0; lc<255; lc++) {
			if (lc>10 && lc<250 && lc%13>2) continue; // skip most cases
			byte[] cmd=new byte[5+lc];
			System.arraycopy(cmdCase3,0,cmd,0,4);
			cmd[4]=(byte)lc;
			for (int i=0; i<lc; i++) cmd[5+i]=(byte)i;
			if (LOG_VERBOSE) Log.v(TAG, "  --> "+ Util.bytesToMaxHexString(cmd, 30));
			byte[] response = channel.transmit(cmd);
			if (LOG_VERBOSE) Log.v(TAG, "  <-- " + Util.bytesToMaxHexString(response));
			int lr=response.length;
			TestCase.assertTrue("Error: SW!=0x9000", lr>=2 && response[lr-2]==(byte)0x90 && response[lr-1]==(byte)0x00);
			TestCase.assertTrue("Error: response has wrong length", lr==2);                    
		}
		channel.close();
	} // testAPDUCase3

	/**
	 * tests case-4 APDUs (with command data, with response data)
	 * with various lengths.
	 * @throws IOException
	 */
	public void testAPDUCase4() throws IOException {
		if (LOG_VERBOSE) Log.v(TAG, "testAPDUCase4()");

		Channel channel = mSession.openBasicChannel(AID_APDU_TESTER);

		for(int l=0; l<255; l++) {
			if (l>10 && l<250 && l%13>2) continue; // skip most cases
			int lc=l;
			int le=l;
			byte[] cmd=new byte[5+lc+1];
			System.arraycopy(cmdCase4, 0, cmd, 0, 4);
			cmd[4]=(byte)lc;
			for (int i=0; i<lc; i++) cmd[5+i]=(byte)i;
			cmd[5+lc]=(byte)le;

			if (LOG_VERBOSE) Log.v(TAG, "  --> "+ Util.bytesToMaxHexString(cmd, 30));
			byte[] response = channel.transmit(cmd);
			if (LOG_VERBOSE) Log.v(TAG, "  <-- " + Util.bytesToMaxHexString(response, 30));
			int lr=response.length;
			TestCase.assertTrue("Error: SW!=0x9000", lr>=2 && response[lr-2]==(byte)0x90 && response[lr-1]==(byte)0x00);
			int i;
			for(i=0; i<lr-2; i++) TestCase.assertTrue("Error: wrong response data", response[i]==(byte)i);                    
			TestCase.assertTrue("Error: response has wrong length", lr-2==le);                    
		}
		channel.close();
	} // testAPDUCase4

} // class
