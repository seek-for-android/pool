package com.gieseckedevrient.javacard.fileviewtest;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.OwnerPIN;
import javacard.framework.UserException;

public class AuthenticationProvider {

    //<DATACREATOR_TB_TEXT>
    private static final byte[] TB_TEXT = {
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 
    };
    //</DATACREATOR_TB_TEXT>

    /** Length of user-password.*/
    private static final byte USER_PASSWORD_LEN = (byte) 0x03;
    /** Length of data field in the Change PIN command.*/
    private static final byte CHANGE_PIN_LENGTH = (byte) 0x06;
    /** Length of user-password.*/
    private static final byte PUK_LENGTH = (byte) 0x07;
    /**Length of two consecutive passwords.*/
    private static final byte USER_MODIFY_PUK = (byte) (USER_PASSWORD_LEN + PUK_LENGTH);
    /** State Blocked -> Reset counter maxed out.*/
    private static final byte STATE_BLOCKED = (byte) 0x00;
    /** State Unblocked.*/
    private static final byte STATE_UNBLOCKED = (byte) 0x01;
    /** Flag for blocked Application.*/
    private static byte b_pinBlocked = STATE_UNBLOCKED;


    /**Initialization of the PIN.*/
    private static final byte[] PIN_INIT = {(byte) 0x00, (byte) 0x00, (byte) 0x00};
    /**Initialization of the PUK.*/
    private static final byte[] PUK_INIT = {
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };

    /**Mask for byte P1.*/
    private static final byte BYTE_P1_MASK =(byte) 0x01;
    /**Mask for byte P2.*/
    private static final byte BYTE_P2_MASK = (byte) 0x80;
    /**P2 for read the Tries remaining.*/
    private static final byte GET_TRIES_REMAINIG = (byte) 0x00;
    /**P2 for check the PIN.*/
    private static final byte CHECK_PIN = (byte) 0x80;
    /**P1 for change the PIN for a new one.*/
    private static final byte CHANGE_PIN_RESET = (byte) 0x00;
    /**P1 for reset the PIN Try Counter.*/
    private static final byte RESET_COUNTER = (byte) 0x01;
    /**P1 for enable PIN verification with PIN.*/
    private static final byte ENABLE_WITH_PIN = (byte) 0x00;
    /**P1 for enable PIN verification without PIN.*/
    private static final byte ENABLE_WITHOUT_PIN = (byte) 0x01;
    /**P1 for disable PIN verification without PIN.*/
    private static final byte DISABLE_WITHOUT_PIN = (byte) 0x01;
    /**P1 for disable PIN verification with PIN.*/
    private static final byte DISABLE_WITH_PIN = (byte) 0x00;

    /**Indicates that PIN verification is enabled.*/
    public static final byte PIN_ENABLE = (byte) 0x01;
    /**Indicates that PIN verification is disabled.*/
    private static final byte PIN_DISABLE = (byte) 0x00;
    /**Indicates if PIN verification is enabled or disabled.*/
    public byte b_Enable = PIN_DISABLE;

    /**PIN code*/
    private OwnerPIN o_PIN;
    /**PUK code*/
    private OwnerPIN o_PUK;


    /**
     * Constructor.
     * 
     * @param tryLimit number of tries.
     * @param maxPINSize PIN size max.
     */
    public AuthenticationProvider(byte tryLimit, byte maxPINSize) {
        o_PIN = new OwnerPIN(tryLimit, maxPINSize);
        o_PIN.update(PIN_INIT, (short) 0, USER_PASSWORD_LEN);

        o_PUK = new OwnerPIN(tryLimit, PUK_LENGTH);
        o_PUK.update(PUK_INIT, (short) 0, PUK_LENGTH);
    }

/*******************************************************************************************************
                                           VERIFY PIN TAG '20' command
/*******************************************************************************************************/

