package com.polestar.domultiple.widget.locker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.polestar.clone.CustomizeAppData;

/**
 * Created by PolestarApp on 2017/7/23.
 */

public class BlurBackground extends LinearLayout {
    private final static String TAG = "BlurBackground";
    private final static boolean DEBUG = false;
    private boolean mDefaultLinearLayout = false;

    LockerThemeLogic mDefaultThemeLogic;

    public BlurBackground(Context context) {
        this(context, null);
    }

    public BlurBackground(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlurBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!mDefaultLinearLayout){
            mDefaultThemeLogic.draw(canvas);
        }
        super.onDraw(canvas);
    }

    public void init() {
        mDefaultThemeLogic = new LockerThemeLogic(getContext(), this);
        mDefaultThemeLogic.setDimension();
        mDefaultThemeLogic.setUseBigIcon(true, true);
    }

    public void reloadWithTheme(String packageName, int userId) {
        if(TextUtils.isEmpty(packageName) ){
            return;
        }
        CustomizeAppData data = CustomizeAppData.loadFromPref(packageName, userId);
//        mCenterIcon.setImageBitmap(data.getCustomIcon());
        mDefaultThemeLogic.setBackground(packageName, new BitmapDrawable( data.getCustomIcon()));
    }

    public void resetLayout() {
        mDefaultThemeLogic.onHide();
    }

    public void setDefaultLinearLayout(boolean defaultLinearLayout){
        mDefaultLinearLayout = defaultLinearLayout;
        invalidate();
    }

    public void onIncorrectPassword(View view) {
        mDefaultThemeLogic.onIncorrectPassword(view);
    }
}
