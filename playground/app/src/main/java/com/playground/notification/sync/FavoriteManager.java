package com.playground.notification.sync;

import java.util.List;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.bus.FavoriteListLoadingErrorEvent;
import com.playground.notification.ds.Playground;
import com.playground.notification.ds.sync.Favorite;
import com.playground.notification.ds.sync.SyncPlayground;
import com.playground.notification.utils.Prefs;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.FindListener;
import de.greenrobot.event.EventBus;

/**
 * Manger for all favorite locations.
 *
 * @author Xinyue Zhao
 */
public final class FavoriteManager extends SyncManager<Favorite> {
	/**
	 * Singleton.
	 */
	private static FavoriteManager sInstance = new FavoriteManager();

	/**
	 * @return The instance of singleton pattern.
	 */
	public static FavoriteManager getInstance() {
		return sInstance;
	}


	/**
	 * No one can create this class.
	 */
	private FavoriteManager() {
	}

	/**
	 * Init the manager.
	 */
	public void init() {
		//Load from backend.
		BmobQuery<Favorite> q = new BmobQuery<>();
		q.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		q.addWhereEqualTo("mUID", Prefs.getInstance().getGoogleId());
		q.findObjects(App.Instance, new FindListener<Favorite>() {
			@Override
			public void onSuccess(List<Favorite> list) {
				if (getCachedList().size() > 0) {
					getCachedList().clear();
				}
				getCachedList().addAll(list);
				setInit();
			}

			@Override
			public void onError(int i, String s) {
				setInit();
				EventBus.getDefault().post(new FavoriteListLoadingErrorEvent());
			}
		});
	}


	/**
	 * Add new {@link Playground} as {@link Favorite} to remote backend.
	 *
	 * @param newGround
	 * 		A new {@link Playground} to save as {@link Favorite}.
	 * @param v
	 * 		{@link android.widget.ImageView} with which user can save    {@link Playground} as {@link Favorite}.
	 * @param viewForSnack
	 * 		{@link View} anchor for showing {@link Snackbar} messages.
	 */
	public void addFavorite(Playground newGround, android.widget.ImageView v, View viewForSnack) {
		add(new Favorite(Prefs.getInstance().getGoogleId(), newGround), v, viewForSnack);
	}


	/**
	 * Remove  a {@link Favorite} from remote backend.
	 *
	 * @param oldT
	 * 		An old {@link Favorite}.
	 * @param v
	 * 		{@link View} the button to fire the removing.
	 * @param viewForSnack
	 * 		{@link View} anchor for showing {@link Snackbar} messages.
	 */
	public void removeFavorite(SyncPlayground oldT, android.widget.ImageView v, View viewForSnack) {
		Favorite delFav = new Favorite(Prefs.getInstance().getGoogleId(), oldT);
		delFav.setObjectId(oldT.getObjectId());
		remove(delFav, v, viewForSnack);
	}

	@Override
	protected int getAddSuccessText() {
		return R.string.lbl_favorite;
	}

	@Override
	protected int getRemoveSuccessText() {
		return R.string.lbl_remove_favorite;
	}

	@Override
	protected int getAddedIcon() {
		return R.drawable.ic_favorite;
	}

	@Override
	protected int getRemovedIcon() {
		return R.drawable.ic_favorite_outline;
	}
}
