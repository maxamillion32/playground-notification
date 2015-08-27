package com.playground.notification.ds.sync;

import com.playground.notification.ds.Playground;

public final class MyLocation extends SyncPlayground {
	private String mName;

	public MyLocation(String uid, Playground ground) {
		super(uid, ground);
	}

	public MyLocation(Playground ground) {
		super(ground);
	}


	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}
}
