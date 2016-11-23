package com.polestar.multiaccount.component.adapter;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by yxx on 2016/8/8.
 */
public class CustomTouchHelperCallBack extends ItemTouchHelper.Callback {

    public interface OnRearrangeListener {

        void onRearrange(int oldIndex, int newIndex);
    }


    public interface OnDragListener {

        void onDragStart();

        void onDragEnd();

        boolean onDragOutSide(int dragLocation);

        boolean completeDragOutSide(int dragLocation, int itemPosition);

        boolean onCancleDragOutSide();
    }

    private OnRearrangeListener onRearrangeListener;
    private OnDragListener onDragListener;

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        final int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        if(onRearrangeListener != null){
            onRearrangeListener.onRearrange(viewHolder.getAdapterPosition(),target.getAdapterPosition());
            return true;
        }
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        //do nothing
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (onDragListener != null) {
                onDragListener.onDragStart();
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (onDragListener != null) {
            onDragListener.onDragEnd();
        }
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    public void setOnRearrangeListener(OnRearrangeListener onRearrangeListener) {
        this.onRearrangeListener = onRearrangeListener;
    }

    public void setOnDragListener(OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
    }
}
