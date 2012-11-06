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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class SystemDataWatcher {
	
	ArrayList<Integer> batteryLevels = new ArrayList<Integer>();

	ArrayList<Long> freeNativeValues = new ArrayList<Long>();
	ArrayList<Long> freeDalvikValues = new ArrayList<Long>();
	ArrayList<Long> freeTotalValues = new ArrayList<Long>();

	ArrayList<Long> allocatedNativeValues = new ArrayList<Long>();
	ArrayList<Long> allocatedDalvikValues = new ArrayList<Long>();
	ArrayList<Long> allocatedTotalValues = new ArrayList<Long>();

	ArrayList<Long> sizeNativeValues = new ArrayList<Long>();
	ArrayList<Long> sizeDalvikValues = new ArrayList<Long>();
	ArrayList<Long> sizeTotalValues = new ArrayList<Long>();

	ArrayList<Long> cpuUsageValues = new ArrayList<Long>();
	
	public void getCpuUsage() {
		try {
			String cmd = "top -n 1";
			Runtime rt = Runtime.getRuntime();
			Process ps = rt.exec(cmd);
			ps.waitFor();
			BufferedReader rd = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				line = line.trim();
				String[] values = Util.removeWithspaces(line.split("[\t ]"));
				for (String value : values) {
					if (value.equals("org.simalliance.openmobileapi") || value.equals("org.simalliance.openmobileapi:remote")) {
						values[1] = values[1].replace("%", "");
						long cpuUsage = Long.parseLong(values[1]);
						cpuUsageValues.add(cpuUsage);
						break;
					}
				}
			}
			rd.close();
		} catch (Exception ex) {
		}
	}
	
	protected void clearMemoryUsageValues() {
		freeNativeValues.clear();
		freeDalvikValues.clear();
		freeTotalValues.clear();

		allocatedNativeValues.clear();
		allocatedDalvikValues.clear();
		allocatedTotalValues.clear();

		sizeNativeValues.clear();
		sizeDalvikValues.clear();
		sizeTotalValues.clear();
	}

	protected void clearCpuTimeValues() {
		cpuUsageValues.clear();
	}

	void clearData() {
		clearMemoryUsageValues();
		clearCpuTimeValues();
	}
	
	MemoryInfo getMemoryAvg() {
		MemoryInfo mf = new MemoryInfo();

		mf.freeNative = Util.getAvarageValue(freeNativeValues);
		mf.freeDalvik = Util.getAvarageValue(freeDalvikValues);
		mf.freeTotal = Util.getAvarageValue(freeTotalValues);

		mf.allocatedNative = Util.getAvarageValue(allocatedNativeValues);
		mf.allocatedDalvik = Util.getAvarageValue(allocatedDalvikValues);
		mf.allocatedTotal = Util.getAvarageValue(allocatedTotalValues);

		mf.sizeNative = Util.getAvarageValue(sizeNativeValues);
		mf.sizeDalvik = Util.getAvarageValue(sizeDalvikValues);
		mf.sizeTotal = Util.getAvarageValue(sizeTotalValues);

		return mf;
	}
	
	public void messureMemoryUsage(Context context) {
		try {

			MemoryInfo mf = new MemoryInfo();
			String cmd = "dumpsys meminfo " + Util.getSmartcardApiPid(context);
			Runtime rt = Runtime.getRuntime();
			Process ps = rt.exec(cmd);
			ps.waitFor();
			BufferedReader rd = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("size")) {
					String[] values = Util.removeWithspaces(line.split("[\t ]"));
					mf.sizeNative = Long.parseLong(values[1]);
					mf.sizeDalvik = Long.parseLong(values[2]);
					mf.sizeTotal = Long.parseLong(values[4]);
					sizeNativeValues.add(mf.sizeNative);
					sizeDalvikValues.add(mf.sizeDalvik);
					sizeTotalValues.add(mf.sizeTotal);
				}
				if (line.startsWith("allocated")) {
					String[] values = Util.removeWithspaces(line.split("[\t ]"));
					mf.allocatedNative = Long.parseLong(values[1]);
					mf.allocatedDalvik = Long.parseLong(values[2]);
					mf.allocatedTotal = Long.parseLong(values[4]);
					allocatedNativeValues.add(mf.allocatedNative);
					allocatedDalvikValues.add(mf.allocatedDalvik);
					allocatedTotalValues.add(mf.allocatedTotal);
				}
				if (line.startsWith("free")) {
					String[] values = Util.removeWithspaces(line.split("[\t ]"));
					mf.freeNative = Long.parseLong(values[1]);
					mf.freeDalvik = Long.parseLong(values[2]);
					mf.freeTotal = Long.parseLong(values[4]);
					freeNativeValues.add(mf.freeNative);
					freeDalvikValues.add(mf.freeDalvik);
					freeTotalValues.add(mf.freeTotal);
				}

			}
			rd.close();

		} catch (Exception ex) {
		}
	}
	
	public void activateBatteryLevelMeassurement(Context context) {
		batteryLevels.clear();

		BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				context.unregisterReceiver(this);
				int rawlevel = intent.getIntExtra("level", -1);
				int scale = intent.getIntExtra("scale", -1);
				int level = -1;
				if (rawlevel >= 0 && scale > 0) {
					level = (rawlevel * 100) / scale;
					batteryLevels.add(level);
				}
			}
		};
		IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		context.registerReceiver(batteryLevelReceiver, batteryLevelFilter);
	}
}
