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





import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAKey;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.w3c.dom.Text;

import be.cosic.android.util.TextUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Certificates extends Activity {
	
	private Intent intent;
	
	private X509Certificate currentCert;
	private int currentCertIndex = 0;
	
	static final int GET_FILE_LOCATION_REQUEST = 0;
	
	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.certificates);
        
        Spinner spinner = (Spinner) findViewById(R.id.certificates);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.certificate_array, android.R.layout.simple_spinner_item);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        
        intent = new Intent().setClass(this, PathQuery.class);
        
        spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
        	public void onItemSelected(AdapterView<?> parent,
        	        View view, int pos, long id) {

        		
        		currentCertIndex = pos;
        		
        		try {
        			
        			
					setEidData();
					
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
       		
        		
        	    }

        	public void onNothingSelected(AdapterView<?> parent) {
        	      // Do nothing.
        	}
        });
        
        
        final Button save_certificate = (Button) findViewById(R.id.save_certificate);
        save_certificate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
            	//The file will be saved in the sdcard directory of the phone. As no default file chooser is yet available in android.
            	//The user is prompted to enter the rest of the path, starting in the directory. Corresponding dirs will be created if not existing yet.
            	
            	
            	
            	
            	Certificates.this.startActivityForResult(intent, GET_FILE_LOCATION_REQUEST);
            	
            	
            	
            	
            	
            	
//            	
            	//TODO: make trhis work!
            	
