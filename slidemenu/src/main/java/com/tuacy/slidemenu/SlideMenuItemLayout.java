package com.tuacy.slidemenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
		/**
		 * 如果不设置，当item中含有button之类的控件的时候，item相应不了点击事件。
		 */
		setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		init(leftId, contentId, rightId);
	}

	private void init(int left, int content, int right) {
		View leftView = null;
		if (left != 0) {
			leftView = LayoutInflater.from(mContext).inflate(left, this, false);
		}
		addLeftView(leftView);

		View rightView = null;
		if (right != 0) {
			rightView = LayoutInflater.from(mContext).inflate(right, this, false);
		}
		addRightView(rightView);

		View contentView;
		if (content != 0) {
			contentView = LayoutInflater.from(mContext).inflate(content, this, false);
		} else {
			throw new NullPointerException("Slide Menu List Content View Can not Null");
		}
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
		rightView.setLayoutParams(params);
		rightView.setId(R.id.slide_id_right_view);
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
		SlideMenuContentLayout realContentView = new SlideMenuContentLayout(mContext);
		realContentView.addView(contentView, params);
		realContentView.setId(R.id.slide_id_content_view);

		addView(realContentView, params);
		mSlideContentView = realContentView;
	}

	private void setViewShow(View view, boolean show) {
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
		int parentHeight = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
		if (mSlideLeftView != null) {
			LayoutParams params = (LayoutParams) mSlideLeftView.getLayoutParams();
			int widthSpec = ViewGroup.getChildMeasureSpec(parentWidthSpec, getPaddingLeft() + getPaddingRight() +
																		   params.leftMargin + params.rightMargin, params.width);
			int heightSpec = ViewGroup.getChildMeasureSpec(parentHeight, getPaddingTop() + getPaddingBottom() +
																		 params.topMargin + params.bottomMargin, params.height);
			mSlideLeftView.measure(widthSpec, heightSpec);
		}
		if (mSlideRightView != null) {
			LayoutParams params = (LayoutParams) mSlideRightView.getLayoutParams();
			int widthSpec = ViewGroup.getChildMeasureSpec(parentWidthSpec, getPaddingLeft() + getPaddingRight() +
																		   params.leftMargin + params.rightMargin, params.width);
			int heightSpec = ViewGroup.getChildMeasureSpec(parentHeight, getPaddingTop() + getPaddingBottom() +
																		 params.topMargin + params.bottomMargin, params.height);
			mSlideRightView.measure(widthSpec, heightSpec);
		}

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mSlideLeftView != null) {
			int top = (b - t - mSlideLeftView.getMeasuredHeight()) / 2;
			if (mSlideLeftAction == SlideMenuAction.SCROLL) {
				/**
				 * 显示在content view的左边
				 */
				mSlideLeftView.layout(mSlideContentView.getLeft() - mSlideLeftView.getMeasuredWidth(), top,
									  mSlideContentView.getLeft(), top + mSlideLeftView.getMeasuredHeight());
			} else {
				mSlideLeftView.layout(mSlideContentView.getLeft(), top,
									  mSlideContentView.getLeft() + mSlideLeftView.getMeasuredWidth(),
									  top + mSlideLeftView.getMeasuredHeight());
			}
		}

		if (mSlideRightView != null) {
			int top = (b - t - mSlideRightView.getMeasuredHeight()) / 2;
			if (mSlideRightAction == SlideMenuAction.SCROLL) {
				/**
				 * 显示在content view的左边
				 */
				mSlideRightView.layout(mSlideContentView.getRight(), top,
									   mSlideContentView.getRight() + mSlideRightView.getMeasuredWidth(),
									   top + mSlideLeftView.getMeasuredHeight());
			} else {
				mSlideRightView.layout(mSlideContentView.getRight() - mSlideRightView.getMeasuredWidth(), top,
									  mSlideContentView.getRight(),
									  top + mSlideLeftView.getMeasuredHeight());
			}
		}
	}
}
