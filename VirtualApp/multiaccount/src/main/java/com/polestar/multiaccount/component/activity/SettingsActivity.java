package com.polestar.multiaccount.component.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.lody.virtual.client.core.VirtualCore;
import com.polestar.multiaccount.BuildConfig;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.polestar.multiaccount.utils.ToastUtils;
import com.polestar.multiaccount.widgets.BlueSwitch;

/**
 * Created by yxx on 2016/7/29.
 */
public class SettingsActivity extends BaseActivity {
    private BlueSwitch shortCutSwich;
    private BlueSwitch gmsSwitch;
    private TextView versionTv;
    private TextView followTv;
    private String fbUrl;

    private final static int REQUEST_UNLOCK_SETTINGS = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
    }

    private void initView() {
        setTitle(getString(R.string.settings));
        versionTv = (TextView)findViewById(R.id.version_info);
        followTv = (TextView)findViewById(R.id.follow_us_txt);
        versionTv.setText(getString(R.string.settings_right) + "\n" + "Version: " + BuildConfig.VERSION_NAME);
        fbUrl = RemoteConfig.getString("fb_follow_page");
        if (fbUrl == null || fbUrl.equals("off")) {
            followTv.setVisibility(View.INVISIBLE);
        }
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
        gmsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferencesUtils.setGMSEnable(gmsSwitch.isChecked());
                //ToastUtils.ToastDefult(SettingsActivity.this, "GMS state: " + PreferencesUtils.isGMSEnable());
                VirtualCore.get().restart();
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

    public void onPrivacyPolicyClick(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.settings_privacy_policy));
        intent.putExtra(WebViewActivity.EXTRA_URL, "file:///android_asset/privacy_policy.html");
        startActivity(intent);
    }

    public void onTermsClick(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_TITLE, getString(R.string.settings_terms_of_service));
        intent.putExtra(WebViewActivity.EXTRA_URL, "file:///android_asset/term_of_service.html");
        startActivity(intent);
    }

    public void onFollowUsClick(View view) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo("com.facebook.katana", 0);
            if (packageInfo != null && packageInfo.versionCode >= 3002850) {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("fb://facewebmodal/f?href=" + fbUrl));
//                intent.putExtra("START_OUTTER_APP_FLAG",true);
                startActivity(intent);
            }else{
                Intent intent = new Intent("android.intent.action.VIEW",Uri.parse(fbUrl));
//                intent.putExtra("START_OUTTER_APP_FLAG",true);
                startActivity(intent);
            }
        } catch (Exception localException1) {
            try {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(fbUrl));
//                intent.putExtra("START_OUTTER_APP_FLAG",true);
                startActivity(intent);
            } catch (Exception localException2) {
            }
        }
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
