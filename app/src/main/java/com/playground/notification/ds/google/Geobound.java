package com.playground.notification.ds.google;


import com.google.gson.annotations.SerializedName;

public final class Geobound {
	@SerializedName("northeast")
	private Geolocation mNortheast;
	@SerializedName("southwest")
	private Geolocation mSouthwest;

	public Geolocation getNortheast() {
		return mNortheast;
	}

	public Geolocation getSouthwest() {
		return mSouthwest;
	}
}
