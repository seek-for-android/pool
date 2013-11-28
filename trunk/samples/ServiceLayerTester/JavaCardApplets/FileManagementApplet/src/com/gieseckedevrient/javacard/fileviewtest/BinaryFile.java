package com.gieseckedevrient.javacard.fileviewtest;

import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * The class <code>CIPURSEBinaryFile</code> extends the <code>CIPURSERecordFile</code> type. Be aware
 * that a CIPURSEBinaryFile is a GDFixFile with only one record and handles the additional
 * functionality of a Binary File.
 */
public class BinaryFile extends BasicFile {

    /**
     * Constructor.
     * 
     * @param sFileSize the total size of the file
     */
    public BinaryFile(short sFID, byte sSFI, short sFileSize, boolean secure, short parentID) {
        super(sFID, sSFI, sFileSize, FILETYPE_EF_TRANSPARENT, secure, parentID);
    }

    /**
     * Reads transparent data from a file into a buffer.
     * 
     * @param sFileOffset offset in the file where reading starts.
     * @param abBuffer buffer where the output data will be stored.
     * @param sOffset offset in abBuffer where the output data will be stored.
     * @param sLength length of data to be read
     */
    public short readBinary(short sFileOffset, byte[] abBuffer, short sOffset, short sLength) {

        Util.arrayCopy(m_FileData, sFileOffset, abBuffer, sOffset, (short) m_FileData.length);

        if (sLength == (byte) 0x00) {
            return (short) m_FileData.length;
        } else {
            return sLength;
        }
    }

    /**
     * Write transparent into a file.
     * 
     * @param sFileOffset offset in the file where writing starts.
     * @param abBuffer buffer where the input data is stored.
     * @param sOffset offset in abBuffer where the input data is stored.
     * @param sLength length of data to be written
     */
    public void writeBinary(short sFileOffset, byte[] abBuffer, short sOffset, short sLength) {
        // do basic checking
        if (sLength != checkOffsetAndLength(sFileOffset, sLength, (short) m_FileData.length)) {
            ISOException.throwIt((short) 0x6282);
        }

        // copy data to file
        Util.arrayCopy(abBuffer, sOffset, m_FileData, sFileOffset, sLength);
    }

    /**
     * Check upper and lower bound of binary file.
     * In the case reading Ne bytes would exceed the end of file, the data up to the end of file are provided 
     * and no further error indication is given.
     * 
     * @param sOffset offset into record.
     * @param sLength length to be read/written starting at offset.
     * @param sFileLen available record length.
     * @exception ISOException with reason code ISO7816.SW_WRONG_DATA.
     */
    protected static short checkOffsetAndLength(short sOffset, short sLength, short sFileLen) {
        // check if offset is inside of the record
        if (sOffset >= sFileLen) {
            // reason code 6282 (EOF reached)
            ISOException.throwIt((short) 0x6282);
        }

        //check the data to Read
        if ((short) (sOffset + sLength) > sFileLen) {
            return (short) (sFileLen - sOffset);
        }

        return sLength;
    }
}
