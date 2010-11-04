package btpcsc.server;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BtpcscBroadcastReceiver extends BroadcastReceiver {

	private void doStartService(Context context) {
		Log.v("BtpcscReceiver", "Starting BTPCSC service.");
		Intent i = new Intent();
		i.setAction("btpcsc.server.BtpcscServer");
		context.startService(i);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			
			if (BluetoothAdapter.getDefaultAdapter().isEnabled())
				return;
			
			doStartService(context);

		} else if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
			
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
			if (state != BluetoothAdapter.STATE_ON) return;
			
			doStartService(context);
			
		}
	}
	
}
