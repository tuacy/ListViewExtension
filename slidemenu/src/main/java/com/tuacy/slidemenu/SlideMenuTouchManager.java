package com.tuacy.slidemenu;

import android.view.MotionEvent;
import android.view.View;

class SlideMenuTouchManager implements View.OnTouchListener {

	private SlideMenuListView mSlideMenuListView;

	public SlideMenuTouchManager(SlideMenuListView slideListView) {
		mSlideMenuListView = slideListView;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}
}
