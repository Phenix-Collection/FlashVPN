package in.dualspace.cloner.widget.locker;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;


import in.dualspace.cloner.R;
import in.dualspace.cloner.utils.MLogs;
import in.dualspace.cloner.utils.PreferencesUtils;
import in.dualspace.cloner.utils.ResourcesUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by DualApp on 2017/1/1.
 */

public class LockerView extends LinearLayout implements View.OnClickListener {

    private static final int TITLE_COLOR_WHITE = Color.parseColor("#FFFFFFFF");
    private static final int TITLE_COLOR_GRAY = Color.parseColor("#FF4A4A4A");

    private static final int TYPE_PATTERN_MAIN = 0;
    private static final int TYPE_PATTERN_START = 1;
    private static final int TYPE_PATTERN_LESS_MIN = 2;
    private static final int TYPE_PATTERN_WRONG_PATTERN = 3;
    private static final int TYPE_PATTERN_CORRECT_PATTERN = 4;
    private static final int TYPE_PATTERN_ERROR = 5;

    public static final int MIN_PATTERN_NUMVER = 4;
    private static final int MSG_CLEAN_PATTERN = 3;
    private static final int CLEAN_PATTERN_TIME = 1000;


    private View mAppLockPasswordRoot, mBottomHalf;
    private TextView mLockTitle, mSwitchOrResetHint;
    private LockPatternView mLockPatternView = null;

    private String mDefaultTitle = null;
    private STATE mState = STATE.CHECK_PASSWORD;
    private boolean mIsWhiteBackground = true;
    private AtomicBoolean mLockPatternViewInflated = new AtomicBoolean(false);
    private boolean mDidReportPatternTouch = false;
    private String mFirstPassword;
    private OnUnlockListener mOnUnlockListener = null;

    private enum STATE {
        CHECK_PASSWORD, RESET_PASSWORD, CONFIRM_PASSWORD, PASSWORD_SET_DONE;

        STATE next() {
            switch (this) {
                case RESET_PASSWORD:
                    return CONFIRM_PASSWORD;
                case CONFIRM_PASSWORD:
                    return PASSWORD_SET_DONE;
                default:
                    return this;
            }
        }

        STATE reset() {
            switch (this) {
                case CONFIRM_PASSWORD:
                    return RESET_PASSWORD;
                default:
                    return this;
            }
        }
    }

    public LockerView(Context context) {
        this(context, null);
    }

    public LockerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CLEAN_PATTERN:
                    if (mLockPatternView != null) {
                        mLockPatternView.clearPattern();
                        updateLockPatternViewAndHint(TYPE_PATTERN_MAIN);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.view_applock_password, this, true);

        mAppLockPasswordRoot = findViewById(R.id.root_applock_password);
        mLockTitle = (TextView) findViewById(R.id.lock_title);
        mBottomHalf = findViewById(R.id.bottom_half);
        mSwitchOrResetHint = (TextView) findViewById(R.id.lock_switch_method);

        mSwitchOrResetHint.setOnClickListener(this);

