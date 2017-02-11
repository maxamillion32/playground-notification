package com.playground.notification.ui.ib;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

/**
 * Created by Xinyue Zhao
 */
public interface IBBackground {
	void drawTopShadow(int top, int height, int alpha);

	void drawBottomShadow(int top, int bottom, int alpha);

	void setTouchable(boolean touchable);

	int dpToPx(int dp);

	int getScrollRange();

	void drawOverlay(Canvas canvas);

	void setNeedToDrawSmallShadow(boolean needToDrawSmallShadow);

	void setNeedToDrawShadow(boolean needToDrawShadow);

	@NonNull
	ViewGroup toViewGroup();

	void setCurrentMode(IBLayoutBase.Mode mode);

	void setSelectedPosition(int itemPosition, int listPosition);
}
