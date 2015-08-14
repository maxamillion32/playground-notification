package com.playground.notification.ds;


import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

public final class Playground implements Serializable{
	@SerializedName("id")
	private String mId;
	@SerializedName("lat")
	private double mLatitude;
	@SerializedName("lon")
	private double mLongitude;

	public String getId() {
		return mId;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}
}
