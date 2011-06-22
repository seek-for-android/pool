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

package android.smartcard.cts;

import java.util.ArrayList;

public class Util {

	/**
	 * converts an array of bytes into a hex string.
	 * 
	 * @param bytes          array of bytes to convert to hex string
	 * @return               hex string representation of input array
	 */
	public static String bytesToHexString(byte[] bytes) {
		if(bytes==null) return "null";
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02X ", b & 0xFF));
		}
		return sb.toString();
	} // bytesToHexString

	/**
	 * converts up to a max. number of bytes of an array of bytes into a hex string.
	 * <p>
	 * If n<5 at least 5 bytes are converted.<br>
	 * If n>=5 first n-2 and last 2 bytes are converted.
	 * 
	 * @param bytes          array of bytes to convert to hex string
	 * @param n              max. number of bytes in hex string
	 * @return               hex string representation of input array
	 */
	public static String bytesToMaxHexString(byte[] bytes, int n) {
		if(bytes==null) return "null";
		if (n < 5 || bytes.length <= n) return bytesToHexString(bytes);
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<n-2; i++)
			sb.append(String.format("%02X ", 0xff & bytes[i]));
		sb.append(String.format("... %02X %02X", bytes[bytes.length-2] & 0xFF, bytes[bytes.length-1] & 0xFF));
		return sb.toString();
	} // bytesToMaxHexString

	/**
	 * converts up to 10 bytes of an array of bytes into a hex string.
	 * <p>
	 * At least first 8 bytes and last 2 bytes are converted.
	 * 
	 * @param bytes          array of bytes to convert to hex string
	 * @return               hex string representation of input array
	 */
	public static String bytesToMaxHexString(byte[] bytes) {
		return bytesToMaxHexString(bytes, 10);
	}

	public static byte[] hexStringToBytes(String hexString) {
		byte[] hsb=hexString.getBytes();
		byte[] bytes=null;
		int j,n=0;
		for (j=0; j<2; j++) {
			int i=0;
			byte b=0;
			if(j==1) bytes=new byte[n];
			n=0;
			for (byte c : hsb) {
				boolean isHexDigit=(c>=48 && c<=57) || (c>=65 && c<=70) || (c>=97 && c<=102);
				if (isHexDigit) {
					c=(byte)((c&0xDF)-48); 
					if(c>9)c-=7;
					b=(byte)((b<<4)+(0x0f&c));
					i++;
				}
				if ((i>0 && !isHexDigit) || i>1) {
					if(j==1)bytes[n]=b;
					b=0;
					n++;
					i=0;
				}
			}	
		}
		return bytes;
	} // hexStringToBytes

	public static byte[] toBytes(Object ...args) {
		ArrayList<Byte> a = new ArrayList<Byte>();

		for (Object arg: args) {
			if      (arg instanceof Byte)      a.add(((Byte)arg).byteValue());
			else if (arg instanceof Short)     a.add(((Short)arg).byteValue());
			else if (arg instanceof Integer)   a.add(((Integer)arg).byteValue());
			else if (arg instanceof Boolean)   a.add(((Boolean)arg).booleanValue()?(byte)1:(byte)0);
			else if (arg instanceof byte[])    for(byte e:    (byte[])   arg) a.add(e);
			else if (arg instanceof short[])   for(short e:   (short[])  arg) a.add((byte)e);
			else if (arg instanceof int[])     for(int e: (int[])arg) a.add((byte)e);
			else if (arg instanceof boolean[]) for(boolean e: (boolean[])arg) a.add(e?(byte)1:(byte)0);
			else if (arg instanceof String)    for(byte b: hexStringToBytes((String)arg))a.add(b);
			//else throw new Exception("argument type not supported for method toBytes(Object ...args)");
		}

		byte[] aob=new byte[a.size()];
		for (int i=0; i<aob.length; i++)aob[i]=a.get(i).byteValue();
		return aob;
	} // toBytes

	/**
	 * returns the response the APDUTester applet would give as answer 
	 * to the given command.
	 * <p>
	 * This method should be very similar to the 
	 * byte[] process(byte[] command)
	 * method of the test applet.
	 * 
	 * @param command        C-APDU
	 * @return               expected R-APDU
	 */
	public static byte[] expectedResponseOfTestApplet(byte[] command) {
		byte ins = command[1];
		byte[] response;
		
		int lc = 0;
		int le = 0;

		switch (ins) {
		case (byte)0x01:
			// CASE 1 command: no body, no return data
		    break;

		case (byte)0x02:
			// CASE 2 command: no body, return data
			le = 0xff & command[4];
	    	break;

		case (byte) 0x03:
			// CASE 3 command: command body, no return data
			lc = 0xff & command[4];
    		break;

		case (byte) 0x04:
			// CASE 4 command: command body, return data
			lc = 0xff & command[4];
			le = 0xff & command[5+lc];
			break;
			
		default:
			return new byte[]{(byte)0x6D, (byte)0x00};

		}
		for (int i=0; i<lc; i++)
			if (command[5+i] != (byte)i)
				return new byte[]{(byte)0x6F, (byte)0xFF};
		response = new byte[le+2];
		for (int i=0; i<le; i++)
			response[i] = (byte)i;
		response[le  ] = (byte)0x90;
		response[le+1] = (byte)0x00;
		return response;
	} // expectedResponseOfTestApplet

} // class
