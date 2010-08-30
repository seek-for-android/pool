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
import java.security.cert.CertificateEncodingException;





import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.smartcard.CardException;
import android.smartcard.SmartcardClient;
import android.smartcard.SmartcardClient.ISmartcardConnectionListener;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import be.cosic.android.eid.engine.EidEngine;
import be.cosic.android.eid.exceptions.*;

public class MainActivity extends TabActivity {
	
	
	
	
	private final int TAB_HEIGHT = 30;
	private TabHost tabHost;
	
	public static final String LOG_TAG = "@string/log_tag";
	
	static final int GET_FILE_LOCATION_FOR_SAVE_REQUEST = 0;
	static final int GET_FILE_LOCATION_FOR_LOAD_REQUEST = 1;
	
	private Intent intent;
	
	private static SmartcardClient smartcard;
	private static boolean smartCardConnected = false;
	
	public static EidEngine belpic = new EidEngine();
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setUpView();
        
        try {
        	smartcard = new SmartcardClient(this, connectionListener);
        	
        	
         	} catch (SecurityException e) {
         	  Log.e(LOG_TAG, "Binding not allowed, uses-permission SMARTCARD?");
         	  
         	  
         	  return;
         
         	} catch (Exception e) {
         	  Log.e(LOG_TAG, "Exception: " + e.getMessage());
         	  
         	}
        
        
        
       
        //Resources res = getResources(); // Resource object to get Drawables
        
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.eid_menu, menu);
        return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.read:
            readEid();
            return true;
        case R.id.load:
            loadEid();
            return true;
        case R.id.save:
            saveEid();
            return true;
        case R.id.exit:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    
    @Override
    protected void onDestroy() {
      if (smartcard != null) {
        smartcard.shutdown();//.unbindService();//.shutdown();
      }
      
      smartCardConnected = false;
      
      belpic.clearData();
      
      super.onDestroy();
    }
    
    
    protected void setUpView() {
		
		setContentView(R.layout.main);
        
		
		tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab
        
        
        
        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, Identity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("identity").setIndicator("Identity")
                      .setContent(intent);
        

        
        tabHost.addTab(spec);
        
        //tabHost.getTabWidget().getChildAt(0).getLayoutParams().height = TAB_HEIGHT;
        
        //TextView tv = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);        
        //tv.setTextColor(R.color.black);
        //tabHost.getTabWidget().setBackgroundColor(R.color.grey);
        //tv.setBackgroundColor(R.color.grey);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, IdentityExtra.class);
        spec = tabHost.newTabSpec("identityextra").setIndicator("Identity Extra")
                      .setContent(intent);
        tabHost.addTab(spec);
        
        

        intent = new Intent().setClass(this, Certificates.class);
        spec = tabHost.newTabSpec("certificates").setIndicator("Certificates")
                      .setContent(intent);
        tabHost.addTab(spec);

       
        
        intent = new Intent().setClass(this, CardPin.class);
        spec = tabHost.newTabSpec("cardpin").setIndicator("Card & PIN")
                      .setContent(intent);
        tabHost.addTab(spec);
        
        
        
        View v;
        for(int i=0;i<tabHost.getTabWidget().getChildCount();i++)
        	
        	{
        	        v = tabHost.getTabWidget().getChildAt(i);//

                	//float[] outerR = new float[] { 12, 12, 12, 12, 0, 0, 0, 0  };
                    //RectF   inset = new RectF(10, 10, 10, 10);
                    //float[] innerR = new float[] { 0, 0, 0, 0  ,12, 12, 12, 12};
        	        //ShapeDrawable shape = new ShapeDrawable(new RoundRectShape(innerR, inset, outerR));
        	        //ShapeDrawable shape = new ShapeDrawable(new OvalShape());
        	        //shape.getPaint()..setIntrinsicHeight(30);
        	        //shape.getPaint().setColor(Color.parseColor("#FFFFFF"));
        	        //v.setBackgroundDrawable(shape);
        	        
        	        v.setBackgroundColor(getResources().getColor(R.color.light_grey));//Color.parseColor("#E6E6E6"));
        	        
        	        ((TextView) v.findViewById(android.R.id.title)).setTextColor(R.color.black);        
        	      
        	
        	}

      
        
       tabHost.setCurrentTab(0);
       
       tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(getResources().getColor(R.color.dark_grey));//Color.parseColor("#C3C3C3"));
       
       tabHost.setOnTabChangedListener(new
       OnTabChangeListener() {
       	
       	public void onTabChanged(String tabId) {
       	
   	    	
   	    	
   	    	for(int i=0;i<tabHost.getTabWidget().getChildCount();i++)
   	    	
   	    	{
   	    	
   	    	tabHost.getTabWidget().getChildAt(i).setBackgroundColor(getResources().getColor(R.color.light_grey));
   	    	
   	    	}
   	    	
   	    	 
   	    	
   	    	tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(getResources().getColor(R.color.dark_grey));
       	}
       });
       
	}
    
   
    
    

    
    ISmartcardConnectionListener connectionListener = new
    ISmartcardConnectionListener() {
       public void serviceConnected() {
    	   
    	   
   	   try{
    	   belpic.connect(smartcard); 
	   	} catch (CardException e) {
			Log.e(LOG_TAG, "Exception in opening basic channel: " + e.getMessage());
			
			//TODO give error message
			
			return;
		}
	   	
	   	smartCardConnected = true;
	   	
	   	
	   	
        readEid();
        
       }
       public void serviceDisconnected() {
    	   
    	   smartCardConnected = false;
    	   
    	   //TODO show message box
       }
    };

    

	

	
	
	
	private void saveEid() {
		
		
		//TODO If android API version lower then 8, saving/load does not work as no Base64 and DOM transformer is available
//		Context context = getApplicationContext();
//    	int duration = Toast.LENGTH_LONG;
//    	Toast toast;
//    	toast = Toast.makeText(context, System.getProperty("java.version"), duration);
//		toast.setGravity(Gravity.CENTER, 0, 0);
//		toast.show();
//		
		
		//if(System.getProperty("java.version")){
		
		
		intent = new Intent().setClass(this, PathQuery.class);
		MainActivity.this.startActivityForResult(intent, GET_FILE_LOCATION_FOR_SAVE_REQUEST);
    	
    	
	}

	private void loadEid() {
		

		
		intent = new Intent().setClass(this, PathQuery.class);
		MainActivity.this.startActivityForResult(intent, GET_FILE_LOCATION_FOR_LOAD_REQUEST);
		
		
		
	}

	//TODO update current activity when refresh
	private void readEid() {
		
		if(smartCardConnected != true){
			
			//TODO show message: connection problem. check if secure smart card is inserted
			
			return;
			
			
		}
		
		try {
			
			belpic.readEid();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CardException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidResponse e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			belpic.parseEidData();
		} catch (UnsupportedEncodingException e) {
			//Should not occur
		}
		
		//TextView text = (TextView) tabHost.getChildAt(0).findViewById(R.id.firstnames);
		//text.setText("ja jong, zijn we er bijna of niet");
		//Toast.makeText(this, text.getText(),
         //       Toast.LENGTH_LONG).show();
		//this.getApplication();
		
		//getCurrentActivity().onContentChanged();
		
		
		//tabHost.getChildAt(0).findViewById(R.id.firstnames)ACCESSIBILITY_SERVICE;
		//belpic.getMF().getIDDirectory().getIdentityFile().toString()
		//tabHost.getChildAt(1).bringToFront();
		
		//tabHost.setCurrentTab(1);
		//tabHost.recomputeViewAttributes(tabHost.getChildAt(0));//getCurrentTabView().bringToFront();
		//Identity.setEidData();
	
		
		//tabHost.getTabWidget().setCurrentTab(0);
		
		//Switch tabs so to refresh the content
		tabHost.setCurrentTab(1);
		tabHost.setCurrentTab(0);
	
	}
    
	//Called when a child activity returns.
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
    	
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_LONG;
    	Toast toast;
    	
    	switch (requestCode){
    	
    	//If the return value is a PIN for testing:
    	case GET_FILE_LOCATION_FOR_SAVE_REQUEST:
    	
            if (resultCode == RESULT_OK) {
               
            	//Files will be stored on the SDcard under the given path
            	String[] files = data.getStringExtra("path").split(File.separator);
            	String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
            	String path = dir + File.separator + data.getStringExtra("path");
            	
            	
            	//Get the directory path
            	for(int i =0;i<(files.length-1);i++){
					dir = dir + File.separator + files[i] ;
				}
            	
          	
				try {
					
					//Check if an extension was added. If not or a false one, correct.
					//Not everything is checked but other things should be checked by OS
					if(!path.endsWith(".xml"))
	            		path=path + ".xml";
	            	else if(!path.endsWith(".xml"))
	            			throw new UnsupportedEncodingException();
					
					//We make new directories where necessary
					new File(dir).mkdirs();
					
					
					//Store the eid as an xml file
					belpic.storeEid(path);				
					
	        		
				} catch (	FileNotFoundException	 e) {
					
					toast = Toast.makeText(context, "FileNotFoundException", duration);
	    			toast.setGravity(Gravity.CENTER, 0, 0);
	    			toast.show();
					
					//TODO
					
					e.printStackTrace();
	        		
				} catch (IOException e) {
					
					toast = Toast.makeText(context, "IOException", duration);
	    			toast.setGravity(Gravity.CENTER, 0, 0);
	    			toast.show();
					
					//TODO
					
					e.printStackTrace();
				} 
            	
            	
            	
            }else ;//TODO if cancel of zo...
            
            break;
           
          //If the return value is a PIN for testing:
    	case GET_FILE_LOCATION_FOR_LOAD_REQUEST:
    	
            if (resultCode == RESULT_OK) {
               
            	
            	String[] files = data.getStringExtra("path").split(File.separator);
            	String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
            	String path = dir + File.separator + data.getStringExtra("path");
            	
            	
            	//Get the directory path
            	for(int i =0;i<(files.length-1);i++){
					dir = dir + File.separator + files[i] ;
				}
            	
          	
				try {
					
					//Check if an extension was added. If not or a false one, correct.
					//Not everything is checked but other things should be checked by OS
	            	if(!path.endsWith(".xml"))
	            		path=path + ".xml";
	            	else if(!path.endsWith(".xml"))
	            			throw new UnsupportedEncodingException();
					
					//We make new directories where necessary
					new File(dir).mkdirs();
					
					
					//Store the eid as an xml file
					belpic.loadEid(path);
	        		
					belpic.parseEidData();
					
					tabHost.setCurrentTab(1);
					tabHost.setCurrentTab(0);
	        		
				} catch (	FileNotFoundException	 e) {
					
					toast = Toast.makeText(context, "FileNotFoundException", duration);
	    			toast.setGravity(Gravity.CENTER, 0, 0);
	    			toast.show();
					
					//TODO
					
					e.printStackTrace();
	        		
				} catch (IOException e) {
					
					toast = Toast.makeText(context, "IOException", duration);
	    			toast.setGravity(Gravity.CENTER, 0, 0);
	    			toast.show();
					
					//TODO
					
					e.printStackTrace();
				} catch (GeneralSecurityException e) {

					toast = Toast.makeText(context, "GeneralSecurityException, Invalid eID Data", duration);
	    			toast.setGravity(Gravity.CENTER, 0, 0);
	    			toast.show();
					
					//TODO
				} 
            	
            	
            }else ;//TODO if cancel of zo...
            
            break;
           
                
    	default:
    		Log.e(MainActivity.LOG_TAG, "Problem in PathQuery return result: Invalid return request code.");
        }
    }
    
    
}