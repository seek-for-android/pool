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
import java.util.Arrays;

import mockcard.MockCard;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;

import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Open Mobile API Conformance Tests
 */
public class TestConformanceRequirements extends AndroidTestCase implements SEService.CallBack {

	private static final boolean LOG_VERBOSE = true;
	private static final String TAG = "CTS";

	/**
	 * AID of test applet that will be used for most of the tests
	 */
	public static final byte[] AID_APDU_TESTER = {(byte)0xD2, (byte)0x76, (byte)0x00, (byte)0x01, (byte)0x18, (byte)0x01, (byte)0x01};

	volatile SEService mOMService;
	String   mReaderName = "CTSMock";
    String   mReaderNameNoCard = "CTSMockNoCard";
	Reader[] mReaders;

	public void serviceConnected(SEService service) {
		mReaders = service.getReaders();
		mOMService = service;
	} // serviceConnected
	
	public TestConformanceRequirements() { 
	} // constructor

	@Override
	public void setUp() throws Exception {
		super.setUp();
		PluginTerminal.setCardPresence(true);
	} // setUp
	
	@Override
	public void tearDown() throws Exception {
		if (LOG_VERBOSE) Log.v(TAG, "shutDownService()");
		PluginTerminal.setCardPresence(true);
		if (mOMService!=null) 
			mOMService.shutdown();
		mReaders = null;
		super.tearDown();
		if (LOG_VERBOSE) { Log.v(TAG, "."); Log.v(TAG, ""); }
	} // tearDown

	void waitMaxUntilConnected(SEService service) {
		// wait for the service to connect; at the most 5s:
		//for(int i=0; i<50 && !service.isConnected(); i++) android.os.SystemClock.sleep(100); // <-- results in failures
		for(int i=0; i<50 && mOMService==null; i++) android.os.SystemClock.sleep(100);
		assertTrue("Error: service did not connect after 5s", service.isConnected());
	} // waitMaxUntilConnected
	
	Reader getDefaultTestReader() {
		assertNotNull(mReaders);
		Reader reader = null;
		for (Reader readerL: mReaders) if (readerL.getName().equals(mReaderName)) { reader = readerL; break; }
		assertNotNull(reader);
		return reader;
	} // getDefaultTestReader
	
	   Reader getDefaultTestReaderNoCard() {
	        assertNotNull(mReaders);
	        Reader reader = null;
	        for (Reader readerL: mReaders) if (readerL.getName().equals(mReaderNameNoCard)) { reader = readerL; break; }
	        assertNotNull(reader);
	        return reader;
	    } // getDefaultTestReader
	
	public void test_SEService_GetReaders() {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		assertNotNull(mOMService);
		assertNotNull(mReaders);
		int n = mReaders.length;
		Reader reader = null;
		for(Reader readerL: mReaders) if (readerL.getName().equals(mReaderName)){ reader = readerL; }
		assertTrue(reader != null ||     // CRN1 
				   n==0);                // CRN2
		for (int i=0; i<n; i++) {
			for (int j=i+1; j<n; j++) {
				assertFalse("duplicate reader name "+mReaders[i].getName(), 
						mReaders[i].getName().equals(mReaders[j].getName())); // CRN3
			}
		}
	} // test_SEService_GetReaders

	public void test_SEService_isConnected() {
		SEService service = new SEService(getContext(), this);
		//assertFalse(service.isConnected()); // CRN1   cannot be tested because service connects so quick after SEService object generation
		waitMaxUntilConnected(service);
		assertTrue(service.isConnected()); // CRN2
	} // test_SEService_isConnected

	public void test_SEService_shutdown() throws Exception {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		// SEService.shutdown() is tested implicitly when tearDown() is executed after this test method
	} // test_SEService_shutdown
	
	public void test_SEService_Callback_serviceConnected() {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		assertTrue(service==mOMService); // CRN1
	} // test_SEService_Callback_serviceConnected

	public void test_Reader_getName() {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		assertNotNull(mReaders);
		Reader reader = mReaders[0];
		assertTrue(reader.getName()!=null && reader.getName().length()>0); // CRN1
	} // test_Reader_getName

	public void test_Reader_getSEService() {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		assertNotNull(mReaders);
		Reader reader = mReaders[0];
		assertTrue(reader.getSEService()==service);
	} // test_Reader_getSEService

	public void test_Reader_isSecureElementPresent() {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		
		Reader reader = getDefaultTestReaderNoCard();
		PluginTerminal.setCardPresence(false);
		assertFalse(reader.isSecureElementPresent()); // CRN1.1
		
	    reader = getDefaultTestReader();
		PluginTerminal.setCardPresence(true);
		assertTrue(reader.isSecureElementPresent()); // CRN1.2
		
	} // test_Reader_isSecureElementPresent
	
