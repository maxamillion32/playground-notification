package com.playground.notification.app.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.google.android.gms.maps.model.LatLng;
import com.playground.notification.app.App;
import com.playground.notification.app.fragments.PlaygroundDetailFragment;
import com.playground.notification.ds.sync.SyncPlayground;

import java.util.List;

/**
 * Adapter for viewpager that shows sync-grounds.
 *
 * @author Xinyue Zhao
 */
public final class SyncPlaygroundsPagerAdapter extends FragmentPagerAdapter {
	private List<? extends SyncPlayground> mGrounds;
	private LatLng                         mFrom;

	public SyncPlaygroundsPagerAdapter( LatLng from, List<? extends SyncPlayground> grounds, FragmentManager fm ) {
		super( fm );
		mFrom = from;
		mGrounds = grounds;
	}

	@Override
	public Fragment getItem( int position ) {
		return PlaygroundDetailFragment.newInstance( App.Instance, mFrom.latitude, mFrom.longitude, mGrounds.get( position ), true );
	}

	@Override
	public int getCount() {
		return mGrounds == null ? 0 : mGrounds.size();
	}
}
