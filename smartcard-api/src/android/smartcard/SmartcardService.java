/*
 * Copyright 2010 Giesecke & Devrient GmbH.
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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.smartcard.ISmartcardService;
import android.smartcard.ISmartcardServiceCallback;
import android.smartcard.pcsc.PcscException;
import android.smartcard.pcsc.PcscJni;
import android.util.Log;

/**
 * The smartcard service is setup with privileges to access smart card hardware.
 * The service enforces the permission 'android.smartcard.service.permission.BIND'.
 */
public final class SmartcardService extends Service {

	public static final String SMARTCARD_SERVICE_TAG = "SmartcardService";
	
	private static void clearError(SmartcardError error) {
		if (error != null) error.clear();
	}
	
	@SuppressWarnings("unchecked")
	private static void setError(SmartcardError error, Class clazz, String message) {
		if (error != null) error.setError(clazz, message);
	}
	
	private static void setError(SmartcardError error, Exception e) {
		if (error != null) error.setError(e.getClass(), e.getMessage());
	}

	/**
	 * Keeps the PC/SC resource manager context
	 */
	private long pcscContext = 0;

	/**
	 * For now this list is setup in onCreate(), not changed later and therefore not synchronized.
	 */
	private Map<String, ITerminal> terminals = new TreeMap<String, ITerminal>();
	
    /**
     * The smartcard service interface implementation.
     */
    private final ISmartcardService.Stub smartcardBinder = new ISmartcardService.Stub() {

		public void closeChannel(long hChannel, SmartcardError error) throws RemoteException {
			clearError(error);
			IChannel channel = getChannel(hChannel, error);
			if (channel == null) {
				return;
			}
			try {
				channel.close();
			} catch (Exception e) {
				setError(error, e);
			}
		}

		public byte[] getAtr(long hChannel, SmartcardError error) throws RemoteException {
			clearError(error);
			IChannel channel = getChannel(hChannel, error);
			if (channel == null) {
				return null;
			}
			
			try {
				byte[] atr = channel.getAtr();
				return atr;
			} catch (Exception e) {
				setError(error, e);
				return null;
			}
		}

		public String[] getReaders(SmartcardError error) throws RemoteException {
			clearError(error);
			Set<String> names = terminals.keySet();
			return names.toArray(new String[names.size()]);
		}

		public boolean isCardPresent(String reader, SmartcardError error) throws RemoteException {
			clearError(error);
			ITerminal terminal = getTerminal(reader, error);
			if (terminal == null) {
				return false;
			}
			try {
				boolean isPresent = terminal.isCardPresent();
				return isPresent;
			} catch (Exception e) {
				setError(error, e);
				return false;
			}
		}

		public long openBasicChannel(String reader, ISmartcardServiceCallback callback, SmartcardError error) throws RemoteException {
			clearError(error);
			if (callback == null) {
				setError(error, NullPointerException.class, "callback must not be null");
				return 0;
			}
			ITerminal terminal = getTerminal(reader, error);
			if (terminal == null) {
				return 0;
			}
			
			try {
				long hChannel = terminal.openBasicChannel(callback);
				return hChannel;
			} catch (Exception e) {
				setError(error, e);
				return 0;
			}
		}

		public long openLogicalChannel(String reader, byte[] aid, ISmartcardServiceCallback callback, SmartcardError error) throws RemoteException {
			clearError(error);
			if (aid == null) {
				setError(error, NullPointerException.class, "AID must not be null");
				return 0;
			}
			if (aid.length < 5 || aid.length > 16) {
				setError(error, IllegalArgumentException.class, "AID out of range");
				return 0;
			}
			if (callback == null) {
				setError(error, NullPointerException.class, "callback must not be null");
				return 0;
			}
			ITerminal terminal = getTerminal(reader, error);
			if (terminal == null) {
				return 0;
			}
			
			try {
				long hChannel = terminal.openLogicalChannel(aid, callback);
				return hChannel;
			} catch (Exception e) {
				setError(error, e);
				return 0;
			}
		}

		public byte[] transmit(long hChannel, byte[] command, SmartcardError error) throws RemoteException {
			clearError(error);
			if (command == null) {
				setError(error, NullPointerException.class, "command must not be null");
				return null;
			}
			if (command.length < 4) {
				setError(error, IllegalArgumentException.class, "command must have at least 4 bytes");
				return null;
			}
			
			IChannel channel = getChannel(hChannel, error);
			if (channel == null) {
				return null;
			}

			try {
				byte[] response = channel.transmit(command);
				return response;
			} catch (Exception e) {
				setError(error, e);
				return null;
			}
		}
    };

    public SmartcardService() {
    	super();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
		Log.v(SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " smartcard service onBind");
        if (ISmartcardService.class.getName().equals(intent.getAction())) {
            return smartcardBinder;
        }
        return null;
    }
    

    @Override
    public void onCreate() {
		Log.v(SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " smartcard service onCreate");

		// fill the PC/SC reader list from the daemon
		try {
			pcscContext = PcscJni.establishContext(PcscJni.Scope.User);
		} catch (PcscException e) {
			Log.e(SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " cannot initialize PC/SC resource manager");
		}
		if (pcscContext > 0) {
			String[] readers = null;
			try {
				readers = PcscJni.listReaders(pcscContext, null);
			} catch (PcscException e) {
				Log.e(SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " cannot list PC/SC readers");
			}
			
			for (String reader: readers) {
				terminals.put(reader, new PcscTerminal(reader, pcscContext));				
				Log.v(SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " adding PC/SC reader " + reader);
			}
		}
    }

    @Override
    public void onDestroy() {
		Log.v(SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " smartcard service onDestroy ...");
		for (ITerminal terminal : terminals.values()) {
			terminal.closeChannels();
    	}
		
		if (pcscContext != 0) {
			try {
				PcscJni.releaseContext(pcscContext);
			} catch (PcscException ignore) {
			}
			pcscContext = 0;
		}
		Log.v(SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " ... smartcard service onDestroy");
    }
    
    private IChannel getChannel(long hChannel, SmartcardError error) {
		for (ITerminal terminal : terminals.values()) {
			IChannel channel = terminal.getChannel(hChannel);
			if (channel != null) {
				return channel;
			}
    	}
		setError(error, IllegalArgumentException.class, "invalid handle");
		return null;
    }
    
    private ITerminal getTerminal(String reader, SmartcardError error) {
    	if (reader == null) {
    		setError(error, NullPointerException.class, "reader must not be null");
    		return null;
    	}
		ITerminal terminal = terminals.get(reader);
		if (terminal == null) {
			setError(error, IllegalArgumentException.class, "unknown reader");
		}
		return terminal;
    }
}
