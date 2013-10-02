package com.gieseckedevrient.android.servicelayertester;

public class Util {
    /**
     * Forms an hex-encoded String of the specified byte array.
     *
     * @param byteArray The byte array to be hex-encoded.
     *
     * @return An hex-encoded String of the specified byte array.
     */
    public static String byteArrayToHexString(byte[] byteArray) {
        if (byteArray == null) {
            return "";
        }

        StringBuffer sb = new StringBuffer();

        for (byte b : byteArray) {
            sb.append(String.format("%02X ", b & 0xFF));
        }

        return sb.substring(0, sb.length() - 1).toString();
    }
}
