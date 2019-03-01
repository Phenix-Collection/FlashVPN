package com.polestar.domultiple.components.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.BuildConfig;
import com.polestar.domultiple.R;
import com.polestar.domultiple.billing.BillingConstants;
import com.polestar.domultiple.billing.BillingProvider;
import com.polestar.domultiple.notification.QuickSwitchNotification;
import com.polestar.domultiple.utils.EventReporter;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;
import com.polestar.domultiple.widget.BlueSwitch;
import com.polestar.domultiple.widget.RateDialog;
import com.polestar.domultiple.widget.UpDownDialog;

/**
 * Created by PolestarApp on 2017/7/18.
 */

public class SettingsActivity extends BaseActivity {
    private BlueSwitch shortCutSwich;
    private BlueSwitch quickSwitch;
    private BlueSwitch liteSwitch;
    private BlueSwitch adFreeSwitch;
    private boolean requestAdFree;

    private final static int REQUEST_UNLOCK_SETTINGS = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity_layout);
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.settings));
        RelativeLayout adFreeLayout = findViewById(R.id.adfree_layout);
        if (RemoteConfig.getBoolean("conf_has_adfree")) {
            adFreeLayout.setVisibility(View.VISIBLE);
        } else {
            adFreeLayout.setVisibility(View.GONE);
        }
        TextView rateUs = (TextView) findViewById(R.id.rate_us_txt);
        if(!RemoteConfig.getBoolean("show_rate_menu")) {
            rateUs.setVisibility(View.GONE);
        }
        shortCutSwich = (BlueSwitch) findViewById(R.id.shortcut_swichbtn);
        shortCutSwich.setChecked(PreferencesUtils.getBoolean(this, AppConstants.KEY_AUTO_CREATE_SHORTCUT,false));
        shortCutSwich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferencesUtils.putBoolean(SettingsActivity.this, AppConstants.KEY_AUTO_CREATE_SHORTCUT,shortCutSwich.isChecked());
            }
        });
        quickSwitch = (BlueSwitch) findViewById(R.id.quick_switch_btn);
        quickSwitch.setChecked(QuickSwitchNotification.isEnable());
        quickSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (QuickSwitchNotification.isEnable()) {
                    QuickSwitchNotification.disable();
                }else {
                    QuickSwitchNotification.enable();
                };
                quickSwitch.setChecked(QuickSwitchNotification.isEnable());
            }
        });
        liteSwitch = (BlueSwitch) findViewById(R.id.lite_switch_btn);
        liteSwitch.setChecked(PreferencesUtils.isLiteMode());
        DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean orig = PreferencesUtils.isLiteMode();
                switch (i) {
                    case UpDownDialog.NEGATIVE_BUTTON:
                        break;
                    case UpDownDialog.POSITIVE_BUTTON:
                        PreferencesUtils.setLiteMode(!orig);
                        VirtualCore.get().restart();
                        boolean newStatus = PreferencesUtils.isLiteMode();
                        if (!newStatus) {
                            Toast.makeText(SettingsActivity.this, getString(R.string.settings_lite_disable_toast), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SettingsActivity.this, getString(R.string.settings_lite_enable_toast), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                liteSwitch.setChecked(PreferencesUtils.isLiteMode());
            }
        };
        liteSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!PreferencesUtils.isLiteMode()) {
                    UpDownDialog.show(SettingsActivity.this, getString(R.string.delete_dialog_title), getString(R.string.settings_lite_enable_notice),
                            getString(R.string.no_thanks), getString(R.string.yes), -1,
                            R.layout.dialog_up_down, dialogListener).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            liteSwitch.setChecked(PreferencesUtils.isLiteMode());
                        }
                    });
                } else {
                    UpDownDialog.show(SettingsActivity.this, getString(R.string.delete_dialog_title), getString(R.string.settings_lite_disable_notice),
                            getString(R.string.no_thanks), getString(R.string.yes), -1,
                            R.layout.dialog_up_down, dialogListener).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            liteSwitch.setChecked(PreferencesUtils.isLiteMode());
                        }
                    });
                }
            }
        });

        adFreeSwitch = (BlueSwitch) findViewById(R.id.adfree_switch);
        adFreeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BillingProvider.get().isAdFreeVIP()) {
                    PreferencesUtils.setAdFree(adFreeSwitch.isChecked());
                    updateBillingStatus();
                } else {
                    PreferencesUtils.updateLastAdFreeDialogTime();
                    UpDownDialog.show(SettingsActivity.this, getString(R.string.adfree_dialog_title), getString(R.string.adfree_dialog_content),
                            getString(R.string.no_thanks), getString(R.string.yes), -1, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case UpDownDialog.POSITIVE_BUTTON:
                                            BillingProvider.get().getBillingManager()
                                                    .initiatePurchaseFlow(SettingsActivity.this, BillingConstants.SKU_AD_FREE, BillingClient.SkuType.INAPP);
                                            requestAdFree = true;
                                            PreferencesUtils.updateAdFreeClickStatus(true);
                                            break;
                                        case UpDownDialog.NEGATIVE_BUTTON:
                                            PreferencesUtils.updateAdFreeClickStatus(false);
                                            break;
                                    }
                                    adFreeSwitch.setChecked(PreferencesUtils.isAdFree());
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            PreferencesUtils.updateAdFreeClickStatus(false);
                            adFreeSwitch.setChecked(PreferencesUtils.isAdFree());
                        }
                    });
                }
            }
        });
        adFreeSwitch.setChecked(PreferencesUtils.isAdFree());
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

    public void onAboutClick(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void onRateUsClick(View view) {
        showRateDialog();
    }

    private void showRateDialog(){
        PreferencesUtils.updateRateDialogTime(this);
        RateDialog rateDialog = new RateDialog(this, "settings" );
        rateDialog.show().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                EventReporter.reportRate("settings_cancel", "settings");
            }
        });
    }

    public void onCustomizeClick(View view) {
        Intent intent = new Intent(this, CustomizeSettingActivity.class);
        startActivity(intent);
    }

    private void updateBillingStatus() {
        BillingProvider.get().updateStatus(new BillingProvider.OnStatusUpdatedListener() {
            @Override
            public void onStatusUpdated() {
                if (requestAdFree) {
                    if (BillingProvider.get().isAdFreeVIP()) {
                        PreferencesUtils.setAdFree(true);
                    }
                    requestAdFree = false;
                }
                adFreeSwitch.setChecked(PreferencesUtils.isAdFree());
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateBillingStatus();
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
