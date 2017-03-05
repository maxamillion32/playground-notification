package com.playground.notification.bus;


import com.playground.notification.ds.grounds.Playground;

public final class PinSelectedEvent {
	private final Playground mPlayground;


	public PinSelectedEvent(Playground playground) {
		mPlayground = playground;
	}


	public Playground getPlayground() {
		return mPlayground;
	}
}
