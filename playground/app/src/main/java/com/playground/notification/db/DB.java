package com.playground.notification.db;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;


/**
 * Defines methods that operate on database.
 * <p/>
 * <b>Singleton pattern.</b>
 * <p/>
 * <p/>
 *
 * @author Xinyue Zhao
 */
public final class DB {
	/**
	 * {@link Context}.
	 */
	private Context mContext;
	/**
	 * Impl singleton pattern.
	 */
	private static DB sInstance;
	/**
	 * Helper class that create, delete, update tables of database.
	 */
	private DatabaseHelper mDatabaseHelper;
	/**
	 * The database object.
	 */
	private SQLiteDatabase mDB;

	/**
	 * Constructor of {@link DB}. Impl singleton pattern so that it is private.
	 *
	 * @param cxt
	 * 		{@link Context}.
	 */
	private DB(Context cxt) {
		mContext = cxt;
	}

	/**
	 * Get instance of  {@link  DB} singleton.
	 *
	 * @param cxt
	 * 		{@link Context}.
	 *
	 * @return The {@link DB} singleton.
	 */
	public static DB getInstance(Context cxt) {
		if (sInstance == null) {
			sInstance = new DB(cxt);
		}
		return sInstance;
	}

	/**
	 * Open database.
	 */
	public synchronized void open() {
		mDatabaseHelper = new DatabaseHelper(mContext);
		mDB = mDatabaseHelper.getReadableDatabase();
	}

	/**
	 * Close database.
	 */
	public synchronized void close() {
		mDatabaseHelper.close();
	}

	/**
	 * Get locations between northeast and southwest.
	 * @return The result of locations between.
	 */
	public synchronized Cursor search(LatLng northeast, LatLng southwest) {
		if (mDB == null || !mDB.isOpen()) {
			open();
		}
		String mainQuery = "SELECT * FROM grounds WHERE " + "latitude <= " + northeast.latitude + " AND latitude >= " + southwest.latitude + " AND longitude <= " +  northeast.longitude + " AND longitude >= " +  southwest.longitude;
		return mDB.rawQuery(mainQuery, null);
	}
}
