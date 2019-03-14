package com.polestar.superclone.component.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseActivity;
import com.polestar.superclone.component.adapter.BasicPackageSwitchAdapter;
import com.polestar.superclone.constant.AppConstants;
import com.polestar.superclone.db.DbManager;
import com.polestar.superclone.model.AppModel;
import com.polestar.superclone.utils.AppManager;
import com.polestar.superclone.utils.CloneHelper;
import com.polestar.superclone.utils.DisplayUtils;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.PreferencesUtils;
import com.polestar.superclone.utils.ToastUtils;
import com.polestar.superclone.widgets.BlueSwitch;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.List;

/**
 * Created by guojia on 2017/1/1.
 */

public class LockSettingsActivity extends BaseActivity {

    public static final String EXTRA_KEY_FROM = "from";
    public static final int REQUEST_SET_PASSWORD = 0;
    private Context mContext;

    private BlueSwitch lockerEnableSwitch;
    private LinearLayout detailedSettingLayout;
    private List<AppModel> mClonedModels;
    private BasicPackageSwitchAdapter mAppsAdapter;
    private ListView mCloneAppsListView;
    private boolean isSettingChanged = false;
    private String from;
    private Spinner lockIntervalSpinner;
    private View fingerprintLine;
    private View fingerprintLayout;

    private final long ARR_INTERVAL[] = {5*1000, 15*1000, 30*1000, 60*1000, 15*60*1000, 30*60*1000, 60*60*1000};

