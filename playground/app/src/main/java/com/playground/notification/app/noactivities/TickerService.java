package com.playground.notification.app.noactivities;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.playground.notification.R;
import com.playground.notification.utils.NotifyUtils;


public class TickerService extends Service {
	private static final int ONGOING_NOTIFICATION_ID = 0x57;
	private static final String TAG = "TickerService";
	private IntentFilter mTickerFilter = new IntentFilter(Intent.ACTION_TIME_TICK);

	private BroadcastReceiver mTickerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			startService(new Intent(context, AppGuardService.class));
		}
	};

	public TickerService() {

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Notification notification = NotifyUtils.buildNotifyWithoutBigImage(
				this,
				ONGOING_NOTIFICATION_ID,
				getString(R.string.application_name),
				getString(R.string.lbl_notify_content),
				R.drawable.ic_balloon,
				NotifyUtils.getAppHome(this),
				true
		);
		startForeground(ONGOING_NOTIFICATION_ID, notification);
		registerReceiver(mTickerReceiver, mTickerFilter);
		return START_STICKY;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTickerReceiver != null) {
			unregisterReceiver(mTickerReceiver);
			mTickerReceiver = null;
		}
	}
}
