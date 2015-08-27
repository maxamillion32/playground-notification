package com.playground.notification.app.activities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import com.chopping.bus.CloseDrawerEvent;
import com.chopping.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.playground.notification.app.fragments.MyLocationFragment;
import com.playground.notification.app.fragments.PlaygroundDetailFragment;
import com.playground.notification.bus.EULAConfirmedEvent;
import com.playground.notification.bus.EULARejectEvent;
import com.playground.notification.bus.FavoriteListInitEvent;
import com.playground.notification.bus.NearRingListInitEvent;
import com.playground.notification.databinding.ActivityMapsBinding;
import com.playground.notification.ds.Playground;
import com.playground.notification.ds.Playgrounds;
import com.playground.notification.ds.Request;
import com.playground.notification.sync.FavoriteManager;
import com.playground.notification.sync.MyLocationManager;
import com.playground.notification.sync.NearRingManager;
import com.playground.notification.utils.Prefs;
import com.playground.notification.views.TouchableMapFragment;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapsActivity extends AppActivity {
	private static final int REQ = 0x98;

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
	/**
	 * Current position.
	 */
	private Location mCurrentLocation;

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


	/**
	 * Handler for {@link com.playground.notification.bus.FavoriteListInitEvent}.
	 *
	 * @param e
	 * 		Event {@link com.playground.notification.bus.FavoriteListInitEvent}.
	 */
	public void onEvent(FavoriteListInitEvent e) {
		if (FavoriteManager.getInstance().isInit() && NearRingManager.getInstance().isInit()) {
			mBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			mBinding.drawerLayout.setEnabled(true);
		}
	}

	/**
	 * Handler for {@link com.playground.notification.bus.NearRingListInitEvent}.
	 *
	 * @param e
	 * 		Event {@link com.playground.notification.bus.NearRingListInitEvent}.
	 */
	public void onEvent(NearRingListInitEvent e) {
		if (FavoriteManager.getInstance().isInit() && NearRingManager.getInstance().isInit()) {
			mBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			mBinding.drawerLayout.setEnabled(true);

		}
	}
	//------------------------------------------------


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ConnectGoogleActivity.REQ:
			if (resultCode == RESULT_OK) {
				//Return from google-login.
				//onYouCanUseApp();
			} else {
				ActivityCompat.finishAffinity(this);
			}
			break;
		case REQ:
			switch (resultCode) {
			case Activity.RESULT_OK:
				//				if (!mGoogleApiClient.isConnected()) {
				//					if (!mGoogleApiClient.isConnecting()) {
				//						mGoogleApiClient.connect();
				//					}
				//				}
				break;
			case Activity.RESULT_CANCELED:
				exitAppDialog();
				break;
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
		initAddFunctions();
	}

	/**
	 * Define UI for add my-location.
	 */
	private void initAddFunctions() {
		mBinding.addPaneV.hide();
		mBinding.exitAddBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBinding.currentBtn.setVisibility(View.GONE);
				mBinding.addBtn.show();
				mBinding.addPaneV.hide();
			}
		});
		mBinding.addBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBinding.currentBtn.setVisibility(View.VISIBLE);
				mBinding.addBtn.hide();
				mBinding.addPaneV.show();
			}
		});
		mBinding.currentBtn.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if(mCurrentLocation == null) {
					mBinding.currentBtn.setVisibility(View.GONE);
					mBinding.addBtn.show();
					mBinding.addPaneV.hide();
					Snackbar.make(mBinding.drawerLayout, R.string.lbl_no_current_location, Snackbar.LENGTH_LONG)
							.show();
					return true;
				}
				final LatLng center = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
				showDialogFragment(MyLocationFragment.newInstance(App.Instance, mCurrentLocation.getLatitude(),
						mCurrentLocation.getLongitude(), new Playground(center.latitude, center.longitude)), null);
				return true;
			}
		});
	}

	/**
	 * Ready to use application.
	 */
	private void initUseApp() {
		//User that have used this application and done clear(logout), should go back to login-page.
		Prefs prefs = Prefs.getInstance();
		if (prefs.isEULAOnceConfirmed() && TextUtils.isEmpty(prefs.getGoogleId())) {
			ConnectGoogleActivity.showInstance(this);
		} else if (prefs.isEULAOnceConfirmed() && !TextUtils.isEmpty(prefs.getGoogleId())) {
			onYouCanUseApp();
		}
	}

	/**
	 * Callback for available using of application.
	 */
	private void onYouCanUseApp() {
		initGoogle();
		populateGrounds();
		FavoriteManager.getInstance().init();
		NearRingManager.getInstance().init();
		MyLocationManager.getInstance().init();
		initDrawerContent();
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
	 * Initialize all map infrastructures, location request etc.
	 */
	private void initGoogle() {
		setUpMapIfNeeded();
		if (mMap != null) {
			mapSettings();
		}

		//Location request.
		if (mLocationRequest == null) {
			mLocationRequest = LocationRequest.create();
			mLocationRequest.setInterval(AlarmManager.INTERVAL_HALF_HOUR);
			mLocationRequest.setFastestInterval(AlarmManager.INTERVAL_HALF_HOUR);
			int ty = 0;
			switch (Prefs.getInstance().getBatteryLifeType()) {
			case "0":
				ty = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
				break;
			case "1":
				ty = LocationRequest.PRIORITY_HIGH_ACCURACY;
				break;
			case "2":
				ty = LocationRequest.PRIORITY_LOW_POWER;
				break;
			case "3":
				ty = LocationRequest.PRIORITY_NO_POWER;
				break;
			}
			mLocationRequest.setPriority(ty);
		}

		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(App.Instance).addApi(LocationServices.API)
					.addConnectionCallbacks(new ConnectionCallbacks() {
								@Override
								public void onConnected(Bundle bundle) {
									if (mGoogleApiClient.isConnected()) {
										LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
												mLocationRequest, new LocationListener() {
													@Override
													public void onLocationChanged(Location location) {
														updateCurLocal(location);
													}
												});
									}
									Location location = LocationServices.FusedLocationApi.getLastLocation(
											mGoogleApiClient);
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
							Utils.showShortToast(App.Instance,
									"onConnectionFailed: " + connectionResult.getErrorCode());
						}
					}).build();

			mGoogleApiClient.connect();
		}

		//Setting turn/off location service of system.
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(
				mLocationRequest);
		builder.setAlwaysShow(true);
		builder.setNeedBle(true);
		PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(
				mGoogleApiClient, builder.build());
		result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
			@Override
			public void onResult(LocationSettingsResult result) {
				final Status status = result.getStatus();
				//				final LocationSettingsStates states = result.getLocationSettingsStates();
				switch (status.getStatusCode()) {
				case LocationSettingsStatusCodes.SUCCESS:
					break;
				case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
					try {
						status.startResolutionForResult(MapsActivity.this, REQ);
					} catch (SendIntentException e) {
						exitAppDialog();
					}
					break;
				case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
					exitAppDialog();
					break;
				}
			}
		});
	}

	/**
	 * Force to exit application for no location-service.
	 */
	private void exitAppDialog() {
		new AlertDialog.Builder(MapsActivity.this).setCancelable(false).setTitle(R.string.application_name).setMessage(
				R.string.lbl_no_location_service).setPositiveButton(R.string.btn_confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						ActivityCompat.finishAfterTransition(MapsActivity.this);
					}
				}).create().show();
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
					R.string.app_name) {
				@Override
				public void onDrawerOpened(View drawerView) {
					super.onDrawerOpened(drawerView);
					FavoriteManager favoriteManager = FavoriteManager.getInstance();
					if (favoriteManager.isInit()) {
						mBinding.navView.getMenu().findItem(R.id.action_favorite).setTitle(getString(
								R.string.action_favorite, favoriteManager.getCachedList().size()));
					} else {
						mBinding.navView.getMenu().findItem(R.id.action_favorite).setTitle(getString(
								R.string.action_favorite, 0));
					}
					NearRingManager nearRingManager = NearRingManager.getInstance();
					if (nearRingManager.isInit()) {
						mBinding.navView.getMenu().findItem(R.id.action_near_ring).setTitle(getString(
								R.string.action_near_ring, nearRingManager.getCachedList().size()));
					} else {
						mBinding.navView.getMenu().findItem(R.id.action_near_ring).setTitle(getString(
								R.string.action_near_ring, 0));
					}
				}
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);

		}

		//Navi-head
		getSupportFragmentManager().beginTransaction().replace(R.id.gplus_container, GPlusFragment.newInstance(
				getApplication())).commit();
	}

	@Override
	protected void onResume() {
		if (mCfgLoadDlg != null && mCfgLoadDlg.isShowing()) {
			mCfgLoadDlg.dismiss();
		}
		mCfgLoadDlg = ProgressDialog.show(this, getString(R.string.application_name), getString(R.string.lbl_load_cfg));

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
		mMap.setMyLocationEnabled(true);

		mMap.setIndoorEnabled(true);
		mMap.setBuildingsEnabled(true);

		UiSettings uiSettings = mMap.getUiSettings();
//		uiSettings.setZoomControlsEnabled(true);
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
	 * Extra settings on map.
	 */
	private void mapSettings() {
		Prefs prefs = Prefs.getInstance();
		mMap.setTrafficEnabled(prefs.isTrafficShowing());
		mMap.setMapType(prefs.getMapType().equals("0") ? GoogleMap.MAP_TYPE_NORMAL : GoogleMap.MAP_TYPE_SATELLITE);
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
					mBinding.loadPinPb.setVisibility(View.GONE);
					if (mCurrentLocation == null) {
						Snackbar.make(mBinding.drawerLayout, R.string.lbl_no_current_location, Snackbar.LENGTH_LONG)
								.show();
						return;
					}
					final LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
					//final LatLng center = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
					List<Playground> grounds = playgrounds.getPlaygroundList();
					for (final Playground ground : grounds) {
						LatLng to = new LatLng(ground.getLatitude(), ground.getLongitude());
						MarkerOptions options = new MarkerOptions().position(to);
						FavoriteManager favMgr = FavoriteManager.getInstance();
						if (favMgr.isInit() && favMgr.isCached(ground)) {
							options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_favorite));
						} else {
							com.playground.notification.utils.Utils.changeMarkerIcon(options, currentLatLng, to);
						}
						mMarkerList.put(mMap.addMarker(options), ground);
					}

					mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
						@Override
						public boolean onMarkerClick(Marker marker) {
							for (Marker m : mMarkerList.keySet()) {
								if (m.equals(marker)) {
									showDialogFragment(PlaygroundDetailFragment.newInstance(App.Instance,
											currentLatLng.latitude, currentLatLng.longitude, mMarkerList.get(m)), null);
									break;
								}
							}
							return true;
						}
					});
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

	/**
	 * An indicator dialog for loading app-config
	 */
	private ProgressDialog mCfgLoadDlg;

	@Override
	protected void onAppConfigLoaded() {
		super.onAppConfigLoaded();
		if (mCfgLoadDlg != null && mCfgLoadDlg.isShowing()) {
			mCfgLoadDlg.dismiss();
		}
		showAppList();
		Api.initialize(App.Instance, Prefs.getInstance().getApiHost());

		initUseApp();
	}

	@Override
	protected void onAppConfigIgnored() {
		super.onAppConfigIgnored();
		if (mCfgLoadDlg != null && mCfgLoadDlg.isShowing()) {
			mCfgLoadDlg.dismiss();
		}
		showAppList();
		Api.initialize(App.Instance, Prefs.getInstance().getApiHost());

		initUseApp();
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
		mCurrentLocation = location;
		Utils.showShortToast(App.Instance, "updateCurLocal");
	}


	/**
	 * Set-up of navi-bar left.
	 */
	private void initDrawerContent() {
		mBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		mBinding.drawerLayout.setEnabled(false);
		mBinding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {
				mBinding.drawerLayout.closeDrawer(Gravity.LEFT);

				if (mMap != null) {
					LatLng center = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
					switch (menuItem.getItemId()) {
					case R.id.action_favorite:
						FavoriteManager favoriteManager = FavoriteManager.getInstance();
						if (favoriteManager.getCachedList().size() > 0) {
							ViewPagerActivity.showInstance(MapsActivity.this, center.latitude, center.longitude,
									favoriteManager.getCachedList());
						}
						break;
					case R.id.action_near_ring:
						NearRingManager nearRingManager = NearRingManager.getInstance();
						if (nearRingManager.getCachedList().size() > 0) {
							ViewPagerActivity.showInstance(MapsActivity.this, center.latitude, center.longitude,
									nearRingManager.getCachedList());
						}
						break;
					case R.id.action_settings:
						SettingsActivity.showInstance(MapsActivity.this);
						break;
					case R.id.action_more_apps:
						mBinding.drawerLayout.openDrawer(Gravity.RIGHT);
						break;
					case R.id.action_radar:
						com.playground.notification.utils.Utils.openExternalBrowser(MapsActivity.this,
								"http://" + getString(R.string.support_spielplatz_radar));
						break;
					}
				}
				return true;
			}
		});
	}
}
