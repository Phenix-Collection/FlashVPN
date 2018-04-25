package mochat.multiple.parallel.whatsclone.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import mochat.multiple.parallel.whatsclone.R;

/**
 * Created by guojia on 2016/11/30.
 */

public class GridAppCell extends LinearLayout{

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
        inflater.inflate(R.layout.item_app, this, true);
    }
}