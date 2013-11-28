package com.gieseckedevrient.javacard.fileviewtest;

import javacard.framework.ISOException;
import javacard.framework.ISO7816;
import javacard.framework.Util;

abstract public class BasicFile {

    /** Array containing all file data */
    protected byte[] m_FileData;

    /** EF file identifier */
    private short m_sFID;

    /** SFI */
    private byte m_sSFI = 0;

    /** PARENT ID */
    private short m_sParentID = (short) 0;

    /** File type*/
    private byte fileType;

    /** File is secure */
    private boolean m_sSecure;

    /** SECURE_FILE */
    public static final boolean SECURE_FILE = true;

    /** NO_SECURE_FILE */
    public static final boolean NO_SECURE_FILE = false;

    /** File type = DF. */
    public static final byte FILETYPE_DF = (byte) 0x00;

    /** File type TRANSPARENT = EF. */
    protected static final byte FILETYPE_EF_TRANSPARENT = (byte) 0x01;

    /** File type TRANSPARENT = EF. */
    protected static final byte FILETYPE_EF_LINEAR_FIX = (byte) 0x02;

    /** File type CYCLIC.*/
    protected static final byte FILETYPE_EF_CYCLIC = (byte) 0x03;

    /** FCP template TLV tag.*/
    private static final byte FCPTAG_FCP_TEMPLATE = (byte) 0x62;

    /** FCP file size TLV tag.*/
    private static final byte FCPTAG_FILE_SIZE = (byte) 0x80;

    /** FCP total file size TLV tag.*/
    private static final byte FCPTAG_TOTAL_FILE_SIZE = (byte) 0x81;

    /** FCP file descriptor TLV tag.*/
    private static final byte FCPTAG_FILE_DESCRIPTOR = (byte) 0x82;

    /** FCP file ID TLV tag.*/
    private static final byte FCPTAG_FILE_ID = (byte) 0x83;

    /** FCP ShortFileIdentifier TLV tag.*/
    private static final byte FCPTAG_SFI = (byte) 0x88;

    /** FCP Life Cycle Status TLV tag. */
    private static final byte FCPTAG_LCS = (byte) 0x8A;

    /** MasterFile ID */
    public static final short MF_FILEID = (short) 0x3F00;

    /** Max File numbers */
    protected short m_sMaxFileNumer;

    /** Number of files on the DF */
    protected short m_sNumberOfFiles;


    /**
     * Constructor.
     * 
     * @param sRecordCount maximum number of records in file.
     * @param sRecordSize size of one single record
     */
    public BasicFile(short sFID, byte sSFI, short size, byte type, boolean secure, short sParentID) {
        m_sFID = sFID;
        m_sSFI = sSFI;
        m_FileData = new byte[size];
        fileType = type;
        m_sSecure = secure;
        m_sParentID = sParentID;
    }

    public BasicFile(short sFID, byte maxNumerOfFiles, byte type, boolean secure, short sParentID) {
        m_sFID = sFID;
        m_FileData = new byte[(byte) (maxNumerOfFiles * 4)];
        fileType = type;
        m_sSecure = secure;
        m_sParentID = sParentID;
    }

    /**
     * Erases any buffer and throws Exception - usual as reaction on a detected attack
     * 
     * @param abBuffer array to erase
     * @exception ISOException with reason code ISO7816.SW_UNKNOWN.
     */
    protected static void clearBuffAndExit(byte[] abBuffer) { 
        // erase everything
        Util.arrayFillNonAtomic(abBuffer, (short) 0, (short) abBuffer.length, (byte) 0xFF);
        //return reason code 6F00
        ISOException.throwIt(ISO7816.SW_UNKNOWN);
    }

    /**
     * Getter for the SFI attribute.
     *
     * @return value of SFI
     */
    public byte getSFI() {
        return m_sSFI;
    }

    /**
     * Getter for the File ID attribute.
     *
     * @return value of File ID
     */
    public short getFID() {
        return m_sFID;
    }

    /**
     * Getter for the Record Size attribute.
     *
     * @return value of Record Size
     */
    public short getFileSize() {
        return (short) m_FileData.length;
    }

    /**
     * Getter for the File Type
     * 
     * @return value of fileType
     */
    public byte getFileType() {
        return fileType;
    }

    public boolean isFileSecure() {
        return m_sSecure;
    }

    public void setFileSecure(boolean secure) {
        m_sSecure = secure;
    }

    public short getM_sParentID() {
        return m_sParentID;
    }

    public void setM_sParentID(short m_sParentID) {
        this.m_sParentID = m_sParentID;
    }

    public short getFCP(byte[] buffer, short sLength) {

        sLength = appendShortToTLVArray(FCPTAG_FILE_SIZE, (short) m_FileData.length, buffer, sLength);
        sLength = appendShortToTLVArray(FCPTAG_TOTAL_FILE_SIZE, (short) m_FileData.length, buffer, sLength);
        sLength = appendByteToTLVArray(FCPTAG_FILE_DESCRIPTOR, (byte) 0x01, buffer, sLength);
        sLength = appendShortToTLVArray(FCPTAG_FILE_ID, m_sFID, buffer, sLength);
        if(m_sSFI != (byte) 0x00) {
            sLength = appendByteToTLVArray(FCPTAG_SFI, m_sSFI, buffer, sLength);
        }
        sLength = appendByteToTLVArray(FCPTAG_LCS, (byte) 0x01, buffer, sLength);

        buffer[0] = FCPTAG_FCP_TEMPLATE;
        buffer[1]= (byte) (sLength - 2);

        return sLength;
    }

    public short appendShortToTLVArray (byte Tag, short value, byte[] buffer, short offset) {
        offset = Util.arrayFillNonAtomic(buffer, offset, (short) 0x01, Tag);
        offset = Util.arrayFillNonAtomic(buffer, offset, (short) 0x01, (byte) 0x02);
        offset = Util.setShort(buffer, offset, value);
        return offset;
    }

    public short appendByteToTLVArray (byte Tag, byte value, byte[] buffer, short offset) {
        offset = Util.arrayFillNonAtomic(buffer, offset, (short) 0x01, Tag);
        offset = Util.arrayFillNonAtomic(buffer, offset, (short) 0x01, (byte) 0x01);
        offset = Util.arrayFillNonAtomic(buffer, offset, (short) 0x01, value);
        return offset;
    }

    public short appendArrayToTLVArray (byte Tag, byte[] value, byte[] buffer, short offset) {
        offset = Util.arrayFillNonAtomic(buffer, offset, (short) 0x01, Tag);
        offset = Util.arrayFillNonAtomic(buffer, offset, (short) 0x01, (byte) 0x01);
        offset = Util.arrayCopy(value, (short) 0x00, buffer, (short) offset, (short) value.length);
        return offset;
    }

    public boolean isFileOnCurrentDF(short m_sFID) {

        for (short i = 0; i < m_sNumberOfFiles; i++) {
            if (Util.makeShort(m_FileData[(short) (i * 2)], m_FileData[(short) ((i * 2) + 1 )]) == m_sFID) {
                return true;
            }
        }
        return false;
    }
}