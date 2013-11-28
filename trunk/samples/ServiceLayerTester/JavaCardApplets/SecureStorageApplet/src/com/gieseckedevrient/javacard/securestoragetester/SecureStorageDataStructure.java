package com.gieseckedevrient.javacard.securestoragetester;


public class SecureStorageDataStructure {

    public byte[] title;
    public byte[] data;
    public byte withData;

    public short tittleOffset;
    public short dataOffset;

    //Class Constructor
    public SecureStorageDataStructure(short inputDataLength, short inputTitleLength){
        //At initialization, no data are provided
        withData = (byte) 0;
        title = new byte[inputTitleLength];
        tittleOffset = (short) 0;
        data = new byte[inputDataLength];
        dataOffset = (short) 0;
    }

}
