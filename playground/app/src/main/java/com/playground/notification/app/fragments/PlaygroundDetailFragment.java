package com.playground.notification.app.fragments;

import java.io.Serializable;
import java.util.Locale;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.playground.notification.R;
import com.playground.notification.api.Api;
import com.playground.notification.app.App;
import com.playground.notification.databinding.PlaygroundDetailBinding;
import com.playground.notification.ds.Playground;
import com.playground.notification.ds.google.Matrix;
import com.playground.notification.ds.sync.SyncPlayground;
import com.playground.notification.sync.FavoriteManager;
import com.playground.notification.sync.NearRingManager;
import com.playground.notification.utils.Prefs;

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
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_playground_detail;
	/**
	 * Data-binding.
	 */
	private PlaygroundDetailBinding mBinding;



	/**
	 * New an instance of {@link GPlusFragment}.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param fromLat
	 * 		The latitude of "from" position to {@code playground}.
	 * @param fromLng
	 * 		The longitude of "from" position to {@code playground}.
	 * @param playground
	 * 		{@link Playground}.
	 *
	 * @return An instance of {@link PlaygroundDetailFragment}.
	 */
	public static PlaygroundDetailFragment newInstance(Context context, double fromLat, double fromLng,
			Playground playground) {
		Bundle args = new Bundle();
		args.putDouble(EXTRAS_LAT, fromLat);
		args.putDouble(EXTRAS_LNG, fromLng);
		args.putSerializable(EXTRAS_GROUND, (Serializable) playground);
		return (PlaygroundDetailFragment) PlaygroundDetailFragment.instantiate(context,
				PlaygroundDetailFragment.class.getName(), args);
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
			Api.getMatrix(lat + "," + lng, playground.getLatitude() + "," + playground.getLongitude(),
					Locale.getDefault().getLanguage(), method, App.Instance.getDistanceMatrixKey(), units,
					new Callback<Matrix>() {
						@Override
						public void success(Matrix matrix, Response response) {
							mBinding.setMatrix(matrix);
							mBinding.setMode(method);
							mBinding.setModeSelectedHandler(new ModeSelectedHandler(lat, lng, playground, mBinding));
						}

						@Override
						public void failure(RetrofitError error) {

						}
					});
			mBinding.favBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FavoriteManager mgr = FavoriteManager.getInstance();
					SyncPlayground favFound = mgr.findInCache(playground);
					if(favFound == null) {
						mgr.addFavorite(playground, mBinding.favIv, mBinding.playgroundDetailVg);
					} else {
						mgr.removeFavorite(favFound, mBinding.favIv, mBinding.playgroundDetailVg);
					}
				}
			});
			mBinding.ringBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					NearRingManager mgr = NearRingManager.getInstance();
					SyncPlayground ringFound = mgr.findInCache(playground);
					if (ringFound == null) {
						mgr.addNearRing(playground, mBinding.ringIv, mBinding.playgroundDetailVg);
					} else {
						mgr.removeNearRing(ringFound, mBinding.ringIv, mBinding.playgroundDetailVg);
					}
				}
			});

			if(FavoriteManager.getInstance().isCached(playground)) {
				mBinding.favIv.setImageResource(R.drawable.ic_favorite);
			}
			if(NearRingManager.getInstance().isCached(playground)) {
				mBinding.ringIv.setImageResource(R.drawable.ic_geo_fence);
			}
		}
	}


	/**
	 * Event-handler for all radio-buttons on UI.
	 */
	public static class ModeSelectedHandler {
		private double mLat;
		private double mLng;
		private Playground mGround;
		private PlaygroundDetailBinding mBinding;

		public ModeSelectedHandler(double fromLat, double fromLng, Playground playground,
				PlaygroundDetailBinding binding) {
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
			Api.getMatrix(mLat + "," + mLng, mGround.getLatitude() + "," + mGround.getLongitude(),
					Locale.getDefault().getLanguage(), mBinding.getMode(), App.Instance.getDistanceMatrixKey(), units,
					new Callback<Matrix>() {
						@Override
						public void success(Matrix matrix, Response response) {
							mBinding.setMatrix(matrix);
							mBinding.setModeSelectedHandler(new ModeSelectedHandler(mLat, mLng, mGround, mBinding));
							mBinding.changingPb.setVisibility(View.GONE);
						}

						@Override
						public void failure(RetrofitError error) {
							mBinding.changingPb.setVisibility(View.GONE);
						}
					});
		}
	}

}
