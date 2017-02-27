package com.playground.notification.app.adapters;


import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.bus.GetListWidthEvent;
import com.playground.notification.ds.grounds.Playground;
import com.playground.notification.utils.Prefs;

import de.greenrobot.event.EventBus;

public final class BinderAdapter {
	private BinderAdapter() {

	}

	@BindingAdapter({ "bind:playground" })
	public static void loadImage(final ImageView imageView, Playground playground) {
		final boolean isLarge = !App.Instance.getResources()
		                                     .getBoolean(R.bool.is_small_screen);
		String fallback = "http://tomatofish.com/wp-content/uploads/2013/08/placeholder-tomatofish.jpg";
		if (playground == null) {
			if (isLarge) {
				Glide.with(App.Instance)
				     .load(fallback)
				     .skipMemoryCache(false)
				     .diskCacheStrategy(DiskCacheStrategy.ALL)
				     .into(new SimpleTarget<GlideDrawable>() {
					     @Override
					     public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
						     imageView.setImageDrawable(resource);
						     EventBus.getDefault()
						             .post(new GetListWidthEvent(resource.getIntrinsicWidth()));
					     }
				     });
			} else {
				Glide.with(App.Instance)
				     .load(fallback)
				     .skipMemoryCache(false)
				     .diskCacheStrategy(DiskCacheStrategy.ALL)
				     .into(imageView);
			}
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
			if (isLarge) {
				Glide.with(App.Instance)
				     .load(url)
				     .skipMemoryCache(false)
				     .fitCenter()
				     .diskCacheStrategy(DiskCacheStrategy.ALL)
				     .into(new SimpleTarget<GlideDrawable>() {
					     @Override
					     public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
						     imageView.setImageDrawable(resource);
						     EventBus.getDefault()
						             .post(new GetListWidthEvent(resource.getIntrinsicWidth()));
					     }
				     });
			} else {
				Glide.with(App.Instance)
				     .load(url)
				     .skipMemoryCache(false)
				     .fitCenter()
				     .diskCacheStrategy(DiskCacheStrategy.ALL)
				     .into(imageView);
			}
		} catch (Exception e) {
			if (isLarge) {
				Glide.with(App.Instance)
				     .load(fallback)
				     .skipMemoryCache(false)
				     .diskCacheStrategy(DiskCacheStrategy.ALL)
				     .into(new SimpleTarget<GlideDrawable>() {
					     @Override
					     public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
						     imageView.setImageDrawable(resource);
						     EventBus.getDefault()
						             .post(new GetListWidthEvent(resource.getIntrinsicWidth()));
					     }
				     });
			} else {
				Glide.with(App.Instance)
				     .load(fallback)
				     .skipMemoryCache(false)
				     .diskCacheStrategy(DiskCacheStrategy.ALL)
				     .into(imageView);
			}
		}
	}
}
