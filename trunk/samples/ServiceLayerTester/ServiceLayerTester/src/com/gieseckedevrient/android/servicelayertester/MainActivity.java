/*
 * Copyright 2013 Giesecke & Devrient GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.gieseckedevrient.android.servicelayertester;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

/**
*
* Main activity of the Application.
*/
public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /**
    *
    * Method called when Discovery Provider API button is pressed.
    *
    * @param view View responsible of this event.
    */
   public void onClickDiscovery(View view) {
       Intent i = new Intent(this, TestActivity.class);
       i.putExtra("providerType", ProviderType.DISCOVERY);
       startActivity(i);
   }

   /**
   *
   * Method called when Secure Storage Provider API button is pressed.
   *
   * @param view View responsible of this event.
   */
   public void onClickSecureStorageProvider(View view) {
       Intent i = new Intent(this, TestActivity.class);
       i.putExtra("providerType", ProviderType.SECURE_STORAGE);
       startActivity(i);
   }

   /**
   *
   * Method called when PKCS15 Provider API button is pressed.
   *
   * @param view View responsible of this event.
   */
   public void onClickPKCS15(View view) {
       Intent i = new Intent(this, TestActivity.class);
       i.putExtra("providerType", ProviderType.PKCS15);
       startActivity(i);
   }

   /**
   *
   * Method called when File View Provider API button is pressed.
   *
   * @param view View responsible of this event.
   */
   public void onClickFileView(View view) {
       Intent i = new Intent(this, TestActivity.class);
       i.putExtra("providerType", ProviderType.FILE_MANAGEMENT);
       startActivity(i);
   }

   /**
   *
   * Method called when Authentication API button is pressed.
   *
   * @param view View responsible of this event.
   */
   public void onClickAuthentication(View view) {
       Intent i = new Intent(this, TestActivity.class);
       i.putExtra("providerType", ProviderType.AUTHENTICATION);
       startActivity(i);
   }
}
