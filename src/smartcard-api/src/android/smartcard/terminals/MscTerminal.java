/*
 * Copyright 2010 Giesecke & Devrient GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.smartcard.terminals;

import android.content.Context;
import android.smartcard.CardException;
import android.smartcard.SmartcardService;
import android.smartcard.Terminal;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;

/**
 * MSC based channel implementation.
 */
final class MscTerminal extends Terminal {

    private String mStorageName = "";

    private byte[] mAtr;

    public MscTerminal(Context context) {
        super("Mobile Security Card", context);

        getStorageName();
    }

    @Override
    protected void internalConnect() throws CardException {
        // Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, "internalConnect");
        if (mStorageName.equals("")) {
            getStorageName();
            if (mStorageName.equals("")) {
                throw new CardException("No Mobile Security Card detected");
            }
        }

        try {
            if (Open(mStorageName) == false) {
                throw new CardException("Card connect failed");
            }

        } catch (Exception e) {
            throw new CardException("Card connect failed");
        }

        try {
            byte[] response = Transmit(new byte[] {
                    0x20, 0x12, 0x01, 0x01, 0x00
            });
            if (response.length < 2) {
                internalDisconnect();
                throw new CardException("Card connect error");
            }
            mAtr = response;
            // Log.v(SmartcardService.SMARTCARD_SERVICE_TAG,
            // Thread.currentThread().getName() + " MSC connected");
        } catch (Exception e) {
            internalDisconnect();
            throw new CardException("Card connect error");
        }
        mIsConnected = true;
    }

    @Override
    protected void internalDisconnect() throws CardException {
        // Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, "internalDisconnect");
        if (mStorageName.equals("")) {
            getStorageName();
            if (mStorageName.equals("")) {
                throw new CardException("No Mobile Security Card detected");
            }
        }

        try {
            Close();
        } catch (Exception ignore) {
        } finally {
            mIsConnected = false;
            // Log.v(SmartcardService.SMARTCARD_SERVICE_TAG,
            // Thread.currentThread().getName() + " MSC disconnected");
        }
    }

    public boolean isCardPresent() throws CardException {
        // Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, "isCardPresent");
        if (mStorageName.equals("")) {
            getStorageName();
            if (mStorageName.equals("")) {
                return false;
            }
        }

        if (!mIsConnected) {
            try {
                internalConnect();
            } catch (Exception e) {
                return false;
            }
        }

        try {
            byte[] response = Transmit(new byte[] {
                    0x20, 0x13, 0x00, (byte) 0x80, 0x00
            });
            if (response == null) {
                return false;
            }

            if (response.length == 0x05 && response[response.length - 2] == (byte) 0x90
                    && response[response.length - 1] == 0x00) {
                return true;
            }

        } catch (Exception e) {
        }

        return false;
    }

    /**
     * Returns the ATR of the connected card or null if the ATR is not
     * available.
     * 
     * @return the ATR of the connected card or null if the ATR is not
     *         available.
     */
    public byte[] getAtr() {
        return mAtr;
    }

