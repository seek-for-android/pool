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
package com.gieseckedevrient.android.pcscdroid;

import java.io.IOException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;

	private PcscDaemon pcscd;
	private static Context appContext;

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		appContext = getApplicationContext();

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);

		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {

			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {

		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = null;
			if (i == 0)
				fragment = new ServiceStatusFragment();
			else if (i == 1)
				fragment = new LogFragment();
			else if (i == 2)
				fragment = new SettingsFragment();

			return fragment;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_servicestatus).toUpperCase();
			case 1:
				return getString(R.string.title_log).toUpperCase();
			case 2:
				return getString(R.string.title_settings).toUpperCase();
			}
			return null;
		}
	}

	public static class DummySectionFragment extends Fragment {
		public DummySectionFragment() {
		}

		public static final String ARG_SECTION_NUMBER = "section_number";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			TextView textView = new TextView(getActivity());
			textView.setGravity(Gravity.CENTER);
			Bundle args = getArguments();
			textView.setText(Integer.toString(args.getInt(ARG_SECTION_NUMBER)));
			return textView;
		}
	}

	public static class ServiceStatusFragment extends Fragment {
		LinearLayout linear;
		boolean running;
		public PcscService mBoundService;
		private BroadcastReceiver serviceConnection;
		private ServiceConnection mConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className,
					IBinder service) {

				mBoundService = ((PcscService.LocalBinder) service)
						.getService();

			}

			public void onServiceDisconnected(ComponentName className) {

				mBoundService = null;

			}
		};
		private TextView runningVarLabel;
		private TextView readerVarLabel;
		private TextView atrVarLabel;
		private TextView usbReaderVarLabel;
		private TextView usbAtrVarLabel;
		private ToggleButton toggleService;

		@Override
		public void onSaveInstanceState(Bundle savedInstanceState) {
			super.onSaveInstanceState(savedInstanceState);

			savedInstanceState.putBoolean("serviceRunning", running);
			savedInstanceState.putString("reader", readerVarLabel.getText()
					.toString());
			savedInstanceState.putString("atr", atrVarLabel.getText()
					.toString());

		}

		public void startPCSC() throws IOException {

			appContext.bindService(new Intent(MainActivity.appContext,
					PcscService.class), mConnection, Context.BIND_AUTO_CREATE);

		}

		public void stopPCSC() {

			appContext.unbindService(mConnection);

		}

		public ServiceStatusFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			serviceConnection = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();

					if (intent.hasExtra("updateReader")
							&& (Boolean) intent.getExtras().get("updateReader")) {
						readerVarLabel.setText((String) intent.getExtras().get(
								"reader"));
					} else if (intent.hasExtra("updateAtr")
							&& (Boolean) intent.getExtras().get("updateAtr")) {
						atrVarLabel.setText((String) intent.getExtras().get(
								"ATR"));
					} else if (intent.hasExtra("updateUsbReader")
							&& (Boolean) intent.getExtras().get("updateUsbReader")) {
						usbReaderVarLabel.setText((String) intent.getExtras().get(
								"reader"));
					} else if (intent.hasExtra("updateUsbAtr")
							&& (Boolean) intent.getExtras().get("updateUsbAtr")) {
						usbAtrVarLabel.setText((String) intent.getExtras().get(
								"ATR"));
					} else if (intent.hasExtra("updateStatus")
							&& (Boolean) intent.getExtras().get("updateStatus")) {
						boolean status = (Boolean) intent.getExtras().get(
								"status");
						if (status == true) {
							running = true;
							toggleService.setEnabled(true);
							runningVarLabel.setText("yes");
							runningVarLabel.setTextColor(0xff00ff00); // green
							try {
								Thread.sleep(1000);

							} catch (InterruptedException e) {
								Log.v("pcscdroid",
										"Error waiting for Service to start");
							}
						} else {
							runningVarLabel.setText("no");
							runningVarLabel.setTextColor(0xffff0000); // red
							readerVarLabel.setText("-");
							atrVarLabel.setText("-");
							usbReaderVarLabel.setText("-");
							usbAtrVarLabel.setText("-");
							toggleService.setEnabled(true);
						}
					}

				}
			};
			IntentFilter filter = new IntentFilter("android.intent.action.MAIN");

			ServiceStatusFragment.this.getActivity().registerReceiver(
					serviceConnection, filter);

			ScrollView stScroll = new ScrollView(getActivity());

			linear = new LinearLayout(getActivity());

			linear.setOrientation(LinearLayout.VERTICAL);
			linear.setPadding(15, 15, 15, 15);

			TextView serviceStatusHeader = new TextView(getActivity());
			serviceStatusHeader.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			serviceStatusHeader.setPadding(5, 30, 0, 30);

			serviceStatusHeader.setText("PcscDroid");
			serviceStatusHeader.setTextSize(25);

			LinearLayout pcscdLine = new LinearLayout(getActivity());
			pcscdLine.setOrientation(LinearLayout.HORIZONTAL);

			toggleService = new ToggleButton(getActivity());
			toggleService.setChecked(false);
			toggleService.setText("Stopped");
			toggleService.setTop(-50);

			toggleService.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View toggleService) {
					ToggleButton button = (ToggleButton) toggleService;
					if (button.isChecked()) {
						try {
							PcscInstaller installer = new PcscInstaller(
									appContext);
							installer.install();
							if (installer.isPresent() || true) {
								startPCSC();
								button.setText("Started");
								button.setEnabled(false);
							} else {

								button.setChecked(false);
								button.setText("Stopped");
								Toast.makeText(appContext,
										"No Card is Present",
										Toast.LENGTH_SHORT).show();
							}

						} catch (IOException e) {
							Log.v("pcscdroid", "Error starting PCSCd");
						}

					} else {
						button.setText("Stopped");
						stopPCSC();
						button.setEnabled(false);
					}
				}
			});

			TextView pcscdText = new TextView(getActivity());

			pcscdText.setPadding(5, 10, 150, 10);

			pcscdText.setText("Pcscd");
			pcscdText.setTextSize(18);

			pcscdLine.addView(pcscdText);
			pcscdLine.addView(toggleService);

			TextView statusLabel = new TextView(getActivity());
			statusLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			statusLabel.setPadding(5, 40, 0, 20);

			statusLabel.setText("Status");
			statusLabel.setTextSize(22);

			LinearLayout runningLine = new LinearLayout(getActivity());
			runningLine.setOrientation(LinearLayout.HORIZONTAL);

			TextView runningLabel = new TextView(getActivity());
			runningLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			runningLabel.setPadding(5, 20, 0, 30);

			runningLabel.setText("Running: ");
			runningLabel.setTextSize(18);

			runningVarLabel = new TextView(getActivity());
			runningVarLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			runningVarLabel.setPadding(20, 20, 0, 30);

			runningVarLabel.setText("no");
			runningVarLabel.setTextColor(0xffff0000); // RED
			runningVarLabel.setTextSize(18);

			runningLine.addView(runningLabel);
			runningLine.addView(runningVarLabel);

			LinearLayout readerLine = new LinearLayout(getActivity());
			readerLine.setOrientation(LinearLayout.HORIZONTAL);

			TextView readerLabel = new TextView(getActivity());
			readerLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			readerLabel.setPadding(5, 20, 0, 30);

			readerLabel.setText("MSC-Reader: ");
			readerLabel.setTextSize(16);

			readerVarLabel = new TextView(getActivity());
			readerVarLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			readerVarLabel.setPadding(20, 20, 0, 30);

			readerVarLabel.setText("-");

			readerVarLabel.setTextSize(16);

			readerLine.addView(readerLabel);
			readerLine.addView(readerVarLabel);

			LinearLayout atrLine = new LinearLayout(getActivity());
			atrLine.setOrientation(LinearLayout.HORIZONTAL);

			TextView atrLabel = new TextView(getActivity());
			atrLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			atrLabel.setPadding(5, 20, 0, 30);

			atrLabel.setText("MSC-ATR: ");
			atrLabel.setTextSize(16);

			atrVarLabel = new TextView(getActivity());
			atrVarLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			atrVarLabel.setPadding(20, 20, 0, 30);

			atrVarLabel.setText("-");

			atrVarLabel.setTextSize(14);

			atrLine.addView(atrLabel);
			atrLine.addView(atrVarLabel);
			
			LinearLayout usbReaderLine = new LinearLayout(getActivity());
			usbReaderLine.setOrientation(LinearLayout.HORIZONTAL);

			TextView usbReaderLabel = new TextView(getActivity());
			usbReaderLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			usbReaderLabel.setPadding(5, 20, 0, 30);

			usbReaderLabel.setText("USB-Reader: ");
			usbReaderLabel.setTextSize(16);

			usbReaderVarLabel = new TextView(getActivity());
			usbReaderVarLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			usbReaderVarLabel.setPadding(20, 20, 0, 30);

			usbReaderVarLabel.setText("-");

			usbReaderVarLabel.setTextSize(14);

			usbReaderLine.addView(usbReaderLabel);
			usbReaderLine.addView(usbReaderVarLabel);
			
			LinearLayout usbAtrLine = new LinearLayout(getActivity());
			usbAtrLine.setOrientation(LinearLayout.HORIZONTAL);

			TextView usbAtrLabel = new TextView(getActivity());
			usbAtrLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			usbAtrLabel.setPadding(5, 20, 0, 30);

			usbAtrLabel.setText("USB ATR: ");
			usbAtrLabel.setTextSize(16);

			usbAtrVarLabel = new TextView(getActivity());
			usbAtrVarLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			usbAtrVarLabel.setPadding(20, 20, 0, 30);

			usbAtrVarLabel.setText("-");

			usbAtrVarLabel.setTextSize(14);

			usbAtrLine.addView(usbAtrLabel);
			usbAtrLine.addView(usbAtrVarLabel);
			

			linear.addView(serviceStatusHeader);
			linear.addView(pcscdLine);
			linear.addView(statusLabel);
			linear.addView(runningLine);
			linear.addView(readerLine);
			linear.addView(atrLine);
			linear.addView(usbReaderLine);
			linear.addView(usbAtrLine);

			stScroll.addView(linear);
			return stScroll;
		}
	}

	public static class LogFragment extends Fragment {
		LinearLayout linear;
		TextView logTextView;
		private BroadcastReceiver serviceConnection;

		public LogFragment() {
		}

		public void appendLog(String logLine) {

			logTextView.append(logLine + "\n");
			final int scrollAmount = logTextView.getLayout().getLineTop(
					logTextView.getLineCount())
					- logTextView.getHeight();

			if (scrollAmount > 0)
				logTextView.scrollTo(0, scrollAmount);
			else
				logTextView.scrollTo(0, 0);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			serviceConnection = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();

					if (intent.hasExtra("updateLog")
							&& (Boolean) intent.getExtras().get("updateLog")) {
						appendLog((String) intent.getExtras().get("logline"));

					} else if (intent.hasExtra("clearLog")) {
						logTextView.setText("----");

					}

				}
			};
			IntentFilter filter = new IntentFilter("android.intent.action.MAIN");
			LogFragment.this.getActivity().registerReceiver(serviceConnection,
					filter);

			linear = new LinearLayout(getActivity());

			linear.setOrientation(LinearLayout.VERTICAL);
			linear.setPadding(15, 15, 15, 15);
			TextView logHeader = new TextView(getActivity());
			logHeader.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			logHeader.setPadding(5, 30, 0, 30);

			logHeader.setText("Log");
			logHeader.setTextSize(25);

			logTextView = new TextView(getActivity());
			logTextView.setLayoutParams(new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			logTextView.setPadding(10, 10, 10, 30);

			logTextView.setText("----\n");
			logTextView.setMovementMethod(new ScrollingMovementMethod());
			logTextView.setTextSize(14);
			logTextView.setBackgroundColor(0xffcccccc); // gray

			linear.addView(logHeader);
			linear.addView(logTextView);

			return linear;
		}
	}

	public static class SettingsFragment extends Fragment {
		LinearLayout linear;

		public SettingsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			ScrollView stScroll = new ScrollView(getActivity());

			linear = new LinearLayout(getActivity());

			linear.setOrientation(LinearLayout.VERTICAL);
			linear.setPadding(15, 15, 15, 15);

			TextView settingsHeader = new TextView(getActivity());
			settingsHeader.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			settingsHeader.setPadding(5, 30, 0, 30);

			settingsHeader.setText("Settings");
			settingsHeader.setTextSize(25);

			TextView logLabel = new TextView(getActivity());
			logLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			logLabel.setPadding(5, 40, 0, 20);

			logLabel.setText("Log");
			logLabel.setTextSize(22);

			final RadioGroup logRadioGroup = new RadioGroup(getActivity());

			RadioButton apduonlyOption = new RadioButton(getActivity());
			apduonlyOption.setText("APDU Only");
			RadioButton debugOption = new RadioButton(getActivity());
			debugOption.setText("Debug");
			RadioButton debugapduOption = new RadioButton(getActivity());
			debugapduOption.setText("Debug + APDU");
			RadioButton standard = new RadioButton(getActivity());
			standard.setText("None");

			logRadioGroup.addView(apduonlyOption);
			logRadioGroup.addView(debugOption);
			logRadioGroup.addView(debugapduOption);
			logRadioGroup.addView(standard);

			OnClickListener radioCL = new OnClickListener() {

				@Override
				public void onClick(View v) {

					SharedPreferences.Editor editor = PreferenceManager
							.getDefaultSharedPreferences(appContext).edit();
					editor.putInt("logSettings",
							logRadioGroup.getCheckedRadioButtonId());
					editor.commit();
					SharedPreferences settings = PreferenceManager
							.getDefaultSharedPreferences(appContext);
					Log.v("pcscdroid",
							"logsettings:"
									+ settings.getInt("logSettings", 1001));
				}
			};
			apduonlyOption.setId(1004);
			apduonlyOption.setOnClickListener(radioCL);
			debugOption.setId(1003);
			debugOption.setOnClickListener(radioCL);
			debugapduOption.setId(1002);
			debugapduOption.setOnClickListener(radioCL);
			standard.setId(1001);
			standard.setOnClickListener(radioCL);

			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(appContext);
			logRadioGroup.check(settings.getInt("logSettings",
					debugapduOption.getId()));
			Log.v("pcscdroid",
					"loaded: "
							+ settings.getInt("logSettings",
									debugapduOption.getId()));

			TextView installLabel = new TextView(getActivity());
			installLabel.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			installLabel.setPadding(5, 40, 0, 20);

			installLabel.setText("Misc");
			installLabel.setTextSize(22);

			Button clearLog = new Button(getActivity());
			clearLog.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			clearLog.setText("Clear Log");
			clearLog.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					Intent clearLogIntent = new Intent();
					clearLogIntent.setAction("android.intent.action.MAIN");
					clearLogIntent.putExtra("clearLog", true);
					appContext.sendBroadcast(clearLogIntent);

				}
			});

			Button unInstallButton = new Button(getActivity());
			unInstallButton.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			unInstallButton.setText("Remove Pcscd");
			unInstallButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					new PcscInstaller(appContext).remove();
				}
			});

			linear.addView(settingsHeader);
			linear.addView(logLabel);
			linear.addView(logRadioGroup);
			linear.addView(installLabel);
			linear.addView(clearLog);
			linear.addView(unInstallButton);

			stScroll.addView(linear);
			return stScroll;
		}
	}
}
