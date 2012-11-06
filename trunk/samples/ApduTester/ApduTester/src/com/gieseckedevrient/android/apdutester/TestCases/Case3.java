/**
 * Copyright 2011 Giesecke & Devrient GmbH.
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
package com.gieseckedevrient.android.apdutester.TestCases;

import java.util.ArrayList;

import org.simalliance.openmobileapi.Channel;

import com.gieseckedevrient.android.apdutester.TestCase;
import com.gieseckedevrient.android.apdutester.TestPerformer;
import com.gieseckedevrient.android.apdutester.Util;

public class Case3 extends TestCase {
	public final String description = "Legacy test case 3";

	@Override
	public boolean run(TestPerformer pt, Channel cardChannel) 	throws Exception {
		ArrayList<Byte> cmdApduA = new ArrayList<Byte>();
		cmdApduA.add((byte) 0x00); // CLA
		cmdApduA.add((byte) 0x03); // INS
		cmdApduA.add((byte) 0x00); // P1
		cmdApduA.add((byte) 0x00); // P2
		if (pt.getDataLength() == 256) {
			cmdApduA.add((byte) 255); // LC
			cmdApduA.addAll(Util.getData(255)); // DATA
		} else {
			cmdApduA.add((byte) pt.getDataLength()); // LC
			cmdApduA.addAll(Util.getData(pt.getDataLength())); // DATA
		}
		time.add(pt.send(cardChannel, cmdApduA, 0, this));
	
		return error;
	}
}
