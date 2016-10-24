package com.tuacy.slidemenu;

import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.AbsListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

class SlideMenuTouchManager implements View.OnTouchListener {

	private static final int INVALID_POINTER      = -1;
	/**
	 * 没有在滑动
	 */
	private static final int SLIDING_STATE_NONE   = 0;
	/**
	 * 正在手动滑动
	 */
	private static final int SLIDING_STATE_MANUAL = 1;
	/**
	 * 正在自动滑动（手动滑动松开时候有的时候菜单要自动打开或者关闭）
	 */
	private static final int SLIDING_STATE_AUTO   = 2;

	private SlideMenuListView mSlideMenuListView;
	private int               mSlideState;
	private int               mDownPosition;
	private int               mDownMotionX;
	private int               mActivePointerId;
	private VelocityTracker   mVelocityTracker;
	private int               mTouchSlop;
	private long              mConfigShortAnimationTime;
	private SlideItem         mSlideItem;

	public SlideMenuTouchManager(SlideMenuListView slideListView) {
		mSlideMenuListView = slideListView;
		mSlideState = SLIDING_STATE_NONE;
		ViewConfiguration configuration = ViewConfiguration.get(slideListView.getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mConfigShortAnimationTime = slideListView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
	}

	private long getAnimationTime() {
		long time = mSlideMenuListView.getAnimationTime();
		if (time <= 0) {
			time = mConfigShortAnimationTime;
		}
		return time;
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

	public boolean isOpend() {
		return mSlideItem != null && mSlideItem.isOpen();
	}

	private void move(int offset) {
		// 相对原始位置偏移
		setTranslationX(mSlideItem.mContentLayout, offset);
		if (offset < 0) {
			//右侧菜单展开
			if (mSlideItem.mRightView != null) {
				mSlideItem.mItemLayout.setViewShow(mSlideItem.mRightView, true);
				SlideMenuAction rightAction = mSlideMenuListView.getRightSlideMenuAction();
				/**
				 *SlideMenuAction.SCROLL模式下是要滑动的，REVEAL模式是不用滑动当上层的content滑动之后会显示出来。
				 */
				if (rightAction == SlideMenuAction.SCROLL) {
					setTranslationX(mSlideItem.mRightView, offset);
				}
			}
			/**
			 * 避免过度绘制
			 */
			if (mSlideItem.mLeftView != null) {
				mSlideItem.mItemLayout.setViewShow(mSlideItem.mLeftView, false);
			}
		} else {
			//左侧菜单展开
			if (mSlideItem.mLeftView != null) {
				mSlideItem.mItemLayout.setViewShow(mSlideItem.mLeftView, true);
				SlideMenuAction leftAction = mSlideMenuListView.getLeftSlideMenuAction();
				/**
				 *SlideMenuAction.SCROLL模式下是要滑动的，REVEAL模式是不用滑动当上层的content滑动之后会显示出来。
				 */
				if (leftAction == SlideMenuAction.SCROLL) {
					setTranslationX(mSlideItem.mLeftView, offset);
				}
			}
			/**
			 * 避免过度绘制
			 */
			if (mSlideItem.mRightView != null) {
				mSlideItem.mItemLayout.setViewShow(mSlideItem.mRightView, false);
			}
		}
	}

	private void autoScroll(final int offset, final boolean open) {
		mSlideState = SLIDING_STATE_AUTO;
		int moveTo;
		if (offset < 0) {
			//右侧菜单展开
			/**
			 * moveTo <= 0
			 */
			moveTo = open ? mSlideItem.mMinOffset : 0;
			SlideMenuAction rightAction = mSlideMenuListView.getRightSlideMenuAction();
			if (mSlideItem.mRightView != null && rightAction == SlideMenuAction.SCROLL) {
				/**
				 * 如果是想要打开右侧菜单，那么应该向左滑动
				 */
				animate(mSlideItem.mRightView).translationX(moveTo).setDuration(getAnimationTime());
			}
		} else {
			//左侧菜单展开
			moveTo = open ? mSlideItem.mMaxOffset : 0;
			SlideMenuAction leftAction = mSlideMenuListView.getLeftSlideMenuAction();
			if (mSlideItem.mLeftView != null && leftAction == SlideMenuAction.SCROLL) {
				animate(mSlideItem.mLeftView).translationX(moveTo).setDuration(getAnimationTime());
			}
		}
		/**
		 * 处理content view
		 */
		animate(mSlideItem.mContentLayout).translationX(moveTo).setDuration(getAnimationTime()).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if (mSlideItem == null) {
					return;
				}
				if (open) {
					if (offset < 0) {
						mSlideItem.mOffset = mSlideItem.mMinOffset;
					} else {
						mSlideItem.mOffset = mSlideItem.mMaxOffset;
					}
				} else {
					mSlideItem.mOffset = 0;
				}
				slidingFinish();
			}
		});

	}

	public void closeOpenedItem() {
		if (isOpend()) {
			autoScroll(mSlideItem.mOffset, false);
		}
	}

	private void reset() {
		//TODO:
	}

	private void slidingFinish() {
		mSlideState = SLIDING_STATE_NONE;
		if (mSlideItem.mPreOffset != mSlideItem.mOffset) {
			//TODO:
			if (mSlideItem.mPreOffset != 0) {
				boolean left = mSlideItem.mPreOffset > 0 && mSlideItem.mPreOffset <= mSlideItem.mMaxOffset;
				mSlideMenuListView.notifySlideMenuClose(mSlideItem.mPosition, left);
			}
			if (mSlideItem.mOffset != 0) {
				boolean left = mSlideItem.mOffset > 0 && mSlideItem.mOffset <= mSlideItem.mMaxOffset;
				mSlideMenuListView.notifySlideMenuOpen(mSlideItem.mPosition, left);
			}
		}
		if (mSlideItem.mOffset != 0) {
			mSlideItem.mContentLayout.setMenuOpenState(true);
			mSlideItem.mPreOffset = mSlideItem.mOffset;
			mSlideItem.mPreDelatX = 0;
		} else {
			mSlideItem.mContentLayout.setMenuOpenState(false);
			mSlideItem.mItemLayout.setViewShow(mSlideItem.mLeftView, false);
			mSlideItem.mItemLayout.setViewShow(mSlideItem.mRightView, false);
			mSlideItem = null;
		}
	}

	/**
	 * 这个函数会SlideMenuListView的onInterceptTouchEvent调用
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
				if (isSliding()) {
					return true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mDownPosition == AbsListView.INVALID_POSITION) {
					break;
				}
				if (mSlideMenuListView.isScrolling()) {
					break;
				}
				int pointerIndex = getPointerIndex(event);
				if (mSlideState == SLIDING_STATE_MANUAL) {
					if (mSlideItem == null) {
						mSlideItem = new SlideItem(mDownPosition);
					}
					int deltaX = (int) event.getX(pointerIndex) - mDownMotionX;
					int nextOffset = deltaX - mSlideItem.mPreDelatX + mSlideItem.mOffset;
					mSlideItem.mPreDelatX = deltaX;
					if (nextOffset < mSlideItem.mMinOffset) {
						nextOffset = mSlideItem.mMinOffset;
					}
					if (nextOffset > mSlideItem.mMaxOffset) {
						nextOffset = mSlideItem.mMaxOffset;
					}
					if (mSlideItem.mOffset != nextOffset) {
						mSlideItem.mOffset = nextOffset;
						move(nextOffset);
					}
					return true;
				} else {
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
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mDownPosition == AbsListView.INVALID_POSITION) {
					break;
				}
				if (mSlideItem == null) {
					break;
				}
				if (mSlideState == SLIDING_STATE_MANUAL) {
					int index = getPointerIndex(event);
					int deltaX = (int) event.getX(index) - mDownMotionX;
					if (deltaX == 0) {
						reset();
						return true;
					}
					/**
					 * 当移动的距离是0，左边菜单完全展开，右边菜单完全展开。的情况下。不需要自动滑动了。
					 */
					if (mSlideItem.mOffset == 0 || mSlideItem.mOffset == mSlideItem.mMinOffset ||
						mSlideItem.mOffset == mSlideItem.mMaxOffset) {
						slidingFinish();
						return true;
					}
					/**
					 * 获取我们选定项的slide mode
					 */
					SlideMenuMode slideMode = mSlideMenuListView.getSlideAdapter()
																.getPositionSlideMode(
																	mSlideItem.mPosition - mSlideMenuListView.getHeaderViewsCount());
					// 菜单最终是需要展开还是关闭
					boolean shouldOpen = false;
					if (mSlideItem.mOffset > 0) {
						// 说明左边的slide 菜单展开了
						if (slideMode == SlideMenuMode.LEFT || slideMode == SlideMenuMode.BOTH) {
							/**
							 * 滑动距离超过了四分之一
							 */
							boolean distanceGreater = Math.abs(mSlideItem.mOffset - mSlideItem.mPreOffset) >
													  Math.abs(mSlideItem.mMaxOffset) / (float) 4;
							if (mSlideItem.mOffset - mSlideItem.mPreOffset > 0) {
								// 最终左侧菜单是要打开的
								shouldOpen = distanceGreater;
							} else {
								shouldOpen = !distanceGreater;
							}
						}
					} else {
						// 说明右边的slide 菜单展开了
						if (slideMode == SlideMenuMode.RIGHT || slideMode == SlideMenuMode.BOTH) {
							boolean distanceGreater = Math.abs(mSlideItem.mOffset - mSlideItem.mPreOffset) >
													  Math.abs(mSlideItem.mMinOffset) / (float) 4;
							if (mSlideItem.mOffset - mSlideItem.mPreOffset > 0) {
								shouldOpen = !distanceGreater;
							} else {
								shouldOpen = distanceGreater;
							}
						}
					}
					autoScroll(mSlideItem.mOffset, shouldOpen);
					return true;
				} else {
					if (mSlideMenuListView.isScrolling()) {
						closeOpenedItem();
					}
				}
				break;
			case MotionEvent.ACTION_CANCEL:
			default:
				mSlideState = SLIDING_STATE_NONE;
				break;
		}
		return false;
	}

	/**
	 * 指向某项item
	 */
	private class SlideItem {

		private       int                    mPosition;
		private       SlideMenuItemLayout    mItemLayout;
		private       SlideMenuContentLayout mContentLayout;
		private       View                   mLeftView;
		private       View                   mRightView;
		private final int                    mMaxOffset;
		private final int                    mMinOffset;
		/**
		 * mOffset > 0 : 可以确定是左侧的菜单显示出来了。
		 * mOffset < 0 : 可以确定是右侧的菜单显示出来了。
		 */
		private       int                    mOffset;
		/**
		 * 记录上一次Slide结束时距离（要不是左边菜单打开了，要不是右边菜单打开了，要不就是都没打开）值呢就三种情况mMaxOffset, mMinOffset, 0 注意赋值的时机
		 */
		private       int                    mPreOffset;
		private       int                    mPreDelatX;

		SlideItem(int position) {
			mPosition = position;
			mItemLayout = (SlideMenuItemLayout) mSlideMenuListView.getChildAt(position - mSlideMenuListView.getFirstVisiblePosition());
			if (mItemLayout == null) {
				throw new NullPointerException("At position:" + position +
											   "child(Item) can not be null.Are your sure you have use createConvertView() method in your adapter");
			}
			mContentLayout = mItemLayout.getContentLayout();
			mLeftView = mItemLayout.getSlideLeftView();
			mRightView = mItemLayout.getSlideRightView();
			SlideMenuMode slideMode = mSlideMenuListView.getSlideAdapter()
														.getPositionSlideMode(position - mSlideMenuListView.getHeaderViewsCount());
			if (mLeftView != null && (slideMode == SlideMenuMode.LEFT || slideMode == SlideMenuMode.BOTH)) {
				// 可以最多往右边滑动的距离
				mMaxOffset = mLeftView.getWidth();
			} else {
				mMaxOffset = 0;
			}
			if (mRightView != null && (slideMode == SlideMenuMode.RIGHT || slideMode == SlideMenuMode.BOTH)) {
				// 可以最多往左边滑动的距离
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
