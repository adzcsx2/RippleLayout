package com.hoyn.main.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Hoyn on 2016/7/4.
 */
public class RippleWaveLinearLayout extends LinearLayout {

    private float mDownX;
    private float mDownY;
    private float mAlphaFactor;
    private float mDensity;
    private float mRadius;
    private boolean mHover = true;

    private Paint mPaint;
    //内圆半径
    private int circleSize;
    //外圆半径
    private float mMaxRadius;
    private int mAnimatorType = 0;

    private boolean isInScroll = false;

    //wave
    private float mMaxRadiusRate = 2f;   // 如果没有设置mMaxRadius，可mMaxRadius = 最小长度 * mMaxRadiusRate;
    private long mDuration = 1000; // 一个波纹从创建到消失的持续时间
    private int mSpeed = 200;   // 波纹的创建速度，每200ms创建一个
    private Interpolator mInterpolator = new LinearInterpolator();
    private List<Circle> mCircleList = new ArrayList<>();
    private boolean mIsRunning;
    private boolean mMaxRadiusSet;
    private long mLastCreateTime;
    private int mRippleColor;

    private Runnable mCreateCircle = new Runnable() {
        @Override
        public void run() {
            if (mIsRunning) {
                newCircle();
                postDelayed(mCreateCircle, mSpeed);
            }
        }
    };

    private int dp(int dp) {
        return (int) (dp * mDensity + 0.5f);
    }

    public RippleWaveLinearLayout(Context context) {
        this(context, null);
    }

    public RippleWaveLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleWaveLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.hoyn.circlerippleview.R.styleable.RippleView);
        mRippleColor = a.getColor(com.hoyn.circlerippleview.R.styleable.RippleView_rippleColor,
                mRippleColor);
        mPaint.setColor(mRippleColor);
        mAlphaFactor = a.getFloat(com.hoyn.circlerippleview.R.styleable.RippleView_alphaFactor,
                mAlphaFactor);
        mAnimatorType = a.getInt(com.hoyn.circlerippleview.R.styleable.RippleView_rippleType, mAnimatorType);
        circleSize = a.getInt(com.hoyn.circlerippleview.R.styleable.RippleView_radius, circleSize);
        mMaxRadius = a.getInt(com.hoyn.circlerippleview.R.styleable.RippleView_maxRadius, 0);
        a.recycle();
    }

    public void init() {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAlpha(100);
        setRippleColor(Color.BLACK, 0.2f);

    }

    public void setRippleColor(int rippleColor, float alphaFactor) {
        mRippleColor = rippleColor;
        mAlphaFactor = alphaFactor;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!mMaxRadiusSet) {
            mMaxRadius = Math.min(w, h) * mMaxRadiusRate / 2.0f;
        }

    }

    private boolean mAnimationIsCancel;
    private Rect mRect;


    private boolean isRootView(ViewParent v) {
        View rootView = getRootView();
        return v == rootView;
    }

    private boolean isInScrollView(ViewParent v) {
        ViewParent parentView = v.getParent();
        if (parentView instanceof AbsListView || parentView instanceof ScrollView) {
            return true;
        } else {
            if (!isRootView(parentView)) {
                isInScrollView(parentView.getParent());
            }
            return false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //init isInScroll on View layout complete
        isInScroll = isInScrollView(getParent());
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean superEvent = super.onTouchEvent(event);
        mDownX = event.getX();
        mDownY = event.getY();
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && this.isEnabled() && mHover) {
            start();
            //如果是在listView，由于listView的点击事件会消耗事件，为了避免动画一直执行，直接stop
            //在listView里，必须返回false,否则不能执行onitemclick等事件
            if (isInScroll) {
                stop();
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                && this.isEnabled() && mHover) {
            //wave
            mDownX = event.getX();
            mDownY = event.getY();
            return false;
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                && !mAnimationIsCancel && this.isEnabled()) {
            stop();
        }
        return superEvent;
    }


    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            return;
        }
        drawWave(canvas);
    }


    // View宽，高
    public int[] getLocation(View v) {
        int[] loc = new int[4];
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        loc[0] = location[0];
        loc[1] = location[1];
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(w, h);

        loc[2] = v.getMeasuredWidth();
        loc[3] = v.getMeasuredHeight();

        //base = computeWH();
        return loc;
    }

    /**
     * 开始
     */
    public void start() {
        if (!mIsRunning) {
            mIsRunning = true;
            mCreateCircle.run();
        }
    }

    /**
     * 停止
     */
    public void stop() {
        mIsRunning = false;
    }

    private void drawWave(Canvas canvas) {
        Iterator<Circle> iterator = mCircleList.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();
            if (System.currentTimeMillis() - circle.mCreateTime < mDuration) {
                mPaint.setAlpha(circle.getAlpha());
                canvas.drawCircle(mDownX, mDownY, circle.getCurrentRadius(), mPaint);
            } else {
                iterator.remove();
            }
        }

        if (mCircleList.size() > 0) {
            postInvalidateDelayed(10);
        }
    }

    public void setInitialRadius(float radius) {
        mRadius = radius;
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    public void setMaxRadius(float maxRadius) {
        this.mMaxRadius = maxRadius;
        mMaxRadiusSet = true;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
    }

    private void newCircle() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastCreateTime < mSpeed) {
            return;
        }
        Circle circle = new Circle();
        mCircleList.add(circle);
        invalidate();
        mLastCreateTime = currentTime;
    }

    private class Circle {
        private long mCreateTime;

        public Circle() {
            this.mCreateTime = System.currentTimeMillis();
        }

        public int getAlpha() {
            float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / mDuration;
            return (int) ((1.0f - mInterpolator.getInterpolation(percent)) * 255);
        }

        public float getCurrentRadius() {
            float percent = (System.currentTimeMillis() - mCreateTime) * 1.0f / mDuration;
            return mRadius + mInterpolator.getInterpolation(percent) * (mMaxRadius - mRadius);
        }
    }

}