    protected boolean isCardPresentInternal(String storageName) throws CardException {
        // Log.v(SmartcardService.SMARTCARD_SERVICE_TAG,
        // "isCardPresentInternal - MSC path: " + storageName);
        if (storageName.equals("")) {
            return false;
        }

        if (!mIsConnected) {
            try {
                // Log.v(SmartcardService.SMARTCARD_SERVICE_TAG,
                // "isCardPresent-call to internalConnect");
                internalConnect();
            } catch (Exception e) {
                // Log.v(SmartcardService.SMARTCARD_SERVICE_TAG,
                // "isCardPresentInternal - connect failed - returns false");
                return false;
            }
        }

        try {
            byte[] response = Transmit(new byte[] {
                    0x20, 0x13, 0x00, (byte) 0x80, 0x00
            });
            if (response == null) {
                // Log.v(SmartcardService.SMARTCARD_SERVICE_TAG,
                // "isCardPresentInternal - response is NULL");
                return false;
            }

            if (response.length == 0x05 && response[response.length - 2] == (byte) 0x90
                    && response[response.length - 1] == 0x00) {
                // Log.v(SmartcardService.SMARTCARD_SERVICE_TAG,
                // "isCardPresentInternal - returns true");
                return true;
            }

        } catch (Exception e) {
            Log.e("MscTerminal", e.getMessage());
        }
        Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, "isCardPresentInternal - returns false");
        return false;
    }

    @Override
    protected byte[] internalTransmit(byte[] command) throws CardException {
        // Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, "internalTransmit");
        if (mStorageName.equals("")) {
            throw new CardException("No Mobile Security Card detected");
        }

        try {
            byte[] response = Transmit(command);
            if (response == null) {
                throw new CardException("internal error");
            }
            return response;
        } catch (Exception e) {
            throw new CardException(e);
        }
    }

    @Override
    protected void internalCloseLogicalChannel(int channelNumber) throws CardException {
        if (channelNumber > 0) {
            byte cla = (byte) channelNumber;
            if (channelNumber > 3) {
                cla |= 0x40;
            }

            byte[] manageChannelClose = new byte[] {
                    cla, 0x70, (byte) 0x80, (byte) channelNumber
            };
            transmit(manageChannelClose, 2, 0x9000, 0xFFFF, "MANAGE CHANNEL");
        }
    }

    @Override
    protected int internalOpenLogicalChannel() throws Exception {
        byte[] manageChannelCommand = new byte[] {
                0x00, 0x70, 0x00, 0x00, 0x01
        };
        byte[] rsp = transmit(manageChannelCommand, 3, 0x9000, 0xFFFF, "MANAGE CHANNEL");
        if (rsp.length == 2 && (rsp[0] == (byte) 0x6A && rsp[1] == (byte) 0x81)) {
            throw new MissingResourceException("no free channel available", "", "");
        }
        if (rsp.length != 3) {
            throw new MissingResourceException("unsupported MANAGE CHANNEL response data", "", "");
        }
        int channelNumber = rsp[0] & 0xFF;
        if (channelNumber == 0 || channelNumber > 19) {
            throw new MissingResourceException("invalid logical channel number returned", "", "");
        }

        return channelNumber;
    }

    @Override
    protected int internalOpenLogicalChannel(byte[] aid) throws Exception {
        if (aid == null) {
            throw new NullPointerException("aid must not be null");
        }
        byte[] manageChannelCommand = new byte[] {
                0x00, 0x70, 0x00, 0x00, 0x01
        };
        byte[] rsp = transmit(manageChannelCommand, 3, 0x9000, 0xFFFF, "MANAGE CHANNEL");
        if (rsp.length == 2 && (rsp[0] == (byte) 0x6A && rsp[1] == (byte) 0x81)) {
            throw new MissingResourceException("no free channel available", "", "");
        }
        if (rsp.length != 3) {
            throw new MissingResourceException("unsupported MANAGE CHANNEL response data", "", "");
        }
        int channelNumber = rsp[0] & 0xFF;
        if (channelNumber == 0 || channelNumber > 19) {
            throw new MissingResourceException("invalid logical channel number returned", "", "");
        }

        byte[] selectCommand = new byte[aid.length + 6];
        selectCommand[0] = (byte) channelNumber;
        if (channelNumber > 3) {
            selectCommand[0] |= 0x40;
        }
        selectCommand[1] = (byte) 0xA4;
        selectCommand[2] = 0x04;
        selectCommand[4] = (byte) aid.length;
        System.arraycopy(aid, 0, selectCommand, 5, aid.length);
        try {
            transmit(selectCommand, 2, 0x9000, 0xFFFF, "SELECT");
        } catch (CardException exp) {
            internalCloseLogicalChannel(channelNumber);
            throw new NoSuchElementException(exp.getMessage());
        }

        return channelNumber;
    }

    private void getStorageName() {
        try {
            Runtime rt = Runtime.getRuntime();
            Process ps = rt.exec("/system/bin/mount");
            ps.waitFor();

            BufferedReader rd = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            String rs;
            while ((rs = rd.readLine()) != null) {
                String[] dirs = rs.split("[ \t]");
                for (String dir : dirs) {
                    dir.trim();
                    if (dir.length() == 0) {
                        continue;
                    }
                    File path = new File(dir);
                    if (path.isDirectory() && path.canWrite()) {
                        mStorageName = path.getAbsolutePath() + "/msc.sig";
                        try {
                            internalConnect();
                            if (isCardPresentInternal(mStorageName)) {
                                Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, "MSC path: "
                                        + mStorageName);
                                return;
                            } else {
                                internalDisconnect();
                                mStorageName = "";
                            }
                        } catch (Exception e) {
                            mStorageName = "";
                        }
                    }
                }

            }
            rd.close();
        } catch (Throwable t) {
        }

        return;
    }

    /*
     * ******************************************
     * MSC library JNI wrapper for card terminals.
     * ******************************************
     */

    /**
     * MSC library load exception; <code>null</code> if the library was loaded
     * successfully.
     */
    private static Throwable loadException;

    static {
        try {
            Runtime.getRuntime().loadLibrary("msc");
        } catch (Throwable t) {
            loadException = t;
        }
    }

    /**
     * Closes the device file.
     * 
     * @throws Exception if the close operation failed.
     */
    private static native void Close() throws Exception;

    /**
     * Returns the MSC library load exception or <code>null</code> if the
     * library was loaded successfully.
     * 
     * @return the MSC library load exception or <code>null</code> if the
     *         library was loaded successfully.
     */
    private static Throwable getLoadError() {
        return loadException;
    }

    /**
     * Returns <code>true</code> if the MSC native library was loaded
     * successfully or <code>false</code> if the MCEX JNI is not available.
     * 
     * @return <code>true</code> if the MSC native library was loaded
     *         successfully or <code>false</code> if the MCEX JNI is not
     *         available.
     */
    private static boolean isLoaded() {
        return (loadException == null);
    }

    /**
     * Opens the MSC device file in the specified mode. Open mode constants are
     * defined in the inner <code>OpenMode</code> class.
     * 
     * @return true if operation successful, otherwise false
     * @throws Exception if the open operation failed.
     */
    private static native boolean Open(String storageName) throws Exception;

    /**
     * Transmits the specified command and returns the response.
     * 
     * @param command the command to be written to the device file.
     * @return the response read from the device file.
     * @throws Exception if the transmit operation failed.
     */
    private static native byte[] Transmit(byte[] command) throws Exception;
}
