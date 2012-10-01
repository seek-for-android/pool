package com.gieseckedevrient.android.pcscdroid;



import java.io.IOException;

import com.gieseckedevrient.android.pcscdroid.MainActivity.ServiceStatusFragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class PcscService extends Service {
	private NotificationManager mNM;
	private final IBinder mBinder = new LocalBinder();
	private PcscDaemon pcscd = null;
	private String pcscPath;
	
	
	
	private int NOTIFICATION = R.string.local_service_started;

	public class LocalBinder extends Binder {
		PcscService getService() {
			return PcscService.this;
		}
	}
	
	public boolean isRunning() {
		if (pcscd!=null)
			return pcscd.isRunning();
		else
			return false;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

	
		showNotification();
		pcscPath = getBaseContext().getFilesDir() + "/pcscd";
		pcscd = new PcscDaemon(pcscPath,getBaseContext());
		pcscd.startService();
	}

	@Override
	public void onDestroy() {
		
		super.onDestroy();
		pcscd.stopService();
		
		
		mNM.cancel(NOTIFICATION);

		Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("pcscdroid", "Received start id " + startId + ": " + intent);

		
		return START_STICKY;
	}

	private void showNotification() {
		
		CharSequence text = getText(R.string.local_service_started);

		Notification notification = new Notification(R.drawable.ic_launcher,
				text, System.currentTimeMillis());

		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, ServiceStatusFragment.class), 0);

		notification.setLatestEventInfo(this,
				getText(R.string.local_service_label), text, contentIntent);
		notification.flags = Notification.FLAG_ONGOING_EVENT;

		mNM.notify(NOTIFICATION, notification);
	}

}