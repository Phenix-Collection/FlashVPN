package com.polestar.multiaccount.component.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.ToastUtils;
import com.polestar.multiaccount.widgets.LockerView;

public class LockPasswordSettingActivity extends BaseActivity implements View.OnClickListener {
    public static final String EXTRA_MODE_RESET_PASSWORD = "launch_mode";
    public static final String EXTRA_PASSWORD_BACKGROUND_WHITE = "password_background";
    public static final int REQUEST_SET_QUESTION = 0;

    private TextView mForgetPasswordBtn;
    private LockerView mAppLockScreenView;

    private boolean mIsReset = false;
    private boolean mIsWhiteBackground = false;

    public static void start(Activity activity, boolean resetPwd, int resultCode) {
        Intent intent = new Intent(activity, LockPasswordSettingActivity.class);
        intent.putExtra(EXTRA_MODE_RESET_PASSWORD, resetPwd);
        activity.startActivityForResult(intent,resultCode);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applock_password);
        setTitle(getResources().getString(R.string.password_setting_title));
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void initView() {
        mForgetPasswordBtn = (TextView) findViewById(R.id.forgot_password_tv);
        mForgetPasswordBtn.setOnClickListener(this);

        mAppLockScreenView = (LockerView) findViewById(R.id.appLockScreenView);
        mAppLockScreenView.setOnUnlockListener(mOnUnlockListener);

        mAppLockScreenView.onAfterShow();
        if (TextUtils.isEmpty(PreferencesUtils.getEncodedPatternPassword(this))
                || !PreferencesUtils.isSafeQuestionSet(this)) {
            mForgetPasswordBtn.setVisibility(View.GONE);
            mAppLockScreenView.setResetStatus(true);
        } else {
            mForgetPasswordBtn.setVisibility(View.VISIBLE);
            mAppLockScreenView.setIsWhiteBackground(true);
            mAppLockScreenView.setResetStatus(false);
        }
        mAppLockScreenView.init();
    }

    private LockerView.OnUnlockListener mOnUnlockListener = new LockerView.OnUnlockListener() {
        @Override
        public void onCorrectPassword() {
            MLogs.d("Password correct");
            if (mIsReset) {
                LockSecureQuestionActivity.start(LockPasswordSettingActivity.this, REQUEST_SET_QUESTION, mIsReset, null);
            } else {
                setResult(Activity.RESULT_OK);
                finish();
            }
        }

        @Override
        public void onIncorrectPassword() {
        }

        @Override
        public void onCancel() {
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null){

            if (intent.hasExtra(EXTRA_MODE_RESET_PASSWORD)) {
                mIsReset = intent.getBooleanExtra(EXTRA_MODE_RESET_PASSWORD, false);
                mAppLockScreenView.setResetStatus(mIsReset);
                if (mIsReset){
                    mForgetPasswordBtn.setVisibility(View.GONE);
                    PreferencesUtils.setSafeAnswer(this, "");
                }
            }

            if (intent.hasExtra(EXTRA_PASSWORD_BACKGROUND_WHITE)){
                mIsWhiteBackground = intent.getBooleanExtra(EXTRA_PASSWORD_BACKGROUND_WHITE, true);
                mAppLockScreenView.setIsWhiteBackground(mIsWhiteBackground);
            }
            mAppLockScreenView.init();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mAppLockScreenView.onBeforeHide();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.forgot_password_tv:
                Intent intent = null;
//                try {
//                    intent = new Intent(LockPasswordSettingActivity.this, AppLockSafeQuestionActivity.class);
//                    intent.putExtra(AppLockSafeQuestionActivity.EXTRA_TITLE,getString(R.string.al_change_security_question));
//                    intent.putExtra(AppLockSafeQuestionActivity.EXTRA_RESET, true);
//                    startActivity(intent);
//                }finally {
//                    finish();
//                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_SET_QUESTION:
                switch (requestCode) {
                    case Activity.RESULT_CANCELED:
                        ToastUtils.ToastDefult(this, getResources().getString(R.string.password_set_no_question));
                        break;
                    case Activity.RESULT_OK:
                        ToastUtils.ToastDefult(this, getResources().getString(R.string.password_set_complete));
                        break;
                }
                setResult(Activity.RESULT_OK);
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
