package com.gieseckedevrient.javacard.fileviewtest;

import com.gieseckedevrient.javacard.fileviewtest.AuthenticationProvider;

import javacard.framework.*;

/**
 *
 */
public final class FileViewApplet extends Applet implements MultiSelectable {

    /**  Interindustry Class */
    private static final byte m_sCLA_00 = (byte) 0x00;

    /** CLA used to enable or disable the command access */
    private static final byte CLA_ENABLE_DISABLE = (byte) 0xA0;

     /** INS_READ_BINARY_SFID */
    private static final byte m_sINS_READ_BINARY = (byte) 0xB0;

    /** INS_WRITE_BINARY_SFID */
    private static final byte m_sINS_UPDATE_BINARY = (byte) 0xD6;

    /** INS_READ_RECORD */
    private static final byte m_sINS_READ_RECORD = (byte) 0xB2;

    /** INS_WRITE_RECORD */
    private static final byte m_sINS_UPDATE_RECORD = (byte) 0xDC;

    /** INS_APPEN_RECORD */
    private static final byte m_sINS_APPEND_RECORD = (byte) 0xE2;

    /** INS_SEARCH_RECORD */
    private static final byte m_sINS_SEARCH_RECORD = (byte) 0xA2;

    /** INS of Multi-Level Commands */
    private static final byte m_sINS_SELECT = (byte) 0xA4;

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

    /**Object PIN1.*/
    private static AuthenticationProvider oPIN1 = null;

    /** m_sTRY_LIMIT **/
    private static final byte m_sTRY_LIMIT = (byte) 0x03;

    /** m_SMAX_PIN_TRY_COUNTER **/
    private static final byte m_SMAX_PIN_TRY_COUNTER = (byte) 0x10;

    /** m_BLOCKED **/
    private static final boolean m_BLOCKED = true;

    /** m_SW_PIN_BLOCKED **/
    private static final short m_SW_PIN_BLOCKED = (short) 0x6984;

    /** m_SW_COMMAND_INCOMPATIBLE_WITH_FILE_SYSTEM **/
    private static final short m_SW_COMMAND_INCOMPATIBLE_WITH_FILE_SYSTEM = (short) 0x6981;

    /** m_SW_RECORD_NOT_FOUND **/
    private static final short m_SW_RECORD_NOT_FOUND = (short) 0x6983;

