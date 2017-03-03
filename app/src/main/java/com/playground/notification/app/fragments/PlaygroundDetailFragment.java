package com.playground.notification.app.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.chopping.application.LL;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.playground.notification.R;
import com.playground.notification.api.Api;
import com.playground.notification.api.ApiNotInitializedException;
import com.playground.notification.app.App;
import com.playground.notification.app.activities.AppActivity;
import com.playground.notification.app.activities.MapActivity;
import com.playground.notification.bus.DetailClosedEvent;
import com.playground.notification.bus.DetailShownEvent;
import com.playground.notification.bus.OpenRouteEvent;
import com.playground.notification.bus.ShowLocationRatingEvent;
import com.playground.notification.bus.ShowStreetViewEvent;
import com.playground.notification.databinding.PlaygroundDetailBinding;
import com.playground.notification.databinding.RatingDialogBinding;
import com.playground.notification.ds.google.Matrix;
import com.playground.notification.ds.grounds.Playground;
import com.playground.notification.ds.sync.Rating;
import com.playground.notification.ds.sync.SyncPlayground;
import com.playground.notification.sync.FavoriteManager;
import com.playground.notification.sync.NearRingManager;
import com.playground.notification.sync.RatingManager;
import com.playground.notification.ui.RouteCalcClientPicker;
import com.playground.notification.utils.PlaygroundIdUtils;
import com.playground.notification.utils.Prefs;
import com.playground.notification.utils.Utils;

import java.io.Serializable;
import java.util.Locale;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.playground.notification.sync.RatingManager.showPersonalRatingOnLocation;
import static com.playground.notification.sync.RatingManager.showRatingSummaryOnLocation;
import static com.playground.notification.utils.Utils.getBitmapDescriptor;
import static com.playground.notification.utils.Utils.streetViewBitmapHasRealContent;

/**
 * Show details of a playground, address, rating.
 *
 * @author Xinyue Zhao
 */
public final class PlaygroundDetailFragment extends BottomSheetDialogFragment implements RatingManager.RatingUI {
	private static final String EXTRAS_GROUND = PlaygroundDetailFragment.class.getName() + ".EXTRAS.playground";
	private static final String EXTRAS_LAT = PlaygroundDetailFragment.class.getName() + ".EXTRAS.lat";
	private static final String EXTRAS_LNG = PlaygroundDetailFragment.class.getName() + ".EXTRAS.lng";
	private static final String EXTRAS_CLICKABLE = MyLocationFragment.class.getName() + ".EXTRAS.clickable";
	private static final String EXTRAS_PAGE = MyLocationFragment.class.getName() + ".EXTRAS.page";
	/**
	 * Main layout for this component.
	 */
	private static final int LAYOUT = R.layout.fragment_playground_detail;
	/**
	 * Data-binding.
	 */
	private PlaygroundDetailBinding mBinding;

