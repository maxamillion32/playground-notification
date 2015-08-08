package com.playground.notification.app.activities;

import java.io.File;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.chopping.bus.CloseDrawerEvent;
import com.chopping.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.app.fragments.AboutDialogFragment;
import com.playground.notification.app.fragments.AppListImpFragment;
import com.playground.notification.app.fragments.GPlusFragment;
import com.playground.notification.bus.EULAConfirmedEvent;
import com.playground.notification.bus.EULARejectEvent;
import com.playground.notification.databinding.ActivityMapsBinding;
import com.playground.notification.db.DB;
import com.playground.notification.db.DatabaseHelper;
import com.playground.notification.utils.Prefs;
import com.playground.notification.views.TouchableMapFragment;

public class MapsActivity extends AppActivity {

	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.activity_maps;
	/**
	 * View's menu.
	 */
	private static final int MENU = R.menu.menu_main;

	private GoogleMap mMap; // Might be null if Google Play services APK is not available.
	private TouchableMapFragment mMapFragment;
	/**
	 * {@code true}: force to load markers, ignore the touch&move effect of map.
	 * Default is {@code true}, because as initializing the map, the markers should be loaded.
	 */
	private volatile  boolean mForcedToLoad = true;
	/**
	 * Use navigation-drawer for this fork.
	 */
	private ActionBarDrawerToggle mDrawerToggle;
	/**
	 * Navigation drawer.
	 */
	private DrawerLayout mDrawerLayout;
	/**
	 * Data-binding.
	 */
	private ActivityMapsBinding mBinding;
	/**
	 * Ask current location.
	 */
	private LocationRequest mLocationRequest;
	/**
	 * Connect to google-api.
	 */
	private GoogleApiClient mGoogleApiClient;
	/**
	 * Receiver for downloading reports.
	 */
	private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(downloadId);
			Cursor cursor = downloadManager.query(query);
			if (cursor.moveToFirst()) {
				int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
				int status = cursor.getInt(columnIndex);
				switch (status) {
				case DownloadManager.STATUS_SUCCESSFUL:
					break;
				case DownloadManager.STATUS_FAILED:
					break;
				}
			}
			populateGrounds();
			dismissProgressIndicator();
		}
	};


	/**
	 * Progress-indicator.
	 */
	private ProgressDialog mProgressDialog;


	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.chopping.bus.CloseDrawerEvent}.
	 *
	 * @param e
	 * 		Event {@link com.chopping.bus.CloseDrawerEvent}.
	 */
	public void onEvent(CloseDrawerEvent e) {
		mDrawerLayout.closeDrawers();
	}


	/**
	 * Handler for {@link  EULARejectEvent}.
	 *
	 * @param e
	 * 		Event {@link  EULARejectEvent}.
	 */
	public void onEvent(EULARejectEvent e) {
		finish();
	}

	/**
	 * Handler for {@link  EULAConfirmedEvent}.
	 *
	 * @param e
	 * 		Event {@link  EULAConfirmedEvent}.
	 */
	public void onEvent(EULAConfirmedEvent e) {
		ConnectGoogleActivity.showInstance(this);
	}

	//------------------------------------------------


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ConnectGoogleActivity.REQ:
			if (resultCode == RESULT_OK) {
				//TODO Return from google-login.
				showProgressIndicator();
				downloadDB();
			} else {
				ActivityCompat.finishAffinity(this);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Download all grounds-db.
	 */
	private void downloadDB() {
		DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(Utils.uriStr2URI(App.Instance.getDl())
				.toASCIIString()));
		request.setDestinationInExternalFilesDir(App.Instance, Environment.DIRECTORY_DCIM, "playgrounds.db");
		request.setVisibleInDownloadsUi(true);//Can see the downloaded file in "download" app.
		if (Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
			request.setNotificationVisibility(Request.VISIBILITY_HIDDEN);
		}
		downloadManager.enqueue(request);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		//Init data-binding.
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		//Init application basic elements.
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));

		initGoogleMap();
		initDrawer();
		initBoard();

		//User that have used this application and done clear(logout), should go back to login-page.
		Prefs prefs = Prefs.getInstance();
		if (prefs.isEULAOnceConfirmed() && TextUtils.isEmpty(prefs.getGoogleId())) {
			ConnectGoogleActivity.showInstance(this);
		} else if (prefs.isEULAOnceConfirmed() && !TextUtils.isEmpty(prefs.getGoogleId())) {
			//TODO Should do something.....
		}
	}

	/**
	 * Initialize information board.
	 */
	private void initBoard() {
		mBinding.boardVg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Utils.showShortToast(App.Instance, "Click on board");
			}
		});
	}

	/**
	 * Initialize all map infrastructures
	 */
	private void initGoogleMap() {
		setUpMapIfNeeded();
		mLocationRequest = LocationRequest.create();
		mGoogleApiClient = new GoogleApiClient.Builder(App.Instance).addApi(LocationServices.API)
				.addConnectionCallbacks(new ConnectionCallbacks() {
					@Override
					public void onConnected(Bundle bundle) {
						LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
								new LocationListener() {
									@Override
									public void onLocationChanged(Location location) {
										updateCurLocal(location);
									}
								});
						Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
						if (location != null) {
							updateCurLocal(location);
						}
					}

					@Override
					public void onConnectionSuspended(int i) {
						Utils.showShortToast(App.Instance, "onConnectionSuspended");

					}
				}).addOnConnectionFailedListener(new OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(ConnectionResult connectionResult) {
						Utils.showShortToast(App.Instance, "onConnectionFailed: " + connectionResult.getErrorCode());
					}
				}).build();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}


	/**
	 * Initialize the navigation drawer.
	 */
	private void initDrawer() {
		setSupportActionBar(mBinding.toolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.application_name,
					R.string.app_name);
			mDrawerLayout.setDrawerListener(mDrawerToggle);

		}

		//Navi-head
		getSupportFragmentManager().beginTransaction().replace(R.id.gplus_container, GPlusFragment.newInstance(
				getApplication())).commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(mDownloadReceiver, intentFilter);

		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
		setUpMapIfNeeded();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mDownloadReceiver);
	}

	/**
	 * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly installed) and the
	 * map has not already been instantiated.. This will ensure that we only ever call {@link #setUpMap()} once when
	 * {@link #mMap} is not null.
	 * <p/>
	 * If it isn't installed {@link SupportMapFragment} (and {@link com.google.android.gms.maps.MapView MapView}) will
	 * show a prompt for the user to install/update the Google Play services APK on their device.
	 * <p/>
	 * A user can return to this FragmentActivity after following the prompt and correctly installing/updating/enabling
	 * the Google Play services. Since the FragmentActivity may not have been completely destroyed during this process
	 * (it is likely that it would only be stopped or paused), {@link #onCreate(Bundle)} may not be called again so we
	 * should call this method in {@link #onResume()} to guarantee that it will be called.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = (mMapFragment = (TouchableMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the camera. In this case, we just add a marker
	 * near Africa.
	 * <p/>
	 * This should only be called once and when we are sure that {@link #mMap} is not null.
	 */
	private void setUpMap() {
		//mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

		mMap.setMyLocationEnabled(true);
		mMap.setTrafficEnabled(true);
		mMap.setIndoorEnabled(true);
		mMap.setBuildingsEnabled(true);

		UiSettings uiSettings = mMap.getUiSettings();
		uiSettings.setZoomControlsEnabled(true);
		uiSettings.setMyLocationButtonEnabled(true);
		uiSettings.setIndoorLevelPickerEnabled(true);
		uiSettings.setCompassEnabled(true);
		uiSettings.setAllGesturesEnabled(true);


		mMap.setPadding(0, getAppBarHeight(), 0, 0);
		mMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
			@Override
			public boolean onMyLocationButtonClick() {
				mForcedToLoad = true;
				return false;
			}
		});
		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
			@Override
			public void onCameraChange(CameraPosition cameraPosition) {
				File dbFile = App.Instance.getDatabasePath(DatabaseHelper.DATABASE_NAME);
				if (dbFile.exists()) {
					populateGrounds();
				}
			}
		});
	}

	/**
	 * Draw grounds on map.
	 */
	private void populateGrounds() {
		if (mForcedToLoad || mMapFragment.isTouchAndMove()) {
			mForcedToLoad = false;
			AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Cursor>() {
				private LatLngBounds mLatLngBounds;

				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					mLatLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
				}

				@Override
				protected Cursor doInBackground(Void... params) {
					Cursor cursor = DB.getInstance(App.Instance).search(mLatLngBounds.northeast,
							mLatLngBounds.southwest);
					return cursor;
				}

				@Override
				protected void onPostExecute(Cursor cursor) {
					super.onPostExecute(cursor);
					try {
						mMap.clear();
						while (cursor.moveToNext()) {
							double lat = cursor.getDouble(cursor.getColumnIndex("latitude"));
							double lng = cursor.getDouble(cursor.getColumnIndex("longitude"));
							String label = cursor.getString(cursor.getColumnIndex("label"));
							mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(label));
						}
					} catch (IllegalStateException e) {
					} finally {
						cursor.close();
						DB.getInstance(App.Instance).close();
					}
				}
			});
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(MENU, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		//Share application.
		MenuItem menuAppShare = menu.findItem(R.id.action_share_app);
		android.support.v7.widget.ShareActionProvider provider =
				(android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(menuAppShare);
		String subject = getString(R.string.lbl_share_app_title);
		String text = getString(R.string.lbl_share_app_content, getString(R.string.application_name),
				Prefs.getInstance().getAppDownloadInfo());
		provider.setShareIntent(Utils.getDefaultShareIntent(provider, subject, text));
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.action_about:
			showDialogFragment(AboutDialogFragment.newInstance(this), null);
			break;

		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
		showAppList();
	}

	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		showAppList();
	}

	/**
	 * Show all external applications links.
	 */
	private void showAppList() {
		getSupportFragmentManager().beginTransaction().replace(R.id.app_list_fl, AppListImpFragment.newInstance(this))
				.commit();
	}


	/**
	 * Update current position on the map.
	 *
	 * @param location
	 * 		Current location.
	 */
	private void updateCurLocal(Location location) {
		if (mMap != null) {
			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
					location.getLongitude()), 16);
			mMap.moveCamera(update);
		}
		Utils.showShortToast(App.Instance, "updateCurLocal");
	}

	/**
	 * Remove progress-indicator.
	 */
	private void dismissProgressIndicator() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.hide();
		}
	}

	/**
	 * Show progress-indicator.
	 */
	private void showProgressIndicator() {
		dismissProgressIndicator();
		mProgressDialog = ProgressDialog.show(this, getString(R.string.lbl_download), getString(R.string.lbl_wait));
	}
}
