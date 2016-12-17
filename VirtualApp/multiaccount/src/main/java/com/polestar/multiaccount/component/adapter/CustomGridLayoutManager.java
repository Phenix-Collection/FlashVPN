package com.polestar.multiaccount.component.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.view.View;

import com.polestar.multiaccount.utils.MLogs;

/**
 * Created by yxx on 2016/8/8.
 */
public class CustomGridLayoutManager extends GridLayoutManager{
    public CustomGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public CustomGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
        MLogs.e(hSpec + "----");
        int heightSpec = View.MeasureSpec.makeMeasureSpec(hSpec - 300, View.MeasureSpec.EXACTLY);
        MLogs.e(heightSpec + "*******");
        super.setMeasuredDimension(childrenBounds, wSpec, heightSpec);
    }
}
