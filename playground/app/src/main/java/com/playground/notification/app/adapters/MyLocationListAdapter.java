package com.playground.notification.app.adapters;

import java.util.List;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.playground.notification.BR;
import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.bus.OpenPlaygroundEvent;
import com.playground.notification.bus.SelectItemEvent;
import com.playground.notification.bus.StartActionModeEvent;
import com.playground.notification.ds.sync.MyLocation;
import com.playground.notification.utils.Prefs;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import de.greenrobot.event.EventBus;


/**
 * The adapter for {@link RecyclerView} in {@link  com.playground.notification.app.activities.MyLocationListActivity}.
 *
 * @author Xinyue Zhao
 */
public final class MyLocationListAdapter extends SelectableAdapter<MyLocationListAdapter.ViewHolder> {
	/**
	 * Main layout for this component.
	 */
	private static final int ITEM_LAYOUT = R.layout.item_my_location_list;
	/**
	 * Data-source.
	 */
	private List<MyLocation> mVisibleData;
	private int              mScreenWidth;
	private int              mColCount;

	/**
	 * Constructor of {@link MyLocationListAdapter}.
	 *
	 * @param data
	 * 		Data-source.
	 */
	public MyLocationListAdapter( List<MyLocation> data, int colCount, int screenWidth ) {
		mColCount = colCount;
		mScreenWidth = screenWidth;
		setData( data );
	}
	/**
	 * Get current used data-source.
	 *
	 * @return The data-source.
	 */
	public List<MyLocation> getData() {
		return mVisibleData;
	}
	/**
	 * Set data-source for list-view.
	 *
	 * @param data
	 * 		Data-source.
	 */
	public void setData( List<MyLocation> data ) {
		mVisibleData = data;
	}
	@Override
	public int getItemCount() {
		return mVisibleData == null ? 0 : mVisibleData.size();
	}

	@Override
	public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
		Context                          cxt        = parent.getContext();
		LayoutInflater                   inflater   = LayoutInflater.from( cxt );
		ViewDataBinding                  binding    = DataBindingUtil.inflate( inflater, ITEM_LAYOUT, parent, false );
		MyLocationListAdapter.ViewHolder viewHolder = new MyLocationListAdapter.ViewHolder( binding );
		return viewHolder;
	}

	@Override
	public void onBindViewHolder( final ViewHolder holder, final int position ) {
		final MyLocation myLocation = mVisibleData.get( position );
		holder.mBinding.setVariable( BR.myLoc, myLocation );
		holder.mBinding.executePendingBindings();

		Prefs  prefs   = Prefs.getInstance();
		String latlng  = myLocation.getLatitude() + "," + myLocation.getLongitude();
		String maptype = Prefs.getInstance().getMapType().equals( "0" ) ? "roadmap" : "hybrid";
		final String url = prefs.getGoogleApiHost() + "maps/api/staticmap?center=" + latlng +
						   "&zoom=16&size=" + prefs.getMyLocationPreviewSize() + "&markers=color:red%7Clabel:S%7C" + latlng + "&key=" +
						   App.Instance.getDistanceMatrixKey() + "&sensor=true&maptype=" + maptype;
		Picasso.with( App.Instance ).load( url ).transform( new Transformation() {
			public Bitmap getResizedBitmap( Bitmap bm, float newWidth, float newHeight ) {
				int   width       = bm.getWidth();
				int   height      = bm.getHeight();
				float scaleWidth  = newWidth / width;
				float scaleHeight = newHeight / height;
				// CREATE A MATRIX FOR THE MANIPULATION
				Matrix matrix = new Matrix();
				// RESIZE THE BIT MAP
				matrix.postScale( scaleWidth, scaleHeight );
				// "RECREATE" THE NEW BITMAP
				return Bitmap.createBitmap( bm, 0, 0, width, height, matrix, false );
			}

			@Override
			public Bitmap transform( Bitmap source ) {
				float  x      = mScreenWidth / ( mColCount + 0.f );
				float  y      = x * ( source.getHeight() / ( source.getWidth() + 0.f ) );
				Bitmap result = getResizedBitmap( source, x, y );
				if( result != source ) {
					source.recycle();
				}
				return result;
			}

			@Override
			public String key() {
				return url.hashCode() + "";
			}
		} ).into( holder.mImageView );
		holder.mImageView.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick( View v ) {
				if( !isActionMode() ) {
					EventBus.getDefault().post( new OpenPlaygroundEvent( myLocation ) );
				}
			}
		} );
		holder.mImageView.setOnLongClickListener( new OnLongClickListener() {
			@Override
			public boolean onLongClick( View v ) {
				if( !isActionMode() ) {
					EventBus.getDefault().post( new StartActionModeEvent() );
				}
				return true;
			}
		} );
		holder.mCheckBox.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick( View v ) {
				EventBus.getDefault().post( new SelectItemEvent( position ) );
			}
		} );
		holder.mCheckBox.setVisibility( !isActionMode() ? View.GONE : View.VISIBLE );
		holder.mCheckBox.setChecked( isSelected( position ) );
	}


	/**
	 * ViewHolder for the list.
	 */
	static class ViewHolder extends RecyclerView.ViewHolder {
		private ImageView       mImageView;
		private CheckBox        mCheckBox;
		private ViewDataBinding mBinding;

		ViewHolder( ViewDataBinding binding ) {
			super( binding.getRoot() );
			mImageView = (ImageView) binding.getRoot().findViewById( R.id.item_iv );
			mCheckBox = (CheckBox) binding.getRoot().findViewById( R.id.item_cb );
			mBinding = binding;
		}
	}
}
