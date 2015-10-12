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
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v7.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
public class AppGuardService extends Service implements LocationListener {
	/**
	 * Connect to google-api.
	 */
	private GoogleApiClient mGoogleApiClient;
	/**
	 * Ask current location.
	 */
	private LocationRequest mLocationRequest;
	/**
	 * Wake-Up device.
	 */
	private WakeLock mWakeLock;
	/**
	 * wakelock
	 */
	private static final String WAKELOCK_KEY = "LOCATING_SERVICE";

	private boolean mWeekendNotify = false;

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
			Calendar calendar = Calendar.getInstance();
			int month = calendar.get(Calendar.MONTH);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int min = calendar.get(Calendar.MINUTE);
			int day = calendar.get(Calendar.DAY_OF_WEEK);
			if (prefs.notificationWeekendCall() && (day == Calendar.SATURDAY || day == Calendar.SUNDAY)) {
				if ((hour == 9 && min == 30) || (hour == 12 && min == 0) || (hour == 14 && min == 30)) {
					mWeekendNotify = true;
					notifyUser();
				}
			}
			if (prefs.notificationWarmTips()) {
				if (month >= Calendar.NOVEMBER && month <= Calendar.FEBRUARY) {
					//Fall ~ Winter
					if ((hour == 15 && min == 0) || (hour == 15 && min == 15) ){
						mWeekendNotify = false;
						notifyUser();
					}
				} else if (month >= Calendar.MARCH && month <= Calendar.MAY) {
					//Spring
					if ((hour == 15 && min == 30) || (hour == 16 && min == 0)) {
						mWeekendNotify = false;
						notifyUser();
					}
				} else if (month >= Calendar.JUNE && month <= Calendar.OCTOBER) {
					//Summer
					if ((hour == 15 && min == 40) || (hour == 16 && min == 0)) {
						mWeekendNotify = false;
						notifyUser();
					}
				}
			}


		}
	};

	private void notifyUser() {
		if (mWakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_KEY);
		}
		mWakeLock.acquire();
		startLocating();
	}


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
		try {
			unregisterReceiver(mReceiver);
		} catch (RuntimeException ex) {
			//Ignore...
		}
		stopLocating();
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
		return weatherDetail.getId() == 800 || weatherDetail.getId() == 801 || weatherDetail.getId() == 802 ||
				weatherDetail.getId() == 803;
	}


	private void startLocating() {
		//Location request.
		if (mLocationRequest == null) {
			mLocationRequest = LocationRequest.create();
			mLocationRequest.setInterval(500);
			mLocationRequest.setFastestInterval(500);
			int ty = 0;
			switch (Prefs.getInstance().getBatteryLifeType()) {
			case "0":
				ty = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
				break;
			case "1":
				ty = LocationRequest.PRIORITY_HIGH_ACCURACY;
				break;
			case "2":
				ty = LocationRequest.PRIORITY_LOW_POWER;
				break;
			case "3":
				ty = LocationRequest.PRIORITY_NO_POWER;
				break;
			}
			mLocationRequest.setPriority(ty);
		}

		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(App.Instance).addApi(LocationServices.API)
					.addConnectionCallbacks(new ConnectionCallbacks() {
						@Override
						public void onConnected(Bundle bundle) {
							startLocationUpdate();
						}

						@Override
						public void onConnectionSuspended(int i) {
							com.chopping.utils.Utils.showShortToast(App.Instance, "onConnectionSuspended");

						}
					}).addOnConnectionFailedListener(new OnConnectionFailedListener() {
						@Override
						public void onConnectionFailed(ConnectionResult connectionResult) {
							com.chopping.utils.Utils.showShortToast(App.Instance,
									"onConnectionFailed: " + connectionResult.getErrorCode());
						}
					}).build();

			mGoogleApiClient.connect();
		}
	}


	private void startLocationUpdate() {
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && mLocationRequest != null) {
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Prefs prefs = Prefs.getInstance();
		if (!prefs.isEULAOnceConfirmed()) {
			return;
		}
		String units = "metric";
		switch (prefs.getWeatherUnitsType()) {
		case "0":
			units = "metric";
			break;
		case "1":
			units = "imperial";
			break;
		}
		try {
			Api.getWeather(location.getLatitude(), location.getLongitude(), Locale.getDefault().getLanguage(), units,
					App.Instance.getWeatherKey(), new Callback<Weather>() {
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

									Intent i = new Intent(App.Instance, MapsActivity.class);
									i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
									String title = mWeekendNotify ? App.Instance.getString(
											R.string.notify_weekend_title) : App.Instance.getString(
											R.string.notify_warm_tips_title);
									if ( isGoodWeatherCondition(weatherDetail)) {
										PendingIntent pi = PendingIntent.getActivity(App.Instance,
												(int) System.currentTimeMillis(), i, PendingIntent.FLAG_ONE_SHOT);
										AppGuardService.this.notify(title, App.Instance.getString(
												R.string.notify_content, weatherDetail.getDescription(), temp), pi);
									}
								}
							}

							if (mWakeLock != null) {
								mWakeLock.release();
							}
						}

						@Override
						public void failure(RetrofitError error) {
							if (mWakeLock != null) {
								mWakeLock.release();
							}
						}
					});
		} catch (ApiNotInitializedException e) {
			if (mWakeLock != null) {
				mWakeLock.release();
			}
		} finally {
			stopLocating();
		}
	}


	private void stopLocating() {
		if (mGoogleApiClient != null) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
			mGoogleApiClient = null;
		}
		if (mLocationRequest != null) {
			mLocationRequest = null;
		}
	}
}
