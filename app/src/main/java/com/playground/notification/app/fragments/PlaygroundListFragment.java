package com.playground.notification.app.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.playground.notification.R;
import com.playground.notification.app.adapters.PlaygroundListAdapter;
import com.playground.notification.databinding.PlaygroundListBinding;
import com.playground.notification.ds.grounds.Playground;

import java.io.Serializable;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Show list of {@link com.playground.notification.ds.grounds.Playground}s.
 *
 * @author Xinyue Zhao
 */
public final class PlaygroundListFragment extends Fragment {
	private static final String EXTRAS_PLAYGROUND_LIST = PlaygroundListFragment.class.getName() + ".EXTRAS.playground.list";
	private static final int LAYOUT = R.layout.fragment_playground_list;
	private PlaygroundListBinding mBinding;

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
	public static PlaygroundListFragment newInstance(Context cxt, List<Playground> playgroundList) {
		Bundle args = new Bundle();
		args.putSerializable(EXTRAS_PLAYGROUND_LIST, (Serializable) playgroundList);
		return (PlaygroundListFragment) PlaygroundListFragment.instantiate(cxt, PlaygroundListFragment.class.getName(), args);
	}


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mBinding = DataBindingUtil.inflate(inflater, LAYOUT, container, false);
		return mBinding.getRoot();
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mBinding.playgroundListRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		mBinding.playgroundListRv.setAdapter(new PlaygroundListAdapter((List<Playground>) getArguments().getSerializable(EXTRAS_PLAYGROUND_LIST)));
		final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
		dividerItemDecoration.setDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.divider_drawable));
		mBinding.playgroundListRv.addItemDecoration(dividerItemDecoration);

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
