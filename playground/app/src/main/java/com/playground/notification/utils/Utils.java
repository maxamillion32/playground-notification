package com.playground.notification.utils;

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

	public static void changeMarkerIcon(MarkerOptions marker, LatLng center, LatLng to) {
		synchronized (TAG) {
			float[] results = new float[1];
			android.location.Location.distanceBetween(center.latitude, center.longitude, to.latitude, to.longitude,
					results);
			float distance = results[0];
			if (results.length > 0) {
				if (distance <= 100) {
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_100));
				} else if (distance <= 200 && distance > 100) {
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_200));
				} else if (distance <= 300 && distance > 200) {
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_300));
				} else if (distance <= 400 && distance > 300) {
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_400));
				} else if (distance <= 500 && distance > 400) {
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_500));
				} else if (distance <= 600 && distance > 500) {
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_600));
				} else {
					marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_more));
				}
			} else {
				marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_more));
			}
		}
	}
}