//            	//popUp();
//            	int duration = Toast.LENGTH_LONG;
//            	Toast toast;
//            	
//            	File[] roots = File.listRoots();
//            	File root = roots[0];
//            	
//            	File sdcard = new File("/sdcard");
//            	
//            	
//            	String[] sub = sdcard.list();
//            	
//            	
//            	CharSequence text = "";
//            	
//            	for(int i =0; i < sub.length; i++){
//            		text = text + "\n" + sub[i];
//            	}
//            	
//    			toast = Toast.makeText(context, text, duration);
//    			toast.setGravity(Gravity.CENTER, 0, 0);
//    			toast.show();// ----> "/" dus slechts 1 root namelijk / ????
//            	
//            	
//            	
//            	Intent intentBrowseFiles = new Intent(Intent.ACTION_GET_CONTENT); 
//            	intentBrowseFiles.setType("*/*"); 
//            	intentBrowseFiles.addCategory(Intent.CATEGORY_OPENABLE); 
//            	
//            	Certificates.this.startActivityForResult(intentBrowseFiles, GET_FILE_LOCATION_REQUEST);
//            	
//            	String filename = "file:///...";
//            	
//            	
//            	FileOutputStream file;
//				try {
            	
            	
//					file = new FileOutputStream(filename);
//					file.write(currentCert.getEncoded());
//	        		file.close();
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (CertificateEncodingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//        		
            	
            	
            }

			
        });
        
        
        
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
    	
    	
    	
    	switch (requestCode){
    	
    	
    	case GET_FILE_LOCATION_REQUEST:
    	
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
					if(!path.endsWith(".crt"))
	            		path=path + ".crt";
	            	else if(!path.endsWith(".crt"))
	            			throw new UnsupportedEncodingException();
					
					//We make new directories where necessary
					new File(dir).mkdirs();
					
					//Now we store the file					
					//openFileOutput can not contain path separators in its name!!!!!!!
					//FileOutputStream fos = openFileOutput(path, Context.MODE_WORLD_READABLE);
					FileOutputStream fos = new FileOutputStream(path);
					fos.write(currentCert.getEncoded());
	        		fos.close();
				} catch (FileNotFoundException e) {
					
					//TODO
					
					e.printStackTrace();
				} catch (CertificateEncodingException e) {
					
					//TODO
					
					e.printStackTrace();
				} catch (IOException e) {
					//TODO
					
					e.printStackTrace();
				}
            	
            	
            	
            }else ;//Do nothing
            
            break;
           
            
    	default:
    		Log.e(MainActivity.LOG_TAG, "Problem in PathQuery return result: Invalid return request code.");
        }
    }
    
    
    
    
    public void setEidData() throws Exception{
    	ByteArrayInputStream inStream;
    	switch (currentCertIndex) {
		case 0:
			inStream = new ByteArrayInputStream(TextUtils.hexStringToByteArray(((Text) MainActivity.belpic.getAuthenticationCertificateFileData().getFirstChild()).getData()));
			break;
		case 1:
			inStream = new ByteArrayInputStream(TextUtils.hexStringToByteArray(((Text) MainActivity.belpic.getNonRepudiationCertificateFileData().getFirstChild()).getData()));
			break;
		case 2:
			inStream = new ByteArrayInputStream(TextUtils.hexStringToByteArray(((Text) MainActivity.belpic.getCaCertificateFileData().getFirstChild()).getData()));
			break;
		case 3:
			inStream = new ByteArrayInputStream(TextUtils.hexStringToByteArray(((Text) MainActivity.belpic.getRootCaCertificateFileData().getFirstChild()).getData()));
			break;
		case 4:
			inStream = new ByteArrayInputStream(TextUtils.hexStringToByteArray(((Text) MainActivity.belpic.getRrnCertificateFileData().getFirstChild()).getData()));
			break;
		default:
			inStream = null;
			throw new Exception();
		}
    	CertificateFactory cf = CertificateFactory.getInstance("X.509");
		currentCert = (X509Certificate) cf.generateCertificate(inStream);
		inStream.close();
		
		
		
		((TextView) findViewById(R.id.owner_value)).setText(currentCert.getSubjectX500Principal().getName().split("N=")[1].split(",")[0]);
		((TextView) findViewById(R.id.CA_value)).setText(currentCert.getIssuerX500Principal().getName().split("N=")[1].split(",")[0]);
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(currentCert.getNotBefore());
		((TextView) findViewById(R.id.certificate_validity_value)).setText(cal.get(Calendar.DAY_OF_MONTH) + "/" + Integer.toString(cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR) + " - ");
		cal.setTime(currentCert.getNotAfter());
		((TextView) findViewById(R.id.certificate_validity_value)).append(cal.get(Calendar.DAY_OF_MONTH) + "/" + Integer.toString(cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
		
		((TextView) findViewById(R.id.version_value)).setText("v" + currentCert.getVersion());
		((TextView) findViewById(R.id.type_value)).setText(currentCert.getSigAlgName());
		
		String use = "";
		if(currentCert.getKeyUsage()[0] == true)
			use = use + "Signature, ";
		if(currentCert.getKeyUsage()[1] == true)
			use = use + "Non-Repudiation, ";
		if(currentCert.getKeyUsage()[2] == true)
			use = use + "Key Encipherment, ";
		if(currentCert.getKeyUsage()[3] == true)
			use = use + "Data Encipherment, ";
		if(currentCert.getKeyUsage()[4] == true)
			use = use + "Key Agreement, ";
		if(currentCert.getKeyUsage()[5] == true)
			use = use + "Key Cert Signature, ";
		if(currentCert.getKeyUsage()[6] == true)
			use = use + "CRL Signing, ";
		if(currentCert.getKeyUsage()[7] == true)
			use = use + "Encipher only, ";
		if(currentCert.getKeyUsage()[8] == true)
			use = use + "Decipher only, ";
		
		if(use.equals(""))
			((TextView) findViewById(R.id.use_value)).setText("None");
		else ((TextView) findViewById(R.id.use_value)).setText(use.subSequence(0, use.length()-2));
		
		((TextView) findViewById(R.id.publickeylength_value)).setText(Integer.toString(((RSAKey) (currentCert.getPublicKey())).getModulus().bitLength()));
		//((TextView) findViewById(R.id.publickeylength_value)).setText("jaja toch");
    }
    
    
    
    //Will show a popup dialog: unfortunatly does not allow user input ---> use dialog theme activity instead! See on saveButton click listenere above.
    private void popUp() {
    	
    	
		LayoutInflater inflater = getLayoutInflater();
    	View mView= inflater.inflate(R.layout.pathquery,(ViewGroup)findViewById(R.id.pathquery));
    	
    	
        PopupWindow mPopupWindow = new PopupWindow(mView,LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, false);
        mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
       
        TextView TV= (TextView) this.findViewById(R.id.owner);          
       
        mPopupWindow.showAtLocation(TV, Gravity.CENTER, 0, 0);
     
		
	}
    
    
}