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

import android.os.IBinder;
import android.os.RemoteException;
import android.smartcard.ISmartcardServiceCallback;
import android.util.Log;

/**
 * Smartcard service base class for channel resources.
 */
class Channel implements IChannel, IBinder.DeathRecipient  {

	protected final int channelNumber;

	protected long handle;
	
	protected Terminal terminal;
	
	protected final IBinder binder;
	
	Channel(Terminal terminal, int channelNumber, ISmartcardServiceCallback callback) {
		this.channelNumber = channelNumber;
		this.terminal = terminal;
		this.binder = callback.asBinder();
		try {
			binder.linkToDeath(this, 0);
		} catch (RemoteException e) {
			Log.e(SmartcardService.SMARTCARD_SERVICE_TAG, "Failed to register client callback");
		}
	}
	
	public void binderDied() {
		// Close this channel if the client died.
		try {
			Log.v(SmartcardService.SMARTCARD_SERVICE_TAG, Thread.currentThread().getName() + " Client " + binder.toString() + " died");
			close();
		} catch (Exception ignore) {
		}
	}
	
	public void close() throws CardException {
		try {
			getTerminal().closeChannel(this);
		} finally {
			binder.unlinkToDeath(this, 0);
		}
	}

	public int getChannelNumber() {
		return channelNumber;
	}
	
	/**
	 * Returns the handle assigned to this channel.
	 * @return the handle assigned to this channel.
	 */
	long getHandle() {
		return handle;
	}
	
	/**
	 * Returns the associated terminal.
	 * @return the associated terminal.
	 */
	Terminal getTerminal() {
		return terminal;
	}
	
	/**
	 * Assigns the channel handle.
	 * @param handle
	 *           the channel handle to be assigned.
	 */
	void setHandle(long handle) {
		this.handle = handle;
	}
	
	public byte[] transmit(byte[] command) throws CardException {
		if (command.length < 4)
			throw new IllegalArgumentException("command must not be smaller than 4 bytes");
		if (((command[0] & 0x80) == 0) && ((command[0] & 0x60) != 0x20)) {
			// ISO command
			if (command[1] == 0x70)
				throw new IllegalArgumentException("MANAGE CHANNEL command not allowed");
			if ((channelNumber > 0) && (command[1] == 0xA4) && (command[2] == 0x04))
				throw new IllegalArgumentException("SELECT by AID not allowed");
			int cla = command[0] & 0x7F;
			if (channelNumber < 4) {
				cla = (cla & 0x1C) | channelNumber;
			} else {
				cla = (cla & 0x30) | 0x40 | channelNumber;
			}
			command[0] = (byte) cla;
		} else {
			command[0] |= channelNumber;
		}
		byte[] rsp = getTerminal().transmit(command, 2, 0, 0, null);
		return rsp;
	}
}
