package com.playground.notification.bus;


import android.view.View;

import com.playground.notification.ds.grounds.Playground;

import java.lang.ref.WeakReference;

public final class OpenPlaygroundEvent {
	private Playground mPlayground;
	private int mPosition;
	private WeakReference<View> mSelectedV;

	public OpenPlaygroundEvent( Playground playground ) {
		mPlayground = playground;
	}

	public OpenPlaygroundEvent(Playground playground, int position, WeakReference<View> selectedV) {
		mPlayground = playground;
		mPosition = position;
		mSelectedV = selectedV;
	}

	public Playground getPlayground() {
		return mPlayground;
	}

	public WeakReference<View> getSelectedV() {
		return mSelectedV;
	}

	public int getPosition() {
		return mPosition;
	}
}
