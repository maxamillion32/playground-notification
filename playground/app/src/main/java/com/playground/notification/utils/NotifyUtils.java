package com.playground.notification.utils;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.ContextCompat;

import com.playground.notification.R;
import com.playground.notification.app.activities.MapsActivity;


public final class NotifyUtils {




	public static Notification buildNotifyWithoutBigImage(Context cxt, int id, String title, String desc,
			@DrawableRes int icon, PendingIntent contentIntent, boolean isVibrated) {
		Builder builder = new Builder(cxt).setWhen(id).setSmallIcon(icon).setTicker(title).setContentTitle(title)
				.setContentText(desc).addAction(R.drawable.ic_rating, cxt.getString(R.string.btn_app_rating),
						getAppPlayStore(cxt)).setStyle(new BigTextStyle().bigText(desc).setBigContentTitle(title))
				.setAutoCancel(true);
		builder.setContentIntent(contentIntent);
		if (isVibrated) {
			Vibrator v = (Vibrator) cxt.getSystemService(Context.VIBRATOR_SERVICE);
			v.vibrate(2000);
		}
		builder.setLights(ContextCompat.getColor(cxt, R.color.primary_color), 1000, 1000);
		return builder.build();
	}


	public static PendingIntent getAppPlayStore(Context cxt) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("https://play.google.com/store/apps/details?id=" + cxt.getPackageName()));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return PendingIntent.getActivity(cxt, com.chopping.utils.Utils.randInt(1, 9999), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static PendingIntent getAppHome(Context cxt) {
		Intent intent = new Intent(cxt, MapsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return PendingIntent.getActivity(cxt, com.chopping.utils.Utils.randInt(1, 9999), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

}