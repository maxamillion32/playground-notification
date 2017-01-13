package com.playground.notification.sync;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.chopping.application.LL;
import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.bus.MyLocationLoadingErrorEvent;
import com.playground.notification.bus.MyLocationLoadingSuccessEvent;
import com.playground.notification.ds.grounds.Playground;
import com.playground.notification.ds.sync.MyLocation;
import com.playground.notification.ds.sync.NearRing;
import com.playground.notification.utils.Prefs;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.FindListener;
import de.greenrobot.event.EventBus;

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
	public   void init() {
		LL.d("Start getting list of myLocation");
		//Load from backend.
		BmobQuery<MyLocation> q = new BmobQuery<>();
		q.setCachePolicy( CachePolicy.NETWORK_ONLY );
		q.addWhereEqualTo( "mUID", Prefs.getInstance().getGoogleId() );
		q.findObjects( App.Instance, new FindListener<MyLocation>() {
			@Override
			public void onSuccess( List<MyLocation> list ) {
				if( getCachedList().size() > 0 ) {
					getCachedList().clear();
				}
				getCachedList().addAll( list );
				setInit();
				EventBus.getDefault().post(new MyLocationLoadingSuccessEvent() );
				LL.d("Get list of myLocation");
			}

			@Override
			public void onError( int i, String s ) {
				setInit();
				EventBus.getDefault().post(new MyLocationLoadingErrorEvent() );
				LL.d("Cant get list of myLocation");
			}
		} );
	}


	/**
	 * Add new {@link Playground} as my own one to remote backend.
	 *
	 * @param newGround
	 * 		A new {@link Playground} to save as my own one.
	 * @param name
	 * 		A label or name of your own 	{@link Playground}.
	 * @param v
	 * 		{@link android.widget.ImageView} with which user can save    {@link Playground} as {@link NearRing}.
	 * @param viewForSnack
	 * 		{@link View} anchor for showing {@link Snackbar} messages.
	 */
	public synchronized void addMyLocation( Playground newGround, String name, android.widget.ImageView v, View viewForSnack ) {
		add( new MyLocation( Prefs.getInstance().getGoogleId(), name, newGround ), v, viewForSnack );
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
	public synchronized void removeMyLocation( MyLocation oldT, android.widget.ImageView v, View viewForSnack ) {
		remove( oldT, v, viewForSnack );
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
		return R.drawable.ic_action_delete;
	}

	@Override
	protected int getRemovedIcon() {
		return R.drawable.ic_save;
	}
}
