package com.playground.notification.bus;

/**
 * Select on item on list when action-mode.
 *
 * @author Xinyue Zhao
 */
public final class SelectItemEvent {
	private int mPosition;

	public SelectItemEvent(int position) {
		mPosition = position;
	}


	public int getPosition() {
		return mPosition;
	}
}
