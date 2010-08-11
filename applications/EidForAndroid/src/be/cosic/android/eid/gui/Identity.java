package be.cosic.android.eid.gui;



import org.w3c.dom.Text;

import be.cosic.android.util.TextUtils;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class Identity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identity);

        
        
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
    	
    	//Put the parsed data in the right views
		
    	if(MainActivity.belpic.identityInfo.get("Name") != null){
    	    	
	    	((TextView) findViewById(R.id.name)).setText(MainActivity.belpic.identityInfo.get("Name"));
	    	((TextView) findViewById(R.id.firstnames)).setText(MainActivity.belpic.identityInfo.get("First names"));
	    	((TextView) findViewById(R.id.birth)).setText(MainActivity.belpic.identityInfo.get("Birth Location") + " " + MainActivity.belpic.identityInfo.get("Birth Date"));
	    	((TextView) findViewById(R.id.sex)).setText(MainActivity.belpic.identityInfo.get("Sex"));
	    	((TextView) findViewById(R.id.nationality)).setText(MainActivity.belpic.identityInfo.get("Nationality"));
	    	((TextView) findViewById(R.id.cardnumber)).setText(MainActivity.belpic.identityInfo.get("Card Number"));
	    	
	    	((TextView) findViewById(R.id.validity)).setText(MainActivity.belpic.identityInfo.get("Card validity start date") + " - " + MainActivity.belpic.identityInfo.get("Card validity end date"));
	    	
	    	ImageView image = (ImageView) findViewById(R.id.photo);
	    	//byte[] photo = TextUtils.hexStringToByteArray(((Text) MainActivity.belpic.getDocument().getElementsByTagName("photoFileData").item(0).getFirstChild()).getData());
	    	byte[] photo = TextUtils.hexStringToByteArray(((Text) MainActivity.belpic.getPhotoFileData().getFirstChild()).getData());
	    	
	    	image.setImageBitmap(BitmapFactory.decodeByteArray(photo,0,photo.length));
	    	
    	}
    	
    }
}