package com.gieseckedevrient.javacard.allowall;

import javacard.framework.*;

public class AllowAll extends Applet implements MultiSelectable
{    
    static final byte INS_GET_DATA            = (byte)  0xCA;
    static final short TAG_CMD_GET_NEXT       = (short) 0xFF60;
    static final short TAG_CMD_GET_SPECIFIC   = (short) 0xFF50;
    static final short TAG_CMD_GET_ALL        = (short) 0xFF40;
    static final short TAG_CMD_GET_REFRESH    = (short) 0xDF20;
    

    private final static byte[] RESPONSE_GET_REFRESH = { (byte)0xDF, (byte)0x20, (byte)0x08, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08 };
        
    private final static byte[] RESPONSE_GET_SPECIFIC = { (byte)0xFF, (byte)0x50, (byte)0x08, (byte) 0xE3, (byte) 0x06, (byte)0xD0, (byte)0x01, (byte)0x01, (byte)0xD1, (byte)0x01, (byte)0x01 };
    
    private final static byte[] RESPONSE_GET_ALL = {(byte)0xFF, (byte)0x40,(byte)0x10, (byte)0xE2,(byte)0x0E, (byte)0xE1,(byte)0x04, (byte)0x4F,(byte)0x00, (byte)0xC1, (byte)0x00, (byte)0xE3, (byte)0x06, (byte)0xD0, (byte)0x01, (byte)0x01, (byte)0xD1, (byte)0x01, (byte)0x01 };
    

    public static void install(byte[] abArray, short sOffset, byte bLength)
    {        
        (new AllowAll()).register( abArray, (short) (sOffset + 1), abArray[sOffset] );
    }
    
	public boolean select(boolean appInstAlreadyActive)
	{
	   return true;
	}

	public void deselect(boolean appInstStillActive)
	{
	   return;
	}
    
    public void process(APDU oAPDU) throws ISOException
    {
        short sSW1SW2 = ISO7816.SW_NO_ERROR;
        short sOutLength = (short)0;
        
        byte[] abData = oAPDU.getBuffer();
        
        byte bINS = abData[ ISO7816.OFFSET_INS ];
        
        short sMode = Util.getShort( abData, ISO7816.OFFSET_P1 );
        
        if( selectingApplet() == true )
        {
            return;
        }

        try
        {
            if( bINS != INS_GET_DATA )
                ISOException.throwIt( ISO7816.SW_INS_NOT_SUPPORTED );
            
            if( sMode == TAG_CMD_GET_ALL )
            {
                sOutLength = Util.arrayCopyNonAtomic( RESPONSE_GET_ALL,
                                                      (short)0,
                                                      abData,
                                                      (short)0,
                                                      (short)RESPONSE_GET_ALL.length
                                                    );
            }
            else if( sMode == TAG_CMD_GET_SPECIFIC )
            {
                oAPDU.setIncomingAndReceive();
                
                sOutLength = Util.arrayCopyNonAtomic( RESPONSE_GET_SPECIFIC,
                                                      (short)0,
                                                      abData,
                                                      (short)0,
                                                      (short)RESPONSE_GET_SPECIFIC.length
                                                    );
            }
            else if( sMode == TAG_CMD_GET_NEXT )
            {
                ISOException.throwIt( ISO7816.SW_CONDITIONS_NOT_SATISFIED );
            }
            else if( sMode == TAG_CMD_GET_REFRESH )
            {
                sOutLength = Util.arrayCopyNonAtomic( RESPONSE_GET_REFRESH,
                                                      (short)0,
                                                      abData,
                                                      (short)0,
                                                      (short)RESPONSE_GET_REFRESH.length
                                                    );
            }
            else
            {
                ISOException.throwIt( ISO7816.SW_WRONG_P1P2 );
            }
        }
        catch( ISOException e )
        {
            sSW1SW2 = e.getReason();
        }
        catch( Exception e )
        {
            sSW1SW2 = ISO7816.SW_UNKNOWN;
        }
        
        if( sSW1SW2 != ISO7816.SW_NO_ERROR )
        {
            ISOException.throwIt( sSW1SW2 );
        }
        
        if( sOutLength > (short)0 )
        {
            oAPDU.setOutgoingAndSend( (short)0, sOutLength );
        }
    }
}
