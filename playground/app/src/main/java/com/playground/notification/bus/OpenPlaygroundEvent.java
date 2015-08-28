package com.playground.notification.bus;


import com.playground.notification.ds.Playground;

public final class OpenPlaygroundEvent {
	private Playground mPlayground;

	public OpenPlaygroundEvent(Playground playground) {
		mPlayground = playground;
	}

	public Playground getPlayground() {
		return mPlayground;
	}
}
