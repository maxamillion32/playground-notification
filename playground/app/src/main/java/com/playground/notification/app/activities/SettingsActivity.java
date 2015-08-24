package com.playground.notification.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

import com.playground.notification.R;


/**
 * Setting .
 */
public final class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

	/**
	 * The "ActionBar".
	 */
	private Toolbar mToolbar;



	/**
	 * Show an instance of SettingsActivity.
	 *
	 * @param context
	 * 		A context object.
	 */
	public static void showInstance(Activity context) {
		Intent intent = new Intent(context, SettingsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		mToolbar = (Toolbar) getLayoutInflater().inflate(R.layout.toolbar, null, false);
		addContentView(mToolbar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		mToolbar.setTitle(R.string.action_settings);
		mToolbar.setTitleTextColor(getResources().getColor(R.color.common_white));
		mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		mToolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityCompat.finishAfterTransition(SettingsActivity.this);
			}
		});
		setTitle(R.string.action_settings);


		((MarginLayoutParams) findViewById(android.R.id.list).getLayoutParams()).topMargin = (int) (getActionBarHeight(
				this) * 1.2);
	}


	/**
	 * Get height of {@link android.support.v7.app.ActionBar}.
	 *
	 * @param activity
	 * 		{@link Activity} that hosts an  {@link android.support.v7.app.ActionBar}.
	 *
	 * @return Height of bar.
	 */
	public static int getActionBarHeight(Activity activity) {
		int[] abSzAttr;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			abSzAttr = new int[] { android.R.attr.actionBarSize };
		} else {
			abSzAttr = new int[] { R.attr.actionBarSize };
		}
		TypedArray a = activity.obtainStyledAttributes(abSzAttr);
		return a.getDimensionPixelSize(0, -1);
	}




	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {

		return true;
	}


}