        if (mDefaultTitle == null) {
            mDefaultTitle = mState == STATE.CHECK_PASSWORD ? ResourcesUtil.getString(R.string.al_lockpattern_draw_unlock_pattern)
                    : ResourcesUtil.getString(R.string.al_lockpattern_create_unlock_pattern);
            mLockTitle.setText(mDefaultTitle);
        }
        updateUIState();
    }

    public void init() {
        updateTextColor();
        mAppLockPasswordRoot.setBackgroundColor((mState == STATE.RESET_PASSWORD || mIsWhiteBackground)
                ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FF54545D"));
        updatePatternPointColor();
    }

    private void updateUIState() {
        init();
        togglePasswordUI();

        mSwitchOrResetHint.setVisibility(View.GONE);

        switch (mState) {
            case CHECK_PASSWORD:
                mSwitchOrResetHint.setVisibility(GONE);
                mLockPatternView.clearPattern();
                mLockTitle.setText(ResourcesUtil.getString(R.string.al_lockpattern_draw_unlock_pattern));
                break;
            case RESET_PASSWORD:
                updateLockPatternViewAndHint(TYPE_PATTERN_MAIN);
                break;
            case CONFIRM_PASSWORD:
                mSwitchOrResetHint.setVisibility(View.VISIBLE);
                mSwitchOrResetHint.setText(ResourcesUtil.getString(R.string.al_btn_reset));
                mSwitchOrResetHint.setTextColor(ResourcesUtil.getColor(R.color.text_gray_dark));
                break;
            default:
        }
    }

    private void updateLockPatternViewAndHint(int type) {
        updateTextColor();
        switch (type) {
            case TYPE_PATTERN_MAIN: {
                mLockPatternView.enableInput();
                if (mState == STATE.RESET_PASSWORD) {
                    mSwitchOrResetHint.setVisibility(View.GONE);
                    mLockPatternView.clearPattern();
                    mLockTitle.setText(ResourcesUtil.getString(R.string.al_lockpattern_create_unlock_pattern));
                } else if (mState == STATE.CONFIRM_PASSWORD) {
                    mLockTitle.setText(
                            ResourcesUtil.getString(R.string.al_lockpattern_confirm_unlock_pattern));
                    mSwitchOrResetHint.setText(ResourcesUtil.getString(R.string.al_btn_reset));
                    mSwitchOrResetHint.setVisibility(View.VISIBLE);
                } else if (mState == STATE.CHECK_PASSWORD) {
                    mLockTitle.setText(ResourcesUtil.getString(R.string.al_lockpattern_draw_unlock_pattern));
                }
                break;
            }
            case TYPE_PATTERN_START: {
                break;
            }
            case TYPE_PATTERN_LESS_MIN: {
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                mHandler.sendEmptyMessageDelayed(MSG_CLEAN_PATTERN, CLEAN_PATTERN_TIME);

                mLockTitle.setText(ResourcesUtil.getString(R.string.al_lockpattern_number_no_correct));
                break;
            }
            case TYPE_PATTERN_WRONG_PATTERN:
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                mHandler.sendEmptyMessageDelayed(MSG_CLEAN_PATTERN, CLEAN_PATTERN_TIME);

                mLockTitle.setText(ResourcesUtil.getString(R.string.al_lockpattern_try_again));

                mSwitchOrResetHint.setText(ResourcesUtil.getString(R.string.al_btn_reset));
                mSwitchOrResetHint.setVisibility(View.VISIBLE);
                break;
            case TYPE_PATTERN_CORRECT_PATTERN:
                mLockPatternView.disableInput();
                mSwitchOrResetHint.setText(ResourcesUtil.getString(R.string.al_btn_reset));
                mSwitchOrResetHint.setVisibility(View.VISIBLE);
                break;
            case TYPE_PATTERN_ERROR:
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                mHandler.sendEmptyMessageDelayed(MSG_CLEAN_PATTERN, CLEAN_PATTERN_TIME);

                mLockTitle.setText(ResourcesUtil.getString(R.string.al_lockpattern_error));
                break;
        }
    }

    private void togglePasswordUI() {
        mBottomHalf.setVisibility(View.VISIBLE);
        if (!mLockPatternViewInflated.get()) {
            try {
                ViewStub stub = (ViewStub) findViewById(R.id.lockpattern_holder);
                stub.inflate();

                mLockPatternView = (LockPatternView) findViewById(R.id.lockpattern_layout);
                mLockPatternView.setOnPatternListener(mLockPatternViewListener);
            } catch (Exception e) {
                MLogs.e(e);
            }
            mLockPatternViewInflated.set(true);
        }
        mLockPatternView.setVisibility(View.VISIBLE);
        mLockPatternView.setInArrowMode(false);
        mLockPatternView.setInCircleMode(true);
        updatePatternPointColor();
    }

    private LockPatternView.OnPatternListener mLockPatternViewListener
            = new LockPatternView.OnPatternListener() {

        @Override
        public void onPatternStart() {
            if (!mDidReportPatternTouch && mState == STATE.RESET_PASSWORD) {
                mDidReportPatternTouch = true;
            }

            if (mState != STATE.CHECK_PASSWORD) {
                updateLockPatternViewAndHint(TYPE_PATTERN_START);
            }
            mHandler.removeMessages(MSG_CLEAN_PATTERN);
        }

        @Override
        public void onPatternCleared() {
        }

        @Override
        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
        }

        @Override
        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (mState == STATE.CHECK_PASSWORD) {
                if (pattern.size() < MIN_PATTERN_NUMVER) {
                    updateLockPatternViewAndHint(TYPE_PATTERN_ERROR);
                } else {
                    if (isPatternCorrect(pattern)) {
                        PreferencesUtils.setLockScreen(getContext(),false);
                        if (mOnUnlockListener != null){
                            mOnUnlockListener.onCorrectPassword();
                        }
                        unlock();
                    } else {
                        updateLockPatternViewAndHint(TYPE_PATTERN_ERROR);
                    }
                }
            } else {
                switch (mState) {
                    case RESET_PASSWORD:
                        if (pattern.size() < MIN_PATTERN_NUMVER) {
                            updateLockPatternViewAndHint(TYPE_PATTERN_LESS_MIN);
                        } else {
                            mFirstPassword = LockPatternUtils
                                    .patternToPatternPassword(mLockPatternView.getPattern());
                            mState = mState.next();
                            mLockPatternView.clearPattern();
                            updateLockPatternViewAndHint(TYPE_PATTERN_MAIN);
                            updateUIState();
                        }
                        break;
                    case CONFIRM_PASSWORD:
                        String confirmPattern = LockPatternUtils
                                .patternToPatternPassword(mLockPatternView.getPattern());
                        if (confirmPattern.equals(mFirstPassword)) {
                            onNewPasswordSet();
                        } else {
                            updateLockPatternViewAndHint(TYPE_PATTERN_WRONG_PATTERN);
                        }
                }

            }
        }
    };

    private void unlock() {

    }

    public void onBeforeHide() {
        mHandler.removeMessages(MSG_CLEAN_PATTERN);
    }

    public void onAfterShow(){
        mHandler.sendEmptyMessage(MSG_CLEAN_PATTERN);
    }

    private void onNewPasswordSet() {
        mState = mState.next();
        LockPatternUtils.setEncodedPatternPassword(mLockPatternView.getPattern());
        if (mOnUnlockListener != null){
            mOnUnlockListener.onCorrectPassword();
        }
        unlock();
    }

    private boolean isPatternCorrect(List<LockPatternView.Cell> pattern) {
        return LockPatternUtils.isPatternMatched(pattern);
    }

    private void updatePatternPointColor() {
        if (mLockPatternView == null) return;
        if (mState == STATE.RESET_PASSWORD || mIsWhiteBackground) {
//            mLockPatternView.setBitmapBtnDefault(R.drawable.applock_pattern_default_btn_gray_point);
//            mLockPatternView.setBitmapBtnTouched(R.drawable.applock_pattern_touched_btn_black_point);
            mLockPatternView.setGreenPathPaintColor(getResources().getColor(R.color.applock_lockpattern_pattern_path_green_light));
        } else {
//            mLockPatternView.setBitmapBtnDefault(R.drawable.applock_pattern_default_btn_white_point);
//            mLockPatternView.setBitmapBtnTouched(R.drawable.applock_pattern_touched_btn_white_point);
            mLockPatternView.setGreenPathPaintColor(TITLE_COLOR_WHITE);
        }
    }

    private void updateTextColor() {
        if (mState == STATE.RESET_PASSWORD || mIsWhiteBackground) {
            mLockTitle.setTextColor(TITLE_COLOR_GRAY);
        } else {
            mLockTitle.setTextColor(TITLE_COLOR_WHITE);
        }
    }

    public void setResetStatus(boolean isReset) {
        if (isReset){
            mState = STATE.RESET_PASSWORD;
        }else {
            mState = STATE.CHECK_PASSWORD;
        }
        updateUIState();
    }

    public void setIsWhiteBackground(boolean isWhite) {
        mIsWhiteBackground = isWhite;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lock_switch_method:
                if (mState == STATE.CONFIRM_PASSWORD) {
                    mState = mState.reset();
                }
                updateUIState();
                break;
        }
    }

    public interface OnUnlockListener{
        public void onCorrectPassword();
        public void onIncorrectPassword();
        public void onCancel();
    }

    public void setOnUnlockListener(OnUnlockListener listener){
        mOnUnlockListener = listener;
    }
}
