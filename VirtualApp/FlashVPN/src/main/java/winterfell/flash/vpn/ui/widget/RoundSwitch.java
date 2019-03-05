package winterfell.flash.vpn.ui.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.CompoundButton;

import winterfell.flash.vpn.R;

public class RoundSwitch extends CompoundButton {
    private boolean mChecked;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnClickListener mOnClickListener;


    public RoundSwitch(Context context) {
        super(context);
        updateImg();
    }

    public RoundSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        updateImg();
//        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BlueSwitch, 0, 0);
//        try {
//            mSwitchOn = a.getDrawable(R.styleable.BlueSwitch_switchOn);
//            mSwitchOff = a.getDrawable(R.styleable.BlueSwitch_switchOff);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        a.recycle();
    }

    public RoundSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        updateImg();
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        mChecked = checked;
        updateImg();
        invalidate();
    }

    private void updateImg() {
        if (mChecked) {
            setButtonDrawable(R.drawable.shape_switch_on);
        } else {
            setButtonDrawable(R.drawable.shape_switch_off);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                setChecked(!mChecked);
                updateImg();
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



