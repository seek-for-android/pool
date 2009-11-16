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
 * JUnit tests for the class 'android.smartcard.CommandAPDU'.
 */
public class CommandAPDUTest extends TestCase {

	public static final byte[] CASE4 = new byte[] { 0x00, (byte) 0xA4,
			(byte) 0x04, (byte) 0x00, 0x08, (byte) 0xA0, 0x00, 0x00, 0x00,
			0x03, 0x00, 0x00, 0x00, 0x00 };

	private CommandAPDU _case4;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		_case4 = new CommandAPDU(CASE4);
	}

	/**
	 * Test method for {@link android.smartcard.CommandAPDU#hashCode()}.
	 */
	public void testHashCode() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for {@link android.smartcard.CommandAPDU#CommandAPDU(byte[])}
	 * .
	 */
	public void testCommandAPDUByteArray() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link android.smartcard.CommandAPDU#CommandAPDU(byte[], int, int)}.
	 */
	public void testCommandAPDUByteArrayIntInt() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link android.smartcard.CommandAPDU#CommandAPDU(java.nio.ByteBuffer)}.
	 */
	public void testCommandAPDUByteBuffer() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link android.smartcard.CommandAPDU#CommandAPDU(int, int, int, int)}.
	 */
	public void testCommandAPDUIntIntIntInt() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link android.smartcard.CommandAPDU#CommandAPDU(int, int, int, int, byte[])}
	 * .
	 */
	public void testCommandAPDUIntIntIntIntByteArray() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link android.smartcard.CommandAPDU#CommandAPDU(int, int, int, int, byte[], int)}
	 * .
	 */
	public void testCommandAPDUIntIntIntIntByteArrayInt() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link android.smartcard.CommandAPDU#CommandAPDU(int, int, int, int, byte[], int, int)}
	 * .
	 */
	public void testCommandAPDUIntIntIntIntByteArrayIntInt() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link android.smartcard.CommandAPDU#CommandAPDU(int, int, int, int, byte[], int, int, int)}
	 * .
	 */
	public void testCommandAPDUIntIntIntIntByteArrayIntIntInt() {
		// Case 1
		CommandAPDU case4 = new CommandAPDU(CASE4[0] & 0xFF,
				CASE4[1] & 0xFF, CASE4[2] & 0xFF, CASE4[3] & 0xFF,
				CASE4, 5, 8, 256);
		assertEquals(_case4, case4);
	}

	/**
	 * Test method for
	 * {@link android.smartcard.CommandAPDU#CommandAPDU(int, int, int, int, int)}
	 * .
	 */
	public void testCommandAPDUIntIntIntIntInt() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link android.smartcard.CommandAPDU#equals(java.lang.Object)}.
	 */
	public void testEqualsObject() {
		// fail("Not yet implemented");
	}

	/**
	 * Test method for {@link android.smartcard.CommandAPDU#getBytes()}.
	 */
	public void testGetBytes() {
		// Case 1
		assertTrue(Arrays.equals(CASE4, _case4.getBytes()));
	}

	/**
	 * Test method for {@link android.smartcard.CommandAPDU#getCLA()}.
	 */
	public void testGetCLA() {
		assertSame(CASE4[0] & 0xFF, _case4.getCLA());
	}

	/**
	 * Test method for {@link android.smartcard.CommandAPDU#getData()}.
	 */
	public void testGetData() {
		assertTrue(Arrays.equals(new byte[] { (byte) 0xA0, 0x00, 0x00, 0x00,
				0x03, 0x00, 0x00, 0x00 }, _case4.getData()));
	}

	/**
	 * Test method for {@link android.smartcard.CommandAPDU#getINS()}.
	 */
	public void testGetINS() {
		assertEquals(CASE4[1] & 0xFF, _case4.getINS());
	}

	/**
	 * Test method for {@link android.smartcard.CommandAPDU#getNc()}.
	 */
	public void testGetNc() {
		assertEquals(CASE4[4] & 0xFF, _case4.getNc());
	}

	/**
	 * Test method for {@link android.smartcard.CommandAPDU#getNe()}.
	 */
	public void testGetNe() {
		assertEquals(256, _case4.getNe());
	}

	/**
	 * Test method for {@link android.smartcard.CommandAPDU#getP1()}.
	 */
	public void testGetP1() {
		assertEquals(CASE4[2] & 0xFF, _case4.getP1());
	}

	/**
	 * Test method for {@link android.smartcard.CommandAPDU#getP2()}.
	 */
	public void testGetP2() {
		assertEquals(CASE4[3] & 0xFF, _case4.getP2());
	}

	/**
	 * Test method for {@link android.smartcard.CommandAPDU#toString()}.
	 */
	public void testToString() {
		// fail("Not yet implemented");
	}

}
