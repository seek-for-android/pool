package com.gieseckedevrient.android.pcscdroid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class PcscInstaller extends AsyncTask {

	private String pcscDir;
	private String pcscdPath;
	private Context appContext;
	private String mMscFile = "";
	private boolean mIsConnected;
	private boolean mDefaultApplicationSelectedOnBasicChannel;
	private byte[] mAtr;
	private boolean hasSuperUser = false;

	public PcscInstaller(Context context) {
		appContext = context;
		pcscDir = "/sdcard/external_sd/pcsc";
		hasSuperUser = hasSuperUser();
	}

	public void extractFiles() throws IOException {

		InputStream databaseInputStream;
		if (hasSuperUser)
			databaseInputStream = appContext.getResources().openRawResource(
					R.raw.pcscd);
		else
			databaseInputStream = appContext.getResources().openRawResource(
					R.raw.pcscd_msconly);
		FileOutputStream fos = appContext.openFileOutput("pcscd",
				Context.MODE_PRIVATE);
		byte[] buffer = new byte[16384];
		int bytesRead;
		while ((bytesRead = databaseInputStream.read(buffer)) > 0) {
			fos.write(buffer, 0, bytesRead);
		}
		fos.close();
		Log.v("pcscdroid", "Exctracted pcscd to: " + appContext.getFilesDir()
				+ "/pcscd");

		if (hasSuperUser)
			databaseInputStream = appContext.getResources().openRawResource(
					R.raw.testpcsc);
		else
			databaseInputStream = appContext.getResources().openRawResource(
					R.raw.testpcsc_msconly);

		fos = appContext.openFileOutput("testpcsc", Context.MODE_PRIVATE);
		buffer = new byte[16384];
		while ((bytesRead = databaseInputStream.read(buffer)) > 0) {
			fos.write(buffer, 0, bytesRead);
		}
		fos.close();
		Log.v("pcscdroid",
				"Exctracted testpcsc to: " + appContext.getFilesDir()
						+ "/testpcsc");

		File file = new File(appContext.getFilesDir() + "/lib");
		file.mkdirs();
		Log.v("pcscdroid", "Created: " + appContext.getFilesDir() + "/lib");

		file = new File(appContext.getFilesDir() + "/ipc");
		file.mkdirs();
		Log.v("pcscdroid", "Created: " + appContext.getFilesDir() + "/ipc");
		file = new File(appContext.getFilesDir() + "/etc");
		file.mkdirs();
		Log.v("pcscdroid", "Created: " + appContext.getFilesDir() + "/etc");

		databaseInputStream = appContext.getResources().openRawResource(
				R.raw.libifdmsc_so);
		fos = new FileOutputStream(appContext.getFilesDir()
				+ "/lib/libifdmsc.so");
		buffer = new byte[16384];
		while ((bytesRead = databaseInputStream.read(buffer)) > 0) {
			fos.write(buffer, 0, bytesRead);
		}
		fos.close();
		Log.v("pcscdroid",
				"extracted: libifdmsc_so -> " + appContext.getFilesDir()
						+ "/lib/libifdmsc.so");

		databaseInputStream = appContext.getResources().openRawResource(
				R.raw.reader_conf);
		fos = new FileOutputStream(appContext.getFilesDir()
				+ "/etc/reader.conf");
		buffer = new byte[16384];
		while ((bytesRead = databaseInputStream.read(buffer)) > 0) {
			fos.write(buffer, 0, bytesRead);
		}
		fos.close();
		Log.v("pcscdroid",
				"extracted: reader_conf -> " + appContext.getFilesDir()
						+ "/etc/reader.conf");

		databaseInputStream = appContext.getResources().openRawResource(
				R.raw.mscpath_conf);
		fos = new FileOutputStream(appContext.getFilesDir()
				+ "/lib/mscpath.conf");
		buffer = new byte[16384];
		while ((bytesRead = databaseInputStream.read(buffer)) > 0) {
			fos.write(buffer, 0, bytesRead);
		}
		fos.close();
		Log.v("pcscdroid",
				"extracted: mscpath_conf -> " + appContext.getFilesDir()
						+ "/lib/mscpath.conf");

		if (hasSuperUser) {
			file = new File(appContext.getFilesDir()
					+ "/usb/ifd-ccid.bundle/Contents/Linux");
			file.mkdirs();
			Log.v("pcscdroid", "Created: " + appContext.getFilesDir()
					+ "/usb/ifd-ccid.bundle/Contents/Linux");

			databaseInputStream = appContext.getResources().openRawResource(
					R.raw.info_plist);
			fos = new FileOutputStream(appContext.getFilesDir()
					+ "/usb/ifd-ccid.bundle/Contents/Info.plist");
			buffer = new byte[16384];
			while ((bytesRead = databaseInputStream.read(buffer)) > 0) {
				fos.write(buffer, 0, bytesRead);
			}
			fos.close();
			Log.v("pcscdroid",
					"extracted: info_plist -> " + appContext.getFilesDir()
							+ "/usb/ifd-ccid.bundle/Contents/Info.plist");

			databaseInputStream = appContext.getResources().openRawResource(
					R.raw.libccid_so);
			fos = new FileOutputStream(appContext.getFilesDir()
					+ "/usb/ifd-ccid.bundle/Contents/Linux/libccid.so");
			buffer = new byte[16384];
			while ((bytesRead = databaseInputStream.read(buffer)) > 0) {
				fos.write(buffer, 0, bytesRead);
			}
			fos.close();
			Log.v("pcscdroid",
					"extracted: libccid_so -> " + appContext.getFilesDir()
							+ "/usb/ifd-ccid.bundle/Contents/Linux/libccid.so");
			
			try {
				Process remount = Runtime.getRuntime().exec(
						new String[] { "su","-c", "mount", "-o", "remount", "rw",
								"/system" });
				Log.v("pcscdroid", "Remounted System Partition");
			} catch (IOException e) {
				Log.v("pcscdroid", "Failed to remount system partition");
				e.printStackTrace();
			}
			try {
				Process copyLibCCid = Runtime.getRuntime().exec(
						new String[] { "su","-c", "cat", appContext.getFilesDir()
								+ "/usb/ifd-ccid.bundle/Contents/Linux/libccid.so", ">", "/system/lib/libccid.so"});
				Log.v("pcscdroid", "copied libccid to /system/lib");
			} catch (IOException e) {
				Log.v("pcscdroid", "failed to copy libccid");
				e.printStackTrace();
			}
			try {
				Process copyLibPCSCLite = Runtime.getRuntime().exec(
						new String[] { "su","-c", "cat", appContext.getFilesDir()
								+ "/../lib/libpcsclite.so", ">", "/system/lib/libpcsclite.so"});
				Log.v("pcscdroid", "copied libpcsclite to /system/lib");
			} catch (IOException e) {
				Log.v("pcscdroid", "failed to copy libpcsclite");
				e.printStackTrace();
			}
			
			

		}

		try {
			Process chmod = Runtime.getRuntime().exec(
					new String[] { "chmod", "-R", "777",
							appContext.getFilesDir() + "" });

			Log.v("pcscdroid", "Set ipc-dir permissions");
		} catch (IOException e) {
			Log.v("pcscdroid", "Failed to set ipc-dir permissions");
			e.printStackTrace();
		}
		
	}

	public void install() {

		try {
			this.extractFiles();
			
		} catch (IOException e) {

			Toast.makeText(appContext, "Previous pcscd locked files. Killing.",
					Toast.LENGTH_SHORT).show();
		}

	}

	public boolean isPresent() {
		try {
			configureMobileSecurityCardAccess();
			Log.v("pcscdroid", "Using Card: " + mMscFile);
			if (!mMscFile.equalsIgnoreCase("")) {
				try {

					FileWriter mscConfFile = new FileWriter(
							appContext.getFilesDir() + "/lib/mscpath.conf",
							false);
					BufferedWriter out = new BufferedWriter(mscConfFile);
					out.write(mMscFile);

					out.close();
					Intent atrIntent = new Intent();
					atrIntent.setAction("android.intent.action.MAIN");
					atrIntent.putExtra("updateAtr", true);
					atrIntent.putExtra("ATR", toHex(mAtr));
					appContext.sendBroadcast(atrIntent);

					Intent pathIntent = new Intent();
					pathIntent.setAction("android.intent.action.MAIN");
					pathIntent.putExtra("updateReader", true);
					pathIntent.putExtra("reader",
							mMscFile.substring(0, mMscFile.lastIndexOf("/")));
					appContext.sendBroadcast(pathIntent);

				} catch (Exception e) {
					// Could not write to mscpath.conf
					Log.v("pcscdroid", "Error: Could not write to mscpath.conf");
				}
				return true;
			}

		} catch (Exception e1) {

			Log.v("pcscdroid", "Error while finding MSC Path.");

		}
		return false;
	}

	public void remove() {
		deleteFiles(appContext.getFilesDir() + "/*");
		Toast.makeText(appContext, "Successfully removed.", Toast.LENGTH_SHORT)
				.show();
		Log.v("pcscdroid", "cleaned all files");
	}

	public static void deleteFiles(String path) {

		File file = new File(path);

		if (file.exists()) {
			String deleteCmd = "rm -r " + path;
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec(deleteCmd);
			} catch (IOException e) {
			}
		}
	}

	@Override
	protected Object doInBackground(Object... params) {

		try {

			extractFiles();
		} catch (IOException e) {
			Log.v("pcscdroid", "Error while extracting files.");
		}
		return null;
	}

	private void configureMobileSecurityCardAccess() throws Exception {

		BufferedReader rd = null;
		try {
			File mounts = new File("/proc/mounts");
			rd = new BufferedReader(new FileReader(mounts));
		} catch (FileNotFoundException e) {
			configureMobileSecurityCardAccessFallback();
			return;
		}

		String line = null;
		;
		try {
			while ((line = rd.readLine()) != null) {
				if (parseAndCheckMscMountPoint(line) == true) {
					// MSC found. No need to search further.
					break;
				}
				mMscFile = "";
			}
			rd.close();
		} catch (IOException ignore) {
		}

		if (mMscFile.equalsIgnoreCase("")) {
			configureMobileSecurityCardAccessFallback();
			return;
		}
	}

	private void configureMobileSecurityCardAccessFallback() throws Exception {

		Log.v("pcscdroid", "'/proc/mounts' could not be read.");
		// as a fallback, run the mount command without parameter
		// in order to get partition information
		Runtime rt = Runtime.getRuntime();
		Process ps = null;
		try {
			ps = rt.exec("mount");
			ps.waitFor();
		} catch (IOException io) {
			Log.v("pcscdroid", "'Mount' command could not be executed.");
			return;
		} catch (InterruptedException ignore) {
		}
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				ps.getInputStream()));

		String line = null;
		;
		try {
			while ((line = rd.readLine()) != null) {
				if (parseAndCheckMscMountPoint(line) == true) {
					// MSC found. No need to search further.
					break;
				}
				mMscFile = "";
			}
			rd.close();
		} catch (IOException ignore) {
		}
	}

	private boolean parseAndCheckMscMountPoint(String line) {

		String[] tokens = line.split("[ \t]");
		for (String token : tokens) {
			token.trim();
			if (token.length() == 0) {
				continue;
			}
			File path = new File(token);
			if (checkMscMountPoint(path) == true) {
				Log.v("pcscd", "####FOUND MSC AT:" + path);
				return true;
			}
		}
		return false;
	}

	private boolean checkMscMountPoint(File path) {
		if (path.isDirectory() && path.canWrite()) {
			mMscFile = path.getAbsolutePath() + "/msc.sig";

			try {
				internalConnect();
			} catch (Exception e) {
				// could not connect to card

				return false;
			}

			try {
				internalDisconnect();
			} catch (Exception e) {
				// Could not disconnect from card
				return false;
			}
			return true;

		}
		return false;
	}

	protected void internalConnect() throws Exception {

		if (mMscFile.equals("")) {
			Log.v("pcscdroid", "No Mobile Security Card detected");
		}
		Log.v("pcscdroid", "Testing file at: " + mMscFile);

		try {
			if (Open(mMscFile) == false)
				throw new Exception("Opening communication file failed");
		} catch (Exception e) {
			throw new Exception("Opening communication file failed: "
					+ e.getMessage());
		}

		byte[] response = null;
		try {
			response = Transmit(new byte[] { 0x20, 0x12, 0x01, 0x01, 0x00 });
		} catch (Exception e) {
			try {
				Close();
			} catch (Exception ignore) {
			}
			throw new Exception(
					"Sending the 'Request SE' APDU to the smart card chip failed");
		}

		if (response == null || response.length < 2) {
			try {
				Close();
			} catch (Exception ignore) {
			}
			throw new Exception(
					"Requesting the smart card chip did not return any response");
		} else {
			mAtr = new byte[response.length - 2];
			System.arraycopy(response, 0, mAtr, 0, response.length - 2);
		}
		
	}

	protected void internalDisconnect() throws Exception {

		if (mIsConnected == false) {
			return;
		}

		try {
			Transmit(new byte[] { 0x20, 0x15, 0x01, 0x00, 0x00 });
		} catch (Exception ignore) {
		}

		try {
			Close();
		} catch (Exception ignore) {
		}

		mIsConnected = false;

	}

	private static Throwable loadException;

	static {
		try {
			Runtime.getRuntime().loadLibrary("msc");
		} catch (Throwable t) {
			System.out.println("Error loading Library libifdmsc.so");
			t.printStackTrace();
		}
	}

	private static Throwable getLoadError() {
		return loadException;
	}

	private static boolean isLoaded() {
		return (loadException == null);
	}

	public static String toHex(byte[] bytes) {
		BigInteger bi = new BigInteger(1, bytes);
		return String.format("%0" + (bytes.length << 1) + "X", bi);
	}

	public static boolean hasSuperUser() {

		

		
		try {
			File file = new File("/system/xbin/su");
			if (file.exists()) {
				return true;
			}
		} catch (Throwable e1) {

		}
		try {
			File file = new File("/system/bin/su");
			if (file.exists()) {
				return true;
			}
		} catch (Throwable e1) {

		}

		return false;
	}

	private static native void Close() throws Exception;

	private static native boolean Open(String storageName) throws Exception;

	private static native byte[] Transmit(byte[] command) throws Exception;

	private static native void KeepAlive() throws Exception;

	private static native void testNative() throws Exception;

}
