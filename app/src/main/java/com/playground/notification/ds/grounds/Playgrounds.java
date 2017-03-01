package com.playground.notification.ds.grounds;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class Playgrounds {
	@SerializedName("result")
	private List<Playground> mPlaygroundList;

	public List<Playground> getPlaygroundList() {
		return mPlaygroundList;
	}
}
