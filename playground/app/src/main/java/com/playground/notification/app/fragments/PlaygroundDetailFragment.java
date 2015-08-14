package com.playground.notification.app.fragments;

import java.io.Serializable;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.application.BasicPrefs;
import com.chopping.fragments.BaseFragment;
import com.playground.notification.R;
import com.playground.notification.databinding.PlaygroundDetailBinding;
import com.playground.notification.ds.Playground;
import com.playground.notification.utils.Prefs;

/**
 * Show details of a playground, address, rating.
 *
 * @author Xinyue Zhao
 */
public final class PlaygroundDetailFragment extends BaseFragment {
	private static final String EXTRAS_DATA = PlaygroundDetailFragment.class.getName() + ".EXTRAS.playground";
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_playground_detail;

	/**
	 * New an instance of {@link GPlusFragment}.
	 *
	 * @param context
	 * 		{@link android.content.Context}.
	 * @param playground
	 * 		{@link Playground}.
	 *
	 * @return An instance of {@link PlaygroundDetailFragment}.
	 */
	public static Fragment newInstance(Context context, Playground playground) {
		Bundle args = new Bundle();
		args.putSerializable(EXTRAS_DATA, (Serializable) playground);
		return PlaygroundDetailFragment.instantiate(context, PlaygroundDetailFragment.class.getName(), args);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Playground playground = (Playground) getArguments().getSerializable(EXTRAS_DATA);
		if (playground != null) {
			PlaygroundDetailBinding binding = DataBindingUtil.bind(view.findViewById(R.id.playground_detail_vg));
		}
	}

	@Override
	protected BasicPrefs getPrefs() {
		return Prefs.getInstance();
	}
}
