package com.playground.notification.ds.weather;


import java.util.List;

import com.google.gson.annotations.SerializedName;

public final class Weather {
	@SerializedName("weather")
	private List<WeatherDetail> mDetails;
	@SerializedName("main")
	private Temperature         mTemperature;
	@SerializedName("wind")
	private Wind                mWind;

	public List<WeatherDetail> getDetails() {
		return mDetails;
	}

	public Temperature getTemperature() {
		return mTemperature;
	}

	public Wind getWind() {
		return mWind;
	}
}
