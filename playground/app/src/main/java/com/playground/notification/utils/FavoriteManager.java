package com.playground.notification.utils;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.bus.FavoriteListInitEvent;
import com.playground.notification.bus.FavoriteListLoadingErrorEvent;
import com.playground.notification.ds.Favorite;
import com.playground.notification.ds.Playground;
import com.software.shell.fab.ActionButton;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobQuery.CachePolicy;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import de.greenrobot.event.EventBus;

/**
 * Manger for all favorite locations.
 *
 * @author Xinyue Zhao
 */
public final class FavoriteManager {
	/**
	 * Cached list of all {@link com.playground.notification.ds.Favorite}s from backend.
	 */
	private List<Favorite> mCachedList = new LinkedList<>();
	/**
	 * Singleton.
	 */
	private static FavoriteManager sInstance = new FavoriteManager();
	/**
	 * {@code true} if init cache-list.
	 */
	private volatile boolean mInit;

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
	 * For initialize the manger.
	 */
	public synchronized void init() {
		//Load from backend.
		BmobQuery<Favorite> q = new BmobQuery<>();
		q.setCachePolicy(CachePolicy.NETWORK_ELSE_CACHE);
		q.addWhereEqualTo("mUID", Prefs.getInstance().getGoogleId());
		q.findObjects(App.Instance, new FindListener<Favorite>() {
			@Override
			public void onSuccess(List<Favorite> list) {
				mCachedList.addAll(list);
				mInit = true;
				EventBus.getDefault().post(new FavoriteListInitEvent());
			}

			@Override
			public void onError(int i, String s) {
				mInit = false;
				EventBus.getDefault().post(new FavoriteListLoadingErrorEvent());
			}
		});
	}

	/**
	 * To check whether the {@link Playground} had been saved as {@link Favorite} or not.
	 *
	 * @param ground
	 * 		{@link Playground}.
	 *
	 * @return {@code true} was  saved.
	 */
	public boolean isFavorite(Playground ground) {
		return mCachedList.contains(ground);
	}


