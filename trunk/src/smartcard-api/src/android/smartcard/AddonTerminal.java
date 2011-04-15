/*
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

package android.smartcard;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;

public class AddonTerminal extends Terminal {
	
	private Object _instance = null;
	private Method _isCardPresent = null;
	private Method _internalConnect = null;
	private Method _internalDisconnect = null;
	private Method _internalTransmit = null;
	private Method _internalOpenLogicalChannel = null;
	private Method _internalOpenLogicalChannelAID = null;
	private Method _internalCloseLogicalChannel = null;
	private Method _getName = null;
	private Method _getAtr = null;
	

	
	public static String[] getPackageNames(Context context)  
	{
		List<String> packageNameList = new LinkedList<String>();
		List<PackageInfo> pis = context.getPackageManager().getInstalledPackages(0);
		for (PackageInfo p : pis) {
			if (p.packageName.startsWith("android.smartcard.terminals.")) {
				packageNameList.add(p.packageName);
			}
		}
		String[] rstrings = new String[packageNameList.size()];
		packageNameList.toArray(rstrings);
		return rstrings;
	}

	public AddonTerminal(Context context, String packageName)  {
		super("Addon", context);

		try {
			Context ctx = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			ClassLoader cl = ctx.getClassLoader();
			Class<?> cls = cl.loadClass(packageName + ".PluginTerminal");
			_instance = cls.getConstructor(new Class[]{Context.class}).newInstance(new Object[]{context});
			if(_instance != null)
			{
			_getAtr = _instance.getClass().getDeclaredMethod("getAtr", (Class<?>[])null);
			_getName = _instance.getClass().getDeclaredMethod("getName", (Class<?>[])null);
			_isCardPresent = _instance.getClass().getDeclaredMethod("isCardPresent", (Class<?>[])null);
			_internalConnect = _instance.getClass().getDeclaredMethod("internalConnect", (Class<?>[])null);
			_internalDisconnect = _instance.getClass().getDeclaredMethod("internalDisconnect", (Class<?>[])null);
			_internalTransmit = _instance.getClass().getDeclaredMethod("internalTransmit", new Class[]{byte[].class});
			_internalOpenLogicalChannel = _instance.getClass().getDeclaredMethod("internalOpenLogicalChannel", (Class<?>[])null);
			_internalOpenLogicalChannelAID = _instance.getClass().getDeclaredMethod("internalOpenLogicalChannel", new Class[]{byte[].class});
			_internalCloseLogicalChannel = _instance.getClass().getDeclaredMethod("internalCloseLogicalChannel", new Class[]{int.class});
			}
		} catch (Exception e) {
			throw new IllegalStateException("plugin internal error: " + e);
		}
	}
	
	/**
	 * Returns the ATR of the connected card or null if the ATR is not available.
	 * @return the ATR of the connected card or null if the ATR is not available.
	 */
	public byte[] getAtr()
	{
		if(_getAtr == null)
			throw new IllegalStateException("plugin error: Function String getAtr() not found");
		try {
			byte[] resp = (byte[])_getAtr.invoke(_instance, (Object[])null);
			return resp;
		} catch (Exception e) {
			throw new IllegalStateException("plugin internal error: getAtr() execution: " + e.getCause());
		}
	}
	
	public String getName()  {
		if(_getName == null)
			throw new IllegalStateException("plugin error: Function String getName() not found");
		try {
			String s = (String)_getName.invoke(_instance, (Object[])null);
			return s;
		} catch (Exception e) {
			throw new IllegalStateException("plugin internal error: getName() execution: " + e.getCause());
		}
	}

	public boolean isCardPresent() throws CardException {
		if(_isCardPresent == null)
			throw new IllegalStateException("plugin error: Function String isCardPresent() not found");
		try {
			Boolean v = (Boolean)_isCardPresent.invoke(_instance, (Object[])null);
			return v.booleanValue();
		} catch (Exception e) {
			throw new CardException("plugin internal error: isCardPresent() execution: " + e.getCause());
		}
	}
	
	protected void internalConnect() throws CardException {
		if(_internalConnect == null)
			throw new IllegalStateException("plugin error: Function String internalConnect() not found");
		try {
			_internalConnect.invoke(_instance, (Object[])null);
			isConnected = true;
		} catch (Exception e) {
			throw new CardException("plugin internal error: internalConnect() execution: " + e.getCause());
		}
	}
	
	protected void internalDisconnect() throws CardException {
		if(_internalDisconnect == null)
			throw new IllegalStateException("plugin error: Function String internalDisconnect() not found");
		try {
			_internalDisconnect.invoke(_instance, (Object[])null);
			isConnected = false;
		} catch (Exception e) {
			throw new CardException("plugin internal error: internalDisconnect() execution");
		}
	}
	
	protected byte[] internalTransmit(byte[] command) throws CardException {
		if(_internalTransmit == null)
			throw new IllegalStateException("plugin error: Function String internalTransmit() not found");
		try {
			byte[] resp = (byte[])_internalTransmit.invoke(_instance, new Object[]{command});
			return resp;
		} catch (Exception e) {
			throw new CardException("plugin internal error: internalTransmit() execution: " + e.getCause());
		}
	}

	protected int internalOpenLogicalChannel() throws Exception {
		if(_internalOpenLogicalChannel == null)
			throw new IllegalStateException("plugin error: Function String internalOpenLogicalChannel() not found");
		try
		{
		Integer channel = (Integer)_internalOpenLogicalChannel.invoke(_instance, (Object[])null);
		return channel.intValue();
		} catch (Exception e) {
			throw (Exception)e.getCause();
		}
	}

	protected int internalOpenLogicalChannel(byte[] aid) throws Exception {
		if(_internalOpenLogicalChannelAID == null)
			throw new IllegalStateException("plugin error: Function internalOpenLogicalChannelAID() not found");
		try{
		Integer channel = (Integer)_internalOpenLogicalChannelAID.invoke(_instance, new Object[]{aid});
		return channel.intValue();
		} catch (Exception e) {
			throw (Exception)e.getCause();
		}
	}
	
	protected void internalCloseLogicalChannel(int channelNumber) throws CardException {
		if(_internalCloseLogicalChannel == null)
			throw new IllegalStateException("plugin error: Function internalCloseLogicalChannel not found");
		try
		{
		_internalCloseLogicalChannel.invoke(_instance, new Object[]{channelNumber});
		} catch (Exception e) {
        throw new CardException("plugin internal error: internalOpenLogicalChannel() execution: " + e.getCause());
		}
	}
}
