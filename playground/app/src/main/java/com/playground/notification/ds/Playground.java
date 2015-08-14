package com.playground.notification.ds;


import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public final class Playground implements Serializable{
	@SerializedName("lat")
	private double mLatitude;
	@SerializedName("lon")
	private double mLongitude;

	public double getLatitude() {
		return mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}
}