	/**
	 * Also to check whether the {@link Playground} had been saved as {@link Favorite} or not. </p>Different from {@link
	 * #isFavorite(Playground)} it returns a found object when find.
	 *
	 * @param ground
	 * 		{@link Playground}.
	 *
	 * @return A {@link Favorite} returned if {@code ground} was saved before, otherwise {@code null} returns.
	 */
	@Nullable
	public Favorite findBookmarked(Playground ground) {
		for (Favorite cached : mCachedList) {
			if (cached.equals(ground)) {
				return cached;
			}
		}
		return null;
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
	public void addNewRemoteFavorite(Playground newGround, android.widget.ImageView v, View viewForSnack) {
		//Same bookmark should not be added again.
		if (mCachedList.contains(newGround)) {
			return;
		}
		Favorite newFav=new Favorite( Prefs.getInstance().getGoogleId(),newGround);
		mCachedList.add(newFav);
		v.setImageResource(R.drawable.ic_favorite);
		v.setEnabled(false);
		addNewBookmarkInternal(newFav, v, viewForSnack);
	}

	/**
	 * Add  new {@link Favorite} to backend.
	 *
	 * @param newFavorite
	 * 		A new {@link Favorite}.
	 * @param v
	 * 		{@link android.widget.ImageView} the button to fire the adding.
	 * @param viewForSnack
	 * 		{@link View} anchor for showing {@link Snackbar} messages.
	 */
	private void addNewBookmarkInternal(final Favorite newFavorite, final android.widget.ImageView v,
			View viewForSnack) {
		final WeakReference<View> anchor = new WeakReference<>(viewForSnack);
		final WeakReference<android.widget.ImageView> actionBtn = new WeakReference<>(v);
		newFavorite.save(App.Instance, new SaveListener() {
			@Override
			public void onSuccess() {
				View anchorV = anchor.get();
				View btn = actionBtn.get();
				if (anchorV != null) {
					Snackbar.make(anchorV, R.string.lbl_favorite, Snackbar.LENGTH_SHORT).show();
				}
				if (btn != null) {
					btn.setEnabled(true);
				}
			}

			@Override
			public void onFailure(int i, String s) {
				View anchorV = anchor.get();
				ImageView btn = actionBtn.get();
				if (anchorV != null) {
					Snackbar.make(anchorV, R.string.meta_load_error, Snackbar.LENGTH_LONG).setAction(R.string.btn_retry,
							new OnClickListener() {
								@Override
								public void onClick(View v) {
									addNewBookmarkInternal(newFavorite, actionBtn.get(), anchor.get());
								}
							}).show();
				}
				if (btn != null) {
					btn.setEnabled(true);
					btn.setImageResource(R.drawable.ic_favorite_outline);
				}
			}
		});
	}

	/**
	 * Remove  a {@link Favorite} from remote backend.
	 *
	 * @param favorite
	 * 		An old {@link Favorite}.
	 * @param v
	 * 		{@link ActionButton} the button to fire the removing.
	 * @param viewForSnack
	 * 		{@link View} anchor for showing {@link Snackbar} messages.
	 */
	public void removeRemoteBookmark(Favorite favorite, android.widget.ImageView v, View viewForSnack) {
		boolean isCached = isFavorite(favorite);
		if (isCached) {
			for (Favorite cached : mCachedList) {
				if (cached.equals(favorite)) {
					mCachedList.remove(cached);
					break;
				}
			}
			v.setImageResource(R.drawable.ic_favorite_outline);
			v.setEnabled(false);
			Favorite favDel = new Favorite(Prefs.getInstance().getGoogleId(), favorite);
			favDel.setObjectId(favorite.getObjectId());
			removeBookmarkInternal(favDel, v, viewForSnack);
		}
	}

	/**
	 * Remove a {@link Favorite} from backend.
	 *
	 * @param favorite
	 * 		An old {@link Favorite}.
	 * @param v
	 * 		{@link ActionButton} the button to fire the removing.
	 * @param viewForSnack
	 * 		{@link View} anchor for showing {@link Snackbar} messages.
	 */
	private void removeBookmarkInternal(final Favorite favorite, android.widget.ImageView v, View viewForSnack) {
		final WeakReference<View> anchor = new WeakReference<>(viewForSnack);
		final WeakReference<android.widget.ImageView> actionBtn = new WeakReference<>(v);
		favorite.delete(App.Instance, new DeleteListener() {
			@Override
			public void onSuccess() {
				View anchorV = anchor.get();
				View btn = actionBtn.get();
				if (anchorV != null) {
					Snackbar.make(anchorV, R.string.lbl_remove_favorite, Snackbar.LENGTH_SHORT).show();
				}
				if (btn != null) {
					btn.setEnabled(true);
				}
			}

			@Override
			public void onFailure(int i, String s) {
				View anchorV = anchor.get();
				ImageView btn = actionBtn.get();
				if (anchorV != null) {
					Snackbar.make(anchorV, R.string.meta_load_error, Snackbar.LENGTH_LONG).setAction(R.string.btn_retry,
							new OnClickListener() {
								@Override
								public void onClick(View v) {
									removeBookmarkInternal(favorite, actionBtn.get(), anchor.get());
								}
							}).show();
				}
				if (btn != null) {
					btn.setEnabled(true);
					btn.setImageResource(R.drawable.ic_favorite);
				}
			}
		});
	}

	/**
	 * Clean all tabs.
	 */
	public void clean() {
		mCachedList.clear();
	}

	/**
	 * @return {@code true} if init cache-list.
	 */
	public synchronized boolean isInit() {
		return mInit;
	}

	/**
	 * @return All cached  {@link Favorite}s.
	 */
	public List<Favorite> getCachedList() {
		return mCachedList;
	}
}
