package com.tuacy.slidemenu.adapter;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;

import com.tuacy.slidemenu.SlideMenuAction;
import com.tuacy.slidemenu.SlideMenuItemLayout;
import com.tuacy.slidemenu.SlideMenuMode;

public abstract class SlideMenuBaseAdapter extends BaseAdapter{

	protected Context mContext;
	private SlideMenuMode mSlideMode;
	private SlideMenuAction mSlideLeftAction;
	private SlideMenuAction mSlideRightAction;

	public SlideMenuBaseAdapter(Context context) {
		mContext = context;
		mSlideMode = SlideMenuMode.getDefault();
		mSlideLeftAction = SlideMenuAction.getDefault();
		mSlideRightAction = SlideMenuAction.getDefault();
	}

	public void setSlideMode(SlideMenuMode mode) {
		mSlideMode = mode;
	}

	public void setLeftSlideAction(SlideMenuAction action) {
		mSlideLeftAction = action;
	}

	public void setRightSlideAction(SlideMenuAction action) {
		mSlideRightAction = action;
	}

	public SlideMenuMode getPositionSlideMode(int position) {
		return mSlideMode;
	}

	public abstract int getSlideContentLayoutId(int position);

	public abstract int getSlideLeftLayoutId(int position);

	public abstract int getSlideRightLayoutId(int position);

	protected View createConvertView(int position) {
		return new SlideMenuItemLayout(mContext, mSlideLeftAction, mSlideRightAction, getSlideContentLayoutId(position),
									   getSlideLeftLayoutId(position), getSlideRightLayoutId(position));
	}

}
