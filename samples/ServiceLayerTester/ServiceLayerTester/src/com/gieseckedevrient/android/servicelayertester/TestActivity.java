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

import org.simalliance.openmobileapi.SEService;
import org.simalliance.openmobileapi.SEService.CallBack;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gieseckedevrient.android.servicelayertester.testrunners.AuthenticationTestRunner;
import com.gieseckedevrient.android.servicelayertester.testrunners.DiscoveryTestRunner;
import com.gieseckedevrient.android.servicelayertester.testrunners.FileManagementTestRunner;
import com.gieseckedevrient.android.servicelayertester.testrunners.Pkcs15TestRunner;
import com.gieseckedevrient.android.servicelayertester.testrunners.SecureStorageTestRunner;

/**
 * Activity loaded when a specific test is selected.
 * Test execution is launched from this Activity.
*/
public class TestActivity extends Activity implements CallBack {
    /**
     * Id used to identify the different types of API Provider Tests.
     */
    private int providerType;
    /**
     * The SEService to be used.
     */
    private SEService seService;

    /**
     * Button.
     */
    private Button buttonStart;
    /**
     * TextView.
     */
    private TextView textViewLog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        // Init UI elements
        buttonStart = (Button) findViewById(R.id.start_test);
        textViewLog = (TextView) findViewById(R.id.log);
        // Disable button
        buttonStart.setEnabled(false);
        // Get Provider Type
        providerType = getIntent().getIntExtra("providerType", -1);
        // Init seService
        seService = new SEService(getApplicationContext(), this);
        writeLine("Connecting to SmartCardService...");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        seService.shutdown();
    }
    @Override
    public void serviceConnected(SEService arg0) {
        writeLine("Done!");
        buttonStart.setEnabled(true);
    }

    /**
     * Method that calls the thread to start the test execution.
     * It shows a message if a test execution is still running
     *  when it is called.
     */
    public void startTest(View v) {
        textViewLog.setText("");
        buttonStart.setEnabled(false);
        switch (providerType) {
        case ProviderType.DISCOVERY:
            new DiscoveryTestRunner(seService, this).start();
            break;
        case ProviderType.AUTHENTICATION:
            new AuthenticationTestRunner(seService, this).start();
            break;
        case ProviderType.FILE_MANAGEMENT:
            new FileManagementTestRunner(seService, this).start();
            break;
        case ProviderType.SECURE_STORAGE:
            new SecureStorageTestRunner(seService, this).start();
            break;
        case ProviderType.PKCS15:
            new Pkcs15TestRunner(seService, this).start();
            break;
        default:
            return;
        }
    }

    /**
     * Writes a line into the log window.
     *
     * @param newLine The text to be written.
     */
    private void writeLine(final String newLine) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = textViewLog.getText() + newLine + "\n";
                textViewLog.setText(text);
            }
        });
    }

    public void logInfo(String text) {
        writeLine(text);
    }

    public void logError(String text) {
        writeLine("ERROR:");
        writeLine(text);
    }

    /**
     * Executed when the test run ends.
     */
    public void testEnded(final boolean testSucceeded) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (testSucceeded) {
                    writeLine("Test succeeded.");
                } else {
                    writeLine("Test failed.");
                }
                buttonStart.setEnabled(true);
            }
        });
    }
}
