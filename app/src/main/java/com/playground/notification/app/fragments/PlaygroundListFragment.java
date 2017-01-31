package com.playground.notification.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.playground.notification.R;
import com.playground.notification.databinding.PlaygroundListBinding;

import de.greenrobot.event.EventBus;

public final class PlaygroundListFragment extends Fragment {

	private static final int LAYOUT = R.layout.fragment_playground_list;

	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link Object}.
	 *
	 * @param e Event {@link}.
	 */
	public void onEvent(Object e) {

	}

	//------------------------------------------------
	public static PlaygroundListFragment newInstance(Context cxt) {
		Bundle args = new Bundle();
		return (PlaygroundListFragment) PlaygroundListFragment.instantiate(cxt, PlaygroundListFragment.class.getName(), args);
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		PlaygroundListBinding binding = DataBindingUtil.inflate(inflater, LAYOUT, container, false);
		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		EventBus.getDefault()
		        .register(this);
	}

	@Override
	public void onPause() {
		EventBus.getDefault()
		        .unregister(this);
		super.onPause();
	}
}
