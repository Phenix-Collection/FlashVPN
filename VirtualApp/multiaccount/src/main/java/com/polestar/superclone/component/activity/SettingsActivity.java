package com.polestar.superclone.component.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.billing.BillingConstants;
import com.polestar.billing.BillingProvider;
import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseActivity;
import com.polestar.superclone.constant.AppConstants;
import com.polestar.superclone.notification.FastSwitch;
import com.polestar.superclone.reward.VIPActivity;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.PreferencesUtils;
import com.polestar.superclone.widgets.BlueSwitch;
import com.polestar.superclone.widgets.UpDownDialog;

/**
 * Created by yxx on 2016/7/29.
 */
public class SettingsActivity extends BaseActivity {
    private BlueSwitch shortCutSwich;
    private BlueSwitch fastSwitch;
    private BlueSwitch gmsSwitch;
    private View vipItem;

    private final static int REQUEST_UNLOCK_SETTINGS = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.settings));

        fastSwitch = (BlueSwitch)  findViewById(R.id.fastswitch_switch);
        fastSwitch.setChecked(FastSwitch.isEnable());
        fastSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FastSwitch.isEnable()) {
                    FastSwitch.disable();
                }else {
                    FastSwitch.enable();
                };
                fastSwitch.setChecked(FastSwitch.isEnable());
            }
        });
        shortCutSwich = (BlueSwitch) findViewById(R.id.shortcut_swichbtn);
        shortCutSwich.setChecked(PreferencesUtils.getBoolean(this, AppConstants.KEY_AUTO_CREATE_SHORTCUT,false));
        shortCutSwich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferencesUtils.putBoolean(SettingsActivity.this, AppConstants.KEY_AUTO_CREATE_SHORTCUT,shortCutSwich.isChecked());
            }
        });
        gmsSwitch = (BlueSwitch) findViewById(R.id.gms_switch_btn);
        gmsSwitch.setChecked(PreferencesUtils.isGMSEnable());
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean orig = PreferencesUtils.isGMSEnable();
                switch (i) {
                    case UpDownDialog.NEGATIVE_BUTTON:
                        break;
                    case UpDownDialog.POSITIVE_BUTTON:
                        PreferencesUtils.setGMSEnable(!orig);
                        VirtualCore.get().restart();
                        boolean newStatus = PreferencesUtils.isGMSEnable();
                        EventReporter.setGMS(SettingsActivity.this,newStatus, "setting");
                        if (newStatus) {
                            Toast.makeText(SettingsActivity.this, getString(R.string.settings_gms_enable_toast), Toast.LENGTH_SHORT);
                        } else {
                            Toast.makeText(SettingsActivity.this, getString(R.string.settings_gms_disable_toast), Toast.LENGTH_SHORT);
                        }
                        break;
                }
                gmsSwitch.setChecked(PreferencesUtils.isGMSEnable());
            }
        };
        gmsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventReporter.generalClickEvent(SettingsActivity.this, "settings_gms_switch");
                if(PreferencesUtils.isGMSEnable()) {
                    UpDownDialog.show(SettingsActivity.this, getString(R.string.delete_dialog_title), getString(R.string.settings_gms_disable_notice),
                            getString(R.string.no_thanks), getString(R.string.yes), -1,
                            R.layout.dialog_up_down, dialogListener).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            gmsSwitch.setChecked(PreferencesUtils.isGMSEnable());
                        }
                    });
                } else {
                    UpDownDialog.show(SettingsActivity.this, getString(R.string.delete_dialog_title), getString(R.string.settings_gms_enable_notice),
                            getString(R.string.no_thanks), getString(R.string.yes), -1,
                            R.layout.dialog_up_down, dialogListener).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            gmsSwitch.setChecked(PreferencesUtils.isGMSEnable());
                        }
                    });
                }
            }
        });

        vipItem = findViewById(R.id.vip_item);
        vipItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VIPActivity.start(SettingsActivity.this, VIPActivity.FROM_SETTING);
            }
        });
    }

    public void onNotificationSettingClick(View view) {
        Intent notification = new Intent(this, NotificationActivity.class);
        startActivity(notification);
    }

    public void onPrivacyLockerClick(View view) {
        if (PreferencesUtils.isLockerEnabled(this) ) {
            LockPasswordSettingActivity.start(this, false, getString(R.string.lock_settings_title), REQUEST_UNLOCK_SETTINGS);
        } else {
            LockSettingsActivity.start(this,"setting");
        }
    }

    public void onFeedbackClick(View view) {
        FeedbackActivity.start(this, 0);
    }

    public void onFaqClick(View view) {
        Intent intent = new Intent(this, FaqActivity.class);
        startActivity(intent);
    }

    public void onCustomizeClick(View view) {
        Intent intent = new Intent(this, CustomizeSettingActivity.class);
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


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_UNLOCK_SETTINGS) {
            switch (resultCode) {
                case RESULT_OK:
                    LockSettingsActivity.start(this, "setting");
                    break;
                case RESULT_CANCELED:
                    break;
            }
        }
    }
}
