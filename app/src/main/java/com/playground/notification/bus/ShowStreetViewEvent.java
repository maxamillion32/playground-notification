package com.playground.notification.bus;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public final class ShowStreetViewEvent {
	private @NonNull String mTitle;
	private @NonNull LatLng mLocation;

	public ShowStreetViewEvent(@NonNull String title, @NonNull LatLng location) {
		mTitle = title;
		mLocation = location;
	}


	@NonNull
	public String getTitle() {
		return mTitle;
	}

	@NonNull
	public LatLng getLocation() {
		return mLocation;
	}
}
