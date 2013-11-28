package com.gieseckedevrient.javacard.fileviewtest;

import javacard.framework.Util;

public class DirectoryFolder extends BasicFile {


    /**
     * Constructor.
     * 
     */
    public DirectoryFolder(short sFID, byte maxNumerOfFiles, boolean secure, short sParentID) {
        super(sFID, maxNumerOfFiles, BasicFile.FILETYPE_DF, secure, sParentID);
        m_sMaxFileNumer = maxNumerOfFiles; 
    }

    public void addFileToDF(short m_sFID) {
        Util.setShort(m_FileData, (short) (m_sNumberOfFiles * 2), m_sFID);
        m_sNumberOfFiles++;
    }
}
