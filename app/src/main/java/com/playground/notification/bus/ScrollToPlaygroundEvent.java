package com.playground.notification.bus;


import com.playground.notification.ds.grounds.Playground;

public final class ScrollToPlaygroundEvent {
	private final Playground mPlayground;


	public ScrollToPlaygroundEvent(Playground playground) {
		mPlayground = playground;
	}


	public Playground getPlayground() {
		return mPlayground;
	}
}
