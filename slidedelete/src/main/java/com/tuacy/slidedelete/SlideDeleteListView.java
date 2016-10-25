package com.tuacy.slidedelete;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * 滑动删除的ListView
 */
public class SlideDeleteListView extends ListView {

	private static final int DEFAULT_ANIMATION_TIME = 150;

	/**
	 * 滑动的最小距离
	 */
	private   int                    mSlop;
	/**
	 * 滑动的最小速度
	 */
	private   int                    mMinFlingVelocity;
	/**
	 * 滑动的最大速度
	 */
	private   int                    mMaxFlingVelocity;
	/**
	 * 手指按下时的X
	 */
	private   float                  mDownX;
	/**
	 * 手指按下时的Y
	 */
	private   float                  mDownY;
	/**
	 * 手指按下的item position
	 */
	private   int                    mDownPosition;
	/**
	 * 按下的item对应的View
	 */
	private   View                   mDownView;
	/**
	 * item的宽度
	 */
	private   int                    mViewWidth;
	/**
	 * 滑动速度检测类
	 */
	private   VelocityTracker        mVelocityTracker;
	/**
	 * 用来标记用户是否正在滑动中
	 */
	private   boolean                mSliding;
	/**
	 * 执行动画的时间
	 */
	protected long                   mAnimationTime;
	/**
	 * item 消失的监听
	 */
	private   OnDismissListener      mDismissListener;
	/**
	 * ListView的适配器
	 */
	private   SlideDeleteBaseAdapter mAdapter;

	public SlideDeleteListView(Context context) {
		this(context, null);
	}

