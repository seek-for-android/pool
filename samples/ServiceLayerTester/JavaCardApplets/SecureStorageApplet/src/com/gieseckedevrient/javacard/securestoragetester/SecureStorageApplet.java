package com.gieseckedevrient.javacard.securestoragetester;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;


/**
 *
 */
public final class SecureStorageApplet extends Applet {


    //<DATACREATOR_TB_TEXT>
    //</DATACREATOR_TB_TEXT>

    //<DATACREATOR_TB_CONSTANTS>
    //</DATACREATOR_TB_CONSTANTS>


    //<TB_VERSION>
    private static final byte[] TB_VERSION = {
        (byte)0x01, (byte)0x00, (byte)0x00
    };
    //</TB_VERSION>


    /*
     * Constants used in the getCapVersion() method. This is a copy paste method
     * that has to be always added to any applet developed.
     */

    /**  CLA byte used to get the applet version = 0xB0 */
    private static final byte CLA_CONTROL                  = (byte) 0xB0;
    /**  Proprietary CLA. */
    private static final byte CLA_PROPRIETARY              = (byte) 0x80;

    /**  INS byte used to get the applet version = 0x76 */
    private static final byte INS_VERSION                  = (byte) 0x76;
    /**  INS of Select SS Entry Data command.*/
    private static final byte INS_SELECTSSENTRY            = (byte) 0xA5;
    /**  INS of Get SS Entry Data command.*/
    private static final byte INS_GETSSENTRYDATA           = (byte) 0xCA;
    /**  INS of Get SS Entry ID command.*/
    private static final byte INS_GETSSENTRYID             = (byte) 0xB2;
    /**  INS of Ping APDU*/
    private static final byte INS_PING                     = (byte) 0xAA;    

    /**  P1 Select ID of Select SS Entry Data command. 
      size(0) of Get SS Entry and Put SS Entry */
    private static final byte P1_SELECT                    = (byte) 0x00;
    /**  P1 Select First of Select SS Entry Data command.
     * size(1) of Get SS Entry and Put SS Entry*/
    private static final byte P1_FIRST                     = (byte) 0x01;

    /** SW: Reference Data not Found.*/
    private static final short SW_REFERENCE_DATA_NOT_FOUND = (short) 0x6A88;

    /** Maxim number of register entries.*/
    private static final short MAX_DATA_LENGTH             = (short) 400;
    private static final short MAX_TITLE_LENGTH            = (short) 60;
    private static short s_selectedID                      = (short) -1;


    /**Byte to control the putData and getData of the SS entry.*/
    private static byte b_Entry_Control = (byte) 0x00;
    /**Indicate if the size data in the GetData command has been requested.*/
    private static final byte GET_DATA_SIZE = (byte) 0x04;
    /**Indicate if the first data in the GetData command has been requested.*/
    private static final byte GET_DATA_FIRST = (byte) 0x08;

