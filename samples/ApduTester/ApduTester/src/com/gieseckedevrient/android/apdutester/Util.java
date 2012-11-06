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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

public class Util {

	static String bytesToString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02x ", b & 0xFF));
		}
		return sb.toString();
	}

	static String fill(long value, int length) {
		return Util.fill(new Long(value).toString(), length);
	}

	public static String fill(String string, int length) {
		StringBuffer sb = new StringBuffer();
		sb.append(string);
		int effLength = length - string.length();
		for (int i = 0; i < effLength; i++)
			sb.append(' ');
		return sb.toString();
	}

	static byte[] getArray(ArrayList<Byte> arrayList) {
		byte[] array = new byte[arrayList.size()];
		int i = 0;
		for (Byte value : arrayList) {
			array[i] = value.byteValue();
			i++;
		}
		return array;
	}

	static long getAvarageValue(ArrayList<Long> times) {
		long num = times.size();
		if (num == 0)
			return 0;
		long timeSum = 0;
		for (long time : times)
			timeSum += time;
		long avgTime = timeSum / num;
		return avgTime;
	}

	public static ArrayList<Byte> getData(int dataLength) {
		ArrayList<Byte> data = new ArrayList<Byte>(dataLength);
		for (int i = 0; i < dataLength; i++)
			data.add((byte) i);
		return data;
	}

	static public int getSmartcardApiPid(Context context) {
		ActivityManager actvityManager = (ActivityManager) context	.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procInfos = actvityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo procInfo : procInfos) {
			if (procInfo.processName.equals("org.simalliance.openmobileapi") || procInfo.processName.equals("org.simalliance.openmobileapi:remote"))
				return procInfo.pid;
		}
		return 0;
	}

	static int getSW1SW2(byte[] data) {
		if (data.length >= 2) {
			int sw1sw2 = ((byte) data[data.length - 2] << 8) & 0xFF00;
			sw1sw2 |= (byte) data[data.length - 1] & 0x00FF;
			return sw1sw2;
		} else
			return 0;
	}

	static public String getTimeStamp() {
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return df.format(dt);
	}

	static boolean isEqual(byte[] refData, byte[] acData) {
		if (refData.length != acData.length) {
			return false;
		}

		for (int i = 0; i < refData.length; i++) {
			if ((byte) refData[i] != (byte) acData[i]) {
				return false;
			}
		}
		return true;
	}

	static String[] removeWithspaces(String[] values) {
		ArrayList<String> list = new ArrayList<String>(values.length);
		for (int i = 0; i < values.length; i++) {
			if (!values[i].equals("") && !values[i].equals(" ")
					&& !values[i].equals("\t"))
				list.add(values[i]);
		}
		return list.toArray(new String[list.size()]);
	}

	static byte[] stripSW1SW2(byte[] data) {
		if (data.length > 2) {
			byte[] strippedData = new byte[data.length - 2];
			for (int i = 0; i < data.length - 2; i++)
				strippedData[i] = data[i];
			return strippedData;
		} else
			return new byte[0];
	}
}
