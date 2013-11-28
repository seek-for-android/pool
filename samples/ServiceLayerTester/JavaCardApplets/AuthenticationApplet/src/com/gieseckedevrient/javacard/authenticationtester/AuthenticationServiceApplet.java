package com.gieseckedevrient.javacard.authenticationtester;

import javacard.framework.*;

/**
 *
 */
public final class AuthenticationServiceApplet extends Applet {


    //<DATACREATOR_TB_TEXT>
    //</DATACREATOR_TB_TEXT>

    //<DATACREATOR_TB_CONSTANTS>
    //</DATACREATOR_TB_CONSTANTS>

    
    //<TB_VERSION>
    private static final byte[] TB_VERSION = {
        (byte)0x01, (byte)0x00, (byte)0x00
    };
    //</TB_VERSION>

    /**  CLA used by the application. */
    private static final byte CLA_00 = (byte) 0x00;

    /**  INS of Verify command with tag '20'.*/
    private static final byte INS_VERIFY_20 = (byte) 0x20;
    /**  INS of Reset Retry Counter.*/
    private static final byte INS_RESET_RETRY_COUNTER = (byte) 0x2C;
    /**  INS of Enable Verification.*/
    private static final byte INS_ENABLE_VERIFICATION = (byte) 0x28;
    /**  INS of Disable Verification.*/
    private static final byte INS_DISABLE_VERIFICATION = (byte) 0x26;
    /**  INS of Disable Verification.*/
    private static final byte INS_CHANGE_PIN = (byte) 0x24;

    /** Length of user-password.*/
    private static final byte USER_PASSWORD_LEN = (byte) 0x04;
    /** Maximum retry counter for password.*/
    private static final byte MAX_RETRY_COUNTER = (byte) 0x03;

    /** Length of user-password.*/
    private static final byte PUK_LENGTH = (byte) 0x08;
    /**Length of two consecutive passwords.*/
    private static final byte USER_MODIFY_PUK = (byte) (USER_PASSWORD_LEN + PUK_LENGTH);
    /** State Blocked -> Reset counter maxed out.*/
    private static final byte STATE_BLOCKED  = (byte) 0x00;
    /** State Unblocked.*/
    private static final byte STATE_UNBLOCKED = (byte) 0x01;
    /** Flag for blocked Application.*/
    private static byte b_pinBlocked = STATE_UNBLOCKED;


