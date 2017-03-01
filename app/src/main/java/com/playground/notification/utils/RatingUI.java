package com.playground.notification.utils;


import com.playground.notification.ds.sync.Rating;

public interface RatingUI {
	void setRating(float rate);
	void setRating(Rating rate);
	void showRating();
	void dismissRating();
}
