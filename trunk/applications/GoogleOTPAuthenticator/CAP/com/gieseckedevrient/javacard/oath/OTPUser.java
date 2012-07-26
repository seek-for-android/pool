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

public class OTPUser {

  public byte[] mail, secretIpad = new byte[20], secretOpad = new byte[20], counter = {0, 0, 0, 0, 0, 0, 0, 0};
  public short digits;
  public boolean hotp;
  
  public OTPUser(short digits, boolean hotp, byte[] buffer, short length) {
    this.digits = digits;
    this.hotp = hotp;
  
    short offset = ISO7816.OFFSET_CDATA;
    byte mailLength = buffer[offset++];
    if ((short) (mailLength + (short) 21) > length)
        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    
    mail = new byte[mailLength];
    Util.arrayCopy(buffer, offset, mail, (short) 0, mailLength);
    offset += mailLength;
    
    setSecret(buffer, offset, length);
  }
  
  public void setSecret(byte[] buffer, short offset, short length) {
    byte[] tmpBuffer = Oath.tmpBuffer;
    for (short i = (short) 0; i < (short) 20; i++)
        tmpBuffer[i] = (byte) (buffer[offset++] ^ (byte) 0x36);
    Util.arrayCopy(tmpBuffer, (short) 0, secretIpad, (short) 0, (short) 20);
    
    offset -= (short) 20;

    for (short i = (short) 0; i < (short) 20; i++)
        tmpBuffer[i] = (byte) (buffer[offset++] ^ (byte) 0x5C);
    Util.arrayCopy(tmpBuffer, (short) 0, secretOpad, (short) 0, (short) 20);    
  }

  public short generateOTP(byte[] buffer, short offset) {
    int otp = generateOTP();
    
    for (short i = (short) ((short) digits - (short) 1); i >= 0; i--) {
      buffer[(short) (offset + i)] = (byte) ((byte) (otp % (short) 10) + (byte) '0');
      otp /= (short) 10;
    }
    
    return digits;
  }
  
  private int generateOTP() {
    MessageDigest md = Oath.md;
    byte[] tmpBuffer = Oath.tmpBuffer;
  	
	// see RFC2104
	md.update(secretIpad, (short) 0, (short) secretIpad.length);
	md.update(Oath.ipad, (short) 0, (short) (64 - 20));
	md.doFinal(counter, (short) 0, (short) counter.length, tmpBuffer, (short) 0);

	md.update(secretOpad, (short) 0, (short) secretOpad.length);
	md.update(Oath.opad, (short) 0, (short) (64 - 20));
	md.doFinal(tmpBuffer, (short) 0, (short) secretOpad.length, tmpBuffer, (short) 0);

	byte tmp = (byte) (0x0f & tmpBuffer[(short) ((short) secretOpad.length - (short) 1)]);

    if (this.hotp) incrementCounter();

	int result = ((0x7f & tmpBuffer[tmp]) << 24) | ((0xff & tmpBuffer[(short) (tmp+(short) 1)]) << 16) | 
	    ((0xff & tmpBuffer[(short) (tmp+(short) 2)]) << 8) | (0xff & tmpBuffer[(short) (tmp+(short) 3)]);
	Oath.clearBuffer();
	return result;
  }
  
  private void incrementCounter() {
	// increment the counter byte array by one
	byte[] tmpBuffer = Oath.tmpBuffer;
	Util.arrayCopyNonAtomic(counter, (short) 0, tmpBuffer, (short) 0, (short) counter.length);

    boolean carry = true;
    for (short i = (short) ((short) counter.length - (short) 1); i >= 0 && carry; i--)
      carry = (++tmpBuffer[i] == (byte) 0);

    Util.arrayCopy(tmpBuffer, (short) 0, counter, (short) 0, (short) counter.length);
  }
  
  public boolean matchesMail(byte[] buffer, short offset, short length) {
    if (length != mail.length) return false;
    return Util.arrayCompare(buffer, offset, mail, (short) 0, length) == (byte) 0;
  }

}