    public static void start(Activity activity, String from) {
        Intent intent = new Intent(activity, LockSettingsActivity.class);
        intent.putExtra(LockSettingsActivity.EXTRA_KEY_FROM, from);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_settings);
        setTitle(getResources().getString(R.string.lock_settings_title));
        mContext = this;
        initView();
        initData();
    }

    private int getIntervalIdx(long inverval) {
        int i = 0;
        for (long val: ARR_INTERVAL) {
            if (inverval == val) {
                return i;
            }
            i ++;
        }
        return  -1;
    }

    private void initData() {
        from = getIntent().getStringExtra(LockSettingsActivity.EXTRA_KEY_FROM);
        CloneHelper.getInstance(this).loadClonedApps(this, new CloneHelper.OnClonedAppChangListener() {
            @Override
            public void onInstalled(List<AppModel> clonedApp) {
            }

            @Override
            public void onUnstalled(List<AppModel> clonedApp) {
            }

            @Override
            public void onLoaded(List<AppModel> clonedApp) {
                mClonedModels = clonedApp;
                for (AppModel model : mClonedModels) {
                    model.setIcon(model.initDrawable(mContext));
                }
                mCloneAppsListView.post(new Runnable() {
                    @Override
                    public void run() {
                        mAppsAdapter = new BasicPackageSwitchAdapter(LockSettingsActivity.this);
                        mAppsAdapter.setModels(mClonedModels);
                        mAppsAdapter.setOnCheckStatusChangedListener(new BasicPackageSwitchAdapter.OnCheckStatusChangedListener() {
                            @Override
                            public void onCheckStatusChangedListener(AppModel model, boolean status) {
                                //
                                isSettingChanged = true;
                                if(status) {
                                    model.setLockerState(AppConstants.AppLockState.ENABLED_FOR_CLONE);
                                    EventReporter.lockerEnable(LockSettingsActivity.this, "enable",model.getPackageName(), from);
                                } else {
                                    model.setLockerState(AppConstants.AppLockState.DISABLED);
                                    EventReporter.lockerEnable(LockSettingsActivity.this, "disable", model.getPackageName(), from);
                                }
                                DbManager.updateAppModel(mContext, model);
                                MLogs.d("lock state changed: " + model.getPackageName());
                            }
                        });
                        mAppsAdapter.setIsCheckedCallback(new BasicPackageSwitchAdapter.IsCheckedCallback() {
                            @Override
                            public boolean isCheckedCallback(AppModel model) {
                                return model.getLockerState() != AppConstants.AppLockState.DISABLED;
                            }
                        });
                        mCloneAppsListView.setAdapter(mAppsAdapter);
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView(){
        detailedSettingLayout = (LinearLayout)findViewById(R.id.locker_detailed_settings);
        lockerEnableSwitch = (BlueSwitch)findViewById(R.id.enable_lock_switch);
        lockIntervalSpinner = (Spinner) findViewById(R.id.lock_interval_spinner);
        lockerEnableSwitch.setChecked(PreferencesUtils.getBoolean(mContext,AppConstants.PreferencesKey.LOCKER_FEATURE_ENABLED));
        lockerEnableSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSettingChanged = true;
                onLockerEnabled(lockerEnableSwitch.isChecked(), true);
            }
        });
        onLockerEnabled(lockerEnableSwitch.isChecked(), false);
        mCloneAppsListView = (ListView)findViewById(R.id.switch_lock_apps);
        lockIntervalSpinner.setSelection(getIntervalIdx(PreferencesUtils.getLockInterval()), true);
        lockIntervalSpinner.setDropDownVerticalOffset(DisplayUtils.dip2px(this, 15));
        lockIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                PreferencesUtils.setLockInterval(ARR_INTERVAL[i]);
                isSettingChanged = true;
                EventReporter.generalClickEvent(LockSettingsActivity.this, "set_relock_interval_" + i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        fingerprintLayout = findViewById(R.id.fingerprint_settings);
        fingerprintLine = findViewById(R.id.fingerprint_line);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            fingerprintLine.setVisibility(View.GONE);
            fingerprintLayout.setVisibility(View.GONE);
        } else {
            FingerprintManager fingerprintManager = (FingerprintManager)getSystemService(Context.FINGERPRINT_SERVICE);
            if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
                fingerprintLine.setVisibility(View.GONE);
                fingerprintLayout.setVisibility(View.GONE);
            }
        }
        BlueSwitch fingerSwith = fingerprintLayout.findViewById(R.id.enable_fingerprint_switch);
        fingerSwith.setChecked(PreferencesUtils.isFingerprintEnable());
        fingerSwith.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSettingChanged = true;
                boolean checked = fingerSwith.isChecked();
                fingerSwith.setChecked(checked);
                PreferencesUtils.setFingerprint(checked);
                EventReporter.generalClickEvent(LockSettingsActivity.this, "set_fingerprint_" + checked);
            }
        });
    }

    private void onLockerEnabled(boolean enabled, boolean report) {
        if (enabled) {
            if (TextUtils.isEmpty(PreferencesUtils.getEncodedPatternPassword(mContext)) || TextUtils.isEmpty(PreferencesUtils.getSafeAnswer(this))) {
                LockPasswordSettingActivity.start(this, true, null, REQUEST_SET_PASSWORD);
                ToastUtils.ToastDefult(this, getString(R.string.no_password_set));
                if(report) {
                    EventReporter.lockerEnable(this, "no_password", "none", from);
                }
            } else {
                detailedSettingLayout.setVisibility(View.VISIBLE);
                PreferencesUtils.setLockerEnabled(this, true);
                if(report) {
                    EventReporter.lockerEnable(this, "enable", "none", from);
                }
            }
        }else{
            detailedSettingLayout.setVisibility(View.GONE);
            PreferencesUtils.setLockerEnabled(this, false);
            PreferencesUtils.setEncodedPatternPassword(this,"");
            if(report) {
                EventReporter.lockerEnable(this, "disable", "none", from);
            }
        }
    }

    public void onPasswordSettingClick(View view) {
        //PreferencesUtils.setEncodedPatternPassword(mContext,"");
        isSettingChanged = true;
        LockPasswordSettingActivity.start(this, true, null, REQUEST_SET_PASSWORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MLogs.d("onActivityResult:　" + requestCode + ":" + resultCode);
        switch (requestCode) {
            case REQUEST_SET_PASSWORD:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        onLockerEnabled(true, true);
                        break;
                    case Activity.RESULT_CANCELED:
                        if (!PreferencesUtils.isLockerEnabled(this)
                                || TextUtils.isEmpty(PreferencesUtils.getEncodedPatternPassword(this))
                                || TextUtils.isEmpty(PreferencesUtils.getSafeAnswer(this))) {
                            onLockerEnabled(false, true);
                            lockerEnableSwitch.setChecked(false);
                        }
                        break;
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        AppManager.reloadLockerSetting();

    }
}
