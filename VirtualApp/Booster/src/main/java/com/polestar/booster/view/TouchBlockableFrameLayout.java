package com.polestar.booster.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class TouchBlockableFrameLayout extends FrameLayout {

    private boolean mBlockTouch = false;

    public TouchBlockableFrameLayout(Context context) {
        super(context);
    }

    public TouchBlockableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchBlockableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TouchBlockableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mBlockTouch)
            return true;

        return super.onInterceptTouchEvent(ev);
    }

    public void setBlockTouch(boolean block) {
        mBlockTouch = block;
    }

    public static void blockTouch(View view) {
        if (!(view instanceof TouchBlockableFrameLayout))
            return;

        ((TouchBlockableFrameLayout) view).setBlockTouch(true);
    }
}
