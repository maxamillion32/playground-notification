package com.playground.notification.sync;

import java.util.List;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.ds.Playground;
import com.playground.notification.ds.sync.MyLocation;
import com.playground.notification.ds.sync.NearRing;
import com.playground.notification.utils.Prefs;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.FindListener;

/**
 * Manger for all my own locations.
 *
 * @author Xinyue Zhao
 */
public final class MyLocationManager extends SyncManager<MyLocation> {
	/**
	 * Singleton.
	 */
	private static MyLocationManager sInstance = new MyLocationManager();

	/**
	 * @return The instance of singleton pattern.
	 */
	public static MyLocationManager getInstance() {
		return sInstance;
	}


	/**
	 * No one can create this class.
	 */
	private MyLocationManager() {
	}

	/**
	 * Init the manager.
	 */
	public void init() {
		//Load from backend.
		BmobQuery<MyLocation> q = new BmobQuery<>();
		q.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		q.addWhereEqualTo("mUID", Prefs.getInstance().getGoogleId());
		q.findObjects(App.Instance, new FindListener<MyLocation>() {
			@Override
			public void onSuccess(List<MyLocation> list) {
				if (getCachedList().size() > 0) {
					getCachedList().clear();
				}
				getCachedList().addAll(list);
				setInit();
			}

			@Override
			public void onError(int i, String s) {


			}
		});
	}


	/**
	 * Add new {@link Playground} as my own one to remote backend.
	 *
	 * @param newGround
	 * 		A new {@link Playground} to save as my own one.
	 * 	@param name A label or name of your own 	{@link Playground}.
	 * @param v
	 * 		{@link android.widget.ImageView} with which user can save    {@link Playground} as {@link NearRing}.
	 * @param viewForSnack
	 * 		{@link View} anchor for showing {@link Snackbar} messages.
	 */
	public void addMyLocation(Playground newGround, String name, android.widget.ImageView v, View viewForSnack) {
		add(new MyLocation(Prefs.getInstance().getGoogleId(), name, newGround), v, viewForSnack);
	}


	/**
	 * Remove  a self-defined {@link Playground} from remote backend.
	 *
	 * @param oldT
	 * 		An old {@link MyLocation}.
	 * @param v
	 * 		{@link View} the button to fire the removing.
	 * @param viewForSnack
	 * 		{@link View} anchor for showing {@link Snackbar} messages.
	 */
	public void removeMyLocation(MyLocation oldT, android.widget.ImageView v, View viewForSnack) {
		remove(oldT, v, viewForSnack);
	}

@Override
	protected int getAddSuccessText() {
		return R.string.lbl_saved;
	}

	@Override
	protected int getRemoveSuccessText() {
		return R.string.lbl_deleted;
	}

	@Override
	protected int getAddedIcon() {
		return R.drawable.ic_action_delete ;
	}

	@Override
	protected int getRemovedIcon() {
		return R.drawable.ic_save;
	}
}
