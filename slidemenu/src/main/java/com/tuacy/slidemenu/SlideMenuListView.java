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

	private Context               mContext;
	private int                   mAnimationTime;
	private SlideMenuMode         mSlideMenuMode;
	private SlideMenuAction       mLeftSlideAction;
	private SlideMenuAction       mRightSlideAction;
	private SlideMenuTouchManager mTouchManager;
	private OnSlideMenuListener   mSlideMenuListener;
	// extern Listener
	private OnItemClickListener   mOnItemClickListener;
	private OnScrollListener      mOnScrollListener;
	private boolean               mIsInScrolling;
	private SlideMenuBaseAdapter  mAdapter;

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
			mLeftSlideAction = SlideMenuAction.int2Value(styled.getInteger(R.styleable.SlideMenuListView_slide_left_action, 0));
			mRightSlideAction = SlideMenuAction.int2Value(styled.getInteger(R.styleable.SlideMenuListView_slide_right_action, 0));
			styled.recycle();
		}
		mTouchManager = new SlideMenuTouchManager(this);
		setOnTouchListener(mTouchManager);
		// You can use setOnScrollListener() in your own code
		setOnScrollListener(mInnerOnScrollListener);
		// You can use setOnItemClickListener() in your own code
		setOnItemClickListener(mInnerOnItemClickListener);
	}

	private OnScrollListener mInnerOnScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == SCROLL_STATE_IDLE) {
				mIsInScrolling = false;
			} else {
				mIsInScrolling = true;
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
			//TODO:
			if (mOnItemClickListener != null) {
				mOnItemClickListener.onItemClick(parent, view, position, id);
			}
		}
	};

	public boolean isSlideEnable() {
		return mAdapter != null && mSlideMenuMode != SlideMenuMode.NONE;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (isSlideEnable() && isEnabled()) {
			int action = MotionEventCompat.getActionMasked(ev);
			if (action == MotionEvent.ACTION_DOWN) {
				int downPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
				//TODO:
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (isEnabled() && isSlideEnable()) {
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
		return mIsInScrolling;
	}


}
