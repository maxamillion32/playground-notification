package com.playground.notification.ds;


import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

import cn.bmob.v3.BmobObject;

public class Playground extends BmobObject implements Serializable {
	@SerializedName("id")
	private String mId;
	@SerializedName("lat")
	private double mLatitude;
	@SerializedName("lon")
	private double mLongitude;

	public Playground(String id, double latitude, double longitude) {
		mId = id;
		mLatitude = latitude;
		mLongitude = longitude;
	}

	public String getId() {
		return mId;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	@Override
	public boolean equals(Object o) {
		Playground other = (Playground) o;
		return getId().equals(other.getId());
	}
}
