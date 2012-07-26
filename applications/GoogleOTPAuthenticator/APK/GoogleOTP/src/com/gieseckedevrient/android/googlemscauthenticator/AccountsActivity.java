package com.gieseckedevrient.android.googlemscauthenticator;

import java.util.Arrays;

import org.simalliance.openmobileapi.Channel;
import org.simalliance.openmobileapi.Reader;
import org.simalliance.openmobileapi.service.CardException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class AccountsActivity extends ListActivity {

	private static final String OTP_DEMO_TAG = "OtpCalculator";
	private static final int SCAN_INTENT_ID = 0x6034f621;

	public String[] accounts;
	public int selectedAccount;
	
	protected Reader destinationReader = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerForContextMenu(getListView());
		refresh();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case 0:
			showCounter(selectedAccount);
			return true;
		case 1:
			deleteAccount(selectedAccount);
			return true;
		}

		return super.onContextItemSelected(item);
	}

	private void showCounter(int index) {
		Channel channel = null;
		try {
			channel = ChannelUtils.openChannelAndSelect(ChannelUtils.getReader(accounts[index]));
			ChannelUtils.selectAccount(channel, accounts[index]);
			long counter = ChannelUtils.getCounter(channel, index);
			boolean hotp = ChannelUtils.getHOTP(channel);
			
			String str;
			if (hotp)
				str = Long.toString(counter);
			else
				str = ChannelUtils.counterToDate(counter).toLocaleString();
			channel.close();
			
			AlertDialog dialog = (new AlertDialog.Builder(this)).create();
			dialog.setTitle((hotp ? "Counter" : "Time") + " of account " + accounts[index]);
			dialog.setMessage(str);
			dialog.show();
			
		} catch (CardException e) {
			if (channel != null)
				channel.close();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void deleteAccount(int index) {
		Channel channel = null;
		try {
			channel = ChannelUtils.openChannelAndSelect(ChannelUtils.getReader(accounts[index]));
			if( channel != null ){
				ChannelUtils.deleteAccount(channel, accounts[index]);
				channel.close();
			}
			refresh();
		} catch (CardException e) {
			if (channel != null)
				channel.close();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private void reset() {
		Channel channel = null;
		try {
			ChannelUtils.reset();
			ChannelUtils.setSelectedAccount("");
			refresh();
		} catch (CardException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		selectedAccount = info.position;
		menu.add(0, 0, 0, "Show counter/time");
		menu.add(0, 1, 1, "Delete");
	}

	private void refresh() {
		String[] accounts;
		Channel channel = null;
		try {
			accounts = ChannelUtils.getAccounts();
			Arrays.sort(accounts);
			this.accounts = accounts;
			setListAdapter(new ArrayAdapter<String>(this, R.layout.account,
					accounts));
		} catch (CardException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String account = accounts[position];
		Intent intent = new Intent();
		intent.putExtra("account", account);
		setResult(RESULT_OK, intent);
		finish();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Scan code").setIcon(R.drawable.menu_scan);
		menu.add(0, 1, 1, "Refresh").setIcon(R.drawable.menu_refresh);
		menu.add(0, 2, 2, "Delete all").setIcon(R.drawable.menu_delete);
		return true;
	}

	// Opens the barcode scanner in order to scan the personalisation code
	private void scanCode() {
		if (!ChannelUtils.ensureConnected())
			return;
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		intent.putExtra("SAVE_HISTORY", false);
		startActivityForResult(intent, SCAN_INTENT_ID);
	}

	// This is called when the barcode scanner has finished. If it was
	// successful,
	// the secret will be read from the URI in the barcode.
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == SCAN_INTENT_ID && resultCode == Activity.RESULT_OK) {
			String result = intent.getStringExtra("SCAN_RESULT");
			try {
				Uri uri = Uri.parse(result);
				parseAndSaveSecret(uri);
			} catch (IllegalArgumentException e) {
				Toast
						.makeText(
								this,
								"Invalid QR code. Please ensure the code you scanned is a valid OTP secret code.",
								Toast.LENGTH_LONG).show();
			} catch( CardException e ){
				Toast
				.makeText(
						this,
						e.getLocalizedMessage(),
						Toast.LENGTH_LONG).show();
				
			}
		}
	}

	// Parses a personalisation URI and saves the secret (and the counter, if
	// HOTP is used)
	// to the smartcard.
	private void parseAndSaveSecret(Uri uri) throws CardException {

		String scheme = uri.getScheme();

		if (scheme == null) {
			Log.w(OTP_DEMO_TAG, "QR code is not a URI.");
			throw new IllegalArgumentException(
					"Invalid QR code: Code is not a URI");
		}

		String authority = uri.getAuthority();
		String user = null, secret = null;
		int type = ChannelUtils.TOTP;
		long counter = 0;

		if (scheme.equalsIgnoreCase("otpauth")) {

			if (authority != null && authority.equalsIgnoreCase("totp")) {
				type = ChannelUtils.TOTP; // TOTP
			} else if (authority != null && authority.equalsIgnoreCase("hotp")) {
				type = ChannelUtils.HOTP; // HOTP
				String counterParameter = uri.getQueryParameter("counter");
				if (counterParameter != null) {
					counter = Long.parseLong(counterParameter);
				}
			}

			user = uri.getPath();
			if (user != null && user.length() > 1) {
				user = user.substring(1); // path is "/user", so remove leading
											// /
			}

			secret = uri.getQueryParameter("secret");

		} else if (scheme.equalsIgnoreCase("totp")) {

			if (authority != null) {
				user = authority;
			}
			secret = uri.getFragment();

		} else {

			user = uri.getQueryParameter("user");
			secret = uri.getFragment();

		}

		if (user == null)
			user = "Default account";

		if (secret == null || secret.length() == 0) {
			Log.w(OTP_DEMO_TAG, "Secret not found in URI.");
			throw new IllegalArgumentException(
					"Invalid QR code: No secret in code.");
		}

		handleSecret(user, Base32Utils.base32ToByteArray(secret, 20), counter,
				type);

	}

	// Writes a secret and (when using HOTP) the counter to the smartcard
	private void handleSecret(String account, byte[] secret, long counter,
			int type) throws CardException {
		Reader reader = ChannelUtils.getReader(account);
		if( reader == null ){
			askForDestinationReader(account, secret, counter, type);
		} else {
			handleSecret( reader, account, secret, counter, type );
		}
	}

	
	private void handleSecret(Reader reader, String account, byte[] secret,
			long counter, int type) {
		Channel channel = null;
		try {

			if (!ChannelUtils.ensureConnected())
				return;
			
			channel = ChannelUtils.openChannelAndSelect(reader);
			if (type == ChannelUtils.HOTP)
				ChannelUtils.setCounter(channel, counter);

			ChannelUtils.personalize(channel, account, secret, type);
			
			ChannelUtils.setSelectedAccount(account);

		} catch (Exception e) {
			Log.e(OTP_DEMO_TAG, "Exception during handleSecret()");
			e.printStackTrace();
			Toast.makeText(this, "Could not save secret: " + e,
					Toast.LENGTH_LONG).show();
		} finally {
			if( channel != null ){
				channel.close();
			}
		}
		refresh();
	}

	private void askForDestinationReader(final String account, final byte[] secret, final long counter, final int type) throws CardException {
		final Reader[] otpReader = ChannelUtils.getOtpReader();
		
		if( otpReader == null ){
			throw new CardException("No OTP enabled secure element available.");
		}
		
		if( otpReader.length == 1 ){
	    	   handleSecret( otpReader[0],
	    			   account,
	    			   secret,
	    			   counter,
	    			   type );
	    	   return;
		} else {
		
		  AlertDialog.Builder builder = new Builder(this);

		  builder.setTitle("Choose the OTP terminal for " + account);
		  
		  String[] readerNames = new String[otpReader.length];
		  int i = 0;
		  for( Reader reader : otpReader ){
			  readerNames[i++] = reader.getName();
		  }
		  
		  builder.setItems(readerNames, new DialogInterface.OnClickListener(){
		       public void onClick(DialogInterface dialog, int which){
		    	   handleSecret( otpReader[which],
		    			   account,
		    			   secret,
		    			   counter,
		    			   type );
		       }
	      });

		  builder.show();
		}
	}

	  protected void onCreateReaderDialog( String account, final Reader[] otpReader ){
	 } 	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		
		// Scan code
		case 0:
			scanCode();
			return true;
			
		// Refresh
		case 1:
			refresh();
			return true;
			
		// Reset
		case 2:
			if (!ChannelUtils.ensureConnected()) return true;
			
			DialogInterface.OnClickListener yesListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					reset();
				}
			};
			
			askYesNo("Are you sure you want to reset all your secure element personalisation for Google OTP?",
					yesListener, null);
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void askYesNo(String message, DialogInterface.OnClickListener yesListener, 
			DialogInterface.OnClickListener noListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("Yes", yesListener);
		builder.setNegativeButton("No", noListener);
		builder.show();
	}

}
