/**
 * Filemanager-Applett
 */
package com.gieseckedevrient.javacard.filemanager;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

/**
 * @author neuerbuj
 */
public class FileManager extends Applet implements MultiSelectable
{

	//implementation for select() and deselect()
	//from MultiSelectable interface
	public boolean select(boolean appInstAlreadyActive)
	{
	return true;
	}

	public void deselect(boolean appInstStillActive)
	{
	return;
	}

	/**
	 * APDU-specific definitions
	 */	
    // Instruction byte for "VerifyGlobal"
    final static byte bTAG_INS_VERIFY_GLOBAL = (byte) 0x10;

    // Instruction byte for "CreateKey"
    final static byte bTAG_INS_CREATE_KEY = (byte) 0x20;
    
    // Instruction byte for "VerifyKey"
    final static byte bTAG_INS_VERIFY_KEY = (byte) 0x30;
    
    // Instruction byte for "DeleteKey"
    final static byte bTAG_INS_DELETE_KEY = (byte) 0x40;
    
    final static byte bTAG_P1_00 = (byte) 0x00;
    
    // number of bytes in the command VerifyGlobal
    final static byte VERIFY_GLOBAL_APDU_DATA_LENGTH = 4; // global PIN is 4 bytes long

    // number of bytes and offsets in the commands CreateKey, VerifyKey, DeleteKey
    // <len hash> <hash bytes> <len password> <password>
    final static byte APDU_DATA_LENGTH = 16; // passed data is 16 bytes long
    final static byte APDU_HASH_OFFSET = 1; // hash data begins after 1 byte <len hash>    

    // Instruction byte for "select"
    final static byte bTAG_INS_SELECT = (byte) 0xA4;
    
    // Instruction byte for "getversion"
    final static byte bTAG_INS_GETVERSION = (byte) 0x76;
    
    final static short SW_COMMAND_NOT_ALLOWED = 0x6900; // according to ISO7816

    /**
     * variable-specific defines
     */    
    // RAM buffer
    final static short sRAMBufferSize = (short) 256;
    
    final static short sInBufferSize = (short)(32+1);
    final static short sResultBufferSize = (short)(32+1);    
    
    // NVM buffer
    final static short sNVMBufferSize = (short) 256;

    /**
     * global variables
     */
    // necessary to be an "array" for clear on reset
    private static short[] sVerifyGlobalPinChecked; // only has one element for verifyChecked true or false
    private static short sVerifyGlobalPinRetryCounter; // 16 bits in NVM which holds the retry counter
    
    private static byte[] s_abRAMBuffer;
    private static byte[] m_inBuffer;
    private static byte[] m_resultBuffer;    
    
    // the following are arrays that -in conjunction with each other- have to be seen as a "table"
    // the first "column" is the use indicator which keeps track of the state of a row (used or unused)
    // the 2., 3., 4. "columns" are following
    private static byte[] s_abNVMBufferElementUseIndicator; // holds 0x00 if unused or 0xFF if used
    private static byte[] s_abNVMBufferHashBytes; // holds hash bytes
    private static byte[] s_abNVMBufferPasswordBytes; // holds password bytes
    private static byte[] s_abNVMCipherKeyBytes; // holds cipher key bytes
    
