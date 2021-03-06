package com.playground.notification.geofence;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v7.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.LatLng;
import com.playground.notification.R;
import com.playground.notification.app.App;
import com.playground.notification.app.activities.MapActivity;
import com.playground.notification.ds.sync.NearRing;
import com.playground.notification.sync.NearRingManager;
import com.playground.notification.utils.PlaygroundIdUtils;
import com.playground.notification.utils.Prefs;
import com.playground.notification.utils.Utils;
import com.tinyurl4j.data.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;

/**
 * Handler on geofence for {@link com.playground.notification.ds.sync.NearRing}s.
 *
 * @author Xinyue Zhao
 */
public final class GeofenceTransitionsIntentService extends Service {
	private NotificationManager                               mNotificationManager;
	private android.support.v4.app.NotificationCompat.Builder mNotifyBuilder;
	private PendingIntent                                     mSharePi;


	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags,   int startId) {
		onHandleIntent(intent);
		return super.onStartCommand(intent, flags, startId);
	}

	protected void onHandleIntent( Intent intent ) {
		mNotificationManager = (NotificationManager) this.getSystemService( Context.NOTIFICATION_SERVICE );
		GeofencingEvent geofencingEvent    = GeofencingEvent.fromIntent( intent );
		int             geofenceTransition = geofencingEvent.getGeofenceTransition();
		if( geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ) {
			List<Geofence> geofences = geofencingEvent.getTriggeringGeofences();

			NearRingManager nearRingManager = NearRingManager.getInstance();
			for( Geofence geofence : geofences ) {
				List<NearRing> rings = nearRingManager.getCachedList();
				for( final NearRing ring : rings ) {
					if(PlaygroundIdUtils.getId(ring).equals(geofence.getRequestId() ) ) {
						final String url = Prefs.getInstance().getGoogleMapSearchHost() + ring.getLatitude() + "," + ring.getLongitude();
						com.tinyurl4j.Api.getTinyUrl( url, new Callback<Response>() {
							@Override
							public void success( com.tinyurl4j.data.Response response, retrofit.client.Response response2 ) {
								String subject = App.Instance.getString( R.string.lbl_share_ground_title );
								String content = App.Instance.getString( R.string.lbl_share_ground_content, response.getResult(),
																		 Prefs.getInstance().getAppDownloadInfo()
								);

								mSharePi = PendingIntent.getActivity( GeofenceTransitionsIntentService.this, (int) System.currentTimeMillis(),
																	  Utils.getShareInformation( subject, content ), PendingIntent.FLAG_UPDATE_CURRENT
								);
								notifyNearRing( ring );
							}

							@Override
							public void failure( RetrofitError error ) {
								String subject = App.Instance.getString( R.string.lbl_share_ground_title );
								String content = App.Instance.getString( R.string.lbl_share_ground_content, url,
																		 Prefs.getInstance().getAppDownloadInfo()
								);

								mSharePi = PendingIntent.getActivity( GeofenceTransitionsIntentService.this, (int) System.currentTimeMillis(),
																	  Utils.getShareInformation( subject, content ), PendingIntent.FLAG_UPDATE_CURRENT
								);
								notifyNearRing( ring );
							}
						} );
					}
				}
			}
		}
	}

	private void notifyNearRing( NearRing ring ) {
		Prefs  prefs   = Prefs.getInstance();
		String latlng  = ring.getLatitude() + "," + ring.getLongitude();
		String maptype = prefs.getMapType().equals( "0" ) ? "roadmap" : "hybrid";
		String url = prefs.getGoogleApiHost() + "maps/api/staticmap?center=" + latlng +
					 "&zoom=16&size=520x300&markers=color:red%7Clabel:S%7C" + latlng + "&key=" +
					 App.Instance.getDistanceMatrixKey() + "&sensor=true&maptype=" + maptype;

		Intent i;
		if( App.Instance != null && App.Instance.getCurrentLocation() != null ) {
			Location location = App.Instance.getCurrentLocation();
			i = Utils.getMapWeb(
					new LatLng( location.getLatitude(), location.getLongitude() ), new LatLng( ring.getLatitude(), ring.getLongitude() ) );
		} else {
			i = new Intent( this, MapActivity.class );
			i.putExtra(MapActivity.EXTRAS_GROUND, (Serializable) ring );
			i.setFlags( Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP );
		}
		PendingIntent contentIntent = PendingIntent.getActivity( this, (int) System.currentTimeMillis(), i, PendingIntent.FLAG_ONE_SHOT );

		try {
			notify( getString( R.string.lbl_notify_title ), getString( R.string.lbl_notify_content ), url, contentIntent  );
		} catch( NullPointerException|IOException|OutOfMemoryError e ) {
			fallbackNotify( getString( R.string.lbl_notify_title ), getString( R.string.lbl_notify_content ), contentIntent );
		}
	}


	private void notify(final String title, final String desc, String image, final PendingIntent contentIntent) throws IOException, OutOfMemoryError {
		Glide.with(this)
		     .load(image)
		     .asBitmap()
		     .into(new SimpleTarget<Bitmap>() {
			     @Override
			     public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
				     mNotifyBuilder = new NotificationCompat.Builder(GeofenceTransitionsIntentService.this).setWhen(System.currentTimeMillis())
				                                                                                           .setSmallIcon(R.drawable.ic_geofence_notify)
				                                                                                           .setTicker(title)
				                                                                                           .setContentTitle(title)
				                                                                                           .setContentText(desc)
				                                                                                           .setStyle(new android.support.v4.app.NotificationCompat.BigPictureStyle().bigPicture(bitmap)
				                                                                                                                                                                    .setBigContentTitle(
						                                                                                                                                                                    title)
				                                                                                                                                                                    .setSummaryText(desc))
				                                                                                           .addAction(R.drawable.ic_share_notification, getString(R.string.action_share), mSharePi)
				                                                                                           .setAutoCancel(true)
				                                                                                           .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_geofence_notify));
				     mNotifyBuilder.setContentIntent(contentIntent);

				     Utils.vibrateSound(GeofenceTransitionsIntentService.this, mNotifyBuilder);

				     mNotificationManager.notify((int) System.currentTimeMillis(), mNotifyBuilder.build());
			     }
		     });

	}

	private void fallbackNotify( String title, String desc, PendingIntent contentIntent ) {
		mNotifyBuilder = new NotificationCompat.Builder( this ).setWhen( System.currentTimeMillis() ).setSmallIcon( R.drawable.ic_geofence_notify )
				.setTicker( title ).setContentTitle( title ).setContentText( desc ).setStyle( new BigTextStyle().bigText( desc )
																									  .setBigContentTitle( title )
																									  .setSummaryText( desc ) ).addAction(
						R.drawable.ic_share_notification, getString( R.string.action_share ), mSharePi ).setAutoCancel( true );
		mNotifyBuilder.setContentIntent( contentIntent );
		mNotificationManager.notify( (int) System.currentTimeMillis(), mNotifyBuilder.build() );
	}
}
