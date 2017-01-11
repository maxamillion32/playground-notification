package com.playground.notification.ds.google;


import com.google.gson.annotations.SerializedName;

public final class Element {
	@SerializedName("distance")
	private Distance mDistance;
	@SerializedName("duration")
	private Duration mDuration;


	public Distance getDistance() {
		return mDistance;
	}

	public Duration getDuration() {
		return mDuration;
	}
}
