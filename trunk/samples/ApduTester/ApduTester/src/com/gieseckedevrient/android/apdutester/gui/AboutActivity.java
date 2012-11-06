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

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.Session;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.gieseckedevrient.android.apdutester.DataContainer;
import com.gieseckedevrient.android.apdutester.TestPerformer;
import com.gieseckedevrient.android.apdutester.Util;
import com.gieseckedevrient.android.apdutester.R;


public class AboutActivity extends Activity {

	String appletVersion = "";
	
	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
		setContentView(R.layout.about);

		TextView tv = (TextView) this.findViewById(R.id.app_version);

		try {
			tv.setText("version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (Exception e) {
			tv.setText("unknown version");
		}

		tv = (TextView) this.findViewById(R.id.about_info_headline);
		if (tv != null)
			tv.setText(getResources().getString(R.string.app_name) + " test applet");

		// enumerating all secure elements might result in ANR if a specific SE is not responding at all
		// in case it is not properly integrated in the system (e.g. MSC on Crespo or eSE on G1, ...)
		// -> put retrieval of applet version is separate thread that will get killed after max. 5 seconds
		appletVersion = "";
		Thread thread = new Thread() {
			public void run() {
				try {
					for (Reader reader: TestPerformer.seService.getReaders()) {
						try {
							appletVersion += " " + Util.fill(reader.getName().split(":")[0], 5);
						} catch (Exception e) {
							appletVersion += reader.getName();
						}
						
						if (reader.isSecureElementPresent()) {
							Session session;
							try {
								session = reader.openSession();
							} catch (Exception e) {
						        appletVersion += "no session available\n";
						        continue;
							}
	
							Channel channel;
							try {
								channel = session.openLogicalChannel(DataContainer.APPLET_AID);
							} catch (Exception e) {
						        appletVersion += "applet not installed\n";
						        continue;
							}
							
							try {
								byte[] rspApdu = channel.transmit(new byte[] { 0x00, 0x76, 0x00, 0x00, 0x02 });
								if (rspApdu == null || rspApdu.length != 4) {
							        appletVersion += "applet installed, no version\n";
								} else {
									appletVersion += "version " + String.valueOf(rspApdu[0]) + "." + String.valueOf(rspApdu[1]) + "\n";
								}
							} catch (Exception e) {
								appletVersion += "applet installed, no response\n";
							}
						} else {
					        appletVersion += "no Secure Element present\n";		
						}
					}
				} catch (Exception e) {
				}
			}
		};
		
		try {
			thread.start();
			thread.join(5000);
		} catch (InterruptedException e) {
		}
		
		if (thread.isAlive())
			thread.destroy();
		
		tv = (TextView) this.findViewById(R.id.app_info);
		if (tv != null)
			tv.setText(appletVersion);
	}
}
