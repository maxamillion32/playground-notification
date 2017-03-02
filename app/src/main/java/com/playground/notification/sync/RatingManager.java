package com.playground.notification.sync;

import android.text.TextUtils;

import com.chopping.application.LL;
import com.playground.notification.R;
import com.playground.notification.bus.RatingOnLocationsLoadingErrorEvent;
import com.playground.notification.bus.RatingOnLocationsLoadingSuccessEvent;
import com.playground.notification.ds.grounds.Playground;
import com.playground.notification.ds.sync.Rating;
import com.playground.notification.utils.Prefs;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import de.greenrobot.event.EventBus;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * {@link RatingManager} manages cached rating information to enhance showing performance.
 *
 * @author Xinyue Zhao
 */
public final class RatingManager extends SyncManager<Rating> {
	/**
	 * Singleton.
	 */
	private static RatingManager sInstance = new RatingManager();

	/**
	 * @return The instance of singleton pattern.
	 */
	public static RatingManager getInstance() {
		return sInstance;
	}


	/**
	 * No one can create this class.
	 */
	private RatingManager() {
	}


	/**
	 * Init the manager.
	 */
	public void init() {
		LL.d("Start getting list of all ratings on all locations.");
		//Load from backend.
		BmobQuery<Rating> q = new BmobQuery<>();
		q.setCachePolicy(BmobQuery.CachePolicy.CACHE_THEN_NETWORK);
		q.findObjects(new FindListener<Rating>() {
			@Override
			public void done(List<Rating> list, BmobException exp) {
				if (exp != null) {
					onError(exp.toString());
					return;
				}
				if (getCachedList().size() > 0) {
					getCachedList().clear();
				}
				getCachedList().addAll(list);
				setInit();
				EventBus.getDefault()
				        .post(new RatingOnLocationsLoadingSuccessEvent());
				LL.d("Get list of all ratings on all locations.");
			}

			private void onError(String s) {
				LL.e("Cannot do at start getting list of all ratings on all locations:" + (TextUtils.isEmpty(s) ?
				                                                                           "." :
				                                                                           s + "."));
				setInit();
				EventBus.getDefault()
				        .post(new RatingOnLocationsLoadingErrorEvent());
			}
		});
	}


	public static void showPersonalRatingOnLocation(final Playground playground, final RatingUI ratingUI) {
		Observable.fromIterable(getInstance().getCachedList())
		          .subscribeOn(Schedulers.newThread())
		          .filter(new Predicate<Rating>() {
			          @Override
			          public boolean test(Rating rating) throws Exception {
				          return TextUtils.equals(rating.getUID(),
				                                  Prefs.getInstance()
				                                       .getGoogleId()) && TextUtils.equals(rating.getId(), playground.getId());
			          }
		          })
		          .observeOn(AndroidSchedulers.mainThread())
		          .subscribe(new Consumer<Rating>() {
			          @Override
			          public void accept(Rating filteredRating) throws Exception {
				          ratingUI.setRating(filteredRating);
			          }
		          });
	}

	public static void showRatingSummaryOnLocation(final Playground playground, final RatingUI ratingUI) {
		Observable.fromIterable(getInstance().getCachedList())
		          .subscribeOn(Schedulers.newThread())
		          .filter(new Predicate<Rating>() {
			          @Override
			          public boolean test(Rating rating) throws Exception {
				          return TextUtils.equals(rating.getId(), playground.getId());
			          }
		          })
		          .toList()
		          .map(new Function<List<Rating>, Float>() {
			          @Override
			          public Float apply(List<Rating> ratings) throws Exception {
				          Float sum = 0.0f;
				          int count = 0;
				          for (Rating r : ratings) {
					          sum += r.getValue();
					          count++;
				          }
				          return sum / count;
			          }
		          })
		          .observeOn(AndroidSchedulers.mainThread())
		          .subscribe(new Consumer<Float>() {
			          @Override
			          public void accept(Float result) throws Exception {
				          ratingUI.setRating(result);
			          }
		          });
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

	public static interface RatingUI {
		void setRating(float rate);

		void setRating(Rating rate);
	}
}
