package org.simalliance.openmobileapi.service.terminals.SamplePluginTerminal;

import android.content.Context;
import android.util.Log;


public class DummyTerminal {

    private static final String LOG_TAG = "SamplePluginTerminal";
    
    private int channelNr = 0x00;
    
    
	private static String bytesToString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02x ", b & 0xFF));
		}
		return sb.toString();
	}
    
    public DummyTerminal(Context context) {
	}

	public byte[] getAtr() {
		return new byte[] { 0x3B, 0x00 };
	}

	public String getName() {
		return "TEST: DummyTerminal";
	}

	public boolean isCardPresent() {
		return true;
	}

	public void internalConnect() {
		Log.v(LOG_TAG, "internalConnect");
	}

	public void internalDisconnect() {
		Log.v(LOG_TAG, "internalDisconnect");
	}

	public byte[] getSelectResponse() {
		return new byte[] { 0x01, 0x03, 0x03, 0x07 };
	}

	public byte[] internalTransmit(byte[] command) {
		Log.v(LOG_TAG, "internalTransmit: " + bytesToString(command));
		return new byte[] { (byte)0xDE, (byte)0xAD, (byte)0xC0, (byte)0xDE, (byte)(command[0] & 0x03), (byte)0x90, (byte)0x00 };
	}

	public int internalOpenLogicalChannel() {
		Log.v(LOG_TAG, "internalOpenLogicalChannel: default applet");
		return ++channelNr;
	}

	public int internalOpenLogicalChannel(byte[] aid) {
		Log.v(LOG_TAG, "internalOpenLogicalChannel: " + bytesToString(aid));
		return ++channelNr;
	}

	public void internalCloseLogicalChannel(int iChannel) {
		Log.v(LOG_TAG, "internalCloseLogicalChannel: " + iChannel);
		if (iChannel > 0)
			--channelNr;
	}
}