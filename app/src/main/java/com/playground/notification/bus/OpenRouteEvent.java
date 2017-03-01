package com.playground.notification.bus;


import android.content.Intent;

public final class OpenRouteEvent {
	private final Intent mIntent;

	public OpenRouteEvent(Intent intent) {
		mIntent = intent;
	}

	public Intent getIntent() {
		return mIntent;
	}
}
