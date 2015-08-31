package com.playground.notification.bus;


import com.playground.notification.ds.grounds.Playground;

public final class ShowLocationRatingEvent {
	private Playground mPlayground;

	public ShowLocationRatingEvent(Playground ground) {
		mPlayground = ground;
	}

	public Playground getPlayground() {
		return mPlayground;
	}
}
