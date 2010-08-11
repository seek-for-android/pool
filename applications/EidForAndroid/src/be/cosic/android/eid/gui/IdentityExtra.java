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
    	
    	
    	
    	if(((String) (MainActivity.belpic.addressInfo.get("Zip code"))).length() == 4)
    		((TextView) findViewById(R.id.country)).setText("Belgium");
    	else ((TextView) findViewById(R.id.country)).setText("Unknown");
    	
    	if(MainActivity.belpic.identityInfo.get("Special status").equals("0"))
    		((TextView) findViewById(R.id.status)).setText("");
    	else ((TextView) findViewById(R.id.status)).setText(MainActivity.belpic.identityInfo.get("Special status"));
    	
    	
    	
    	
    }
}