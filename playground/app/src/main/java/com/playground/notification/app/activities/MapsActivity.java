package com.playground.notification.app.activities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.playground.notification.R;
import com.playground.notification.api.Api;
import com.playground.notification.app.App;
import com.playground.notification.app.fragments.AboutDialogFragment;
import com.playground.notification.app.fragments.AppListImpFragment;
import com.playground.notification.app.fragments.GPlusFragment;
import com.playground.notification.app.fragments.PlaygroundDetailFragment;
import com.playground.notification.bus.EULAConfirmedEvent;
import com.playground.notification.bus.EULARejectEvent;
import com.playground.notification.databinding.ActivityMapsBinding;
import com.playground.notification.ds.Playground;
import com.playground.notification.ds.Playgrounds;
import com.playground.notification.ds.Request;
import com.playground.notification.utils.Prefs;
import com.playground.notification.views.TouchableMapFragment;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapsActivity extends AppActivity  {

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
	 * {@code true}: force to load markers, ignore the touch&move effect of map. Default is {@code true}, because as
	 * initializing the map, the markers should be loaded.
	 */
	private volatile boolean mForcedToLoad = true;
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


	private Map<Marker, Playground> mMarkerList = new LinkedHashMap<>();

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
				//Return from google-login.
				initGoogleMap();
				populateGrounds();
			} else {
				ActivityCompat.finishAffinity(this);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		//Init data-binding.
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		//Init application basic elements.
		setUpErrorHandling((ViewGroup) findViewById(R.id.error_content));

		initDrawer();
		initBoard();

		//User that have used this application and done clear(logout), should go back to login-page.
		Prefs prefs = Prefs.getInstance();
		if (prefs.isEULAOnceConfirmed() && TextUtils.isEmpty(prefs.getGoogleId())) {
			ConnectGoogleActivity.showInstance(this);
		} else if (prefs.isEULAOnceConfirmed() && !TextUtils.isEmpty(prefs.getGoogleId())) {
			initGoogleMap();
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
		if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mMap != null) {
			mMap.clear();
			mMarkerList.clear();
		}
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
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

		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
		setUpMapIfNeeded();
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
				populateGrounds();
			}
		});
	}

	/**
	 * Draw grounds on map.
	 */
	private void populateGrounds() {
		if (mForcedToLoad || mMapFragment.isTouchAndMove()) {
			mForcedToLoad = false;
			mBinding.loadPinPb.setVisibility(View.VISIBLE);

			LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
			List<String> filter = new ArrayList<>();
			filter.add("playground");
			Request request = new Request();
			request.setTimestamps(System.currentTimeMillis());
			request.setWidth(App.Instance.getScreenSize().Width);
			request.setHeight(App.Instance.getScreenSize().Height);
			request.setFilter(filter);
			request.setResult(new ArrayList<String>());
			request.setNorth(bounds.northeast.latitude);
			request.setEast(bounds.northeast.longitude);
			request.setSouth(bounds.southwest.latitude);
			request.setWest(bounds.southwest.longitude);

			Api.getPlaygrounds(Prefs.getInstance().getApiSearch(), request, new Callback<Playgrounds>() {
				@Override
				public void success(Playgrounds playgrounds, Response response) {
					List<Playground> grounds = playgrounds.getPlaygroundList();
					final LatLng center = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
					for (final Playground ground : grounds) {
						LatLng to = new LatLng(ground.getLatitude(), ground.getLongitude());
						MarkerOptions options = new MarkerOptions().position(to);
						com.playground.notification.utils.Utils.changeMarkerIcon(options, center, to);
						mMarkerList.put(mMap.addMarker(options), ground);
					}

					mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
						@Override
						public boolean onMarkerClick(Marker marker) {
							for(Marker m : mMarkerList.keySet()) {
								if(m.equals(marker)) {
									showDialogFragment(PlaygroundDetailFragment.newInstance(App.Instance, center.latitude,
											center.longitude, mMarkerList.get(m)), null);
									break;
								}
							}
							return true;
						}
					});
					mBinding.loadPinPb.setVisibility(View.GONE);
				}

				@Override
				public void failure(RetrofitError error) {
					mBinding.loadPinPb.setVisibility(View.GONE);
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
		Api.initialize(App.Instance, Prefs.getInstance().getApiHost());
	}

	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		showAppList();
		Api.initialize(App.Instance, Prefs.getInstance().getApiHost());
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
}
