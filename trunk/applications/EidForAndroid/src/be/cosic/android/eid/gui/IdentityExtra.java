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
import android.os.Bundle;
import android.widget.TextView;

public class IdentityExtra extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identityextra);
    }
    
    //Note: onResume is also called after onCreate! Do not duplicate code.
    public void onResume() {
        
        super.onResume();
        try{
        	setEidData();
        	//TextView firstnames = (TextView) findViewById(R.id.firstnames);
        	//firstnames.setText("jaja, nu wel ja");
        	
        }catch(Exception e){
        	//do nothing:
        }
        
    }
    
    public void setEidData() throws Exception{
    	//TextView firstnames = (TextView) findViewById(R.id.firstnames);
        
    	((TextView) findViewById(R.id.idnumber)).setText(MainActivity.belpic.identityInfo.get("National Number"));
    	((TextView) findViewById(R.id.issueplace)).setText(MainActivity.belpic.identityInfo.get("Card delivery municipality"));
    	((TextView) findViewById(R.id.title)).setText(MainActivity.belpic.identityInfo.get("Noble condition"));
    	((TextView) findViewById(R.id.street)).setText(MainActivity.belpic.addressInfo.get("Address"));
    	((TextView) findViewById(R.id.zipcode)).setText(MainActivity.belpic.addressInfo.get("Zip code"));
    	((TextView) findViewById(R.id.commune)).setText(MainActivity.belpic.addressInfo.get("Municipality"));
    	((TextView) findViewById(R.id.cardnumber)).setText(MainActivity.belpic.identityInfo.get("Card Number"));
    	((TextView) findViewById(R.id.chipnumber)).setText(MainActivity.belpic.identityInfo.get("Chip Number"));
    	
    	if(MainActivity.belpic.identityInfo.get("Card validity start date") != null)
    		((TextView) findViewById(R.id.validity)).setText(MainActivity.belpic.identityInfo.get("Card validity start date") + " - " + MainActivity.belpic.identityInfo.get("Card validity end date"));
    	else ((TextView) findViewById(R.id.validity)).setText("");
    	
    	if(MainActivity.belpic.addressInfo.get("Zip code") == null)
    		((TextView) findViewById(R.id.country)).setText("");
    	else if(((String) (MainActivity.belpic.addressInfo.get("Zip code"))).length() == 4)
    		((TextView) findViewById(R.id.country)).setText("Belgium");
    	else ((TextView) findViewById(R.id.country)).setText("Unknown");
    	
    	if(MainActivity.belpic.identityInfo.get("Special status").equals("0"))
    		((TextView) findViewById(R.id.status)).setText("");
    	else ((TextView) findViewById(R.id.status)).setText(MainActivity.belpic.identityInfo.get("Special status"));
    	
    	
    	
    	
    }
}