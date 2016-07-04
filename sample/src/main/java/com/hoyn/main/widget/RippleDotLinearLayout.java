package com.hoyn.main.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.hoyn.circlerippleview.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by Hoyn on 2016/7/4.
 */
public class RippleDotLinearLayout extends LinearLayout {

    private float mDownX;
    private float mDownY;
    private float mAlphaFactor;
    private float mDensity;
    private float mRadius;
    private int mRippleColor;

    private boolean mHover = true;

    private RadialGradient mRadialGradient;
    private Paint mPaint;
    private ObjectAnimator mRadiusAnimator;

    //半径大小
    private int circleSize = 50;
    private int mAnimatorType = 0;

    private int dp(int dp) {
        return (int) (dp * mDensity + 0.5f);
    }

    public RippleDotLinearLayout(Context context) {
        super(context);
    }

    public RippleDotLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    public RippleDotLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
    }


    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean superEvent = super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && this.isEnabled() && mHover) {
            mDownX = event.getX();
            mDownY = event.getY();
            animationStartByUp(0, dp(circleSize), 150);
        }
        return superEvent;
    }


    public void setRadius(final float radius) {
        mRadius = radius;
        if (mRadius > 0) {
            mRadialGradient = new RadialGradient(mDownX, mDownY, mRadius,
                    Color.WHITE, mRippleColor,
                    Shader.TileMode.MIRROR);
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
        canvas.drawCircle(mDownX, mDownY, mRadius, mPaint);
    }


    //up
    private void animationStartByUp(final float from, final float to, final int duration) {
        mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", from,
                to);
        mRadiusAnimator.setDuration(duration);
        mRadiusAnimator.setRepeatMode(Animation.REVERSE);
        mRadiusAnimator.setRepeatCount(1);
        mRadiusAnimator
                .setInterpolator(new AccelerateDecelerateInterpolator());
        mRadiusAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mRadiusAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mRadiusAnimator.start();
    }

}
