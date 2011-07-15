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

import junit.framework.TestCase;
import android.smartcard.SmartcardClient;
import android.smartcard.SmartcardClient.ISmartcardConnectionListener;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Mother class of all Open Mobile API test cases.
 */
public abstract class SmartcardTestCase extends AndroidTestCase implements ISmartcardConnectionListener {

	private static final boolean LOG_VERBOSE = true;
	private static final String TAG = "CTS";

	/**
	 * AID of test applet that will be used for most of the tests
	 */
	public static final byte[] AID_APDU_TESTER = {(byte)0xD2, (byte)0x76, (byte)0x00, (byte)0x01, (byte)0x18, (byte)0x01, (byte)0x01};

	SmartcardClient mSmartcardClient;
	String   mReader = "CTSMock";
	volatile String[] mReaders;

	public SmartcardTestCase() { 
	}

	public void serviceConnected() {
		try { 
		    android.os.SystemClock.sleep(100);
			mReaders = mSmartcardClient.getReaders(); 
		}
		catch (Exception e) { 
			Log.v(TAG, "serviceConnected: exception="+e.toString()); 
			mReaders = null; 
		}
	} // serviceConnected

	public void serviceDisconnected() {
		mReaders = new String[0];
	} // serviceDisconnected
	
	/**
	 * connects to the Open Mobile Service and verifies 
	 * that reader mReader is available and 
	 * that a secure element is available in the reader
	 */
	protected void setUp() throws Exception {
		if (LOG_VERBOSE) Log.v(TAG, "setUp()");

		super.setUp();
		mSmartcardClient = new SmartcardClient(getContext(), this);
		
		// wait for the smart card client to connect; at the most 3s:
		for(int i=0; i<30 && mReaders==null; i++)android.os.SystemClock.sleep(100);

		TestCase.assertNotNull("Error: no readers", mReaders);

		// log list of available readers:
		boolean readerAvailable = false;
		if (LOG_VERBOSE) 
			for (String reader: mReaders) {
				readerAvailable |= mReader.equals(reader); 
				Log.v(TAG, "  isCardPresent(\""+reader+"\")="+mSmartcardClient.isCardPresent(reader));
			}
		TestCase.assertTrue("Error: reader \""+mReader+"\" not available", readerAvailable);
		TestCase.assertTrue("Error: no card present in reader \""+mReader+"\"", 
				mSmartcardClient.isCardPresent(mReader));
		if (LOG_VERBOSE) Log.v(TAG, ".");
	} // setUp

	protected void tearDown() throws Exception {
		if (LOG_VERBOSE) Log.v(TAG, "tearDown()");
		mSmartcardClient.shutdown();
		super.tearDown();
	} // tearDown

} // class

