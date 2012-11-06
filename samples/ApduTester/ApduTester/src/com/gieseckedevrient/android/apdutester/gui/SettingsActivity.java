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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.gieseckedevrient.android.apdutester.DataContainer;
import com.gieseckedevrient.android.apdutester.R;


public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

	private DataContainer dataContainer;

	private EditTextPreference editTestLogFile;
	private ListPreference listPrefCommunicationChannel;
	private CheckBoxPreference checkBoxDelays;
	private CheckBoxPreference checkBoxErrors;
	private CheckBoxPreference checkBoxCase1;
	private CheckBoxPreference checkBoxCase2;
	private CheckBoxPreference checkBoxCase3;
	private CheckBoxPreference checkBoxCase4;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(getResources().getString(R.string.app_name) + " settings");

		addPreferencesFromResource(R.xml.settings);
		PreferenceScreen prefSet = getPreferenceScreen();

		dataContainer = DataContainer.getInstance();
		
		editTestLogFile = (EditTextPreference) prefSet.findPreference("logpath");
		editTestLogFile.setTitle(dataContainer.getLogPath());
		editTestLogFile.setOnPreferenceChangeListener(this);

		listPrefCommunicationChannel = (ListPreference) prefSet.findPreference("communicationChannel");
		listPrefCommunicationChannel.setOnPreferenceChangeListener(this);
		listPrefCommunicationChannel.setEntries(new CharSequence[] { "Basic channel", "Logical Channel"});
		listPrefCommunicationChannel.setEntryValues(new CharSequence[] { "basic", "logical"});
		listPrefCommunicationChannel.setValue((dataContainer.getBasicChannel() == true) ? "basic" : "logical");
		listPrefCommunicationChannel.setTitle((dataContainer.getBasicChannel() == true) ? "Basic channel" : "Logical channel");
		
		checkBoxDelays = (CheckBoxPreference) prefSet.findPreference("delays");
		checkBoxDelays.setChecked(DataContainer.getInstance().getDelays());

		checkBoxErrors = (CheckBoxPreference) prefSet.findPreference("errors");
		checkBoxErrors.setChecked(DataContainer.getInstance().getErrors());

		checkBoxCase1 = (CheckBoxPreference) prefSet.findPreference("case1");
		checkBoxCase1.setChecked(DataContainer.getInstance().getCase1());

		checkBoxCase2 = (CheckBoxPreference) prefSet.findPreference("case2");
		checkBoxCase2.setChecked(DataContainer.getInstance().getCase2());

		checkBoxCase3 = (CheckBoxPreference) prefSet.findPreference("case3");
		checkBoxCase3.setChecked(DataContainer.getInstance().getCase3());

		checkBoxCase4 = (CheckBoxPreference) prefSet.findPreference("case4");
		checkBoxCase4.setChecked(DataContainer.getInstance().getCase4());
		
		setResult(1337, null);
	}

	public boolean onPreferenceChange(Preference pref, Object newValue) {
		if (pref == editTestLogFile) {
			dataContainer.setLogPath(newValue.toString());
			editTestLogFile.setTitle(dataContainer.getLogPath());
			return true;
		} else if (pref == listPrefCommunicationChannel) {
			if (newValue.toString().compareToIgnoreCase("basic") == 0)
				dataContainer.setBasicChannel(true);
			else
				dataContainer.setBasicChannel(false);
			listPrefCommunicationChannel.setTitle((dataContainer.getBasicChannel() == true) ? "Basic channel" : "Logical channel");

			return true;
		}
		return false;
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen prefScreen, Preference pref) {
		if (pref == checkBoxDelays) {
			dataContainer.setDelays(checkBoxDelays.isChecked());
			return true;
		} else if (pref == checkBoxErrors) {
			dataContainer.setErrors(checkBoxErrors.isChecked());
			return true;
		} else if (pref == checkBoxCase1) {
			dataContainer.setCase1(checkBoxCase1.isChecked());
			return true;
		} else if (pref == checkBoxCase2) {
			dataContainer.setCase2(checkBoxCase2.isChecked());
			return true;
		} else if (pref == checkBoxCase3) {
			dataContainer.setCase3(checkBoxCase3.isChecked());
			return true;
		} else if (pref == checkBoxCase4) {
			dataContainer.setCase4(checkBoxCase4.isChecked());
			return true;
		}
		return false;
	}
}
