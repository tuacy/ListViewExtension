package com.tuacy.slidemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class SlideMenuContentLayout extends LinearLayout {

	private boolean mIsOpen;

	public SlideMenuContentLayout(Context context) {
		super(context);
	}

	public SlideMenuContentLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SlideMenuContentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mIsOpen) {
			return false;
		}
		return super.dispatchTouchEvent(ev);
	}

	public void setMenuOpenState(boolean isOpen) {
		mIsOpen = isOpen;
	}
}
