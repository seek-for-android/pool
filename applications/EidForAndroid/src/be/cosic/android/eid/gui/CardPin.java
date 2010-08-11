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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.smartcard.CardException;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import be.cosic.android.eid.exceptions.InvalidPinException;
import be.cosic.android.util.TextUtils;

public class CardPin extends Activity {
	
	private Intent intent;
	
	private String old_pin;
	private String new_pin_1;
	private String new_pin_2;
	
	static final int GET_PIN_FOR_TEST_REQUEST = 0;
	static final int GET_PIN_FOR_CHANGE_REQUEST_1 = 1;
	static final int GET_PIN_FOR_CHANGE_REQUEST_2 = 2;
	static final int GET_PIN_FOR_CHANGE_REQUEST_3 = 3;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cardpin);
        
        final Button test_pin = (Button) findViewById(R.id.test_pin);
        
        test_pin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
	            
            	//start new activity
            	intent.putExtra("requestCode", GET_PIN_FOR_TEST_REQUEST);
            	CardPin.this.startActivityForResult(intent, GET_PIN_FOR_TEST_REQUEST);
            	
            	//get the pin from the input and check it: see on activity result method
	            	
	            	
            }
        });
        
        
        final Button change_pin = (Button) findViewById(R.id.change_pin);
        
        change_pin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
            	
            	
            	//get old pin
            	intent.putExtra("requestCode", GET_PIN_FOR_CHANGE_REQUEST_1);
            	CardPin.this.startActivityForResult(intent, GET_PIN_FOR_CHANGE_REQUEST_1);
            	
            	
            }
        });
        
        
        
        intent = new Intent().setClass(this, PinQuery.class);
        
        
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
            }else ;//TODO if cancel of zo...
            
            break;
            
        //If the return value is a PIN for changing:
    	case GET_PIN_FOR_CHANGE_REQUEST_1:
    	
            if (resultCode == RESULT_OK) {
               
            	
	            	
            		old_pin = data.getStringExtra("PIN");
            		
            		
            		//get new pin
            		intent.putExtra("requestCode", GET_PIN_FOR_CHANGE_REQUEST_2);
                	CardPin.this.startActivityForResult(intent, GET_PIN_FOR_CHANGE_REQUEST_2);
                	
	    			
            	
            }else ;//TODO if cancel of zo...
            
            
            break;
        
          //If the return value is a PIN for changing:
    	case GET_PIN_FOR_CHANGE_REQUEST_2:
    	
            if (resultCode == RESULT_OK) {
               
            	
	            	
            		new_pin_1 = data.getStringExtra("PIN");
            		
            		//get new pin
            		intent.putExtra("requestCode", GET_PIN_FOR_CHANGE_REQUEST_3);
                	CardPin.this.startActivityForResult(intent, GET_PIN_FOR_CHANGE_REQUEST_3);
                	
                	//get the pin from the input and change it: see on activity result method
                	
                	
	    			
            	
            }else ;//TODO if cancel of zo...
        
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
            }else ;//TODO if cancel of zo...
            
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