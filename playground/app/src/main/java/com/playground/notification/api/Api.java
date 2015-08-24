package com.playground.notification.api;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

import com.playground.notification.ds.Playgrounds;
import com.playground.notification.ds.Request;
import com.playground.notification.ds.google.Matrix;
import com.squareup.okhttp.OkHttpClient;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Api to get all Faroo-feeds.
 *
 * @author Xinyue Zhao
 */
public final class Api {
	/**
	 * For header, cache before request will be out.
	 */
	private final static RequestInterceptor sInterceptor = new RequestInterceptor() {
		@Override
		public void intercept(RequestFacade request) {
			request.addHeader("Content-Type", "application/json");
		}
	};
	private static final String TAG = Api.class.getSimpleName();
	/**
	 * Response-cache.
	 */
	private static com.squareup.okhttp.Cache sCache;
	/**
	 * The host of API.
	 */
	private static String sHost = null;
	/**
	 * The host of Google 's API.
	 */
	private static String sGoogle = "https://maps.googleapis.com/";
	/**
	 * Response-cache size with default value.
	 */
	private static long sCacheSize = 1024 * 10;

	/**
	 * Http-client.
	 */
	private static OkClient sClient = null;
	/**
	 * API methods.
	 */
	private static S s;
	private static G g;
	/**
	 * Init the http-client and cache.
	 */
	private static void initClient(Context cxt) {
		// Create an HTTP client that uses a cache on the file system. Android applications should use
		// their Context to get a cache directory.
		OkHttpClient okHttpClient = new OkHttpClient();
		//		okHttpClient.networkInterceptors().add(new StethoInterceptor());

		File cacheDir = new File(cxt != null ? cxt.getCacheDir().getAbsolutePath() : System.getProperty(
				"java.io.tmpdir"), UUID.randomUUID().toString());
		try {
			sCache = new com.squareup.okhttp.Cache(cacheDir, sCacheSize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		okHttpClient.setCache(sCache);
		okHttpClient.setReadTimeout(3600, TimeUnit.SECONDS);
		okHttpClient.setConnectTimeout(3600, TimeUnit.SECONDS);
		sClient = new OkClient(okHttpClient);


		RestAdapter adapter = new RestAdapter.Builder().setClient(sClient).setRequestInterceptor(sInterceptor)
				.setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint(sHost).build();
		s = adapter.create(S.class);

		adapter = new RestAdapter.Builder().setClient(sClient).setRequestInterceptor(sInterceptor)
				.setLogLevel(RestAdapter.LogLevel.FULL).setEndpoint(sGoogle).build();
		g = adapter.create(G.class);
	}

	/**
	 * Init the http-client and cache.
	 */
	private static void initClient() {
		initClient(null);
	}


	/**
	 * To initialize API.
	 *
	 * @param host
	 * 		The host of API.
	 */
	public static void initialize(Context cxt, String host) {
		sHost = host;
		initClient(cxt);
	}

	/**
	 * Api port.
	 */
	static private interface S {
		@POST("/q/{api}")
		void getPlaygrounds(@Path("api") String api, @Body Request req, Callback<Playgrounds> callback);

	}

	/**
	 * Api port.
	 */
	static private interface G {
		@GET("/maps/api/distancematrix/json")
		void getMatrix(@Query("origins") String origins, @Query("destinations") String destinations,
				@Query("language") String language, @Query("mode") String mode, @Query("key") String key, @Query("units") String units,
				Callback<Matrix> callback);
	}


	public static final void getPlaygrounds(String api, Request req, Callback<Playgrounds> callback) {
		assertCall();
		s.getPlaygrounds(api, req, callback);
	}


	public static final void getMatrix(String origins, String destinations, String language, String mode, String key, String units,
			Callback<Matrix> callback) {
		assertCall();
		g.getMatrix(origins, destinations, language, mode, key, units, callback);
	}


	/**
	 * Assert before calling api.
	 */
	private static void assertCall() {
		if (sClient == null) {//Create http-client when needs.
			initClient();
		}
		if (sHost == null) {//Default when needs.
			sHost = "http://www.faroo.com/";
		}
		Log.i(TAG, String.format("Host:%s, Cache:%d", sHost, sCacheSize));
		if (sCache != null) {
			Log.i(TAG, String.format("RequestCount:%d", sCache.getRequestCount()));
			Log.i(TAG, String.format("NetworkCount:%d", sCache.getNetworkCount()));
			Log.i(TAG, String.format("HitCount:%d", sCache.getHitCount()));
		}
	}

}
