/*
 * Copyright 2010 Gauthier Van Damme for COSIC
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *	http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package be.cosic.android.eid.gui;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PinQuery extends Activity {
	
	
	private EditText pin_entry;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pinquery);
        
        final Intent resultIntent = new Intent();
        pin_entry = (EditText) findViewById(R.id.pin_entry);
        
        TextView text = (TextView) findViewById(R.id.your_pin);
        	
        int value = 0;
        Bundle extras = getIntent().getExtras(); 
        if(extras !=null)
        {
        value = extras.getInt("requestCode");
        }

       
        text.setText(getResources().getStringArray(R.array.your_pin)[value]);
        	
        
        
        final Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	
                
            	resultIntent.putExtra("PIN", pin_entry.getText().toString());
            	
            	setResult(RESULT_OK, resultIntent);
            	
            	finish();
            	
            }
        });
        
        final Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
            	finish();
            	
            }
        });
        
	}
}



