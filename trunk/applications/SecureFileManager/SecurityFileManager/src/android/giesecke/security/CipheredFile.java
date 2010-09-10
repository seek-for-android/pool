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

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.smartcard.CardException;
import android.smartcard.ICardChannel;
import android.util.Log;

/**
 * This class handels the processing of the encrypted-file and also that file,
 * which should be encrypted.
 * 
 * @author stuerzem
 */
public class CipheredFile
{

	/**
	 * The length of the random file ID
	 */
	private static final int ENCRYPTION_RANDOM_ID_LENGTH = 4;

	/**
	 * maximal blocksize of a package that get ciphered If a file is bigger then
	 * the blocksize, it will be devided in several parts
	 */
	private long maxBlockSize = 10000L; // in byte

	/**
	 * Magic number in encrypted files, 16 bytes long This magic number marks
	 * ciphered files
	 */
	public static final byte[] ENCRYPTION_MAGIC_NUMBER = Helper
			.hexStringToByteArray("96362045ad5c1e4dd83553a989a7614a");

	/**
	 * For internal comparison and for a better code understanding.<br>
	 * Symbolizes the decrypt modus
	 */
	private static final int DECRYPT = 1;

	/**
	 * For internal comparison and for a better code understanding.<br>
	 * Symbolizes the encrypt modus
	 */
	private static final int ENCRYPT = 2;

	/**
	 * Tag for logging
	 */
	private static final String TAG = "SecureSDCard";

	/**
	 * Name of the file you are operating on
	 */
	private String fileName;

	/**
	 * The password, for doing ciphering operations on a specific file
	 */
	private String password;

	/**
	 * CardChannel to the smartcard
	 */
	private ICardChannel cardChannel;

	/**
	 * To handle CipherProgress for DialogProgressbar of the UI
	 */
	private Handler progressHandler;

	/**
	 * To handle the filename for DialogProgressbar of the UI
	 */
	private Handler encryptedFileHandler;

	/**
	 * Handles the Progressvalue for the {@link #progressHandler}
	 */
	private int progressValue = 0;

	/**
	 * The constructor to initialize the CipheredFile-class.
	 * 
	 * @param name
	 *            the name or the path of the file to process. It depends on the
	 *            storage location of the file.
	 * @param cardChannel
	 *            CardChannel to the smartcard
	 * @param password
	 *            the password needed to process the file.
	 * @param progressHandler
	 *            Handler to organize the progressbar of the UI
	 * @param encryptedFileHandler
	 *            Handler to organize UI for Filenames and errors
	 */
	public CipheredFile(String name, ICardChannel cardChannel, String password, Handler progressHandler,
			Handler encryptedFileHandler)
	{
		this.fileName = name;
		this.password = password;
		this.cardChannel = cardChannel;
		this.progressHandler = progressHandler;
		this.encryptedFileHandler = encryptedFileHandler;
	}

	/**
	 * Handles the decryption of a file or a directory.
	 * 
	 * @throws CardException
	 *             the card exception is thrown, if the access to the secure
	 *             element failed
	 */
	public void decrypt() throws CardException
	{
		File file = new File(fileName);

		/**
		 * all encrypted files to decrypt
		 */
		List<File> allEncryptedFiles = new ArrayList<File>();

		// look if the file is a directory
		if (file.isDirectory())
		{
			// archive all parent directories on the stack
			// and add all undecrypted files to the allEncryptedFiles list.
			Stack<File> dirs = new Stack<File>();
			dirs.push(file);

			while (dirs.size() > 0)
			{
				for (File tmp : dirs.pop().listFiles())
				{
					if (tmp.isDirectory())
					{
						dirs.push(tmp);
					} else
					{
						if (acceptFile(tmp, DECRYPT))
						{
							allEncryptedFiles.add(tmp);
						}
					}
				}
			}

		} else
		{
			if (acceptFile(file, DECRYPT))
			{
				allEncryptedFiles.add(file);
			} else
			{
				throw new CardException("File is not encrypted");
			}
		}

		// step throw allEncryptedFiles to decrypt each file
		for (int filelocation = 0; filelocation < allEncryptedFiles.size(); filelocation++)
		{

			try
			{
				decryptFile(allEncryptedFiles.get(filelocation));
			} catch (CardException e)
			{
				progressValue = 101;
				progressHandler.sendEmptyMessage(progressValue);
				// throw only Exception if you decrypt a single file
				if (!file.isDirectory())
					throw new CardException(e.getMessage());
			}
		}
	}

