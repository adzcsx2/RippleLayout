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
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.sanniuben.circlerippleview.R;

@SuppressLint("ClickableViewAccessibility")
public class CycleRippleView extends Button {

    private static final String TAG = "RippleView";

    private static final int TYPE_DOT = 0;
    private static final int TYPE_RECYCLE = 1;
    private static final int TYPE_WAVE = 2;



    private float mDownX;
    private float mDownY;
    private float mAlphaFactor;
    private float mDensity;
    private float mRadius;
    private float mMaxRadius;

    private int mRippleColor;

    private boolean mHover = true;

    private RadialGradient mRadialGradient;
    private Paint mPaint;
    private ObjectAnimator mRadiusAnimator;
    private boolean isAnimating = false;
    private boolean isAnimating_up = false;
    private boolean isOnActionDown = false;

    private  int circleSize = 50;
    private int mAnimatorType = 0;

    private int dp(int dp) {
        return (int) (dp * mDensity + 0.5f);
    }

    public CycleRippleView(Context context) {
        this(context, null);
    }

    public CycleRippleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CycleRippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RippleView);
        mRippleColor = a.getColor(R.styleable.RippleView_rippleColor,
                mRippleColor);
        mAlphaFactor = a.getFloat(R.styleable.RippleView_alphaFactor,
                mAlphaFactor);
        mHover = a.getBoolean(R.styleable.RippleView_hover, mHover);
        mAnimatorType = a.getInt(R.styleable.RippleView_rippleType, mAnimatorType);
        circleSize = a.getInt(R.styleable.RippleView_radius, circleSize);
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

    public void setHover(boolean enabled) {
        mHover = enabled;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxRadius = (float) Math.sqrt(w * w + h * h);
    }

    private boolean mAnimationIsCancel;
    private Rect mRect;



    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean superResult = super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && this.isEnabled() && mHover) {
            //can't click before figger up
            if (mAnimatorType == TYPE_WAVE) {
                return true;
            }
            if (isOnActionDown) {
                return false;
            }
            isOnActionDown = true;
            mRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
            mAnimationIsCancel = false;
            mDownX = event.getX();
            mDownY = event.getY();
            if(mAnimatorType==TYPE_RECYCLE){
                animationStart(0, dp(circleSize), 400);
            }else if(mAnimatorType==TYPE_DOT){
                animationStartByUp(0, dp(circleSize), 150);
            }
            if (!superResult) {
                return true;
            }

        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                && this.isEnabled() && mHover) {
            if (mAnimatorType == TYPE_RECYCLE) {

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
                if (!superResult) {
                    return true;
                }
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                && !mAnimationIsCancel && this.isEnabled()) {
            if (mAnimatorType == TYPE_RECYCLE) {
                Log.e("aa", "bb");
                mDownX = event.getX();
                mDownY = event.getY();

                final float tempRadius = (float) Math.sqrt(mDownX * mDownX + mDownY
                        * mDownY);
                float targetRadius = Math.max(tempRadius, mMaxRadius);

                if (isAnimating) {
                    mRadiusAnimator.cancel();
                }
                animationStartByUp(0, targetRadius, 500);
                if (!superResult) {
                    return true;
                }
            }
        }
        return superResult;
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
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

    private Path mPath = new Path();

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            return;
        }
        canvas.save(Canvas.CLIP_SAVE_FLAG);
        mPath.reset();
        mPath.addCircle(mDownX, mDownY, mRadius, Path.Direction.CW);
        canvas.clipPath(mPath);
        canvas.restore();
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
            isAnimating_up = true;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            isAnimating_up = false;
            isOnActionDown = false;
        }

        @Override
        public void onAnimationCancel(Animator animator) {
            isAnimating_up = false;
        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

}
