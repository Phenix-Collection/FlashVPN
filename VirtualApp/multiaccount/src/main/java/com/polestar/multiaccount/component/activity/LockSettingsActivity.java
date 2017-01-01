package com.polestar.multiaccount.component.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.MenuItem;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.widgets.BlueSwitch;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by guojia on 2017/1/1.
 */

public class LockSettingsActivity extends BaseActivity {

    public static final String EXTRA_KEY_FROM = "from";
    public static final String FROM_HOME_ICON = "home_icon";
    private Context mContext;

    private BlueSwitch lockerEnableSwitch;
    private LinearLayout detailedSettingLayout;

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
    }

    private void onLockerEnabled(boolean enabled) {
        if (enabled) {
            detailedSettingLayout.setVisibility(View.VISIBLE);
        }else{
            detailedSettingLayout.setVisibility(View.GONE);
        }
    }
    public void onPasswordSettingClick(View view) {

    }
}
