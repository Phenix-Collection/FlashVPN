package com.polestar.superclone.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.polestar.superclone.R;


/**
 * Created by Huan on 2019/1/28.
 */

public class ProductGridCell extends RelativeLayout {

    public ProductGridCell(Context context) {
        super(context);
        init(context);
    }

    public ProductGridCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ProductGridCell(Context context, AttributeSet attrs, int style) {
        super (context, attrs, style);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.product_grid_item, this, true);
    }
}
