package com.tuacy.slidemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * 包含三个view, 左边slide的view，content view, 右边slide view
 */
public class SlideMenuItemLayout extends RelativeLayout {

	private Context                mContext;
	private View                   mSlideLeftView;
	private View                   mSlideRightView;
	private SlideMenuContentLayout mSlideContentView;
	private SlideMenuAction        mSlideLeftAction;
	private SlideMenuAction        mSlideRightAction;

	public SlideMenuItemLayout(Context context,
							   SlideMenuAction slideLeftAction,
							   SlideMenuAction slideRightAction,
							   int contentId,
							   int leftId,
							   int rightId) {
		super(context);
		mContext = context;
		mSlideLeftAction = slideLeftAction;
		mSlideRightAction = slideRightAction;
	}
}
