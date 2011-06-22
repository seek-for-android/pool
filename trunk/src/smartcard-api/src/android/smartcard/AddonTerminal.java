/*
 * Copyright (C) 2011, The Android Open Source Project
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
/*
 * Contributed by: Giesecke & Devrient GmbH.
 */

package android.smartcard;

import android.content.Context;
import android.content.pm.PackageInfo;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class AddonTerminal extends Terminal {

    private Object mInstance = null;

    private Method mIsCardPresent = null;

    private Method mInternalConnect = null;

    private Method mInternalDisconnect = null;

    private Method mInternalTransmit = null;

    private Method mInternalOpenLogicalChannel = null;

    private Method mInternalOpenLogicalChannelAID = null;

    private Method mInternalCloseLogicalChannel = null;

    private Method mGetName = null;

    private Method mGetAtr = null;

    public static String[] getPackageNames(Context context) {
        List<String> packageNameList = new LinkedList<String>();
        List<PackageInfo> pis = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo p : pis) {
            if (p.packageName.startsWith("android.smartcard.terminals.")
                    || p.packageName.startsWith("android.smartcard.cts")
                    || p.packageName.startsWith("org.simalliance.openmobileapi.cts")) {
                packageNameList.add(p.packageName);
            }
        }
        String[] rstrings = new String[packageNameList.size()];
        packageNameList.toArray(rstrings);
        return rstrings;
    }

    public AddonTerminal(Context context, String packageName, String className) {
        super("Addon", context);

        try {
            Context ctx = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY
                    | Context.CONTEXT_INCLUDE_CODE);
            ClassLoader cl = ctx.getClassLoader();
            Class<?> cls = cl.loadClass(className);
            mInstance = cls.getConstructor(new Class[] {
                Context.class
            }).newInstance(new Object[] {
                context
            });
            if (mInstance != null) {
                mGetAtr = mInstance.getClass().getDeclaredMethod("getAtr", (Class<?>[]) null);
                mGetName = mInstance.getClass().getDeclaredMethod("getName", (Class<?>[]) null);
                mIsCardPresent = mInstance.getClass().getDeclaredMethod("isCardPresent",
                        (Class<?>[]) null);
                mInternalConnect = mInstance.getClass().getDeclaredMethod("internalConnect",
                        (Class<?>[]) null);
                mInternalDisconnect = mInstance.getClass().getDeclaredMethod("internalDisconnect",
                        (Class<?>[]) null);
                mInternalTransmit = mInstance.getClass().getDeclaredMethod("internalTransmit",
                        new Class[] {
                            byte[].class
                        });
                mInternalOpenLogicalChannel = mInstance.getClass().getDeclaredMethod(
                        "internalOpenLogicalChannel", (Class<?>[]) null);
                mInternalOpenLogicalChannelAID = mInstance.getClass().getDeclaredMethod(
                        "internalOpenLogicalChannel", new Class[] {
                            byte[].class
                        });
                mInternalCloseLogicalChannel = mInstance.getClass().getDeclaredMethod(
                        "internalCloseLogicalChannel", new Class[] {
                            int.class
                        });
            }
        } catch (Exception e) {
            throw new IllegalStateException("plugin internal error: " + e);
        }
    }

    /**
     * Returns the ATR of the connected card or null if the ATR is not
     * available.
     * 
     * @return the ATR of the connected card or null if the ATR is not
     *         available.
     */
    public byte[] getAtr() {
        if (mGetAtr == null) {
            throw new IllegalStateException("plugin error: Function String getAtr() not found");
        }
        try {
            byte[] resp = (byte[]) mGetAtr.invoke(mInstance, (Object[]) null);
            return resp;
        } catch (Exception e) {
            throw new IllegalStateException("plugin internal error: getAtr() execution: "
                    + e.getCause());
        }
    }

    public String getName() {
        if (mGetName == null) {
            throw new IllegalStateException("plugin error: Function String getName() not found");
        }
        try {
            String s = (String) mGetName.invoke(mInstance, (Object[]) null);
            return s;
        } catch (Exception e) {
            throw new IllegalStateException("plugin internal error: getName() execution: "
                    + e.getCause());
        }
    }

    public boolean isCardPresent() throws CardException {
        if (mIsCardPresent == null) {
            throw new IllegalStateException(
                    "plugin error: Function String isCardPresent() not found");
        }
        try {
            Boolean v = (Boolean) mIsCardPresent.invoke(mInstance, (Object[]) null);
            return v.booleanValue();
        } catch (Exception e) {
            throw new CardException("plugin internal error: isCardPresent() execution: "
                    + e.getCause());
        }
    }

    protected void internalConnect() throws CardException {
        if (mInternalConnect == null) {
            throw new IllegalStateException(
                    "plugin error: Function String internalConnect() not found");
        }
        try {
            mInternalConnect.invoke(mInstance, (Object[]) null);
            mIsConnected = true;
        } catch (Exception e) {
            throw new CardException("plugin internal error: internalConnect() execution: "
                    + e.getCause());
        }
    }

    protected void internalDisconnect() throws CardException {
        if (mInternalDisconnect == null) {
            throw new IllegalStateException(
                    "plugin error: Function String internalDisconnect() not found");
        }
        try {
            mInternalDisconnect.invoke(mInstance, (Object[]) null);
            mIsConnected = false;
        } catch (Exception e) {
            throw new CardException("plugin internal error: internalDisconnect() execution");
        }
    }

    protected byte[] internalTransmit(byte[] command) throws CardException {
        if (mInternalTransmit == null) {
            throw new IllegalStateException(
                    "plugin error: Function String internalTransmit() not found");
        }
        try {
            byte[] resp = (byte[]) mInternalTransmit.invoke(mInstance, new Object[] {
                command
            });
            return resp;
        } catch (Exception e) {
            throw new CardException("plugin internal error: internalTransmit() execution: "
                    + e.getCause());
        }
    }

    protected int internalOpenLogicalChannel() throws Exception {
        if (mInternalOpenLogicalChannel == null) {
            throw new IllegalStateException(
                    "plugin error: Function String internalOpenLogicalChannel() not found");
        }
        try {
            Integer channel = (Integer) mInternalOpenLogicalChannel.invoke(mInstance,
                    (Object[]) null);
            return channel.intValue();
        } catch (Exception e) {
            throw (Exception) e.getCause();
        }
    }

    protected int internalOpenLogicalChannel(byte[] aid) throws Exception {
        if (mInternalOpenLogicalChannelAID == null) {
            throw new IllegalStateException(
                    "plugin error: Function internalOpenLogicalChannelAID() not found");
        }
        try {
            Integer channel = (Integer) mInternalOpenLogicalChannelAID.invoke(mInstance,
                    new Object[] {
                        aid
                    });
            return channel.intValue();
        } catch (Exception e) {
            throw (Exception) e.getCause();
        }
    }

    protected void internalCloseLogicalChannel(int channelNumber) throws CardException {
        if (mInternalCloseLogicalChannel == null) {
            throw new IllegalStateException(
                    "plugin error: Function internalCloseLogicalChannel not found");
        }
        try {
            mInternalCloseLogicalChannel.invoke(mInstance, new Object[] {
                channelNumber
            });
        } catch (Exception e) {
            throw new CardException(
                    "plugin internal error: internalOpenLogicalChannel() execution: "
                            + e.getCause());
        }
    }
}
