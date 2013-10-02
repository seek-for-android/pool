package com.gieseckedevrient.android.servicelayertester.testrunners;

import java.io.IOException;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.SEService;

import com.gieseckedevrient.android.servicelayertester.TestActivity;

public class DiscoveryTestRunner extends TestRunner {
    byte[] aid = {
            (byte) 0xD2, (byte) 0x76, (byte) 0x00,
            (byte) 0x01, (byte) 0x18, (byte) 0x00,
            (byte) 0x02, (byte) 0xFF, (byte) 0x49,
            (byte) 0x50, (byte) 0x25, (byte) 0x89,
            (byte) 0xC0, (byte) 0x01, (byte) 0x9B,
            (byte) 0x01
    };

    public DiscoveryTestRunner(SEService seService, TestActivity activity) {
        super(seService, activity);
    }

    @Override
    public void run() {
        mActivity.logInfo("Starting discovery process...");
        try {
            mChannel = openChannelToApplet(aid);
            if (mChannel != null) {
                mActivity.logInfo("Done!");
            } else {
                endTest("HelloSmartCard applet not found,"
                        + " make sure it is installed.");
                return;
            }
        } catch (Exception e) {
            endTest(e);
            return;
        }

        endTest();
    }
}
