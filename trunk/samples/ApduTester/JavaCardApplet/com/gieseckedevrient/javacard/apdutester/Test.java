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
package com.gieseckedevrient.javacard.apdutester;

import javacard.framework.*;

public class Test extends Applet implements MultiSelectable {

    private byte[] outBuffer;
        
	public static void install(byte[] aArray, short sOffset, byte bLength) {
		new Test(aArray, sOffset, bLength);
	}

	private Test(byte[] aArray, short sOffset, byte bLength) {
	    	    
	    try
	    {
	        outBuffer = JCSystem.makeTransientByteArray( (short)256, JCSystem.CLEAR_ON_RESET );
	    }
	    catch( Exception e )
	    {
	        outBuffer = new byte[256];
	    }
	    
		register(aArray, (short) (sOffset + 1), aArray[sOffset]);
	}

	public boolean select(boolean appInstAlreadyActive)
	{
	   return true;
	}

	public void deselect(boolean appInstStillActive)
	{
	   return;
	}

	public void process(APDU apdu) throws ISOException {
 		if (selectingApplet())
			return;

		byte inBuffer[] = apdu.getBuffer();
		short echoOffset = (short)0;

		switch (inBuffer[ISO7816.OFFSET_INS]) {
		case (byte) 0x01:
			// CASE 1 command: no body, no return data
			apdu.setOutgoingAndSend((short) 0, (short) 0);
			break;
			
		case (byte) 0x02:
			// CASE 2 command: no body, return data
			echoOffset = (short) inBuffer[ISO7816.OFFSET_LC];
			for (short i=(short)0; i<echoOffset; i++)
				outBuffer[i] = (byte)(i & 0xFF);
			Util.arrayCopyNonAtomic(outBuffer, ( short ) 0, inBuffer, ( short ) 0, echoOffset );
			apdu.setOutgoingAndSend((short) 0, echoOffset);
			break;
		
		case (byte) 0x03:
			// CASE 3 command: command body, no return data
			apdu.setIncomingAndReceive();
			apdu.setOutgoingAndSend((short) 0, (short) 0);
			break;
		
		case (byte) 0x04:
			// CASE 4 command: command body, return data
			short bytesRead = apdu.setIncomingAndReceive();
		
			echoOffset = Util.arrayCopyNonAtomic(inBuffer, ISO7816.OFFSET_CDATA, outBuffer, echoOffset, bytesRead);
		
			Util.arrayCopyNonAtomic(outBuffer, ( short ) 0, inBuffer, ( short ) 0, echoOffset );
		
			apdu.setOutgoingAndSend( ( short ) 0, echoOffset );
			
			break;
		}
	}
}
