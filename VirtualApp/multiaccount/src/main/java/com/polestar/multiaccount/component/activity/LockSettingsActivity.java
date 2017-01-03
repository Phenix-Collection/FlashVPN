package com.polestar.multiaccount.component.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.component.adapter.BasicPackageSwitchAdapter;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.ToastUtils;
import com.polestar.multiaccount.widgets.BlueSwitch;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

/**
 * Created by guojia on 2017/1/1.
 */

public class LockSettingsActivity extends BaseActivity {

    public static final String EXTRA_KEY_FROM = "from";
    public static final String FROM_HOME_ICON = "home_icon";
    public static final int REQUEST_SET_PASSWORD = 0;
    private Context mContext;

    private BlueSwitch lockerEnableSwitch;
    private LinearLayout detailedSettingLayout;
    private List<AppModel> mClonedModels;
    private BasicPackageSwitchAdapter mAppsAdapter;
    private ListView mCloneAppsListView;

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

    private void initData() {
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
                                model.setLockerState(AppConstants.AppLockState.ENABLED_FOR_CLONE);
                                DbManager.updateAppModel(mContext, model);
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
        lockerEnableSwitch.setChecked(PreferencesUtils.getBoolean(mContext,AppConstants.PreferencesKey.LOCKER_FEATHER_ENABLED));
        lockerEnableSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferencesUtils.putBoolean(LockSettingsActivity.this,
                        AppConstants.PreferencesKey.LOCKER_FEATHER_ENABLED,lockerEnableSwitch.isChecked());
                onLockerEnabled(lockerEnableSwitch.isChecked());
            }
        });
        onLockerEnabled(lockerEnableSwitch.isChecked());
        mCloneAppsListView = (ListView)findViewById(R.id.switch_lock_apps);

    }

    private void onLockerEnabled(boolean enabled) {
        if (enabled) {
            if (TextUtils.isEmpty(PreferencesUtils.getEncodedPatternPassword(mContext))) {
                LockPasswordSettingActivity.start(this, true, REQUEST_SET_PASSWORD);
                ToastUtils.ToastDefult(this, getString(R.string.no_password_set));
            } else {
                detailedSettingLayout.setVisibility(View.VISIBLE);
            }
        }else{
            detailedSettingLayout.setVisibility(View.GONE);
        }
    }
    public void onPasswordSettingClick(View view) {
        PreferencesUtils.setEncodedPatternPassword(mContext,"");
        LockPasswordSettingActivity.start(this, true, REQUEST_SET_PASSWORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MLogs.d("onActivityResult:　" + requestCode + ":" + resultCode);
        switch (requestCode) {
            case REQUEST_SET_PASSWORD:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        onLockerEnabled(true);
                        break;
                    case Activity.RESULT_CANCELED:
                        onLockerEnabled(false);
                        lockerEnableSwitch.setChecked(false);
                        break;
                }
                break;
        }
    }
}
