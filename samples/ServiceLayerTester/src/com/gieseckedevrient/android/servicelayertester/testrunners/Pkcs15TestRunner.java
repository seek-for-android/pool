/**
 * 
 */
package com.gieseckedevrient.android.servicelayertester.testrunners;

import java.io.IOException;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.PKCS15Provider;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;

import com.gieseckedevrient.android.servicelayertester.TestActivity;
import com.gieseckedevrient.android.servicelayertester.Util;

/**
 * @author openmobileapis
 * 
 */
public class Pkcs15TestRunner extends TestRunner {
    byte[] AID_PKCS15 = { (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x63, (byte) 0x50, (byte) 0x4B, (byte) 0x43, (byte) 0x53,
            (byte) 0x2D, (byte) 0x31, (byte) 0x35 };
    byte[] AID_CARD_MANAGER = { (byte) 0xA0, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    /**
     * @param seService
     * @param activity
     */
    public Pkcs15TestRunner(SEService seService, TestActivity activity) {
        super(seService, activity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.gieseckedevrient.android.servicelayertester.testrunners.TestRunner
     * #run()
     */
    @Override
    public void run() {
        Channel mChannel;
        PKCS15Provider provider;
        try {
            mActivity.logInfo("Trying to connect to PKCS#15 applet...");
            mChannel = openChannelToApplet(AID_PKCS15);
            if (mChannel != null) {
                mActivity.logInfo("Done!");
                mActivity.logInfo("Initialising PKCS15Provider...");
                try {
                    provider = new PKCS15Provider(mChannel);
                    mActivity.logInfo("Done!");
                } catch (Exception e) {
                    endTest(e);
                    return;
                }
            } else {
                provider = null;
                mChannel = null;
                mActivity.logError("PKCS#15 applet not found.");
                mActivity.logInfo("Searching for a PKCS#15 file structure...");
                Reader[] readers = mSeService.getReaders();
                for (Reader r : readers) {
                    if (r.isSecureElementPresent()) {
                        try {
                            mActivity.logInfo("Opening channel...");
                            mChannel = r.openSession().openLogicalChannel(
                                    AID_CARD_MANAGER);
                            mActivity.logInfo("Done!");
                            if (mChannel != null) {
                                mActivity.logInfo(
                                        "Initialising PKCS#15 provider...");
                                provider = new PKCS15Provider(mChannel);
                                mActivity.logInfo("Done!");
                                break;
                            }
                        } catch (Exception e) {
                            mActivity.logError(e.getMessage()
                                    + "\nSee log for details.");
                            mChannel = null;
                        }
                    }
                }
                if (provider == null) {
                    endTest("No PKCS#15 file structure found.");
                    return;
                }
            }
        } catch (Exception e) {
            endTest(e);
            return;
        }

        // Read the ODF
        mActivity.logInfo("ODF content is:");
        mActivity.logInfo(Util.byteArrayToHexString(provider.getODF()));
        endTest();
    }
}
