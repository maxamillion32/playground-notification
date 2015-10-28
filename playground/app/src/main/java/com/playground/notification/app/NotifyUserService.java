package com.playground.notification.app;

import java.util.List;
import java.util.Locale;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
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
public class NotifyUserService extends IntentService implements LocationListener {
	public static final String EXTRAS_WEEKEND = NotifyUserService.class.getName() + ".EXTRAS.WEEKEND";
	/**
	 * Connect to google-api.
	 */
	private GoogleApiClient mGoogleApiClient;
	/**
	 * Ask current location.
	 */
	private LocationRequest mLocationRequest;
	private boolean mWeekendNotify = false;

	private NotificationManager mNotificationManager;


	public NotifyUserService() {
		super("AppGuardService");
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		mWeekendNotify = intent.getBooleanExtra(EXTRAS_WEEKEND, false);
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		startLocating();
	}


	/**
	 * Notify user.
	 */
	private void notify(String title, String desc, PendingIntent contentIntent) {
		android.support.v4.app.NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this).setWhen(
				System.currentTimeMillis()).setSmallIcon(R.drawable.ic_balloon).setTicker(title).setContentTitle(title)
				.setContentText(desc).setStyle(new BigTextStyle().bigText(desc).setBigContentTitle(title)
						.setSummaryText(desc)).setAutoCancel(true);
		notifyBuilder.setContentIntent(contentIntent);
		Utils.vibrateSound(this, notifyBuilder);
		mNotificationManager.notify((int) System.currentTimeMillis(), notifyBuilder.build());
	}


	private boolean isGoodWeatherCondition(WeatherDetail weatherDetail) {
		return weatherDetail.getId() == 800 || weatherDetail.getId() == 801 || weatherDetail.getId() == 802 ||
				weatherDetail.getId() == 803;
	}


	private void startLocating() {
		//Location request.
		if (mLocationRequest == null) {
			mLocationRequest = LocationRequest.create();
			mLocationRequest.setInterval(300);
			mLocationRequest.setFastestInterval(300);
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
							if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && mLocationRequest != null) {
								LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, NotifyUserService.this);
							}
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
									if (isGoodWeatherCondition(weatherDetail)) {
										PendingIntent pi = PendingIntent.getActivity(App.Instance,
												(int) System.currentTimeMillis(), i, PendingIntent.FLAG_UPDATE_CURRENT);
										NotifyUserService.this.notify(title, App.Instance.getString(
												R.string.notify_content, weatherDetail.getDescription(), temp), pi);
									}
								}
							}
							//Ignore...
						}

						@Override
						public void failure(RetrofitError error) {
							//Ignore...
						}
					});
		} catch (ApiNotInitializedException e) {
			//Ignore...
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
