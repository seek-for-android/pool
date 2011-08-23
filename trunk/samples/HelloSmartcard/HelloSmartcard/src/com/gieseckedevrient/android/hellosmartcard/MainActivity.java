package com.gieseckedevrient.android.hellosmartcard;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import org.simalliance.openmobileapi.*;

/**
 * This is the refactored sample programm using only the new OMA.
 * 
 * @author user
 * 
 */
public class MainActivity extends Activity implements SEService.CallBack {

	final String LOG_TAG = "HelloSmartcard";

	/**
	 * API entry point
	 */
	private SEService seService;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// do layout and UI stuff
		LinearLayout layout = createLayout();
		Button button = createButton();

		// the OnClickListener implements the communication with the secure element
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					Log.d(LOG_TAG, "Getting available readers...");
					Reader[] readers = seService.getReaders();

					Log.d(LOG_TAG, "readers.length = " + readers.length);
					if (readers.length < 1)
						return;

					Log.d(LOG_TAG, "Getting Session from the first reader...");
					Session session = readers[0].openSession();

					// Select our applet
					Log.d(LOG_TAG,
							"Getting logical channel from the session...");
					Channel channel = session.openLogicalChannel(new byte[] {
							(byte) 0xD2, 0x76, 0x00, 0x01, 0x18, 0x00, 0x02,
							(byte) 0xFF, 0x49, 0x50, 0x25, (byte) 0x89,
							(byte) 0xC0, 0x01, (byte) 0x9B, 0x01 });

					// Send our custom hello world command
					Log.d(LOG_TAG, "transmit()");
					byte[] respApdu = channel.transmit(new byte[] {
							(byte) 0x90, 0x10, 0x00, 0x00, 0x00 });

					channel.close();

					// Parse response and show String
					byte[] helloStr = new byte[respApdu.length - 2];
					System.arraycopy(respApdu, 0, helloStr, 0,
							respApdu.length - 2);
					Toast.makeText(MainActivity.this, new String(helloStr),
							Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					Log.e(LOG_TAG, "Error occured:", e);
					return;
				}
			}
		});

		layout.addView(button);
		setContentView(layout);

		try {
			// create API entry point
			Log.i(LOG_TAG, "creating new SEService");
			seService = new SEService(this, this);
		} catch (SecurityException e) {
			Log.e(LOG_TAG, "Binding not allowed, uses-permission SMARTCARD?");
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception: " + e.getMessage());
		}
	}

	@Override
	protected void onDestroy() {
		if (seService != null && seService.isConnected()) {
			seService.shutdown();
		}
		super.onDestroy();
	}

	public void serviceConnected(SEService service) {
		Log.i(LOG_TAG, "entry: seviceConnected()");

	}

	private LinearLayout createLayout() {
		LinearLayout layout = new LinearLayout(this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		return layout;
	}

	private Button createButton() {
		Button button = new Button(this);
		button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		button.setText("Click Me");
		return button;
	}

}