    // hard-coded to '1' '2' '3' '4'
    private static byte[] globalPin = { (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34};
    
    /**
     * general definitions
     */
    private static final byte RETURN_VALUE_PASS = (byte)0;
    private static final byte RETURN_VALUE_FAIL = (byte)0xFF;
    
    private static final byte IS_EQUAL = (byte)0;
    private static final byte IS_GREATER_THAN = (byte)(1);
    private static final byte IS_LESS_THAN = (byte)(-1);
    
    private static final byte ELEMENT_UNUSED = (byte)0;
    private static final byte ELEMENT_USED = (byte)0xFF;
    
    // 3 tries to authenticate before retry counter expires
    private static final short RESET_VERIFY_GLOBAL_PIN_RETRY_COUNTER = (short)3;
    private static final short VERIFY_GLOBAL_PIN_SUCCESSFULLY_CHECKED = (short)0xA5A5;
    
    private static final short NUMBER_OF_ELEMENTS_IN_TABLE = (short)256;
    
    private static final short NUMBER_OF_BYTES_FOR_HASH_IN_TABLE = (short)(16+1);
    private static final short NUMBER_OF_BYTES_FOR_PASSWORD_IN_TABLE = (short)(16+1);
    private static final short NUMBER_OF_BYTES_FOR_CIPHER_KEY_IN_TABLE = (short)(32+1);
    
    // Major-version byte for "getversion"
    final static byte bVERSION_MAJOR = (byte) 0x01;
    // Minor-version byte for "getversion"
    final static byte bVERSION_MINOR = (byte) 0x00; //next: 01

    /*
    // "NORMAL"
    private static byte receiptTypeName_Normal[] = {'N','O','R','M','A','L'};
    */
    
    RandomData rnd;
    private final byte[] abRandomDES_KEY_ARRAYInRAM;    

    private DESKey myDESKey;
    
    private Cipher myCipher;

    /**
     * Constructor of CryptoApplication.
     * </p>
     * The constructor creates all buffers and registers the Applet for:
     * <ul>
     * </ul>
     * </p>
     */
    public FileManager()
    {
    	// allocate 256 bytes of RAM memory (available from beginning of session till end of session)
        s_abRAMBuffer = JCSystem.makeTransientByteArray(sRAMBufferSize,
                JCSystem.CLEAR_ON_RESET);

        // allocate RAM to hold the Bytes to be encrypted
        m_inBuffer = JCSystem.makeTransientByteArray(sInBufferSize,
                JCSystem.CLEAR_ON_RESET);        
        
        // allocate RAM to hold the calculated Cipher Key
        m_resultBuffer = JCSystem.makeTransientByteArray(sResultBufferSize,
                JCSystem.CLEAR_ON_RESET);

        abRandomDES_KEY_ARRAYInRAM = JCSystem.makeTransientByteArray((short)32,
                JCSystem.CLEAR_ON_RESET);
        
        // allocate bytes of EEPROM memory
        s_abNVMBufferElementUseIndicator = new byte[NUMBER_OF_ELEMENTS_IN_TABLE];
        s_abNVMBufferHashBytes = new byte[NUMBER_OF_ELEMENTS_IN_TABLE*NUMBER_OF_BYTES_FOR_HASH_IN_TABLE];
        s_abNVMBufferPasswordBytes = new byte[NUMBER_OF_ELEMENTS_IN_TABLE*NUMBER_OF_BYTES_FOR_PASSWORD_IN_TABLE];
        s_abNVMCipherKeyBytes = new byte[NUMBER_OF_ELEMENTS_IN_TABLE*NUMBER_OF_BYTES_FOR_CIPHER_KEY_IN_TABLE];
        
        // 3 tries to authenticate
        sVerifyGlobalPinRetryCounter = RESET_VERIFY_GLOBAL_PIN_RETRY_COUNTER;

        // global RAM variable to indicate if VerifyGlobal has been successfully checked or not
        sVerifyGlobalPinChecked = JCSystem.makeTransientShortArray((short)1, JCSystem.CLEAR_ON_RESET);
        
        rnd = RandomData.getInstance(RandomData.ALG_PSEUDO_RANDOM);

        // DES key instance ...
        myDESKey = (DESKey) KeyBuilder.buildKey(
                KeyBuilder.TYPE_DES,
                KeyBuilder.LENGTH_DES, false); // 64 bit

        /*
        my3DESKey = (DESKey) KeyBuilder.buildKey(
                KeyBuilder.TYPE_DES,
                KeyBuilder.LENGTH_DES3_3KEY, false); // 192 bit
		*/
        
        // MyCipher instance
        myCipher = Cipher.getInstance(
                Cipher.ALG_DES_CBC_NOPAD, false);
	}

    /**
     * This method must be implemented by Applets (it is declared as abstract in
     * class Applet).
     * </p>
     * When the applet is selcected and the apdu 00 76 00 00 02 is sent to the
     * applet, it returns two bytes containing the major and minor version of
     * this applet (G+D internal applet version).
     * </p>
     *
     * @param apdu
     *            APDU
     */
    public void process(APDU apdu)
    {
        // local reference to apdu buffer
        byte[] apdu_buffer = apdu.getBuffer();
        byte cla = apdu_buffer[ISO7816.OFFSET_CLA];
        byte bINS = apdu_buffer[ISO7816.OFFSET_INS];
        byte bP1 = apdu_buffer[ISO7816.OFFSET_P1];
        byte bP2 = apdu_buffer[ISO7816.OFFSET_P2];
        short sLe = (byte)0;
        short sLc = (short) (apdu_buffer[ISO7816.OFFSET_LC] & 0xFF);
        short apdu_len = (short)0;
        short sLen = (short) 0;
        short sReasonCode = 0x6F00;
        
        /********************************************************************************************************************************************/
        /********************************************************************************************************************************************/
        /* NORMAL FLOW STARTS HERE WITH CLASS BYTE 0 AND THEN THE DIFFERENT COMMANDS ARE HANDLED 													*/
        /********************************************************************************************************************************************/
        /********************************************************************************************************************************************/
        // the main block should only be executed if the Terminal was authenticated before
        // therefore, the applet checks a passed PIN with its known reference
        // if these 2 strings do not match, an error counter which is incremented results in blocking of the applet
        // i.e. before the comparison, the applet is "blocked" and then the compare is done
        // this also means that if these "authenticate" command is issued very often, it might be necessary
        // to reserve a byte in the high update activity region
        
        // select command should be passed through        
        if ( bTAG_INS_SELECT == bINS )
        {
        	return;
        }

	if (selectingApplet())
			return;
        
        // this applet's proprietary class byte must be 0x90, otherwise an exception is thrown
        if ( ((byte)(cla & (byte)0xFC)) != (byte)0x90 )
        {
        	sReasonCode = ISO7816.SW_CLA_NOT_SUPPORTED;
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

	
        if ( VERIFY_GLOBAL_PIN_SUCCESSFULLY_CHECKED == sVerifyGlobalPinChecked[0] )
        {
        	/********************************************************************************************************************************/
        	/********************************************************************************************************************************/
        	// START SUCCESSFUL HASH CHECK/AUTHENTICATE
        	
            try
            {
                // check if instruction is for us...
                switch (bINS)
                {
                	case bTAG_INS_VERIFY_GLOBAL:
                	{
                		if ( bP1 == bTAG_P1_00 )
 			       				{
        				// -------------------------HANDLE APDU
                		apdu_len = apdu.setIncomingAndReceive();
                        
                        if (sLc != apdu_len)
                        {
                        	sReasonCode = ISO7816.SW_WRONG_LENGTH;
                            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                        }
                        
                        sLe = apdu.setOutgoing();
                        if (sLe != (short)0x0000)
                        {
                        	sReasonCode = ISO7816.SW_WRONG_LENGTH;
                            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                        }
                        
                        if (apdu_len == VERIFY_GLOBAL_APDU_DATA_LENGTH)                  	
                        {
                            // make plausibility check that the retry counter is between 1 and MAX_NMBR_OF TRIES
        	            	if ( ( (short)1 <= sVerifyGlobalPinRetryCounter ) && (RESET_VERIFY_GLOBAL_PIN_RETRY_COUNTER >= sVerifyGlobalPinRetryCounter) )
        	                {                    
        	            		sVerifyGlobalPinRetryCounter --; // decrement before check!!
                            	
                            	// check of passed HASH value against the stored hash value (in EEPROM)
                            	if ( 0 == Util.arrayCompare(apdu_buffer, (short)(ISO7816.OFFSET_CDATA), globalPin, (short) 0, VERIFY_GLOBAL_APDU_DATA_LENGTH))
                            	{
                            		sVerifyGlobalPinChecked[0] = VERIFY_GLOBAL_PIN_SUCCESSFULLY_CHECKED;
                            		sVerifyGlobalPinRetryCounter = RESET_VERIFY_GLOBAL_PIN_RETRY_COUNTER; // reset retry counter
                            		return; // 90 00
                            	}
                            	
                            	// -- we land here if the hash was entered incorrectly! --
                            	sReasonCode = ISO7816.SW_CONDITIONS_NOT_SATISFIED;
                            	ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
                            }
        	            	else
        	            	{
        	            		// blocked forever!! (retry counter has expired)
        	            		sReasonCode = ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED;
        	            		ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        	            	}
                        }
                        else
                        {
                        	sReasonCode = ISO7816.SW_DATA_INVALID;
                        	ISOException.throwIt(ISO7816.SW_DATA_INVALID);                    	
                        }
                		}
 
									}
                	
                	/******************* CREATE KEY *******************/
	                case bTAG_INS_CREATE_KEY: // 20 00 - Case 4
	            	{
	            		if ( bTAG_P1_00 == bP1 )
	            		{
	            			// -------------------------HANDLE APDU
	            			// receive the data which is transmitted to the Applet via APDU
	            			apdu_len = apdu.setIncomingAndReceive();
	            			
	            			// if LC bytes is different than apdu data length
                            if ( sLc != apdu_len )
                            {
                            	sReasonCode = ISO7816.SW_WRONG_LENGTH;
                                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                            }
                            
                            // store APDU data into RAM because apdu_buffer is not available in the subfunction
                    		Util.arrayCopy(apdu_buffer, (short)(ISO7816.OFFSET_CDATA), s_abRAMBuffer, (short)0, sLc);
                    		
                    		// do the actual work                            
                    		if ( RETURN_VALUE_PASS != handleCommandsCreateVerifyDelete(bTAG_INS_CREATE_KEY, bP2) )
                    		{
                            	sReasonCode = ISO7816.SW_DATA_INVALID;
                                ISOException.throwIt(ISO7816.SW_DATA_INVALID);                    			
                    		}

                    		// ensure that the command requested correct number of answer bytes
                            sLe = apdu.setOutgoing();
                            if ( (sLe > 0) && (sLe < m_resultBuffer[0]) )
                            {
                            	sReasonCode = ISO7816.SW_WRONG_LENGTH;
                                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                            }
                            
                            // put the result in the apdu buffer in order to send it out
                            Util.arrayCopyNonAtomic(m_resultBuffer,
				                                    (short) 1, apdu_buffer,
				                                    (short) 0, m_resultBuffer[0]);

		                    apdu.setOutgoingLength(m_resultBuffer[0]);
		                    apdu.sendBytes((short) 0, m_resultBuffer[0]);                            
	            		}
	            		else // command 20 !00
                        {
	            			sReasonCode = ISO7816.SW_INCORRECT_P1P2;
	                        ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);      
                        }
	            		
	            		break; // end case
	            	}
	            	
	            	/******************* VERIFY KEY *******************/
	                case bTAG_INS_VERIFY_KEY: // 30 00 - Case 4
	            	{
	            		if ( bTAG_P1_00 == bP1 )
	            		{
	            			// -------------------------HANDLE APDU
	            			// receive the data which is transmitted to the Applet via APDU
	            			apdu_len = apdu.setIncomingAndReceive();
	            			
	            			// if LC bytes is different than apdu data length
                            if ( sLc != apdu_len )
                            {
                            	sReasonCode = ISO7816.SW_WRONG_LENGTH;
                                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                            }
                            
                            // store APDU data into RAM because apdu_buffer is not available in the subfunction
                    		Util.arrayCopy(apdu_buffer, (short)(ISO7816.OFFSET_CDATA), s_abRAMBuffer, (short)0, sLc);
                    		
                    		// do the actual work                            
                    		if ( RETURN_VALUE_PASS != handleCommandsCreateVerifyDelete(bTAG_INS_VERIFY_KEY, bP2) )
                    		{
                            	sReasonCode = ISO7816.SW_DATA_INVALID;
                                ISOException.throwIt(ISO7816.SW_DATA_INVALID);                    			
                    		}
                            
	            			// ensure that the command requested correct number of answer bytes
                            sLe = apdu.setOutgoing();
                            if ( (sLe > 0) && (sLe < m_resultBuffer[0]) )
                            {
                            	sReasonCode = ISO7816.SW_WRONG_LENGTH;
                                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                            }
                            
                            // put the result in the apdu buffer in order to send it out
                            Util.arrayCopyNonAtomic(m_resultBuffer,
				                                    (short) 1, apdu_buffer,
				                                    (short) 0, m_resultBuffer[0]);

		                    apdu.setOutgoingLength(m_resultBuffer[0]);
		                    apdu.sendBytes((short) 0, m_resultBuffer[0]); 
	            			
	            		}
	            		else // command 30 !00
                        {
	            			sReasonCode = ISO7816.SW_INCORRECT_P1P2;
	                        ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                        }
	            		
	            		break; // end case
	            	}
	            	
	            	/******************* DELETE KEY *******************/
	                case bTAG_INS_DELETE_KEY: // 40 00 - Case 3
	            	{
	            		if ( bTAG_P1_00 == bP1 )
	            		{
	            			// -------------------------HANDLE APDU
	            			// receive the data which is transmitted to the Applet via APDU
	            			apdu_len = apdu.setIncomingAndReceive();
	            			
	            			// if LC bytes is different than apdu data length
                            if ( sLc != apdu_len )
                            {
                            	sReasonCode = ISO7816.SW_WRONG_LENGTH;
                                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                            }
                            
                            // store APDU data into RAM because apdu_buffer is not available in the subfunction
                    		Util.arrayCopy(apdu_buffer, (short)(ISO7816.OFFSET_CDATA), s_abRAMBuffer, (short)0, sLc);
                            
                            // do the actual work
                    		if ( RETURN_VALUE_PASS != handleCommandsCreateVerifyDelete(bTAG_INS_DELETE_KEY, bP2) )
                    		{
                            	sReasonCode = ISO7816.SW_DATA_INVALID;
                                ISOException.throwIt(ISO7816.SW_DATA_INVALID);                    			
                    		}
	            			
	            			// ensure that the command did not request answer bytes
                            sLe = apdu.setOutgoing();
                            if (sLe != (short)0x0000)
                            {
                            	sReasonCode = ISO7816.SW_WRONG_LENGTH;
                                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                            }
	            		}
	            		else // command 40 !00
                        {
	            			sReasonCode = ISO7816.SW_INCORRECT_P1P2;
	                        ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                        }
	            		
	            		break; // end case	            		
	            	}
	            	
	            	/******************* VERSION INFO *******************/
                    case bTAG_INS_GETVERSION: // 0x76 will return 2 bytes version information - Case 2
                    {
                    	// -------------------------HANDLE APDU
                        apdu_len = apdu.setIncomingAndReceive();
                        
                        sLen = 2; // length of version information
                        
                        if (0 != apdu_len)
                        {
                        	sReasonCode = ISO7816.SW_DATA_INVALID;
                        	ISOException.throwIt(ISO7816.SW_DATA_INVALID);
                        }
                        
                    	sLe = apdu.setOutgoing();
                        if ((sLe > 0) && (sLe < sLen))
                        {
                        	sReasonCode = ISO7816.SW_WRONG_LENGTH;
                            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                        }
                        
                        // store version in apdu buffer
                        apdu_buffer[(short)0] = bVERSION_MAJOR;
                        apdu_buffer[(short)1] = bVERSION_MINOR;

                        // send it
                        apdu.setOutgoingLength((short) sLen);
                        apdu.sendBytes((short) 0, (short) sLen);

                        break;
                    }
                    
                    /******************* ERROR HANDLING *******************/
                    default:
                    {
                    	sReasonCode = ISO7816.SW_INS_NOT_SUPPORTED;
                        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                        break; // end case (not needed)
                    }
	            } // end switch instruction
            } // end try
            catch (Exception e)
            {
            	ISOException.throwIt(sReasonCode); // initialized with 6F00 and overwritten in case of defined error
            }
            finally
            {
            }
            // END SUCCESSFUL HASH CHECK / AUTHENTICATION
    		/********************************************************************************************************************************/
    		/********************************************************************************************************************************/
        }        
        else
        {
        	/********************************************************************************************************************************/
        	/********************************************************************************************************************************/
        	// START AUTHENTICATE / HASH CHECK
        	try
            {		// " CLA != 70 " is caught already at the very beginning
        		if ( bINS == bTAG_INS_VERIFY_GLOBAL )
        		{
        			if ( bP1 == bTAG_P1_00 )
        			{
        				// -------------------------HANDLE APDU
                		apdu_len = apdu.setIncomingAndReceive();
                        
                        if (sLc != apdu_len)
                        {
                        	sReasonCode = ISO7816.SW_WRONG_LENGTH;
                            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                        }
                        
                        sLe = apdu.setOutgoing();
                        if (sLe != (short)0x0000)
                        {
                        	sReasonCode = ISO7816.SW_WRONG_LENGTH;
                            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                        }
                        
                        if (apdu_len == VERIFY_GLOBAL_APDU_DATA_LENGTH)                  	
                        {
                            // make plausibility check that the retry counter is between 1 and MAX_NMBR_OF TRIES
        	            	if ( ( (short)1 <= sVerifyGlobalPinRetryCounter ) && (RESET_VERIFY_GLOBAL_PIN_RETRY_COUNTER >= sVerifyGlobalPinRetryCounter) )
        	                {                    
        	            		sVerifyGlobalPinRetryCounter --; // decrement before check!!
                            	
                            	// check of passed HASH value against the stored hash value (in EEPROM)
                            	if ( 0 == Util.arrayCompare(apdu_buffer, (short)(ISO7816.OFFSET_CDATA), globalPin, (short) 0, VERIFY_GLOBAL_APDU_DATA_LENGTH))
                            	{
                            		sVerifyGlobalPinChecked[0] = VERIFY_GLOBAL_PIN_SUCCESSFULLY_CHECKED;
                            		sVerifyGlobalPinRetryCounter = RESET_VERIFY_GLOBAL_PIN_RETRY_COUNTER; // reset retry counter
                            		return; // 90 00
                            	}
                            	
                            	// -- we land here if the hash was entered incorrectly! --
                            	sReasonCode = ISO7816.SW_CONDITIONS_NOT_SATISFIED;
                            	ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
                            }
        	            	else
        	            	{
        	            		// blocked forever!! (retry counter has expired)
        	            		sReasonCode = ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED;
        	            		ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        	            	}
                        }
                        else
                        {
                        	sReasonCode = ISO7816.SW_DATA_INVALID;
                        	ISOException.throwIt(ISO7816.SW_DATA_INVALID);                    	
                        }
        				
        			}
        			else
        			{
            			sReasonCode = ISO7816.SW_INCORRECT_P1P2;
                        ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);        				
        			}
        			
        		}
        		else // INS != bTAG_INS_VERIFY_GLOBAL
                {
                	// application is blocked (still) - so no (other) instructions are allowed
                	sReasonCode = ISO7816.SW_CONDITIONS_NOT_SATISFIED;
                	ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
                }    		
            } // end try
            catch (Exception e)
            {
            	ISOException.throwIt(sReasonCode); // initialized with 6F00 and overwritten in case of defined error
            }
            finally
            {
            }
            // END AUTHENTICATE / HASH CHECK
    		/********************************************************************************************************************************/
    		/********************************************************************************************************************************/
        }
        return; // 90 00
    }
    