    /** m_sDEFAULT_PIN **/
    private static final byte[] m_sDEFAULT_PIN = {(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08,(byte)0x09, (byte)0x0A,
                                                  (byte)0x0B, (byte)0x0C,(byte)0x0D, (byte)0x0E,(byte)0x0F};

    /** File system reference */
    public static BasicFile[] cfFileSystem;

    /** Number of files created in cfFileSystem */
    private static short sNumFiles = (short)0;

    /** Max Number of files created in cfFileSystem */
    private static final byte bMaxNumFiles = (byte)0x0A;

    /*** Current FileID */
    private short s_CurrentFileID = (short) 0;

    /** Current Record */
    private short s_CurrentRecord = (short) 0;

    /*** Status word of commands */
    short statusWord;

    /** Auxiliary buffer */
    private byte[] ab_String ;

    /** Max number of records in each file */
    private static final byte MAX_RECORDS = (byte) 0x04;

    /** Length of each Record */
    private static final short RECORDS_LENGTH = (short) 10;

    /** Set access command to enable*/
    private static final byte COMMAND_ACCESS_ENABLE = (byte) 0x01;

    /** Set access command to disable*/
    private static final byte COMMAND_ACCESS_DISABLE = (byte) 0x00;

    /** Indicates if the access to the command is enable or disable.*/
    private static byte b_CommandAccess = COMMAND_ACCESS_ENABLE;

    /**
     * Constructor of the Applet. Creates objects, allocates RAM buffers,
     * initializes global variables, ...
     */
    private FileViewApplet() {
        createFiles ();
        oPIN1 = new AuthenticationProvider(m_sTRY_LIMIT, (byte) m_sDEFAULT_PIN.length);
        ab_String = JCSystem.makeTransientByteArray((short) 20, JCSystem.CLEAR_ON_DESELECT);
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
        FileViewApplet applet = new FileViewApplet();

        applet.register(abInstallData, (short) (sOffset + (short) 1),
                        abInstallData[sOffset]);

    }

    public static void createFiles () {
        
        byte[] dataA = {(byte) 0xA0, (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0xA6, (byte) 0xA7, (byte) 0xA8, (byte) 0xA9};
        byte[] dataB = {(byte) 0xB0, (byte) 0xB1, (byte) 0xB2, (byte) 0xB3, (byte) 0xB4, (byte) 0xB5, (byte) 0xB6, (byte) 0xB7, (byte) 0xB8, (byte) 0xB9};
        byte[] dataC = {(byte) 0xC0, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4, (byte) 0xC5, (byte) 0xC6, (byte) 0xC7, (byte) 0xC8, (byte) 0xC9};
        byte[] dataD = {(byte) 0xD0, (byte) 0xD1, (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6, (byte) 0xD7, (byte) 0xD8, (byte) 0xD9};
        byte[] dataE = {(byte) 0xE0, (byte) 0xE1, (byte) 0xE2, (byte) 0xE3, (byte) 0xE4, (byte) 0xE5, (byte) 0xE6, (byte) 0xE7, (byte) 0xE8, (byte) 0xE9};
        byte[] dataF = {(byte) 0xF0, (byte) 0xF1, (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xF5, (byte) 0xF6, (byte) 0xF7, (byte) 0xF8, (byte) 0xF9};
        cfFileSystem = new BasicFile[bMaxNumFiles];

        DirectoryFolder masterFile = new DirectoryFolder(BasicFile.MF_FILEID, (byte) 0x000A, BasicFile.NO_SECURE_FILE, BasicFile.MF_FILEID);
        cfFileSystem[sNumFiles] = masterFile;
        sNumFiles++;

        DirectoryFolder secureDF = new DirectoryFolder((short) 0x1000, (byte) 0x000A, BasicFile.SECURE_FILE, BasicFile.MF_FILEID);
        cfFileSystem[sNumFiles] = secureDF;
        masterFile.addFileToDF(secureDF.getFID());
        sNumFiles++;

        cfFileSystem[sNumFiles] = new BinaryFile((short) 0x1001, (byte) 0x0B, (short) 0x000A, BasicFile.SECURE_FILE, secureDF.getFID());
        setValueToBinaryFile(cfFileSystem[sNumFiles] , (short) 0x00, dataA);
        secureDF.addFileToDF(cfFileSystem[sNumFiles].getFID());
        sNumFiles++;

        cfFileSystem[sNumFiles] =  new RecordFile((short) 0x1002, (byte) 0x0C, MAX_RECORDS, RECORDS_LENGTH, BasicFile.SECURE_FILE, secureDF.getFID());
        setValueToRecordFile(cfFileSystem[sNumFiles], (byte) 0x01, (short) 0x00, dataC);
        secureDF.addFileToDF(cfFileSystem[sNumFiles].getFID());
        sNumFiles++;

        cfFileSystem[sNumFiles] =  new CyclicFile((short) 0x1003, (byte) 0x0D, MAX_RECORDS, RECORDS_LENGTH, BasicFile.SECURE_FILE, secureDF.getFID());
        setValueToCyclicFile(cfFileSystem[sNumFiles], (short) 0x00, dataE);
        secureDF.addFileToDF(cfFileSystem[sNumFiles].getFID());
        sNumFiles++;

        DirectoryFolder noSecureDF = new DirectoryFolder((short) 0x2000, (byte) 0x000A, BasicFile.NO_SECURE_FILE, BasicFile.MF_FILEID);
        cfFileSystem[sNumFiles] = noSecureDF;
        masterFile.addFileToDF(noSecureDF.getFID());
        sNumFiles++;       

        cfFileSystem[sNumFiles] = new BinaryFile((short) 0x2001, (byte) 0x15, (short) 0x000A, BasicFile.NO_SECURE_FILE, noSecureDF.getFID());
        setValueToBinaryFile(cfFileSystem[sNumFiles] , (short) 0x00, dataB);
        noSecureDF.addFileToDF(cfFileSystem[sNumFiles].getFID());
        sNumFiles++;

        cfFileSystem[sNumFiles] =  new RecordFile((short) 0x2002, (byte) 0x16, MAX_RECORDS, RECORDS_LENGTH, BasicFile.NO_SECURE_FILE, noSecureDF.getFID());
        setValueToRecordFile(cfFileSystem[sNumFiles], (byte) 0x01, (short) 0x00, dataD);
        noSecureDF.addFileToDF(cfFileSystem[sNumFiles].getFID());
        sNumFiles++;

        cfFileSystem[sNumFiles] =  new CyclicFile((short) 0x2003, (byte) 0x17, MAX_RECORDS, RECORDS_LENGTH, BasicFile.NO_SECURE_FILE, noSecureDF.getFID());
        setValueToCyclicFile(cfFileSystem[sNumFiles], (short) 0x00, dataF);
        noSecureDF.addFileToDF(cfFileSystem[sNumFiles].getFID());
        sNumFiles++;
    }

    public boolean select() {
        s_CurrentFileID = (short)0;
        return true;
    }

    public boolean select(boolean appInstAlreadyActive) {
        return select();
    }

    public void deselect(boolean appInstStillActive) {
        deselect();
    }

    private static void setValueToBinaryFile(BasicFile file, short fileOffset, byte[] data) {
        ((BinaryFile) file).writeBinary(fileOffset, data, (short) 0x00, (short) data.length);
    }

    private static void setValueToRecordFile(BasicFile file, byte recordNumber, short fileOffset, byte[] data) {
        ((RecordFile) file).updateRecord(recordNumber, data, fileOffset, (short) data.length);
    }

    private static void setValueToCyclicFile (BasicFile file, short fileOffset, byte[] data) {
        ((CyclicFile) file).appendRecord(data, fileOffset, (short) data.length);
    }

    /**
     * This method must be implemented by applets. It is declared as abstract in
     * class Applet.
     *
     * @param   aApdu  the incoming APDU object.
     */
    public void process(APDU aApdu) {
        AuthenticationProvider oPIN = oPIN1;

        if (selectingApplet()) {
            return;
        }
        //Gets the APDU Buffer
        byte[] buffer = aApdu.getBuffer();

        //Allow the connection from all the different logical channels. The logical channel used is determined
        //by the two LSB of the CLA byte.
        buffer[ISO7816.OFFSET_CLA] = (byte) (buffer[ISO7816.OFFSET_CLA] & (byte) 0xFC);

        byte bP1 = buffer[ISO7816.OFFSET_P1];
        byte bP2 = buffer[ISO7816.OFFSET_P2];
        byte bLc = buffer[ISO7816.OFFSET_LC];
        short sLe = (short) 0;

        // Total command length
        short commandLength = toShort(ISO7816.OFFSET_CDATA);
        // Total response length
        short responseLength = (short) 0;

        // Add data length to the total
        if((buffer[ISO7816.OFFSET_INS] != m_sINS_READ_RECORD) && (buffer[ISO7816.OFFSET_INS] != m_sINS_READ_BINARY) && (buffer[ISO7816.OFFSET_INS] != CLA_ENABLE_DISABLE)) {
            commandLength += aApdu.setIncomingAndReceive();
        }

        if (commandLength != (short) 5) {
            bLc = buffer[ISO7816.OFFSET_LC];
            sLe = toShort(buffer[(byte) (bLc + ISO7816.OFFSET_CDATA)]);
        } else {
            bLc = (byte) 0;
            sLe = toShort(buffer[ISO7816.OFFSET_LC]);
        }

        statusWord = ISO7816.SW_NO_ERROR;
        try {
            //Evaluate the CLA byte
            switch (buffer[ISO7816.OFFSET_CLA]){
            case CLA_ENABLE_DISABLE:
                v_EnableCommand(bP1, bP2);
                break;
            case m_sCLA_00:
                //Check if the command access is enable or disable
                if (b_CommandAccess == COMMAND_ACCESS_DISABLE) {
                    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                } else {
                    switch (buffer[ISO7816.OFFSET_INS]){
                    case ISO7816.INS_SELECT:
                        responseLength = processSelectEF(bP1, bP2, bLc, buffer);
                        break;
                    case m_sINS_READ_BINARY:
                        responseLength = processReadBinary(bP1, bP2, buffer, sLe);
                        break;
                    case m_sINS_UPDATE_BINARY:
                        processWriteBinary(bP1, bP2, bLc, buffer);
                        break;
                    case m_sINS_READ_RECORD:
                        responseLength = processReadRecord(bP1, bP2, buffer, sLe);
                        break;
                    case m_sINS_UPDATE_RECORD:
                        processUpdateRecord(bP1, bP2, bLc, buffer);
                        break;
                    case m_sINS_APPEND_RECORD:
                        processAppendRecord(bP1, bP2, bLc, buffer);
                        break;
                    case m_sINS_SEARCH_RECORD:
                        responseLength = processSearchRecord(bP1, bP2, bLc, buffer);
                        break;
                    case INS_VERIFY_20:
                        //Check if P2 indicates PIN1 or PIN2
                        if (((((byte) (bP2 & (byte) 0x1F)) != 0x01) && ((byte) (bP2 & (byte) 0x1F) != 0x02))) {
                            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
                        }
                        if (((byte) (bP2 & (byte) 0x1F)) == 0x02) oPIN = oPIN1;
                        oPIN.v_ProcessVerifyTag20(bP1, bP2, bLc, buffer);
                        break;
                    case INS_RESET_RETRY_COUNTER:
                        //Check if P2 indicates PIN1 or PIN2
                        if (((((byte) (bP2 & (byte) 0x1F)) != 0x01) && ((byte) (bP2 & (byte) 0x1F) != 0x02))) {
                            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
                        }
                        if (((byte) (bP2 & (byte) 0x1F)) == 0x02) oPIN = oPIN1;
                        oPIN.v_ProcessReset(bP1, bP2, bLc, buffer);
                        break;
                    case INS_CHANGE_PIN:
                        //Check if P2 indicates PIN1 or PIN2
                        if (((((byte) (bP2 & (byte) 0x1F)) != 0x01) && ((byte) (bP2 & (byte) 0x1F) != 0x02))) {
                            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
                        }
                        if (((byte) (bP2 & (byte) 0x1F)) == 0x02) oPIN = oPIN1;
                        oPIN.v_ChangePIN(bP1, bP2, bLc, buffer);
                        break;
                    case INS_ENABLE_VERIFICATION:
                        //Check if P2 indicates PIN1 or PIN2
                        if (((((byte) (bP2 & (byte) 0x1F)) != 0x01) && ((byte) (bP2 & (byte) 0x1F) != 0x02))) {
                            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
                        }
                        if (((byte) (bP2 & (byte) 0x1F)) == 0x02) oPIN = oPIN1;
                        oPIN.v_EnablePIN(bP1, bP2, bLc, buffer);
                        break;
                    case INS_DISABLE_VERIFICATION:
                        //Check if P2 indicates PIN1 or PIN2
                        if (((((byte) (bP2 & (byte) 0x1F)) != 0x01) && ((byte) (bP2 & (byte) 0x1F) != 0x02))) {
                            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
                        }
                        if (((byte) (bP2 & (byte) 0x1F)) == 0x02) oPIN = oPIN1;
                        oPIN.v_DisablePIN(bP1, bP2, bLc, buffer);
                        break;
                    default:
                        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                        break;
                    }
                }
                break;
            //Not supported
            default:
                ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
                break;
            }
        } catch (ISOException e) {
            statusWord = e.getReason();
        } 

        // Send output data if appropriate
        if (responseLength > 0) {
            if ((responseLength < sLe) || (sLe == (byte)0x00)) {
                aApdu.setOutgoingAndSend((short) 0, responseLength);
            } else {
                aApdu.setOutgoingAndSend((short) 0, sLe);
            }
        }

        if (statusWord != ISO7816.SW_NO_ERROR) {
            ISOException.throwIt(statusWord);
        }
    }


    /**
     * Enable or disable the command access.
     * 
     * @param bP1 Parameter P1 of the incoming APDU
     * @param bP2 Parameter P2 of the incoming APDU
     */
    private void v_EnableCommand(byte bP1, byte bP2) {

        //Check if P1 and P2 are corrects
        //P1 = 00
        //P2 = 00 --> enable
        //P2 = 01 --> disable
        if ((bP1 != 0x00) || ((bP2 != 0x00) && (bP2 != 0x01))) {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        if (bP2 == 0x00) {
            b_CommandAccess = COMMAND_ACCESS_ENABLE;
        } else {
            b_CommandAccess = COMMAND_ACCESS_DISABLE;
        }
    }

    public short processSelectEF(byte bP1, byte bP2, byte bLc, byte[] bArray) {
        short sLength = (short)0;

        BasicFile basicFile;
        short tempSelectionFile;

        if ((oPIN1.b_Enable == AuthenticationProvider.PIN_ENABLE) && (!oPIN1.z_Validated())) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        // Check P2 correctness
        // P2 = 0b00000100 means return FCP template
        if (bP2 != (byte) 0x04){
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        if((oPIN1.b_Enable == AuthenticationProvider.PIN_ENABLE) && (!oPIN1.z_Validated())){
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        //P1 = 00 -> Lc has to be 2 bytes (FileID)
        switch (bP1) {
            case (byte) 0x00:
                if (bLc == (byte) 0x00){
                    s_CurrentFileID = (short) 0;
                    return sLength;
                } else if (bLc != (byte) 0x02){
                    ISOException.throwIt(ISO7816.SW_WRONG_DATA);
                }

                //Good case: Data Commands has a FileID
                for (short i = 0; i < sNumFiles; i++) {
                    if (cfFileSystem[i].getFID() == Util.getShort(bArray, ISO7816.OFFSET_CDATA)) {
                        s_CurrentFileID = cfFileSystem[i].getFID();

                        basicFile = getBasicFileByFileID(s_CurrentFileID);
                        sLength = basicFile.getFCP(bArray, (short) (sLength + 2));
                        return sLength;
                    }
                }

                ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
                break;
            // Select Parent
            case (byte) 0x03:
                if(s_CurrentFileID == (short) 0x00) {
                    ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
                } else {
                     for (short i = 0; i < sNumFiles; i++) {
                        if (cfFileSystem[i].getFID() == s_CurrentFileID) {
                            if(cfFileSystem[i].getFileType() == BasicFile.FILETYPE_DF) {
                                s_CurrentFileID = cfFileSystem[i].getM_sParentID();
                            } else {
                                for (short j = 0; j < sNumFiles; j++) {
                                    if (cfFileSystem[j].getFID() == cfFileSystem[i].getM_sParentID()) {
                                        s_CurrentFileID = cfFileSystem[j].getM_sParentID();
                                        break;
                                    }
                                }
                            }
                            basicFile = getBasicFileByFileID(s_CurrentFileID);
                            sLength = basicFile.getFCP(bArray, (short) (sLength + 2));
                            return sLength;
                        }
                    }
                }
                break;
            case (byte) 0x08:
                // Select by path start on MF.

                basicFile = (BasicFile) getBasicFileByFileID(BasicFile.MF_FILEID);
                tempSelectionFile = Util.getShort(bArray, ISO7816.OFFSET_CDATA);

                if(!basicFile.isFileOnCurrentDF(tempSelectionFile)) {
                    ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
                }

                while (toShort(bLc) > sLength) {
                    for (short i = 0; i < sNumFiles; i++) {
                        if (tempSelectionFile == cfFileSystem[i].getFID()) {
                            basicFile = (BasicFile) getBasicFileByFileID(cfFileSystem[i].getFID());

                            if(basicFile.getFileType() != BasicFile.FILETYPE_DF) {
                                sLength = (short) (sLength + 2);
                                if((sLength != toShort(bLc))) {
                                    ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
                                } else {
                                    s_CurrentFileID = basicFile.getFID();
                                    sLength = basicFile.getFCP(bArray, sLength);
                                    break;
                                }
                            } else {
                                sLength = (short) (sLength + 2);
                                if((sLength != toShort(bLc))) {
                                    if(!basicFile.isFileOnCurrentDF(Util.getShort(bArray, (short) ((short) ISO7816.OFFSET_CDATA + sLength)))) {
                                        ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
                                    } else {
                                        if(sLength != toShort(bLc)) {
                                            tempSelectionFile = Util.getShort(bArray, (short) ((short) ISO7816.OFFSET_CDATA + sLength));
                                        }
                                    }
                                } else {
                                    s_CurrentFileID = basicFile.getFID();
                                    sLength = basicFile.getFCP(bArray, sLength);
                                    break;
                                }
                            }
                        }
                    }
                }

                break;
            case (byte) 0x09:
                if(s_CurrentFileID == (short) 0x00) {
                    s_CurrentFileID = BasicFile.MF_FILEID;
                    basicFile = getBasicFileByFileID(BasicFile.MF_FILEID);
                } else {
                    basicFile = (BasicFile) getBasicFileByFileID(s_CurrentFileID);
                }

                tempSelectionFile = Util.getShort(bArray, ISO7816.OFFSET_CDATA);

                if(!basicFile.isFileOnCurrentDF(tempSelectionFile)) {
                    ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
                }

                while(toShort(bLc) > sLength) {
                    for (short i = 0; i < sNumFiles; i++) {
                        if (tempSelectionFile == cfFileSystem[i].getFID()) {
                            basicFile = (BasicFile) getBasicFileByFileID(cfFileSystem[i].getFID());

                            if(basicFile.getFileType() != BasicFile.FILETYPE_DF) {
                                sLength = (short) (sLength + 2);
                                if((sLength != toShort(bLc))) {
                                    ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
                                } else {
                                    s_CurrentFileID = basicFile.getFID();
                                    sLength = basicFile.getFCP(bArray, sLength);
                                    break;
                                }
                            } else {
                                sLength = (short) (sLength + 2);
                                if((sLength != toShort(bLc))) {
                                    if(!basicFile.isFileOnCurrentDF(Util.getShort(bArray, (short) (toShort(ISO7816.OFFSET_CDATA) + sLength)))) {
                                        ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);  
                                    } else {
                                        if(sLength != toShort(bLc)) {
                                            tempSelectionFile = Util.getShort(bArray, (short) (toShort(ISO7816.OFFSET_CDATA) + sLength));
                                        }
                                    }
                                } else {
                                    s_CurrentFileID = basicFile.getFID();
                                    sLength = basicFile.getFCP(bArray, sLength);
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                break;
        }
        return sLength;
    }

    /**
     * The response includes (part of) the content of a binary EF.
     * 
     * @param bP1 Parameter P1 of command
     * @param bP2 Parameter P2 of command
     * @param bLc Length of command data
     * @param bArray buffer where the input data is stored
     * @return Length of data stored in bArray
     */
    public short processReadBinary(byte bP1, byte bP2, byte[] bArray, short sLe) {
        short sLength = (short)0;
        short sOffset = (short)0;

        //Check P1 and P2 correctness
        if ((bP1 & (byte) 0x80) == (byte) 0x80){
            //P1[7:6] = 00b (RFU)
            if ((bP1 & (byte) 0x60) != (byte) 0x00){
                // Incorrect parameters P1-P2
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            }
            //P1[8] = 1b -> P1[5:1] = SFID & P2[8:1] = Offset (from 0 to 255)
            short sCurrentSFI = toShort((byte) (bP1 & (byte) 0x1F));
            sOffset = toShort(bP2);
            short i = 0;
            for (; i < sNumFiles; i++) {
                if (cfFileSystem[i].getSFI() == sCurrentSFI) {
                    s_CurrentFileID = cfFileSystem[i].getFID();
                    break;
                }
            }
            if (i == sNumFiles) {
                ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
            }
        } else {
            //P1[8] = 1b ??? (P1[7:1] || P2[8:1]) = Offset (from 0 to 32767)
            sOffset = (short) (((short) ((short) (toShort(bP1) & (short) 0x007F) << 8)) | toShort(bP2));
            if(s_CurrentFileID == (short) 0) {
                ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
            }
        }

        BasicFile CipBinaryFile = getBasicFileByFileID(s_CurrentFileID);
        if (!(CipBinaryFile instanceof BinaryFile)) {
            ISOException.throwIt(m_SW_COMMAND_INCOMPATIBLE_WITH_FILE_SYSTEM);
        }

        if(CipBinaryFile.isFileSecure() == BasicFile.SECURE_FILE) {
            if((oPIN1.b_Enable == AuthenticationProvider.PIN_ENABLE) && (!oPIN1.z_Validated())) {
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }
        }

        //ReadBinary
        sLength = ((BinaryFile)(CipBinaryFile)).readBinary(sOffset, bArray, (short) 0, sLe);

        return sLength;
    }

    /**
     * This command writes the content of the command data field into the binary EF
     * 
     * @param bP1 Parameter P1 of command
     * @param bP2 Parameter P2 of command
     * @param bLc Length of command data
     * @param bArray buffer where the input data is stored
     * @return Length of data stored in bArray
     */
    public void processWriteBinary(byte bP1, byte bP2, byte bLc, byte[] bArray) {
        short sOffset = (short) 0;

        //Check P1 and P2 correctness
        if ((bP1 & (byte) 0x80) == (byte) 0x80){
            //P1[7:6] = 00b (RFU)
            if ((bP1 & (byte) 0x60) != (byte) 0x00){
                //6A86 Incorrect parameters P1-P2
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            }
            //P1[8] = 1b -> P1[5:1] = SFID & P2[8:1] = Offset (from 0 to 255)
            short sCurrentSFI = toShort((byte) (bP1 & (byte)0x1F));
            short i = 0;
            for (; i < sNumFiles; i++) {
                if (cfFileSystem[i].getSFI() == sCurrentSFI) {
                    s_CurrentFileID = cfFileSystem[i].getFID();
                    break;
                }
            }
            if (i == sNumFiles) {
                ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
            }

            sOffset = toShort(bP2);

        } else {
            //P1[8] = 1b ??? (P1[7:1] || P2[8:1]) = Offset (from 0 to 32767)
            sOffset = (short) (((short) ((short) (toShort(bP1) & (short) 0x007F) << 8)) | toShort(bP2));
            if(s_CurrentFileID == (short) 0) {
                ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
            }
        }

        BasicFile CipBinaryFile = getBasicFileByFileID(s_CurrentFileID);

        if(CipBinaryFile.isFileSecure() == BasicFile.SECURE_FILE) {
            if((oPIN1.b_Enable == AuthenticationProvider.PIN_ENABLE) && (!oPIN1.z_Validated())) {
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }
        }

        if (!(CipBinaryFile instanceof BinaryFile)) {
            ISOException.throwIt(m_SW_COMMAND_INCOMPATIBLE_WITH_FILE_SYSTEM);
        }

        //UpdateBinary
        ((BinaryFile)(CipBinaryFile)).writeBinary(sOffset, bArray, ISO7816.OFFSET_CDATA, toShort(bLc));

    }

    /**
     * Returns (part of) the contents of the referenced record(s) in the referenced linear record, cyclic record or value-record EF.
     * 
     * @param bP1 Parameter P1 of command
     * @param bP2 Parameter P2 of command
     * @param bLc Length of command data
     * @param bArray buffer where the input data is stored
     * @return Length of data stored in bArray
     */
    public short processReadRecord(byte bP1, byte bP2, byte[] bArray, short sLe) {
        short sLength = (short) 0;

        // P1 = 0x00 means current record.
        if (bP1  == (byte) 0x00){
            // TODO: implement this
            ISOException.throwIt(m_SW_RECORD_NOT_FOUND);
        }

        // P2[8:4] contains SFI
        if((bP2 & 0xF8) != (byte) 0x00) {
            // If SFI != 0, update current file
            short sCurrentSFI = toShort((byte) (((bP2 & (byte) 0xF8) >> 3) & ((byte) 0x1F)));
            short i = 0;
            for (; i < sNumFiles; i++) {
                if (cfFileSystem[i].getSFI() == sCurrentSFI) {
                    s_CurrentFileID = cfFileSystem[i].getFID();
                    break;
                }
            }

            if (i == sNumFiles) {
                ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
            }
        }

        // P2[3:1] = 100b -> Update record referenced in P1.
        if ((bP2 & (byte) 0x07) != (byte) 0x04){
            // Incorrect parameters P1-P2
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        if(s_CurrentFileID == (short) 0) {
            ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
        }

        BasicFile CipRecordFile = getBasicFileByFileID(s_CurrentFileID);
        if (!(CipRecordFile instanceof RecordFile)) {
            ISOException.throwIt(m_SW_COMMAND_INCOMPATIBLE_WITH_FILE_SYSTEM);
        }

        if(CipRecordFile.isFileSecure() == BasicFile.SECURE_FILE) {
            if((oPIN1.b_Enable == AuthenticationProvider.PIN_ENABLE) && (!oPIN1.z_Validated())) {
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }
        }

        // ReadRecord
        sLength += ((RecordFile)(CipRecordFile)).readRecord(toShort(bP1), bArray, (short) 0, sLe);

        return sLength;
    }

    /***
     * Updates the contents of the referenced record in the referenced linear record, cyclic record or value-record
     * EF with the data in the command data field. 
     * 
     * @param bP1 Parameter P1 of command
     * @param bP2 Parameter P2 of command
     * @param bLc Length of command data
     * @param bArray buffer where the input data is stored
     * @return Length of data stored in bArray
     */
    public void processUpdateRecord(byte bP1, byte bP2, byte bLc, byte[] bArray) {

        // P2[8:4] contains SFI
        if((bP2 & 0xF8) != (byte) 0x00) {
            //SFI is not the current
            short sCurrentSFI = toShort((byte) (((bP2 & (byte) 0xF8) >> 3) & ((byte) 0x1F)));
            SearchSFI(sCurrentSFI);
        }

        // P2[3:1] = 100b -> Update record referenced in P1.
        if ((bP2 & (byte) 0x07) != (byte) 0x04){
            // Incorrect parameters P1-P2
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }

        if(s_CurrentFileID == (short) 0) {
            ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
        }

        BasicFile CipRecordFile = getBasicFileByFileID(s_CurrentFileID);
        if (!(CipRecordFile instanceof RecordFile)) {
            ISOException.throwIt(m_SW_COMMAND_INCOMPATIBLE_WITH_FILE_SYSTEM);
        }

        if(CipRecordFile.isFileSecure() == BasicFile.SECURE_FILE) {
            if((oPIN1.b_Enable == AuthenticationProvider.PIN_ENABLE) && (!oPIN1.z_Validated())) {
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }
        }

        if (bP1 != (byte) 0x00) {
            s_CurrentRecord = toShort(bP1);
        }

        ((RecordFile)(CipRecordFile)).updateRecord(s_CurrentRecord, bArray, ISO7816.OFFSET_CDATA, toShort(bLc));

    }

    /**
     * Append the contents of the referenced record in the referenced cyclic record 
     * EF with the data in the command data field. 
     * 
     * @param bP1 Parameter P1 of command
     * @param bP2 Parameter P2 of command
     * @param bLc Length of command data
     * @param abArray buffer where the input data is stored
     * @return Length of data stored in abArray
     */
    public void processAppendRecord(byte bP1, byte bP2, byte bLc, byte[] abArray) {

        //Check if bP1 is 0x00
        if (bP1 != (byte) 0x00) {
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        }

        //Check if bP2 means current EF or new EF
        if ((bP2 & (byte) 0x07) != (byte) 0x00) {
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2); //Check if bP2[2:0] == '000'
        }
        if (bP2 != (byte) 0x00) {
            //SFI is not the current
            short sCurrentSFI = toShort((byte) (((bP2 & (byte) 0xF8) >> 3) & ((byte) 0x1F)));
            SearchSFI(sCurrentSFI);
        }

        //SFI is selected
        if(s_CurrentFileID == (short) 0) {
            ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
        }

        BasicFile CipRecordFile = getBasicFileByFileID(s_CurrentFileID);
        if (!(CipRecordFile instanceof CyclicFile)) {
            ISOException.throwIt(m_SW_COMMAND_INCOMPATIBLE_WITH_FILE_SYSTEM);
        }

        if(CipRecordFile.isFileSecure() == BasicFile.SECURE_FILE) {
            if((oPIN1.b_Enable == AuthenticationProvider.PIN_ENABLE) && (!oPIN1.z_Validated())) {
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }
        }

        s_CurrentRecord = ((CyclicFile)(CipRecordFile)).appendRecord(abArray, ISO7816.OFFSET_CDATA, toShort(bLc));

    }

    /***
     * Updates the contents of the referenced record in the referenced linear record, cyclic record or value-record
     * EF with the data in the command data field. 
     * 
     * @param bP1 Parameter P1 of command
     * @param bP2 Parameter P2 of command
     * @param bLc Length of command data
     * @param bArray buffer where the input data is stored
     * @return Length of data stored in bArray
     */
    public short processSearchRecord(byte bP1, byte bP2, byte bLc, byte[] abArray) {

        byte bOffsetRecord = (byte) 0x00;
        byte bSearchRecord = bP1;
        boolean zFirsRecord = false;

        //Check if bP2 is correct. bP2[2:0] = '100'
        if ((bP2 & (byte) 0x07) != 0x04) {
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        }

        // P1 != 0x00 means not current record. Only searches with record number
        if (bP1 != (byte) 0x00) {

            // P2[8:4] contains SFI
            if((byte) (bP2 & 0xF8) != (byte) 0x00) {
                //SFI is not the current
                short sCurrentSFI = toShort((byte) (((bP2 & (byte) 0xF8) >> 3) & ((byte) 0x1F)));
                SearchSFI(sCurrentSFI);
            }

            s_CurrentRecord = toShort(bSearchRecord);
        }

        //SFI found
        BasicFile CipRecordFile = getBasicFileByFileID(s_CurrentFileID);
        //Check if it's a Cyclic File and the record selected doesn't exist
        if ((CipRecordFile instanceof CyclicFile)) {
            if (((CyclicFile) (CipRecordFile)).m_sRecordCount < bSearchRecord) {
                ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
            }
        }

        if(CipRecordFile instanceof BinaryFile) {
                ISOException.throwIt(m_SW_COMMAND_INCOMPATIBLE_WITH_FILE_SYSTEM);
        }

        if(CipRecordFile.isFileSecure() == BasicFile.SECURE_FILE) {
            if((oPIN1.b_Enable == AuthenticationProvider.PIN_ENABLE) && (!oPIN1.z_Validated())) {
                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
            }
        }

        //Check if the length is correct
        if ((bLc < (byte) 0x01) || (bLc > RECORDS_LENGTH)) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        //Get the data of the record string to search
        Util.arrayCopy(abArray, toShort(ISO7816.OFFSET_CDATA), ab_String, (short) 0, toShort(bLc));

        for (short i = toShort(bSearchRecord); i <= toShort(MAX_RECORDS); i++) {
            //Read the record
            ((RecordFile) (CipRecordFile)).readRecord(i, ab_String, (short) RECORDS_LENGTH, RECORDS_LENGTH);
            for (short j = 0; j <= ((short) (RECORDS_LENGTH - toShort(bLc))); j++) {
                if (Util.arrayCompare(ab_String, (short) 0, ab_String, (short) (RECORDS_LENGTH + j), toShort(bLc)) == (short) 0) {
                    abArray[bOffsetRecord ++] = (byte) i;
                    //Set the CurrentRecord to the first record found, according to ISO/IEC FDIS 7816-4:2004 
                    if (!zFirsRecord) { 
                        zFirsRecord = true;
                        s_CurrentRecord = i;
                    }
                    break;

                }
            }
        }

        return toShort(bOffsetRecord);
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
        // return ((Shareable) this);
        return null;
    }

    /**
     * Safe conversion from unsigned byte to unsigned short.
     *
     * @param bByte byte to be converted
     * @return value converted to short
     */
    private static short toShort(final byte bByte) {
        return (short) ((short) 0x00FF & bByte);
    }

    /**
     * Search a file with specific FileID under ADF
     * 
     * @param sNewFileID Specific FileID
     * @return Data object of file with the specific FileID
     */
    private BasicFile getBasicFileByFileID(short sNewFileID) {
        for (short i = 0; i < sNumFiles; i++) {
            if (cfFileSystem[i].getFID() == sNewFileID) {
                return cfFileSystem[i];
            }
        }
        ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
        return cfFileSystem[(short) 0];
    }

    /**
     * Check the correctness value of Le parameter
     * 
     * @param sLe Maximum length of data expected in response
     */
    public short checkValidLe(short sLenResp, short sLe) {
        if (sLenResp > sLe) {
            return sLe;
        }
        return sLenResp;
    }

    /**
     * Search and select the SFI received
     * 
     * @param sCurrentSFI SFI to be selected
     */
    private void SearchSFI (short sCurrentSFI) {
        short i = 0;
        for (; i < sNumFiles; i++) {
            if (cfFileSystem[i].getSFI() == sCurrentSFI) {
                s_CurrentFileID = cfFileSystem[i].getFID();
                break;
            }
        }

        if (i == sNumFiles) {
            ISOException.throwIt(ISO7816.SW_FILE_NOT_FOUND);
        }
    }
}