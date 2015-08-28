package com.playground.notification.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

import com.playground.notification.R;
import com.playground.notification.utils.Prefs;


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

		initSettings();
	}

	/**
	 * Init list.
	 */
	private void initSettings() {
		Prefs prefs = Prefs.getInstance();

		ListPreference mapType = (ListPreference) findPreference(Prefs.KEY_MAP_TYPES);
		mapType.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateSummary(preference, newValue, R.array.map_types);
				return true;
			}
		});
		String value = prefs.getMapType();
		updateSummary(mapType, value, R.array.map_types);


		ListPreference batteryType = (ListPreference) findPreference(Prefs.KEY_BATTERY_TYPES);
		batteryType.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateSummary(preference, newValue, R.array.battery_life_types);
				return true;
			}
		});
		value = prefs.getBatteryLifeType();
		batteryType.setValue(value);
		updateSummary(batteryType, value, R.array.battery_life_types);

		ListPreference unitsType = (ListPreference) findPreference(Prefs.KEY_UNITS);
		unitsType.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateSummary(preference, newValue, R.array.units_types);
				return true;
			}
		});
		value = prefs.getUnitsType();
		unitsType.setValue(value);
		updateSummary(unitsType, value, R.array.units_types);

		ListPreference transMethodType = (ListPreference) findPreference(Prefs.KEY_TRANSPORTATION);
		transMethodType.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateSummary(preference, newValue, R.array.transportation_types);
				return true;
			}
		});
		value = prefs.getTransportationMethod();
		transMethodType.setValue(value);
		updateSummary(transMethodType, value, R.array.transportation_types);


		ListPreference alarmArea = (ListPreference) findPreference(Prefs.KEY_ALARM_AREA);
		alarmArea.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateSummary(preference, newValue, R.array.area_types);
				return true;
			}
		});
		value = prefs.getAlarmAreaValue();
		alarmArea.setValue(value);
		updateSummary(alarmArea, value, R.array.area_types);
	}

	/**
	 * Update settings summary.
	 */
	private void updateSummary(Preference preference, Object newValue, int valuesResId) {
		Resources res = getResources();
		int pos = Integer.valueOf(newValue.toString());
		String summary = (res.getStringArray(valuesResId))[pos];
		preference.setSummary(summary);
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
