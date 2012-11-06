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

import java.io.File;

import android.os.Environment;

public class DataContainer {

	public static final byte[] APPLET_AID = new byte[] { (byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x01, 0x01 };

	private static DataContainer instance = null;

	private boolean basicChannel = false;
	private boolean delays = false;
	private boolean errors = true;
	private boolean case1 = true;
	private boolean case2 = true;
	private boolean case3 = true;
	private boolean case4 = true;
	private int loops = 1;
	private int dataLength = 250;
	private String logPath = "/sdcard";
	
	
    private  DataContainer() {
		logPath = "";
		try {
			File path = new File(Environment.getExternalStorageDirectory().toString());
	    	if (path.isDirectory() && path.canWrite())
				logPath = path.getAbsolutePath();
		} catch (Exception e) {	
		}
    }
    
    public static DataContainer getInstance() {
    	if (instance == null) {
    		instance = new DataContainer();
        }
        return instance;
    }
	
	public void setBasicChannel(boolean basicChannel) {
		this.basicChannel = basicChannel;
	}
	
	public boolean getBasicChannel() {
		return basicChannel;
	}
	
	public void setDelays(boolean delays) {
		this.delays = delays;
	}
	
	public boolean getDelays() {
		return delays;
	}
	
	public void setErrors(boolean errors) {
		this.errors = errors;
	}
	
	public boolean getErrors() {
		return errors;
	}
	
	public void setCase1(boolean case1) {
		this.case1 = case1;
	}
	
	public boolean getCase1() {
		return case1;
	}
	
	public void setCase2(boolean case2) {
		this.case2 = case2;
	}
	
	public boolean getCase2() {
		return case2;
	}
	
	public void setCase3(boolean case3) {
		this.case3 = case3;
	}
	
	public boolean getCase3() {
		return case3;
	}
	
	public void setCase4(boolean case4) {
		this.case4 = case4;
	}
	
	public boolean getCase4() {
		return case4;
	}
	
	public void setLoops(int loops) {
		this.loops = loops;
	}
	
	public int getLoops() {
		return loops;
	}
	
	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}
	
	public int getDataLength() {
		return dataLength;
	}
	
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	
	public String getLogPath() {
		return logPath;
	}
}
