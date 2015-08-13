package com.playground.notification.ds;


import java.util.List;

import com.google.gson.annotations.SerializedName;

public final class Playgrounds {
	@SerializedName("result")
	private List<Playground> mPlaygroundList;

	public List<Playground> getPlaygroundList() {
		return mPlaygroundList;
	}
}
