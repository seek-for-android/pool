/*
 * Copyright (C) 2009 The Android Open Source Project
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

package android.smartcard.cts;

import java.util.Arrays;
import mockcard.Applet;
import mockcard.MockCardException;

public class APDUTester extends Applet
{
	byte[] buffer = new byte[260];
	
    public APDUTester() {
    } // constructor
      
    public byte[] process(byte[] command) throws MockCardException {
        System.arraycopy(command, 0, buffer, 0, command.length);
        byte ins = buffer[INX_INS];
        
        if (isSelect()) {
            return new byte[0];
        }
        
        int lc = 0;
        int le = 0;
        
        switch (ins) {
            case (byte)0x01:
                break;
            
            case (byte)0x02:
                le = 0xff & buffer[4];
                break;
        
            case (byte)0x03:
                lc = 0xff & buffer[INX_LC];
                break;
        
            case (byte)0x04:
                lc = 0xff & buffer[INX_LC];
                le = 0xff & buffer[OFFSET_CDATA+lc];
                break;
            
            default:
                throw new MockCardException(0x6D00);
        }
        
        // verify incoming data (if there is any):
        for (int i=0; i<lc; i++)
            if (buffer[OFFSET_CDATA+i]!=(byte)i)
                throw new MockCardException(0x6FFF);
        
        // set outgoing data (if there is any):
        for (int i=0; i<le; i++)
            buffer[i] = (byte)i;
        
        return Arrays.copyOf(buffer, le);
    } // process
  
} // class
