package com.sanniuben.circlerippleview.view;

/*
 * Copyright (C) 2013 Muthuramakrishnan <siriscac@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.sanniuben.circlerippleview.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressLint("ClickableViewAccessibility")
public class CycleRippleLinearLayout extends LinearLayout {
    private static final String TAG = "CircleRippleView";

    private static final int TYPE_DOT = 0;
    private static final int TYPE_RECYCLE = 1;
    private static final int TYPE_WAVE = 2;

    private float mDownX;
    private float mDownY;
    private float initX;
    private float initY;
    private float mAlphaFactor;
    private float mDensity;
    private float mRadius;
    private float mMaxRadius;
    private boolean mHover = true;

    private RadialGradient mRadialGradient;
    private Paint mPaint;
    private ObjectAnimator mRadiusAnimator;
    private boolean isAnimating = false;
    private boolean isOnActionDown = false;

    private int circleSize = 50;
    private int mAnimatorType = 0;

    private boolean isInScroll = false;

    //wave
    private float mMaxRadiusRate = 2f;   // 如果没有设置mMaxRadius，可mMaxRadius = 最小长度 * mMaxRadiusRate;
    private long mDuration = 1000; // 一个波纹从创建到消失的持续时间
    private int mSpeed = 200;   // 波纹的创建速度，每500ms创建一个
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

    public CycleRippleLinearLayout(Context context) {
        this(context, null);
    }

    public CycleRippleLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CycleRippleLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RippleView);
        mRippleColor = a.getColor(R.styleable.RippleView_rippleColor,
                mRippleColor);
        mPaint.setColor(mRippleColor);
        mAlphaFactor = a.getFloat(R.styleable.RippleView_alphaFactor,
                mAlphaFactor);
        mAnimatorType = a.getInt(R.styleable.RippleView_rippleType, mAnimatorType);
        circleSize = a.getInt(R.styleable.RippleView_radius, circleSize);
        mMaxRadius = a.getInt(R.styleable.RippleView_maxRadius, 0);
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
        if (TYPE_WAVE == mAnimatorType) {
            if (!mMaxRadiusSet) {
                mMaxRadius = Math.min(w, h) * mMaxRadiusRate / 2.0f;
            }
        } else {
            mMaxRadius = (float) Math.sqrt(w * w + h * h);
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
        boolean superResult = super.onTouchEvent(event);
        mDownX = event.getX();
        mDownY = event.getY();
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && this.isEnabled() && mHover) {
            //wave
            if (mAnimatorType == TYPE_WAVE) {
                start();
                //如果是在listView，由于listView的点击事件会消耗事件，为了避免动画一直执行，直接stop
                //在listView里，必须返回false,否则不能执行onitemclick等事件
                if(isInScroll){
                    stop();
                    return false;
                }
                return true;
            }
            //ripple
            //can't click before figger up
            if (isOnActionDown) {
                return true;
            }
            isOnActionDown = true;
            mRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
            mAnimationIsCancel = false;
            if (!isAnimating) {
                if (mAnimatorType == TYPE_RECYCLE) {
                    //Only click if in view which can scroll
                    if (isInScroll) {
                        recycleUp(event);
                    } else {
                        animationStart(0, dp(circleSize), 400);
                    }
                } else if (mAnimatorType == TYPE_DOT) {
                    animationStartByUp(0, dp(circleSize), 150);
                }
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                && this.isEnabled() && mHover) {
            //wave
            if (mAnimatorType == TYPE_WAVE) {
                mDownX = event.getX();
                mDownY = event.getY();
            }
            //ripple
            if (mAnimatorType == TYPE_RECYCLE) {
                if (!isInScroll) {
                    //can't click before figger up
                    mDownX = event.getX();
                    mDownY = event.getY();
                    // Cancel the ripple animation when moved outside
                    if (mAnimationIsCancel = !mRect.contains(getLeft() + (int) event.getX(), getTop() + (int) event.getY())) {
                        final float tempRadius = (float) Math.sqrt(mDownX * mDownX + mDownY
                                * mDownY);
                        float targetRadius = Math.max(tempRadius, mMaxRadius);
                        animationStartByUp(0, targetRadius, 500);
                    } else {
                        setRadius(dp(circleSize));
                    }
                }
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                && !mAnimationIsCancel && this.isEnabled()) {
            if (mAnimatorType == TYPE_WAVE) {
                stop();
            } else if (mAnimatorType == TYPE_RECYCLE) {
                recycleUp(event);
            }
        }
        return false;
    }

    private void recycleUp(MotionEvent event) {
        mDownX = event.getX();
        mDownY = event.getY();

        final float tempRadius = (float) Math.sqrt(mDownX * mDownX + mDownY
                * mDownY);
        float targetRadius = Math.max(tempRadius, mMaxRadius);

        if (isAnimating) {
            mRadiusAnimator.cancel();
        }
        animationStartByUp(0, targetRadius, 500);
    }


    public void setRadius(final float radius) {
        mRadius = radius;
        if (mRadius > 0) {
            mRadialGradient = new RadialGradient(mDownX, mDownY, mRadius,
                    Color.WHITE, mRippleColor,
                    Shader.TileMode.MIRROR);
//            mRadialGradient = new RadialGradient(mDownX, mDownY, mRadius,
//                    adjustAlpha(mRippleColor, mAlphaFactor), mRippleColor,
//                    Shader.TileMode.MIRROR);
            mPaint.setShader(mRadialGradient);
        }
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            return;
        }
//        canvas.save(Canvas.CLIP_SAVE_FLAG);
//        mPath.reset();
//        mPath.addCircle(mDownX, mDownY, mRadius, Path.Direction.CW);
//        canvas.clipPath(mPath);
//        canvas.restore();
        if (mAnimatorType == TYPE_WAVE)
            drawWave(canvas);
        else
            canvas.drawCircle(mDownX, mDownY, mRadius, mPaint);
    }

    //down和move
    private void animationStart(float from, float to, int duration) {
        mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", from,
                to);
        mRadiusAnimator.setDuration(duration);
        //首尾慢，中间快
        mRadiusAnimator
                .setInterpolator(new AccelerateDecelerateInterpolator());
        mRadiusAnimator.addListener(touchListener);
        mRadiusAnimator.start();
    }

    //up
    private void animationStartByUp(final float from, final float to, final int duration) {
        if (mAnimatorType == TYPE_DOT) {
            mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", from,
                    to);
        } else if (mAnimatorType == TYPE_RECYCLE) {
            mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", 0,
                    to);
        }
        mRadiusAnimator.setDuration(duration);
        mRadiusAnimator.setRepeatMode(Animation.REVERSE);
        mRadiusAnimator.setRepeatCount(1);
        mRadiusAnimator
                .setInterpolator(new AccelerateDecelerateInterpolator());
        mRadiusAnimator.addListener(touchListener_up);
        mRadiusAnimator.start();
    }


    Animator.AnimatorListener touchListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            isAnimating = true;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            isAnimating = false;
        }

        @Override
        public void onAnimationCancel(Animator animator) {
        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };
    Animator.AnimatorListener touchListener_up = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            isOnActionDown = false;
        }

        @Override
        public void onAnimationCancel(Animator animator) {
        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    //wave

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
