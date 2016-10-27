package com.tuacy.slidemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class SlideMenuContentLayout extends LinearLayout {

	/**
	 * 当前item的菜单是否是打开的状态
	 */
	private boolean mIsMenuOpen;

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
		/**
		 * 如果item的菜单是打开的状态，事件不用往里面传递了
		 */
		return !mIsMenuOpen && super.dispatchTouchEvent(ev);
	}

	public void setMenuOpenState(boolean isOpen) {
		mIsMenuOpen = isOpen;
	}
}
