package com.playground.notification.ds.google;


import com.google.gson.annotations.SerializedName;

public final class Geolocation {
	@SerializedName("lat")
	private double mLatitude;
	@SerializedName("lng")
	private double mLongitude;

	public double getLatitude() {
		return mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}
}
