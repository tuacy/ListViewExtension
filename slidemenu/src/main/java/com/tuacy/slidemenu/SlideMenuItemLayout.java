package com.tuacy.slidemenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * 包含三个view, 左边slide的view，content view, 右边slide view
 * 注意，这里在测量者三个View高度的时候，是以Content View的LayoutParams为基础的
 */
public class SlideMenuItemLayout extends RelativeLayout {

	/**
	 * 左边的菜单View
	 */
	private View                   mSlideLeftView;
	/**
	 * 右边的菜单View
	 */
	private View                   mSlideRightView;
	/**
	 * 实际的内容View
	 */
	private SlideMenuContentLayout mSlideContentView;
	/**
	 * 左边菜单的展开模式
	 */
	private SlideMenuAction        mSlideLeftAction;
	/**
	 * 右边菜单的展开模式
	 */
	private SlideMenuAction        mSlideRightAction;
	/**
	 * item的LayoutParams的高度，Content View为准
	 */
	private int                    mItemParamHeight;

	public SlideMenuItemLayout(Context context,
							   SlideMenuAction slideLeftAction,
							   SlideMenuAction slideRightAction,
							   int contentId,
							   int leftId,
							   int rightId) {
		super(context);
		mSlideLeftAction = slideLeftAction;
		mSlideRightAction = slideRightAction;
		/**
		 * 如果不设置，当item中含有button之类的控件的时候，item响应不了点击事件。
		 */
		setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		init(leftId, contentId, rightId);
	}

	private void init(int left, int content, int right) {
		/**
		 * 获取左侧的菜单View
		 */
		View leftView = null;
		if (left != 0) {
			leftView = LayoutInflater.from(getContext()).inflate(left, this, false);
		}

		/**
		 * 获取右侧的菜单View
		 */
		View rightView = null;
		if (right != 0) {
			rightView = LayoutInflater.from(getContext()).inflate(right, this, false);
		}

		/**
		 * 获取Content View
		 */
		View contentView;
		if (content != 0) {
			contentView = LayoutInflater.from(getContext()).inflate(content, this, false);
		} else {
			throw new NullPointerException("Slide Menu List Content View Can not Null");
		}
		/**
		 * 高度，我们一Content View的高度为基准。Content View的高度我们肯定是给确切的值的
		 */
		RelativeLayout.LayoutParams params = (LayoutParams) contentView.getLayoutParams();
		mItemParamHeight = params.height;
		/**
		 * 添加三个View，注意顺序
		 */
		addLeftView(leftView);
		addRightView(rightView);
		addContentView(contentView);

	}

	private void addLeftView(View leftView) {
		if (leftView == null) {
			return;
		}
		RelativeLayout.LayoutParams params = (LayoutParams) leftView.getLayoutParams();
		if (params == null) {
			params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		}
		switch (mSlideLeftAction) {
			case SCROLL:
				/**
				 * 如果是滑动的方式，那么开始的时候left view在content view的左边。
				 */
				params.addRule(RelativeLayout.LEFT_OF, R.id.slide_id_content_view);
				break;
			case REVEAL:
				/**
				 * 如果是覆盖的方式，那么开始的时候left view在content view的下面。
				 */
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
				break;
		}
		params.height = mItemParamHeight;
		leftView.setLayoutParams(params);
		leftView.setId(R.id.slide_id_left_view);
		addView(leftView);
		mSlideLeftView = leftView;
		setViewShow(leftView, false);
	}

	private void addRightView(View rightView) {
		if (rightView == null) {
			return;
		}
		RelativeLayout.LayoutParams params = (LayoutParams) rightView.getLayoutParams();
		if (params == null) {
			params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		}
		switch (mSlideRightAction) {
			case SCROLL:
				params.addRule(RelativeLayout.RIGHT_OF, R.id.slide_id_content_view);
				break;
			case REVEAL:
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
				break;
		}
		params.height = mItemParamHeight;
		rightView.setLayoutParams(params);
		rightView.setId(R.id.slide_id_right_view);
		addView(rightView);
		mSlideRightView = rightView;
		setViewShow(rightView, false);
	}

