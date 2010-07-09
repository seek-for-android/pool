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

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class CommandAPDU implements java.io.Serializable {

	private static final long serialVersionUID = -1938399342443540322L;

	private byte[] apdu;

	public CommandAPDU(byte[] apdu) {
		assertApdu(apdu);
		this.apdu = apdu.clone();
	}

	public CommandAPDU(byte[] apdu, int apduOffset, int apduLength) {
		assertRange(apdu, apduOffset, apduLength, "APDU");
		this.apdu = new byte[apduLength];
		System.arraycopy(apdu, apduOffset, this.apdu, 0, apduLength);
		assertApdu(this.apdu);
	}

	public CommandAPDU(ByteBuffer apdu) {
		this.apdu = new byte[apdu.remaining()];
		apdu.get(this.apdu);
		assertApdu(this.apdu);
	}

	public CommandAPDU(int cla, int ins, int p1, int p2) {
		this(cla, ins, p1, p2, null, 0, 0, 0);
	}

	public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data) {
		this(cla, ins, p1, p2, data, 0, (data == null) ? 0 : data.length, 0);
	}

	public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data, int ne) {
		this(cla, ins, p1, p2, data, 0, (data == null) ? 0 : data.length, ne);
	}

	public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data,
			int dataOffset, int dataLength) {
		this(cla, ins, p1, p2, data, dataOffset, dataLength, 0);
	}

	public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data,
			int dataOffset, int dataLength, int ne) {
		assertRange(cla, 0, 0xFF, "cla");
		assertRange(ins, 0, 0xFF, "ins");
		assertRange(p1, 0, 0xFF, "p1");
		assertRange(p2, 0, 0xFF, "p2");
		assertRange(ne, 0, 256, "ne");
		assertRange(data, dataOffset, dataLength, "data");

		int apduLength = 4;
		if (dataLength > 0)
			apduLength += (dataLength + 1);
		if (ne > 0)
			apduLength += 1;

		apdu = new byte[apduLength];
		apdu[0] = (byte) cla;
		apdu[1] = (byte) ins;
		apdu[2] = (byte) p1;
		apdu[3] = (byte) p2;
		if (dataLength > 0) {
			apdu[4] = (byte) dataLength;
			System.arraycopy(data, dataOffset, apdu, 5, dataLength);
		}
		if (ne > 0)
			apdu[apduLength - 1] = (byte) ne;
	}

	public CommandAPDU(int cla, int ins, int p1, int p2, int ne) {
		this(cla, ins, p1, p2, null, 0, 0, ne);
	}

	private void assertApdu(byte[] apdu) {
		if (apdu.length < 4)
			throw new IllegalArgumentException("command must contain at least 4 header bytes");
		if (apdu.length > 5) {
			int p3 = apdu[4] & 0xFF;
			if (p3 == 0)
				throw new IllegalArgumentException("P3 must not be zero");
			if (apdu.length < (5 + p3) || (apdu.length > (6 + p3)))
				throw new IllegalArgumentException("inconsistent command APDU");
		}
	}

	private void assertRange(int value, int minValue, int maxValue, String name) {
		if (value < minValue)
			throw new IllegalArgumentException(name + " out of range");
		if (value > maxValue)
			throw new IllegalArgumentException(name + " out of range");
	}

	private void assertRange(byte[] data, int dataOffset, int dataLength,
			String name) {
		if (data == null) {
			if (dataLength != 0)
				throw new IllegalArgumentException("inconsistent " + name + " parameters");
		} else {
			assertRange(dataLength, 0, 0xFF, name + " length");
			if (dataOffset < 0)
				throw new IllegalArgumentException(name	+ " offset must not be negative");
			if (data.length < dataOffset + dataLength)
				throw new IllegalArgumentException("inconsistent " + name + " parameters");
		}
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof CommandAPDU) {
			CommandAPDU _obj = (CommandAPDU) obj;
			return Arrays.equals(_obj.apdu, this.apdu);
		}
		return false;
	}

	public byte[] getBytes() {
		return apdu.clone();
	}

	public int getCLA() {
		return apdu[0] & 0xFF;
	}

	public byte[] getData() {
		int dataLength = getNc();
		byte[] data = new byte[dataLength];
		System.arraycopy(apdu, 5, data, 0, dataLength);
		return data;
	}

	public int getINS() {
		return apdu[1] & 0xFF;
	}

	public int getNc() {
		if (apdu.length > 5)
			return apdu[4] & 0xFF;
		return 0;
	}

	public int getNe() {
		if (apdu.length < 5)
			return 0;
		int p3 = apdu[4] & 0xFF;
		if (apdu.length == 5)
			return (p3 == 0) ? 256 : p3;
		if (apdu.length == (6 + p3)) {
			int le = apdu[apdu.length - 1] & 0xFF;
			return (le == 0) ? 256 : le;
		}
		return 0;
	}

	public int getP1() {
		return apdu[2] & 0xFF;
	}

	public int getP2() {
		return apdu[3] & 0xFF;
	}

	public int hashCode() {
		return Arrays.hashCode(apdu);
	}

	public String toString() {
		StringBuffer string = new StringBuffer();
		for (int i = 0; i < apdu.length; ++i)
			string.append(Integer.toHexString(0x0100 + (apdu[i] & 0x00FF)).substring(1));

		return string.toString();
	}

}