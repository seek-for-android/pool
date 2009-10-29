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
		this.apdu = apdu.clone();
	}

	public CommandAPDU(byte[] apdu, int apduOffset, int apduLength) {
		this.apdu = new byte[apduLength];
		System.arraycopy(apdu, apduOffset, this.apdu, 0, apduLength);
	}

	public CommandAPDU(ByteBuffer apdu) {
		this.apdu = new byte[apdu.remaining()];
		apdu.get(this.apdu);
	}

	public CommandAPDU(int cla, int ins, int p1, int p2) {
		throw new UnsupportedOperationException("work in progress");
	}

	public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data) {
		throw new UnsupportedOperationException("work in progress");
	}

	public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data, int ne) {
		throw new UnsupportedOperationException("work in progress");
	}

	public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data, int dataOffset, int dataLength) {
		throw new UnsupportedOperationException("work in progress");
	}

	public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data, int dataOffset, int dataLength, int ne) {
		throw new UnsupportedOperationException("work in progress");
	}

	public CommandAPDU(int cla, int ins, int p1, int p2, int ne) {
		throw new UnsupportedOperationException("work in progress");
	}

	public boolean equals(Object obj) {
		if (obj instanceof CommandAPDU == true) {
			CommandAPDU _obj = (CommandAPDU) obj;
			return Arrays.equals(_obj.apdu, this.apdu);
		}

		return false;
	}

	public byte[] getBytes() {
		return apdu.clone();
	}

	public int getCLA() {
		return apdu[0] & 0xff;
	}

	public byte[] getData() {
		byte[] data = new byte[apdu.length - 5];
		System.arraycopy(apdu, 5, data, 0, apdu.length - 5);
		return data;
	}

	public int getINS() {
		return apdu[1] & 0xff;
	}

	public int getNc() {
		throw new UnsupportedOperationException("work in progress");
	}

	public int getNe() {
		throw new UnsupportedOperationException("work in progress");
	}

	public int getP1() {
		return apdu[2] & 0xff;
	}

	public int getP2() {
		return apdu[3] & 0xff;
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