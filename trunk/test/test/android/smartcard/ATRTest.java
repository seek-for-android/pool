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

package android.smartcard;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * JUnit tests for the class 'android.smartcard.ATR'.
 */
public class ATRTest extends TestCase {

	public static final byte[] ATR1 = new byte[] { 0x3B, (byte) 0x9F,
			(byte) 0x96, (byte) 0x80, 0x3F, 0x47, (byte) 0xA0, (byte) 0x80,
			0x31, (byte) 0xE0, 0x73, (byte) 0xF6, 0x21, 0x13, 0x57, 0x4A, 0x4D,
			0x0E, 0x0D, 0x31, 0x30, 0x00, (byte) 0xE5 };

	public static final byte[] ATR2 = new byte[] { 0x3B, (byte) 0xF8, 0x18,
			0x00, 0x00, (byte) 0x81, 0x31, (byte) 0xFE, 0x45, 0x00, 0x73,
			(byte) 0xC8, 0x40, 0x13, 0x00, (byte) 0x90, 0x00, (byte) 0x93 };

	private ATR _atr1;
	private ATR _atr2;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		_atr1 = new ATR(ATR1);
		_atr2 = new ATR(ATR2);
	}

	/**
	 * Test method for {@link android.smartcard.ATR#hashCode()}.
	 */
	public void testHashCode() {
		assertNotSame(_atr1.hashCode(), _atr2.hashCode());
	}

	/**
	 * Test method for {@link android.smartcard.ATR#ATR(byte[])}.
	 */
	public void testATR() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for {@link android.smartcard.ATR#equals(java.lang.Object)}.
	 */
	public void testEqualsObject() {
		// Case 1
		assertFalse(_atr1.equals(_atr2));
		// Case 2
		ATR atr1 = new ATR(_atr1.getBytes());
		assertTrue(_atr1.equals(atr1));
	}

	/**
	 * Test method for {@link android.smartcard.ATR#getBytes()}.
	 */
	public void testGetBytes() {
		// Case 1
		assertTrue(Arrays.equals(ATR1, _atr1.getBytes()));
		// Case 2
		assertTrue(Arrays.equals(ATR2, _atr2.getBytes()));
	}

	/**
	 * Test method for {@link android.smartcard.ATR#getHistoricalBytes()}.
	 */
	public void testGetHistoricalBytes() {
		// Case 1
		assertTrue(Arrays.equals(new byte[] { (byte) 0x80, 0x31, (byte) 0xE0,
				0x73, (byte) 0xF6, 0x21, 0x13, 0x57, 0x4A, 0x4D, 0x0E, 0x0D,
				0x31, 0x30, 0x00 }, _atr1.getHistoricalBytes()));
		// Case 2
		assertTrue(Arrays.equals(new byte[] { 0x00, 0x73, (byte) 0xC8, 0x40,
				0x13, 0x00, (byte) 0x90, 0x00 }, _atr2.getHistoricalBytes()));
	}

	/**
	 * Test method for {@link android.smartcard.ATR#toString()}.
	 */
	public void testToString() {
		// fail("Not yet implemented");
	}

}
