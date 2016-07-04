package com.hoyn.main.widget;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.hoyn.circlerippleview.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by Hoyn on 2016/7/4.
 */
public class RippleRecycleLinearLayout extends LinearLayout {
    private static final int TYPE_DOT = 0;
//    private static final int TYPE_RECYCLE = 1;
//    private static final int TYPE_WAVE = 2;

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

    private int circleSize = 50;
    private int mAnimatorType = 0;
    private boolean isInScroll = false;

    private int dp(int dp) {
        return (int) (dp * mDensity + 0.5f);
    }

    public RippleRecycleLinearLayout(Context context) {
        super(context);
    }

    public RippleRecycleLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.hoyn.circlerippleview.R.styleable.RippleView);
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

    public RippleRecycleLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
    public boolean onTouchEvent(final MotionEvent event) {
        boolean superEvent = super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN
                && this.isEnabled() && mHover) {
            //can't click before figger up
            if (isOnActionDown) {
                return !superEvent;
            }
            isOnActionDown = true;
            mRect = new Rect(getLeft(), getTop(), getRight(), getBottom());
            mAnimationIsCancel = false;
            mDownX = event.getX();
            mDownY = event.getY();
            //if in ListView,Gridview or ScrollView
            if (isInScroll) {
                final float tempRadius = (float) Math.sqrt(mDownX * mDownX + mDownY
                        * mDownY);
                float targetRadius = Math.max(tempRadius, mMaxRadius);
                animationStartByUp(0, targetRadius, 500);
            } else {
                animationStart(0, dp(circleSize), 400);
            }

        } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE
                && this.isEnabled() && mHover) {

            //can't click before figger up
            mDownX = event.getX();
            mDownY = event.getY();
            invalidate();
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                && !mAnimationIsCancel && this.isEnabled()) {
            if(isInScroll){
                return superEvent;
            }
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
        return superEvent;
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
                    adjustAlpha(mRippleColor, mAlphaFactor), mRippleColor,
                    Shader.TileMode.MIRROR);
            mPaint.setShader(mRadialGradient);
        }
        invalidate();
    }

    private Path mPath = new Path();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        isInScroll = isInScrollView(getParent());
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode()) {
            return;
        }
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
        mRadiusAnimator = ObjectAnimator.ofFloat(this, "radius", 0,
                to);
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
