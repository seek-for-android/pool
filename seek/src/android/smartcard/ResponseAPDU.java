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

public final class ResponseAPDU implements java.io.Serializable {

	private static final long serialVersionUID = 2736124087921765365L;

	private byte[] apdu;

	public ResponseAPDU(byte[] apdu) {
		this.apdu = apdu.clone();
	}

	public boolean equals(Object obj) {
		if (obj instanceof ResponseAPDU == true) {
			ResponseAPDU _obj = (ResponseAPDU) obj;
			return Arrays.equals(_obj.apdu, this.apdu);
		}

		return false;
	}

	public byte[] getBytes() {
		return apdu.clone();
	}

	public byte[] getData() {
		byte[] data = new byte[apdu.length - 2];
		System.arraycopy(apdu, 0, data, 0, data.length);
		return data;
	}

	public int getNr() {
		return apdu.length - 2;
	}

	public int getSW() {
		return (getSW1() << 8) + getSW2();
	}

	public int getSW1() {
		return apdu[apdu.length - 2] & 0xff;
	}

	public int getSW2() {
		return apdu[apdu.length - 1] & 0xff;
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
