package com.playground.notification.ds.grounds;


import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;
import com.google.maps.android.clustering.ClusterItem;

import cn.bmob.v3.BmobObject;

public class Playground extends BmobObject implements ClusterItem {
	@SerializedName(value = "mId", alternate = { "id" }) private String mId;
	@SerializedName(value = "mLatitude", alternate = { "lat" }) private double mLatitude;
	@SerializedName(value = "mLongitude", alternate = { "lon" }) private double mLongitude;

	public Playground(String id, double latitude, double longitude) {
		mId = id;
		mLatitude = latitude;
		mLongitude = longitude;
	}

	public Playground(double latitude, double longitude) {
		mLatitude = latitude;
		mLongitude = longitude;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
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
			if (o == null) {
				return false;
			}
			Playground other = (Playground) o;
			return !TextUtils.isEmpty(other.getId()) && getId().equals(other.getId());
		} catch (NullPointerException e) {
			return false;
		}
	}

	@Override
	public LatLng getPosition() {
		return new LatLng(mLatitude, mLongitude);
	}

	@Override
	public String getTitle() {
		return "[" + mLatitude + "," + mLongitude + "]";
	}

	@Override
	public String getSnippet() {
		return "[" + mLatitude + "," + mLongitude + "]";
	}
}
