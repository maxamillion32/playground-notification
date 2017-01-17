/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.playground.notification.map;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterManager;
import com.playground.notification.app.App;
import com.playground.notification.app.fragments.PlaygroundDetailFragment;
import com.playground.notification.ds.grounds.Playground;

import java.lang.ref.WeakReference;
import java.util.List;


public final class PlaygroundClusterManager extends ClusterManager<Playground> implements ClusterManager.OnClusterItemClickListener<Playground>  {
	private WeakReference<FragmentActivity> mHost;

	private PlaygroundClusterManager(@NonNull FragmentActivity host, @NonNull GoogleMap map) {
		super(host.getApplicationContext(), map, new MarkerManager(map));
		map.setOnMarkerClickListener(this);
		setRenderer(new PlaygroundClusterRenderer(host.getApplicationContext(), map, this));
		setOnClusterItemClickListener(this);
		mHost = new WeakReference<>(host);
	}

	public static PlaygroundClusterManager showAvailablePlaygrounds(@NonNull FragmentActivity host, @NonNull GoogleMap googleMap, List<Playground> exposes) {
		PlaygroundClusterManager ret = new PlaygroundClusterManager(host, googleMap);
		ret.addItems(exposes);
		return  ret;
	}


	@Override
	public boolean onClusterItemClick(Playground playground) {
		if (mHost.get() == null) {
			return false;
		}
		Location location = App.Instance.getCurrentLocation();
		LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		PlaygroundDetailFragment.newInstance(App.Instance, currentLatLng.latitude, currentLatLng.longitude, playground, false)
		                        .show(mHost.get()
		                                   .getSupportFragmentManager(), null);

		return true;
	}
}
