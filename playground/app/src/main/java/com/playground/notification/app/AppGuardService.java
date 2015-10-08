package com.playground.notification.app;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v7.app.NotificationCompat;

import com.playground.notification.R;
import com.playground.notification.api.Api;
import com.playground.notification.api.ApiNotInitializedException;
import com.playground.notification.app.activities.MapsActivity;
import com.playground.notification.ds.weather.Weather;
import com.playground.notification.ds.weather.WeatherDetail;
import com.playground.notification.utils.Prefs;
import com.playground.notification.utils.Utils;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * A server that protect application  by deleting unused data.
 *
 * @author Xinyue Zhao
 */
public class AppGuardService extends Service {

	private NotificationManager mNotificationManager;
	private android.support.v4.app.NotificationCompat.Builder mNotifyBuilder;

	private IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context cxt, Intent intent) {
			Prefs prefs = Prefs.getInstance();
			if (!prefs.isEULAOnceConfirmed()) {
				return;
			}
			if (App.Instance.getCurrentLocation() != null) {
				String units = "metric";
				switch (prefs.getWeatherUnitsType()) {
				case "0":
					units = "metric";
					break;
				case "1":
					units = "imperial";
					break;
				}
				Location location = App.Instance.getCurrentLocation();
				try {
					Api.getWeather(location.getLatitude(), location.getLongitude(), Locale.getDefault().getLanguage(),
							units, App.Instance.getWeatherKey(), new Callback<Weather>() {
								@Override
								public void success(Weather weather, Response response) {
									Prefs prefs = Prefs.getInstance();
									List<WeatherDetail> details = weather.getDetails();
									if (details != null && details.size() > 0) {
										WeatherDetail weatherDetail = details.get(0);
										if (weatherDetail != null) {
											int units = R.string.lbl_c;
											switch (prefs.getWeatherUnitsType()) {
											case "0":
												units = R.string.lbl_c;
												break;
											case "1":
												units = R.string.lbl_f;
												break;
											}
											String temp = weather.getTemperature() != null ? getString(units,
													weather.getTemperature().getValue()) : getString(units, 0f);
											Calendar calendar = Calendar.getInstance();
											int month = calendar.get(Calendar.MONTH);
											int hour = calendar.get(Calendar.HOUR_OF_DAY);
											int min = calendar.get(Calendar.MINUTE);
											int day = calendar.get(Calendar.DAY_OF_WEEK);
											Intent i = new Intent(App.Instance, MapsActivity.class);
											i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
											if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
												if ((hour == 9 && min == 30 || hour == 14 && min == 0) &&
														isGoodWeatherCondition(weatherDetail)) {
													if (prefs.notificationWeekendCall()) {
														PendingIntent pi = PendingIntent.getActivity(App.Instance,
																(int) System.currentTimeMillis(), i,
																PendingIntent.FLAG_ONE_SHOT);
														AppGuardService.this.notify(App.Instance.getString(
																R.string.notify_weekend_title), App.Instance.getString(
																R.string.notify_weekend_content,
																weatherDetail.getDescription(), temp), pi);
													}
												}
											} else {
												if (month >= Calendar.NOVEMBER && month <= Calendar.FEBRUARY) {
													//Fall ~ Winter
													if (hour == 17 && min == 0 &&
															isGoodWeatherCondition(weatherDetail)) {
														if (prefs.notificationWarmTips()) {
															PendingIntent pi = PendingIntent.getActivity(App.Instance,
																	(int) System.currentTimeMillis(), i,
																	PendingIntent.FLAG_ONE_SHOT);
															AppGuardService.this.notify(App.Instance.getString(
																			R.string.notify_warm_tips_title),
																	App.Instance.getString(
																			R.string.notify_weekend_content,
																			weatherDetail.getDescription(), temp), pi);
														}
													}
												} else if (month >= Calendar.MARCH && month <= Calendar.MAY) {
													//Spring
													if (hour == 17 && min == 30 &&
															(isGoodWeatherCondition(weatherDetail))) {
														if (prefs.notificationWarmTips()) {
															PendingIntent pi = PendingIntent.getActivity(App.Instance,
																	(int) System.currentTimeMillis(), i,
																	PendingIntent.FLAG_ONE_SHOT);
															AppGuardService.this.notify(App.Instance.getString(
																			R.string.notify_warm_tips_title),
																	App.Instance.getString(
																			R.string.notify_weekend_content,
																			weatherDetail.getDescription(), temp), pi);
														}
													}
												} else if (month >= Calendar.JUNE && month <= Calendar.OCTOBER) {
													//Summer
													if (hour == 18 && min == 30 &&
															(isGoodWeatherCondition(weatherDetail))) {
														if (prefs.notificationWarmTips()) {
															PendingIntent pi = PendingIntent.getActivity(App.Instance,
																	(int) System.currentTimeMillis(), i,
																	PendingIntent.FLAG_ONE_SHOT);
															AppGuardService.this.notify(App.Instance.getString(
																			R.string.notify_warm_tips_title),
																	App.Instance.getString(
																			R.string.notify_weekend_content,
																			weatherDetail.getDescription(), temp), pi);
														}
													}
												}
											}
										}
									}
								}

								@Override
								public void failure(RetrofitError error) {

								}
							});
				} catch (ApiNotInitializedException e) {
					//Ignore this request.
				}
			}
		}
	};


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Utils.showShortToast(this, "AppGuardService");
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		registerReceiver(mReceiver, mIntentFilter);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	/**
	 * Notify user.
	 */
	private void notify(String title, String desc, PendingIntent contentIntent) {
		mNotifyBuilder = new NotificationCompat.Builder(this).setWhen(System.currentTimeMillis()).setSmallIcon(
				R.drawable.ic_balloon).setTicker(title).setContentTitle(title).setContentText(desc).setStyle(
				new BigTextStyle().bigText(desc).setBigContentTitle(title).setSummaryText(desc)).setAutoCancel(true);
		mNotifyBuilder.setContentIntent(contentIntent);
		Utils.vibrateSound(this, mNotifyBuilder);
		mNotificationManager.notify((int) System.currentTimeMillis(), mNotifyBuilder.build());
	}


	private boolean isGoodWeatherCondition(WeatherDetail weatherDetail) {
		return weatherDetail.getId() == 800 || weatherDetail.getId() == 801 || weatherDetail.getId() == 802|| weatherDetail.getId() == 803;
	}
}