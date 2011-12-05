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

package org.simalliance.openmobileapi.service.security;

import org.apache.http.util.ByteArrayBuffer;
import org.simalliance.openmobileapi.service.CardException;
import org.simalliance.openmobileapi.service.IChannel;


import java.security.AccessControlException;

public class AccessControlDB {

    private AccessControlApplet mApplet;

    AccessControlDB(IChannel channel) throws AccessControlException, CardException {
        mApplet = new AccessControlApplet(channel);
    }

    public void selectAID(byte[] aid) throws AccessControlException, CardException {

        mApplet.selectAID(aid);
    }

    public byte[] readAPCertificate() throws AccessControlException, CardException {

        ByteArrayBuffer bytes = new ByteArrayBuffer(1024);
        ByteArrayBuffer buffer = new ByteArrayBuffer(256);
        int offset = 0;
        int length = 0;
        while (!mApplet.readAPCertificate(buffer, offset, length)) {
            if (buffer.length() == 0) {
                break;
            }
            offset += buffer.length();
            bytes.append(buffer.toByteArray(), 0, buffer.length());
        }
        return bytes.toByteArray();
    }

    public AccessCondition[] readAPKACRecord(byte[] hashApkCert) throws AccessControlException,
            CardException {

        byte[] accessConditions = mApplet.readAPKACRecord(hashApkCert);
        return AccessConditionUtil.parseAccessConditions(accessConditions);
    }

}
