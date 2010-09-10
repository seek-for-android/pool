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

package com.gieseckedevrient.android.otpauthenticator;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;

public class AboutActivity extends Activity {

	public boolean onTouchEvent(MotionEvent event) {
		((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
		finish();
		return true;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		finish();
		return true;
	}

	protected void onCreate(Bundle savedInstanceState) {
		// Initialize the layout
		super.onCreate(savedInstanceState);

		// remove title
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
		setContentView(R.layout.about);

		// Set the application version
		TextView tv = (TextView) this.findViewById(R.id.app_version);

		try {
			tv.setText("version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (Exception e) {
			tv.setText("unknown version");
		}
	}
}
