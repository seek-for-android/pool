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

public class AccessControlApplet {

    final private static CommandApdu mSelectAID = new CommandApdu(0x80, 0xA5, 0x00, 0x00, 0x00);

    final private static CommandApdu mReadAPCertificate = new CommandApdu(0x80, 0xB0, 0x00, 0x00,
            0x00);

    final private static CommandApdu mReadAPKACRecord = new CommandApdu(0x80, 0xB2, 0x00, 0x00,
            0x00);

    private IChannel mChannel = null;

    public AccessControlApplet(IChannel channel) {
        mChannel = channel;
    }

    public void selectAID(byte[] aid) throws AccessControlException, CardException {
        CommandApdu apdu = (CommandApdu) mSelectAID.clone();
        apdu.setData(aid);
        ResponseApdu response = send(apdu);
        response.checkLengthAndStatus(0, 0x9000, "SELECT AID");
    }

    public boolean readAPCertificate(ByteArrayBuffer bytes, int offset, int length)
            throws AccessControlException, CardException {
        CommandApdu apdu = mReadAPCertificate.clone();
        apdu.setP1(offset >> 8 & 0xFF);
        apdu.setP2(offset & 0xFF);
        apdu.setLe(length & 0xFF);
        ResponseApdu response = send(apdu);
        byte[] data = response.getData();
        bytes.clear();
        bytes.append(data, 0, data.length);
        if (response.getSW1SW2() == 0x6A86) {
            return true;
        }
        response.checkStatus(new int[] {
                0x9000, 0x6A82
        }, "READ AP CERTIFICATE");
        return false;
    }

    public byte[] readAPKACRecord(byte[] hashApkCert) throws AccessControlException, CardException {
        CommandApdu apdu = mReadAPKACRecord.clone();
        apdu.setData(hashApkCert);
        ResponseApdu response = send(apdu);
        if (response.getSW1SW2() == 0x6984) {
            throw new AccessControlException("referenced ACL contains invalid data");
        }
        response.checkStatus(new int[] {
                0x9000, 0x6A83
        }, "READ APK AC RECORD");
        return response.getData();
    }

    private ResponseApdu send(CommandApdu cmdApdu) throws CardException {
        byte[] response = mChannel.transmit(cmdApdu.toBytes());
        ResponseApdu resApdu = new ResponseApdu(response);
        return resApdu;
    }

}