    /**
     * function responsible for handling of data
     * m_resultBuffer will always hold the result buffer if bTAG_INS_CREATE_KEY or bTAG_INS_VERIFY_KEY
     */
    public byte handleCommandsCreateVerifyDelete(byte bCommand, byte bP2)
    {
    	// needed for all 3 flows
		short i; // loop variable needed later
		byte bHashLength;
		byte bPasswordLength;
		
		// only needed for CREATE_KEY
		byte bSizeOfDesiredOutData; // size of the actual data to be used for output
		byte bSizeOfInputAPDUData; // size of data "hash" and "password"
    	
		// always same structure:
		// received APDU <len> <len hash> <hash bytes> <len password> <password> will be copied to s_abRAMBuffer
		// <len hash> <hash bytes> <len password> <password>
    	bHashLength = s_abRAMBuffer[0];
		bPasswordLength = s_abRAMBuffer[(byte)(bHashLength+1)]; // length byte "len hash" plus "hash bytes"
		
		// check if the values have space in the "columns" of the "table"
		// note: ">=" and not ">" is correct, because 1 byte for the length is required in addition
		if ( (bHashLength >= NUMBER_OF_BYTES_FOR_HASH_IN_TABLE) || (bPasswordLength >= NUMBER_OF_BYTES_FOR_PASSWORD_IN_TABLE) )
        {
			return RETURN_VALUE_FAIL;
        }
		
        /**************************************/
        // check if element is used (valid for all 3 commands CREATE, VERIFY, DELETE)
        for (i = 0; i < NUMBER_OF_ELEMENTS_IN_TABLE; i++)
        {
        	if ( s_abNVMBufferElementUseIndicator[i] == ELEMENT_USED )
        	{
        		// check if hash incl. length byte (important, otherwise someone could fake 1 byte len!!!) was found
        		if ( IS_EQUAL == Util.arrayCompare(s_abRAMBuffer, (short)0, s_abNVMBufferHashBytes, (short)(i*NUMBER_OF_BYTES_FOR_HASH_IN_TABLE), (short)(1+bHashLength)) )
        		{		        			
        			// check if password incl. length byte (important, otherwise someone could fake 1 byte len!!!) is correct
	        		if ( IS_EQUAL == Util.arrayCompare(s_abRAMBuffer, (short)(bHashLength+1), s_abNVMBufferPasswordBytes, (short)(i*NUMBER_OF_BYTES_FOR_PASSWORD_IN_TABLE), (short)(1+bPasswordLength)) )
	        		{
	        			break; // exit loop
	        		}		        			
        		}
        	}                            	
        }
        
        // the value of i now has different meanings, depending on the context
        // if the loop above has been gone through till the end,
        // it means that no entry has been found where hash and password match
        // - if CREATE_KEY was called, it means there is already a cipher key with this hash and password => not normal behavior -> indicate this to the user
        // - otherwise if VERIFY_KEY or DELETE_KEY was called, it means there is a cipher key with this hash and password => normal behavior
		
		switch (bCommand)
		{
			case bTAG_INS_CREATE_KEY:
			{
		        // valid entry - loop has been exited with "break" meaning there is a pair hash-password already
		        if ( i != NUMBER_OF_ELEMENTS_IN_TABLE )
		        {
		        	return RETURN_VALUE_FAIL;
		        }
		        
		        // normal flow for "CREATE_KEY" begins:
				// P2 in create cipher key contains the desired length of the cipher key
				switch (bP2) // this switch block: visualization purpose only - bP2 * 8 would yield the same result!!
				{
					case 0x01:
					{
						bSizeOfDesiredOutData = 8;
						break;
					}
					case 0x02:
					{
						bSizeOfDesiredOutData = 16;
						break;
					}
					case 0x03:
					{
						bSizeOfDesiredOutData = 24;
						break;
					}
					case 0x04:
					{
						bSizeOfDesiredOutData = 32;
						break;
					}
					default:
					{
						return RETURN_VALUE_FAIL;
					}
				}		
				
		        /**************************************/
		        // check if there is still a free element in the table to store a new cipher key
		        for (i = 0; i < NUMBER_OF_ELEMENTS_IN_TABLE; i++)
		        {
		        	if ( s_abNVMBufferElementUseIndicator[i] == ELEMENT_UNUSED )
		        	{
		        		s_abNVMBufferElementUseIndicator[i] = ELEMENT_USED;
		        		break;
		        	}                            	
		        }
		        
		        // no more free entry - loop has not been exited with "break"
		        if ( i == NUMBER_OF_ELEMENTS_IN_TABLE )
		        {
		        	return RETURN_VALUE_FAIL;
		        }
		        
		        // copy <len hash> <hash bytes> ( 1 + bHashLength) starting from 0 (s_abRAMBuffer starts with <len hash>)
		        Util.arrayCopy(s_abRAMBuffer, (short)0, s_abNVMBufferHashBytes, (short)(i*NUMBER_OF_BYTES_FOR_HASH_IN_TABLE), (short)(1+bHashLength));
		        
		        // copy <len password> <password> ( 1 + bPasswordLength) starting from 1+bHashLength (s_abRAMBuffer starts with <len hash> <hash bytes> <len password>)
		        Util.arrayCopy(s_abRAMBuffer, (short)(bHashLength+1), s_abNVMBufferPasswordBytes, (short)(i*NUMBER_OF_BYTES_FOR_PASSWORD_IN_TABLE), (short)(1+bPasswordLength));

		        // copy hash and password in RAM (inBuffer) which is used as input for the DES encryption
		        // this time, the length bytes are not used and thus discarded (skipped)
		        Util.arrayCopy(s_abRAMBuffer, (short)APDU_HASH_OFFSET, m_inBuffer, (short)1, (short)bHashLength);
		        Util.arrayCopy(s_abRAMBuffer, (short)(APDU_HASH_OFFSET+bHashLength+1), m_inBuffer, (short)(1+bHashLength), (short)bPasswordLength);
		        
		        bSizeOfInputAPDUData = (byte)(bHashLength + bPasswordLength);
		        m_inBuffer[0] = bSizeOfDesiredOutData;
		        
		        if ( bSizeOfInputAPDUData < bSizeOfDesiredOutData )
		        {
		            // fill with 0x00 to be clean - eventhough it could be interesting to make use of the effect that this RAM array
		            // still contains "garbage"
		            Util.arrayFillNonAtomic(m_inBuffer, (short)(1+bSizeOfInputAPDUData), (short)(bSizeOfDesiredOutData-bSizeOfInputAPDUData), (byte)0x00);        	
		        }
		        
		        // create key with Algo depending on bP2
		        if ( RETURN_VALUE_PASS != createCipherKey() )
		        {
		        	return RETURN_VALUE_FAIL;
		        }
		        
		        // save the generated cipher key (plus 1 length byte) in the table
		        Util.arrayCopy(m_resultBuffer, (short) 0, s_abNVMCipherKeyBytes, (short)(i*NUMBER_OF_BYTES_FOR_CIPHER_KEY_IN_TABLE), (short)(1+m_resultBuffer[0]));
		        
		        return RETURN_VALUE_PASS; // PASS				
			}
		
			case bTAG_INS_VERIFY_KEY:
			case bTAG_INS_DELETE_KEY:
			{
		        // no valid entry - loop has not been exited with "break"
		        if (i == NUMBER_OF_ELEMENTS_IN_TABLE)
		        {
		        	return RETURN_VALUE_FAIL;
		        }
		        
				// at the end: if verify key, return this key, if delete key, delete this key
		        if (bCommand == bTAG_INS_VERIFY_KEY)
		        {
			        // save the cipher key from the table into m_resultBuffer - hard code to NUMBER_OF_BYTES_FOR_CIPHER_KEY_IN_TABLE, real length is filtered lateron
		        	Util.arrayCopy(s_abNVMCipherKeyBytes, (short)(i*NUMBER_OF_BYTES_FOR_CIPHER_KEY_IN_TABLE), m_resultBuffer, (short) 0, NUMBER_OF_BYTES_FOR_CIPHER_KEY_IN_TABLE);
		        	
		        	return RETURN_VALUE_PASS; // PASS
		        }
		        else if (bCommand == bTAG_INS_DELETE_KEY)
		        {
		        	s_abNVMBufferElementUseIndicator[i] = ELEMENT_UNUSED;
		        	Util.arrayFillNonAtomic(s_abNVMBufferHashBytes, (short)(i*NUMBER_OF_BYTES_FOR_HASH_IN_TABLE), NUMBER_OF_BYTES_FOR_HASH_IN_TABLE, (byte)0x00);
		        	Util.arrayFillNonAtomic(s_abNVMBufferPasswordBytes, (short)(i*NUMBER_OF_BYTES_FOR_PASSWORD_IN_TABLE), NUMBER_OF_BYTES_FOR_PASSWORD_IN_TABLE, (byte)0x00);
		        	
		        	return RETURN_VALUE_PASS; // PASS
		        }
		        else // cannot occur because only 2 values in case block (but for better readability)
		        {
		        	return RETURN_VALUE_FAIL;

		        }
			}			
			default:
			{
				return RETURN_VALUE_FAIL;
			}
			
		}
    }
    
    
    /**
     * function responsible to generate cipher key
     */
    public byte createCipherKey()
    {
    	short sOutputLen;
    	
		// first of all create 32 random bytes and store them in RAM
		rnd.generateData(abRandomDES_KEY_ARRAYInRAM, (short)0, (short)32);
		
		// use these random bytes as the key for DES
		myDESKey.setKey(abRandomDES_KEY_ARRAYInRAM,(short)0);
		
		// initialize DES encryption
		myCipher.init(myDESKey, Cipher.MODE_ENCRYPT);
		
		// perform DES encryption and store it from offset 1 in m_resultBuffer (0 will hold the length)
		sOutputLen = myCipher.doFinal(m_inBuffer, (short)1, m_inBuffer[0], m_resultBuffer, (short)1);
		// element 0 of m_resultBuffer holds the length of the output
		m_resultBuffer[0] = (byte)(sOutputLen & 0xFF); // length
    	
		return RETURN_VALUE_PASS;
    }
    
