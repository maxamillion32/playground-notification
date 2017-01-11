package com.playground.notification.ds.weather;

import com.google.gson.annotations.SerializedName;

public final class Temperature {
	@SerializedName("temp")
	private double mValue;

	public double getValue() {
		return mValue;
	}
}
