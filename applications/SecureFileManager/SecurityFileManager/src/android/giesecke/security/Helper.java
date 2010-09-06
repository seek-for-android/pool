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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

/**
 * This class is for handling file operations and cryptographical operations e.g encrption and decryption
 * 
 * @author stuerzem
 */
public class Helper {

	/**
	 * Tag for logging
	 */
	private static final String TAG = "Helper";
	
	/**
	 * For Fileoperations
	 * Position in the file, which is read
	 */
	private static long readStreamPosition;	
	
	/**
	 * For Fileoperations
	 * Position in the file, which is written
	 */
	private static long writeStreamPosition = 0L;	

	/**
	 * For Fileoperations
	 * FileOutputStream-object to leave the stream open during multiple write-operations
	 */
	private static FileOutputStream fos = null;

	// flags to indicate if the crypto-algorithms are initialized. This should only be done
	// once, during a multi-part crypto-operation

	/**
	 * flag to indicate if the crypto-algorithms is initialized. 
	 * This should only be done once, during a multi-part crypto-operation
	 */
	private static boolean decryptInitialized = false;
	
	/**
	 * flag to indicate if the crypto-algorithms is initialized. 
	 * This should only be done once, during a multi-part crypto-operation
	 */
	private static boolean encryptInitialized = false;

	/**
	 * Cipherobject to make decrypt operations
	 */
	private static Cipher decryptCipher;
	
	/**
	 * Cipherobject to make encrypt operations
	 */
	private static Cipher encryptCipher;

