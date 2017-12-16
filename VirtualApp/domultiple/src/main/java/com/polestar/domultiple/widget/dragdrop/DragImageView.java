package com.polestar.domultiple.widget.dragdrop;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by PolestarApp on 2016/11/30.
 */

public class DragImageView extends android.support.v7.widget.AppCompatImageView implements DragSource {
    public DragImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DragImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DragImageView(Context context) {
        super(context);
    }

    @Override
    public boolean allowDrag() {
        return true;
    }

    @Override
    public void setDragController(DragController dragger) {

    }

    @Override
    public void onDropCompleted(View target, boolean success) {

    }
}
