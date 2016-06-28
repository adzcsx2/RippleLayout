package com.sanniuben.circlerippleview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import com.sanniuben.circlerippleview.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 水波纹特效
 * Created by hackware on 2016/6/17.
 */
public class WaveView extends Button {
    private float mRadius;   // 初始波纹半径
    private float mMaxRadiusRate = 2f;   // 如果没有设置mMaxRadius，可mMaxRadius = 最小长度 * mMaxRadiusRate;
    private float mMaxRadius;   // 最大波纹半径
    private long mDuration = 1000; // 一个波纹从创建到消失的持续时间
    private int mSpeed = 200;   // 波纹的创建速度，每500ms创建一个
    private Interpolator mInterpolator = new LinearInterpolator();

    private List<Circle> mCircleList = new ArrayList<Circle>();
    private boolean mIsRunning;

    private boolean mMaxRadiusSet;

    private Paint mPaint;
    private long mLastCreateTime;

    private int mRippleColor;

    private float down_x,down_y;

    private Runnable mCreateCircle = new Runnable() {
        @Override
        public void run() {
            if (mIsRunning) {
                newCircle();
                postDelayed(mCreateCircle, mSpeed);
            }
        }
    };

    public WaveView(Context context) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setStyle(Paint.Style.FILL);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RippleView);
        mRippleColor = a.getColor(R.styleable.RippleView_rippleColor,
                mRippleColor);
        setColor(mRippleColor);

        setStyle(Paint.Style.FILL);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setStyle(Paint.Style.FILL);
    }


    public void setStyle(Paint.Style style) {
        mPaint.setStyle(style);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (!mMaxRadiusSet) {
            mMaxRadius = Math.min(w, h) * mMaxRadiusRate / 2.0f;
        }
    }

    public void setMaxRadiusRate(float maxRadiusRate) {
        this.mMaxRadiusRate = maxRadiusRate;
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                start();
                down_x = event.getX();
                down_y = event.getY();
            break;
            case MotionEvent.ACTION_MOVE:
                down_x = event.getX();
                down_y = event.getY();
            break;
            case MotionEvent.ACTION_UP:
                stop();
            break;
        }
        return true;
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

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawWave(canvas);
    }

    private void drawWave(Canvas canvas) {
        Iterator<Circle> iterator = mCircleList.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();
            if (System.currentTimeMillis() - circle.mCreateTime < mDuration) {
                mPaint.setAlpha(circle.getAlpha());
                canvas.drawCircle(down_x, down_y, circle.getCurrentRadius(), mPaint);
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

//    public void setInterpolator(Interpolator interpolator) {
//        mInterpolator = interpolator;
//        if (mInterpolator == null) {
//            mInterpolator = new LinearInterpolator();
//        }
//    }
}
