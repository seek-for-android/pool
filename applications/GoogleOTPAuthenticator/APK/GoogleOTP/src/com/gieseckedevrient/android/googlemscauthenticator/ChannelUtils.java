package com.gieseckedevrient.android.googlemscauthenticator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.Session;
import org.simalliance.openmobileapi.service.CardException;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public final class ChannelUtils implements
	SEService.CallBack {

	private static final String OTP_DEMO_TAG = "OtpCalculator";
	private static final byte[] OTP_AID = new byte[] 
			{ (byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x00, 0x03, (byte) 0xFF, 
				     0x49, 0x10, 0x00, (byte) 0x89, 0x00, 0x00,	0x02, 0x01 };
	public static final int TOTP = 0;
	public static final int HOTP = 1;
	public static final int N_DIGITS = 6;
	private static final int INTERVAL_LENGTH = 30;

	private static boolean connected;
	private static int disconnects;
	private static Reader[] otpReader;
	private static MainActivity main;
	private static SEService seService;
	private static HashMap<String, Reader> accountToReader = new HashMap<String, Reader>();
	private static HashMap<String, Session> sessionToReader = new HashMap<String, Session>();
	private static String selectedAccount = "";

	private ChannelUtils() {
	}

	public static final void init(MainActivity main ) {
		ChannelUtils.main = main;
		accountToReader.clear();
		sessionToReader.clear();
		
		try {
		    new SEService(main.getApplicationContext(), new ChannelUtils() );
		} catch (SecurityException e) {
			Log.e(OTP_DEMO_TAG, "Smartcard service binding not allowed");
		} catch (Exception e) {
			Log.e(OTP_DEMO_TAG, "Exception: " + e.getMessage());
		}
	}

	public static Reader getReader(String account ){
		return accountToReader.get(account);
	}
	
	public static Reader[] getOtpReader(){
		return otpReader;
	}
	
	@Override
	public void serviceConnected(SEService service) {
		try {
			seService = service;
			otpReader = ChannelUtils.checkForOtpAppletAndAccounts(seService.getReaders());
			
		} catch (Exception e) {
			Toast.makeText(main,
					"Could not access the terminal: " + e,
					Toast.LENGTH_LONG).show();
			return;
		}

		connected = true;
		
		main.refresh();
	}

	private static Reader[] checkForOtpAppletAndAccounts( Reader[] availableReader ){
		
		ArrayList<Reader> otpReader = new ArrayList<Reader>();
		accountToReader.clear();
		for( Reader reader : availableReader ){
			if( reader.isSecureElementPresent() ){
				Session session = null;
				Channel channel = null;
				try {
					session = reader.openSession();
					channel = session.openLogicalChannel(OTP_AID);
					if( channel != null ){
						String[] accounts = ChannelUtils.getAccounts(channel);
						for( String account : accounts ) {
							accountToReader.put(account, reader);
						}
						otpReader.add(reader);
						channel.close();
					}
					session.close();
				} catch (Exception e) {
					if( channel != null ){
						channel.close();
					}
					if( session != null ){
						session.close();
					}
				} 
			}
		}
		return otpReader.toArray(new Reader[otpReader.size()]);
	}
	
	
	public static final boolean ensureConnected() {
		if (!connected)
			Toast.makeText(
					main,
					"Could not establish connection to the smartcard service. "
							+ "Please try again.", Toast.LENGTH_LONG).show();
		return connected;
	}

	public static final Channel openChannelAndSelect(Reader reader)
			throws CardException {
		
		if( reader == null ){
			return null;
		}
		
		try {
			Session session = sessionToReader.get(reader.getName());
			if( session == null )
				sessionToReader.put(reader.getName(), reader.openSession() );
		} catch (IOException e) {
			throw new CardException("Could not open session on terminal " + reader.getName() );
		}
		
		Channel cardChannel;
		try {
			cardChannel = sessionToReader.get(reader.getName()).openLogicalChannel( OTP_AID );
		} catch (IOException e) {
			throw new CardException("Could not open channel on terminal " + reader.getName() );
		} catch( SecurityException se ){
			throw new CardException("Security Exception:  " + se.getLocalizedMessage() );
		}

		if (cardChannel == null)
			throw new CardException("No channel available");

		return cardChannel;
	}

	// Sets the counter on the smartcard to the specified value.
	public static final void setCounter(Channel channel, long counter)
			throws CardException {

		// The APDU to be sent
		byte[] cmd = new byte[] { 0x00, 0x10, 0x00, 0x00, 0x08, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

		// Write counter to APDU
		for (int i = 0; i < 8; i++) {
			cmd[12 - i] = (byte) (counter & 0xFF);
			counter >>= 8;
		}

		// Transmit APDU and check if the card returns success (0x9000)
		byte[] response = null;
		try {
			response = channel.transmit(cmd);
		} catch (IOException e) {
			throw new CardException("Unable to transmit data. " + e.getLocalizedMessage());
		}
		if( response == null ){
			throw new CardException("Invalid response from Secure Element" );
		}
		
		if (response.length == 2 && response[0] == (byte) 0x6A
				&& response[1] == (byte) 0x80)
			throw new CardException(
					"Counter time lies in future - possible security violation.");

		if (response.length != 2 || response[0] != (byte) 0x90
				|| response[1] != 0x00)
			throw new CardException("Could not set counter.");

	}

	// Writes a secret to the smartcard
	public static final void personalize(Channel channel, String account,
			byte[] secret, int type) throws CardException {

		byte[] accountData = account.getBytes();

		byte[] cmd = new byte[5 + 1 + accountData.length + 20];
		cmd[1] = (byte) 0x12;
		cmd[2] = (byte) (N_DIGITS);
		cmd[3] = (type == HOTP) ? (byte) 1 : (byte) 0;
		cmd[4] = (byte) (1 + accountData.length + 20);
		cmd[5] = (byte) (accountData.length);

		System.arraycopy(accountData, 0, cmd, 6, accountData.length);
		System.arraycopy(secret, 0, cmd, 6 + accountData.length, 20);

		byte[] response;
		try {
			response = channel.transmit(cmd);
		} catch (IOException e) {
			throw new CardException("Unable to transmit data. " + e.getLocalizedMessage());
		}

		if (response.length == 2 && response[0] == (byte) 0x69
				&& response[1] == 0xC2)
			throw new CardException("No free account slot.");
		if (response.length != 2 || response[0] != (byte) 0x90
				|| response[1] != 0x00)
			throw new CardException("Could not set secret.");

		String tmpAccount = null;
		try {
			tmpAccount = getSelectedAccount(channel);
		} catch (CardException e) {
		}
		selectAccount(channel, account);
		setCounter(channel, getCurrentInterval());
		if (tmpAccount != null)
			try {
				selectAccount(channel, tmpAccount);
			} catch (CardException e) {
			}
	}

	public static final void shutdown() {
		Collection<Session> sessions = sessionToReader.values();
		Iterator<Session> iter = sessions.iterator();
		while(iter.hasNext()){
			Session session = iter.next();
			if( session != null ){
				session.closeChannels();
				session.close();
			}
		}
		
		if (seService != null) {
			seService.shutdown();
			seService = null;
		}
	}

	public static final String[] getAccounts()
			throws CardException {

		accountToReader.clear();
		Channel channel = null;
		for( Reader reader : otpReader ){
			try {
				if( channel != null ){
					channel.close();
					channel = null;
				}
				channel = ChannelUtils.openChannelAndSelect( reader );
				String[] accounts = ChannelUtils.getAccounts(channel);
				
				for( String account : accounts ) {
					accountToReader.put(account, reader);
				}
			} catch( Exception e )  {
				Toast.makeText(main.getApplicationContext(), e.getLocalizedMessage() 
						, Toast.LENGTH_LONG).show();
				break;
			}
		}

		if( channel != null ){
			channel.close();
		}
		
		Set<String> accounts = accountToReader.keySet();
		return accounts.toArray(new String[accounts.size()]);
		//return accounts;
	}
	
	public static final String[] getAccounts( Channel channel ) throws CardException{
		byte[] cmd = new byte[] { 0x00, 0x18, 0x00, 0x00, 0x00 };
		byte[] response;
		String[] accounts = null;
		try {
			response = channel.transmit(cmd);
		} catch (IOException e) {
			throw new CardException("Unable to transmit data. " + e.getLocalizedMessage());
		}
	
		if (response.length != 3 || response[1] != (byte) 0x90
				|| response[2] != 0x00) {
			throw new CardException("Could not get number of accounts.");
		}
	
		int nAccounts = ((int) response[0]) & 0xff;
		accounts = new String[nAccounts];
		cmd[2] = (byte) 1;
	
		for (int i = 0; i < nAccounts; i++) {
			cmd[3] = (byte) i;
			try {
				response = channel.transmit(cmd);
			} catch (IOException e) {
				//throw new CardException("Unable to transmit data. " + e.getLocalizedMessage());
				break;
			}
			if (response.length < 2
					|| response[response.length - 2] != (byte) 0x90
					|| response[response.length - 1] != 0x00) {
				throw new CardException("Could not get account data.");
			}
			
			accounts[i] = new String(response, 0, response.length - 2);
		}
		return accounts;
	}

	public static final String getSelectedAccount(Channel channel)
			throws CardException {
		byte[] cmd = new byte[] { 0x00, 0x18, 0x01, (byte) 0xff, 0x00 };
		byte[] response;
		try {
			response = channel.transmit(cmd);
		} catch (IOException e) {
			throw new CardException("Unable to transmit data. " + e.getLocalizedMessage());
		}

		if( response.length > 2  && response[response.length - 2] == (byte) 0x90 ) {
			return new String(response, 0, response.length - 2);
		} else  if (response.length == 2 
				|| response[0] != 0x69) {
			return null;
		} else  if (response.length < 2 
				|| response[response.length - 1] != 0x00) {
			throw new CardException("Could not get account data.");
		} 
		return null;
	}

	public static final boolean selectAccount(Channel channel,
			String account) throws CardException {
		byte[] accountData = account.getBytes();

		byte[] cmd = new byte[5 + accountData.length];
		cmd[1] = 0x16;
		cmd[4] = (byte) accountData.length;
		System.arraycopy(accountData, 0, cmd, 5, accountData.length);
		byte[] response;
		try {
			response = channel.transmit(cmd);
		} catch (IOException e) {
			throw new CardException("Unable to transmit data. " + e.getLocalizedMessage());
		}

		if (response.length == 2 && response[0] == 0x69
				&& response[0] == (byte) 0xC1)
			return false;

		if (response.length < 2 || response[response.length - 2] != (byte) 0x90
				|| response[response.length - 1] != 0x00)
			throw new CardException("Could not select account. " + account);

		return true;
	}

	public static final boolean getHOTP(Channel channel)
			throws CardException {

		byte[] cmd = new byte[] { 0x00, 0x15, 0x00, 0x00, 0x00 };
		byte[] response;
		try {
			response = channel.transmit(cmd);
		} catch (IOException e) {
			throw new CardException("Unable to transmit data. " + e.getLocalizedMessage());
		}

		if (response.length != 3 || response[1] != (byte) 0x90
				|| response[2] != 0x00)
			return false;

		return response[0] != 0;
	}

	public static long getCurrentInterval() {
		long currentTimeSeconds = System.currentTimeMillis() / 1000;
		return currentTimeSeconds / INTERVAL_LENGTH;
	}

	public static Date counterToDate(long counter) {
		return new Date(counter * 1000 * INTERVAL_LENGTH);
	}

	public static void deleteAccount(Channel channel, String account)
			throws CardException {
		byte[] accountData = account.getBytes();

		byte[] cmd = new byte[5 + accountData.length];
		cmd[1] = 0x17;
		cmd[4] = (byte) accountData.length;
		System.arraycopy(accountData, 0, cmd, 5, accountData.length);
		byte[] response;
		try {
			response = channel.transmit(cmd);
		} catch (IOException e) {
			throw new CardException("Unable to transmit data. " + e.getLocalizedMessage());
		}

		if (response.length == 2 && response[0] == 0x69
				&& response[0] == (byte) 0xC1)
			throw new CardException("Account does not exist.");

		if (response.length < 2 || response[response.length - 2] != (byte) 0x90
				|| response[response.length - 1] != 0x00)
			throw new CardException("Could not delete account.");
	}

	public static long getCounter(Channel channel, int index)
			throws CardException {
		byte[] cmd = new byte[] { 0x00, 0x11, 0x00, 0x00, 0x00 };
		byte[] response;
		try {
			response = channel.transmit(cmd);
		} catch (IOException e) {
			throw new CardException("Unable to transmit data. " + e.getLocalizedMessage());
		}

		if (response.length < 2 || response[response.length - 2] != (byte) 0x90
				|| response[response.length - 1] != 0x00)
			throw new CardException("Could not get counter value.");

		long result = 0;
		for (int i = 0; i < response.length - 2; i++) {
			result <<= 8;
			result |= (long) response[i] & (long) 0xff;
		}

		return result;
	}

	public static void reset() throws CardException {
		byte[] cmd = new byte[5];
		cmd[1] = 0x14;
		byte[] response;
		Channel channel = null;
		for( Reader reader : otpReader ){
			try {
				channel = ChannelUtils.openChannelAndSelect(reader );
				response = channel.transmit(cmd);
				channel.close();
			} catch (IOException e) {
				// throw new CardException("Unable to transmit data. " + e.getLocalizedMessage());
				if( channel != null ){
					channel.close();
				}
				break;
			} 
	
			if (response.length < 2 || response[response.length - 2] != (byte) 0x90
					|| response[response.length - 1] != 0x00) {
				//throw new CardException("Could not reset.");
				Toast.makeText(main.getApplicationContext(), "Could not reset on terminal " + reader.getName() 
						, Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	public static String getSelectedAccount() {
		return selectedAccount;
	}

	public static void setSelectedAccount(String selectedAccount) {
		ChannelUtils.selectedAccount = selectedAccount;
	}

}
