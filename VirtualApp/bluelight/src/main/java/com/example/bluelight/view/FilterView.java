package com.example.bluelight.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class FilterView extends View {
    private int dimColor;
    private int filterColor;
    private Paint paint;

    public FilterView(Context arg1) {
        super(arg1);
        this.init();
    }

    public FilterView(Context arg1, @Nullable AttributeSet arg2) {
        super(arg1, arg2);
        this.init();
    }

    public FilterView(Context arg1, @Nullable AttributeSet arg2, int arg3) {
        super(arg1, arg2, arg3);
        this.init();
    }

    @TargetApi(value = 21)
    public FilterView(Context arg1, @Nullable AttributeSet arg2, int arg3, int arg4) {
        super(arg1, arg2, arg3, arg4);
        this.init();
    }

    private void init() {
        this.paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas arg8) {
        super.onDraw(arg8);
        arg8.drawColor(this.filterColor);
        this.paint.setColor(this.dimColor);
        arg8.drawRect(0f, 0f, ((float) this.getWidth()), ((float) this.getHeight()), this.paint);
    }

    public void setDimColor(int arg1) {
        this.dimColor = arg1;
    }

    public void setDimColorAndInvalidate(int arg1) {
        this.setDimColor(arg1);
        this.invalidate();
    }

    public void setFilterColor(int arg1) {
        this.filterColor = arg1;
    }

    public void setFilterColorAndInvalidate(int arg1) {
        this.setFilterColor(arg1);
        this.invalidate();
    }
}

