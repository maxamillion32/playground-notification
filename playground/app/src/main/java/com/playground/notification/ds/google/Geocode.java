package com.playground.notification.ds.google;


import com.google.gson.annotations.SerializedName;

public final class Geocode {
	@SerializedName("formatted_address")
	private String   mAddress;
	@SerializedName("geometry")
	private Geometry mGeometry;

	public String getAddress() {
		return mAddress;
	}

	public Geometry getGeometry() {
		return mGeometry;
	}

	@Override
	public String toString() {
		return getAddress();
	}
}
