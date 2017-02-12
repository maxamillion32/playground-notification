package com.playground.notification.ui.ib.appcompat;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.playground.notification.R;
import com.playground.notification.ui.ib.IBLayoutBase;


/**
 * Created by Xinyue Zhao
 */
public final class IBCompatFrameLayout extends IBLayoutBase<FrameLayout> {
	private static final String TAG = IBCompatFrameLayout.class.getName();


	public IBCompatFrameLayout(Context context) {
		this(context, null);
	}

	public IBCompatFrameLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IBCompatFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected FrameLayout createDragableView(Context context, AttributeSet attrs) {
		return new FrameLayout(context);
	}

	@Override
	protected boolean isReadyForDragStart() {
		boolean isReadyForDragStart = false;
		IBCompatNestedScrollView ibCompatNestedScrollView = (IBCompatNestedScrollView) findViewById(R.id.ib_compat_nested_scrollview);
		if(ibCompatNestedScrollView != null) {
			isReadyForDragStart = ibCompatNestedScrollView.isAtTop();
		}
		return isReadyForDragStart;
	}

	@Override
	protected boolean isReadyForDragEnd() {
		boolean isReadyForDragEnd = false;
		IBCompatNestedScrollView ibCompatNestedScrollView = (IBCompatNestedScrollView) findViewById(R.id.ib_compat_nested_scrollview);
		if(ibCompatNestedScrollView != null) {
			isReadyForDragEnd = ibCompatNestedScrollView.isAtBottom();
		}
		return isReadyForDragEnd;
	}
}
