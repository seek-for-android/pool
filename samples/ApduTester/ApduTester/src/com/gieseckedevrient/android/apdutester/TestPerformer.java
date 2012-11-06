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
import java.util.ArrayList;
import java.util.Random;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;

import android.content.Context;

import com.gieseckedevrient.android.apdutester.TestCases.Case1;
import com.gieseckedevrient.android.apdutester.TestCases.Case2;
import com.gieseckedevrient.android.apdutester.TestCases.Case3;
import com.gieseckedevrient.android.apdutester.TestCases.Case4;


public class TestPerformer implements org.simalliance.openmobileapi.SEService.CallBack {

	static public SEService seService;
	private Reader reader = null;
	private Context context;
	private Reader[] readers;

	private SystemDataWatcher sysDataWatcher;
	private TestResultListener listener;
	private ArrayList<TestCase> testCases = new ArrayList<TestCase>();
	private Random randomGenerator = new Random();
	private boolean isReady = false;
	public volatile boolean testFinished = false;
	private DataContainer dataContainer;


	public TestPerformer(Context context, TestResultListener listener) {
		this.context = context;
		this.listener = listener;

		testCases.add(new Case1());
		testCases.add(new Case2());
		testCases.add(new Case3());
		testCases.add(new Case4());

		dataContainer = DataContainer.getInstance();
		
		this.sysDataWatcher = new SystemDataWatcher();
	}

	public void connectToService() {
		listener.logText("Connecting to SmartCardService...\n");
		try {
			seService = new SEService(context, this);
		} catch (SecurityException e) {
			listener.logText("Binding not allowed, permission 'org.simalliance.openmobileapi.SMARTCARD' not sufficient?");
		} catch (Exception e) {
			listener.logText(e.toString());
		}
	}

	public void serviceConnected(SEService service) {
		listener.logText("SmartCardService connected\n");
		try {
			setReaders(seService.getReaders());
			if (getReaders().length > 0)
				setReader(getReaders()[0]);
		} catch (Exception e) {
			listener.logText("Exception: " + e.getMessage());
		}

		isReady = true;
		listener.uiUpdate();
	}

	public void onDestroy() {
		if (seService != null) {
			listener.logText("Disconnecting from SmartCardService\n");
			seService.shutdown();
		}
	}

	protected void clearErrors() {
		for (TestCase c : testCases)
			c.clearError();
	}

	protected void clearTimeValues() {
		for (TestCase c : testCases)
			c.time.clear();
	}

