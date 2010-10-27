package com.gieseckedevrient.javacard.hellosmartcard;

import javacard.framework.*;

public class HelloSmartcard extends Applet implements MultiSelectable {
	short counter = 0x00;
	
	public static void install(byte[] aArray, short sOffset, byte bLength) {
		new HelloSmartcard(aArray, sOffset, bLength);
	}

	private HelloSmartcard(byte[] aArray, short sOffset, byte bLength) {
		register(aArray, (short) (sOffset + 1), aArray[sOffset]);
	}

	//implementation for select() and deselect() from MultiSelectable interface
	
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

		byte[] buffer = apdu.getBuffer();
	       
        if ( ((byte)(buffer[ISO7816.OFFSET_CLA] & (byte)0xFC)) != (byte)0x90 )
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
		}

		switch (buffer[ISO7816.OFFSET_INS]) {
		case (byte) 0x10:
		buffer[0] = (byte) 'H';
		buffer[1] = (byte) 'e';
		buffer[2] = (byte) 'l';
		buffer[3] = (byte) 'l';
		buffer[4] = (byte) 'o';
		buffer[5] = (byte) ',';
		buffer[6] = (byte) ' ';
		buffer[7] = (byte) 'S';
		buffer[8] = (byte) 'm';
		buffer[9] = (byte) 'a';
		buffer[10] = (byte) 'r';
		buffer[11] = (byte) 't';
		buffer[12] = (byte) 'c';
		buffer[13] = (byte) 'a';
		buffer[14] = (byte) 'r';
		buffer[15] = (byte) 'd';
		buffer[16] = (byte) ':';
		buffer[17] = (byte) ' ';
		// increase counter by one and return counter value
		buffer[18] = (byte) (counter++ % 10 + 0x30);
			apdu.setOutgoingAndSend((short) 0, (short) 19);
			break;
		default:
			ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
		}
	}
}
