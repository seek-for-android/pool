package com.gieseckedevrient.javacard.fileviewtest;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

public class RecordFile extends BasicFile {

    /** Size of one single record. */
    protected short m_sRecordSize;

    /**
     * Number of actual created records. 
     * The maximum number of possible record can be retrieved by
     * m_abRecordData.length / m_sRecordSize
     */
    protected short m_sRecordCount;

    /**
     * Constructor.
     * 
     * @param sRecordCount maximum number of records in file.
     * @param sRecordSize size of one single record
     */
    public RecordFile(short sFID, byte sSFI, short sRecordCount, short sRecordSize, boolean secure, short parentID) {
        super(sFID, sSFI, (short) (sRecordCount * sRecordSize), FILETYPE_EF_LINEAR_FIX, secure, parentID);
        m_sRecordCount = sRecordCount;
        m_sRecordSize = sRecordSize;
    }

    /**
     * Constructor for CyclicFile.
     * 
     * @param sFID File Identifier
     * @param sSFI Short File Identifier
     * @param size of the file
     * @param type Indicate if the file is cyclic or fix.
     * @param secure Security of the file
     * @param sParentID Folder which is related to. 
     */
    public RecordFile(short sFID, byte sSFI, short sRecordCount, short sRecordSize, byte type, boolean secure, short sParentID) {
        super(sFID, sSFI, (short) (sRecordCount * sRecordSize), type, secure, sParentID);
        m_sRecordCount = sRecordCount;
        m_sRecordSize = sRecordSize;
    }


    /**
     * Checks if the record number is in the valid range
     * 
     * @param sRecord record number.
     */
    public void checkRecordNumber(short sRecord) {
        // check upper and lower bound of record number
        if ((sRecord < 1) || (sRecord > getRecordCount())) {
            // return reason code 6A83
            ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
        }
    }

    /**
     * Getter for the Record Size attribute.
     *
     * @return value of Record Size
     */
    public short getRecordSize() {
        return m_sRecordSize;
    }

    /**
     * Returns the number of records in the file.
     * 
     * @return number of records
     */
    public short getRecordCount() {
        return m_sRecordCount;
    }

    /**
     * Update a complete record into a file.
     * 
     * @param sRecordNumber record number in the file.
     * @param abBuffer buffer containing the record data.
     * @param sOffset offset in abBuffer where record data is located.
     * @param sLength length of data to be stored in the record. This value must not exceed the
     *            maximum record length.
     */
    public void updateRecord(short sRecordNumber, byte[] abBuffer, short sOffset, short sLength) {
        // check parameter sRecord
        checkRecordNumber(sRecordNumber);
        sRecordNumber--;

        //Length must be equal to the size of the referenced record
        if (sLength != m_sRecordSize) {
            // return reason code 6700
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // copy data from input buffer
        Util.arrayCopy(abBuffer, sOffset, m_FileData, (short) (sRecordNumber * m_sRecordSize), sLength);
        // Verify that sRecordNumber has the new value
        verifyRecord((short) (sRecordNumber + 1), abBuffer, sOffset);
    }

    /**
     * Read a complete record from a file into a buffer.
     * 
     * @param sRecordNumber record number in the file.
     * @param abBuffer buffer where record data will be copied to.
     * @param sOffset offset in abBuffer where record data will be copied to.
     * @return returns the current record length.
     */
    public short readRecord(short sRecordNumber, byte[] abBuffer, short sOffset, short sLength) {
        // check parameter sRecord
        checkRecordNumber(sRecordNumber);
        sRecordNumber--;

        // copy data to output buffer
        Util.arrayCopy(m_FileData, (short) (sRecordNumber * m_sRecordSize), abBuffer, sOffset,
            m_sRecordSize);
        
        if(sLength != (short) 0) {
            return sLength;
        } else {
            // return length of copied data
            return m_sRecordSize;
        }
    }

    /**
     * Read a complete record from a file into a buffer.
     * 
     * @param sRecordNumber record number in the file.
     * @param abBuffer buffer where record data will be copied to.
     * @param sOffset offset in abBuffer where record data will be copied to.
     * @return returns the current record length.
     */
    public short verifyRecord(short sRecordNumber,  byte[] abBuffer, short sOffset) {
        // check parameter sRecord
        checkRecordNumber(sRecordNumber);
        sRecordNumber--;

        // copy data to output buffer
        if (Util.arrayCompare(m_FileData, (short) (sRecordNumber * m_sRecordSize), abBuffer, sOffset,
            m_sRecordSize) != 0) {
            clearBuffAndExit(abBuffer);
        }

        // return length of copied data
        return m_sRecordSize;
    }
}
