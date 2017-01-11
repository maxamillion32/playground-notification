package com.playground.notification.app.activities;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.playground.notification.R;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static pub.devrel.easypermissions.AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE;

public final class SplashActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

	private static final int RC_PERMISSIONS = 123;


	private void gotPermissions() {
		MapsActivity.showInstance(this);
		ActivityCompat.finishAfterTransition(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		requirePermissions();
	}


	@SuppressLint("InlinedApi")
	private boolean hasPermissions() {
		return EasyPermissions.hasPermissions(this, permission.READ_PHONE_STATE, permission.WRITE_EXTERNAL_STORAGE, permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION);
	}


	@SuppressLint("InlinedApi")
	@AfterPermissionGranted(RC_PERMISSIONS)
	private void requirePermissions() {
		if (hasPermissions()) {
			gotPermissions();
		} else {
			// Ask for one permission
			EasyPermissions.requestPermissions(this,
			                                   getString(R.string.rationale_permissions),
			                                   RC_PERMISSIONS,
			                                   permission.READ_PHONE_STATE,
			                                   permission.WRITE_EXTERNAL_STORAGE,
			                                   permission.ACCESS_COARSE_LOCATION,
			                                   permission.ACCESS_FINE_LOCATION);
		}
	}


	@Override
	public void onPermissionsGranted(int requestCode, List<String> perms) {
		gotPermissions();
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}


	@Override
	public void onPermissionsDenied(int requestCode, List<String> perms) {
		permissionsDeniedOpenSetting();
	}


	private void permissionsDeniedOpenSetting() {
		if (!hasPermissions()) {
			new AppSettingsDialog.Builder(this, getString(R.string.app_settings_dialog_rationale_ask_again)).setTitle(getString(R.string.app_settings_dialog_title_settings_dialog))
			                                                                                                .setPositiveButton(getString(R.string.app_settings_dialog_setting))
			                                                                                                .setNegativeButton(getString(R.string.app_settings_dialog_cancel),
			                                                                                                                   new DialogInterface.OnClickListener() {
				                                                                                                                   @Override
				                                                                                                                   public void onClick(DialogInterface dialogInterface, int i) {
					                                                                                                                   ActivityCompat.finishAffinity(SplashActivity.this);
				                                                                                                                   }
			                                                                                                                   })
			                                                                                                .build()
			                                                                                                .show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case DEFAULT_SETTINGS_REQ_CODE:
				permissionsDeniedOpenSetting();
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
