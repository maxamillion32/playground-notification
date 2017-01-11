package com.playground.notification.ds.sync;


import com.playground.notification.ds.grounds.Playground;

public final class Rating extends SyncPlayground {
	private float mValue;

	public Rating( String uid, Playground ground ) {
		super( uid, ground );
	}

	public Rating( Playground ground ) {
		super( ground );
	}

	public float getValue() {
		return mValue;
	}

	public void setValue( float value ) {
		mValue = value;
	}
}
