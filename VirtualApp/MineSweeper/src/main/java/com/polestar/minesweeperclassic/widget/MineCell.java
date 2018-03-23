package com.polestar.minesweeperclassic.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.polestar.minesweeperclassic.R;

/**
 * Created by doriscoco on 2017/4/3.
 */

public class MineCell extends Button {
    public int row;
    public int col;
    public int state;
    public boolean isMine;
    public int around = 0;

    public static final int STATE_INIT = 0;
    public static final int STATE_CLICKED = 1;
    public static final int STATE_FLAG = 2;
    public static final int STATE_SHOW = 3;

    public void onShow() {
        switch (state) {
            case STATE_INIT:
                updateState(STATE_SHOW);
                break;
            case STATE_FLAG:
                if (!isMine) {
                    setBackgroundResource(R.drawable.wrflag);
                }
                break;
        }
    }

    public void onInit() {
        updateState(STATE_INIT);
    }

    public void onClick() {
        updateState(STATE_CLICKED);
    }

    public void onFlag() {
        if (state == STATE_INIT ){
            updateState(STATE_FLAG);
        }else {
            updateState(STATE_INIT);
        }
    }

    public void updateState(int state) {
        this.state = state;
        switch (state) {
            case STATE_INIT:
                setBackgroundResource(R.drawable.button);
                break;
            case STATE_CLICKED:
                if (!isMine) {
                    updateAroundNumber();
                } else {
                    setBackgroundResource(R.drawable.rmine);
                }
                setClickable(false);
                break;
            case STATE_FLAG:
                markFlag();
                break;
            case STATE_SHOW:
                if (!isMine) {
                    updateAroundNumber();
                } else {
                    setBackgroundResource(R.drawable.gmine);
                }
                break;
        }

    }

    private void markFlag(){
        setBackgroundResource(R.drawable.flag);
    }

    private void updateAroundNumber(){
        switch (around) {
            case 0:
                setBackgroundResource(R.drawable.empty);
                break;
            case 1:
                setBackgroundResource(R.drawable.n1);
                break;
            case 2:
                setBackgroundResource(R.drawable.n2);
                break;
            case 3:
                setBackgroundResource(R.drawable.n3);
                break;
            case 4:
                setBackgroundResource(R.drawable.n4);
                break;
            case 5:
                setBackgroundResource(R.drawable.n5);
                break;
            case 6:
                setBackgroundResource(R.drawable.n6);
                break;
            case 7:
                setBackgroundResource(R.drawable.n7);
                break;
            case 8:
                setBackgroundResource(R.drawable.n8);
                break;
        }
    }

    public MineCell(Context context) {
        super(context);
    }

    public MineCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MineCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
