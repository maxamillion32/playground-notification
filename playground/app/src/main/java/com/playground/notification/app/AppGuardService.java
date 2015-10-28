package com.playground.notification.app;


import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.playground.notification.utils.Prefs;

public final class AppGuardService extends GcmTaskService {
	private static final String TAG = "AppGuardService";
	private static int sLastHour = -1;
	private static int sLastMin = -1;

	@Override
	public int onRunTask(TaskParams taskParams) {
		synchronized (AppGuardService.TAG) {
			Intent service = null;
			Prefs prefs = Prefs.getInstance();
			if (!prefs.isEULAOnceConfirmed()) {
				return GcmNetworkManager.RESULT_SUCCESS;
			}
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int min = calendar.get(Calendar.MINUTE);
			if (hour == sLastHour && min == sLastMin) {
				return GcmNetworkManager.RESULT_SUCCESS;
			}
			sLastHour = hour;
			sLastMin = min;
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_WEEK);
			if (prefs.notificationWeekendCall() && (day == Calendar.SATURDAY || day == Calendar.SUNDAY)) {
				if ((hour == 9 && min == 30) ||
						(hour == 12 && min == 0) ||
						(hour == 14 && min == 30)) {
					service = initService(this, true);
				}
			}
			if (prefs.notificationWarmTips()) {
				if (month >= Calendar.NOVEMBER && month <= Calendar.FEBRUARY) {
					//Fall ~ Winter
					if ((hour == 15 && min == 0) || (hour == 15 && min == 15)) {
						service = initService(this, false);
					}
				} else if (month >= Calendar.MARCH && month <= Calendar.MAY) {
					//Spring
					if ((hour == 15 && min == 30) || (hour == 16 && min == 0)) {
						service = initService(this, false);
					}
				} else if (month >= Calendar.JUNE && month <= Calendar.OCTOBER) {
					//Summer
					if ((hour == 15 && min == 40) || (hour == 16 && min == 0)) {
						service = initService(this, false);
					}
				}
			}

			if (service != null) {
				startService(service);
			}

			return GcmNetworkManager.RESULT_SUCCESS;
		}
	}


	@NonNull
	private Intent initService(Context context, boolean isWeekend) {
		Intent service;
		service = new Intent(context, NotifyUserService.class);
		service.putExtra(NotifyUserService.EXTRAS_WEEKEND, isWeekend);
		return service;
	}
}
