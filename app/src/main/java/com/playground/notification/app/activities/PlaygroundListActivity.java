package com.playground.notification.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.app.fragments.PlaygroundListFragment;
import com.playground.notification.bus.BackPressedEvent;
import com.playground.notification.bus.DetailClosedEvent;
import com.playground.notification.bus.DetailShownEvent;
import com.playground.notification.ds.grounds.Playground;

import java.io.Serializable;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * The list-mode of search result.
 *
 * @author Xinyue Zhao
 */
public final class PlaygroundListActivity extends AppBarActivity {

	private static final String EXTRAS_PLAYGROUND_LIST = PlaygroundListActivity.class.getName() + ".EXTRAS.playground.list";
	/**
	 * View's menu.
	 */
	private static final int MENU = R.menu.menu_list;

	private boolean mItemSelected;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link DetailShownEvent}.
	 * @param e Event {@link DetailShownEvent}.
	 */
	public void onEvent(DetailShownEvent e) {
		mItemSelected = true;
	}


	/**
	 * Handler for {@link DetailClosedEvent}.
	 * @param e Event {@link DetailClosedEvent}.
	 */
	public void onEvent(DetailClosedEvent e) {
		mItemSelected = false;
	}
	//------------------------------------------------

	/**
	 * Show single instance of {@link PlaygroundListActivity}
	 *
	 * @param cxt            {@link Activity}.
	 * @param playgroundList A list of {@link Playground}.
	 */
	public static void showInstance(@NonNull Activity cxt, @Nullable List<Playground> playgroundList) {
		if (playgroundList == null) {
			return;
		}
		Intent intent = new Intent(cxt, PlaygroundListActivity.class);
		intent.putExtra(EXTRAS_PLAYGROUND_LIST, (Serializable) playgroundList);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		ActivityCompat.startActivity(cxt, intent, Bundle.EMPTY);
	}

	@Override
	protected void setupContent(@NonNull FrameLayout contentLayout) {
		Intent intent = getIntent();
		if (intent == null) {
			return;
		}
		List<Playground> playgroundList = (List<Playground>) intent.getSerializableExtra(EXTRAS_PLAYGROUND_LIST);
		getSupportFragmentManager().beginTransaction()
		                           .replace(contentLayout.getId(), PlaygroundListFragment.newInstance(App.Instance, playgroundList))
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

	@Override
	public void onBackPressed() {
		if(mItemSelected) {
			EventBus.getDefault()
			        .post(new BackPressedEvent());
		} else {
			super.onBackPressed();
		}
	}
}
