package com.playground.notification.app.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.chopping.utils.DeviceUtils;
import com.chopping.utils.DeviceUtils.ScreenSize;
import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.app.adapters.MyLocationListAdapter;
import com.playground.notification.bus.OpenPlaygroundEvent;
import com.playground.notification.bus.SelectItemEvent;
import com.playground.notification.bus.StartActionModeEvent;
import com.playground.notification.databinding.MyLocationListBinding;
import com.playground.notification.ds.sync.MyLocation;
import com.playground.notification.sync.MyLocationManager;
import com.playground.notification.utils.Prefs;

/**
 * A list of all my-locations.
 *
 * @author Xinyue Zhao
 */
public final class MyLocationListActivity extends AppActivity {
	public static final int REQ = 0x91;
	private static final int GRID_COL_COUNT = 3;
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_my_location_list;
	/**
	 * Action-mode menu.
	 */
	private static final int MENU_ACTIONMODE = R.menu.menu_actionmode;
	/**
	 * An adapter for the list
	 */
	private MyLocationListAdapter mAdp;
	/**
	 * User action-mode for delete images.
	 */
	private ActionMode mActionMode;
	/**
	 * Data-binding.
	 */
	private MyLocationListBinding mBinding;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------


	/**
	 * Handler for {@link SelectItemEvent}.
	 *
	 * @param e
	 * 		Event {@link SelectItemEvent}.
	 */
	public void onEvent(SelectItemEvent e) {
		toggleSelection(e.getPosition());
	}



	/**
	 * Handler for {@link StartActionModeEvent}.
	 *
	 * @param e
	 * 		Event {@link  StartActionModeEvent}.
	 */
	public void onEvent(StartActionModeEvent e) {
		//See more about action-mode.
		//http://databasefaq.com/index.php/answer/19065/android-android-fragments-recyclerview-android-actionmode-problems-with-implementing-contextual-action-mode-in-recyclerview-fragment
		mActionMode = startSupportActionMode(new Callback() {
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				mode.getMenuInflater().inflate(MENU_ACTIONMODE, menu);
				mBinding.toolbar.setVisibility(View.GONE);

				mAdp.setActionMode(true);
				mAdp.notifyDataSetChanged();
				return true;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				//It has only "delete".
				List<Integer> selectedPositions = mAdp.getSelectedItems();
				List<MyLocation> myLocations = new ArrayList<>();
				for(Integer pos : selectedPositions) {
					myLocations.add(mAdp.getData().get(pos));
				}
				//Remove on remote.
				for(MyLocation location : myLocations) {
					MyLocation del = new MyLocation(Prefs.getInstance().getGoogleId(), location.getLabel(), location);
					del.setObjectId(location.getObjectId());
					del.delete(App.Instance);
				}
				//Remove on cache.
				for(MyLocation alreadyRemoved : myLocations) {
					mAdp.getData().remove(alreadyRemoved);
				}
				mAdp.notifyDataSetChanged();
				mActionMode.finish();
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				mActionMode = null;
				mBinding.toolbar.setVisibility(View.VISIBLE);

				mAdp.clearSelection();
				mAdp.setActionMode(false);
				mAdp.notifyDataSetChanged();
			}
		});
	}

	/**
	 * Handler for {@link com.playground.notification.bus.OpenPlaygroundEvent}.
	 *
	 * @param e
	 * 		Event {@link com.playground.notification.bus.OpenPlaygroundEvent}.
	 */
	public void onEvent(OpenPlaygroundEvent e) {
		MapsActivity.showInstance(this, e.getPlayground());
	}
	//------------------------------------------------

	/**
	 * Show single instance of {@link MyLocationListActivity}
	 *
	 * @param cxt
	 * 		{@link Context}.
	 */
	public static void showInstance(Activity cxt ) {
		Intent intent = new Intent(cxt, MyLocationListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ActivityCompat.startActivity(cxt, intent, null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Init data-binding.
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		//Init application basic elements.
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));

		//App-bar.
		setSupportActionBar(mBinding.toolbar);
		mBinding.toolbar.setTitle(R.string.lbl_my_location_list);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDefaultDisplayHomeAsUpEnabled(true);

		//List of gallery.
		ScreenSize screenSize = DeviceUtils.getScreenSize(App.Instance);
		mBinding.listRv.setLayoutManager(new GridLayoutManager(this, GRID_COL_COUNT));
		mAdp = new MyLocationListAdapter(MyLocationManager.getInstance().getCachedList(), GRID_COL_COUNT, screenSize.Width);
		mBinding.listRv.setAdapter(mAdp);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			ActivityCompat.finishAfterTransition(this);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Select items on view when opened action-mode.
	 *
	 * @param position
	 * 		The select position.
	 */
	private void toggleSelection(int position) {
		mAdp.toggleSelection(position);
		int count = mAdp.getSelectedItemCount();

		if (count == 0) {
			mActionMode.finish();
		} else {
			mActionMode.setTitle(String.valueOf(count));
			mActionMode.invalidate();
		}
	}
}
