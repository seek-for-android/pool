package android.smartcard;

import android.content.Context;
import android.os.ServiceManager;
import android.os.SystemProperties;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.telephony.IccUtils;

public class UiccTerminal  extends Terminal {
	
	private ITelephony manager = null;
	private int[] channelId = new int[4];
	
	public UiccTerminal(Context context) {
		super("UICC", context);
		
		try {
			manager = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                } catch (Exception ex) {}
		
		for(int i = 0; i < channelId.length; i++)
			channelId[i] = 0;
	}

	public boolean isCardPresent() throws CardException {
		String prop = SystemProperties.get(TelephonyProperties.PROPERTY_SIM_STATE);
                if ("READY".equals(prop))
                    return true;
                return false;
	}
	
	@Override
	protected void internalConnect() throws CardException {
		if (manager == null) {
			throw new CardException("Cannot connect to Telephony Service");
		}
		isConnected = true;
	}
	
	@Override
	protected void internalDisconnect() throws CardException {
	}
	
	private byte[] StringToByteArray(String s) {
		byte[] b = new byte[s.length() / 2];
		for(int i = 0; i < b.length; i++)
			b[i] = (byte) Integer.parseInt(s.substring(2*i, 2*i+2), 16);
		return b;
	}
	
	private String ByteArrayToString(byte[] b, int start) {
		StringBuffer s = new StringBuffer();
		for (int i = start; i < b.length; i++)
			s.append(Integer.toHexString(0x100 + (b[i] & 0xff)).substring(1));
		return s.toString();
	}
	
	@Override
	protected byte[] internalTransmit(byte[] command) throws CardException {
		int cla = command[0] & 0xff;
		int ins = command[1] & 0xff;
		int p1 = command[2] & 0xff;
		int p2 = command[3] & 0xff;
		int p3 = -1;
		if (command.length > 4)
			p3 = command[4] & 0xff;
		String data = null;
		if (command.length > 5)
			data = ByteArrayToString(command, 5);
		
		int channelNumber = cla & 0xf;
		
		if (channelNumber == 0) {
			try {
				String response = manager.transmitIccAPDU(cla, ins, p1, p2, p3, data);
				return StringToByteArray(response);
			} catch (Exception ex) {
				throw new CardException("transmit command failed");
			}
		} else {
			if((channelNumber > 0) && (channelId[channelNumber] == 0))
				throw new CardException("channel not open");
	
			try {
				String response = manager.transmitIccLogicalChannel(cla & 0xf0, ins, channelId[channelNumber], p1, p2, p3, data);
				return StringToByteArray(response);
			} catch (Exception ex) {
				throw new CardException("transmit command failed");
			}
		}
	}

	@Override
	protected int internalOpenLogicalChannel(byte[] aid) throws CardException {
		for(int i = 1; i < channelId.length; i++)
			if(channelId[i] == 0) {
				try {
		        		channelId[i] = manager.openIccLogicalChannel(ByteArrayToString(aid, 0));
				} catch (Exception ex) {
					throw new CardException("open channel failed");
				}
		        	return i;
			}
		throw new CardException("out of channels");
	}
	
	@Override
	protected void internalCloseLogicalChannel(int channelNumber) throws CardException {
		if (channelNumber == 0)
			return;
		if (channelId[channelNumber] == 0)
			throw new CardException("channel not open");
                try {
			manager.closeIccLogicalChannel(channelId[channelNumber]);
                } catch (Exception ex) {
			throw new CardException("close channel failed");
		}
		channelId[channelNumber] = 0;
	}
}