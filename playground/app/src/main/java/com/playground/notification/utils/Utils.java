package com.playground.notification.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.playground.notification.R;

/**
 * Util-methods of application.
 *
 * @author Xinyue Zhao
 */
public final class Utils {
	private static final String TAG = Utils.class.getName();

	/**
	 * Show different marker-icon for different distance.
	 * @param options The the option of a marker.
	 * @param center Current center position.
	 * @param to The ground position.
	 */
	public static void changeMarkerIcon(MarkerOptions options, LatLng center, LatLng to) {
		synchronized (TAG) {
			float[] results = new float[1];
			android.location.Location.distanceBetween(center.latitude, center.longitude, to.latitude, to.longitude,
					results);
			float distance = results[0];
			if (results.length > 0) {
				if (distance <= 100) {
					options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_100));
				} else if (distance <= 200 && distance > 100) {
					options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_200));
				} else if (distance <= 300 && distance > 200) {
					options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_300));
				} else if (distance <= 400 && distance > 300) {
					options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_400));
				} else  {
					options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_500));
				}
			} else {
				options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_500));
			}
		}
	}


	/**
	 * Open a html page with external browser.
	 * @param cxt The {@link Context}.
	 * @param url The url to the site.
	 */
	public static void openExternalBrowser(Activity cxt, String url) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		ActivityCompat.startActivity(cxt, i, null);
	}

	/**
	 * Open Google's map to show two points.
	 * @param cxt {@link Context}.
	 * @param fromLatLng From point.
	 * @param toLatLng To point
	 */
	public static void openMapWeb(Context cxt, LatLng fromLatLng, LatLng toLatLng     ) {
		String q = new StringBuilder().append("http://maps.google.com/maps?").append("saddr=").append(fromLatLng.latitude + "," + fromLatLng.longitude)
				.append("&daddr=").append(toLatLng.latitude + "," + toLatLng.longitude).toString();
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(q.trim()));
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		cxt.startActivity(intent);
	}
}