	/**
	 * This method handels the write operations for a specified file.
	 * 
	 * @param file the file to which the data should be written.
	 * @param data the data,which should be written to a specified file.
	 * @param writeCompleted a flag which indicates, if writing to a file is complete, or
	 *            if there data left, which should be append to the end of the
	 *            file.
	 */
	public static void writeFile(File file, byte[] data, boolean writeCompleted) {
		// Writing the encrypted data with header
		try {

			if (fos == null)
				fos = new FileOutputStream(file);

			FileChannel fileChannel = fos.getChannel();

			if (writeStreamPosition != 0L)
				fileChannel.position(writeStreamPosition);

			fos.write(data);
			fos.flush();

			writeStreamPosition = fileChannel.position();

			// reset flags, if file-operation is completed
			if (writeCompleted) {
				fileChannel.close();
				fos.close();
				fos = null;
				writeStreamPosition = 0L;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * This method handels the reading of data from a specific file.
	 * 
	 * @param file the file, to read from.
	 * @param blockSize the length of the data-block, which should be read from the specified file.
	 * @return the data read from the file.
	 */
	public static byte[] readFile(File file, long blockSize) {
		FileInputStream fis;
		byte[] fileContent = null;

		try {
			fis = new FileInputStream(file);

			FileChannel fileChannel = fis.getChannel();

			int bytesToRead;
			fileChannel.position(readStreamPosition);

			int dataLen = fis.available();

			// if there is a zero block size specified, read the whole file
			if (blockSize == 0L){
				bytesToRead = dataLen;}
			else{
				bytesToRead = (int) blockSize;}

			fileContent = new byte[bytesToRead];

			// reading the data
			for (int i = 0; i < bytesToRead; i++){
				fileContent[i] = (byte) fis.read();}

			// storing read-position
			readStreamPosition = fileChannel.position();

			fis.close();
			fileChannel.close();

			// zero blockSize indicates, that reading of this file is completed,
			// stream position reset
			if (blockSize == 0L) {
				readStreamPosition = 0L;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fileContent;
	}

	/**
	 * This method could be used to determine, if a file is encrypted or not.
	 * 
	 * @param File
	 *            the data to proof.
	 * @return true, if the file is encrypted, otherwise false.
	 */
	public static boolean isFileEncrypted(File tmp)
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(tmp);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		byte[] magicNumber = new byte[CipheredFile.ENCRYPTION_MAGIC_NUMBER.length];
		try
		{
			fis.read(magicNumber, 0, magicNumber.length);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		Log.i(TAG, "FileContent: " + Helper.bytesToString(magicNumber));
		Log.i(TAG, "Encrypt: " + tmp.getAbsolutePath());

		if (Arrays.equals(CipheredFile.ENCRYPTION_MAGIC_NUMBER, magicNumber))
			return true;
		return false;
	}
	
	/**
	 * This method should be used to encrypt a block of data.
	 * 
	 * @param fileData
	 *            the data to encrypt.
	 * @param encryptionKey
	 *            the key to initialize the cipher-algorithm.
	 * @param encryptCompleted
	 *            a flag, that indicates, that a multi-part encryption is to be
	 *            completed. e.g. false, if the fileData consist of multiple
	 *            parts. true, if the fileData consists only of one single part
	 *            and so they could be encrypted in one operation.
	 * 
	 * @return the encrypted data.
	 */
	public static byte[] encryptData(byte[] fileData, byte[] encryptionKey,
			boolean encryptCompleted) {

		// Initializing may only be done at the start of a multi-part crypto-operation.
		// if it's a single part crypto-operation initialization must always be done.
		if (!encryptInitialized) {
			// Initializing the cipher
			try {
				encryptCipher = Cipher.getInstance("AES");
			} catch (Exception e) {
				Log.e(TAG, "Error while initializing encryption.");
				return null;
			}

			try {
				SecretKeySpec keySpec = new SecretKeySpec(encryptionKey, "AES");
				encryptCipher.init(Cipher.ENCRYPT_MODE, keySpec);
			} catch (Exception e) {
				Log.e(TAG, "Error while initializing encryption.");
				return null;
			}
			encryptInitialized = true;
		}

		// encrypting
		try {
			if (!encryptCompleted)
				// done in case of multi-part operation
				fileData = encryptCipher.update(fileData);
			else
				// done in case of single part operation or to finish a multi-part operation
				fileData = encryptCipher.doFinal(fileData);
		} catch (Exception e) {
			Log.e(TAG, "Error during encryption.");
			return null;
		}

		// at the and of an multi-part operation flags must be reset
		if (encryptCompleted)
			encryptInitialized = false;
		return fileData;
	}

	/**
	 * This method should be used to decrypt a block of data.
	 * 
	 * @param fileData the data to decrypt.
	 * @param decryptionKey the key to initialize the cipher-algorithm.
	 * @param decryptCompleted a flag, that indicates, that a multi-part decryption is to be
	 *            completed. e.g. false, if the fileData consist of multiple
	 *            parts. true, if the fileData consists only of one single part
	 *            and so they could be decrypted in one operation.
	 * 
	 * @return the decrypted data.
	 */
	public static byte[] decryptData(byte[] fileData, byte[] decryptionKey,
			boolean decryptCompleted) {

		// Initializing may only be done at the start of a multi-part
		// crypto-operation.
		// if it's a single part crypto-operation initialization must always be
		// done.
		if (!decryptInitialized) {
			// Initializing the cipher
			try {
				decryptCipher = Cipher.getInstance("AES");
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			try {
				SecretKeySpec keySpec = new SecretKeySpec(decryptionKey, "AES");
				decryptCipher.init(Cipher.DECRYPT_MODE, keySpec);
			} catch (Exception e) {
				Log.e(TAG, "Error while initializing decryption.");
				return null;
			}
			decryptInitialized = true;
		}

		// Decrypting
		try {
			if (!decryptCompleted) // done in case of multi-part operation
				fileData = decryptCipher.update(fileData);
			else
				// done in case of single part operation or to finish a
				// multi-part operation
				fileData = decryptCipher.doFinal(fileData);
		} catch (Exception e) {
			Log.e(TAG, "Error during decryption.");
			return null;
		}

		// at the and of an multi-part operation flags must be reset
		if (decryptCompleted)
			decryptInitialized = false;

		return fileData;
	}
	
	/**
	 * This method is responsible to convert the SW1 and SW2 bytes, received with an response APDU into 
	 * more significant text explanations.
	 * 
	 * @param apdu the APDU received from the card in hex-format.
	 * @return the hex-APDU with an additional text-explanation.
	 */
	public static String APDUtoText(String apdu)
	{
		if(apdu.compareTo("9000")==0)
			return new String(apdu+" - Command executed successful!");
		if(apdu.compareTo("6e00")==0)
			return new String(apdu+" - Class not supported!");
		return apdu;
	}
	
	/**
	 * This method converts a decimal representation of an value into the corresponding
	 * hex representation.
	 * 
	 * @param decimalValue the decimal presentation of anything.
	 * @return a String containing the hex presentation of anything.
	 */
	public static String toHexOutput(int decimalValue)
	{		
		return Integer.toHexString(decimalValue);
	}
	
	/**
	 * Converts a string consisting of hex-coded signs into an byte-array.
	 * 
	 * @param hexString the input string with values in hex representation.
	 * @return a byte array holding the hex-values in the expected format.
	 */
	public static byte[] hexStringToByteArray(String hexString) 
	{
		int len = hexString.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) 
		{
		   data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
		                         + Character.digit(hexString.charAt(i+1), 16));
		}
		return data;
	}
	
	/**
	 * The Internal helper method converts a byte[] to a String
	 * 
	 * @param byte[] to transform in a String 
	 * @return String of the byte[]
	 */
	public static String bytesToString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (byte b : bytes) {
			sb.append(String.format("%02x ", b & 0xFF));
		}
		return sb.toString();
	}
}
