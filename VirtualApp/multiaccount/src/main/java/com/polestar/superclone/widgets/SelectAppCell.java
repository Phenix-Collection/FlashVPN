package com.polestar.superclone.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.polestar.superclone.R;


/**
 * Created by PolestarApp on 2017/7/17.
 */

public class SelectAppCell extends RelativeLayout {

    public SelectAppCell(Context context) {
        super(context);
        init(context);
    }

    public SelectAppCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SelectAppCell(Context context, AttributeSet attrs, int style) {
        super (context, attrs, style);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.select_grid_app_item, this, true);
    }
}
