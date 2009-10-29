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

public final class ATR implements java.io.Serializable {

	private static final long serialVersionUID = -3342974588337944409L;

	private byte[] atr;

	public ATR(byte[] atr) {
		this.atr = atr.clone();
	}

	public boolean equals(Object obj) {
		if (obj instanceof ATR == true) {
			ATR _obj = (ATR) obj;
			return Arrays.equals(_obj.atr, this.atr);
		}

		return false;
	}

	public byte[] getBytes() {
		return atr.clone();
	}

	public byte[] getHistoricalBytes() {
		throw new UnsupportedOperationException("work in progress");
	}

	public int hashCode() {
		return Arrays.hashCode(atr);
	}

	public String toString() {
		StringBuffer string = new StringBuffer();
		for (int i = 0; i < atr.length; ++i)
			string.append(Integer.toHexString(0x0100 + (atr[i] & 0x00FF)).substring(1));

		return string.toString();
	}

}
