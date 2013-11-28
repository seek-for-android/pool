package com.gieseckedevrient.javacard.fileviewtest;

import javacard.framework.ISOException;
import javacard.framework.ISO7816;
import javacard.framework.Util;

/**
 * The class <code>CyclicFile</code> extends the linear fix <code>RecordFile</code> type. The
 * basic readRecord and writeRecord methods operating on the whole record (and therefore the only
 * methods intended to be used on a cyclic file) use a cyclic index scheme. All other methods
 * inherited from GDFixFile can be used, but use the normal linear indexing scheme.
 */
public class CyclicFile extends RecordFile {

    /** index of oldest record */
    protected short m_sOldestPosition;


    /**
     * Constructor.
     * 
     * @param sRecordCount number of records in the current cyclic file.
     * @param sRecordLength length of the record. Every record owns always the same length.
     */
    public CyclicFile(short sFID, byte sSFI, short sRecordCount, short sRecordSize, boolean secure, short sParentID) {
        super(sFID, sSFI, sRecordCount,  sRecordSize, FILETYPE_EF_CYCLIC, secure, sParentID);

        // Initialization when file is not full
        m_sOldestPosition = (short) 0;

        // one spare record is always available
        m_sRecordCount = (short) 0;
    }

    /**
     * Initiates creation of a new record with record number 1 in a cyclic record EF not full of records. 
     * When applied to a cyclic record EF full of records, then the oldest record (i.e. the record with 
     * the highest record number) is replaced. This record then becomes record number 1. The contents of 
     * the appended record are updated with the data in the command data field.
     * 
     * @param sRecordNumber cyclic record number in the file.
     * @param abBuffer buffer containing the record data.
     * @param sOffset offset in abBuffer where record data is located.
     * @param sLength length of data to be stored in the record. This value must be equal to the
     *            record length.
     * @return the number of the record appended.
     */
    public short appendRecord(byte[] abBuffer, short sOffset, short sLength) {
        // Length must be equal to the size of the referenced record
        if (sLength != m_sRecordSize) {
            // return reason code 6700
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        //file already full?
        if (m_sRecordCount < (short) (m_FileData.length / m_sRecordSize)) {
            // file not full: increment total record count
            // copy data from input buffer into spare record
            Util.arrayCopy(abBuffer, sOffset, m_FileData, (short) (m_sRecordCount * m_sRecordSize), sLength);
            m_sRecordCount++;
            return ((short) (m_sRecordCount - (short) 1));

        } else {
            Util.arrayCopy(abBuffer, sOffset, m_FileData, (short) (m_sOldestPosition * m_sRecordSize), sLength);
            short sRecordNumber = m_sOldestPosition;
            m_sOldestPosition = (short) ((short) (m_sOldestPosition + 1) % m_sRecordCount);
            return sRecordNumber;
        }
    }

    /**
     * Read a complete record from a file into a buffer.
     * 
     * @param sRecordNumber record number in the file.
     * @param abBuffer buffer where record data will be copied to.
     * @param sOffset offset in abBuffer where record data will be copied to.
     * @param sLength length of the record to read.
     * @return returns the current record length.
     */
    public short readRecord(short sRecordNumber, byte[] abBuffer, short sOffset, short sLength) {
        //update the record number because in a cyclic file the records are in special order
        if (m_sRecordCount < (short) (m_FileData.length/m_sRecordSize)) {
            //case: file is not full
            sRecordNumber = (short) ((short) (m_sRecordCount - sRecordNumber) + 1);
        } else {
            //case: file is full (all records are created)
            sRecordNumber = (short) ((short) ((short) (m_sOldestPosition - sRecordNumber + m_sRecordCount) % m_sRecordCount) + 1);
        }
        //use the function of RecordFile
        return super.readRecord(sRecordNumber, abBuffer, sOffset, sLength);
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
        //update the record number because in a cyclic file the records are in special order
        if (m_sRecordCount < (short) (m_FileData.length / m_sRecordSize)) {
            //case: file is not full
            sRecordNumber = (short) ((short) (m_sRecordCount - sRecordNumber) + 1);
        } else {
            //case: file is full (all records are created)
            sRecordNumber = (short) ((short) ((short) (m_sOldestPosition - sRecordNumber + m_sRecordCount) % m_sRecordCount) + 1);
        }
        //use the function of RecordFile
        super.updateRecord(sRecordNumber, abBuffer, sOffset, sLength);
    }
}
