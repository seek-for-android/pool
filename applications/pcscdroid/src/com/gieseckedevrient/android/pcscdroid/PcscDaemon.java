package com.gieseckedevrient.android.pcscdroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

public class PcscDaemon extends AsyncTask {
	private String pcscdPath;
	private boolean running;
	private Process pcscd = null;
	private ArrayList<String> logLines = new ArrayList<String>();
	private Context appContext;
	private String reader, atr;
	private boolean hasSuperUser = false;
	private boolean hasUsbReader = false;
	private boolean hasMscReader = false;
	private boolean lastLogLineNotMSCInsert = false;

	public PcscDaemon(String pathToPcscd, Context appContext) {
		this.pcscdPath = pathToPcscd;
		this.appContext = appContext;
		this.running = false;
		hasSuperUser = hasSuperUser();
		try {
			Process chmod = Runtime.getRuntime().exec(
					new String[] { "chmod", "777", pcscdPath });

			Log.v("pcscdroid", "Set pcscd permissions");
		} catch (IOException e) {
			Log.v("pcscdroid", "Failed to set pcscd permissions");
			e.printStackTrace();
		}

	}

	public void killPcscd() {

		try {
			Log.v("pcscdroid", "Checking for previous pcscd.");
			Process ps = Runtime.getRuntime().exec(new String[] { "ps" });

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					ps.getInputStream()));
			int read;
			char[] buffer = new char[1];
			StringBuffer output = new StringBuffer();

			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
				if (output.charAt(output.length() - 1) == '\n') {
					// Log.v("pcscd-log", output.toString());

					if (output.toString().contains("files/pcscd")
							|| output.toString().contains(" pcscd")) {
						String pid = output.toString();
						Pattern pattern = Pattern.compile("\\s+\\d+\\s+");
						Matcher matcher = pattern.matcher(pid);
						matcher.find();
						pid = matcher.group(0).trim();
						Log.v("pcscdroid", "killing:" + pid);
						Process leKiller;
						if (hasSuperUser)
							leKiller = Runtime.getRuntime().exec(
									new String[] { "su", "-c", "kill", pid });
						else
							leKiller = Runtime.getRuntime().exec(
									new String[] { "kill", pid });
						Log.v("pcscdroid", "Killed previous pcscd at PID '"
								+ pid + "'");

						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {

						}
						this.deleteFiles(appContext.getFilesDir() + "/ipc/*");
						Log.v("pcscdroid",
								"Cleared: " + appContext.getFilesDir()
										+ "/ipc/*");

					}
					output = new StringBuffer();
				}

			}
		} catch (IOException e) {
			Log.v("pcscdroid", "Error checking for previous pcscd.");
			e.printStackTrace();

		}

	}

	public PcscDaemon() {
		pcscdPath = "pcscd";
	}

	public void startService() {
		this.execute();
	}

	public void stopService() {
		if (this.isRunning()) {
			Log.v("pcscdroid", "Stopping pcscd.");
			killPcscd();

			Intent statusIntent = new Intent();
			statusIntent.setAction("android.intent.action.MAIN");
			statusIntent.putExtra("updateStatus", true);
			statusIntent.putExtra("status", false);
			appContext.sendBroadcast(statusIntent);
			running = false;

		} else
			Log.v("pcscdroid", "Pcscd is not running biatch." + running);
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	protected Object doInBackground(Object... params) {

		this.killPcscd();

		try {
			this.running = true;
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(appContext);
			Log.v("pcscdroid",
					"logsettings:" + settings.getInt("logSettings", 1001));
			String[] startpcscd;
			if (hasSuperUser)
				startpcscd = new String[] { "su", "-c", pcscdPath, "-f", "-a",
						"-d" };
			else
				startpcscd = new String[] { pcscdPath, "-f", "-a", "-d" };

			this.pcscd = Runtime.getRuntime().exec(startpcscd);
			Log.v("pcscdroid", "Started pcscd.(" + startpcscd.toString() + ")"
					+ running);
			Intent statusIntent = new Intent();
			statusIntent.setAction("android.intent.action.MAIN");
			statusIntent.putExtra("updateStatus", true);
			statusIntent.putExtra("status", true);
			appContext.sendBroadcast(statusIntent);


			BufferedReader reader = new BufferedReader(new InputStreamReader(
					pcscd.getInputStream()));
			int read;
			char[] buffer = new char[1];
			StringBuffer output = new StringBuffer();

			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
				if (output.charAt(output.length() - 1) == '\n') {
					// Log.v("pcscd-log", output.toString());
					parseLogLine(output.toString());
					output = new StringBuffer();
				}

			}

		} catch (IOException e) {
			Log.v("pcscdroid", "Error starting pcscd.");
			e.printStackTrace();

		}
		return null;
	}
	
	private void parseLogLine(String logLine) {
		Log.v("pcscd-log", logLine);
		
		if (logLine.contains("Card ATR") && hasSuperUser && hasUsbReader && lastLogLineNotMSCInsert) {
			Intent atrIntent = new Intent();
			atrIntent.setAction("android.intent.action.MAIN");
			atrIntent.putExtra("updateUsbAtr", true);
			atrIntent.putExtra(
					"ATR",
					logLine.substring(logLine.lastIndexOf(": ") + 2,
							logLine.length() - 1));
			appContext.sendBroadcast(atrIntent);
		}
		if (logLine.contains("Vendor/Product")) {
			hasUsbReader = true;
			Intent pathIntent = new Intent();
			pathIntent.setAction("android.intent.action.MAIN");
			pathIntent.putExtra("updateUsbReader", true);
			pathIntent.putExtra(
					"reader",
					logLine.substring(logLine.lastIndexOf("(") + 1,
							logLine.lastIndexOf(")")));
			appContext.sendBroadcast(pathIntent);
		}
		if (logLine.contains("Card Removed") && hasSuperUser
				&& !logLine.contains("Mobile Security Card") && hasUsbReader) {
			Intent atrIntent = new Intent();
			atrIntent.setAction("android.intent.action.MAIN");
			atrIntent.putExtra("updateUsbAtr", true);
			atrIntent.putExtra("ATR", "-");
			appContext.sendBroadcast(atrIntent);
		}
		if (logLine.contains("Unloading reader driver")) {
			hasUsbReader = false;
			Intent pathIntent = new Intent();
			pathIntent.setAction("android.intent.action.MAIN");
			pathIntent.putExtra("updateUsbReader", true);
			pathIntent.putExtra("reader", "-");
			appContext.sendBroadcast(pathIntent);
			
			Intent atrIntent = new Intent();
			atrIntent.setAction("android.intent.action.MAIN");
			atrIntent.putExtra("updateUsbAtr", true);
			atrIntent.putExtra("ATR", "-");
			appContext.sendBroadcast(atrIntent);
		}
		Intent logIntent = new Intent();
		logIntent.setAction("android.intent.action.MAIN");
		logIntent.putExtra("updateLog", true);

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(appContext);
		if (settings.getInt("logSettings", 1002) == 1001) {

		} else if (settings.getInt("logSettings", 1002) == 1002) {
			logIntent.putExtra("logline", logLine);
			appContext.sendBroadcast(logIntent);
		} else if (settings.getInt("logSettings", 1002) == 1003) {
			if (!logLine.startsWith("APDU") && !logLine.startsWith("SW")) {
				logIntent.putExtra("logline", logLine);
				appContext.sendBroadcast(logIntent);
			}
		} else if (settings.getInt("logSettings", 1002) == 1004) {
			if (logLine.startsWith("Card ATR") || logLine.startsWith("APDU")
					|| logLine.startsWith("SW")) {
				logIntent.putExtra("logline", logLine);
				appContext.sendBroadcast(logIntent);
			}
		}
		lastLogLineNotMSCInsert = false;
		if (!logLine.contains("Mobile Security Card")) {
			lastLogLineNotMSCInsert = true;
		}

	}

	public ArrayList<String> getLogLines() {
		return this.logLines;
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

}
