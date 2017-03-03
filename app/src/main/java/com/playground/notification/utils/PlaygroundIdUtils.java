package com.playground.notification.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.playground.notification.ds.grounds.Playground;

/**
 * Because not all official grounds on the map have a real ID, we need
 * logical to return or emit the ID.
 *
 * @author Xinyue Zhao
 */
public final class PlaygroundIdUtils {
	public static String getId(@NonNull Playground playground) {
		if (TextUtils.isEmpty(playground.getId())) {
			return makeId(playground);
		} else {
			return playground.getId();
		}
	}

	private static String makeId(@NonNull Playground playground) {
		return "my_" + playground.getLatitude() + "," + playground.getLongitude();
	}
}
