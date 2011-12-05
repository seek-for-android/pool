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
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Mother class of all Open Mobile API test cases.
 */
public abstract class OMAPITestCase extends AndroidTestCase implements SEService.CallBack {

	private static final boolean LOG_VERBOSE = true;
	private static final String TAG = "CTS";

	/**
	 * AID of test applet that will be used for most of the tests
	 */
	public static final byte[] AID_APDU_TESTER = {(byte)0xD2, (byte)0x76, (byte)0x00, (byte)0x01, (byte)0x18, (byte)0x00, (byte)0x00};

	volatile SEService mOMService;
	String   mReaderName = "CTSMock";
	Reader[] mReaders;
	Reader   mReader;

	public OMAPITestCase() { 
	}

	public void serviceConnected(SEService service) {
	    android.os.SystemClock.sleep(100);
		mReaders = service.getReaders();
		mOMService = service;
	} // serviceConnected
	
	/**
	 * connects to the Open Mobile Service and verifies 
	 * that reader mReader is available and 
	 * that a secure element is available in the reader
	 */
	@Override
	public void setUp() throws Exception {
		if (LOG_VERBOSE) Log.v(TAG, "setUp()");

		super.setUp();

		// get reader name from InstrumentationTestRunner parameter 
		// only if a "reader" parameter is given:
		if (InstrumentationTestRunnerParameterized.mArguments != null && 
			InstrumentationTestRunnerParameterized.mArguments.getString("reader") != null)
			mReaderName = InstrumentationTestRunnerParameterized.mArguments.getString("reader");
		
		new SEService(getContext(), this);
		
		// wait for the service to connect; at the most 10s:
		for(int i=0; i<100 && mOMService==null; i++) android.os.SystemClock.sleep(100);

		assertNotNull("Error: service not connected", mOMService);
		assertNotNull("Error: no readers", mReaders);

		mReader = null;
		for (Reader reader: mReaders) {
			if (mReaderName.equals(reader.getName())) { mReader = reader; break; }
		}

		// log list of available readers:
		if (LOG_VERBOSE) 
			for (Reader reader: mReaders) {
				Log.v(TAG, String.format("  %s %c \"%s\"", 
					reader==mReader?"-->":"   ",
					reader.isSecureElementPresent()?'*':'o',
					reader.getName()));
			}
		
	    assertNotNull("Error: reader \""+mReaderName+"\" not available", mReader);
		assertTrue("Error: no secure element present in reader \""+mReaderName+"\"", mReader.isSecureElementPresent());
		if (LOG_VERBOSE) Log.v(TAG, ".");
	} // setUp

	@Override
	public void tearDown() throws Exception {
		if (mOMService==null) return;
		if (LOG_VERBOSE) Log.v(TAG, "tearDown()");

		mOMService.shutdown();
		mReaders = null;
		mReader = null;
		super.tearDown();
		if (LOG_VERBOSE) { Log.v(TAG, "."); Log.v(TAG, ""); }
	} // tearDown

	public void testPreconditions() {
	} // testPreconditions
	
} // class

