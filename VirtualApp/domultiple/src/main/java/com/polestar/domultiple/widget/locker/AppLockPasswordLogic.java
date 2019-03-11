package com.polestar.domultiple.widget.locker;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;

import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;
import com.polestar.domultiple.utils.DisplayUtils;
import com.polestar.domultiple.utils.PreferencesUtils;

import java.util.List;

/**
 * Created by PolestarApp on 2017/1/3.
 */

public class AppLockPasswordLogic {
    private static final int TITLE_COLOR_WHITE = Color.parseColor("#FFFFFFFF");
    private static final int MSG_CLEAR_PATTERN = 0x01;
    private static final long CLEAR_PATTERN_INTERVAL = 600;
    private LockPatternView mLockPatternView = null;
    private View mView = null;
    private Handler mHandler = null;
    private EventListener mEventListener = null;

    public interface EventListener {

        public void onCorrectPassword();

        public void onIncorrectPassword();

        public void onCancel();
    }

    public AppLockPasswordLogic(View view, EventListener listener) {
        mView = view;
        mEventListener = listener;
    }


    public void onFinishInflate() {
        initLockPattern();
    }

    private void initLockPattern() {
        if (mView == null) return;
        mLockPatternView = (LockPatternView) mView.findViewById(R.id.applock_pattern_layout);
        mLockPatternView.setOnPatternListener(mPatternListener);
        mLockPatternView.setInArrowMode(false);
        mLockPatternView.setInCircleMode(false);
//        mLockPatternView.setBitmapBtnDefault(R.drawable.applock_lockpattern_indicator_code_lock_backgorund_holo);
//        mLockPatternView.setBitmapBtnTouched(R.drawable.applock_lockpattern_indicator_code_lock_point_area_green_holo);
//        mLockPatternView.setGreenPathPaintColor(TITLE_COLOR_WHITE);

        int screenWidth = DisplayUtils.getScreenWidth(PolestarApp.getApp());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLockPatternView.getLayoutParams();
        params.width = (screenWidth * 3) / 4;
        params.height = (screenWidth * 3) / 4;
        mLockPatternView.setLayoutParams(params);

        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_CLEAR_PATTERN:
                        if (mLockPatternView != null) {
                            mLockPatternView.clearPattern();
                        }
                        break;
                }
            }
        };
    }

    public void onBeforeHide() {
        mHandler.removeMessages(MSG_CLEAR_PATTERN);
    }


    public void onShow() {
        mLockPatternView.clearPattern();
        mLockPatternView.setVisibility(View.VISIBLE);
        // set lock pattern mode
        if (mLockPatternView != null) {
            mLockPatternView
                    .setInStealthMode(PreferencesUtils.getAppLockInVisiablePatternPath(PolestarApp.getApp()));
        }

    }

    private final LockPatternView.OnPatternListener mPatternListener = new LockPatternView.OnPatternListener() {

        @Override
        public void onPatternStart() {
            mHandler.removeMessages(MSG_CLEAR_PATTERN);
        }

        @Override
        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (LockPatternUtils.isPatternMatched(pattern)) {
                PreferencesUtils.setLockScreen(PolestarApp.getApp(),false);
                mEventListener.onCorrectPassword();
            } else {
                mHandler.sendEmptyMessageDelayed(MSG_CLEAR_PATTERN, CLEAR_PATTERN_INTERVAL);
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);

                if ((pattern != null && pattern.size() > 2)) {
                    mEventListener.onIncorrectPassword();
                } else {
                    mEventListener.onCancel();
                }
            }
        }

        @Override
        public void onPatternCleared() {
        }
        @Override
        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

        }
    };
}
