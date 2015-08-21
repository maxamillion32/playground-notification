package com.playground.notification.sync;

import java.util.List;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.bus.NearRingListInitEvent;
import com.playground.notification.bus.NearRingListLoadingErrorEvent;
import com.playground.notification.ds.Playground;
import com.playground.notification.ds.sync.NearRing;
import com.playground.notification.ds.sync.SyncPlayground;
import com.playground.notification.utils.Prefs;
import com.software.shell.fab.ActionButton;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.FindListener;
import de.greenrobot.event.EventBus;

/**
 * Manger for all  locations for near-ring feature(geo-fence).
 *
 * @author Xinyue Zhao
 */
public final class NearRingManager extends SyncManager<NearRing> {
	/**
	 * Singleton.
	 */
	private static NearRingManager sInstance = new NearRingManager();

	/**
	 * @return The instance of singleton pattern.
	 */
	public static NearRingManager getInstance() {
		return sInstance;
	}


	/**
	 * No one can create this class.
	 */
	private NearRingManager() {
	}

	/**
	 * Init the manager.
	 */
	public void init() {
		//Load from backend.
		BmobQuery<NearRing> q = new BmobQuery<>();
		q.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		q.addWhereEqualTo("mUID", Prefs.getInstance().getGoogleId());
		q.findObjects(App.Instance, new FindListener<NearRing>() {
			@Override
			public void onSuccess(List<NearRing> list) {
				getCachedList().addAll(list);
				EventBus.getDefault().post(new NearRingListInitEvent());
			}

			@Override
			public void onError(int i, String s) {
				EventBus.getDefault().post(new NearRingListLoadingErrorEvent());
			}
		});
	}


	/**
	 * Add new {@link Playground} as {@link NearRing} to remote backend.
	 *
	 * @param newGround
	 * 		A new {@link Playground} to save as {@link NearRing}.
	 * @param v
	 * 		{@link android.widget.ImageView} with which user can save    {@link Playground} as {@link NearRing}.
	 * @param viewForSnack
	 * 		{@link View} anchor for showing {@link Snackbar} messages.
	 */
	public void addNearRing(Playground newGround, android.widget.ImageView v, View viewForSnack) {
		add(new NearRing(Prefs.getInstance().getGoogleId(), newGround), v, viewForSnack);
	}


	/**
	 * Remove  a {@link NearRing} from remote backend.
	 *
	 * @param oldT
	 * 		An old {@link NearRing}.
	 * @param v
	 * 		{@link ActionButton} the button to fire the removing.
	 * @param viewForSnack
	 * 		{@link View} anchor for showing {@link Snackbar} messages.
	 */
	public void removeNearRing(SyncPlayground oldT, android.widget.ImageView v, View viewForSnack) {
		NearRing delNearRing = new NearRing(Prefs.getInstance().getGoogleId(), oldT);
		delNearRing.setObjectId(oldT.getObjectId());
		remove(delNearRing, v, viewForSnack);
	}

@Override
	protected int getAddSuccessText() {
		return R.string.lbl_near_ring;
	}

	@Override
	protected int getRemoveSuccessText() {
		return R.string.lbl_remove_near_ring;
	}

	@Override
	protected int getAddedIcon() {
		return R.drawable.ic_geo_fence;
	}

	@Override
	protected int getRemovedIcon() {
		return R.drawable.ic_geo_fence_no_check;
	}
}
