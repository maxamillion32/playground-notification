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

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.ds.grounds.Playground;
import com.playground.notification.ds.sync.MyLocation;
import com.playground.notification.sync.FavoriteManager;
import com.playground.notification.sync.NearRingManager;
import com.playground.notification.utils.Prefs;


/**
 * Custom renderer to use the app's styled markers.
 */
final class PlaygroundClusterRenderer extends DefaultClusterRenderer<Playground> {
	private final @NonNull GoogleMap mGoogleMap;
	private final Context mContext;

	PlaygroundClusterRenderer(Context cxt, @NonNull GoogleMap map, @NonNull ClusterManager<Playground> clusterManager) {
		super(cxt, map, clusterManager);
		mContext = cxt;
		mGoogleMap = map;
	}

	@Override
	protected boolean shouldRenderAsCluster(Cluster<Playground> cluster) {
		return cluster.getSize() >= Prefs.getInstance().getClusterLimit();
	}

	@Override
	protected void onBeforeClusterItemRendered(Playground playground, MarkerOptions options) {
		Location location = App.Instance.getCurrentLocation();
		final LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		LatLng to = new LatLng(playground.getLatitude(), playground.getLongitude());
		//Draw different markers, for fav , for normal ground, for grounds in near-rings.
		options.position(to);

		if (playground instanceof MyLocation) {
			options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_saved_ground));
		} else {
			FavoriteManager favMgr = FavoriteManager.getInstance();
			if (favMgr.isInit() && favMgr.isCached(playground)) {
				options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_favorited));
			} else {
				com.playground.notification.utils.Utils.changeMarkerIcon(options, currentLatLng, to);
			}
		}

		//Geofence-ring.
		NearRingManager nearRingMgr = NearRingManager.getInstance();
		if (nearRingMgr.isInit() && nearRingMgr.isCached(playground)) {
			mGoogleMap.addCircle(new CircleOptions().center(playground.getPosition())
			                                        .radius(Prefs.getInstance()
			                                                     .getAlarmArea())
			                                        .strokeWidth(1)
			                                        .strokeColor(Color.BLUE)
			                                        .fillColor(ResourcesCompat.getColor(mContext.getResources(), R.color.common_blue_50, null)));
		}
	}
}
