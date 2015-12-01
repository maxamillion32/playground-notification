package com.playground.notification.ds.sync;

import com.playground.notification.ds.grounds.Playground;

public abstract class SyncPlayground extends Playground {
	private String mUID;

	public SyncPlayground( String uid, Playground ground ) {
		super( ground.getId(), ground.getLatitude(), ground.getLongitude() );
		mUID = uid;
	}

	public SyncPlayground( Playground ground ) {
		super( ground.getId(), ground.getLatitude(), ground.getLongitude() );
	}
}
