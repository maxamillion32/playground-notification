package com.playground.notification.app.fragments;

import java.io.Serializable;
import java.util.Locale;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
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
		Log.d("asdfasdf", String.format("from: %f, %f to %f, %f", fromLat, fromLng, playground.getLatitude(),
				playground.getLongitude()));
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
		Playground playground = (Playground) args.getSerializable(EXTRAS_GROUND);
		if (playground != null) {
			double lat = args.getDouble(EXTRAS_LAT);
			double lng = args.getDouble(EXTRAS_LNG);

			mBinding = DataBindingUtil.bind(view.findViewById(R.id.playground_detail_vg));
			mBinding.closeVg.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
			Api.getMatrix(lat + "," + lng, playground.getLatitude() + "," + playground.getLongitude(),
					Locale.getDefault().getLanguage(), "walking", App.Instance.getDistanceMatrixKey(),
					new Callback<Matrix>() {
						@Override
						public void success(Matrix matrix, Response response) {
							mBinding.setMatrix(matrix);
						}

						@Override
						public void failure(RetrofitError error) {

						}
					});
		}
	}


}
