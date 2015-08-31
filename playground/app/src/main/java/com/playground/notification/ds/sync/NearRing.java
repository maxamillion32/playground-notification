package com.playground.notification.ds.sync;


import com.playground.notification.ds.grounds.Playground;

public final class NearRing extends SyncPlayground {
	public NearRing(String uid, Playground ground) {
		super(uid, ground);
	}

	public NearRing(Playground ground) {
		super(ground);
	}
}
