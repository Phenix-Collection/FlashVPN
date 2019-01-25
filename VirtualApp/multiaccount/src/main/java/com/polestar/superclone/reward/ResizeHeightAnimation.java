package com.polestar.superclone.reward;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by guojia on 2019/1/24.
 */

public class ResizeHeightAnimation extends Animation {
    int startHeight;
    final int targetHeight;
    View view;

    public ResizeHeightAnimation(View arg1, int arg2, int arg3) {
        super();
        this.view = arg1;
        this.targetHeight = arg2;
        this.startHeight = arg3;
    }

    protected void applyTransformation(float arg5, Transformation arg6) {
        this.view.getLayoutParams().height = ((int)((((float)this.startHeight)) + (((float)(this.targetHeight - this.startHeight))) * arg5));
        this.view.requestLayout();
    }

    public void initialize(int arg1, int arg2, int arg3, int arg4) {
        super.initialize(arg1, arg2, arg3, arg4);
    }

    public boolean willChangeBounds() {
        return true;
    }
}