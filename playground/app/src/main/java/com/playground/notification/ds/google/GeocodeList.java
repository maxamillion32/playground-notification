package com.playground.notification.ds.google;


import java.util.List;

import com.google.gson.annotations.SerializedName;

public final class GeocodeList {
	@SerializedName("results")
	private List<Geocode> mGeocodeList;
	@SerializedName("status")
	private String        mStatus;

	public List<Geocode> getGeocodeList() {
		return mGeocodeList;
	}

	public String getStatus() {
		return mStatus;
	}
}
