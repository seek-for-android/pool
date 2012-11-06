/**
 * Copyright 2011 Giesecke & Devrient GmbH.
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
package com.gieseckedevrient.android.apdutester.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.gieseckedevrient.android.apdutester.DataContainer;
import com.gieseckedevrient.android.apdutester.TestPerformer;
import com.gieseckedevrient.android.apdutester.TestResultListener;
import com.gieseckedevrient.android.apdutester.Util;
import com.gieseckedevrient.android.apdutester.R;


public class MainActivity extends Activity implements	TestResultListener {

	TestPerformer performer = new TestPerformer(this, this);

	TextView textView = null;
	ScrollView scrollView = null;
	Button startButton = null;
	Button stopButton = null;
	Spinner spinnerDataLen = null;
	Spinner spinnerLoops = null;
	BufferedWriter logOutput;
	DataContainer dataContainer;
	
	final Handler mHandler = new Handler();
	List<String> message = new ArrayList<String>(0);

	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			scrollView.post(new Runnable() {
				public void run() {
					scrollView.fullScroll(ScrollView.FOCUS_DOWN);
				}
			});
			
			String msg = "";
			synchronized (this) {
				if (message.isEmpty() == false) {
					msg = message.remove(0);
				}
			}
			
			if (msg != null) {
				Log.v(getResources().getString(R.string.app_name), msg);
				textView.append(msg);
				if (logOutput != null) {
					try {
						logOutput.append(msg);
					} catch (IOException e) {
					}
				}
			}
		}
	};

	public void logText(String message) {
		this.message.add(message);
		mHandler.post(mUpdateResults);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.main);

		dataContainer = DataContainer.getInstance();
		SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.app_name), 0);
		dataContainer.setBasicChannel(settings.getBoolean("basicChannel", false));		
		dataContainer.setCase1(settings.getBoolean("case1", true));		
		dataContainer.setCase2(settings.getBoolean("case2", true));		
		dataContainer.setCase3(settings.getBoolean("case3", true));		
		dataContainer.setCase4(settings.getBoolean("case4", true));		
		dataContainer.setDelays(settings.getBoolean("delays", false));		
		dataContainer.setErrors(settings.getBoolean("errors", true));		
		dataContainer.setDataLength(settings.getInt("dataLength", 250));		
		dataContainer.setLoops(settings.getInt("loops", 1));		
		dataContainer.setLogPath(settings.getString("logPath", "/sdcard"));		
		
		ArrayList<String> arrayList_spinnerDataLength = new ArrayList<String>();
		for (int i = 1; i <= 256; i++)
			arrayList_spinnerDataLength.add("Data len: " + i);
		final String[] array_spinnerDataLength = arrayList_spinnerDataLength.toArray(new String[arrayList_spinnerDataLength.size()]);
		ArrayAdapter adapterDataLength = new ArrayAdapter(this, android.R.layout.simple_spinner_item, array_spinnerDataLength);
		spinnerDataLen = (Spinner) findViewById(R.id.spinnerDataLen);
		spinnerDataLen.setAdapter(adapterDataLength);
		spinnerDataLen.setSelection(dataContainer.getDataLength() - 1);
		spinnerDataLen.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				String value = array_spinnerDataLength[position];
				String[] values = value.split("[:]");
				dataContainer.setDataLength(Integer.parseInt(values[1].trim()));
			}
			public void onNothingSelected(AdapterView<?> parentView) {
			}
		});

		ArrayList<String> arrayList_spinnerLoops = new ArrayList<String>();
		int selection = 0;
		for (int i = 0; i <= 16; i++) {
			int value = new Double(Math.pow(2, i)).intValue();
			arrayList_spinnerLoops.add("Loops: " + value);
			if (value == dataContainer.getLoops())
				selection = i;
		}
		if (dataContainer.getLoops() == 0)
			selection = 17;
		arrayList_spinnerLoops.add("Loops: endless");
		final String[] array_spinnerLoops = arrayList_spinnerLoops.toArray(new String[arrayList_spinnerLoops.size()]);
		ArrayAdapter adapterLoops = new ArrayAdapter(this, android.R.layout.simple_spinner_item, array_spinnerLoops);
		spinnerLoops = (Spinner) findViewById(R.id.spinnerLoops);
		spinnerLoops.setAdapter(adapterLoops);
		spinnerLoops.setSelection(selection);
		spinnerLoops.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				String value = array_spinnerLoops[position];
				String[] values = value.split("[:]");
				if (values[1].trim().equalsIgnoreCase("endless") == true)
					dataContainer.setLoops(0);
				else
					dataContainer.setLoops(Integer.parseInt(values[1].trim()));
			}
			public void onNothingSelected(AdapterView<?> parentView) {
			}
		});

		startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				textView.setText("");
				spinnerLoops.setEnabled(false);
				spinnerDataLen.setEnabled(false);
				startButton.setVisibility(Button.INVISIBLE);
				stopButton.setVisibility(Button.VISIBLE);
				performer.runApduCaseTest(MainActivity.this);
			}
		});

		stopButton = (Button) findViewById(R.id.stopButton);
		stopButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				performer.testFinished = true;
			}
		});

		scrollView = (ScrollView) findViewById(R.id.scrollView);
		textView = (TextView) findViewById(R.id.textView);
		
		performer.connectToService();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	        finish();
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		performer.onDestroy();
		
		SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.app_name), 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("basicChannel", dataContainer.getBasicChannel());
		editor.putBoolean("case1", dataContainer.getCase1());
		editor.putBoolean("case2", dataContainer.getCase2());
		editor.putBoolean("case3", dataContainer.getCase3());
		editor.putBoolean("case4", dataContainer.getCase4());
		editor.putBoolean("delays", dataContainer.getDelays());
		editor.putBoolean("errors", dataContainer.getErrors());
		editor.putInt("dataLength", dataContainer.getDataLength());
		editor.putInt("loops", dataContainer.getLoops());
		editor.putString("logPath", dataContainer.getLogPath());
		editor.commit();
		
		super.onDestroy();
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu smReaders = menu.addSubMenu(0, 1000, 0, "Readers");
		try {
			for (int i = 0; i < performer.getReaders().length; i++)
				smReaders.add(0, 1000 + i + 1, 0, performer.getReaders()[i].getName());
		} catch (Exception e) {
		}
		menu.addSubMenu(1, 2000, 0, "Settings");		
		menu.add(0, 3000, 0, "About");
		menu.add(0, 4000, 0, "Exit");
		
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		if (id < 2000) {
			try {
				if (id != 1000 & (id - 1000 - 1) < performer.getReaders().length)
					performer.setReader(performer.getReaders()[id - 1000 - 1]);
			} catch (Exception e) {
			}
		} else  if (id == 2000) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, 1337);
		} else if (id == 3000) {
			Intent intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
		} else if (id == 4000) {
			finish();
		}
		
		uiUpdate();
		
		return true;
	}

	public void uiUpdate() {
		if (startButton != null) {
			try {
				startButton.setText("Start Test ("	+ performer.getReader().getName() + " - " + (performer.isBasicChannel() ? "Basic Channel)" 	: "Logical Channel)"));
			} catch (Exception e) {
				startButton.setText("Start Test");
			}
			startButton.setEnabled(performer.isReady());
		}
		
		if (performer.isReady())
			startButton.setEnabled(true);
		else
			startButton.setEnabled(false);
	}

	public void testFinished() {
		runOnUiThread(new Runnable() {
		    public void run() {
				spinnerLoops.setEnabled(true);
				spinnerDataLen.setEnabled(true);
				startButton.setVisibility(Button.VISIBLE);
				stopButton.setVisibility(Button.INVISIBLE);
		    }
		});
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 1337)
        	uiUpdate();
	}    
	
	public File openNewLogFile() throws IOException {
		File file = null;
		try {
			String path = dataContainer.getLogPath();
			if (path.length() > 0) {
				file = new File(path, "test_log_" + Util.getTimeStamp() + ".txt");
				logOutput = new BufferedWriter(new FileWriter(file));
			}
		} catch (Exception e) {
			file = null;
		}
		return file;
	}

	public boolean allMessagesHandled() {
		return message.isEmpty();
	}

	public void closeLogFile() throws IOException {
		if (logOutput != null)
			logOutput.close();
	}

}
