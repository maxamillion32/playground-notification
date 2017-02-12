package com.playground.notification.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.playground.notification.R;
import com.playground.notification.bus.OpenPlaygroundEvent;
import com.playground.notification.databinding.ItemPlaygroundBinding;
import com.playground.notification.ds.grounds.Playground;

import java.lang.ref.WeakReference;
import java.util.List;


/**
 * The adapter for {@link RecyclerView} in {@link com.playground.notification.app.fragments.PlaygroundListFragment}.
 *
 * @author Xinyue Zhao
 */
public final class PlaygroundListAdapter extends RecyclerView.Adapter<PlaygroundListAdapter.PlaygroundListAdapterViewHolder> {
	private static final int ITEM_LAYOUT = R.layout.item_playground_list;
	private List<Playground> mPlaygroundList;

	public PlaygroundListAdapter(List<Playground> playgroundList) {
		mPlaygroundList = playgroundList;
	}

	@Override
	public PlaygroundListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context cxt = parent.getContext();
		ItemPlaygroundBinding binding = DataBindingUtil.inflate(LayoutInflater.from(cxt), ITEM_LAYOUT, parent, false);
		return new PlaygroundListAdapter.PlaygroundListAdapterViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(final PlaygroundListAdapterViewHolder holder, int position) {
		Playground playground = mPlaygroundList.get(position);
		holder.mBinding.setPlayground(playground);
		holder.mBinding.setOpenPlaygroundEvent(new OpenPlaygroundEvent(playground, new WeakReference<>(holder.itemView)));
		holder.mBinding.executePendingBindings();
	}

	@Override
	public int getItemCount() {
		return mPlaygroundList == null ?
		       0 :
		       mPlaygroundList.size();
	}

	static class PlaygroundListAdapterViewHolder extends RecyclerView.ViewHolder {
		private ItemPlaygroundBinding mBinding;

		public PlaygroundListAdapterViewHolder(ItemPlaygroundBinding binding) {
			super(binding.getRoot());
			mBinding = binding;
		}
	}
}
