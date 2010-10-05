package btpcsc.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BtpcscBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.v("BtpcscReceiver", "Starting BTPCSC service.");
			Intent i = new Intent();
			i.setAction("btpcsc.server.BtpcscServer");
			context.startService(i);
		}
	}
	
}
