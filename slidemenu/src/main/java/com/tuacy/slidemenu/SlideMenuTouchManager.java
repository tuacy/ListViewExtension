package com.tuacy.slidemenu;

import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import android.support.v4.view.MotionEventCompat;
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
	private MotionEvent       mPreUpEvent;

	SlideMenuTouchManager(SlideMenuListView slideListView) {
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

	boolean sliding() {
		return mSlideState != SLIDING_STATE_NONE;
	}

	/**
	 * 获取slide menu打开的item对应的position
	 */
	int getOpenedPosition() {
		if (isOpened()) {
			return mSlideItem.mPosition;
		}
		return AbsListView.INVALID_POSITION;
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

	boolean isOpened() {
		return mSlideItem != null && mSlideItem.isOpen();
	}

	/**
	 * 随着手指滑动item，这里要注意参数
	 *
	 * @param offset：要时刻注意这个offset是相对于最初位置的偏移。并不是相对于Down的位置。
	 */
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

	/**
	 * 自动滑动，自动关闭或者打开侧滑菜单
	 *
	 * @param offset：相对于初始位置当前的偏移量
	 * @param open：准备打开还是关闭菜单
	 */
	private void autoScroll(final int offset, final boolean open) {
		mSlideState = SLIDING_STATE_AUTO;
		int moveTo;
		if (offset < 0) {
			//右侧菜单被展开
			/**
			 * moveTo <= 0
			 */
			moveTo = open ? mSlideItem.mMinOffsetInitial : 0;
			SlideMenuAction rightAction = mSlideMenuListView.getRightSlideMenuAction();
			if (mSlideItem.mRightView != null && rightAction == SlideMenuAction.SCROLL) {
				/**
				 * 如果是想要打开右侧菜单，那么应该向左滑动
				 */
				animate(mSlideItem.mRightView).translationX(moveTo).setDuration(getAnimationTime());
			}
		} else {
			//左侧菜单被展开
			moveTo = open ? mSlideItem.mMaxOffsetInitial : 0;
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
						mSlideItem.mOffsetInitial = mSlideItem.mMinOffsetInitial;
					} else {
						mSlideItem.mOffsetInitial = mSlideItem.mMaxOffsetInitial;
					}
				} else {
					mSlideItem.mOffsetInitial = 0;
				}
				slidingFinish();
			}
		});

	}

	void closeOpenedItem() {
		if (isOpened()) {
			autoScroll(mSlideItem.mOffsetInitial, false);
		}
	}

	private void reset() {
		mSlideItem = null;
		mSlideState = SLIDING_STATE_NONE;
	}

	private void slidingFinish() {
		mSlideState = SLIDING_STATE_NONE;
		if (mSlideItem.mOpenStateOffsetInitial != mSlideItem.mOffsetInitial) {
			if (mSlideItem.mOpenStateOffsetInitial != 0) {
				boolean left = mSlideItem.mOpenStateOffsetInitial > 0 && mSlideItem.mOpenStateOffsetInitial <= mSlideItem.mMaxOffsetInitial;
				mSlideMenuListView.notifySlideMenuClose(mSlideItem.mPosition, left);
			}
			if (mSlideItem.mOffsetInitial != 0) {
				boolean left = mSlideItem.mOffsetInitial > 0 && mSlideItem.mOffsetInitial <= mSlideItem.mMaxOffsetInitial;
				mSlideMenuListView.notifySlideMenuOpen(mSlideItem.mPosition, left);
			}
			//TODO:
		}
		if (mSlideItem.mOffsetInitial != 0) {
			/**
			 * slide menu 是打开的状态
			 */
			mSlideItem.mContentLayout.setMenuOpenState(true);
			mSlideItem.mOpenStateOffsetInitial = mSlideItem.mOffsetInitial;
			mSlideItem.mPreOffsetDown = 0;
		} else {
			/**
			 * slide menu 是关闭的状态
			 */
			mSlideItem.mContentLayout.setMenuOpenState(false);
			/**
			 * 避免过度绘制
			 */
			mSlideItem.mItemLayout.setViewShow(mSlideItem.mLeftView, false);
			mSlideItem.mItemLayout.setViewShow(mSlideItem.mRightView, false);
			mSlideItem = null;
			if (mPreUpEvent != null) {
				/**
				 * 当菜单关闭的时候，给一个ACTION_CANCEL事件到ListView，reset一些selector的状态
				 */
				MotionEvent cancelEvent = MotionEvent.obtain(mPreUpEvent);
				cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (mPreUpEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				mSlideMenuListView.onTouchEvent(cancelEvent);
			}
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
				if (sliding()) {
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
				/**
				 * 判断当前down对应的item时候可以slide
				 */
				boolean allowSlide = mSlideMenuListView.getSlideAdapter().isEnabled(position) &&
									 mSlideMenuListView.getSlideAdapter().getPositionSlideMode(position) != SlideMenuMode.NONE;
				if (allowSlide) {
					mDownPosition = position;
					mActivePointerId = event.getPointerId(0);
					mDownMotionX = (int) event.getX();
					resetVelocityTracker();
					mVelocityTracker.addMovement(event);
					// 注意哦，这里光通过ACTION_DOWN我们是是判断不了是否要拦截的
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
		if (mDownPosition == AbsListView.INVALID_POSITION) {
			return false;
		}
		boolean allowSlide = mSlideMenuListView.getSlideAdapter().isEnabled(mDownPosition) &&
							 mSlideMenuListView.getSlideAdapter().getPositionSlideMode(mDownPosition) != SlideMenuMode.NONE;
		/**
		 * 不允许slide menu
		 */
		if (!allowSlide) {
			return false;
		}
		int action = MotionEventCompat.getActionMasked(event);
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (sliding()) {
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
					/**
					 * 可以随手指滑动的情况
					 */
					if (mSlideItem == null) {
						mSlideItem = new SlideItem(mDownPosition);
					}
					/**
					 * 相对于Down时候的位置
					 */
					int offsetDown = (int) event.getX(pointerIndex) - mDownMotionX;
					int offsetInitial = offsetDown - mSlideItem.mPreOffsetDown + mSlideItem.mOffsetInitial;
					mSlideItem.mPreOffsetDown = offsetDown;
					if (offsetInitial < mSlideItem.mMinOffsetInitial) {
						offsetInitial = mSlideItem.mMinOffsetInitial;
					}
					if (offsetInitial > mSlideItem.mMaxOffsetInitial) {
						offsetInitial = mSlideItem.mMaxOffsetInitial;
					}
					/**
					 * 随手指滑动
					 */
					if (mSlideItem.mOffsetInitial != offsetInitial) {
						mSlideItem.mOffsetInitial = offsetInitial;
						move(offsetInitial);
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
				mPreUpEvent = event;
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
					if (mSlideItem.mOffsetInitial == 0 || mSlideItem.mOffsetInitial == mSlideItem.mMinOffsetInitial ||
						mSlideItem.mOffsetInitial == mSlideItem.mMaxOffsetInitial) {
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
					if (mSlideItem.mOffsetInitial > 0) {
						// 说明左边的slide菜单被我们手动拉开了，这个时候我们就得去判断这个左侧菜单的最终停留位置了
						if (slideMode == SlideMenuMode.LEFT || slideMode == SlideMenuMode.BOTH) {
							/**
							 * 滑动距离超过了四分之一
							 */
							boolean distanceGreater = Math.abs(mSlideItem.mOffsetInitial - mSlideItem.mOpenStateOffsetInitial) >
													  Math.abs(mSlideItem.mMaxOffsetInitial) / (float) 4;
							if (mSlideItem.mOffsetInitial - mSlideItem.mOpenStateOffsetInitial > 0) {
								// 最终左侧菜单是要打开的
								shouldOpen = distanceGreater;
							} else {
								shouldOpen = !distanceGreater;
							}
						}
					} else {
						// 说明右边的slide 菜单展开了
						if (slideMode == SlideMenuMode.RIGHT || slideMode == SlideMenuMode.BOTH) {
							boolean distanceGreater = Math.abs(mSlideItem.mOffsetInitial - mSlideItem.mOpenStateOffsetInitial) >
													  Math.abs(mSlideItem.mMinOffsetInitial) / (float) 4;
							if (mSlideItem.mOffsetInitial - mSlideItem.mOpenStateOffsetInitial > 0) {
								shouldOpen = !distanceGreater;
							} else {
								shouldOpen = distanceGreater;
							}
						}
					}
					autoScroll(mSlideItem.mOffsetInitial, shouldOpen);
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

		/**
		 * 当前准备Slide Menu的位置
		 */
		private       int                    mPosition;
		/**
		 * 当前准备Slide Menu的Item View
		 */
		private       SlideMenuItemLayout    mItemLayout;
		/**
		 * Item View的Content View
		 */
		private       SlideMenuContentLayout mContentLayout;
		/**
		 * Item View的Left View
		 */
		private       View                   mLeftView;
		/**
		 * Item View的Right View
		 */
		private       View                   mRightView;
		/**
		 * 最大的偏移位置 是大于0的,可以最多往右边滑动的距离 LeftView的宽度
		 */
		private final int                    mMaxOffsetInitial;
		/**
		 * 最小的偏移位置 是小于0,可以最多往左边滑动的距离 RightView的宽度
		 */
		private final int                    mMinOffsetInitial;
		/**
		 * 相对于出事位置的偏移量
		 * mOffsetInitial > 0 : 可以确定是左侧的菜单显示出来了。
		 * mOffsetInitial < 0 : 可以确定是右侧的菜单显示出来了。
		 */
		private       int                    mOffsetInitial;
		/**
		 * 当菜单打开的时候，记录的相对初始位置的偏移量
		 */
		private       int                    mOpenStateOffsetInitial;
		/**
		 * 前一次相对于手指按下的位置
		 */
		private       int                    mPreOffsetDown;

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
				mMaxOffsetInitial = mLeftView.getWidth();
			} else {
				mMaxOffsetInitial = 0;
			}
			if (mRightView != null && (slideMode == SlideMenuMode.RIGHT || slideMode == SlideMenuMode.BOTH)) {
				// 可以最多往左边滑动的距离
				mMinOffsetInitial = -mRightView.getWidth();
			} else {
				mMinOffsetInitial = 0;
			}
		}

		private boolean isOpen() {
			/**
			 * 只要相对初始位置有偏移，认为菜单是打开的。
			 */
			return mOffsetInitial != 0;
		}

	}
}
