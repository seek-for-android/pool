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

public class Case2 extends TestCase {
	public final String description = "Legacy test case 2";

	@Override
	public boolean run(TestPerformer pt, Channel cardChannel) throws Exception {
		ArrayList<Byte> cmdApduA = new ArrayList<Byte>();
		cmdApduA.add((byte) 0x00); // CLA
		cmdApduA.add((byte) 0x02); // INS
		cmdApduA.add((byte) 0x00); // P1
		cmdApduA.add((byte) 0x00); // P2
		if (pt.getDataLength() == 256)
			cmdApduA.add((byte) 0); // LE
		else
			cmdApduA.add((byte) pt.getDataLength()); // LE
		time.add(pt.send(cardChannel, cmdApduA, pt.getDataLength(), this));
	
		return error;
	}
}
