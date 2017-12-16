package com.polestar.domultiple.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.polestar.domultiple.R;
import com.polestar.domultiple.widget.dragdrop.DragController;
import com.polestar.domultiple.widget.dragdrop.DragSource;

/**
 * Created by PolestarApp on 2017/7/16.
 */

public class GridAppCell extends LinearLayout {

    public GridAppCell(Context context) {
        super(context);
        init(context);
    }

    public GridAppCell (Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GridAppCell (Context context, AttributeSet attrs, int style) {
        super (context, attrs, style);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.grid_app_item, this, true);
    }
}