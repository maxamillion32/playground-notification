package com.playground.notification.ds.weather;


import com.google.gson.annotations.SerializedName;

public final class Wind {
	@SerializedName("deg")
	private float mDegree;

	public float getDegree() {
		return mDegree;
	}
}
