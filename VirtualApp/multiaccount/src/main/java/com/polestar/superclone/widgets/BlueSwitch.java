package com.polestar.superclone.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.CompoundButton;

import com.polestar.superclone.R;


public class BlueSwitch extends CompoundButton {

    private Drawable mSwitchOn;
    private Drawable mSwitchOff;
    private boolean mChecked;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnClickListener mOnClickListener;


    public BlueSwitch(Context context) {
        super(context);
    }

    public BlueSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
//        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BlueSwitch, 0, 0);
//        try {
//            mSwitchOn = a.getDrawable(R.styleable.BlueSwitch_switchOn);
//            mSwitchOff = a.getDrawable(R.styleable.BlueSwitch_switchOff);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        a.recycle();
        mSwitchOn = getResources().getDrawable(R.mipmap.switch_on);
        mSwitchOff = getResources().getDrawable(R.mipmap.switch_off);
    }

    public BlueSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        mChecked = checked;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mSwitchOn.getIntrinsicWidth(), mSwitchOn.getIntrinsicHeight());
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mSwitchOn.getIntrinsicWidth(), heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, mSwitchOn.getIntrinsicHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int width = getWidth() - paddingLeft - paddingRight;
        int height = getHeight() - paddingTop - paddingBottom;

        if (mChecked) {
            Bitmap newOn = zoomDrawable(mSwitchOn, width, height);
            canvas.drawBitmap(newOn, paddingLeft, paddingTop, null);
        } else {
            Bitmap newOff = zoomDrawable(mSwitchOff, width, height);
            canvas.drawBitmap(newOff, paddingLeft, paddingTop, null);
        }
    }

    private Bitmap zoomDrawable(Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap oldmp = ((BitmapDrawable) drawable).getBitmap();
        Matrix matrix = new Matrix();
        float sx = ((float) w / (float) width);
        float sy = ((float) h / (float) height);
        matrix.setScale(sx, sy);
        Bitmap newmp = Bitmap.createBitmap(oldmp, 0, 0, width, height, matrix, false);
        return newmp;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                setChecked(!mChecked);
                if (mOnCheckedChangeListener != null) {
                    mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
                }
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(this);
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        mOnCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }
}


