package com.gieseckedevrient.android.servicelayertester.testrunners;

import org.simalliance.openmobileapi.AuthenticationProvider;
import org.simalliance.openmobileapi.AuthenticationProvider.PinID;
import org.simalliance.openmobileapi.SEService;

import com.gieseckedevrient.android.servicelayertester.TestActivity;

public class AuthenticationTestRunner extends TestRunner {
    byte[] aid = {
            (byte) 0xD2, (byte) 0x76, (byte) 0x00,
            (byte) 0x01, (byte) 0x18, (byte) 0x00,
            (byte) 0x03, (byte) 0xFF, (byte) 0x34,
            (byte) 0x00, (byte) 0x7E, (byte) 0x89,
            (byte) 0xAA, (byte) 0x00, (byte) 0x80,
            (byte) 0x09
    };

    PinID pinId;
    byte[] pin = new byte[] {
            (byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31
    };
    byte[] pin2 = new byte[] {
            (byte) 0x32, (byte) 0x32, (byte) 0x32, (byte) 0x32
    };
    byte[] resetPin = {
        (byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31,
        (byte) 0x31, (byte) 0x31, (byte) 0x31, (byte) 0x31
    };

    public AuthenticationTestRunner(SEService seService, TestActivity activity) {
        super(seService, activity);
    }

    @Override
    public void run() {
        mActivity.logInfo("Connecting to Authentication applet...");
        try {
            mChannel = openChannelToApplet(aid);
        } catch (Exception e) {
            endTest(e);
            return;
        }
        if (mChannel == null) {
            endTest("Authentication applet not found,"
                    + " make sure it is installed.");
            return;
        }
        mActivity.logInfo("Done!");

        mActivity.logInfo("Initialising AuthenticationProvider...");
        AuthenticationProvider provider;
        try {
            provider = new AuthenticationProvider(mChannel);
            mActivity.logInfo("Done!");
        } catch (Exception e) {
            endTest(e);
            return;
        }

        pinId = provider.new PinID(1, true);

        mActivity.logInfo("Verifying local pin...");
        try {
            if (provider.verifyPin(pinId, pin)) {
                mActivity.logInfo("Success.");
            } else {
                mActivity.logInfo("Failure.");
            }
        } catch (Exception e) {
            endTest(e);
            return;
        }

        mActivity.logInfo("Changing local pin...");
        try {
            provider.changePin(pinId, pin, pin2);
            mActivity.logInfo("Done!");
        } catch (Exception e) {
            endTest(e);
            return;
        }

        mActivity.logInfo("Resetting local pin...");
        try {
            provider.resetPin(pinId, resetPin, pin);
            mActivity.logInfo("Done!");
        } catch (Exception e) {
            endTest(e);
            return;
        }
        endTest();
    }

}
