/*
 * Copyright 2009 Giesecke & Devrient GmbH.
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

package android.smartcard.service;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * The SEEK service is setup with privileges to access smart card hardware.
 * The service enforces the permission 'android.smartcard.service.permission.BIND'.
 */
public final class SeekService extends Service {

	public static final String SEEK_SERVICE_TAG = "SeekService";
	
	private static void clearError(SeekError error) {
		if (error != null) error.clear();
	}
	
	@SuppressWarnings("unchecked")
	private static void setError(SeekError error, Class clazz, String message) {
		if (error != null) error.setError(clazz, message);
	}
	
	private static void setError(SeekError error, Exception e) {
		if (error != null) error.setError(e.getClass(), e.getMessage());
	}
	
	/**
	 * For now this list is setup in onCreate(), not changed later and therefore not synchronized.
	 */
	private Map<String, ITerminal> terminals = new TreeMap<String, ITerminal>();
	
    /**
     * The SEEK service interface implementation.
     */
    private final ISeekService.Stub seekBinder = new ISeekService.Stub() {

		@Override
		public void closeChannel(long hChannel, SeekError error) throws RemoteException {
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

		@Override
		public byte[] getAtr(long hChannel, SeekError error) throws RemoteException {
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

		@Override
		public String[] getReaders(SeekError error) throws RemoteException {
			clearError(error);
			Set<String> names = terminals.keySet();
			return names.toArray(new String[names.size()]);
		}

		@Override
		public boolean isCardPresent(String reader, SeekError error) throws RemoteException {
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

		@Override
		public long openBasicChannel(String reader, ISeekServiceCallback callback, SeekError error) throws RemoteException {
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

		@Override
		public long openLogicalChannel(String reader, byte[] aid, ISeekServiceCallback callback, SeekError error) throws RemoteException {
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

		@Override
		public byte[] transmit(long hChannel, byte[] command, SeekError error) throws RemoteException {
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

    public SeekService() {
    	super();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
		Log.v(SEEK_SERVICE_TAG, Thread.currentThread().getName() + " SEEK service onBind");
        if (ISeekService.class.getName().equals(intent.getAction())) {
            return seekBinder;
        }
        return null;
    }
    

    @Override
    public void onCreate() {
		Log.v(SEEK_SERVICE_TAG, Thread.currentThread().getName() + " SEEK service onCreate");
    	// TODO: setup terminal list using system information
   		terminals.put("Native 0", new NativeTerminal("Native 0"));
    }

    @Override
    public void onDestroy() {
		Log.v(SEEK_SERVICE_TAG, Thread.currentThread().getName() + " SEEK service onDestroy ...");
		for (ITerminal terminal : terminals.values()) {
			terminal.closeChannels();
    	}
		Log.v(SEEK_SERVICE_TAG, Thread.currentThread().getName() + " ... SEEK service onDestroy");
    }
    
    private IChannel getChannel(long hChannel, SeekError error) {
		for (ITerminal terminal : terminals.values()) {
			IChannel channel = terminal.getChannel(hChannel);
			if (channel != null) {
				return channel;
			}
    	}
		setError(error, IllegalArgumentException.class, "invalid handle");
		return null;
    }
    
    private ITerminal getTerminal(String reader, SeekError error) {
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
