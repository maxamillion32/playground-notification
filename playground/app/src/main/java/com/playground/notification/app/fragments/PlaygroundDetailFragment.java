package com.playground.notification.app.fragments;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.playground.notification.R;
import com.playground.notification.api.Api;
import com.playground.notification.api.ApiNotInitializedException;
import com.playground.notification.app.App;
import com.playground.notification.app.activities.AppActivity;
import com.playground.notification.app.activities.MapsActivity;
import com.playground.notification.bus.ShowLocationRatingEvent;
import com.playground.notification.databinding.PlaygroundDetailBinding;
import com.playground.notification.databinding.RatingDialogBinding;
import com.playground.notification.ds.Playground;
import com.playground.notification.ds.google.Matrix;
import com.playground.notification.ds.sync.Rating;
import com.playground.notification.ds.sync.SyncPlayground;
import com.playground.notification.sync.FavoriteManager;
import com.playground.notification.sync.NearRingManager;
import com.playground.notification.utils.Prefs;
import com.playground.notification.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.FindStatisticsListener;
import cn.bmob.v3.listener.UpdateListener;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Show details of a playground, address, rating.
 *
 * @author Xinyue Zhao
 */
public final class PlaygroundDetailFragment extends DialogFragment {
	private static final String EXTRAS_GROUND = PlaygroundDetailFragment.class.getName() + ".EXTRAS.playground";
	private static final String EXTRAS_LAT = PlaygroundDetailFragment.class.getName() + ".EXTRAS.lat";
	private static final String EXTRAS_LNG = PlaygroundDetailFragment.class.getName() + ".EXTRAS.lng";
	private static final String EXTRAS_CLICKABLE = MyLocationFragment.class.getName() + ".EXTRAS.clickable";
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_playground_detail;
	/**
	 * Data-binding.
	 */
	private PlaygroundDetailBinding mBinding;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.playground.notification.bus.ShowLocationRatingEvent}.
	 *
	 * @param e
	 * 		Event {@link com.playground.notification.bus.ShowLocationRatingEvent}.
	 */
	public void onEvent(ShowLocationRatingEvent e) {
		AppActivity activity = (AppActivity) getActivity();
		if (activity != null) {
			activity.showDialogFragment(RatingDialogFragment.newInstance(App.Instance, e.getPlayground(),
					mBinding.getRating()), "rating");
		}
	}
	//------------------------------------------------

	//A dialog to update current rating status of a ground for you.
	public static class RatingDialogFragment extends DialogFragment {
		/**
		 * Data-binding.
		 */
		private RatingDialogBinding mBinding;

		public static RatingDialogFragment newInstance(Context cxt, Playground playground, Rating rating) {
			Bundle args = new Bundle();
			args.putSerializable("rating", (Serializable) rating);
			args.putSerializable("ground", (Serializable) playground);
			return (RatingDialogFragment) RatingDialogFragment.instantiate(cxt, RatingDialogFragment.class.getName(),
					args);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_rating, container, false);
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			mBinding = DataBindingUtil.bind(view.findViewById(R.id.rating_dialog_vg));
			Drawable progress = mBinding.locationRb.getProgressDrawable();
			DrawableCompat.setTint(progress, getResources().getColor(R.color.primary_dark_color));
			getDialog().setTitle(R.string.lbl_rating);
			Rating rating = ((Rating) getArguments().getSerializable("rating"));
			if (rating != null) {
				mBinding.setRating(rating);
			}
			view.findViewById(R.id.close_iv).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
					Playground playground = (Playground) getArguments().getSerializable("ground");
					Rating rating = ((Rating) getArguments().getSerializable("rating"));
					if (rating == null) {
						Rating newRating = new Rating(Prefs.getInstance().getGoogleId(), playground);
						newRating.setValue(mBinding.locationRb.getRating());
						newRating.save(App.Instance);
					} else {
						Rating updateRating = new Rating(Prefs.getInstance().getGoogleId(), playground);
						updateRating.setValue(mBinding.locationRb.getRating());
						updateRating.update(App.Instance, rating.getObjectId(), new UpdateListener() {
							@Override
							public void onSuccess() {
							}

							@Override
							public void onFailure(int code, String msg) {
							}
						});
					}
				}
			});
		}
	}

	/**
	 * New an instance of {@link PlaygroundDetailFragment}.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param fromLat
	 * 		The latitude of "from" position to {@code playground}.
	 * @param fromLng
	 * 		The longitude of "from" position to {@code playground}.
	 * @param playground
	 * 		{@link Playground}.
	 * 	@param clickable  {@code true} if the preview map can be clicked and show marker on main map.
	 *
	 * @return An instance of {@link PlaygroundDetailFragment}.
	 */
	public static PlaygroundDetailFragment newInstance(Context context, double fromLat, double fromLng,
			Playground playground, boolean clickable) {
		Bundle args = new Bundle();
		args.putDouble(EXTRAS_LAT, fromLat);
		args.putDouble(EXTRAS_LNG, fromLng);
		args.putSerializable(EXTRAS_GROUND, (Serializable) playground);
		args.putBoolean(EXTRAS_CLICKABLE, clickable);
		return (PlaygroundDetailFragment) PlaygroundDetailFragment.instantiate(context,
				PlaygroundDetailFragment.class.getName(), args);
	}

	@Override
	public void onResume() {
		EventBus.getDefault().register(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Light_Dialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Bundle args = getArguments();
		final Playground playground = (Playground) args.getSerializable(EXTRAS_GROUND);
		if (playground != null) {

			final double lat = args.getDouble(EXTRAS_LAT);
			final double lng = args.getDouble(EXTRAS_LNG);

			Prefs prefs = Prefs.getInstance();
			mBinding = DataBindingUtil.bind(view.findViewById(R.id.playground_detail_vg));


			final String method;
			switch (prefs.getTransportationMethod()) {
			case "0":
				method = "driving";
				break;
			case "1":
				method = "walking";
				break;
			case "2":
				method = "bicycling";
				break;
			case "3":
				method = "transit";
				break;
			default:
				method = "walking";
				break;
			}
			String units = "metric";
			switch (prefs.getUnitsType()) {
			case "0":
				units = "metric";
				break;
			case "1":
				units = "imperial";
				break;

			}
			try {
				Api.getMatrix(lat + "," + lng, playground.getLatitude() + "," + playground.getLongitude(),
						Locale.getDefault().getLanguage(), method, App.Instance.getDistanceMatrixKey(), units,
						new Callback<Matrix>() {
							@Override
							public void success(Matrix matrix, Response response) {
								mBinding.setMatrix(matrix);
								mBinding.setMode(method);
								mBinding.setHandler(new EventHandler(lat, lng, playground, mBinding));
							}

							@Override
							public void failure(RetrofitError error) {

							}
						});
			} catch (ApiNotInitializedException e) {
				dismiss();
			}


			if (FavoriteManager.getInstance().isCached(playground)) {
				mBinding.favIv.setImageResource(R.drawable.ic_favorite);
			}
			if (NearRingManager.getInstance().isCached(playground)) {
				mBinding.ringIv.setImageResource(R.drawable.ic_geo_fence);
			}


			//Have you rated?
			BmobQuery<Rating> q = new BmobQuery<>();
			q.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
			q.addWhereEqualTo("mUID", Prefs.getInstance().getGoogleId());
			q.addWhereEqualTo("mId", playground.getId());
			q.findObjects(App.Instance, new FindListener<Rating>() {
				@Override
				public void onSuccess(List<Rating> list) {
					if (list.size() > 0) {
						mBinding.setRating(list.get(0));
					}
				}

				@Override
				public void onError(int i, String s) {
				}
			});

			//Rating summary.
			q = new BmobQuery<>();
			q.addWhereEqualTo("mId", playground.getId());
			q.average(new String[] { "mValue" });
			q.findStatistics(App.Instance, Rating.class, new FindStatisticsListener() {

				@Override
				public void onSuccess(Object object) {
					JSONArray ary = (JSONArray) object;
					if (ary != null) {//
						try {
							JSONObject obj = ary.getJSONObject(0);
							int avg = obj.getInt("_avgMValue");
							mBinding.locationRb.setRating(avg);
						} catch (JSONException e) {
						}
					} else {
						mBinding.setRatedValue(0f);
					}
					mBinding.ratingVg.setVisibility(View.VISIBLE);
				}

				@Override
				public void onFailure(int code, String msg) {
					mBinding.setRatedValue(0f);
					mBinding.ratingVg.setVisibility(View.VISIBLE);
				}
			});


			String latlng = playground.getLatitude() + "," + playground.getLongitude();
			String maptype = prefs.getMapType().equals("0") ? "roadmap" : "hybrid";
			String url = prefs.getGoogleApiHost() + "maps/api/staticmap?center=" + latlng +
					"&zoom=16&size=620x250&markers=color:red%7Clabel:S%7C" + latlng + "&key=" +
					App.Instance.getDistanceMatrixKey() + "&sensor=true&maptype=" + maptype;
			Picasso.with(App.Instance).load(url).into(mBinding.locationPreviewIv);
			if(getArguments().getBoolean(EXTRAS_CLICKABLE)){
				mBinding.locationPreviewIv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						MapsActivity.showInstance((Activity) mBinding.locationPreviewIv.getContext(), playground);
					}
				});
			}
		}
	}


	/**
	 * Event-handler for all radio-buttons on UI.
	 */
	public static class EventHandler {
		private double mLat;
		private double mLng;
		private Playground mGround;
		private PlaygroundDetailBinding mBinding;

		public EventHandler(double fromLat, double fromLng, Playground playground, PlaygroundDetailBinding binding) {
			mLat = fromLat;
			mLng = fromLng;
			mGround = playground;
			mBinding = binding;
		}

		public void onModeSelected(View view) {
			mBinding.setMode(view.getTag().toString());
			mBinding.changingPb.setVisibility(View.VISIBLE);

			Prefs prefs = Prefs.getInstance();
			String units = "metric";
			switch (prefs.getUnitsType()) {
			case "0":
				break;
			case "1":
				break;

			}
			try {
				Api.getMatrix(mLat + "," + mLng, mGround.getLatitude() + "," + mGround.getLongitude(),
						Locale.getDefault().getLanguage(), mBinding.getMode(), App.Instance.getDistanceMatrixKey(), units,
						new Callback<Matrix>() {
							@Override
							public void success(Matrix matrix, Response response) {
								mBinding.setMatrix(matrix);
								mBinding.setHandler(new EventHandler(mLat, mLng, mGround, mBinding));
								mBinding.changingPb.setVisibility(View.GONE);
							}

							@Override
							public void failure(RetrofitError error) {
								mBinding.changingPb.setVisibility(View.GONE);
							}
						});
			} catch (ApiNotInitializedException e) {
				//Ignore this request.
				mBinding.changingPb.setVisibility(View.GONE);
			}
		}

		public void onRatingClicked(View view) {
			EventBus.getDefault().post(new ShowLocationRatingEvent(mGround));
		}

		public void onSaveFavClicked(View view) {
			FavoriteManager mgr = FavoriteManager.getInstance();
			SyncPlayground favFound = mgr.findInCache(mGround);
			if (favFound == null) {
				mgr.addFavorite(mGround, mBinding.favIv, mBinding.playgroundDetailVg);
			} else {
				mgr.removeFavorite(favFound, mBinding.favIv, mBinding.playgroundDetailVg);
			}
		}

		public void onSaveNearRingClicked(View view) {
			NearRingManager mgr = NearRingManager.getInstance();
			SyncPlayground ringFound = mgr.findInCache(mGround);
			if (ringFound == null) {
				mgr.addNearRing(mGround, mBinding.ringIv, mBinding.playgroundDetailVg);
			} else {
				mgr.removeNearRing(ringFound, mBinding.ringIv, mBinding.playgroundDetailVg);
			}
		}

		public void onGoClicked(View v) {
			mBinding.goBtn.getContext().startActivity(com.playground.notification.utils.Utils.getMapWeb(new LatLng(mLat,
					mLng), new LatLng(mGround.getLatitude(), mGround.getLongitude())));
		}


		public void onShareGround(View v) {
			final String url = "https://www.google.de/maps/search/" + mGround.getLatitude() + "," + mGround.getLongitude();
			com.tinyurl4j.Api.getTinyUrl(url, new Callback<com.tinyurl4j.data.Response>() {
				@Override
				public void success(com.tinyurl4j.data.Response response, retrofit.client.Response response2) {
					String subject = App.Instance.getString(R.string.lbl_share_ground_title);
					String content = App.Instance.getString(R.string.lbl_share_ground_content, response.getResult(),
							Prefs.getInstance().getAppDownloadInfo());
					mBinding.shareGroundBtn.getContext().startActivity(Utils.getShareInformation(subject, content));
				}

				@Override
				public void failure(RetrofitError error) {
					String subject = App.Instance.getString(R.string.lbl_share_ground_title);
					String content = App.Instance.getString(R.string.lbl_share_ground_content, url,
							Prefs.getInstance().getAppDownloadInfo());
					mBinding.shareGroundBtn.getContext().startActivity(Utils.getShareInformation(subject, content));
				}
			});
		}

		public void onPreviewClicked(View v) {
			MapsActivity.showInstance((Activity) mBinding.locationPreviewIv.getContext(), mGround);
		}
	}
}
