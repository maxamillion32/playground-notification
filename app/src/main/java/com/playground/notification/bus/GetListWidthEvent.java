package com.playground.notification.bus;


public final class GetListWidthEvent {
	private int mWidth;

	public GetListWidthEvent(int width) {
		mWidth = width;
	}


	public int getWidth() {
		return mWidth;
	}
}
