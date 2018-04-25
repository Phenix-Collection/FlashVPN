package mochat.multiple.parallel.whatsclone.component.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import mochat.multiple.parallel.whatsclone.R;
import mochat.multiple.parallel.whatsclone.component.BaseActivity;
import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.PreferencesUtils;
import mochat.multiple.parallel.whatsclone.utils.ToastUtils;
import mochat.multiple.parallel.whatsclone.widgets.locker.LockerView;

public class LockPasswordSettingActivity extends BaseActivity implements View.OnClickListener {
    public static final String EXTRA_MODE_RESET_PASSWORD = "launch_mode";
    public static final String EXTRA_PASSWORD_BACKGROUND_WHITE = "password_background";
    public static final int REQUEST_SET_QUESTION = 0;
    public static final int REQUEST_CHECK_ANSWER = 1;
    private static final String EXTRA_TITLE = "extra_title";

    private TextView mForgetPasswordBtn;
    private LockerView mAppLockScreenView;

    private boolean mIsReset = false;
    private boolean mIsWhiteBackground = false;

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
                LockSecureQuestionActivity.start(LockPasswordSettingActivity.this, REQUEST_SET_QUESTION, true);
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
                    mForgetPasswordBtn.setVisibility(View.GONE);
//                    PreferencesUtils.setSafeAnswer(this, "");
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
                LockSecureQuestionActivity.start(this, REQUEST_CHECK_ANSWER, false );
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MLogs.d("password onActivityResult:　" + requestCode + ":" + resultCode);
        switch (requestCode){
            case REQUEST_SET_QUESTION:
                switch (resultCode) {
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
            case REQUEST_CHECK_ANSWER:
//                setResult(resultCode);
//                finish();
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
