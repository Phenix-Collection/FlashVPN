package com.polestar.domultiple.widget.locker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.polestar.domultiple.R;

/**
 * Created by guojia on 2017/7/19.
 */


public class LockIconImageView extends android.support.v7.widget.AppCompatImageView {
    private int mShaderColor = Color.GRAY;

    public LockIconImageView(Context context) {
        this(context, null);
    }

    public LockIconImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockIconImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.LockIconShader, defStyleAttr, 0);
        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.LockIconShader_shader_color_on_click:
                    mShaderColor = a.getColor(attr, mShaderColor);
                    break;
            }
        }

        a.recycle();
    }

    /*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                setColorFilter();
                break;
            case MotionEvent.ACTION_UP:
                removeColorFilter();
                performClick();
                break;
            case MotionEvent.ACTION_CANCEL:
                removeColorFilter();
                break;
        }

        return true;
    }
    */

    public void setColorFilter() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            drawable = getBackground();
        }

        if (drawable != null) {
            drawable.setColorFilter(mShaderColor, PorterDuff.Mode.MULTIPLY);
        }
    }

    public void removeColorFilter() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            drawable = getBackground();
        }

        if (drawable != null) {
            drawable.clearColorFilter();
        }
    }
}

