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

import org.simalliance.openmobileapi.FileViewProvider;
import org.simalliance.openmobileapi.SEService;

import com.gieseckedevrient.android.servicelayertester.TestActivity;
import com.gieseckedevrient.android.servicelayertester.Util;

public class FileManagementTestRunner extends TestRunner {
    byte[] aid = {
            (byte) 0xD2, (byte) 0x76, (byte) 0x00,
            (byte) 0x01, (byte) 0x18, (byte) 0x00,
            (byte) 0x02, (byte) 0xFF, (byte) 0x34,
            (byte) 0x00, (byte) 0x75, (byte) 0x89,
            (byte) 0xAA, (byte) 0x00, (byte) 0x76,
            (byte) 0x19
    };

    int FID_DF = 0x2000;

    int SFI_EF = 21;

    public FileManagementTestRunner(SEService seService, TestActivity activity) {
        super(seService, activity);
    }

    @Override
    public void run() {
        mActivity.logInfo("Connecting to File Management applet...");
        try {
            mChannel = openChannelToApplet(aid);
        } catch (Exception e) {
            endTest(e);
            return;
        }
        if (mChannel == null) {
            endTest("File Management applet not found,"
                    + " make sure it is installed.");
            return;
        }
        mActivity.logInfo("Done!");

        mActivity.logInfo("Initialising FileViewProvider...");
        FileViewProvider provider = new FileViewProvider(mChannel);
        mActivity.logInfo("Done!");

        mActivity.logInfo("Selecting directory...");
        try {
            provider.selectByFID(FID_DF);
        } catch (Exception e) {
            endTest(e);
            return;
        }
        mActivity.logInfo("Done!");

        mActivity.logInfo("Reading binary file...");
        byte[] content;
        try {
            content = provider.readBinary(SFI_EF, 0, 0);
        } catch (Exception e) {
            endTest(e);
            return;
        }
        mActivity.logInfo("File content is:");
        mActivity.logInfo(Util.byteArrayToHexString(content));

        byte[] newContent = new byte[] {
                (byte) 0xA0, (byte) 0xA1, (byte) 0xA2, (byte) 0xA3,
                (byte) 0xA4, (byte) 0xA5, (byte) 0xA6, (byte) 0xA7,
                (byte) 0xA8, (byte) 0xA9,
        };
        mActivity.logInfo("Writting file...");
        try {
            provider.writeBinary(SFI_EF, newContent, 0, newContent.length);
        } catch (Exception e) {
            endTest(e);
            return;
        }
        mActivity.logInfo("New content is:");
        try {
            mActivity.logInfo(Util.byteArrayToHexString(
                    provider.readBinary(SFI_EF, 0, 0)));
        } catch (Exception e) {
            endTest(e);
            return;
        }

        mActivity.logInfo("Restoring file...");
        try {
            provider.writeBinary(SFI_EF, content, 0, content.length);
        } catch (Exception e) {
            endTest(e);
            return;
        }
        mActivity.logInfo("Done!");

        endTest();
    }

}
