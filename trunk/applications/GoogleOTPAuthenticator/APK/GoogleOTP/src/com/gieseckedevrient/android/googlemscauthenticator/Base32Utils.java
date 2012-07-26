/*
 * Copyright 2010 Giesecke & Devrient GmbH.
 * Author: Manuel Eberl
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
 * 
 */

package com.gieseckedevrient.android.googlemscauthenticator;


public class Base32Utils {

	private static final byte[] charToBitsMap = new byte[255];
	
	static {
		for (int i = 0; i < 255; i++)
			charToBitsMap[i] = -1;
		for (int i = 0; i < 26; i++) {
			charToBitsMap[65 + i] = (byte) i;
			charToBitsMap[97 + i] = (byte) i;
		}
		for (int i = 0; i < 6; i++)
			charToBitsMap[50 + i] = (byte) (26 + i);
	}
	
	public static final byte[] base32ToByteArray(String str, int nBytes) {
		char[] chars = str.toCharArray();
		byte[] result = new byte[nBytes];
		
		int bitBuffer = 0, nBitsInBuffer = 0;
		int charIndex = 0;
		
		for (int byteIndex = 0; byteIndex < nBytes && charIndex < chars.length; byteIndex++) {
			
			while (nBitsInBuffer < 8 && charIndex < chars.length) {
				byte newBits = charToBitsMap[chars[charIndex++]];
				bitBuffer <<= 5;
				bitBuffer |= newBits;
				nBitsInBuffer += 5;
			}
			
			nBitsInBuffer -= 8;
			result[byteIndex] = (byte) (bitBuffer >> nBitsInBuffer);
		}
		
		return result;
		
	}
	
	public static byte[] base32ToByteArray(String str) {
		return base32ToByteArray(str, str.length() * 5 / 8);
	}

}
