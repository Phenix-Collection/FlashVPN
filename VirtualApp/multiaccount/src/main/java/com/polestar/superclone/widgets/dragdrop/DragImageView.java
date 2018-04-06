package com.polestar.superclone.widgets.dragdrop;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by guojia on 2016/11/30.
 */

public class DragImageView extends ImageView implements DragSource {
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
