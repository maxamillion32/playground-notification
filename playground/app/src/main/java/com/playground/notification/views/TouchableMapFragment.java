package com.playground.notification.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * A   map class to handle touch&movement effect.
 *
 * @author Xinyue Zhao
 */
public final class TouchableMapFragment extends SupportMapFragment {
	public View mOriginalContentView;
	public TouchableWrapper mTouchView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
		mTouchView = new TouchableWrapper(getActivity());
		mTouchView.addView(mOriginalContentView);
		return mTouchView;
	}

	@Override
	public View getView() {
		return mOriginalContentView;
	}

	public boolean isTouchAndMove() {
		return mTouchView.isTouchAndMove();
	}
}