package com.polestar.domultiple.components.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.domultiple.R;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.widget.locker.LockerView;


public class LockPasswordSettingActivity extends BaseActivity implements View.OnClickListener {
    public static final String EXTRA_MODE_RESET_PASSWORD = "launch_mode";
    public static final String EXTRA_PASSWORD_BACKGROUND_WHITE = "password_background";
    private static final String EXTRA_TITLE = "extra_title";

    private LockerView mAppLockScreenView;

    private boolean mIsReset = false;
    private boolean mIsWhiteBackground = false;
    private ImageView mFingerprint;
    private CancellationSignal cancellationSignal;

    public static void start(Activity activity, boolean resetPwd, String title, int requestCode) {
        Intent intent = new Intent(activity, LockPasswordSettingActivity.class);
        intent.putExtra(EXTRA_MODE_RESET_PASSWORD, resetPwd);
        if (title != null) {
            intent.putExtra(EXTRA_TITLE, title);
        }
        activity.startActivityForResult(intent,requestCode);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applock_password);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        setTitle(title != null? title : getResources().getString(R.string.password_setting_title));
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
        mAppLockScreenView = (LockerView) findViewById(R.id.appLockScreenView);
        mAppLockScreenView.setOnUnlockListener(mOnUnlockListener);

        mAppLockScreenView.onAfterShow();
        if (TextUtils.isEmpty(PreferencesUtils.getEncodedPatternPassword(this))) {
            mAppLockScreenView.setResetStatus(true);
        } else {
            mAppLockScreenView.setIsWhiteBackground(true);
            mAppLockScreenView.setResetStatus(false);
        }
        mAppLockScreenView.init();

        mFingerprint = (ImageView) findViewById(R.id.fingerprint_icon);
    }

    private LockerView.OnUnlockListener mOnUnlockListener = new LockerView.OnUnlockListener() {
        @Override
        public void onCorrectPassword() {
            MLogs.d("Password correct");
            if (mIsReset) {
                Toast.makeText(LockPasswordSettingActivity.this, R.string.password_set_complete, Toast.LENGTH_SHORT).show();
            }
            setResult(Activity.RESULT_OK);
            finish();
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
//                    PreferencesUtils.setSafeAnswer(this, "");
                    mFingerprint.setVisibility(View.INVISIBLE);
                }
            }

            if (intent.hasExtra(EXTRA_PASSWORD_BACKGROUND_WHITE)){
                mIsWhiteBackground = intent.getBooleanExtra(EXTRA_PASSWORD_BACKGROUND_WHITE, true);
                mAppLockScreenView.setIsWhiteBackground(mIsWhiteBackground);
            }
            mAppLockScreenView.init();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || !PreferencesUtils.isFingerprintEnable()) {
                mFingerprint.setVisibility(View.INVISIBLE);
            } else {
                FingerprintManager fm = (FingerprintManager)getSystemService(Context.FINGERPRINT_SERVICE);
                if (fm == null || !fm.isHardwareDetected() || !fm.hasEnrolledFingerprints()) {
                    mFingerprint.setVisibility(View.INVISIBLE);
                } else {
                    cancellationSignal = new CancellationSignal();
                    fm.authenticate(null, cancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, CharSequence errString) {
                            if (errorCode != FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                                Toast.makeText(LockPasswordSettingActivity.this, "" + errString, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                            Toast.makeText(LockPasswordSettingActivity.this, "" + helpString, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                            setResult(Activity.RESULT_OK);
                            finish();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                        }
                    }, null);
                }
            }
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        mAppLockScreenView.onBeforeHide();
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MLogs.d("password onActivityResult:　" + requestCode + ":" + resultCode);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
