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
package com.gieseckedevrient.android.apdutester;

import java.util.ArrayList;

import org.simalliance.openmobileapi.Channel;


public abstract class TestCase {
	
	public final String description = "";

	protected boolean error = false;

	protected ArrayList<Long> time = new ArrayList<Long>();

	public abstract boolean run(TestPerformer pt, Channel channel) throws Exception;

	public boolean didErrorOccured() {
		return error;
	}

	public void clearError() {
		error = false;
	}

	public void errorOccurred() {
		error = true;
	}

	public ArrayList<Long> getTime() {
		return time;
	}
}
