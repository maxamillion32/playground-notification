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
import com.playground.notification.ds.sync.Rating;
import com.playground.notification.utils.RatingUI;
import com.playground.notification.utils.Utils;

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
	private List<Playground> mPlaygroundList = new ArrayList<>();

	public PlaygroundListAdapter(List<? extends Playground> playgroundList) {
		mPlaygroundList.addAll(playgroundList);
	}

	@Override
	public PlaygroundListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Context cxt = parent.getContext();
		ItemPlaygroundBinding binding = DataBindingUtil.inflate(LayoutInflater.from(cxt), ITEM_LAYOUT, parent, false);
		return new PlaygroundListAdapterViewHolder(this, binding);
	}

	@Override
	public void onBindViewHolder(final PlaygroundListAdapterViewHolder holder, int position) {
		holder.mBinding.itemMapRecyclerview.getLayoutParams().width = (int) App.Instance.getListItemWidth();
		holder.mBinding.itemMapRecyclerview.getLayoutParams().height = (int) App.Instance.getListItemHeight();
		holder.initializeMapView();
		holder.mBinding.executePendingBindings();
	}

	@Override
	public void onViewRecycled(PlaygroundListAdapterViewHolder holder) {
		holder.onViewRecycled();
	}


	@Override
	public int getItemCount() {
		return mPlaygroundList == null ?
		       0 :
		       mPlaygroundList.size();
	}

	public void refresh(List<? extends Playground> data) {
		if (mPlaygroundList.size() > 0) {
			mPlaygroundList.clear();
		}
		mPlaygroundList.addAll(data);
		notifyDataSetChanged();
	}

	protected static class PlaygroundListAdapterViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback,
	                                                                                                  RatingUI {
		private final ItemPlaygroundBinding mBinding;
		private final PlaygroundListAdapter mPlaygroundListAdapter;
		private GoogleMap mGoogleMap;

		private PlaygroundListAdapterViewHolder(PlaygroundListAdapter playgroundListAdapter, ItemPlaygroundBinding binding) {
			super(binding.getRoot());
			mPlaygroundListAdapter = playgroundListAdapter;
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
			if (getAdapterPosition() < 0 || mPlaygroundListAdapter.mPlaygroundList == null || mPlaygroundListAdapter.mPlaygroundList.size() <= 0) {
				return;
			}
			Playground playground = mPlaygroundListAdapter.mPlaygroundList.get(getAdapterPosition());
			Utils.showRatingSummary(playground, this);
			mGoogleMap = googleMap;
			mGoogleMap.setBuildingsEnabled(false);
			mGoogleMap.setIndoorEnabled(false);
			googleMap.getUiSettings()
			         .setMapToolbarEnabled(false);
			googleMap.getUiSettings()
			         .setScrollGesturesEnabled(false);
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playground.getPosition(), 16));
			Marker marker = googleMap.addMarker(new MarkerOptions().position(playground.getPosition()));
			marker.setIcon(getBitmapDescriptor(App.Instance, R.drawable.ic_pin_500));
			googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
				@Override
				public void onMapClick(LatLng latLng) {
					if (getAdapterPosition() >= 0) {
						Playground playground = mPlaygroundListAdapter.mPlaygroundList.get(getAdapterPosition());
						EventBus.getDefault()
						        .post(new OpenPlaygroundEvent(playground, getAdapterPosition(), new WeakReference<>(itemView)));
					}
				}
			});
		}

		private void onViewRecycled() {
			if (mGoogleMap != null) {
				mGoogleMap.clear();
				mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

				mBinding.itemMapRecyclerview.onPause();
				mBinding.itemMapRecyclerview.onStop();
				mBinding.itemMapRecyclerview.onDestroy();
			}
			mBinding.locationRb.setRating(0f);
		}

		@Override
		public void setRating(Rating rate) {
		}

		@Override
		public void setRating(float rate) {
			mBinding.locationRb.setRating(rate);
		}

		@Override
		public void showRating() {
		}

		@Override
		public void dismissRating() {
		}
	}
}
