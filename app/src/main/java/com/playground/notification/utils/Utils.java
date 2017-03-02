package com.playground.notification.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.content.res.AppCompatResources;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.ds.grounds.Playground;
import com.playground.notification.ds.sync.Rating;
import com.playground.notification.sync.SyncManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;

/**
 * Util-methods of application.
 *
 * @author Xinyue Zhao
 */
public final class Utils {
	private static final String TAG = Utils.class.getName();

	/**
	 * Show different marker-icon for different distance.
	 *
	 * @param options The the option of a marker.
	 * @param center  Current center position.
	 * @param to      The ground position.
	 */
	public static void changeMarkerIcon(MarkerOptions options, LatLng center, LatLng to) {
		synchronized (TAG) {
			float[] results = new float[1];
			android.location.Location.distanceBetween(center.latitude, center.longitude, to.latitude, to.longitude, results);
			float distance = results[0];
			if (results.length > 0) {
				if (distance <= 100) {
					options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_100));
				} else if (distance <= 200 && distance > 100) {
					options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_200));
				} else if (distance <= 300 && distance > 200) {
					options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_300));
				} else if (distance <= 400 && distance > 300) {
					options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_400));
				} else {
					options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_500));
				}
			} else {
				options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_500));
			}
		}
	}


	/**
	 * Open a html page with external browser.
	 *
	 * @param cxt The {@link Context}.
	 * @param url The url to the site.
	 */
	public static void openExternalBrowser(Activity cxt, String url) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		ActivityCompat.startActivity(cxt, i, null);
	}

	/**
	 * Open Google's map to show two points.
	 *
	 * @param fromLatLng From point.
	 * @param toLatLng   To point
	 */
	public static Intent getMapWeb(LatLng fromLatLng, LatLng toLatLng) {
		String q = new StringBuilder().append("http://maps.google.com/maps?")
		                              .append("saddr=")
		                              .append(fromLatLng.latitude + "," + fromLatLng.longitude)
		                              .append("&daddr=")
		                              .append(toLatLng.latitude + "," + toLatLng.longitude)
		                              .toString();
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(q.trim()));
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}


	/**
	 * Share information by calling standards of system.
	 */
	public static Intent getShareInformation(String subject, String body) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		i.putExtra(android.content.Intent.EXTRA_TEXT, body);
		return i;
	}

	/**
	 * Vibrate and make sound.
	 */
	public static void vibrateSound(Context cxt, android.support.v4.app.NotificationCompat.Builder notifyBuilder) {
		AudioManager audioManager = (AudioManager) cxt.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
			notifyBuilder.setVibrate(new long[] { 1000,
			                                      1000,
			                                      1000,
			                                      1000 });
			notifyBuilder.setSound(Uri.parse(String.format("android.resource://%s/%s", cxt.getPackageName(), R.raw.signal)));
		}
		notifyBuilder.setLights(cxt.getResources()
		                           .getColor(R.color.primary_color), 1000, 1000);
	}

	/**
	 * Test whether the {@link String} is  valid value or not, if invalidate, shakes it.
	 */
	public static boolean validateStr(Context cxt, String s) {
		boolean val;
		if (s.matches(".*[/=():;].*")) {
			val = false;
			com.chopping.utils.Utils.showLongToast(cxt, R.string.lbl_exclude_chars);
		} else {
			val = true;
		}
		return val;
	}

	public static boolean streetViewBitmapHasRealContent(@NonNull Bitmap bitmap) {
		if (bitmap == null || bitmap.isRecycled()) {
			return false;
		}

		int color = bitmap.getPixel(0, 0);
		for (int i = 0;
				i < bitmap.getWidth();
				i++) {

			int compareColor = bitmap.getPixel(i, 0);
			if (compareColor != color) {
				return true;
			}

		}
		return false;

	}


	/**
	 * Helper to update menu-titles on drawer.
	 */
	public static void updateDrawerMenuItem(NavigationView niv, int itemResId, int itemTitleResId, SyncManager mgr) {
		if (mgr.isInit()) {
			niv.getMenu()
			   .findItem(itemResId)
			   .setTitle(App.Instance.getString(itemTitleResId,
			                                    mgr.getCachedList()
			                                       .size()));
		} else {
			niv.getMenu()
			   .findItem(itemResId)
			   .setTitle(App.Instance.getString(itemTitleResId, 0));
		}
	}


	public static BitmapDescriptor getBitmapDescriptor(Context cxt, int resId) {
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			return BitmapDescriptorFactory.fromResource(resId);
		}
		Drawable drawable = AppCompatResources.getDrawable(cxt, resId);
		Canvas canvas = new Canvas();
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return BitmapDescriptorFactory.fromBitmap(bitmap);
	}


	public static void showAllRating(Playground playground, final RatingUI ratingUI) {
		showPersonalRatingOnLocation(playground, ratingUI);
		showRatingSummaryOnLocation(playground, ratingUI);
	}


	public static void showRatingSummaryOnLocation(Playground playground, final RatingUI ratingUI) {
		BmobQuery q = new BmobQuery<>();
		q.setCachePolicy(BmobQuery.CachePolicy.CACHE_THEN_NETWORK);
		q.addWhereEqualTo("mId", playground.getId());
		q.average(new String[] { "mValue" });
		q.findStatistics(Rating.class, new QueryListener<JSONArray>() {

			@Override
			public void done(JSONArray array, BmobException exp) {
				if (exp != null) {
					ratingUI.setRating(0f);
					ratingUI.showRating();
					return;
				}
				if (array != null) {//
					try {
						JSONObject obj = array.getJSONObject(0);
						int avg = obj.getInt("_avgMValue");
						ratingUI.setRating(avg);
					} catch (JSONException ignored) {
					}
				} else {
					ratingUI.setRating(0f);
				}
				ratingUI.showRating();
			}
		});
	}


	public static void showPersonalRatingOnLocation(Playground playground, final RatingUI ratingUI) {
		BmobQuery<Rating> q = new BmobQuery<>();
		q.setCachePolicy(BmobQuery.CachePolicy.CACHE_THEN_NETWORK);
		q.addWhereEqualTo("mUID",
		                  Prefs.getInstance()
		                       .getGoogleId());
		q.addWhereEqualTo("mId", playground.getId());
		q.findObjects(new FindListener<Rating>() {
			@Override
			public void done(List<Rating> list, BmobException exp) {
				if (list.size() > 0) {
					ratingUI.setRating(list.get(0));
				}
			}
		});
	}
}