	public void test_Reader_openSession() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		assertNotNull(session); // CRN1
		session.close();
	} // test_Reader_openSession
	
	public void test_Reader_closeSessions() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		final int n = 3;
		Session[] sessions = new Session[n];
		Channel[] channels = new Channel[n];
		for(int i=0; i<n; i++) {
			sessions[i] = reader.openSession();
			channels[i] = sessions[i].openLogicalChannel(OMAPITestCase.AID_APDU_TESTER);
		}
		reader.closeSessions(); 
		for(Session session: sessions) 
			assertTrue(session.isClosed()); // CRN1
		for(Channel channel: channels) 
			assertTrue(channel.isClosed()); // CRN2
	} // test_Reader_closeSessions
	
	public void test_Session_getReader() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		assertTrue(session.getReader()==reader); // CRN1
		session.close();
	} // test_Session_getReader

	public void test_Session_getATR() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		
		assertTrue(session.getATR()==null ||                       // CRN2
				   Arrays.equals(session.getATR(), MockCard.ATR)); // CRN1
		session.close();
	} // test_Session_getATR

	public void test_Session_close() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		Channel channel = session.openBasicChannel(null);
		session.close();
		assertTrue(session.isClosed()); // CRN1
		assertTrue(channel.isClosed()); // CRN2
	} // test_Session_close

	public void test_Session_isClosed() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		assertFalse(session.isClosed()); // CRN2
		session.close();
		assertTrue(session.isClosed()); // CRN1
	} // test_Session_isClosed
	
	public void test_Session_closeChannels() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		Channel[] channels = new Channel[3];
		for(int i=0; i<channels.length; i++)
			channels[i] = session.openLogicalChannel(OMAPITestCase.AID_APDU_TESTER);
		session.closeChannels();
		for(Channel channel: channels)
			assertTrue(channel.isClosed()); // CRN1
		session.close();
	} // test_Session_closeChannels

	public void test_Session_openBasicChannel() throws IOException{
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		Channel channel;
		byte[] response;
		channel = session.openBasicChannel(null); 
		response = channel.transmit(new byte[]{(byte)0x00, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x04});
		assertFalse(Arrays.equals(response, new byte[]{(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x90, (byte)0x00})); // CRN2, AID_APDU_TESTER was NOT selected
		channel.close();
		channel = session.openBasicChannel(OMAPITestCase.AID_APDU_TESTER);
		response = channel.transmit(new byte[]{(byte)0x00, (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x01});
		assertTrue(Arrays.equals(response, new byte[]{(byte)0x00, (byte)0x90, (byte)0x00})); // CRN2, AID_APDU_TESTER has been selected by openBasicChannel
		// according to CRN3 and CRN4 openBasicChannel must return null if a basic channel is already open (expect NO exception!):
		assertNull(session.openBasicChannel(null)); // CRN3, CRN4 
		session.close();
	} // test_Session_openBasicChannel
	
	public void test_Session_openLogicalChannel() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		Channel channel;
		byte[] response;
		channel = session.openLogicalChannel(OMAPITestCase.AID_APDU_TESTER); 
		response = channel.transmit(new byte[]{(byte)0x00, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x04});
		assertTrue(Arrays.equals(response, new byte[]{(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x90, (byte)0x00})); // CRN1, AID_APDU_TESTER was selected
		channel.close();
		channel = session.openLogicalChannel(null);
		response = channel.transmit(new byte[]{(byte)0x00, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x04});
		assertFalse(Arrays.equals(response, new byte[]{(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x90, (byte)0x00})); // CRN2, AID_APDU_TESTER was NOT selected
		// CRN3: nothing to test
		
		// CRN4:
		// open logical channels until capacity of card is reached;
		// openLogiocalChannel must return null if no more channels can be opened (expect NO exception!)
		for(int i=0; i<50; i++) {
			channel = session.openLogicalChannel(null);
			if (channel==null)
				break;
		}
		session.close();
	} // test_Session_openLogicalChannel

	public void test_Channel_close() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		Channel channel;
		channel = session.openBasicChannel(null); // CRN1
		//assertNull(session.openBasicChannel(null)); // basic channel is locked (expect NO exception)
		channel.close(); // must release basic channel
		assertTrue(channel.isClosed());
        channel = session.openBasicChannel(null);
		assertNotNull(channel); // CRN2: basic channel is not locked
		channel.close();
		assertTrue(channel.isClosed());
		channel.close(); // CRN3: closing a closed channel doesn't hurt (expect NO exception)
        // CRN4 can't be tested
		session.close();
	} // test_Channel_close

	public void test_Channel_isBasicChannel() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		Channel channel;
		channel = session.openBasicChannel(null); 
		assertTrue(channel.isBasicChannel()); // CRN1
		channel.close();
		channel = session.openLogicalChannel(null); 
		assertFalse(channel.isBasicChannel()); // CRN2
		channel.close();
		session.close();
	} // test_Channel_isBasicChannel

	public void test_Channel_isClosed() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		Channel channel = session.openBasicChannel(null); 
		assertFalse(channel.isClosed()); // CRN1
		channel.close();
		assertTrue(channel.isClosed()); // CRN2
		session.close();
	} // test_Channel_isClosed

	public void test_Channel_getSession() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		Channel channel = session.openBasicChannel(null); 
		assertTrue(channel.getSession()==session); // CRN1
		channel.close();
		session.close();
	} // test_Channel_getSession

	static class ConcurrentTransmitRunnable implements Runnable {

		static int iI=0;
	    
		Channel channel;
	    byte[] command, expectedResponse;
		
    	public ConcurrentTransmitRunnable() {}
    	
    	public ConcurrentTransmitRunnable(Channel channel) { 
    		this.channel = channel; 
    		int l = (30 + 5*iI) % 255; // each instance uses it's own APDU length

    		command = new byte[5+l+1];
    		command[0]=(byte)0x00; 
    		command[1]=(byte)0x04; 
    		command[2]=(byte)0x00; 
    		command[3]=(byte)0x00; 
    		command[4]=(byte)l; 
    		for(int i=0; i<l; i++) command[5+i]=(byte)i;
    		command[5+l]=(byte)l;

    		expectedResponse = new byte[l+2];
    		for(int i=0; i<l; i++) expectedResponse[i]=(byte)i;
    		expectedResponse[l  ]=(byte)0x90;
    		expectedResponse[l+1]=(byte)0x00;
    		
    		iI++; 
    	} // constructor
 
    	public void run() {
    		try {
    			for (int i=0; i<100; i++) {
    				assertTrue(Arrays.equals(channel.transmit(command), expectedResponse));
    				android.os.SystemClock.sleep(2);
    			}
    		}
    		catch(IOException ioe) {
    			fail();
    		}
    	} // run
    	
    } // class
	
	public void test_Channel_transmit() throws IOException {
		SEService service = new SEService(getContext(), this);
		waitMaxUntilConnected(service);
		Reader reader = getDefaultTestReader();
		Session session = reader.openSession();
		Channel channel0;
		byte[] response;
		channel0 = session.openBasicChannel(OMAPITestCase.AID_APDU_TESTER); 
		response = channel0.transmit(new byte[]{(byte)0x00, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x04});
		assertTrue(Arrays.equals(response, new byte[]{(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x90, (byte)0x00})); // CRN1, obviously C-APDU has been transmitted correctly
		channel0.close();
		
		// concurrent transmit test for CRN2 and CRN3:
		// start N threads each running a loop that transmits and receives long C- and R-APDUs on a different channel
		final int N = 8;
		final Channel[] channels = new Channel[N];
		Thread[] threads = new Thread[N];
		for(int i=0; i<N; i++) {
			channels[i] = session.openLogicalChannel(OMAPITestCase.AID_APDU_TESTER);
			threads[i] = new Thread(new ConcurrentTransmitRunnable(channels[i]));
		}

		// start all threads:
		for(Thread thread: threads) thread.start();
		
		// wait till all threads have finished:
		while (true) {
			int i;
			for (i=0; i<N; i++) if (threads[i].isAlive()) break;
			if (i>=N) break;
			android.os.SystemClock.sleep(100);
		}

		for(Channel channel: channels) channel.close();
		
		channel0 = session.openLogicalChannel(OMAPITestCase.AID_APDU_TESTER); 
		byte cla = channel0.transmit(new byte[]{(byte)0x00, (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x01})[0];
		// CRN4: manipulating the lowest two bits of the CLA doesn't affect the channel number that is actually used in the C-APDU
		assertTrue(channel0.transmit(new byte[]{(byte)(cla^0x01), (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x01})[0]==cla);
		assertTrue(channel0.transmit(new byte[]{(byte)(cla^0x02), (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x01})[0]==cla);
		channel0.close();
		
		session.close();
	} // test_Channel_transmit

} // class

