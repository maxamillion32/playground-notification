package com.playground.notification.app;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.playground.notification.utils.Prefs;

/**
 * Wakeup device.
 *
 * @author Xinyue Zhao
 */
public final class WakeupDeviceReceiver extends WakefulBroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = null;
		Prefs prefs = Prefs.getInstance();
		if (!prefs.isEULAOnceConfirmed()) {
			return;
		}
		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		if (prefs.notificationWeekendCall() && (day == Calendar.SATURDAY || day == Calendar.SUNDAY)) {
			if ((hour == 9 && min == 30) || (hour == 12 && min == 0) || (hour == 14 && min == 30)) {
				service = initService(context, true);
			}
		}
		if (prefs.notificationWarmTips()) {
			if (month >= Calendar.NOVEMBER && month <= Calendar.FEBRUARY) {
				//Fall ~ Winter
				if ((hour == 15 && min == 0) || (hour == 15 && min == 15)) {
					service = initService(context, false);
				}
			} else if (month >= Calendar.MARCH && month <= Calendar.MAY) {
				//Spring
				if ((hour == 15 && min == 30) || (hour == 16 && min == 0)) {
					service = initService(context, false);
				}
			} else if (month >= Calendar.JUNE && month <= Calendar.OCTOBER) {
				//Summer
				if ((hour == 15 && min == 40) || (hour == 16 && min == 0)) {
					service = initService(context, false);
				}
			}
		}

		if (service != null) {
			startWakefulService(context, service);
		}
	}

	@NonNull
	private Intent initService(Context context, boolean isWeekend) {
		Intent service;
		service = new Intent(context, AppGuardService.class);
		service.putExtra(AppGuardService.EXTRAS_WEEKEND, isWeekend);
		return service;
	}
}