	private void addContentView(View contentView) {
		if (contentView == null) {
			throw new NullPointerException("Slide Menu List Content View Can not Null");
		}
		RelativeLayout.LayoutParams params = (LayoutParams) contentView.getLayoutParams();
		if (params == null) {
			params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		}
		SlideMenuContentLayout realContentView = new SlideMenuContentLayout(getContext());
		realContentView.addView(contentView, params);
		realContentView.setId(R.id.slide_id_content_view);

		addView(realContentView, params);
		mSlideContentView = realContentView;
	}

	public void setViewShow(View view, boolean show) {
		if (view == null) {
			return;
		}
		if (show) {
			if (view.getVisibility() != VISIBLE) {
				view.setVisibility(VISIBLE);
			}
		} else {
			if (view.getVisibility() != INVISIBLE) {
				view.setVisibility(INVISIBLE);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int parentWidthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
		int parentHeightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
		if (mSlideLeftView != null) {
			/**
			 * 测量左侧菜单
			 */
			LayoutParams params = (LayoutParams) mSlideLeftView.getLayoutParams();
			int widthSpec = ViewGroup.getChildMeasureSpec(parentWidthSpec, getPaddingLeft() + getPaddingRight() +
																		   params.leftMargin + params.rightMargin, params.width);
			int heightSpec = ViewGroup.getChildMeasureSpec(parentHeightSpec, getPaddingTop() + getPaddingBottom() +
																			 params.topMargin + params.bottomMargin, params.height);
			mSlideLeftView.measure(widthSpec, heightSpec);
		}
		if (mSlideRightView != null) {
			/**
			 * 测量右侧菜单
			 */
			LayoutParams params = (LayoutParams) mSlideRightView.getLayoutParams();
			int widthSpec = ViewGroup.getChildMeasureSpec(parentWidthSpec, getPaddingLeft() + getPaddingRight() +
																		   params.leftMargin + params.rightMargin, params.width);
			int heightSpec = ViewGroup.getChildMeasureSpec(parentHeightSpec, getPaddingTop() + getPaddingBottom() +
																			 params.topMargin + params.bottomMargin, params.height);
			mSlideRightView.measure(widthSpec, heightSpec);
		}

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mSlideLeftView != null) {
			/**
			 * 布局左侧菜单
			 */
			int top = (b - t - mSlideLeftView.getMeasuredHeight()) / 2;
			if (mSlideLeftAction == SlideMenuAction.SCROLL) {
				/**
				 * 显示在content view的左边
				 */
				mSlideLeftView.layout(mSlideContentView.getLeft() - mSlideLeftView.getMeasuredWidth(), top, mSlideContentView.getLeft(),
									  top + mSlideLeftView.getMeasuredHeight());
			} else {
				/**
				 * 显示在content view的下面并且左边对齐
				 */
				mSlideLeftView.layout(mSlideLeftView.getLeft(), top, mSlideLeftView.getRight(), top + mSlideLeftView.getMeasuredHeight());
			}
		}

		if (mSlideRightView != null) {
			/**
			 * 布局右侧菜单
			 */
			int top = (b - t - mSlideRightView.getMeasuredHeight()) / 2;
			if (mSlideRightAction == SlideMenuAction.SCROLL) {
				/**
				 * 显示在content view的右边
				 */
				mSlideRightView.layout(mSlideContentView.getRight(), top, mSlideContentView.getRight() + mSlideRightView.getMeasuredWidth(),
									   top + mSlideRightView.getMeasuredHeight());
			} else {
				/**
				 * 显示在content view的下面并且右边对齐
				 */
				mSlideRightView.layout(mSlideRightView.getLeft(), top, mSlideRightView.getRight(),
									   top + mSlideRightView.getMeasuredHeight());
			}
		}
	}

	public SlideMenuContentLayout getContentLayout() {
		return mSlideContentView;
	}

	public View getSlideLeftView() {
		return mSlideLeftView;
	}

	public View getSlideRightView() {
		return mSlideRightView;
	}

}
