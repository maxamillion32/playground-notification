package com.playground.notification.ui.ib.appcompat;


import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;

public final class IBCompatNestedScrollView extends NestedScrollView {
	private static final String TAG = IBCompatNestedScrollView.class.getName();

	private boolean mAtTop = true;

	private boolean mAtBottom;


	public IBCompatNestedScrollView(Context context) {
		super(context);
	}

	public IBCompatNestedScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IBCompatNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
		super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY);
		if (scrollY > oldScrollY) {
			mAtTop = false;
			mAtBottom = false;
		}
		if (scrollY < oldScrollY) {
			mAtTop = false;
			mAtBottom = false;
		}

		if (scrollY == 0) {
			mAtTop = true;
		}

		if (scrollY == (getChildAt(0).getMeasuredHeight() - getMeasuredHeight())) {
			mAtBottom = true;
		}

		Log.d(TAG, "onScrollChanged: isAtTop: " + isAtTop() + ", isAtBottom: " + isAtBottom());
	}

	public boolean isAtTop() {
		return mAtTop;
	}

	public boolean isAtBottom() {
		return mAtBottom;
	}
}
