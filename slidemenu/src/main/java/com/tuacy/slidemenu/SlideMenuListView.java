package com.tuacy.slidemenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.tuacy.slidemenu.adapter.SlideMenuBaseAdapter;

public class SlideMenuListView extends ListView {

	/**
	 * 当手指抬起的时候，菜单自动打开或者关闭需要的时间
	 */
	private int                   mAnimationTime;
	/**
	 * 统一设定的菜单的模式（左侧可以打开，右侧可以打开，都可以打开，都不可以打开），也可以在Adapter里面单独设置每项
	 */
	private SlideMenuMode         mSlideMenuMode;
	/**
	 * 左侧菜单，是scroll还是reveal
	 */
	private SlideMenuAction       mLeftSlideAction;
	/**
	 * 右侧菜单，是scroll还是reveal
	 */
	private SlideMenuAction       mRightSlideAction;
	/**
	 * 触摸事件管理
	 */
	private SlideMenuTouchManager mTouchManager;
	/**
	 * Slide菜单打开关闭事件监听
	 */
	private OnSlideMenuListener   mSlideMenuListener;
	/**
	 * ListView Item点击事件
	 */
	private OnItemClickListener   mOnItemClickListener;
	/**
	 * ListView滚动事件监听
	 */
	private OnScrollListener      mOnScrollListener;
	/**
	 * ListView是否处于滚动状态
	 */
	private boolean               mScrolling;
	/**
	 * ListView的适配器
	 */
	private SlideMenuBaseAdapter  mAdapter;

	/**
	 * 菜单打开关闭的接口
	 */
	public interface OnSlideMenuListener {

		/**
		 * 菜单打开
		 */
		void onSlideMenuOpen(int position, boolean left);

		/**
		 * 菜单关闭
		 */
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
			mLeftSlideAction = SlideMenuAction.int2Value(styled.getInteger(R.styleable.SlideMenuListView_slide_left_action, 0));
			mRightSlideAction = SlideMenuAction.int2Value(styled.getInteger(R.styleable.SlideMenuListView_slide_right_action, 0));
			styled.recycle();
		}
		mTouchManager = new SlideMenuTouchManager(this);
		setOnTouchListener(mTouchManager);
		// 设置内部使用的滑动监听
		setOnScrollListener(mInnerOnScrollListener);
		// 设置内部item点击事件
		setOnItemClickListener(mInnerOnItemClickListener);
	}

	private OnScrollListener mInnerOnScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == SCROLL_STATE_IDLE) {
				mScrolling = false;
			} else {
				mScrolling = true;
			}
			if (mOnScrollListener != null) {
				mOnScrollListener.onScrollStateChanged(view, scrollState);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (mOnScrollListener != null) {
				mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		}
	};

	private AdapterView.OnItemClickListener mInnerOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (mTouchManager.isOpened()) {
				mTouchManager.closeOpenedItem();
				return;
			}
			if (mOnItemClickListener != null) {
				mOnItemClickListener.onItemClick(parent, view, position, id);
			}
		}
	};

	//	/**
	//	 * 是否允许Slide Menu
	//	 */
	//	public boolean isSlideEnable() {
	//		return mAdapter != null && mSlideMenuMode != SlideMenuMode.NONE;
	//	}

	/**
	 * 内部已经用了一个mInnerOnItemClickListener为了保证外部setOnItemClickListener()可用
	 */
	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		if (listener != mInnerOnItemClickListener) {
			mOnItemClickListener = listener;
		} else {
			super.setOnItemClickListener(listener);
		}
	}

	/**
	 * 内部已经用了一个mInnerOnScrollListener为了保证外部setOnScrollListener()可用
	 */
	@Override
	public void setOnScrollListener(OnScrollListener listener) {
		if (listener != mInnerOnScrollListener) {
			mOnScrollListener = listener;
		} else {
			super.setOnScrollListener(listener);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (isEnabled()) {
			int action = MotionEventCompat.getActionMasked(ev);
			if (action == MotionEvent.ACTION_DOWN) {
				int downPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
				int openedPosition = mTouchManager.getOpenedPosition();
				/**
				 * 之前已经有打开的菜单了
				 */
				if (openedPosition != INVALID_POSITION) {
					if (mTouchManager.sliding()) {
						return false;
					}
					if (downPosition != openedPosition) {
						mTouchManager.closeOpenedItem();
						return false;
					}
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (isEnabled()) {
			return mTouchManager.onInterceptTouchEvent(ev);
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (!(adapter instanceof SlideMenuBaseAdapter)) {
			throw new IllegalArgumentException("Please set SlideMenuBaseAdapter !!!!!!");
		}
		if (mAdapter != null && mInnerDataSetObserver != null) {
			mAdapter.unregisterDataSetObserver(mInnerDataSetObserver);
		}
		mAdapter = (SlideMenuBaseAdapter) adapter;
		mAdapter.setSlideMode(mSlideMenuMode);
		mAdapter.setLeftSlideAction(mLeftSlideAction);
		mAdapter.setRightSlideAction(mRightSlideAction);
		mInnerDataSetObserver = new InnerDataSetObserver();
		mAdapter.registerDataSetObserver(mInnerDataSetObserver);
		super.setAdapter(adapter);
	}

	public SlideMenuBaseAdapter getSlideAdapter() {
		return mAdapter;
	}

	private InnerDataSetObserver mInnerDataSetObserver;

	private class InnerDataSetObserver extends DataSetObserver {

		@Override
		public void onChanged() {
			super.onChanged();
			//TODO:
		}

		@Override
		public void onInvalidated() {
			super.onInvalidated();
			//TODO:
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

	public boolean isScrolling() {
		return mScrolling;
	}

	public SlideMenuAction getLeftSlideMenuAction() {
		return mLeftSlideAction;
	}

	public SlideMenuAction getRightSlideMenuAction() {
		return mRightSlideAction;
	}

	public long getAnimationTime() {
		return mAnimationTime;
	}


}
