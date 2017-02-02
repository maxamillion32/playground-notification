package com.playground.notification.app.adapters;


import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.playground.notification.app.App;
import com.playground.notification.ds.grounds.Playground;
import com.playground.notification.utils.Prefs;

public final class BinderAdapter {
	private BinderAdapter() {

	}

	@BindingAdapter({ "bind:playground" })
	public static void loadImage(ImageView imageView, Playground playground) {
		String fallback = "http://tomatofish.com/wp-content/uploads/2013/08/placeholder-tomatofish.jpg";
		if (playground == null) {
			Glide.with(App.Instance)
			     .load(fallback)
			     .into(imageView);
			return;
		}

		try {
			Prefs prefs = Prefs.getInstance();
			String latlng = playground.getLatitude() + "," + playground.getLongitude();
			String maptype = prefs.getMapType()
			                      .equals("0") ?
			                 "roadmap" :
			                 "hybrid";
			String url = prefs.getGoogleApiHost() + "maps/api/staticmap?center=" + latlng + "&zoom=16&size=" + prefs.getDetailPreviewSize() + "&markers=color:red%7Clabel:S%7C" + latlng + "&key=" +
					App.Instance.getDistanceMatrixKey() + "&sensor=true&maptype=" + maptype;
			Glide.with(App.Instance)
			     .load(url)
			     .into(imageView);
		} catch (Exception e) {
			Glide.with(App.Instance)
			     .load(fallback)
			     .into(imageView);
		}
	}
}
