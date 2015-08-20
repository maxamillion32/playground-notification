package com.playground.notification.ds;


public final class Favorite extends Playground {
	private String mUID;

	public Favorite(String uid, Playground ground) {
		super(ground.getId(), ground.getLatitude(), ground.getLongitude());
		mUID = uid;
	}

	public Favorite(Playground ground) {
		super(ground.getId(), ground.getLatitude(), ground.getLongitude());
	}

	public String getUID() {
		return mUID;
	}


	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}
}
