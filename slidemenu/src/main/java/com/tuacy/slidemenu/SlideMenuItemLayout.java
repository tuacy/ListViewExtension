package com.tuacy.slidemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * 包含三个view, 左边slide的view，content view, 右边slide view
 */
public class SlideMenuItemLayout extends RelativeLayout {

	private View mSlideLeftView;
	private View mSlideRightView;

	public SlideMenuItemLayout(Context context) {
		this(context, null);
	}

	public SlideMenuItemLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideMenuItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
}
