/* 
 * Copyright (C) 2010 Giesecke & Devrient GmbH
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

package android.giesecke.security;

/**
 * Constructs a Command APDU byte array. See {@link CardCommand} for information on
 * how the byte array message has to be created.
 * 
 * @author stuerzem
 */
public class CardCommandBuilder
{
	static final byte keyLength = 0x04; // 32 Bytes

	private CardCommand cardCommand;
	private byte[] cmd;

	/**
	 * Creates a new Builder to make APDU message arrays based on the given
	 * {@link CardCommand}.
	 */
	public CardCommandBuilder(CardCommand apduCommand, byte[] hash, String passphrase)
	{
		checkLengths(hash, passphrase);

		this.cardCommand = apduCommand;

		int hashPassphraseLength = 1 + hash.length + 1 + passphrase.length();
		cmd = new byte[4 + 1 + hashPassphraseLength];

		cmd[0] = (byte) 0x90;

		setCommandSpecificBytes();

		// Insert complete length
		cmd[4] = (byte) hashPassphraseLength;

		// Insert hash length
		cmd[5] = (byte) hash.length;

		// Insert hash
		System.arraycopy(hash, 0, cmd, 5 + 1, hash.length);

		// Insert passphrase length
		cmd[5 + hash.length + 1] = (byte) passphrase.length();

		// Insert passphrase
		System.arraycopy(passphrase.getBytes(), 0, cmd, 5 + hash.length + 2, passphrase.length());
	}
	
	/**
	 * Creates a new Builder to make APDU message arrays based on the given
	 * {@link CardCommand}.
	 */
	public CardCommandBuilder(CardCommand verifyPin, byte[] pin)
	{
		this.cardCommand = verifyPin;
		
		cmd = new byte[5 + pin.length];

		cmd[0] = (byte) 0x90;
		
		setCommandSpecificBytes();

		// Insert pin length
		cmd[4] = (byte) pin.length;

		// Insert pin
		System.arraycopy(pin, 0, cmd, 4 + 1, pin.length);
	}

	/**
	 * This method checks the length of the File-ID and the password
	 * @param hash File-ID that has to be checked for a maximum size of 255
	 * @param passphrase Password has to be checked for a maximum size of 255
	 * @throws IllegalArgumentException if one of both is too long
	 */
	private void checkLengths(byte[] hash, String passphrase)
	{
		if (hash.length > 255)
			throw new IllegalArgumentException("Hash is too long");
		if (passphrase.length() > 255)
			throw new IllegalArgumentException("Password is too long");
	}

	/**
	 * Copy constructor. Creates a new Builder with the same hash and passphrase
	 * as the given builder to make APDU message arrays based on the given
	 * {@link CardCommand}.
	 */
	public CardCommandBuilder(CardCommandBuilder original, CardCommand cardCommand)
	{
		this.cardCommand = cardCommand;
		this.cmd = original.cmd;
		setCommandSpecificBytes();
	}

	/**
	 * This method sets the specific Bytes for each Command
	 */
	private void setCommandSpecificBytes()
	{
		switch (cardCommand)
			{
			case VerifyPin:
				cmd[1] = 0x10;
				break;
			case CreateKey:
				cmd[1] = 0x20;
				cmd[3] = keyLength;
				break;
			case VerifyKey:
				cmd[1] = 0x30;
				break;
			case DeleteKey:
				cmd[1] = 0x40;
				break;
			}
	}

	/**
	 * get method for the command bytes
	 * @return command bytes
	 */
	public byte[] getCommandBytes()
	{
		return cmd.clone();
	}

	/**
	 * ToString Method
	 * Convert the command bytes to a string
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(cmd.length * 5);
		for (int i = 0; i < cmd.length; i++)
		{
			sb.append((int) cmd[i]);
			if (i != cmd.length - 1)
				sb.append(" ");
		}
		return sb.toString();
	}

	/**
	 * Enumeration for the all possible CardCommands<br>
	 * <br>CreateKey {@link CardCommand#CreateKey}
	 * <br>VerifyKey {@link CardCommand#VerifyKey}
	 * <br>DeleteKey {@link CardCommand#DeleteKey}
	 * <br>VerifyPin {@link CardCommand#VerifyPin}
	 * 
	 * @author matthias
	 *
	 */
	public enum CardCommand
	{
		/**
		 * Creates a new hash/passphrase/key triple on the cipher card.<br />
		 * Construction (in hex):<br /> {@code 90 20 00 0x <len> <len hash> <hash
		 * bytes> <len passphrase> <passphrase>} where x is for requesting 8*x
		 * bytes of key length (atm max(x) is 4 for a 256 bit key).
		 */
		CreateKey,
		/**
		 * Receives a new key from a given hash/passphrase tuple from the cipher
		 * card.<br />
		 * Construction (in hex):<br /> {@code 90 30 00 0x <len> <len hash> <hash
		 * bytes> <len passphrase> <passphrase>}
		 */
		VerifyKey,
		/**
		 * Deletes a hash/passphrase/key triple on the cipher card.<br />
		 * Construction (in hex):<br /> {@code 90 40 00 0x <len> <len hash> <hash
		 * bytes> <len passphrase> <passphrase>}
		 */
		DeleteKey,
		/**
		 * Verifies the Pin on the cipher card.
		 * Construction (in hex):<br /> {@code 90 10 00 00 <len> <global Pin>}
		 */
		VerifyPin
	}
}