	/**
	 * Handles the decryption of a file
	 * 
	 * @param file
	 *            file to decrypt
	 * @throws CardException
	 *             the card exception is thrown, if the access to the secure
	 *             element failed
	 */
	private void decryptFile(File file) throws CardException
	{
		// handle main UI
		Message msg = encryptedFileHandler.obtainMessage();
		Bundle b = new Bundle();
		b.putString("file", file.getName());
		msg.setData(b);
		encryptedFileHandler.sendMessage(msg);
		progressValue = 0;
		progressHandler.sendEmptyMessage(progressValue);
		Log.d(TAG, "Try to decrypt " + file.toString());

		// rounds needed to fully process the specified file
		int rounds = 1;
		long blockSize = 0L;

		byte[] decryptionKey = null;
		byte[] dataToDecr = null;
		byte[] id = null;

		// initialize round counter
		if (file.length() > maxBlockSize)
		{
			rounds = (int) (file.length() / maxBlockSize);

			if (file.length() % maxBlockSize != 0)
				rounds++;

			blockSize = maxBlockSize;
		}

		// performing the determined number of rounds and processing the
		// file-data in blocks
		for (int i = 0; i < rounds; i++)
		{
			// set progress for ProgressDialog of the UI
			progressValue = (int) ((double) (i + 1) / rounds * 100);
			progressHandler.sendEmptyMessage(progressValue);

			boolean completed = false;

			if (i == rounds - 1)
			{
				completed = true;
				blockSize = 0L;
			}

			byte[] encData;

			if (i == 0 && !completed)
				// read the first block if its file is bigger than maxBlocksize
				// must be done, because of the right block-sizes for cipher
				encData = Helper.readFile(file, blockSize + ENCRYPTION_RANDOM_ID_LENGTH
						+ ENCRYPTION_MAGIC_NUMBER.length);
			else
				encData = Helper.readFile(file, blockSize);

			// header must be extracted in the first round
			if (i == 0)
			{
				int idLen = ENCRYPTION_RANDOM_ID_LENGTH;
				int magicNrLen = ENCRYPTION_MAGIC_NUMBER.length;
				int dataLen = encData.length - idLen - magicNrLen;
				
				// sorting the data
				id = new byte[idLen];
				dataToDecr = new byte[dataLen];
				System.arraycopy(encData, magicNrLen, id, 0, idLen);
				System.arraycopy(encData, idLen + magicNrLen, dataToDecr, 0, dataLen);
				
				decryptionKey = getCipherKey(id, password, DECRYPT);
				
				if (!file.getAbsolutePath().endsWith(".enc"))
				{
					int i1 = 0;
					do
					{
						fileName = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("."))
								+ ".decrypted"
								+ i1
								+ file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."),
										file.getAbsolutePath().length());
						i1++;
					} while (new File(fileName).exists());

				} else
				{
					fileName = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - ".enc".length());
				}

			} else
			{
				dataToDecr = encData;
			}

			// decrypting the data
			byte[] decrData = Helper.decryptData(dataToDecr, decryptionKey, completed);

			// writing back the decrypted data
			File plainFile = new File(fileName);
			Helper.writeFile(plainFile, decrData, completed);
		}

		// deleting the key from the card, because it's not needed anymore
		deleteCipherKey(id, password);

		// setProgressvalue > 100 to exit the progressdialog of the UI
		progressValue = 101;
		progressHandler.sendEmptyMessage(progressValue);

		// Delete File if its correct decrypted
		if (file.canWrite())
			file.delete();
	}

	/**
	 * Handles the encryption of a file.
	 * 
	 * @throws CardException
	 *             the card exception is thrown, the access to the secure
	 *             element failed
	 */
	public void encrypt() throws CardException
	{
		// int magicNumLength = ENCRYPTION_MAGIC_NUMBER.length;
		Log.d(TAG, "Magic Number " + ENCRYPTION_MAGIC_NUMBER);

		File file = new File(fileName);

		/**
		 * all decrypted files to encrypt
		 */
		List<File> allDecryptedFiles = new ArrayList<File>();

		if (file.isDirectory())
		{
			// archive all parent directories on the stack
			// and add all unencrypted files to the allDecryptedFiles list.
			Stack<File> dirs = new Stack<File>();
			dirs.push(file);

			while (dirs.size() > 0)
			{
				for (File tmp : dirs.pop().listFiles())
				{
					if (tmp.isDirectory())
					{
						dirs.push(tmp);
					} else
					{
						if (acceptFile(tmp, ENCRYPT))
						{
							allDecryptedFiles.add(tmp);
						}
					}
				}
			}

		} else
		{
			if (acceptFile(file, ENCRYPT))
			{
				allDecryptedFiles.add(file);
			}
		}

		// encrypt each file of the allDecryptedFiles list
		for (int filelocation = 0; filelocation < allDecryptedFiles.size(); filelocation++)
		{
			encryptFile(allDecryptedFiles.get(filelocation));
		}
	}

	private void encryptFile(File file) throws CardException
	{
		// handle main UI
		Message msg = encryptedFileHandler.obtainMessage();
		Bundle b = new Bundle();
		b.putString("file", file.getName());
		msg.setData(b);
		encryptedFileHandler.sendMessage(msg);
		progressValue = 0;
		progressHandler.sendEmptyMessage(progressValue);
		Log.d(TAG, "Try to encrypt " + file.toString());

		// generate new RandomID
		byte[] randomID = generateRandomID();
		byte[] key = null;
		try
		{
			key = getCipherKey(randomID, password, ENCRYPT);
		} catch (CardException e)
		{
			throw new CardException("Could not encrypt File");
		}

		// rounds needed to fully process the specified file
		int rounds = 1;
		long blockSize = 0L;

		// determining number of rounds, which are needed to process the
		// file
		if (file.length() > maxBlockSize)
		{
			rounds = (int) (file.length() / maxBlockSize);
			if (file.length() % maxBlockSize != 0)
				rounds++;

			blockSize = maxBlockSize;
		}

		// performing the determing number of round and processing the
		// file-data in blocks
		for (int i = 0; i < rounds; i++)
		{
			// set progress for ProgressDialog of the UI
			progressValue = (int) ((double) (i + 1) / rounds * 100);
			progressHandler.sendEmptyMessage(progressValue);
			Log.i(TAG, Integer.toString(progressValue));

			boolean completed = false;

			// determining the end of processing
			if (i == rounds - 1)
			{
				completed = true;
				blockSize = 0L;
			}

			byte[] dataToEncr = Helper.readFile(file, blockSize);

			// encrypting data
			byte[] encData = Helper.encryptData(dataToEncr, key, completed);

			byte[] dataToWrite = new byte[encData.length + ENCRYPTION_RANDOM_ID_LENGTH + ENCRYPTION_MAGIC_NUMBER.length];

			// sorting data has to be done only for the first data-block
			if (i == 0)
			{
				System.arraycopy(ENCRYPTION_MAGIC_NUMBER, 0, dataToWrite, 0, ENCRYPTION_MAGIC_NUMBER.length);
				System.arraycopy(randomID, 0, dataToWrite, ENCRYPTION_MAGIC_NUMBER.length, ENCRYPTION_RANDOM_ID_LENGTH);
				System.arraycopy(encData, 0, dataToWrite, ENCRYPTION_MAGIC_NUMBER.length + ENCRYPTION_RANDOM_ID_LENGTH,
						encData.length);
			} else
				dataToWrite = encData;

			File encFile = new File(file.getAbsolutePath() + ".enc");

			Helper.writeFile(encFile, dataToWrite, completed);
		}

		// Delete File if its correct encrypted
		if (file.canWrite())
		{
			file.delete();
		}
		// setProgressvalue > 100 to exit the progressdialog of the UI
		progressValue = 101;
		progressHandler.sendEmptyMessage(progressValue);
	}

	/**
	 * Handles the decryption on the fly of a file It doesn't delete the key
	 * from the smartcard, because the encrypted file still exists after
	 * decryption
	 * 
	 * @throws CardException
	 *             the card exception is thrown, the access to the secure
	 *             element failed
	 */
	public void decryptOnTheFly() throws CardException
	{

		File file = new File(fileName);

		// test if the file is ready to decrypt
		if (!acceptFile(file, DECRYPT))
		{
			return;
		}

		// // Create a read-only memory-mapped file
		// FileChannel roChannel = new RandomAccessFile(file, "r").getChannel();
		// ByteBuffer readonlybuffer =
		// roChannel.map(FileChannel.MapMode.READ_ONLY, 0,
		// (int)roChannel.size());
		// // Create a read-write memory-mapped file
		// FileChannel rwChannel = new RandomAccessFile(file,
		// "rw").getChannel();
		// ByteBuffer writeonlybuffer =
		// rwChannel.map(FileChannel.MapMode.READ_WRITE, 0,
		// (int)rwChannel.size());
		//		
		// // Create a private (copy-on-write) memory-mapped file.
		// // Any write to this channel results in a private copy of the data.
		// FileChannel pvChannel =
		// new RandomAccessFile(file, "rw").getChannel();
		//        
		// ByteBuffer privatebuffer =
		// roChannel.map(FileChannel.MapMode.READ_WRITE,
		// 0, (int)rwChannel.size());

		// handle main UI
		Message msg = encryptedFileHandler.obtainMessage();
		Bundle b = new Bundle();
		b.putString("file", file.getName());
		msg.setData(b);
		encryptedFileHandler.sendMessage(msg);
		progressValue = 0;
		progressHandler.sendEmptyMessage(progressValue);
		Log.d(TAG, "Try to decrypt " + file.toString());

		// rounds needed to fully process the specified file
		int rounds = 1;
		long blockSize = 0L;

		byte[] decryptionKey = null;
		byte[] dataToDecr = null;
		byte[] id = null;

		// initialize round counter
		if (file.length() > maxBlockSize)
		{
			rounds = (int) (file.length() / maxBlockSize);

			if (file.length() % maxBlockSize != 0)
				rounds++;

			blockSize = maxBlockSize;
		}

		// performing the determing number of round and processing the file-data
		// in blocks
		for (int i = 0; i < rounds; i++)
		{
			progressValue = (int) ((double) (i + 1) / rounds * 100);
			progressHandler.sendEmptyMessage(progressValue);

			boolean completed = false;

			if (i == rounds - 1)
			{
				completed = true;
				blockSize = 0L;
			}

			byte[] encData;

			if (i == 0 && !completed)
				// must be done, because of the right
				// block-sizes for cipher
				encData = Helper.readFile(file, blockSize + ENCRYPTION_RANDOM_ID_LENGTH
						+ ENCRYPTION_MAGIC_NUMBER.length);
			else
				encData = Helper.readFile(file, blockSize);

			// header must be extracted in the first round
			if (i == 0)
			{
				fileName = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4);

				int idLen = ENCRYPTION_RANDOM_ID_LENGTH;
				int magicNrLen = ENCRYPTION_MAGIC_NUMBER.length;
				int dataLen = encData.length - idLen - magicNrLen;

				// sorting the data
				id = new byte[idLen];
				dataToDecr = new byte[dataLen];
				System.arraycopy(encData, magicNrLen, id, 0, idLen);
				System.arraycopy(encData, idLen + magicNrLen, dataToDecr, 0, dataLen);

				decryptionKey = getCipherKey(id, password, DECRYPT);

			} else
			{
				dataToDecr = encData;
			}

			// decrypting the data
			byte[] decrData = Helper.decryptData(dataToDecr, decryptionKey, completed);

			// writing back the decrypted data
			int index = fileName.lastIndexOf("/") + 1;
			File plainFile = new File(fileName.substring(0, index) + "tmp_" + fileName.substring(index));
			Helper.writeFile(plainFile, decrData, completed);
		}

		// delete not the key from the card, because it still needed

		// setProgressvalue > 100 to exit the progressdialog of the UI
		progressValue = 101;
		progressHandler.sendEmptyMessage(progressValue);

	}

	/**
	 * This private method tests if the operation is correct for the kind of
	 * file. Furthermore there is tested if there are read rights to the file.
	 * 
	 * @param file
	 *            The file you want to prove for the desired operation.
	 * @param function
	 *            operation you want to do on the file. Choose between
	 *            {@link #DECRYPT} or {@link #ENCRYPT}
	 * @return true, if you can make the requested operations on the file ,
	 *         otherwise false
	 */
	private boolean acceptFile(File file, int function)
	{
		File tmp = file;
		if (tmp.canRead())
		{
			if (Helper.isFileEncrypted(tmp) && DECRYPT == function)
			{
				if (!(new File(tmp.getAbsolutePath().substring(0, tmp.getAbsolutePath().length() - ".enc".length())))
						.exists())
				{
					return true;
				} else
				{
					Message msg = encryptedFileHandler.obtainMessage();
					Bundle b = new Bundle();
					b
							.putString("error", "There already exists a File with the name: " + tmp.getAbsolutePath()
									+ ".enc");
					msg.setData(b);
					encryptedFileHandler.handleMessage(msg);
					return false;
				}
			} else if (!Helper.isFileEncrypted(tmp) && ENCRYPT == function)
			{
				if (!(new File(tmp.getAbsolutePath() + ".enc")).exists())
				{
					return true;
				} else
				{
					Message msg = encryptedFileHandler.obtainMessage();
					Bundle b = new Bundle();
					b
							.putString("error", "There already exists a File with the name: " + tmp.getAbsolutePath()
									+ ".enc");
					msg.setData(b);
					encryptedFileHandler.handleMessage(msg);
					return false;
				}
			} else
			{
				// this case only occur when a file isn't encrypted, but has the
				// ending ".enc"
				Message msg = encryptedFileHandler.obtainMessage();
				Bundle b = new Bundle();
				b.putString("error", "File isn't " + ((function == ENCRYPT) ? "encrypted" : "decrypted"));
				msg.setData(b);
				encryptedFileHandler.handleMessage(msg);
				return false;
			}
		} else
		{
			Message msg = encryptedFileHandler.obtainMessage();
			Bundle b = new Bundle();
			b.putString("error", "The file " + tmp + "cant be read");
			msg.setData(b);
			encryptedFileHandler.handleMessage(msg);
			return false;
		}
	}

	/**
	 * Method to generate a random file ID to later on address the cipher at the
	 * secure-element.
	 * 
	 * @return the generated random ID.
	 */
	private byte[] generateRandomID()
	{
		// Creating the random file ID
		byte[] tmpRandomID = new BigInteger(ENCRYPTION_RANDOM_ID_LENGTH * 8, new Random(System.currentTimeMillis()))
				.toByteArray();
		byte[] randomID;
		// Cutting off the first byte because BigInteger is bad
		if (tmpRandomID.length == ENCRYPTION_RANDOM_ID_LENGTH)
		{
			randomID = tmpRandomID;
		} else
		{
			randomID = new byte[ENCRYPTION_RANDOM_ID_LENGTH];
			System.arraycopy(tmpRandomID, tmpRandomID.length - ENCRYPTION_RANDOM_ID_LENGTH, randomID, 0,
					ENCRYPTION_RANDOM_ID_LENGTH);
		}

		return randomID;
	}

	/**
	 * Gets the key from the secure element that belongs to the given File ID
	 * and passphrase. If no such key exists yet, a new one will be created.
	 * 
	 * @param id
	 *            File-ID
	 * @param passphrase
	 *            password
	 * @param encryption
	 *            Ciphering modus. Choose between {@link #DECRYPT} or
	 *            {@link #ENCRYPT}
	 * 
	 * @return The cipher key belonging to the given File ID and passphrase.<br>
	 *         If there does't exists such key, a new one will be created and
	 *         saved on the secure element
	 * @throws CardException
	 *             If the connection to the card couldn't be established, or the
	 *             password was wrong to the file-ID.
	 */
	public byte[] getCipherKey(byte[] id, String passphrase, int encryption) throws CardException
	{
		byte[] key = null;

		CardCommandBuilder verifyCcb = new CardCommandBuilder(CardCommandBuilder.CardCommand.VerifyKey, id, passphrase);

		Log.v(TAG, "File ID: " + Helper.bytesToString(id));
		Log.v(TAG, "Passphrase: " + passphrase);

		byte[] verifyAPDU = verifyCcb.getCommandBytes();
		Log.i(TAG, "Verify-Command: " + Helper.bytesToString(verifyAPDU));

		// Send key request to card
		byte[] verifyKeyResp = cardChannel.transmit(verifyAPDU);
		Log.i(TAG, "Verify-Response: " + Helper.bytesToString(verifyKeyResp));

		if ((verifyKeyResp[verifyKeyResp.length - 2] & 0xFF) != 0x90
				|| (verifyKeyResp[verifyKeyResp.length - 1] & 0xFF) != 0x00)
		{
			if (encryption == DECRYPT)
			{
				Log.e(TAG, "Wrong passphrase");
				throw new CardException("Wrong passphrase");
			}
			Log.i(TAG, "Key does not exist yet, will now create it");

			// Create key
			CardCommandBuilder createCcb = new CardCommandBuilder(verifyCcb, CardCommandBuilder.CardCommand.CreateKey);
			byte[] createAPDU = createCcb.getCommandBytes();
			Log.i(TAG, "Create-Command: " + Helper.bytesToString(createAPDU));

			byte[] createKeyResp = cardChannel.transmit(createAPDU);
			Log.i(TAG, "Create-Response: " + Helper.bytesToString(createKeyResp));
			key = new byte[createKeyResp.length - 2];
			System.arraycopy(createKeyResp, 0, key, 0, createKeyResp.length - 2);

			if (((createKeyResp[createKeyResp.length - 2] & 0xFF) != 0x90)
					|| ((createKeyResp[createKeyResp.length - 1] & 0xFF) != 0x00))
			{
				Log.i(TAG, "Create-Key Response: " + createKeyResp[createKeyResp.length - 2] + " "
						+ createKeyResp[createKeyResp.length - 1]);
				Log.e(TAG, "Could not create key");
				throw new CardException("Could not Create Key");
			}
		} else
		{
			// Cut 90 00
			key = new byte[verifyKeyResp.length - 2];
			System.arraycopy(verifyKeyResp, 0, key, 0, verifyKeyResp.length - 2);
		}

		return key;
	}

	/**
	 * Deletes a specific cipher-key from this secure SD-card.
	 * 
	 * @param id
	 *            the ID which identifies the specific cipher-key uniquely in
	 *            connection with a passphrase.
	 * @param passphrase
	 *            the passphrase which identifies the specific cipher-key
	 *            uniquely in connection with a ID.
	 * @throws CardException
	 *             the card exception is thrown, if the key could not be found
	 *             or deleted at this card.
	 */
	public void deleteCipherKey(byte[] id, String passphrase) throws CardException
	{
		CardCommandBuilder deleteCcb = new CardCommandBuilder(CardCommandBuilder.CardCommand.DeleteKey, id, passphrase);

		byte[] deleteCommandAPDU = deleteCcb.getCommandBytes();
		Log.i(TAG, "Delete-Command: " + deleteCommandAPDU);
		byte[] verifyKeyResp = cardChannel.transmit(deleteCommandAPDU);
		if (((verifyKeyResp[0] & 0xFF) != 0x90) || ((verifyKeyResp[1] & 0xFF) != 0x00))
		{
			Log.e(TAG, "Could not delete key");
		}
		Log.i(TAG, "Delete-Response: " + verifyKeyResp.toString());
	}

}