	public void runApduCaseTest(final Context context) {
		final boolean activated[] = {dataContainer.getCase1(), dataContainer.getCase2(), dataContainer.getCase3(), dataContainer.getCase4()};
		
		testFinished = false;
		if (activated.length != testCases.size()) {
			throw new IllegalArgumentException("activated.length != testCases.size");
		}

		Thread thread1 = new Thread() {
			public void run() {

				Channel channel = null;
				try {
					File file = listener.openNewLogFile();

					TestPerformer.this.sysDataWatcher.activateBatteryLevelMeassurement(context);

					clearData();

					if (getReader().equals("")) {
						listener.logText("\nReader not available\n");
						return;
					}

					if (!getReader().isSecureElementPresent()) {
						listener.logText("\nCard not available\n");
						return;
					}

					Session session = getReader().openSession();

					if (TestPerformer.this.isBasicChannel()) {
						listener.logText("\nOpen Basic Channel to Applet: " + Util.bytesToString(DataContainer.APPLET_AID) 	+ "\n");
						channel = session.openBasicChannel(DataContainer.APPLET_AID);
					} else {
						listener.logText("\nOpen Logical Channel to Applet: " + Util.bytesToString(DataContainer.APPLET_AID) + "\n");
						channel = session.openLogicalChannel(DataContainer.APPLET_AID);
					}
					
					byte[] selectResponseData = channel.getSelectResponse();
					if (selectResponseData != null)
					    listener.logText("Applet select response: " + Util.bytesToString(selectResponseData) + "\n");
					
					if (dataContainer.getDelays())
						randomGenerator.setSeed(10);

					boolean errorOccured = false;
					int iLoop = 1;
					long start = System.currentTimeMillis();
					do {
						listener.logText("\n================ Loop: " + iLoop + " ================\n");

						for (int c = 0; c < testCases.size(); c++) {
							if (activated[c]) {
								listener.logText("\nTestcase CASE-" + (c + 1) + " APDU\n");
								errorOccured = testCases.get(c).run(TestPerformer.this, channel);
								
								if (errorOccured == true && dataContainer.getErrors() == true)
									break;
							}
						}

						sysDataWatcher.messureMemoryUsage(context);
						
						if (errorOccured == true && dataContainer.getErrors() == true)
							break;

						if (dataContainer.getDelays()) {
							int waitTime = randomGenerator.nextInt(1000);
							listener.logText("wait " + waitTime + " ms...\n");
							Thread.sleep(waitTime);
						}
						iLoop++;
					} while (((getLoops() == 0) ? true : iLoop <= getLoops()) && (testFinished == false));
					long end = System.currentTimeMillis();
					long exeTime = (end - start);
					testFinished = true;
					
					int preSpaces = 15;
					int inSpaces = 10;
					listener.logText("\n\n\n\n================ Summary ================\n");
					listener.logText(Util.fill("Reader", preSpaces) + getReader().getName() + "\n");
					listener.logText(Util.fill("Channel", preSpaces) + (TestPerformer.this.isBasicChannel() ? "Basic Channel" : "Logical Channel") + "\n");
					listener.logText(Util.fill("Data length", preSpaces) + getDataLength() + "\n");
					if (errorOccured == true) 
						listener.logText(Util.fill("Loops", preSpaces) + getLoops() + " - executed loops: " + iLoop + "\n");
					else
						listener.logText(Util.fill("Loops", preSpaces) + getLoops() + "\n");							
					listener.logText(Util.fill("Delays", preSpaces) + (dataContainer.getDelays() ? "enabled" : "disabled") + "\n");
					listener.logText(Util.fill("Execution time", preSpaces) + String.format("%.2f", (float)exeTime/1000) + "s\n");
					listener.logText(Util.fill("Response data", preSpaces));
					if (selectResponseData == null) { 
					    listener.logText("no response data retrieved\n");
					} else {
					    listener.logText(Util.bytesToString(selectResponseData) + "\n");
					}
					
					listener.logText("\nExecution time (average)\n");
					for (int c = 0; c < testCases.size(); c++) {
						TestCase testCase = testCases.get(c);
						if (activated[c])
							listener.logText(Util.fill("Test Case-" + (c+1), preSpaces) + Util.getAvarageValue(testCase.time) + " ms " + (testCase.didErrorOccured() ? "(errors occured!)" : "") + "\n");
						else
							listener.logText(Util.fill("Test Case-" + (c+1), preSpaces) + ": not executed\n");
					}

					MemoryInfo mf = sysDataWatcher.getMemoryAvg();
					listener.logText("\nMemory usage (average):\n");
					listener.logText(Util.fill("", preSpaces) + Util.fill("native", inSpaces) + Util.fill("dalvik", inSpaces) + Util.fill("total", inSpaces) + "\n");
					listener.logText(Util.fill("Size", preSpaces) + Util.fill(mf.sizeNative, inSpaces) + Util.fill(mf.sizeDalvik, inSpaces) + Util.fill(mf.sizeTotal, inSpaces) + "\n");
					listener.logText(Util.fill("Allocated", preSpaces) + Util.fill(mf.allocatedNative, inSpaces) + Util.fill(mf.allocatedDalvik, inSpaces) + Util.fill(mf.allocatedTotal, inSpaces) + "\n");
					listener.logText(Util.fill("Free", preSpaces) + Util.fill(mf.freeNative, inSpaces) + Util.fill(mf.freeDalvik, inSpaces) + Util.fill(mf.freeTotal, inSpaces) + "\n\n");

					long avgCpuTimeAvg = Util.getAvarageValue(sysDataWatcher.cpuUsageValues);

					listener.logText(Util.fill("CPU usage", preSpaces) + avgCpuTimeAvg + " % \n");

					SystemDataWatcher sysDataWatcher = TestPerformer.this.sysDataWatcher;
					listener.logText(Util.fill("Battery begin", preSpaces) + sysDataWatcher.batteryLevels.get(0) + "% \n");
					listener.logText(Util.fill("Battery end", preSpaces) + sysDataWatcher.batteryLevels.get(sysDataWatcher.batteryLevels.size() - 1) + "% \n");

					if (file != null)
						listener.logText(Util.fill("\nLogfile", preSpaces) + file);
				} catch (Exception e) {
					listener.logText(e.toString());
				} finally {
					try {
						if (channel != null && !channel.isClosed())
							channel.close();

						while (!listener.allMessagesHandled())
							Thread.sleep(1000);

						listener.closeLogFile();
					} catch (Exception e) {
						listener.logText(e.toString());
					}
					
					listener.testFinished();
				}
			}
		};

		Thread thread2 = new Thread() {
			public void run() {

				while (!testFinished) {
					sysDataWatcher.getCpuUsage();
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
				}
			}
		};

		thread1.start();
		thread2.start();
	}

	public long send(Channel channel, ArrayList<Byte> cmdApduA, int refDataLength, TestCase c) throws Exception {
		byte[] cmdApdu = Util.getArray(cmdApduA);
		listener.logText("-> " + Util.bytesToString(cmdApdu) + "\n");
		long start = System.currentTimeMillis();
		byte[] rspApdu;
		try {
			rspApdu = channel.transmit(cmdApdu);
		} catch (Exception e) {
			rspApdu = new byte[0];
		}
		long end = System.currentTimeMillis();
		long exeTime = (end - start);
		listener.logText("<- " + Util.bytesToString(rspApdu) + "\n");
		listener.logText("Time: " + exeTime + " ms\n");
		byte[] refData = Util.getArray(Util.getData(refDataLength));
		byte[] acData = Util.stripSW1SW2(rspApdu);
		int sw1sw2 = Util.getSW1SW2(rspApdu);
		if (sw1sw2 == 0x9000 && Util.isEqual(refData, acData)) {
			listener.logText("Ok: test succeeded\n");
		} else {
			listener.logText("Error: test failed (expected response " + Util.bytesToString(refData) + ")\n");
			c.errorOccurred();
		}
		return exeTime;
	}

	protected void clearData() {
		clearErrors();
		sysDataWatcher.clearData();
		clearTimeValues();
	}

	public boolean isReady() {
		return isReady;
	}

	public boolean isBasicChannel() {
		return dataContainer.getBasicChannel();
	}

	public int getDataLength() {
		return dataContainer.getDataLength();
	}

	public int getLoops() {
		return dataContainer.getLoops();
	}
	
	public Reader[] getReaders() {
		return readers;
	}

	public void setReaders(Reader[] readers) {
		this.readers = readers;
	}

	public Reader getReader() {
		return reader;
	}

	public void setReader(Reader reader) {
		this.reader = reader;
	}

}
