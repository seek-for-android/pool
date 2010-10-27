package com.gieseckedevrient.android.hellosmartcard;

import android.app.Activity;
import android.os.Bundle;
import android.smartcard.CardException;
import android.smartcard.ICardChannel;
import android.smartcard.SmartcardClient;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

	SmartcardClient smartcard;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		final String LOG_TAG = "HelloSmartcard";
		
		super.onCreate(savedInstanceState);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		
		Button button = new Button(this);
		button.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		
		button.setText("Click Me");
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ICardChannel cardChannel;
				try {
					cardChannel = smartcard.openLogicalChannel(
							"UICC", new byte[] {
									(byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x00,
									0x02, (byte) 0xFF, 0x49, 0x50, 0x25,
									(byte) 0x89, (byte) 0xC0, 0x01,
									(byte) 0x9B, 0x01 });
		
					byte[] respApdu = cardChannel.transmit(new byte[] {
							(byte) 0x90, 0x10, 0x00, 0x00, 0x00 });
					
					cardChannel.close();
					
					byte[] helloStr = new byte[respApdu.length - 2];
					System.arraycopy(respApdu, 0, helloStr, 0,
							respApdu.length - 2);
					
					Toast.makeText(MainActivity.this, new String(helloStr),
							Toast.LENGTH_LONG).show();
				} catch (CardException e) {
					return;
				}
			}
		});
		
		layout.addView(button);
		setContentView(layout);

		
		try {
			smartcard = new SmartcardClient(this, null);
		} catch (SecurityException e) {
			Log.e(LOG_TAG, "Binding not allowed, uses-permission SMARTCARD?");
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception: " + e.getMessage());
		}
	}

	@Override
	protected void onDestroy() {
		if (smartcard != null) {
			smartcard.shutdown();
		}
		super.onDestroy();
	}

}
