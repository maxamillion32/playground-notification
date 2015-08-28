package com.playground.notification.ds;


import java.io.Serializable;

import android.text.TextUtils;

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

	public Playground( double latitude, double longitude) {
		mLatitude = latitude;
		mLongitude = longitude;
	}

	public void setId(String id) {
		mId = id;
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
		try {
			if (o == null)
				return false;
			Playground other = (Playground) o;
			return !TextUtils.isEmpty(other.getId()) && getId().equals(other.getId());
		} catch (NullPointerException e) {
			return false;
		}
	}
}
