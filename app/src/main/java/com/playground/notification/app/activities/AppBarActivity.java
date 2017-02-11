package com.playground.notification.app.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.playground.notification.R;
import com.playground.notification.databinding.AppBarLayoutBinding;


public abstract class AppBarActivity extends AppActivity {

	private static final @LayoutRes int LAYOUT = R.layout.activity_appbar;
	private AppBarLayoutBinding mBinding;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBinding = DataBindingUtil.setContentView(this, LAYOUT);
		setupMain();
		setupContent(mBinding.appbarContent);
	}

	private void setupMain() {
		setSupportActionBar(mBinding.appbar.toolbar);
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				supportFinishAfterTransition();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected abstract void setupContent(@NonNull FrameLayout contentLayout);

	protected final void setupFragment(@IdRes int container, @NonNull Fragment fragment) {
		getSupportFragmentManager().beginTransaction()
		                           .replace(container, fragment)
		                           .commit();
	}


	protected final void setupFragment(@NonNull Fragment fragment) {
		setupFragment(R.id.appbar_content, fragment);
	}


	protected void addViewToCoordinatorLayout(@NonNull View addView) {
		mBinding.coordinatorLayout.addView(addView);
	}

	protected AppBarLayoutBinding getBinding() {
		return mBinding;
	}

	protected void showShortSnackbar(@StringRes int message, @StringRes int buttonLabel, @NonNull View.OnClickListener clickListener) {
		Snackbar.make(mBinding.errorContent, message, Snackbar.LENGTH_SHORT)
		        .setAction(buttonLabel, clickListener)
		        .show();
	}

	protected void showShortSnackbar(@StringRes int message) {
		Snackbar.make(mBinding.errorContent, message, Snackbar.LENGTH_SHORT)
		        .show();
	}


	protected void showLongSnackbar(@StringRes int message, @StringRes int buttonLabel, @NonNull View.OnClickListener clickListener) {
		Snackbar.make(mBinding.errorContent, message, Snackbar.LENGTH_LONG)
		        .setAction(buttonLabel, clickListener)
		        .show();
	}

	protected void showLongSnackbar(@StringRes int message) {
		Snackbar.make(mBinding.errorContent, message, Snackbar.LENGTH_LONG)
		        .show();
	}


	protected void showIndefiniteSnackbar(@StringRes int message, @StringRes int buttonLabel, @NonNull View.OnClickListener clickListener) {
		Snackbar.make(mBinding.errorContent, message, Snackbar.LENGTH_INDEFINITE)
		        .setAction(buttonLabel, clickListener)
		        .show();
	}

	protected void showIndefiniteSnackbar(@StringRes int message) {
		Snackbar.make(mBinding.errorContent, message, Snackbar.LENGTH_LONG)
		        .show();
	}

}