    /**Initialization of the PIN.*/
    private static final byte[] PIN_INIT = {(byte) 0x31, (byte) 0x31, (byte) 0x31,(byte) 0x31};
    /**Initialization of the PUK.*/
    private static final byte[] PUK_INIT = {
        (byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31
    };

    /**Mask for byte P1.*/
    private static final byte BYTE_P1_MASK =(byte) 0x01;
    /**Mask for byte P2.*/
    private static final byte BYTE_P2_MASK = (byte) 0x81;
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

    /*
     * Constants used in the getCapVersion() method. This is a copy paste method
     * that has to be always added to any applet developed.
     */

    /**
     * CLA byte used to get the applet version = 0xB0
     */
    private static final byte CLA_CONTROL = (byte) 0xB0;

    /**
     * INS byte used to get the applet version = 0x76
     */
    private static final byte INS_VERSION = (byte) 0x76;


    /**
     * Constructor of the Applet. Creates objects, allocates RAM buffers,
     * initializes global variables, ...
     */
    private AuthenticationServiceApplet(byte[] abInstallData, short sOffset, byte bLength) {

        o_PIN = new OwnerPIN(MAX_RETRY_COUNTER, USER_PASSWORD_LEN);
        o_PIN.update(PIN_INIT, (short) 0, USER_PASSWORD_LEN);

        o_PUK = new OwnerPIN(MAX_RETRY_COUNTER, PUK_LENGTH);
        o_PUK.update(PUK_INIT, (short) 0, PUK_LENGTH);

    }


    /**
     * Method called by the JCRE at the installation of the applet. Creates the
     * applet instance, and registers to all required events.
     *
     * @param abInstallData the array containing installation parameters.
     * @param sOffset the starting offset in installData.
     * @param bLength the length in bytes of the parameter data in installData.
     */
    public static void install(byte[] abInstallData, short sOffset,
            byte bLength) throws ISOException {
        AuthenticationServiceApplet applet = new AuthenticationServiceApplet(abInstallData, sOffset, bLength);

        applet.register(abInstallData, (short) (sOffset + (short) 1),
                        abInstallData[sOffset]);
    }


    /**
     * This method must be implemented by applets. It is declared as abstract in
     * class Applet.
     *
     * @param   aApdu  the incoming APDU object.
     */
    public void process(APDU aApdu) {
        if (selectingApplet()) {
            return;
        }

        //Gets the APDU Buffer
        byte[] abAPDUbuffer = aApdu.getBuffer();

        //Allow the connection from all the different logical channels. The logical channel used is determined
        //by the two LSB of the CLA byte.
        abAPDUbuffer[ISO7816.OFFSET_CLA] = (byte) (abAPDUbuffer[ISO7816.OFFSET_CLA] & (byte) 0xFC);

        byte bP1 = abAPDUbuffer[ISO7816.OFFSET_P1];
        byte bP2 = abAPDUbuffer[ISO7816.OFFSET_P2];
        short sLc = toShort(abAPDUbuffer[ISO7816.OFFSET_LC]);

        //Get incoming data
        if (sLc != aApdu.setIncomingAndReceive()) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        //Evaluate the CLA byte
        switch(abAPDUbuffer[ISO7816.OFFSET_CLA]){
        //Get information about the version
        case CLA_CONTROL:
            getCapVersion(aApdu, TB_VERSION, (short) 0, (short) TB_VERSION.length);
            break;
        case CLA_00:
            switch (abAPDUbuffer[ISO7816.OFFSET_INS]) {
            case INS_VERIFY_20:
                //Check if P2 indicates PIN1
                if ((((bP2 & (byte) 0xE0)) != (byte) 0x80) && (bP1 != (byte) 0x00)) {
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                }
                v_ProcessVerifyTag20(bP1, bP2, sLc, abAPDUbuffer);
                break;
            case INS_RESET_RETRY_COUNTER:
                //Check if P2 indicates PIN1
                if ((((bP2 & (byte) 0xE0)) != (byte) 0x80) && ((bP1 & (byte) 0xFC) != (byte) 0x00)) {
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                }
                v_ProcessReset(bP1, bP2, sLc, abAPDUbuffer);
                break;
            case INS_CHANGE_PIN:
                //Check if P2 indicates PIN1 or PIN2
                if ((((bP2 & (byte) 0xE0)) != (byte) 0x80) && (bP1 != (byte) 0x00)) {
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                }
                v_ChangePIN(bP1, bP2, sLc, abAPDUbuffer);
                break;
            case INS_ENABLE_VERIFICATION:
                //Check if P2 indicates PIN1
                if ((((bP2 & (byte) 0xE0)) != (byte) 0x80) && ((bP1 & (byte) 0xFC) != (byte) 0x00)) {
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                }
                v_EnablePIN(bP1, bP2, sLc, abAPDUbuffer);
                break;
            case INS_DISABLE_VERIFICATION:
                //Check if P2 indicates PIN1 or PIN2
                if ((((bP2 & (byte) 0xE0)) != (byte) 0x80) && ((bP1 & (byte) 0xFC) != (byte) 0x00)) {
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                }
                v_DisablePIN(bP1, bP2, sLc, abAPDUbuffer);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                }
                break;
        //Not supported
        default:
          ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

    }
    

    /**
     * Method called by the GSM Framework.
     *
     * @param clientAID AID of the application requesting access
     * @param parameter parameters of the request
     * @return The shareable object interface or null.
     */
    public Shareable getShareableInterfaceObject(final AID clientAID,
                                                 final byte parameter) {
        return null;
    }


    /**
     * Method used to get the applet version. 
     * The APDU command is defined as B0 76 00 00 03. It is based in the implementation proposed by GDM in
     * CSOP-Q0223AA-01 but adapted to return 3 bytes instead of 2.
     *
     * @param aApdu       The incoming APDU.
     * @param abVerBuffer Buffer where the version is stored.
     * @param sOffset     Offset in abVerBuffer where version starts.
     * @param sLength     Length of version. It must be 3.
     * @throws ISOException if the incoming APDU format is not the one expected.
     */
    public static void getCapVersion(final APDU aApdu, final byte[] abVerBuffer,
                                     final short sOffset, final short sLength) throws ISOException{

        // get the apdu buffer handle
        byte[] abAPDUBuffer= aApdu.getBuffer();

        // Allow the connection from all the different logical channels. The logical channel used is determined
        // by the two LSB of the CLA byte.
        abAPDUBuffer[ISO7816.OFFSET_CLA] = (byte)(abAPDUBuffer[ISO7816.OFFSET_CLA] & (byte)0xFC);

        // Check CLA byte
        if (abAPDUBuffer[ISO7816.OFFSET_CLA] != CLA_CONTROL) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
        // Check INS byte
        if(abAPDUBuffer[ISO7816.OFFSET_INS] != INS_VERSION) {
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
        // Check P1 and P2
        if(Util.getShort(abAPDUBuffer, ISO7816.OFFSET_P1) != (short) 0) {
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        }
        // Check L
        if (abAPDUBuffer[ISO7816.OFFSET_LC] != (byte) 3) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        // Save version in apdu buffer
        Util.arrayCopyNonAtomic(abVerBuffer, sOffset, abAPDUBuffer, (short) 0, sLength);
        // Send it
        aApdu.setOutgoingAndSend((short) 0, (short) 3);
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

        byte bTries = (byte) 0x00;
        byte bP2_mask = (byte) (bP2 & BYTE_P2_MASK);


        //Check that P2 it is referencing to PIN 1. value 0x01
        if(bP2_mask != (byte) 0x81) {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        //Check if pin is blocked. If so, then through exception
        if(b_pinBlocked == STATE_BLOCKED) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        if(sLc != (short) 0) {
            if (o_PIN.check(abArray, toShort(ISO7816.OFFSET_CDATA), (byte) sLc)) {
                ISOException.throwIt(ISO7816.SW_NO_ERROR);
            }
            bTries = o_PIN.getTriesRemaining();
            if (bTries == (byte) 0x00) {
                b_pinBlocked = STATE_BLOCKED;
            }
            ISOException.throwIt((short) ((short) 0x63C0 + toShort(bTries)));
        } else {
            bTries = o_PIN.getTriesRemaining();
            ISOException.throwIt((short) ((short) 0x63C0 + toShort(bTries)));
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
        //bP2 = 0x01
        if (((bP1 & (byte) 0xFC) != (byte) 0x00) || (bP2_mask != (byte) 0x81)) {
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

        } else {
            //Update the new PIN and reset PTC
            if (bP1_mask == CHANGE_PIN_RESET) {
                o_PIN.update(abArray, toShort((byte) (ISO7816.OFFSET_CDATA + PUK_LENGTH)), USER_PASSWORD_LEN);
            }
            //Reset the PIN try counter
            o_PIN.resetAndUnblock();
            //Change the status 
            b_pinBlocked = STATE_UNBLOCKED;
        }

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
       if ((bP1 != (byte) 0x00) || (bP2_mask != (byte) 0x81)) {
           ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
       }

       //Check length
       if (sLc != USER_PASSWORD_LEN * 2) {
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
        //bP1 = 0x00 or 0x01 && bP2 = 0x01
        if (((bP1 & (byte) 0xFE) != (byte) 0x00) || (bP2_mask != (byte) 0x81)) {
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
        if (((bP1 & (byte) 0xFE) != (byte) 0x00) || (bP2_mask != (byte) 0x81)) {
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
        return (short) ((short) 0x00FF & (short) bByte);
    }
}
