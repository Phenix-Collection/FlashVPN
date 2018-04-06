package com.polestar.superclone.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Created by yxx on 2016/7/26.
 */
public class AnimationLayout extends FrameLayout {

    private static final int TRANSLATE_DURATION_MILLIS = 300;
    public static final int MODE_BOTTOM = 1;
    public static final int MODE_TOP = 2;
    private boolean mVisible;
    private int curMode = MODE_BOTTOM;
    private int duration = TRANSLATE_DURATION_MILLIS;
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public AnimationLayout(Context context) {
        super(context);
        mVisible = true;
    }

    public AnimationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mVisible = true;
    }

    public void setMode(int mode){
        this.curMode = mode;
    }

    public boolean isVisible() {
        return mVisible;
    }

    public void show() {
        show(true);
    }

    public void hide() {
        hide(true);
    }

    public void show(boolean animate) {
        toggle(true, animate, false);
    }

    public void hide(boolean animate) {
        toggle(false, animate, false);
    }

    private void toggle(final boolean visible, final boolean animate, boolean force) {
        if (mVisible != visible || force) {
            mVisible = visible;
            int height = getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY;
            if(curMode == MODE_TOP){
                translationY = - (visible ? 0 : height + getMarginTop());
            }else{
                translationY = visible ? 0 : height + getMarginBottom();
            }
            if (animate) {
                ViewPropertyAnimator.animate(this).setInterpolator(mInterpolator)
                        .setDuration(duration)
                        .translationY(translationY);
            } else {
                ViewHelper.setTranslationY(this, translationY);
            }
        }
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }
    private int getMarginTop() {
        int marginTop = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginTop = ((MarginLayoutParams) layoutParams).topMargin;
        }
        return marginTop;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
