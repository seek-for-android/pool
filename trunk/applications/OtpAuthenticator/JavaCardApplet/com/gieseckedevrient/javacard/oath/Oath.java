/*
 * Copyright 2010 Giesecke & Devrient GmbH.
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
package com.gieseckedevrient.javacard.oath;

import javacard.framework.*;
import javacard.security.*;


/* WARNING
 * This applet is for demonstration and test purposes only.
 * Do not use in production environments!
 * Giesecke & Devrient is not liable for any damage.
 */
 
public class Oath extends Applet {

	private static final byte VERSION[] = {	0, 1 };

	// OATH counter
	private byte counter[] = { 0, 0, 0, 0, 0, 0, 0, 0 };

	// OATH digits
	private short digits = 6;

	// tmp buffer for computation	
	private byte[] tmpBuffer = new byte[20];

	// OATH shared secret xored with opad:
	private static byte sharedSecretOpad[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	// OATH shared secret xored with ipad:
	private static byte sharedSecretIpad[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	// remaining ipad
	private static final byte ipad[] = { 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36 };

	// remaining opad
	private static final byte opad[] = { 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C };

	// OATH OTP without truncation
	private static byte fullOtp[] = {0, 0, 0, 0};

	// personalized indicator
	private static boolean personalized = false;
	
	private static MessageDigest md;
	private static final int DIGITS_POWER[] = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000,100000000, 1000000000 };


	public static void install(byte[] aArray, short sOffset, byte bLength) {
		new Oath(aArray, sOffset, bLength);
	}

	private Oath(byte[] aArray, short sOffset, byte bLength) {
		md = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);

		register(aArray, (short) (sOffset + 1), aArray[sOffset]);
	}

	public void process(APDU apdu) throws ISOException {
		if (selectingApplet())
			return;

		byte[] buffer = apdu.getBuffer();
		short CLA = (short) (0xff & buffer[ISO7816.OFFSET_CLA]);
		short INS = (short) (0xff & buffer[ISO7816.OFFSET_INS]);
		
                
		if (buffer[ISO7816.OFFSET_CLA] == 0xd0 && INS == 0x10) {
			// get version - not supported over logical channels
			Util.arrayCopyNonAtomic(VERSION, (short) 0, buffer, (short) 0, (short) VERSION.length);
			apdu.setOutgoingAndSend((short) 0, (short) VERSION.length);
		}

		// this applet's proprietary class byte must be 0x00, otherwise an exception is thrown
		if ( (CLA & 0xFC) != 0x00 )
		{
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}


		switch (INS) {
		case 0x10:
			// SET new OATH counter value
			Util.arrayCopy(buffer, (short) ISO7816.OFFSET_CDATA, counter, (short) 0, (short) counter.length);

			break;

		case 0x11:
			// GET current OATH counter value 
			Util.arrayCopy(counter, (short) 0, buffer, (short) 0, (short) counter.length);
			apdu.setOutgoingAndSend((short) 0, (short) counter.length);
			
			break;

		case 0x12:
			// SET the number of digits for the OTP
			this.digits = (byte)buffer[ISO7816.OFFSET_P1]; 

			// Personalize the shared secret (=seed)
			if (apdu.setIncomingAndReceive() /* =Lc */ != 20)
				ISOException.throwIt(ISO7816.SW_WRONG_DATA);

			for (short i = 0; i < tmpBuffer.length; i++)
				tmpBuffer[i] = (byte) (buffer[(short) (ISO7816.OFFSET_CDATA + i)] ^ 0x36);
	
			Util.arrayCopy(tmpBuffer, (short) 0, sharedSecretIpad, (short) 0, (short) sharedSecretIpad.length);

			for (short i = 0; i < tmpBuffer.length; i++)
				tmpBuffer[i] = (byte) (buffer[(short) (ISO7816.OFFSET_CDATA + i)] ^ 0x5C);
	
			Util.arrayCopy(tmpBuffer, (short) 0, sharedSecretOpad, (short) 0, (short) sharedSecretOpad.length);

			// reset all values
			Util.arrayFillNonAtomic(counter, (short)0, (short)counter.length, (byte)0x00);
			Util.arrayFillNonAtomic(fullOtp, (short)0, (short)fullOtp.length, (byte)0x00);
			md = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
			
			personalized = true;
			
			break;

		case 0x13:
			if (personalized == false)
				ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
				
			// RETURN the OATH OTP
			generateOTP();
			
			int otp = ((0xff & fullOtp[0]) << 24) + ((0xff & fullOtp[1]) << 16) + ((0xff & fullOtp[2]) << 8) + (0xff & fullOtp[3]);
			int oathOTPTrunc = otp;

			if (this.digits < 10)
				oathOTPTrunc = otp % DIGITS_POWER[digits];
			
			// convert required digits to ASCII
			short digits = this.digits;
			while (digits != 0) {
				digits--;
				byte tmp = (byte) (otp % 10);
				buffer[digits] = (byte) (0x30 + tmp);
				otp = otp / 10;
			}

			apdu.setOutgoingAndSend((short) 0, this.digits);

			break;

		default:
			ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
		}
	}
	
	private void generateOTP()
	{
		// see to RFC2104
		md.update(sharedSecretIpad, (short) 0, (short) sharedSecretIpad.length);
		md.update(ipad, (short) 0, (short) (64 - 20));
		md.doFinal(counter, (short) 0, (short) counter.length, tmpBuffer, (short) 0);

		md.update(sharedSecretOpad, (short) 0, (short) sharedSecretOpad.length);
		md.update(opad, (short) 0, (short) (64 - 20));
		md.doFinal(tmpBuffer, (short) 0, (short) sharedSecretOpad.length, tmpBuffer, (short) 0);

		byte tmp = (byte) (0x0f & tmpBuffer[(short)(sharedSecretOpad.length - 1)]);
		tmpBuffer[tmp] = (byte) (tmpBuffer[tmp] & 0x7f);
		Util.arrayCopy(tmpBuffer, (short) tmp, fullOtp, (short) 0, (short) 4);

		incrementCounter();

		return;
	}
	
	private void incrementCounter()
	{
		// increment the counter byte arrary by one
		Util.arrayCopyNonAtomic(counter, (short) 0, tmpBuffer, (short) 0, (short) counter.length);
		short inc  = (short) (1 + (0xff & tmpBuffer[(short)(counter.length - 1)]));
		tmpBuffer[(short)(counter.length - 1)] = (byte) (0xff & inc);

		for (short i = (byte)(counter.length - 2); i >= 0; i = i--) {
			if ((inc & ((short) 0xff00)) != 0)
				inc = (short) (1 + (0xff & tmpBuffer[i]));
			else
				break;
			
			tmpBuffer[i] = (byte) (0xff & inc);
		}
	
		Util.arrayCopy(tmpBuffer, (short) 0, counter, (short) 0, (short) counter.length);

		return;
	}
	
}
