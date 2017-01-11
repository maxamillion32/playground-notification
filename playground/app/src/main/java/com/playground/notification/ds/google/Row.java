package com.playground.notification.ds.google;


import java.util.List;

import com.google.gson.annotations.SerializedName;

public final class Row {
	@SerializedName("elements")
	private List<Element> mElements;

	public List<Element> getElements() {
		return mElements;
	}
}
