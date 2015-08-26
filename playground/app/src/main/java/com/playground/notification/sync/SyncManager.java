package com.playground.notification.sync;

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
import com.playground.notification.ds.Playground;
import com.playground.notification.ds.sync.Favorite;
import com.playground.notification.ds.sync.SyncPlayground;

import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.SaveListener;


public  abstract class SyncManager<T extends SyncPlayground> {
	private List<T> mCachedList = new LinkedList<>();
	private boolean mInit;
	/**
	 * No one can create this class.
	 */
	protected SyncManager() {
	}



	public boolean isCached(Playground ground) {
		return ground != null && mCachedList.contains(ground);
	}



	@Nullable
	public T findInCache(Playground ground) {
		for (T cached : mCachedList) {
			if (cached.equals(ground)) {
				return cached;
			}
		}
		return null;
	}


	protected void add(T newT, ImageView v, View viewForSnack) {
		//Same bookmark should not be added again.
		if (mCachedList.contains(newT)) {
			return;
		}
		mCachedList.add(newT);
		v.setImageResource(getAddedIcon());
		v.setEnabled(false);
		addInternal(newT, v, viewForSnack);
	}


	private void addInternal(final T newT, final ImageView v, View viewForSnack) {
		final WeakReference<View> anchor = new WeakReference<>(viewForSnack);
		final WeakReference<ImageView> actionBtn = new WeakReference<>(v);
		newT.save(App.Instance, new SaveListener() {
			@Override
			public void onSuccess() {
				View anchorV = anchor.get();
				View btn = actionBtn.get();
				if (anchorV != null) {
					Snackbar.make(anchorV, getAddSuccessText(), Snackbar.LENGTH_SHORT).show();
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
									addInternal(newT, actionBtn.get(), anchor.get());
								}
							}).show();
				}
				if (btn != null) {
					btn.setEnabled(true);
					btn.setImageResource(getRemovedIcon());
				}
			}
		});
	}

	protected void remove(T oldT, ImageView v, View viewForSnack) {
		boolean isCached = isCached(oldT);
		if (isCached) {
			for (T cached : mCachedList) {
				if (cached.equals(oldT)) {
					mCachedList.remove(cached);
					break;
				}
			}
			v.setImageResource(getRemovedIcon());
			v.setEnabled(false);
			removeInternal(oldT, v, viewForSnack);
		}
	}


	private void removeInternal(final T syncPlayground, ImageView v, View viewForSnack) {
		final WeakReference<View> anchor = new WeakReference<>(viewForSnack);
		final WeakReference<ImageView> actionBtn = new WeakReference<>(v);
		syncPlayground.delete(App.Instance, new DeleteListener() {
			@Override
			public void onSuccess() {
				View anchorV = anchor.get();
				View btn = actionBtn.get();
				if (anchorV != null) {
					Snackbar.make(anchorV, getRemoveSuccessText(), Snackbar.LENGTH_SHORT).show();
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
									removeInternal(syncPlayground, actionBtn.get(), anchor.get());
								}
							}).show();
				}
				if (btn != null) {
					btn.setEnabled(true);
					btn.setImageResource(getAddedIcon());
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
	 * @return All cached  {@link Favorite}s.
	 */
	public List<T> getCachedList() {
		return mCachedList;
	}

	protected abstract  int getAddSuccessText() ;

	protected abstract int getRemoveSuccessText() ;

	protected abstract int getAddedIcon();

	protected abstract int getRemovedIcon();

	protected void setInit() {
		mInit = true;
	}

	public boolean isInit() {
		return mInit;
	}
}
