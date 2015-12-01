package com.playground.notification.views;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * A wrapper class for wrap map class to handle touch&movement effect.
 *
 * @author Xinyue Zhao
 */
final class TouchableWrapper extends FrameLayout {
	private boolean mDown;
	private boolean move;

	public TouchableWrapper( Context context ) {
		super( context );
	}

	@Override
	public boolean dispatchTouchEvent( MotionEvent event ) {
		switch( event.getAction() ) {
			case MotionEvent.ACTION_DOWN:
				mDown = true;
				break;
			case MotionEvent.ACTION_UP:
				mDown = false;
				move = false;
				break;
			case MotionEvent.ACTION_MOVE:
				move = true;
				break;
		}
		return super.dispatchTouchEvent( event );
	}

	public boolean isTouchAndMove() {
		return mDown && move;
	}
}
