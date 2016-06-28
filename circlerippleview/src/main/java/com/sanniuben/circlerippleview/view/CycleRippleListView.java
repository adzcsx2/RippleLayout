package com.sanniuben.circlerippleview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Hoyn on 2016/6/24.
 */
public class CycleRippleListView extends ListView{
    public CycleRippleListView(Context context) {
        super(context);
    }

    public CycleRippleListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CycleRippleListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return false;
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        return false;
//    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }
}
