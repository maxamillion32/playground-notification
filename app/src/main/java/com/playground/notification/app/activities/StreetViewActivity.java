package com.playground.notification.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;
import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.app.fragments.StreetViewFragment;
import com.playground.notification.databinding.StreetViewBinding;


/**
 * Show street-view for given location.
 *
 * @author Xinyue Zhao
 */
public final class StreetViewActivity extends AppCompatActivity {

	private static final String EXTRAS_TITLE = StreetViewActivity.class.getName() + ".EXTRAS.";
	private static final String EXTRAS_LOCATION = StreetViewActivity.class.getName() + ".EXTRAS.location";
	private static final int LAYOUT = R.layout.activity_street_view;

	/**
	 * Show single instance of {@link}
	 *
	 * @param cxt {@link Activity}.
	 */
	public static void showInstance(@NonNull Activity cxt, @NonNull String title, @NonNull LatLng location) {
		Intent intent = new Intent(cxt, StreetViewActivity.class);
		intent.putExtra(EXTRAS_TITLE, title);
		intent.putExtra(EXTRAS_LOCATION, location);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ActivityCompat.startActivity(cxt, intent, Bundle.EMPTY);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StreetViewBinding binding = DataBindingUtil.setContentView(this, LAYOUT);
		setSupportActionBar(binding.toolbar);
		handleIntent(getIntent());
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleIntent(getIntent());
	}

	private void handleIntent(Intent intent) {
		String title = intent.getStringExtra(EXTRAS_TITLE);
		LatLng location = intent.getParcelableExtra(EXTRAS_LOCATION);
		Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.street_view_fragment_container);
		StreetViewFragment dialogFragment;
		if (fragment == null) {
			dialogFragment = StreetViewFragment.newInstance(getApplicationContext(), location);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.street_view_fragment_container, dialogFragment);
			ft.commit();
		} else {
			dialogFragment = (StreetViewFragment) fragment;
			dialogFragment.setStreetView(location);
		}
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeAsUpIndicator(AppCompatResources.getDrawable(App.Instance, R.drawable.ic_close_light));
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(title);
		}
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
}
