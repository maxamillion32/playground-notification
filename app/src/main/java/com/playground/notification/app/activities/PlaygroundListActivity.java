package com.playground.notification.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.app.fragments.PlaygroundListFragment;

/**
 * The list-mode of search result.
 *
 * @author Xinyue Zhao
 */
public final class PlaygroundListActivity extends AppBarActivity {

	/**
	 * View's menu.
	 */
	private static final int MENU = R.menu.menu_list;

	/**
	 * Show single instance of {@link PlaygroundListActivity}
	 *
	 * @param cxt {@link PlaygroundListActivity}.
	 */
	public static void showInstance(@NonNull Activity cxt) {
		Intent intent = new Intent(cxt, PlaygroundListActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ActivityCompat.startActivity(cxt, intent, Bundle.EMPTY);
	}

	@Override
	protected void setupContent(@NonNull FrameLayout contentLayout) {
		getSupportFragmentManager().beginTransaction()
		                           .replace(contentLayout.getId(), PlaygroundListFragment.newInstance(App.Instance))
		                           .commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(MENU, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_map_mode:
				supportFinishAfterTransition();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
