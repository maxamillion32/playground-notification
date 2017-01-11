package com.playground.notification.ds.sync;

import com.playground.notification.ds.grounds.Playground;

public final class MyLocation extends SyncPlayground {
	private String mLabel;

	public MyLocation( String uid, String label, Playground ground ) {
		super( uid, ground );
		mLabel = label;
	}


	public String getLabel() {
		return mLabel;
	}

	@Override
	public boolean equals( Object o ) {
		try {
			if( o == null ) {
				return false;
			}
			Playground other = (Playground) o;
			return getLatitude() == other.getLatitude() && getLongitude() == other.getLongitude();
		} catch( NullPointerException e ) {
			return false;
		}
	}
}
