package com.polestar.domultiple.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.widget.dragdrop.DragSource;
import com.polestar.domultiple.widget.dragdrop.DragView;
import com.polestar.domultiple.widget.dragdrop.DropTarget;

/**
 * Created by PolestarApp on 2017/6/1.
 */

public class DropableLinearLayout extends LinearLayout implements DropTarget {
    private static final String TAG = "DropableLinearLayout";
    private IDragListener mListener = null;
    public DropableLinearLayout(Context context) {
        super(context);
    }

    public DropableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DropableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private boolean selected = false;

    public boolean isSelected() {
        return selected;
    }

    public void clearState() {
        selected = false;
    }

    public void setOnEnterListener(IDragListener listener) {
        mListener = listener;
    }
    public interface IDragListener {
        void onEnter();
        void onExit();
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        MLogs.d(TAG, "onDrop");
    }

    @Override
    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        MLogs.d(TAG, "onDragEnter");
        selected = true;
        if (mListener != null) {
            mListener.onEnter();
        }
    }

    @Override
    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        MLogs.d(TAG, "onDragOver");
    }

    @Override
    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        MLogs.d(TAG, "onDragExit");
        selected = false;
        if (mListener != null) {
            mListener.onExit();
        }
    }

    @Override
    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo) {
        MLogs.d(TAG, "acceptDrop");
        return true;
    }

    @Override
    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset, DragView dragView, Object dragInfo, Rect recycle) {
        return null;
    }
}