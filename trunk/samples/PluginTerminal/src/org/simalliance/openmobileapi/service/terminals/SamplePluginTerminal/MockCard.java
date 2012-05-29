package org.simalliance.openmobileapi.service.terminals.SamplePluginTerminal;

import java.util.Map;
import java.util.Arrays;
import java.util.Hashtable;
import android.util.Log;

/**
 * This class simulates a smart card.
 * It handles C-APDUs for
 * - channel management (MANGE_CHANNEL; open and close) and
 * - applet selection (SELECT)
 * Any other C-APDUs are forwarded to the currently selected applet or answered with SW=6E00.
 *
 */
public class MockCard {

	public static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b: bytes) {
			sb.append(String.format("%02X ", 0xff & b));
		}
		if (sb.length() > 0) sb.setLength(sb.length() - 1); // remove trailing space
		return sb.toString();
	} // bytesToHexString
	
	/**
	 * converts up to a max. number of bytes of an array of bytes into a hex string.
	 * <p>
	 * If n<=10 at least 10 bytes are converted.<br>
	 * If n>10 first n-5 and last 4 bytes are converted.
	 * 
	 * @param bytes          array of bytes to convert to hex string
	 * @param n              max. number of bytes in hex string
	 * @return               hex string representation of input array
	 */
	public static String bytesToMaxHexString(byte[] bytes, int n) {
		if (bytes == null) return "null";
		if (n < 10 || bytes.length <= n) return bytesToHexString(bytes);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < n-5; i++)
			sb.append(String.format("%02X ", 0xff & bytes[i]));
		sb.append(String.format("... %02X %02X %02X %02X", 
				bytes[bytes.length-4] & 0xFF, 
				bytes[bytes.length-3] & 0xFF, 
				bytes[bytes.length-2] & 0xFF, 
				bytes[bytes.length-1] & 0xFF));
		return sb.toString();
	} // bytesToMaxHexString
	
	public static class MockCardException extends Throwable {

		private static final long serialVersionUID = 1L;
		
	    static final int SW_APPLET_SELECT_FAILED           = 0x6999;
	    static final int SW_BYTES_REMAINING_00             = 0x6100;
	    static final int SW_CLA_NOT_SUPPORTED              = 0x6E00;
	    static final int SW_COMMAND_CHAINING_NOT_SUPPORTED = 0x6884;
	    static final int SW_COMMAND_NOT_ALLOWED            = 0x6986;
	    static final int SW_CONDITIONS_NOT_SATISFIED       = 0x6985;
	    static final int SW_CORRECT_LENGTH_00              = 0x6C00;
	    static final int SW_DATA_INVALID                   = 0x6984;
	    static final int SW_FILE_FULL                      = 0x6A84;
	    static final int SW_FILE_INVALID                   = 0x6983;
	    static final int SW_FILE_NOT_FOUND                 = 0x6A82; 
	    static final int SW_FUNC_NOT_SUPPORTED             = 0x6A81;
	    static final int SW_INCORRECT_P1P2                 = 0x6A86;
	    static final int SW_INS_NOT_SUPPORTED              = 0x6D00;
	    static final int SW_LAST_COMMAND_EXPECTED          = 0x6883;
	    static final int SW_LOGICAL_CHANNEL_NOT_SUPPORTED  = 0x6881;
	    static final int SW_NO_ERROR                       = 0x9000;
	    static final int SW_RECORD_NOT_FOUND               = 0x6A83;
	    static final int SW_SECURE_MESSAGING_NOT_SUPPORTED = 0x6882;
	    static final int SW_SECURITY_STATUS_NOT_SATISFIED  = 0x6982;
	    static final int SW_UNKNOWN                        = 0x6F00;
	    static final int SW_WARNING_STATE_UNCHANGED        = 0x6200;
	    static final int SW_WRONG_DATA                     = 0x6A80;
	    static final int SW_WRONG_LENGTH                   = 0x6700;
	    static final int SW_WRONG_P1P2                     = 0x6B00;	

		protected byte[] swBytes = {0, 0};
	    //static MockCardException isoException = new MockCardException();
		
	    public MockCardException(){ }

	    public MockCardException(int sw){ 
	    	swBytes[0] = (byte)(sw>>8);
	    	swBytes[1] = (byte)sw;
	    } // constructor

	    /**
	     * gets the SW for this exception as array of bytes
	     * @return array of two bytes containing SW
	     */
	    public byte[] getSWBytes() { return swBytes; }
	    
	} // class
	
	private static final boolean LOG_VERBOSE = true; //false;
	private static final String TAG = MockCard.class.getSimpleName();

	protected static final int INS_SELECT = 0xa4;
	protected static final int INS_MANAGE_CHANNEL = 0x70;

	protected static final int N_CHANNELS = 20;

	//                                        TS          T0          TCK 
	public static final byte[] ATR = {(byte)0x3B, (byte)0x0E, (byte)0x0E};
	
	
	class AID {
    	protected byte[] aidBytes;
    	protected int hashCode;
    	AID(){}
    	AID(byte[] aidBytes){ setValue(aidBytes); }
    	AID(byte[] aidBytes, int offset, int length){ setValue(Arrays.copyOfRange(aidBytes, offset, offset+length)); }
    	public void setValue(byte[] aidBytes){ this.aidBytes=aidBytes; hashCode=Arrays.hashCode(aidBytes); }
    	public int hashCode(){ return hashCode; }
    	public boolean equals(Object o){ return Arrays.equals(aidBytes, ((AID)o).aidBytes); }
  	} // class
	
	/**
	 * maps AID to installed applet
	 */
	protected Map<AID,Applet> aid2applet;

	protected boolean[] isChannelOpen = new boolean[N_CHANNELS];

	/** 
	 * maps channel number (index) to selected applet (or null if none is selected)
	 */
	protected Applet[] selectedApplet = new Applet[N_CHANNELS];
	
	/**
	 * constructs a Mockcard object with no applets installed
	 */
	public MockCard() {
		aid2applet = new Hashtable<AID,Applet>();
		reset();
	} // constructor

	public byte[] getATR(){ return ATR; }
	
	/** 
	 * installs an applet with a given AID
	 * 
	 * @param aidBytes
	 * @param applet
	 */
	public void installApplet(byte[] aidBytes, Applet applet) {
		//if (LOG_VERBOSE) 
		//	Log.v(TAG, "MockCard: install: AID="+Util.bytesToHexString(aidBytes)+" --> "+
		//			   "Applet \""+applet.getClass().getName()+"\"");
		
		AID aid = new AID(aidBytes);
		if (aid2applet.containsKey(aid))
			throw new Error("Tried to install with same AID more than once");
		aid2applet.put(aid, applet);
	} // installApplet

	/**
	 * handles logical channels (MANAGE_CHANNEL)
	 * and applet selection (SELECT)
	 * 
	 * @param iChannel
	 * @param command
	 * @return
	 *   1..19 (new) channel number after MANAGE_CHANNEL, open, P2=0
	 *   0                          after MANAGE_CHANNEL, close 
	 *                              or    MANAGE_CHANNEL, open with P2>0
	 *  -1                          after SELECT
	 *  -2                          after any other C-APDU
	 * @throws MockCardException
	 */
	protected int cardManager(int iChannel, byte[] command) throws MockCardException {
		int ins = 0xff & command[1];
		int p1  = 0xff & command[2];
		int p2  = 0xff & command[3];
		int p3  = command.length>4? 0xff & command[4]: 0;
		
		switch (ins) {
			case INS_SELECT: {
				// command SELECT:
				Applet a = aid2applet.get(new AID(command, 5, p3));
				if (a == null) 
					throw new MockCardException(MockCardException.SW_FILE_NOT_FOUND);
				isChannelOpen[iChannel] = true;
				selectedApplet[iChannel] = a;
				return -1; // indicate that command is SELECT
			}
		
			case INS_MANAGE_CHANNEL: {
				// command MANAGE_CHANNEL:
				switch (p1) {
					case 0x00:
						// open logical channel:
						if (p2 == 0) {
							if (p3 != 1)
								throw new MockCardException(MockCardException.SW_WRONG_LENGTH);
							for (iChannel = 1; iChannel < N_CHANNELS; iChannel++)
								if (!isChannelOpen[iChannel]) break;
							if (iChannel >= N_CHANNELS)
								throw new MockCardException(MockCardException.SW_FUNC_NOT_SUPPORTED);
							isChannelOpen[iChannel] = true;
							selectedApplet[iChannel] = null;
							return iChannel;
						}
						if (p2<N_CHANNELS) {
							if (isChannelOpen[p2])
								throw new MockCardException(MockCardException.SW_WARNING_STATE_UNCHANGED);
							isChannelOpen[p2] = true;
							selectedApplet[p2] = null;
							return 0;
						}
						throw new MockCardException(MockCardException.SW_INCORRECT_P1P2);	

					case 0x80:
						// close logical channel:
						if (p2 != iChannel || p2 >= N_CHANNELS)
							throw new MockCardException(MockCardException.SW_INCORRECT_P1P2);
						if (p2 == 0) 
							return 0; // basic channel can't be closed; nevertheless return 9000
						if (!isChannelOpen[p2])
							throw new MockCardException(MockCardException.SW_WARNING_STATE_UNCHANGED);
						isChannelOpen[p2] = false;
						selectedApplet[p2] = null;
						return 0; 

					default:
						throw new MockCardException(MockCardException.SW_INCORRECT_P1P2);
				}
			}
		
			default:
				return -2; // indicate that command is neither MANAGE_CHANNEL nor SELECT
		}
	} // cardManager
	
	/** 
	 * simulates a reset
	 */
	public void reset() {
		for(int i = 1; i < isChannelOpen.length; i++) {
			isChannelOpen[i] = false;
			selectedApplet[i] = null;
		}
		isChannelOpen[0] = true; // basic channel is open
		selectedApplet[0] = null;
	} // reset
	
	/**
	 * simulates APDU processing
	 * 
	 * @param command
	 * @return
	 */
	public byte[] process(byte[] command) {
		int iChannel;
		int cla;
		byte[] response;
		
		if (LOG_VERBOSE) {
			String s = "";
			if (Arrays.equals(Arrays.copyOfRange(command, 1, 18),
					          new byte[]{(byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x0D, (byte)0xD2, (byte)0x76, (byte)0x00, (byte)0x01, (byte)0x18, (byte)0xAA, (byte)0xFF, (byte)0xFF, (byte)0x49, (byte)0x10, (byte)0x48, (byte)0x89, (byte)0x01}))
				s = " = select ACA";
			Log.v(TAG, "process: ----------------------------------------------------------------------------");
			Log.v(TAG, "process: C-APDU = "+bytesToMaxHexString(command, 20)+s);
		}

		// extract channel number and remove channel number bits from CLA variable:
		cla= 0xff & command[0];
		if ((0x40 & cla) == 0) {
			iChannel = 0x03 & cla;
			cla &= 0xfc;
		}
		else {
			iChannel = 4 + (0x0f & cla);
			cla &= 0xb0;
		}
		
		try {
			boolean isSelect = false;
			if (cla == (byte)0x00) {
				int r = cardManager(iChannel, command);
				if (r > 0) { 
					// command is MANAGE_CHANNEL, open with P2=0:
					if (LOG_VERBOSE) Log.v(TAG, String.format("process: R-APDU = %02X 90 00",r));
					return new byte[]{(byte)r, (byte)0x90, (byte)0x00};
				}
				if (r == 0) { 
					// command is MANAGE_CHANNEL, close 
					// or         MANAGE_CHANNEL, open with P2>0:
					if (LOG_VERBOSE) Log.v(TAG, "process: R-APDU = 90 00");
					return new byte[]{(byte)0x90, (byte)0x00};
				}
				if (r == -1)
					isSelect = true; // command is SELECT
			}

			if (!isChannelOpen[iChannel] || selectedApplet[iChannel] == null) 
				throw new MockCardException(MockCardException.SW_CLA_NOT_SUPPORTED);
			
			// hand over to selected applet:
			selectedApplet[iChannel].mockSetSelect(isSelect);
			response = selectedApplet[iChannel].process(command);
	
			// append SW=0x9000:
			int l = response.length+2;
			response = Arrays.copyOf(response, l);
			response[l-2] = (byte)0x90;
			response[l-1] = (byte)0x00; 
		}
		catch(MockCardException e) {
			response = e.getSWBytes();
		}
		catch(Throwable th) {
			if (LOG_VERBOSE) {
				Log.v(TAG, "process: "+th);
			}
			response = new byte[]{(byte)0x6f, (byte)0xff};
		}
		if (LOG_VERBOSE) Log.v(TAG, "process: R-APDU = " + bytesToMaxHexString(response, 20));
		return response;
	} // process
	
} // class 