	public SlideDeleteListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideDeleteListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);

	}

	private void init(AttributeSet attrs) {
		if (attrs != null) {
			TypedArray styled = getContext().obtainStyledAttributes(attrs, R.styleable.SlideDeleteListView);
			mAnimationTime = styled.getInteger(R.styleable.SlideDeleteListView_delete_animation_time, DEFAULT_ANIMATION_TIME);
			styled.recycle();
		}
		ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mSlop = configuration.getScaledTouchSlop();
		mMinFlingVelocity = configuration.getScaledMinimumFlingVelocity(); //获取滑动的最小速度
		mMaxFlingVelocity = configuration.getScaledMaximumFlingVelocity(); //获取滑动的最大速度
	}

	/**
	 * 设置动画时间
	 *
	 * @param mAnimationTime
	 */
	public void setAnimationTime(long mAnimationTime) {
		this.mAnimationTime = mAnimationTime;
	}

	/**
	 * 设置监听的回调
	 *
	 * @param listener
	 */
	public void setOnDismissListener(OnDismissListener listener) {
		mDismissListener = listener;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (!(adapter instanceof SlideDeleteBaseAdapter)) {
			throw new IllegalArgumentException("Please set Adapter extends SlideDeleteBaseAdapter!!!!!!");
		}
		mAdapter = (SlideDeleteBaseAdapter) adapter;
		super.setAdapter(adapter);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				handleActionDown(ev);
				break;
			case MotionEvent.ACTION_MOVE:
				return handleActionMove(ev);
			case MotionEvent.ACTION_UP:
				handleActionUp(ev);
				break;
		}
		return super.onTouchEvent(ev);
	}

	private void handleActionDown(MotionEvent ev) {
		/**
		 * 获取按下的时候的位置
		 */
		mDownX = ev.getX();
		mDownY = ev.getY();
		//获取item 位置
		mDownPosition = pointToPosition((int) mDownX, (int) mDownY);
		/**
		 * 如果当前位置无效
		 */
		if (mDownPosition == AdapterView.INVALID_POSITION) {
			return;
		}
		/**
		 * 得到当前按下位置对于的item
		 */
		mDownView = getChildAt(mDownPosition - getFirstVisiblePosition());
		/**
		 * 获取按下的item 对于View的宽度
		 */
		if (mDownView != null) {
			mViewWidth = mDownView.getWidth();
		}
		/**
		 * 加入速度检测
		 */
		mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(ev);
	}

	private boolean handleActionMove(MotionEvent ev) {
		if (mVelocityTracker == null || mDownView == null) {
			return super.onTouchEvent(ev);
		}
		float deltaX = ev.getX() - mDownX;
		float deltaY = ev.getY() - mDownY;
		// X方向滑动的距离大于mSlop并且Y方向滑动的距离小于mSlop，表示X方向有滑动
		if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < mSlop) {
			mSliding = true;
			//当手指滑动item,取消item的点击事件，不然我们滑动Item也伴随着item点击事件的发生
			MotionEvent cancelEvent = MotionEvent.obtain(ev);
			cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
			onTouchEvent(cancelEvent);
		}

		if (mSliding) {
			// 跟谁手指移动item
			ViewHelper.setTranslationX(mDownView, deltaX);
			// 透明度渐变
			ViewHelper.setAlpha(mDownView, Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
			// 手指滑动的时候,返回true，表示SwipeDismissListView自己处理onTouchEvent,其他的就交给父类来处理
			return true;
		}

		return super.onTouchEvent(ev);
	}

	private void handleActionUp(MotionEvent ev) {
		if (mVelocityTracker == null || mDownView == null || !mSliding) {
			return;
		}
		float deltaX = ev.getX() - mDownX;
		//通过滑动的距离计算出X,Y方向的速度
		mVelocityTracker.computeCurrentVelocity(1000);
		float velocityX = Math.abs(mVelocityTracker.getXVelocity());
		float velocityY = Math.abs(mVelocityTracker.getYVelocity());

		boolean dismiss = false; //item是否要滑出屏幕
		boolean right = false;//是否往右边删除
		//当拖动item的距离大于item的一半，item滑出屏幕
		if (Math.abs(deltaX) > mViewWidth / 2) {
			dismiss = true;
			right = deltaX > 0;
			//手指在屏幕滑动的速度在某个范围内，也使得item滑出屏幕
		} else if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity && velocityY < velocityX) {
			dismiss = true;
			right = mVelocityTracker.getXVelocity() > 0;
		}
		if (dismiss) {
			ViewPropertyAnimator.animate(mDownView).translationX(right ? mViewWidth : -mViewWidth)//X轴方向的移动距离
								.alpha(0).setDuration(mAnimationTime).setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					//Item滑出界面之后执行删除
					performDismiss(mDownView, mDownPosition);
				}
			});
		} else {
			//将item滑动至开始位置
			ViewPropertyAnimator.animate(mDownView).translationX(0).alpha(1).setDuration(mAnimationTime).setListener(null);
		}

		//移除速度检测
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}

		mSliding = false;
	}

	/**
	 * 在此方法中执行item删除之后，其他的item向上或者向下滚动的动画，并且将position回调到方法onDismiss()中
	 *
	 * @param dismissView
	 * @param dismissPosition
	 */
	private void performDismiss(final View dismissView, final int dismissPosition) {
		final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();//获取item的布局参数
		final int originalHeight = dismissView.getHeight();//item的高度

		/**
		 * item View的高度直接慢慢的变成0
		 */
		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0).setDuration(mAnimationTime);
		animator.start();

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mAdapter.dismiss(dismissPosition);
				//这段代码很重要，因为我们并没有将item从ListView中移除，而是将item的高度设置为0
				//所以我们在动画执行完毕之后将item设置回来, view复用了
				ViewHelper.setAlpha(dismissView, 1f);
				ViewHelper.setTranslationX(dismissView, 0);
				ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
				lp.height = originalHeight;
				dismissView.setLayoutParams(lp);
				/**
				 * 回调
				 */
				if (mDismissListener != null) {
					mDismissListener.onDismiss(dismissPosition);
				}

			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				//这段代码的效果是ListView删除某item之后，其他的item向上滑动的效果
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				dismissView.setLayoutParams(lp);
			}
		});

	}

	public interface OnDismissListener {

		void onDismiss(int dismissPosition);
	}
}
