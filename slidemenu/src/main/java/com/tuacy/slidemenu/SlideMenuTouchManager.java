package com.tuacy.slidemenu;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.AbsListView;

class SlideMenuTouchManager implements View.OnTouchListener {

	private static final int INVALID_POINTER = -1;
	/**
	 * 没有在滑动
	 */
	private static final int SLIDING_STATE_NONE = 0;
	/**
	 * 正在手动滑动
	 */
	private static final int SLIDING_STATE_MANUAL = 1;
	/**
	 * 正在自动滑动（手动滑动松开时候有的时候菜单要自动打开或者关闭）
	 */
	private static final int SLIDING_STATE_AUTO = 2;

	private SlideMenuListView mSlideMenuListView;
	private int               mSlideState;
	private int               mDownPosition;
	private int               mDownMotionX;
	private int               mActivePointerId;
	private VelocityTracker   mVelocityTracker;
	private int mTouchSlop;
	private long mConfigShortAnimationTime;

	public SlideMenuTouchManager(SlideMenuListView slideListView) {
		mSlideMenuListView = slideListView;
		mSlideState = SLIDING_STATE_NONE;
		ViewConfiguration configuration = ViewConfiguration.get(slideListView.getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mConfigShortAnimationTime = slideListView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
	}

	private boolean isSliding() {
		return mSlideState != SLIDING_STATE_NONE;
	}

	private int getPointerIndex(MotionEvent event) {
		int pointerIndex = event.findPointerIndex(mActivePointerId);
		if (pointerIndex == INVALID_POINTER) {
			pointerIndex = 0;
			mActivePointerId = event.getPointerId(pointerIndex);
		}
		return pointerIndex;
	}

	private void resetVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		} else {
			mVelocityTracker.clear();
		}
	}

	private void initVelocityTracker() {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	/**
	 *这个函数会SlideMenuListView的onInterceptTouchEvent调用
	 */
	 boolean onInterceptTouchEvent(MotionEvent event) {
		int action = MotionEventCompat.getActionMasked(event);
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				/**
				 * 如果还在滑动，事件没必要往下传递了，可以直接拦截下来
				 */
				if (isSliding()) {
					return true;
				}
				/**
				 * 重置一些参数
				 */
				mDownPosition = AbsListView.INVALID_POSITION;
				mDownMotionX = 0;
				mActivePointerId = INVALID_POINTER;
				int position = mSlideMenuListView.pointToPosition((int) event.getX(), (int) event.getY());
				if (position == AbsListView.INVALID_POSITION) {
					break;
				}
				//TODO:判断是否可以Slide
				//TODO:Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB
				mDownPosition = position;
				mActivePointerId = event.getPointerId(0);
				mDownMotionX = (int) event.getX();
				resetVelocityTracker();
				mVelocityTracker.addMovement(event);
				break;
			case MotionEvent.ACTION_MOVE:
				if (mDownPosition == AbsListView.INVALID_POSITION) {
					break;
				}
				if (mSlideMenuListView.isScrolling()) {
					break;
				}
				int pointerIndex = getPointerIndex(event);
				initVelocityTracker();
				mVelocityTracker.addMovement(event);
				mVelocityTracker.computeCurrentVelocity(1000);
				float velocityX = Math.abs(mVelocityTracker.getXVelocity(mActivePointerId));
				float velocityY = Math.abs(mVelocityTracker.getYVelocity(mActivePointerId));
				boolean isScrollX = velocityX > velocityY;
				int distance = Math.abs((int) event.getX(pointerIndex) - mDownMotionX);
				if (isScrollX && distance > mTouchSlop) {
					ViewParent parent = mSlideMenuListView.getParent();
					if (parent != null) {
						parent.requestDisallowInterceptTouchEvent(true);
					}
					mSlideState = SLIDING_STATE_MANUAL;
					return true;
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!mSlideMenuListView.isEnabled() || !mSlideMenuListView.isSlideEnable()) {
			return false;
		}
		int action = MotionEventCompat.getActionMasked(event);
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			case MotionEvent.ACTION_UP:
				break;
			case MotionEvent.ACTION_CANCEL:
				break;
		}
		return false;
	}

	/**
	 * 指向某项item
	 */
	private class SlideItem {

		private int mPosition;
		private SlideMenuItemLayout mItemLayout;
		private SlideMenuContentLayout mContentLayout;
		private View mLeftView;
		private View mRightView;
		private final int mMaxOffset;
		private final int mMinOffset;
		private int mOffset;

		public SlideItem(int position) {
			mPosition = position;
			mItemLayout = (SlideMenuItemLayout) mSlideMenuListView.getChildAt(
				position - mSlideMenuListView.getFirstVisiblePosition());
			if (mItemLayout == null) {
				throw new NullPointerException("At position:" + position
											   + "child(Item) can not be null.Are your sure you have use createConvertView() method in your adapter");
			}
			mContentLayout = mItemLayout.getContentLayout();
			mLeftView = mItemLayout.getSlideLeftView();
			mRightView = mItemLayout.getSlideRightView();
			SlideMenuMode slideMode =mSlideMenuListView.getSlideAdapter().getPositionSlideMode(
				position - mSlideMenuListView.getHeaderViewsCount());
			if (mLeftView != null &&(slideMode == SlideMenuMode.LEFT || slideMode == SlideMenuMode.BOTH)) {
				mMaxOffset = mLeftView.getWidth();
			} else {
				mMaxOffset = 0;
			}
			if (mRightView != null &&(slideMode == SlideMenuMode.RIGHT || slideMode == SlideMenuMode.BOTH)) {
				mMinOffset = -mRightView.getWidth();
			} else {
				mMinOffset = 0;
			}
		}

		private boolean isOpen() {
			return mOffset != 0;
		}

	}
}
