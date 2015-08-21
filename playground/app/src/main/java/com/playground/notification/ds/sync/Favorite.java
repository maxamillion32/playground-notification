package com.playground.notification.ds.sync;


import com.playground.notification.ds.Playground;

public final class Favorite extends SyncPlayground {
	public Favorite(String uid, Playground ground) {
		super(uid, ground);
	}

	public Favorite(Playground ground) {
		super(ground);
	}
}
