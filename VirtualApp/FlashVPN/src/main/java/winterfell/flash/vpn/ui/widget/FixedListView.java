package winterfell.flash.vpn.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by PolestarApp on 2016/7/14.
 */
public class FixedListView extends ListView {
    public FixedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedListView(Context context) {
        super(context);
    }

    public FixedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //显示ListView完整长度
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

}
