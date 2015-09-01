package com.playground.notification.ds.google;


import com.google.gson.annotations.SerializedName;

public final class Geometry {
	@SerializedName("bounds")
	private Geobound mBound;
	@SerializedName("location")
	private Geolocation mLocation;

	public Geobound getBound() {
		return mBound;
	}

	public Geolocation getLocation() {
		return mLocation;
	}
}
