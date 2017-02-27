package com.playground.notification.app.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.bus.OpenPlaygroundEvent;
import com.playground.notification.databinding.ItemPlaygroundBinding;
import com.playground.notification.ds.grounds.Playground;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.playground.notification.utils.Utils.getBitmapDescriptor;


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
		return new PlaygroundListAdapterViewHolder(mPlaygroundList, binding);
	}

	@Override
	public void onBindViewHolder(final PlaygroundListAdapterViewHolder holder, int position) {
		holder.mBinding.getRoot().getLayoutParams().width = (int) App.Instance.getListItemWidth();
		holder.mBinding.getRoot().getLayoutParams().height = (int) App.Instance.getListItemHeight();
		holder.initializeMapView();
		holder.mBinding.executePendingBindings();
	}

	@Override
	public void onViewRecycled(PlaygroundListAdapterViewHolder holder) {
		if (holder.mGoogleMap != null) {
			holder.mGoogleMap.clear();
			holder.mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

			holder.mBinding.itemMapRecyclerview.onPause();
			holder.mBinding.itemMapRecyclerview.onStop();
			holder.mBinding.itemMapRecyclerview.onDestroy();
		}
	}


	@Override
	public int getItemCount() {
		return mPlaygroundList == null ?
		       0 :
		       mPlaygroundList.size();
	}

	public void refresh(List<Playground> data) {
		if (mPlaygroundList != null && mPlaygroundList.size() > 0) {
			mPlaygroundList.clear();
		} else {
			if (mPlaygroundList == null) {
				mPlaygroundList = new ArrayList<>();
			}
		}
		mPlaygroundList.addAll(data);
		notifyDataSetChanged();
	}

	protected static class PlaygroundListAdapterViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
		private final ItemPlaygroundBinding mBinding;
		private final List<Playground> mPlaygroundList;
		private GoogleMap mGoogleMap;

		private PlaygroundListAdapterViewHolder(List<Playground> playgroundList, ItemPlaygroundBinding binding) {
			super(binding.getRoot());
			mPlaygroundList = playgroundList;
			mBinding = binding;
		}

		private void initializeMapView() {
			mBinding.itemMapRecyclerview.onCreate(null);
			mBinding.itemMapRecyclerview.onStart();
			mBinding.itemMapRecyclerview.onResume();
			mBinding.itemMapRecyclerview.getMapAsync(this);
		}


		@Override
		public void onMapReady(GoogleMap googleMap) {
			showData(googleMap);
		}

		private void showData(GoogleMap googleMap) {
			if (getAdapterPosition() < 0) {
				return;
			}
			mGoogleMap = googleMap;
			Playground playground = mPlaygroundList.get(getAdapterPosition());
			googleMap.getUiSettings()
			         .setMapToolbarEnabled(false);
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playground.getPosition(), 16));
			Marker marker = googleMap.addMarker(new MarkerOptions().position(playground.getPosition()));
			marker.setIcon(getBitmapDescriptor(App.Instance, R.drawable.ic_pin_500));
			googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
				@Override
				public void onMapClick(LatLng latLng) {
					if (getAdapterPosition() >= 0) {
						Playground playground = mPlaygroundList.get(getAdapterPosition());
						EventBus.getDefault()
						        .post(new OpenPlaygroundEvent(playground, getAdapterPosition(), new WeakReference<>(itemView)));
					}
				}
			});
		}
	}
}
