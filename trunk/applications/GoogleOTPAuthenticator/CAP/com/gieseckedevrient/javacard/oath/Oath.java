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

    private static final short ERROR_NO_ACCOUNT_SELECTED = (short) 0x69C0;
        private static final short ERROR_NO_SUCH_ACCOUNT = (short) 0x69C1;
    private static final short ERROR_NO_FREE_SLOT = (short) 0x69C2;

	private static final byte VERSION[] = {	0, 1 };

	static byte[] tmpBuffer;
	
	static short N_ACCOUNTS = (short) 20, activeAccounts = (short) 0;

	static OTPUser[] accounts;
	
	static OTPUser selectedAccount;
	
	static MessageDigest md;
	
	// remaining ipad
    static final byte ipad[] = { 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36 };

    // remaining opad
    static final byte opad[] = { 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C, 0x5C };

	public static void install(byte[] aArray, short sOffset, byte bLength) {
		new Oath(aArray, sOffset, bLength);
	}

	private Oath(byte[] aArray, short sOffset, byte bLength) {
		md = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
		
		accounts = new OTPUser[N_ACCOUNTS];

		register(aArray, (short) (sOffset + 1), aArray[sOffset]);
		tmpBuffer = JCSystem.makeTransientByteArray((short) 20, JCSystem.CLEAR_ON_DESELECT);
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
			ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
			
		switch (INS) {
		
		// Set counter value
		case 0x10: {
    	    if (selectedAccount == null)
		        ISOException.throwIt(ERROR_NO_ACCOUNT_SELECTED);
			if (apdu.setIncomingAndReceive() != 8)
				ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		    if (!isCounterGreaterOrEqual(buffer, ISO7816.OFFSET_CDATA, selectedAccount.counter))
		        ISOException.throwIt(ISO7816.SW_WRONG_DATA);
			Util.arrayCopy(buffer, (short) ISO7816.OFFSET_CDATA, selectedAccount.counter, (short) 0, 
			    (short) selectedAccount.counter.length);
			break;
		}

        // Get counter value
		case 0x11: {
		    if (selectedAccount == null)
		        ISOException.throwIt(ERROR_NO_ACCOUNT_SELECTED);
		    
			// GET current OATH counter value 
			Util.arrayCopy(selectedAccount.counter, (short) 0, buffer, (short) 0, 
			    (short) selectedAccount.counter.length);
			apdu.setOutgoingAndSend((short) 0, (short) (selectedAccount.counter.length));
			break;
		}

        // Personalise
		case 0x12: {
			// SET the number of digits for the OTP
			
			short digits = buffer[ISO7816.OFFSET_P1];
			boolean hotp = buffer[ISO7816.OFFSET_P2] != (byte) 0;
			short length = apdu.setIncomingAndReceive();
			
			// Personalize the shared secret (=seed)
			if (length < 22)
				ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
				
		    short offset = ISO7816.OFFSET_CDATA;
		    short mailLength = buffer[offset++];
			
            OTPUser account = findAccount(buffer, offset, mailLength);
            if (account == null) {
                account = createAccount(digits, hotp, buffer, length);
                if (account == null)
                    ISOException.throwIt(ERROR_NO_FREE_SLOT);
            } else {
                account.hotp = hotp;
                account.digits = digits;
                offset += mailLength;
                account.setSecret(buffer, offset, (short) 20);
            }
			
			break;
	    }

        // Generate OTP
		case 0x13: {
		    if (selectedAccount == null)
		        ISOException.throwIt(ERROR_NO_ACCOUNT_SELECTED);
				
			// RETURN the OATH OTP
			short length = selectedAccount.generateOTP(buffer, (short) 0);
			apdu.setOutgoingAndSend((short) 0, length);
			break;
	    }
			
			
	    // Reset
		case 0x14:
		    for (short i = 0; i < activeAccounts; i++)
		        accounts[i] = null;
		    selectedAccount = null;
		    activeAccounts = (short) 0;
		    break;
		    
		// Get HOTP/TOTP
		case 0x15: {
    	    if (selectedAccount == null)
		        ISOException.throwIt(ERROR_NO_ACCOUNT_SELECTED);
		
		    buffer[0] = (selectedAccount.hotp) ? (byte) 1 : (byte) 0;
		    apdu.setOutgoingAndSend((short) 0, (short) 1);
		    break;
		}

        // Select/Deselect account
		case 0x16: {
		    if (buffer[ISO7816.OFFSET_P1] == (byte) 0) {
		        // Select specified account
			    short length = apdu.setIncomingAndReceive();
			    if (length < 1)
			    	ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		        selectedAccount = findAccount(buffer, ISO7816.OFFSET_CDATA, length);
		        if (selectedAccount == null)
		            ISOException.throwIt(ERROR_NO_SUCH_ACCOUNT);
		        
		    } else if (buffer[ISO7816.OFFSET_P1] == (byte) 1) {
		        // Deselect
		        selectedAccount = null;
		    } else {
		    	ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
		    }
		    break;
		}
		    
        // Delete account
		case 0x17: {
			short length = apdu.setIncomingAndReceive();
			if (length < 1)
				ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
		    short index = findAccountNumber(buffer, ISO7816.OFFSET_CDATA, length);
		    if (index < (short) 0)
		        ISOException.throwIt(ERROR_NO_SUCH_ACCOUNT);
		    if (selectedAccount == accounts[index]) selectedAccount = null;
		    
		    for (short i = index; i < (short) (activeAccounts - (short) 1); i++)
		        accounts[i] = accounts[(short) (i + (short) 1)];
            accounts[activeAccounts--] = null;
		    break;
		}
		    
		// Get account info
		case 0x18: {
		    if (buffer[ISO7816.OFFSET_P1] == (byte) 0) {
		    
		        // Get number of accounts
		        buffer[0] = (byte) activeAccounts;
		        apdu.setOutgoingAndSend((short) 0, (short) 1);
		        
		    } else if (buffer[ISO7816.OFFSET_P1] == (byte) 1) {
		    
		        // Get account mail address
		        short index = Util.makeShort((byte) 0, buffer[ISO7816.OFFSET_P2]);
		        OTPUser account;
		        
		        if (index < 0 || index >= activeAccounts && index != 255)
    		        ISOException.throwIt(ERROR_NO_SUCH_ACCOUNT);
		        
		        if (index < (short) 255) {
		            account = accounts[index];
	    	        if (account == null)
        		        ISOException.throwIt(ERROR_NO_SUCH_ACCOUNT);
		        } else {
		            account = selectedAccount;
    		        if (account == null)
        		        ISOException.throwIt(ERROR_NO_ACCOUNT_SELECTED);
		        }
		            
		        Util.arrayCopy(account.mail, (short) 0, buffer, (short) 0, (short) account.mail.length);
		        apdu.setOutgoingAndSend((short) 0, (short) account.mail.length);
		        
		    } else {
		    	ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
		    }
		
		    break;
		}
		
		default:
			ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
			break;
		}
		
	}
	
	public static void clearBuffer() {
	    Util.arrayFillNonAtomic(tmpBuffer, (short) 0, (short) tmpBuffer.length, (byte) 0);
	}	
	
	private short findAccountNumber(byte[] buffer, short offset, short length) {
	    for (short i = 0; i < activeAccounts; i++) {
	        if (accounts[i].matchesMail(buffer, offset, length))
	            return i;
	    }
	    return (short) -1;
	}
	
	private OTPUser findAccount(byte[] buffer, short offset, short length) {
	    short i = findAccountNumber(buffer, offset, length);
	    if (i >= (short) 0)
	        return accounts[i];
	    else
	        return null;
	}
	
	private OTPUser createAccount(short digits, boolean hotp, byte[] buffer, short length) {
	    if (activeAccounts == N_ACCOUNTS)
	        return null;
	
	    OTPUser account = accounts[activeAccounts++] = new OTPUser(digits, hotp, buffer, length);
	    return account;
	}
	
	private boolean isCounterGreaterOrEqual(byte[] buffer, short offset, byte[] counter) {
	    for (short i = (short) 0; i < (short) counter.length; i++) {
	        short b1 = (short) ((short) buffer[(short) (offset + i)] & (short) 0xff);
	        short b2 = (short) ((short) counter[i] & (short) 0xff);
	        if (b1 > b2) return true;
	        if (b1 < b2) return false;
	    }
	    return true;
	}
	
}
