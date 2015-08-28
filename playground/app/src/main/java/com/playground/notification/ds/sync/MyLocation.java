package com.playground.notification.ds.sync;

import com.playground.notification.ds.Playground;

public final class MyLocation extends SyncPlayground {
	private String mLabel;

	public MyLocation(String uid, String label, Playground ground) {
		super(uid, ground);
		mLabel = label;
	}


	public String getLabel() {
		return mLabel;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		Playground other = (Playground) o;
		return getLatitude() == other.getLatitude() && getLongitude() == other.getLongitude();
	}
}
