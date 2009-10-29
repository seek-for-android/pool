/*
 * Copyright 2009 Giesecke & Devrient GmbH.
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

package android.smartcard.libraries.smartcard.mcex;

import android.smartcard.libraries.smartcard.mcex.McexException;
import android.smartcard.libraries.smartcard.mcex.McexJni;
import android.smartcard.libraries.smartcard.mcex.McexJni.OpenMode;

import junit.framework.TestCase;

/**
 * JUnit tests for the class 'android.smartcard.libraries.smartcard.mcex.McexJni'.
 */
public class McexJniTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.mcex.McexJni#close()}.
	 * @throws McexException 
	 */
	public final void testClose() throws McexException {
		// Case 1
		int fd = McexJni.open(OpenMode.Exclusive);
		assertTrue(fd != 0);
		McexJni.close(fd);
		// Case 2
		McexJni.close(0);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.mcex.McexJni#open()}.
	 * @throws McexException 
	 */
	public final void testOpen() throws McexException {
		// Case 1
		int fd = McexJni.open(OpenMode.Shared);
		assertEquals(0, fd);
		// Case 2
		fd = McexJni.open(OpenMode.BeginExclusive);
		assertTrue(fd != 0);
		McexJni.close(fd);
		// Case 3
		fd = McexJni.open(OpenMode.Shared);
		assertEquals(0, fd);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.mcex.McexJni#stat()}.
	 * @throws McexException 
	 */
	public final void testStat() throws McexException {
		// Case 1
		int stat = McexJni.stat();
		assertTrue(stat == 0);
	}

	/**
	 * Test method for {@link android.smartcard.libraries.smartcard.mcex.McexJni#transmit()}.
	 * @throws McexException 
	 */
	public final void testTransmit() throws McexException {
		// Case 1
		int fd = McexJni.open(OpenMode.Shared);
		assertEquals(0, fd);
		
        byte[] response = McexJni.transmit(fd, new byte[] { 0x20, (byte) 0x12, 0x01, 0x01, 0x00 });
        assertTrue(response.length > 0);
        
        // Case 2
        response = McexJni.transmit(fd, new byte[] { 0x00, (byte) 0xA4, 0x04, 0x00, 0x00 });
        assertTrue(response.length > 0);
	}
}