    /**
    * Verify if the PIN is correct.
    * 
    * @param bP1 Parameter P1 of the incoming APDU
    * @param bP2 Parameter P2 of the incoming APDU
    * @param Lc Lc Parameter LC of the incoming APDU
    * @param bArray Data of the incoming APDU
    */
    public void v_ProcessVerifyTag20(byte bP1, byte bP2, short sLc, byte[] abArray) {

        byte bP2_mask = (byte) (bP2 & BYTE_P2_MASK);
        byte bTries = (byte) 0x00;

        if(b_pinBlocked == STATE_BLOCKED) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        // P2= 0x80 -> Specific reference data(e.g DF specific password or key). 
        // P2= 0x00 -> No information given.
        if ((bP1 != (byte) 0x00) && (!(bP2_mask == (byte) 0x80 || (bP2 == (byte) 0x00)))) {
            // Incorrect parameters P1-P2
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        switch (bP2_mask) {
        case GET_TRIES_REMAINIG:
            bTries = o_PIN.getTriesRemaining();
            ISOException.throwIt((short) ((short) 0x63C0 + toShort(bTries)));
            break;
        case CHECK_PIN:
            if (o_PIN.check(abArray, toShort(ISO7816.OFFSET_CDATA), (byte) sLc)) {
                ISOException.throwIt(ISO7816.SW_NO_ERROR);
            } 
            bTries = o_PIN.getTriesRemaining();
            if (bTries == (byte) 0x00) {
                b_pinBlocked = STATE_BLOCKED;
            }
            ISOException.throwIt((short) ((short) 0x63C0 + toShort(bTries)));
            break;
        default:
            break;
        }

    }

/*******************************************************************************************************
                                           RESET RETRY COUNTER command
/*******************************************************************************************************/

    /**
     * Method that process RESET RETRY COUNTER command.
     * 
     * @param bP1 Parameter P1 of the incoming APDU
     * @param bP2 Parameter P2 of the incoming APDU
     * @param Lc Lc Parameter LC of the incoming APDU
     * @param bArray Data of the incoming APDU
     * @throws ISOException if any condition is not satisfied. '90 00' if successful
     */
    public void v_ProcessReset(byte bP1, byte bP2, short sLc, byte[] abArray) {

        byte bP1_mask = (byte) (bP1 & BYTE_P1_MASK);
        byte bP2_mask = (byte) (bP2 & BYTE_P2_MASK);

        //Check if bP1 and bP2 are corrects
        //bP1 = 00 or 01
        //bP2_mask = 80
        if (((bP1 & (byte) 0xFC) != (byte) 0x00) || (bP2_mask != (byte) 0x80)) {
            // Incorrect parameters P1-P2
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }
        switch (bP1_mask) {
        case CHANGE_PIN_RESET: //Update PIN and reset PTC
            if (sLc != toShort(USER_MODIFY_PUK)) {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }
            break;
        case RESET_COUNTER: //Reset PTC
            if (sLc != toShort(PUK_LENGTH)) {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }
            break;
        default:
            break;
        }

        //Check if PUK is correct
        if (!o_PUK.check(abArray, toShort(ISO7816.OFFSET_CDATA), PUK_LENGTH)) {
            //PIN is not correct
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        //Update the new PIN and reset PTC
        if (bP1_mask == CHANGE_PIN_RESET) {
            o_PIN.update(abArray, toShort((byte) (ISO7816.OFFSET_CDATA + PUK_LENGTH)), USER_PASSWORD_LEN);
        }

        //Change the status 
        b_pinBlocked = STATE_UNBLOCKED;

        //PIN is updated and PTCb fixed to its limit
        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }

/*******************************************************************************************************
                                           CHANGE PASSCODE command
/*******************************************************************************************************/

    /**
    * Change the PIN. 
    *
    * @param abBuffer buffer to store the text to display
    * @param sOffset Offset into the buffer
    * @throws UserException propagates errors or session termination
    */
   public  void v_ChangePIN(byte bP1, byte bP2, short sLc, byte[] abBuffer) {

       byte bP2_mask = (byte) (bP2 & BYTE_P2_MASK);

       //Check if PIN is blocked
       if (b_pinBlocked == STATE_BLOCKED) {
           ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
       }

       //Check if P1 == 0x00 and bP2_mask = 0x80
       if ((bP1 != (byte) 0x00) || (bP2_mask != (byte) 0x80)) {
           ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
       }

       //Check length
       if (sLc != toShort(CHANGE_PIN_LENGTH)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

       if (!o_PIN.check(abBuffer, toShort(ISO7816.OFFSET_CDATA), USER_PASSWORD_LEN)) {
           ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
       }

       //Update PIN
       o_PIN.update(abBuffer, toShort((byte) (ISO7816.OFFSET_CDATA + USER_PASSWORD_LEN)), USER_PASSWORD_LEN);

       //PIN is updated 
       ISOException.throwIt(ISO7816.SW_NO_ERROR);

    }

/*******************************************************************************************************
                                       ENABLE PASSCODE command
/*******************************************************************************************************/
   /**
     * Function for enable the PIN verification.
     * 
     * @param bP1 Parameter P1 of the incoming APDU
     * @param bP2 Parameter P2 of the incoming APDU
     * @param sLc Parameter LC of the incoming APDU
     * @param abBuffer Data of the incoming APDU
     */
    public void v_EnablePIN(byte bP1, byte bP2, short sLc, byte[] abBuffer) {

        byte bP1_mask = (byte) (bP1 & BYTE_P1_MASK);
        byte bP2_mask = (byte) (bP2 & BYTE_P2_MASK);

        //Check if PIN is blocked 
        if (b_pinBlocked == STATE_BLOCKED) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        //Check P1 and P2
        //bP1 = 0x00 or 0x01
        //bP2_mask = 0x80
        if (((bP1 & (byte) 0xFE) != (byte) 0x00) || (bP2_mask != (byte) 0x80)){
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        switch (bP1_mask) {
        case ENABLE_WITH_PIN:
            //Check if sLc is correct.
            if (sLc != toShort(USER_PASSWORD_LEN)) {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }

            //Check if PIN is correct
            if(!o_PIN.check(abBuffer, toShort(ISO7816.OFFSET_CDATA), USER_PASSWORD_LEN)) {
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }

            //Enable PIN verification
            b_Enable = PIN_ENABLE;
            break;
        case ENABLE_WITHOUT_PIN:
            //Check if PIN is validated
            if (!o_PIN.isValidated()) {
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }

            //Enable PIN verification
            b_Enable = PIN_ENABLE;
            break;
        default:
            break;
        }

        //If any problem has occurred, send the SW '9000'
        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }


/*******************************************************************************************************
                                            DISABLE PASSCODE command
/*******************************************************************************************************/
    /**
    * Function for disable the PIN verification.
    * 
    * @param bP1 Parameter P1 of the incoming APDU
    * @param bP2 Parameter P2 of the incoming APDU
    * @param sLc Parameter LC of the incoming APDU
    * @param abBuffer Data of the incoming APDU
    */
    public void v_DisablePIN(byte bP1, byte bP2, short sLc, byte[] abBuffer) {

        byte bP1_mask = (byte) (bP1 & BYTE_P1_MASK);
        byte bP2_mask = (byte) (bP2 & BYTE_P2_MASK);

        //Check if PIN is blocked 
        if (b_pinBlocked == STATE_BLOCKED) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        //Check P1 and P2
        //bP1 = 0x00 or 0x01
        //bP2_mask = 0x80
        if (((bP1 & (byte) 0xFE) != (byte) 0x00) || (bP2_mask != (byte) 0x80)) {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        switch (bP1_mask) {
        case DISABLE_WITH_PIN:
            //Check if sLc is correct.
            if (sLc != toShort(USER_PASSWORD_LEN)) {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }

            //Check if PIN is correct
            if(!o_PIN.check(abBuffer, toShort(ISO7816.OFFSET_CDATA), USER_PASSWORD_LEN)) {
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }

            //Enable PIN verification
            b_Enable = PIN_DISABLE;
            break;
        case DISABLE_WITHOUT_PIN:
            //Check if PIN is validated
            if (!o_PIN.isValidated()) {
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }

            //Enable PIN verification
            b_Enable = PIN_DISABLE;
            break;
        default:
            break;
        }

        //If any problem has occurred, send the SW '9000'
        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }

    /**
     * Check if PIN is validated.
     * 
     * @return true if validated. False if not.
     */
    public boolean z_Validated () {
        if (o_PIN.isValidated())
            return true;
        else
            return false;
    }


    /**
     * Safe conversion from unsigned byte to unsigned short.
     *
     * @param bByte byte to be converted
     * @return value converted to short
     */
    private static short toShort(final byte bByte) {
        return (short) (0x00FF & bByte);
    }


}
