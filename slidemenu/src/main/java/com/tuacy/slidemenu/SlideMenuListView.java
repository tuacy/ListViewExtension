package com.tuacy.slidemenu;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ListView;

public class SlideMenuListView extends ListView {

	private Context               mContext;
	private int                   mAnimationTime;
	private SlideMenuMode         mSlideMenuMode;
	private SlideMenuAction       mLeftSldeAction;
	private SlideMenuAction       mRightSldeAction;
	private SlideMenuTouchManager mTouchManager;
	private OnSlideMenuListener   mSlideMenuListener;

	public interface OnSlideMenuListener {

		void onSlideMenuOpen(int position, boolean left);

		void onSlideMenuClose(int position, boolean left);

	}

	public SlideMenuListView(Context context) {
		this(context, null);
	}

	public SlideMenuListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideMenuListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		if (attrs != null) {
			TypedArray styled = getContext().obtainStyledAttributes(attrs, R.styleable.SlideMenuListView);
			mAnimationTime = styled.getInteger(R.styleable.SlideMenuListView_slide_animation_time, 0);
			mSlideMenuMode = SlideMenuMode.int2Value(styled.getInteger(R.styleable.SlideMenuListView_slide_mode, 0));
			mLeftSldeAction = SlideMenuAction.int2Value(styled.getInteger(R.styleable.SlideMenuListView_slide_left_action, 0));
			mRightSldeAction = SlideMenuAction.int2Value(styled.getInteger(R.styleable.SlideMenuListView_slide_right_action, 0));
			styled.recycle();
		}
	}

	public void setOnSlideMenuListener(OnSlideMenuListener listener) {
		mSlideMenuListener = listener;
	}

	public void notifySlideMenuOpen(int position, boolean left) {
		if (mSlideMenuListener != null) {
			mSlideMenuListener.onSlideMenuOpen(position, left);
		}
	}

	public void notifySlideMenuClose(int position, boolean left) {
		if (mSlideMenuListener != null) {
			mSlideMenuListener.onSlideMenuClose(position, left);
		}
	}


}
