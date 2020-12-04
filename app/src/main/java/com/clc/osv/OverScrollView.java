package com.clc.osv;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ScrollView;

public class OverScrollView extends ScrollView {
    // 阻尼系数
    private static final int DAMPING_NUM = 2;
    // 回弹动画时间
    private static final int ANIM_DURATION = 200;
    private static final String TAG = "OverScrollView";
    // 子View
    private View mInnerView;
    // 上次MotionEvent的y坐标
    private float mLastY;
    // 一个矩形对象，用于记录子View的位置
    private Rect mRect;

    public OverScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        init();
        super.onFinishInflate();
    }

    private void init() {
        Log.d(TAG, "initView: ");
        // 去除原本ScrollView的边界反馈
        setOverScrollMode(OVER_SCROLL_NEVER);
        mRect = new Rect();
        if (getChildAt(0) != null) {
            mInnerView = getChildAt(0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                // 松手恢复
                if (!mRect.isEmpty()) {
                    rebound();
                    mRect.setEmpty();
                    mLastY = 0;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float currentY = event.getY();
                int distanceY = (int) (mLastY - currentY);
                if ((isToTop() && distanceY < 0) || (isToBottom() && distanceY > 0)) {
                    Log.d(TAG, "onTouchEvent: move" + getScrollY());
                    if (mRect.isEmpty()) {
                        // 保存正常的布局位置
                        mRect.set(mInnerView.getLeft(), mInnerView.getTop(), mInnerView.getRight(), mInnerView.getBottom());
                    }
                    // 移动布局， 使distance / 2 防止平移过快
                    mInnerView.layout(mInnerView.getLeft(), mInnerView.getTop() - distanceY / DAMPING_NUM,
                            mInnerView.getRight(), mInnerView.getBottom() - distanceY / DAMPING_NUM);
                }
                mLastY = currentY;
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 回弹动画
     */
    private void rebound() {
        TranslateAnimation animation = new TranslateAnimation(0, 0, mInnerView.getTop(), mRect.top);
        animation.setDuration(ANIM_DURATION);
        mInnerView.startAnimation(animation);
        // 补间动画并不会真正修改子view的位置，这里需要设置位置，使得子view回到正常的位置
        mInnerView.layout(mRect.left, mRect.top, mRect.right, mRect.bottom);
    }

    private boolean isToBottom() {
        int offset = mInnerView.getMeasuredHeight() - getHeight();
        return getScrollY() == offset;
    }

    private boolean isToTop() {
        return getScrollY() == 0;
    }
}
