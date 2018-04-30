package mochat.multiple.parallel.whatsclone.component.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.lody.virtual.client.core.VirtualCore;
import mochat.multiple.parallel.whatsclone.billing.BillingConstants;
import mochat.multiple.parallel.whatsclone.billing.BillingProvider;
import mochat.multiple.parallel.whatsclone.R;
import mochat.multiple.parallel.whatsclone.component.BaseActivity;
import mochat.multiple.parallel.whatsclone.constant.AppConstants;
import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.EventReporter;
import mochat.multiple.parallel.whatsclone.utils.PreferencesUtils;
import mochat.multiple.parallel.whatsclone.widgets.RoundSwitch;
import mochat.multiple.parallel.whatsclone.widgets.UpDownDialog;

/**
 * Created by yxx on 2016/7/29.
 */
public class SettingsActivity extends BaseActivity {
    private RoundSwitch shortCutSwich;
    private RoundSwitch gmsSwitch;
    private RoundSwitch adFreeSwitch;

    private boolean requestAdFree;

    private final static int REQUEST_UNLOCK_SETTINGS = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.settings));

        shortCutSwich = (RoundSwitch) findViewById(R.id.shortcut_swichbtn);
        shortCutSwich.setChecked(PreferencesUtils.getBoolean(this, AppConstants.KEY_AUTO_CREATE_SHORTCUT,false));
        shortCutSwich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferencesUtils.putBoolean(SettingsActivity.this, AppConstants.KEY_AUTO_CREATE_SHORTCUT,shortCutSwich.isChecked());
            }
        });
        gmsSwitch = (RoundSwitch) findViewById(R.id.gms_switch_btn);
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

        adFreeSwitch = (RoundSwitch) findViewById(R.id.adfree_switch);
        adFreeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventReporter.generalClickEvent(SettingsActivity.this, "click_ad_free_switch");
                if (BillingProvider.get().isAdFreeVIP()) {
                    PreferencesUtils.setAdFree(adFreeSwitch.isChecked());
                    updateBillingStatus();
                } else {
                    EventReporter.generalClickEvent(SettingsActivity.this, "ad_free_dialog_from_setting");
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
                                            EventReporter.generalClickEvent(SettingsActivity.this, "click_ad_free_dialog_yes");
                                            break;
                                        case UpDownDialog.NEGATIVE_BUTTON:
                                            PreferencesUtils.updateAdFreeClickStatus(false);
                                            EventReporter.generalClickEvent(SettingsActivity.this, "click_ad_free_dialog_no");
                                            break;
                                    }
                                    adFreeSwitch.setChecked(PreferencesUtils.isAdFree());
                                }
                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            PreferencesUtils.updateAdFreeClickStatus(false);
                            EventReporter.generalClickEvent(SettingsActivity.this, "click_ad_free_dialog_no");
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

    public void onFeedbackClick(View view) {
        Intent intent = new Intent(this, FeedbackActivity.class);
        startActivity(intent);
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

    private void updateBillingStatus() {
        BillingProvider.get().updateStatus(new BillingProvider.OnStatusUpdatedListener() {
            @Override
            public void onStatusUpdated() {
                MLogs.d("Billing onStatusUpdated");
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
