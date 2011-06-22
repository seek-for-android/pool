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

package android.smartcard.security;

import android.smartcard.Util;

public class AccessConditionUtil {

    static AccessCondition[] parseAccessConditions(byte[] accessConditions) {
        if (accessConditions.length == 0) {
            return new AccessCondition[0];
        }

        if ((accessConditions.length % 8) != 0) {
            throw new IllegalArgumentException("Access Conditions must have a length of 8 bytes");
        }

        int numOfACs = accessConditions.length / 8;
        AccessCondition[] acs = new AccessCondition[numOfACs];
        int offset = 0;
        int length = 8;
        int index = 0;
        while ((offset + length) <= accessConditions.length && length != 0) {
            acs[index] = new AccessCondition(Util.getMid(accessConditions, offset, length));
            offset += length;
            index++;
        }
        return acs;
    }
}