    //Default arrays for the SS entry
    private static final byte[] DEFAULT_DATA = {(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04};
    private static final byte[] DEFAULT_TITLE = {(byte) 0x45, (byte) 0x6e, (byte) 0x74, (byte) 0x72, (byte) 0x79, (byte) 0x31};

    /** List of registers for CREATE SS ENTRY.*/
    static SecureStorageDataStructure[] m_RegList;


    /**
     * Constructor of the Applet. Creates objects, allocates RAM buffers,
     * initializes global variables, ...
     */
    private SecureStorageApplet(byte[] abInstallData, short sOffset, byte bLength) {

        //Create array to store register list
        m_RegList = new SecureStorageDataStructure[(short) 1];

        //Creation of the SS entry
        m_RegList[(short) 0] = new SecureStorageDataStructure(MAX_DATA_LENGTH, MAX_TITLE_LENGTH);

        //Define the default SS entry
        m_RegList[(short) 0].withData = (byte) 0x01;
        m_RegList[(short) 0].tittleOffset = (short) DEFAULT_TITLE.length;
        m_RegList[(short) 0].dataOffset = (short) DEFAULT_DATA.length;
        Util.arrayCopyNonAtomic(DEFAULT_DATA, (short) 0, m_RegList[(short) 0].data, (short) 0, (short) DEFAULT_DATA.length);
        Util.arrayCopyNonAtomic(DEFAULT_TITLE, (short) 0, m_RegList[(short) 0].title, (short) 0, (short) DEFAULT_TITLE.length);


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
        SecureStorageApplet applet = new SecureStorageApplet(abInstallData, sOffset, bLength);

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
        abAPDUbuffer[ISO7816.OFFSET_CLA] = (byte) (abAPDUbuffer[ISO7816.OFFSET_CLA] & (byte)0xFC);

        byte bP1 = abAPDUbuffer[ISO7816.OFFSET_P1];
        byte bP2 = abAPDUbuffer[ISO7816.OFFSET_P2];
        short sLc = toShort(abAPDUbuffer[ISO7816.OFFSET_LC]);
        short sDataToSend = (short) 0;

        //Get incoming data
        if (sLc != aApdu.setIncomingAndReceive()) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        //Evaluate the CLA byte
        switch (abAPDUbuffer[ISO7816.OFFSET_CLA]) {
        //Get information about the version
        case CLA_CONTROL:
            getCapVersion(aApdu, TB_VERSION, (short)0, (short)TB_VERSION.length);
            break;
        case CLA_PROPRIETARY:
            switch (abAPDUbuffer[ISO7816.OFFSET_INS]) {
            case INS_SELECTSSENTRY:
                sDataToSend = processSelectSSEntry(bP1, bP2, sLc, abAPDUbuffer);
                break;
            case INS_PING:
                //Check if P1 and P2 are 0x00
                if ((bP1 != (byte) 0x00) || (bP2 != 0x00)) {
                    ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
                }
                ISOException.throwIt(ISO7816.SW_NO_ERROR);
                break;
            case INS_GETSSENTRYDATA:
                sDataToSend = (short) processGetSSEntryData(bP1, bP2, abAPDUbuffer);
                break;
            case INS_GETSSENTRYID:
                // buffer[ISO7816.OFFSET_CDATA] contains title
                sDataToSend = processGetSSEntryID(bP1, bP2, sLc, abAPDUbuffer);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
            break;
        // CLA not supported
        default:
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        //Send response APDU
        //The status word is stored in the last 2 bytes
        sDataToSend -= (short) 2;
        short sSW = Util.getShort(abAPDUbuffer, sDataToSend);

        //Send the data
        aApdu.setOutgoingAndSend((short) 0, sDataToSend);

        //Send the SW
        if (sSW != ISO7816.SW_NO_ERROR) {
            ISOException.throwIt(sSW);
        }
    }


    /**
     * Returns the ID associated to the title received.
     * 
     * @param inputTitle buffer containing the title.
     * @param sLc length of the title.
     * @return the ID if found.
     */
    private short getIDFromRegList(byte[] inputTitle, short sLc) {

        if ((Util.arrayCompare(m_RegList[(short) 0].title, (short) 0, inputTitle, ISO7816.OFFSET_CDATA, sLc) == (short) 0)) {
                return 0;
        }
        ISOException.throwIt(SW_REFERENCE_DATA_NOT_FOUND);
        return 0;
    }

    /*******************************************************************************************************
                                        SELECT SS ENTRY
    /*******************************************************************************************************/

    /**
    * Select a SS entry checking if PIN1 is enabled and validated.
    * 
    * @param bP1 Parameter P1 of the incoming APDU
    * @param bP2 Parameter P2 of the incoming APDU
    * @param sLc Parameter LC of the incoming APDU
    * @param abArray Data of the incoming APDU
    * @return
    */
    private short processSelectSSEntry(byte bP1, byte bP2, short sLc, byte[] abArray) {

        //Check P1 and P2 correctness
        if (!((bP1 == P1_SELECT) && (bP2 == (byte) 0x00))) {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        //Check Lc correctness
        if (sLc != (short) 2) {
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }

        //Get ID from CData
        s_selectedID = Util.getShort(abArray, ISO7816.OFFSET_CDATA);

        //Check whether the input ID lies within the available number of entries and the referenced SS entry exists
        if(s_selectedID != (short) 0) {
            ISOException.throwIt(SW_REFERENCE_DATA_NOT_FOUND);
        }

        short sOutputOffset = m_RegList[s_selectedID].tittleOffset;

        //For the assigned ID, the response is created
        Util.arrayCopy(m_RegList[s_selectedID].title, (short) 0, abArray, (short) 0, sOutputOffset);


        //Set the SW in the output buffer
        Util.setShort(abArray, sOutputOffset, ISO7816.SW_NO_ERROR);
        sOutputOffset += (short) 2;

        //Return the total response length: Title + SW
        return sOutputOffset;

    }


    /*******************************************************************************************************
                                        GET SS ENTRY DATA
    /*******************************************************************************************************/

    /**
    * Get data of the SS entry selected, checking if PIN2 is enabled and selected.
    * 
    * @param bP1 Parameter P1 of the incoming APDU
    * @param bP2 Parameter P2 of the incoming APDU
    * @param sLc Parameter LC of the incoming APDU
    * @param abArray Data of the incoming APDU
    * @return Data element
    */

    private short processGetSSEntryData(byte bP1, byte bP2, byte[] abArray) {

        //Check P1 and P2 correctness
        if (!(((bP1 == P1_SELECT) || (bP1 == P1_FIRST)) && (bP2 == (byte) 0x00))) {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        //Check whether the referenced SS entry exists
        if(s_selectedID == (short) -1){
            ISOException.throwIt(SW_REFERENCE_DATA_NOT_FOUND);
        }

        //For the selected SS, the response is created
        short sOutputOffset = (short) 0;

        switch (bP1) {
        case P1_SELECT:
            //Response contains the whole size of data stored
            short responseLength =  m_RegList[(byte) s_selectedID].dataOffset;
            Util.setShort(abArray, sOutputOffset, responseLength);
            sOutputOffset = (short) 2;
            b_Entry_Control = GET_DATA_SIZE;
            break;
        case P1_FIRST:
            //Response contains the first data part m_partialLc
            if(((b_Entry_Control & GET_DATA_SIZE) != GET_DATA_SIZE)) {
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            }
            Util.arrayCopy(m_RegList[(byte) s_selectedID].data, (short) 0, abArray, (short) 0, m_RegList[(byte) s_selectedID].dataOffset);
            sOutputOffset = m_RegList[(byte) s_selectedID].dataOffset;

            b_Entry_Control |= GET_DATA_FIRST;
            break;
        default:
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        //Set the SW in the output buffer
        Util.setShort(abArray, sOutputOffset, ISO7816.SW_NO_ERROR);
        sOutputOffset += (short) 2;

        //Return the total response length: Data length/Data field + SW
        return sOutputOffset;

    }

    /*******************************************************************************************************
                                        GET SS ENTRY ID
    /*******************************************************************************************************/

    /**
    * Get ID of the SS entry corresponding to the provided title, checking if PIN2 is enabled and validated.
    * 
    * @param bP1 Parameter P1 of the incoming APDU
    * @param bP2 Parameter P2 of the incoming APDU
    * @param sLc Parameter LC of the incoming APDU
    * @param abArray Data of the incoming APDU
    * @return ID of the provided title
    */
    private short processGetSSEntryID(byte bP1, byte bP2, short sLc, byte[] abArray) {

        //Check P1 and P2 correctness
        if (!((bP1 == 0x00) && (bP2 == (byte) 0x00))) {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        short id = getIDFromRegList(abArray, sLc);

        Util.setShort(abArray, (short) 0, id);

        //Set the SW in the output buffer
        Util.setShort(abArray, (short) 2, ISO7816.SW_NO_ERROR);

        // Return the total response length: ID + SW
        return (short) 4;
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
        final short sOffset, final short sLength) throws ISOException {

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
        Util.arrayCopy(abVerBuffer, sOffset, abAPDUBuffer, (short) 0, sLength);
        // Send it
        aApdu.setOutgoingAndSend((short) 0, (short) 3);
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
