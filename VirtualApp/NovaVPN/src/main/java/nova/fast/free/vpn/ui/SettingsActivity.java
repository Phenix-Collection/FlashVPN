package nova.fast.free.vpn.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;

import com.android.billingclient.api.BillingClient;

import nova.fast.free.vpn.NovaUser;
import nova.fast.free.vpn.R;
import nova.fast.free.vpn.billing.BillingConstants;
import nova.fast.free.vpn.billing.BillingProvider;
import nova.fast.free.vpn.ui.widget.RoundSwitch;
import nova.fast.free.vpn.ui.widget.UpDownDialog;
import nova.fast.free.vpn.utils.EventReporter;
import nova.fast.free.vpn.utils.MLogs;
import nova.fast.free.vpn.utils.PreferenceUtils;

public class
SettingsActivity extends BaseActivity {
    private RoundSwitch bootSwitch;

    private boolean requestVip;

    public static void start(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.settings));
        bootSwitch = findViewById(R.id.start_onboot_switch);
        bootSwitch.setChecked(PreferenceUtils.isStartOnBoot());
        bootSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                EventReporter.generalEvent(SettingsActivity.this,"set_start_on_boot_" + b);
                PreferenceUtils.setStartOnBoot(b);
            }
        });
    }

    public void onAppProxyClick(View view) {
        AppProxySettingActivity.start(this);
    }

    public void onFeedbackClick(View view) {
        Intent intent = new Intent(this, FeedbackActivity.class);
        startActivity(intent);
    }

    public void onFaqClick(View view) {
        Intent intent = new Intent(this, FaqActivity.class);
        startActivity(intent);
    }

    public void onAboutClick(View view) {
        try {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } catch (Exception localException1) {
            localException1.printStackTrace();
        }
    }

    public void onUserCenterClick(View view){
        UserCenterActivity.start(this, UserCenterActivity.FROM_SETTING);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
