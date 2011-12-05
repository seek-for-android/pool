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

package org.simalliance.openmobileapi.cts;

import java.util.Arrays;
import mockcard.Applet;
import mockcard.MockCardException;

public class APDUTester extends Applet
{

	byte[] buffer = new byte[261];
	
    public APDUTester() {
    } // constructor
      
    public byte[] process(byte[] command) throws MockCardException {
        int lc;
        int le;
        byte ins = command[1];
        
        if (isSelect()) {
            return new byte[]{(byte)0x08, (byte)0x15, (byte)0x47, (byte)0x11};
        }
        
        lc = (command.length<=4)? 0: (0xff & command[4]);
        le = 0;
        
        switch (ins) {
            case (byte)0x01:
            	// case-1 test command:
            	le = 0;
                break;
            
            case (byte)0x02:
            	// case-2 test command:
            	lc = 0;
                le = (command.length<=4)? 0: ((command[4]==0)? 256: (0xff & command[4]));
                break;
        
            case (byte)0x03:
            	// case-3 test command:
                le = 0;
                break;
        
            case (byte)0x04:
            	// case-4 test command:
                le = (command[2]<<8) | (0xff & command[3]);
                if (le == 0)
                	le = (command.length<=5+lc)? 0: ((command[5+lc]==0)? 256: (0xff & command[5+lc]));
                break;
            
            case (byte)0x10:
            	// send CLA byte as response:
                le = 1;
                return Arrays.copyOf(command, le);
            
            default:
                throw new MockCardException(0x6D00);
        }

        // verify incoming data (if there is any):
        for (int i=0; i<lc; i++)
            if (command[5+i]!=(byte)i)
                throw new MockCardException(0x6FFF);
        
        // set outgoing data (if there is any):
        for (int i=0; i<le; i++)
            buffer[i] = (byte)i;
        
        return Arrays.copyOf(buffer, le);
    } // process
  
} // class
