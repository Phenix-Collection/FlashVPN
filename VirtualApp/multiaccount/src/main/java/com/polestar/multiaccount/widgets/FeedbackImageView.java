package com.polestar.multiaccount.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.polestar.multiaccount.R;

/**
 * Created by guojia on 2017/1/3.
 */

public class FeedbackImageView extends ImageView {
    private int mShaderColor = Color.GRAY;

    public FeedbackImageView(Context context) {
        this(context, null);
    }

    public FeedbackImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeedbackImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.FeedbackShader, defStyleAttr, 0);
        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.FeedbackShader_shader_color_on_click:
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
