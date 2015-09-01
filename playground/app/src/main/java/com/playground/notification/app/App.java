/*
                   _ooOoo_
                  o8888888o
                  88" . "88
                  (| -_- |)
                  O\  =  /O
               ____/`---'\____
             .'  \\|     |//  `.
            /  \\|||  :  |||//  \
           /  _||||| -:- |||||-  \
           |   | \\\  -  /// |   |
           | \_|  ''\---/''  |   |
           \  .-\__  `-`  ___/-. /
         ___`. .'  /--.--\  `. . __
      ."" '<  `.___\_<|>_/___.'  >'"".
     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
     \  \ `-.   \_ __\ /__ _/   .-` /  /
======`-.____`-.___\_____/___.-`____.-'======
                   `=---='
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         佛祖保佑       永无BUG
*/
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱。

package com.playground.notification.app;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.chopping.net.TaskHelper;
import com.chopping.utils.DeviceUtils;
import com.chopping.utils.DeviceUtils.ScreenSize;
import com.crashlytics.android.Crashlytics;
import com.playground.notification.R;
import com.playground.notification.api.Api;
import com.playground.notification.api.ApiNotInitializedException;
import com.playground.notification.app.activities.MapsActivity;
import com.playground.notification.ds.weather.Weather;
import com.playground.notification.ds.weather.WeatherDetail;
import com.playground.notification.utils.Prefs;
import com.playground.notification.utils.Utils;

import cn.bmob.v3.Bmob;
import io.fabric.sdk.android.Fabric;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * The app-object of the project.
 *
 * @author Xinyue Zhao
 */
public final class App extends MultiDexApplication {

	private NotificationManager mNotificationManager;
	private android.support.v4.app.NotificationCompat.Builder mNotifyBuilder;

	/**
	 * Current position.
	 */
	private Location mCurrentLocation;
	/**
	 * Application's instance.
	 */
	public static App Instance;

	{
		Instance = this;
	}

	/**
	 * Display-size.
	 */
	private ScreenSize mScreenSize;


	/**
	 * API key for requiring distance-matrix.
	 */
	private String mDistanceMatrixKey;
	/**
	 * API key for weather-API.
	 */
	private String mWeatherKey;

	//----------------------------------------------------------
	// Description: A receiver for system time-tick.
	//
	// Impl of Hungry mod.
	//----------------------------------------------------------
	/**
	 * We wanna every-minute-event, this is the {@link android.content.IntentFilter} for every minute from system.
	 */
	private IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
	/**
	 * We wanna event to handle for every minute, this is the {@link android.content.BroadcastReceiver} for every minute
	 * from system.
	 */
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context cxt, Intent intent) {
			Prefs prefs = Prefs.getInstance();
			if (!prefs.isEULAOnceConfirmed()) {
				return;
			}
			if (getCurrentLocation() != null) {
				String units = "metric";
				switch (prefs.getWeatherUnitsType()) {
				case "0":
					units = "metric";
					break;
				case "1":
					units = "imperial";
					break;
				}
				Location location = getCurrentLocation();
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
														App.Instance.notify(App.Instance.getString(
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
															App.Instance.notify(App.Instance.getString(
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
															App.Instance.notify(App.Instance.getString(
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
															App.Instance.notify(App.Instance.getString(
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

	private boolean isGoodWeatherCondition(WeatherDetail weatherDetail) {
		return weatherDetail.getId() == 800 || weatherDetail.getId() == 801;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		Fabric.with(this, new Crashlytics());
		//		Stetho.initialize(Stetho.newInitializerBuilder(this).enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
		//				.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this)).build());

		Properties prop = new Properties();
		try {
			prop.load(getClassLoader().getResourceAsStream("key.properties"));
			Bmob.initialize(this, prop.getProperty("bmobkey"));
			mDistanceMatrixKey = prop.getProperty("distancematrixkey");
			mWeatherKey = prop.getProperty("weather_key");
		} catch (IOException e) {
			e.printStackTrace();
		}
		TaskHelper.init(getApplicationContext());
		Prefs.createInstance(this);

		//Short the link of app-download and make a download-info, store to preference.
		//You'll see text like
		//<code>
		//			Download: http://tinyurl/asdfasdf
		//</code>
		//in sharing text.
		String url = Prefs.getInstance().getAppDownloadInfo();
		if (TextUtils.isEmpty(url) || !url.contains("tinyurl")) {
			com.tinyurl4j.Api.getTinyUrl(getString(R.string.lbl_store_url, getPackageName()),
					new Callback<com.tinyurl4j.data.Response>() {
						@Override
						public void success(com.tinyurl4j.data.Response response, retrofit.client.Response response2) {
							Prefs.getInstance().setAppDownloadInfo(getString(R.string.lbl_share_download_app, getString(
									R.string.application_name), response.getResult()));
						}

						@Override
						public void failure(RetrofitError error) {
							Prefs.getInstance().setAppDownloadInfo(getString(R.string.lbl_share_download_app, getString(
									R.string.application_name), getString(R.string.lbl_store_url, getPackageName())));
						}
					});
		}
		mScreenSize = DeviceUtils.getScreenSize(this);
		registerReceiver(mReceiver, mIntentFilter);
	}

	/**
	 * API key for requiring distance-matrix.
	 */
	public String getDistanceMatrixKey() {
		return mDistanceMatrixKey;
	}


	/**
	 * @return Display-size.
	 */
	public ScreenSize getScreenSize() {
		return mScreenSize;
	}

	/**
	 * Current position.
	 */
	public synchronized Location getCurrentLocation() {
		return mCurrentLocation;
	}

	/**
	 * Current position.
	 */
	public synchronized void setCurrentLocation(Location currentLocation) {
		mCurrentLocation = currentLocation;
	}

	/**
	 * API key for weather-API.
	 */
	public String getWeatherKey() {
		return mWeatherKey;
	}

	/**
	 * Notify user.
	 */
	private void notify(String title, String desc, PendingIntent contentIntent) {
		Utils.vibrateSound(this, mNotifyBuilder);
		mNotifyBuilder = new NotificationCompat.Builder(this).setWhen(System.currentTimeMillis()).setSmallIcon(
				R.drawable.ic_balloon).setTicker(title).setContentTitle(title).setContentText(desc).setStyle(
				new BigTextStyle().bigText(desc).setBigContentTitle(title).setSummaryText(desc)).setAutoCancel(true);
		mNotifyBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify((int) System.currentTimeMillis(), mNotifyBuilder.build());
	}
}