    /**
     * Method called by the JCRE at the installation of the applet.
     * </p>
     * Creates the applet instance, and registers to all necessary events.
     * </p>
     *
     * @param abInstallData
     *            the array containing installation parameters.
     * @param sOffset
     *            the starting offset in installData.
     * @param bLength
     *            the length in bytes of the parameter data in
     *            <code>installData</code>.
     */
    public static void install(byte[] abInstallData, short sOffset, byte bLength)
    {
    	short sLength;
    	
    	//----------------------------------------------------------------------
        // create and register new applet instance
        //----------------------------------------------------------------------
    	
    	new FileManager().register();

        //----------------------------------------------------------------------
        // offset points to AID length
        //----------------------------------------------------------------------

        sLength = ( short ) ( abInstallData[ sOffset ] & ( short ) 0x00FF );

        //----------------------------------------------------------------------
        // first: offset points to privileges length
        //----------------------------------------------------------------------

        sOffset += ( short ) ( sLength + ( short ) 1 );

        sLength = ( short ) ( abInstallData[ sOffset ] & ( short ) 0x00FF );

        //----------------------------------------------------------------------
        // offset points to install parameters length
        //----------------------------------------------------------------------

        sOffset+= ( short ) ( sLength + ( short ) 1 );

        //----------------------------------------------------------------------
        // in case of parameter length is 0 or is missing goto catch
        //----------------------------------------------------------------------

        sLength = ( short ) ( abInstallData[ sOffset++ ] & ( short ) 0x00FF );
    }
}