	/**
	 * {@code true} if we show map here, otherwise we show streetview.
	 */
	private boolean mShowMap = true;
	private SupportMapFragment mMapFragment;
	private SupportStreetViewPanoramaFragment mStreetViewPanoramaFragment;
	private BottomSheetBehavior mBehavior;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link OpenRouteEvent}.
	 *
	 * @param e Event {@link OpenRouteEvent}.
	 */
	public void onEvent(OpenRouteEvent e) {
		FragmentActivity activity = getActivity();
		if (activity == null) {
			return;
		}
		RouteCalcClientPicker.show(activity, e.getIntent());
	}

	/**
	 * Handler for {@link com.playground.notification.bus.ShowLocationRatingEvent}.
	 *
	 * @param e Event {@link com.playground.notification.bus.ShowLocationRatingEvent}.
	 */
	public void onEvent(ShowLocationRatingEvent e) {
		if (!getUserVisibleHint()) {
			return;
		}
		AppActivity activity = (AppActivity) getActivity();
		if (activity != null) {
			activity.showDialogFragment(RatingDialogFragment.newInstance(App.Instance, e.getPlayground(), mBinding.getRating()), "rating");
		}
	}
	//------------------------------------------------

	//A dialog to update current rating status of a ground for you.
	public static final class RatingDialogFragment extends DialogFragment {
		/**
		 * Data-binding.
		 */
		private RatingDialogBinding mBinding;

		public static RatingDialogFragment newInstance(Context cxt, Playground playground, Rating rating) {
			Bundle args = new Bundle();
			args.putSerializable("rating", (Serializable) rating);
			args.putSerializable("ground", (Serializable) playground);
			return (RatingDialogFragment) RatingDialogFragment.instantiate(cxt, RatingDialogFragment.class.getName(), args);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_rating, container, false);
		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
			mBinding = DataBindingUtil.bind(view.findViewById(R.id.rating_dialog_vg));
			getDialog().setTitle(R.string.lbl_rating);
			Rating rating = ((Rating) getArguments().getSerializable("rating"));
			if (rating != null) {
				mBinding.setRating(rating);
			}
			view.findViewById(R.id.close_iv)
			    .setOnClickListener(new OnClickListener() {
				    @Override
				    public void onClick(View v) {
					    dismiss();
					    Playground playground = (Playground) getArguments().getSerializable("ground");
					    playground.setId(PlaygroundIdUtils.getId(playground));
					    Rating rating = ((Rating) getArguments().getSerializable("rating"));
					    if (rating == null) {
						    Rating newRating = new Rating(Prefs.getInstance()
						                                       .getGoogleId(), playground);
						    newRating.setValue(mBinding.locationRb.getRating());
						    newRating.save(new SaveListener<String>() {
							    @Override
							    public void done(String s, BmobException exp) {
								    if (exp != null) {
									    LL.d("newRating failed");
									    return;
								    }
								    LL.d("newRating success");
							    }
						    });
					    } else {
						    Rating updateRating = new Rating(Prefs.getInstance()
						                                          .getGoogleId(), playground);
						    updateRating.setValue(mBinding.locationRb.getRating());
						    updateRating.update(rating.getObjectId(), new UpdateListener() {
							    @Override
							    public void done(BmobException exp) {
								    if (exp != null) {
									    LL.d("updateRating failed");
									    return;
								    }
								    LL.d("updateRating success");
							    }
						    });
					    }
				    }
			    });
		}
	}

	/**
	 * New an instance of {@link PlaygroundDetailFragment}.
	 *
	 * @param context    {@link android.content.Context}.
	 * @param fromLat    The latitude of "from" position to {@code playground}.
	 * @param fromLng    The longitude of "from" position to {@code playground}.
	 * @param playground {@link Playground}.
	 * @param clickable  {@code true} if the preview map can be clicked and show marker on main map.
	 * @return An instance of {@link PlaygroundDetailFragment}.
	 */
	public static PlaygroundDetailFragment newInstance(Context context, double fromLat, double fromLng, Playground playground, boolean clickable) {
		return newInstance(context, fromLat, fromLng, playground, clickable, false);
	}


	/**
	 * New an instance of {@link PlaygroundDetailFragment}.
	 *
	 * @param context    {@link android.content.Context}.
	 * @param fromLat    The latitude of "from" position to {@code playground}.
	 * @param fromLng    The longitude of "from" position to {@code playground}.
	 * @param playground {@link Playground}.
	 * @param clickable  {@code true} if the preview map can be clicked and show marker on main map.
	 * @param page       The host of this {@link PlaygroundDetailFragment} is {@link com.playground.notification.app.activities.ViewPagerActivity}.
	 * @return An instance of {@link PlaygroundDetailFragment}.
	 */
	public static PlaygroundDetailFragment newInstance(Context context, double fromLat, double fromLng, Playground playground, boolean clickable, boolean page) {
		Bundle args = new Bundle();
		args.putDouble(EXTRAS_LAT, fromLat);
		args.putDouble(EXTRAS_LNG, fromLng);
		args.putSerializable(EXTRAS_GROUND, (Serializable) playground);
		args.putBoolean(EXTRAS_CLICKABLE, clickable);
		args.putBoolean(EXTRAS_PAGE, page);
		return (PlaygroundDetailFragment) PlaygroundDetailFragment.instantiate(context, PlaygroundDetailFragment.class.getName(), args);
	}


	@Override
	public void onStart() {
		super.onStart();
		if (mBehavior != null) {
			mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
		}
	}

	@Override
	public void onResume() {
		EventBus.getDefault()
		        .register(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		EventBus.getDefault()
		        .unregister(this);
		super.onPause();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(LAYOUT, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initView(view);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		View view = View.inflate(getContext(), LAYOUT, null);

		dialog.setContentView(view);
		mBehavior = BottomSheetBehavior.from((View) view.getParent());
		return dialog;
	}

	private void initView(View view) {
		Bundle args = getArguments();
		final Playground playground = (Playground) args.getSerializable(EXTRAS_GROUND);
		LL.d("Ground ID: " + playground.getId());
		if (playground != null) {

			final double lat = args.getDouble(EXTRAS_LAT);
			final double lng = args.getDouble(EXTRAS_LNG);

			Prefs prefs = Prefs.getInstance();
			mBinding = DataBindingUtil.bind(view.findViewById(R.id.playground_detail_vg));
			if (!prefs.isShowcaseShown(Prefs.KEY_SHOWCASE_NEAR_RING)) {
				mBinding.showcaseVg.setVisibility(View.VISIBLE);
				mBinding.closeBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						ViewPropertyAnimator animator = ViewPropertyAnimator.animate(mBinding.showcaseVg);
						animator.alpha(0f)
						        .setDuration(1000)
						        .setListener(new AnimatorListenerAdapter() {
							        @Override
							        public void onAnimationEnd(Animator animation) {
								        super.onAnimationEnd(animation);
								        mBinding.showcaseVg.setVisibility(View.GONE);
							        }
						        })
						        .start();
					}
				});
				prefs.setShowcase(Prefs.KEY_SHOWCASE_NEAR_RING, true);
			}

			mBinding.viewSwitchIbtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					view.setVisibility(View.INVISIBLE);
					mBinding.loadingImgPb.setVisibility(View.VISIBLE);
					mShowMap = !mShowMap;
					mBinding.viewSwitchIbtn.setImageDrawable(AppCompatResources.getDrawable(App.Instance,
					                                                                        mShowMap ?
					                                                                        R.drawable.ic_streetview :
					                                                                        R.drawable.ic_map));
					setPreview();
				}
			});

			final String method;
			switch (prefs.getTransportationMethod()) {
				case "0":
					method = "driving";
					break;
				case "1":
					method = "walking";
					break;
				case "2":
					method = "bicycling";
					break;
				case "3":
					method = "transit";
					break;
				default:
					method = "walking";
					break;
			}
			String units = "metric";
			switch (prefs.getDistanceUnitsType()) {
				case "0":
					units = "metric";
					break;
				case "1":
					units = "imperial";
					break;

			}
			try {
				Api.getMatrix(lat + "," + lng,
				              playground.getLatitude() + "," + playground.getLongitude(),
				              Locale.getDefault()
				                    .getLanguage(),
				              method,
				              App.Instance.getDistanceMatrixKey(),
				              units,
				              new Callback<Matrix>() {
					              @Override
					              public void success(Matrix matrix, Response response) {
						              mBinding.setMatrix(matrix);
						              mBinding.setMode(method);
						              mBinding.setHandler(new EventHandler(lat, lng, playground, mBinding));
					              }

					              @Override
					              public void failure(RetrofitError error) {

					              }
				              });
			} catch (ApiNotInitializedException e) {
				dismiss();
			}


			if (FavoriteManager.getInstance()
			                   .isCached(playground)) {
				mBinding.favIv.setImageResource(R.drawable.ic_favorite);
			}
			if (NearRingManager.getInstance()
			                   .isCached(playground)) {
				mBinding.ringIv.setImageResource(R.drawable.ic_geo_fence);
			}


			//Have you rated?
			showPersonalRatingOnLocation(playground, this);
			showRatingSummaryOnLocation(playground, this);

			//Preview
			setPreview();
		}
	}

	@Override
	public void setRating(Rating rate) {
		mBinding.setRating(rate);
	}

	@Override
	public void setRating(float rate) {
		mBinding.locationRb.setRating(rate);
	}



	private void setPreview() {
		Playground playground = (Playground) getArguments().getSerializable(EXTRAS_GROUND);
		if (playground == null) {
			return;
		}

		mBinding.locationContainer.getLayoutParams().width = (int) App.Instance.getListItemWidth() * 2;
		mBinding.locationContainer.getLayoutParams().height = (int) App.Instance.getListItemHeight() * 2;
		if (mShowMap) {
			showMapLite();
		} else {
			showStreetView();
		}
	}

	private void showStreetView() {
		mBinding.viewSwitchIbtn.setVisibility(View.VISIBLE);
		mBinding.loadingImgPb.setVisibility(View.GONE);
		Playground playground = (Playground) getArguments().getSerializable(EXTRAS_GROUND);
		String url = Prefs.getInstance()
		                  .getApiStreetView(700, 350, new LatLng(playground.getLatitude(), playground.getLongitude()));
		Glide.with(App.Instance)
		     .load(url)
		     .asBitmap()
		     .skipMemoryCache(false)
		     .diskCacheStrategy(DiskCacheStrategy.ALL)
		     .into(new SimpleTarget<Bitmap>() {
			     @Override
			     public void onLoadFailed(Exception e, Drawable errorDrawable) {
				     super.onLoadFailed(e, errorDrawable);
				     mBinding.viewSwitchIbtn.setVisibility(View.VISIBLE);
				     mBinding.loadingImgPb.setVisibility(View.GONE);
				     com.chopping.utils.Utils.showLongToast(getContext(), R.string.streetview_not_available);
				     mBinding.viewSwitchIbtn.performClick();
			     }

			     @Override
			     public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
				     mBinding.viewSwitchIbtn.setVisibility(View.VISIBLE);
				     mBinding.loadingImgPb.setVisibility(View.GONE);
				     boolean streetViewAvail = streetViewBitmapHasRealContent(resource);
				     if (!streetViewAvail) {
					     com.chopping.utils.Utils.showLongToast(getContext(), R.string.streetview_not_available);
					     mBinding.viewSwitchIbtn.performClick();
				     } else {
					     if (mStreetViewPanoramaFragment == null) {
						     mStreetViewPanoramaFragment = SupportStreetViewPanoramaFragment.newInstance();
					     }
					     FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
					     fragmentTransaction.replace(R.id.location_container, mStreetViewPanoramaFragment);
					     fragmentTransaction.commit();
					     mStreetViewPanoramaFragment.getStreetViewPanoramaAsync(new OnStreetViewPanoramaReadyCallback() {
						     @Override
						     public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
							     Playground playground = (Playground) getArguments().getSerializable(EXTRAS_GROUND);
							     streetViewPanorama.setPosition(playground.getPosition());
						     }
					     });
					     mBinding.locationContainer.setOnClickListener(new OnClickListener() {
						     @Override
						     public void onClick(View v) {
							     Matrix matrix = mBinding.getMatrix();
							     Playground playground = (Playground) getArguments().getSerializable(EXTRAS_GROUND);
							     if (playground.getPosition() != null && matrix != null && matrix.getDestination() != null && matrix.getDestination()
							                                                                                                        .size() > 0 && matrix.getDestination()
							                                                                                                                             .get(0) != null) {
								     EventBus.getDefault()
								             .post(new ShowStreetViewEvent(matrix.getDestination()
								                                                 .get(0), playground.getPosition()));
							     }
						     }
					     });
				     }
			     }
		     });
	}

	private void showMapLite() {
		mBinding.viewSwitchIbtn.setVisibility(View.VISIBLE);
		mBinding.loadingImgPb.setVisibility(View.GONE);
		if (mMapFragment == null) {
			mMapFragment = SupportMapFragment.newInstance(new GoogleMapOptions().liteMode(true)
			                                                                    .rotateGesturesEnabled(false)
			                                                                    .zoomControlsEnabled(false)
			                                                                    .zoomGesturesEnabled(false)
			                                                                    .scrollGesturesEnabled(false));
		}
		FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.location_container, mMapFragment);
		fragmentTransaction.commit();
		mMapFragment.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				Playground playground = (Playground) getArguments().getSerializable(EXTRAS_GROUND);
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(playground.getPosition(), 16));
				Marker marker = googleMap.addMarker(new MarkerOptions().position(playground.getPosition()));
				marker.setIcon(getBitmapDescriptor(App.Instance, R.drawable.ic_pin_500));

				googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
					@Override
					public void onMapClick(LatLng latLng) {
						//Nothing , just prevent from opening extra map apps.
					}
				});
				if (getArguments().getBoolean(EXTRAS_CLICKABLE)) {
					googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
						@Override
						public void onMapClick(LatLng latLng) {
							Playground playground = (Playground) getArguments().getSerializable(EXTRAS_GROUND);
							MapActivity.showInstance((Activity) mBinding.locationContainer.getContext(), playground);
						}
					});
				}
			}
		});


	}


	/**
	 * Event-handler for all radio-buttons on UI.
	 */
	public static final class EventHandler {
		private final double mLat;
		private final double mLng;
		private final Playground mGround;
		private final PlaygroundDetailBinding mBinding;

		public EventHandler(double fromLat, double fromLng, Playground playground, PlaygroundDetailBinding binding) {
			mLat = fromLat;
			mLng = fromLng;
			mGround = playground;
			mBinding = binding;
		}

		public void onModeSelected(View view) {
			mBinding.setMode(view.getTag()
			                     .toString());
			mBinding.changingPb.setVisibility(View.VISIBLE);

			Prefs prefs = Prefs.getInstance();
			String units = "metric";
			switch (prefs.getDistanceUnitsType()) {
				case "0":
					break;
				case "1":
					break;

			}
			try {
				Api.getMatrix(mLat + "," + mLng,
				              mGround.getLatitude() + "," + mGround.getLongitude(),
				              Locale.getDefault()
				                    .getLanguage(),
				              mBinding.getMode(),
				              App.Instance.getDistanceMatrixKey(),
				              units,
				              new Callback<Matrix>() {
					              @Override
					              public void success(Matrix matrix, Response response) {
						              mBinding.setMatrix(matrix);
						              mBinding.setHandler(new EventHandler(mLat, mLng, mGround, mBinding));
						              mBinding.changingPb.setVisibility(View.GONE);
					              }

					              @Override
					              public void failure(RetrofitError error) {
						              mBinding.changingPb.setVisibility(View.GONE);
					              }
				              });
			} catch (ApiNotInitializedException e) {
				//Ignore this request.
				mBinding.changingPb.setVisibility(View.GONE);
			}
		}

		public void onRatingClicked(@SuppressWarnings("UnusedParameters") View view) {
			EventBus.getDefault()
			        .post(new ShowLocationRatingEvent(mGround));
		}

		public void onSaveFavClicked(@SuppressWarnings("UnusedParameters") View view) {
			FavoriteManager mgr = FavoriteManager.getInstance();
			SyncPlayground favFound = mgr.findInCache(mGround);
			if (favFound == null) {
				mgr.addFavorite(mGround, mBinding.favIv, mBinding.playgroundDetailVg);
			} else {
				mgr.removeFavorite(favFound, mBinding.favIv, mBinding.playgroundDetailVg);
			}
		}

		public void onSaveNearRingClicked(@SuppressWarnings("UnusedParameters") View view) {
			NearRingManager mgr = NearRingManager.getInstance();
			SyncPlayground ringFound = mgr.findInCache(mGround);
			if (ringFound == null) {
				mgr.addNearRing(mGround, mBinding.ringIv, mBinding.playgroundDetailVg);
			} else {
				mgr.removeNearRing(ringFound, mBinding.ringIv, mBinding.playgroundDetailVg);
			}
		}

		public void onGoClicked(@SuppressWarnings("UnusedParameters") View v) {
			EventBus.getDefault()
			        .post(new OpenRouteEvent(com.playground.notification.utils.Utils.getMapWeb(new LatLng(mLat, mLng), new LatLng(mGround.getLatitude(), mGround.getLongitude()))));
		}


		public void onShareGround(@SuppressWarnings("UnusedParameters") View v) {
			final String url = Prefs.getInstance()
			                        .getGoogleMapSearchHost() + mGround.getLatitude() + "," + mGround.getLongitude();
			com.tinyurl4j.Api.getTinyUrl(url, new Callback<com.tinyurl4j.data.Response>() {
				@Override
				public void success(com.tinyurl4j.data.Response response, retrofit.client.Response response2) {
					String subject = App.Instance.getString(R.string.lbl_share_ground_title);
					String content = App.Instance.getString(R.string.lbl_share_ground_content,
					                                        response.getResult(),
					                                        Prefs.getInstance()
					                                             .getAppDownloadInfo());
					mBinding.shareGroundBtn.getContext()
					                       .startActivity(Utils.getShareInformation(subject, content));
				}

				@Override
				public void failure(RetrofitError error) {
					String subject = App.Instance.getString(R.string.lbl_share_ground_title);
					String content = App.Instance.getString(R.string.lbl_share_ground_content,
					                                        url,
					                                        Prefs.getInstance()
					                                             .getAppDownloadInfo());
					mBinding.shareGroundBtn.getContext()
					                       .startActivity(Utils.getShareInformation(subject, content));
				}
			});
		}
	}
}
