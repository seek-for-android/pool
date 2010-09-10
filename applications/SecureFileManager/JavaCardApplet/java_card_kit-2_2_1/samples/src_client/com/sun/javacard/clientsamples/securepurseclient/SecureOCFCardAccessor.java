/*
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @(#)SecureOCFCardAccessor.java	1.11 03/07/08
 */


package com.sun.javacard.clientsamples.securepurseclient;

import java.util.ResourceBundle;

public class SecureOCFCardAccessor extends com.sun.javacard.ocfrmiclientimpl.OCFCardAccessor {
    
    
    private static final byte INS_SELECT   = (byte)0xA4;
    private static final byte APDU_CMD_MASK = (byte)0xFC;
    private static final byte CLA_ISO7816 = (byte)0x00;
    
    private static final byte CLA_AUTH = (byte)0x80;
    private static final byte INS_AUTH = (byte)0x39;
    

    private static final boolean debug = false;
    
    private static final ResourceBundle msg
            = ResourceBundle.getBundle("com/sun/javacard/clientsamples/securepurseclient/MessagesBundle");

    
    
    
    /** Modifies the data and calls super.sendCommandAPDU to perform the actual send.
     * The data returned from the smart card is returned by this method within
     * the <CODE>ResponseAPDU</CODE> object
     * @param apdu The command APDU to be sent to the smart card
     * @return The response APDU returned from the card. <CODE>null</CODE> if none available.
     * @throws Exception if a communication error or timeout occurred
     */
    public byte[] exchangeAPDU( byte[] sendData ) throws java.io.IOException{
        
        final boolean select = isSelect(sendData);
        
        
        byte[] dataWithChecksum;
        
        if(select) {
            dataWithChecksum = new byte[ sendData.length ];
            System.arraycopy(sendData, 0, dataWithChecksum, 0, sendData.length);
        }
        else {
            dataWithChecksum = new byte[ sendData.length + 2];
            System.arraycopy(sendData, 0, dataWithChecksum, 0, sendData.length-1);
            dataWithChecksum[dataWithChecksum.length-1] = sendData[sendData.length-1];
            
            int Lc = dataWithChecksum[4];
            
            short csum = 0;
            for(short n = 5; n<Lc+5; ++n) {
                csum += sendData[n];
            }
            
            dataWithChecksum[Lc+5] = (byte) (csum>>8);
            dataWithChecksum[Lc+6] = (byte) (csum);
            
            dataWithChecksum[4] += 2;  // increase Lc
            
        }
        
        
        if(debug) {
            for(int i=0; i<dataWithChecksum.length;++i) {
                System.out.print(Integer.toHexString(dataWithChecksum[i] & 0x00FF) + " ");
            }
            System.out.println();
        }
        byte[] receiveDataWithChecksum =  super.exchangeAPDU( dataWithChecksum );
        
        byte[] receiveData;
        
        if(debug) {
            for(int i=0; i<receiveDataWithChecksum.length;++i) {
                System.out.print(Integer.toHexString(receiveDataWithChecksum[i] & 0x00FF) + " ");
            }
            System.out.println();
            System.out.println();
        }
        
        if(!select)  // verify the checksum
        {
            int Le = receiveDataWithChecksum.length - 2;   // 2 bytes reserved for SW
            
            if(debug) {
                System.out.println("Le=" + Le);
            }
            
            short csum1 = 0;
            for(short n = 2; n<Le; ++n) {
                csum1 += receiveDataWithChecksum[n];
            }
            
            short csum2 = (short)
            (
            (receiveDataWithChecksum[receiveDataWithChecksum.length - 2]<<8)
            |
            (receiveDataWithChecksum[receiveDataWithChecksum.length - 1] & 0x00FF)
            );
            
            if(debug) {
                
                System.out.println("csum1=" + csum1);
                System.out.println("csum2=" + csum2);
            }
            
            if(csum1 != csum2) throw new java.io.IOException(msg.getString("msg01"));
            
            receiveData = new byte[receiveDataWithChecksum.length-2];
            System.arraycopy(receiveDataWithChecksum, 0, receiveData, 0, receiveData.length);
            
        }
        else {
            receiveData = new byte[receiveDataWithChecksum.length];
            System.arraycopy(receiveDataWithChecksum, 0, receiveData, 0, receiveData.length);
            
        }
        
        if(debug) {
            for(int i=0; i<receiveData.length;++i) {
                System.out.print(Integer.toHexString(receiveData[i] & 0x00FF) + " ");
            }
            System.out.println();
            System.out.println();
        }
        
        return receiveData;
    }
    
    
    boolean authenticateUser( short ID ){
        
        byte[] externalAuthCommand = new byte[8];
        externalAuthCommand[0] = CLA_AUTH;
        externalAuthCommand[1] = INS_AUTH;
        
        externalAuthCommand[4] = 2;  // Lc
        externalAuthCommand[5] = (byte)(ID>>8);
        externalAuthCommand[6] = (byte)ID;
        externalAuthCommand[7] = 0x7F;
        
        
        try {
            byte[] response = this.exchangeAPDU( externalAuthCommand );
            if(response[0] != (byte)0x90 || response[1] != 0x00) return false;
            else return true;
        }catch (Exception e){
            return false;
        }
        
        
    }
    
    private boolean isSelect(byte[] buffer) {
        if(buffer.length < 2) return false;
        
        if((buffer[0]&APDU_CMD_MASK)==CLA_ISO7816 && buffer[1]==INS_SELECT) {
            if(debug) {
                System.out.println(msg.getString("msg02"));
            }
            return true;
        }
        else {
            return false;
        }
    }
    
    
}
