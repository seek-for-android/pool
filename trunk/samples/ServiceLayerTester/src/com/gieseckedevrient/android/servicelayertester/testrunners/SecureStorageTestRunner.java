/*
 * Copyright 2013 Giesecke & Devrient GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gieseckedevrient.android.servicelayertester.testrunners;

import java.io.IOException;

import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.SecureStorageProvider;

import com.gieseckedevrient.android.servicelayertester.TestActivity;
import com.gieseckedevrient.android.servicelayertester.Util;

public class SecureStorageTestRunner extends TestRunner {

    byte[] aid = {
            (byte) 0xD2, (byte) 0x76, (byte) 0x00,
            (byte) 0x01, (byte) 0x18, (byte) 0x00,
            (byte) 0x03, (byte) 0xFF, (byte) 0x34,
            (byte) 0x00, (byte) 0x7E, (byte) 0x89,
            (byte) 0xAA, (byte) 0x00, (byte) 0x7F,
            (byte) 0x09
    };

    public SecureStorageTestRunner(SEService seService, TestActivity activity) {
        super(seService, activity);
    }

    @Override
    public void run() {
        mActivity.logInfo("Connecting to Secure Storage applet...");
        try {
            mChannel = openChannelToApplet(aid);
        } catch (Exception e) {
            endTest(e);
            return;
        }
        if (mChannel == null) {
            endTest("Secure Storage applet not found,"
                    + " make sure it is installed.");
            return;
        }
        mActivity.logInfo("Done!");

        mActivity.logInfo("Initialising SecureStorageProvider...");
        SecureStorageProvider provider;
        try {
            provider = new SecureStorageProvider(mChannel);
        } catch (Exception e) {
            endTest(e);
            return;
        }
        mActivity.logInfo("Done!");

        String title = "Entry1";
        byte[] content = new byte[] {
                (byte) 0xA0, (byte) 0xA1, (byte) 0xA2, (byte) 0xA3,
                (byte) 0xA4, (byte) 0xA5, (byte) 0xA6, (byte) 0xA7,
                (byte) 0xA8, (byte) 0xA9,
        };
        mActivity.logInfo("Creating SeS entry...");
        try {
            provider.create(title, content);
        } catch (Exception e) {
            endTest(e);
            return;
        }
        mActivity.logInfo("Done!");

        mActivity.logInfo("Entry content is:");
        try {
            mActivity.logInfo(Util.byteArrayToHexString(provider.read(title)));
        } catch (Exception e) {
            endTest(e);
            return;
        }

        byte[] newContent = new byte[] {
                (byte) 0xB0, (byte) 0xB1, (byte) 0xB2, (byte) 0xB3,
                (byte) 0xB4, (byte) 0xB5, (byte) 0xB6, (byte) 0xB7,
                (byte) 0xB8, (byte) 0xB9,
        };
        mActivity.logInfo("Updating entry...");
        try {
            provider.update(title, newContent);
        } catch (Exception e) {
            endTest(e);
            return;
        }
        mActivity.logInfo("Done!");

        mActivity.logInfo("New entry content is:");
        try {
            mActivity.logInfo(Util.byteArrayToHexString(provider.read(title)));
        } catch (Exception e) {
            endTest(e);
            return;
        }

        mActivity.logInfo("Deleting entry...");
        try {
            provider.delete(title);
        } catch (Exception e) {
            endTest(e);
            return;
        }
        mActivity.logInfo("Done!");
        endTest();
    }

}
