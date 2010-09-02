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




import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.smartcard.CardException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import be.cosic.android.eid.exceptions.InvalidPinException;
import be.cosic.android.eid.exceptions.InvalidResponse;
import be.cosic.android.util.TextUtils;

public class Functions extends Activity {
	
	private Intent intent;
	
	private String old_pin;
	private String new_pin_1;
	private String new_pin_2;
	
	static final int GET_PIN_FOR_TEST_REQUEST = 0;
	static final int GET_PIN_FOR_CHANGE_REQUEST_1 = 1;
	static final int GET_PIN_FOR_CHANGE_REQUEST_2 = 2;
	static final int GET_PIN_FOR_CHANGE_REQUEST_3 = 3;
	static final int GET_PIN_FOR_SIGN_REQUEST = 4;
	static final int GET_RAW_FILE_LOCATION_REQUEST = 5;
	static final int GET_SIGNED_FILE_LOCATION_REQUEST = 6;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        //intent = new Intent().setClass(this, PathQuery.class);
    }
    
    
  //Note: onResume is also called after onCreate! Do not duplicate code.
    public void onResume() {
        
        super.onResume();
        if(MainActivity.own_id == true){
        	setContentView(R.layout.own_functions);
       
        	intent = new Intent().setClass(this, PinQuery.class);
        	
	        final Button test_pin = (Button) findViewById(R.id.test_pin);
	        
	        test_pin.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
		            
	            	//start new activity
	            	intent.putExtra("requestCode", GET_PIN_FOR_TEST_REQUEST);
	            	Functions.this.startActivityForResult(intent, GET_PIN_FOR_TEST_REQUEST);
	            	
	            	//get the pin from the input and check it: see on activity result method
		            	
		            	
	            }
	        });
	        
	        
	        final Button change_pin = (Button) findViewById(R.id.change_pin);
	        
	        change_pin.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                
	            	
	            	
	            	//get old pin
	            	intent.putExtra("requestCode", GET_PIN_FOR_CHANGE_REQUEST_1);
	            	Functions.this.startActivityForResult(intent, GET_PIN_FOR_CHANGE_REQUEST_1);
	            	
	            	
	            }
	        });
	        
	        final Button sign = (Button) findViewById(R.id.sign_data);
	        
	        sign.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	              
	            	//Get the PIN to enable signing
	            	
	            	intent.putExtra("requestCode", GET_PIN_FOR_SIGN_REQUEST);
	            	Functions.this.startActivityForResult(intent, GET_PIN_FOR_SIGN_REQUEST);
	            	
	            	
	            }
	        });
	        
	        final Button authenticate = (Button) findViewById(R.id.authenticate);
	        
	        authenticate.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                
	            	//TODO: need to use SSL for client authentication 
	            	//also: does eID card implement the PKCS11 standard? used for client authentication
	            	//and do we use PKCS11 on both application side (this side) and token side (smart card)?
	            	
	            	
	            	Context context = getApplicationContext();
	            	int duration = Toast.LENGTH_LONG;
	            	Toast toast;
	            	
	            	toast = Toast.makeText(context, "Function not implemented yet.", duration);
	    			toast.setGravity(Gravity.CENTER, 0, 0);
	    			toast.show();
	            	
	            }
	        });
	        
	        
	        try {
				setEidData();//TODO : dit niet in een specifieke functie zetten?
			} catch (CardException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //TextView firstnames = (TextView) findViewById(R.id.firstnames);
	        //firstnames.setText("jaja, nu wel ja");
	        	
	        
        
        }else {
        	setContentView(R.layout.external_functions);
        	
        	
        	
        	final Button verify = (Button) findViewById(R.id.verify);
	        
        	intent = new Intent().setClass(this, PathQuery.class);
        	
        	verify.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
		            
	            	//start new activity
	            	
	            	Functions.this.startActivityForResult(intent, GET_SIGNED_FILE_LOCATION_REQUEST);
	            	
	            	//get the pin from the input and check it: see on activity result method
		            	
		            	
	            }
	        });
        }
        
        
        
    }
    
    
  //Called when a child activity returns.
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
    	
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_LONG;
    	Toast toast;
    	
    	
    	
    	switch (requestCode){
    	
    	//If the return value is a PIN for testing:
    	case GET_PIN_FOR_TEST_REQUEST:
    	
            if (resultCode == RESULT_OK) {
               
            	try{   
	            	
	            	//validate pin
	    			MainActivity.belpic.pinValidationEngine(data.getStringExtra("PIN"));
	    			
	    			
	    			CharSequence text = "PIN ok";
	    			toast = Toast.makeText(context, text, duration);
	    			toast.setGravity(Gravity.CENTER, 0, 0);
	    			toast.show();
	    			
            	} catch (InvalidPinException e) {
	    			
            		CharSequence text = "Invalid PIN: "+ e.getMessage() + " tries left.";
	    			toast = Toast.makeText(context, text, duration);
	    			toast.setGravity(Gravity.CENTER, 0, 0);
	    			toast.show();
            		
	    			Log.e(MainActivity.LOG_TAG, "Exception in PIN validation: " + e.getMessage());
	            } catch (CardException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
            }else ;
            
            break;
            
        //If the return value is a PIN for changing:
    	case GET_PIN_FOR_CHANGE_REQUEST_1:
    	
            if (resultCode == RESULT_OK) {
               
            	
	            	
            		old_pin = data.getStringExtra("PIN");
            		
            		
            		//get new pin
            		intent.putExtra("requestCode", GET_PIN_FOR_CHANGE_REQUEST_2);
                	Functions.this.startActivityForResult(intent, GET_PIN_FOR_CHANGE_REQUEST_2);
                	
	    			
            	
            }else ;
            
            
            break;
        
          //If the return value is a PIN for changing:
    	case GET_PIN_FOR_CHANGE_REQUEST_2:
    	
            if (resultCode == RESULT_OK) {
               
            	
	            	
            		new_pin_1 = data.getStringExtra("PIN");
            		
            		//get new pin
            		intent.putExtra("requestCode", GET_PIN_FOR_CHANGE_REQUEST_3);
                	Functions.this.startActivityForResult(intent, GET_PIN_FOR_CHANGE_REQUEST_3);
                	
                	//get the pin from the input and change it: see on activity result method
                	
                	
	    			
            	
            }else ;
        
            break;
            
          //If the return value is a PIN for changing:
    	case GET_PIN_FOR_CHANGE_REQUEST_3:
    	
            if (resultCode == RESULT_OK) {
               
            	try{   
	            	
            		new_pin_2 = data.getStringExtra("PIN");
            		
            		if(!new_pin_1.equals(new_pin_2) || new_pin_1.length() != 4){
            			
            			CharSequence text = "New PIN incorrect";
    	    			toast = Toast.makeText(context, text, duration);
    	    			toast.setGravity(Gravity.CENTER, 0, 0);
    	    			toast.show();
            			
            			break;
            		}
                	//get the pin from the input and change it: see on activity result method
            		MainActivity.belpic.changeThisPin(old_pin, new_pin_1);
            		
            		CharSequence text = "PIN changed";
	    			toast = Toast.makeText(context, text, duration);
	    			toast.setGravity(Gravity.CENTER, 0, 0);
	    			toast.show();
                	
	    			
            	} catch (InvalidPinException e) {
            		CharSequence text = "Invalid old PIN: "+ e.getMessage() + " tries left.";
	    			toast = Toast.makeText(context, text, duration);
	    			toast.setGravity(Gravity.CENTER, 0, 0);
	    			toast.show();
	    			
	    			Log.e(MainActivity.LOG_TAG, "Exception in PIN validation: " + e.getMessage());
	            } catch (CardException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
            }else ;
            
            break;
        
    	case GET_SIGNED_FILE_LOCATION_REQUEST:
        	
            if (resultCode == RESULT_OK) {
               
            	
            	String[] files = data.getStringExtra("path").split(File.separator);
            	String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
            	String path = dir + File.separator + data.getStringExtra("path");
            	
            	
            	//Get the directory path
            	for(int i =0;i<(files.length-1);i++){
					dir = dir + File.separator + files[i] ;
				}
            	
          	
				try {
					
					//TODO !!!!!!!!!!!!!!!!!
					
					
					//Check if an extension was added. If not or a false one, correct.
					//Not everything is checked but other things should be checked by OS
					
					
//					
					if(!path.endsWith(".crt"))
	            		path=path + ".crt";
	            	else if(!path.endsWith(".crt"))
	            			throw new UnsupportedEncodingException();
					
//					//We make new directories where necessary
//					new File(dir).mkdirs();
//					
//					//Now we store the file					
//					//openFileOutput can not contain path separators in its name!!!!!!!
//					//FileOutputStream fos = openFileOutput(path, Context.MODE_WORLD_READABLE);
//					FileOutputStream fos = new FileOutputStream(path);
//					fos.write(currentCert.getEncoded());
//	        		fos.close();
				} catch (IOException e) {
					//TODO
					
					e.printStackTrace();
				}
            	
            	
            	
            }else ;//Do nothing
            
            break;
            
    	case GET_PIN_FOR_SIGN_REQUEST:
        	
            if (resultCode == RESULT_OK) {
               
            	try{   
	            	
	            	//Check pin for signing
	    			MainActivity.belpic.pinValidationEngine(data.getStringExtra("PIN"));
            		
	    			
	    			//Ask the path of the file to be signed
	    			intent = new Intent().setClass(this, PathQuery.class);
	    			Functions.this.startActivityForResult(intent, GET_RAW_FILE_LOCATION_REQUEST);
	            	
	    			
            	} catch (InvalidPinException e) {
	    			
            		CharSequence text = "Invalid PIN: "+ e.getMessage() + " tries left.";
	    			toast = Toast.makeText(context, text, duration);
	    			toast.setGravity(Gravity.CENTER, 0, 0);
	    			toast.show();
            		
	    			Log.e(MainActivity.LOG_TAG, "Exception in PIN validation: " + e.getMessage());
	            } catch (CardException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
            	
            	
            	
            	
            }else ;//Do nothing
            
            break;
            
    	case GET_RAW_FILE_LOCATION_REQUEST:
        	
            if (resultCode == RESULT_OK) {
               
            	
            	String[] files = data.getStringExtra("path").split(File.separator);
            	String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
            	String path = dir + File.separator + data.getStringExtra("path");
            	
            	
            	//Get the directory path
            	for(int i =0;i<(files.length-1);i++){
					dir = dir + File.separator + files[i] ;
				}
            	
          	
				try {
					
					//TODO Make an xml signature??? and imbed reference to document/in document/...
					//For now, just a signature is created and stored under 'filename_signature.sign'
					
					//Encode the file into a byte array
					byte[] encodedData = TextUtils.getBytesFromFile(path);
					
					//Calculate hash 
					MessageDigest hash = MessageDigest.getInstance("SHA-1");
					byte[] hashValue = hash.digest(encodedData);
					
					//Calculate the signature inside the eID card
					MainActivity.belpic.generateNonRepudiationSignature(hashValue);
					
					
//					//We make new directories where necessary
//					new File(dir).mkdirs();
//					
//					//Now we store the file					
//					//openFileOutput can not contain path separators in its name!!!!!!!
//					//FileOutputStream fos = openFileOutput(path, Context.MODE_WORLD_READABLE);
//					FileOutputStream fos = new FileOutputStream(path);
//					fos.write(currentCert.getEncoded());
//	        		fos.close();
					
					
					//If everything went fine, let the user know the signature was stored under 'filename_signature.sign'
					
				} catch (IOException e) {
					//TODO
					
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidResponse e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CardException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            	
            	
            }else ;//Do nothing
            
            break;
            
    	default:
    		Log.e(MainActivity.LOG_TAG, "Problem in PINquery return result: Invalid return request code.");
        }
    }
    
    
    
    
    
    public void setEidData() throws CardException, Exception{
    	
    	byte[] data = MainActivity.belpic.getCardInfo();
    	
    	((TextView) findViewById(R.id.card_data_value)).setText(TextUtils.hexDump(data, data.length - 12, 12));
    }
}