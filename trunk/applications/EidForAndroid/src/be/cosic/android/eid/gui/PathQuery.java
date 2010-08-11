package be.cosic.android.eid.gui;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PathQuery extends Activity {
	
	
	private EditText path_entry;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pathquery);
        
        final Intent resultIntent = new Intent();
        path_entry = (EditText) findViewById(R.id.path_entry);
        
        
        	
        
        
        final Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	
                
            	resultIntent.putExtra("path", path_entry.getText().toString());
            	
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



