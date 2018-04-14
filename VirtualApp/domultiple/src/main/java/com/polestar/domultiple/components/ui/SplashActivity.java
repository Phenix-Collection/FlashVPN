package com.polestar.domultiple.components.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.polestar.booster.util.AndroidUtil;
import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;

/**
 * Created by PolestarApp on 2017/7/15.
 */

public class SplashActivity extends BaseActivity {

    private static boolean created;
    public final static String EXTRA_FROM_SHORTCUT = "extra_from_shortcut";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MLogs.d(this.getClass().getName() +" launching from intent: " +getIntent());
        long time = System.currentTimeMillis();
        setContentView(R.layout.splash_activity_layout);
//        mainLayout.setBackgroundResource(R.mipmap.launcher_bg_main);
        if (!PreferencesUtils.isAdFree()) {
            FuseAdLoader adLoader = FuseAdLoader.get(HomeActivity.SLOT_HOME_NATIVE, this.getApplicationContext());
            adLoader.setBannerAdSize(HomeActivity.getBannerAdSize());
            adLoader.preloadAd();
        }
        Handler handler = new Handler();
        CloneManager.getInstance(this).loadClonedApps(this, null);

        //VirtualCore.get().waitForEngine();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServiceManagerNative.ensureServerStarted();
            }
        }).start();
        long delta = System.currentTimeMillis() - time;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enterHome();
            }
        }, 2500 - delta);

        if (getIntent().getBooleanExtra(EXTRA_FROM_SHORTCUT, false)) {
            MLogs.d("Launching from shortcut");
            if (AndroidUtil.hasShortcut(this)) {
                PreferencesUtils.setAbleToDetectShortcut(true);
                long install = CommonUtils.getInstallTime(PolestarApp.getApp(),this.getPackageName());
                long firstHideSec = RemoteConfig.getLong("first_hide_shortcut_sec");
                if ( RemoteConfig.getBoolean("auto_hide_shortcut") &&
                        (System.currentTimeMillis() - install) > firstHideSec*1000) {
                    hide();
                }
            } else {
                PreferencesUtils.setAbleToDetectShortcut(false);
                MLogs.d("Failed to detect shortcut!");
            }
        } else {
//            if(checkCallingOrSelfPermission("com.android.launcher.permission.INSTALL_SHORTCUT")
//                    == PackageManager.PERMISSION_GRANTED) {
            MLogs.d("Launch from not shortcut!");
                long install = CommonUtils.getInstallTime(PolestarApp.getApp(),this.getPackageName());
                long firstHideSec= RemoteConfig.getLong("first_hide_shortcut_sec");
                if ( RemoteConfig.getBoolean("force_hide_shortcut") && RemoteConfig.getBoolean("auto_hide_shortcut") &&
                        (System.currentTimeMillis() - install) > firstHideSec*1000) {
                    MLogs.d("Force hide shortcut!");
                    hide();
                }
            }
//        }
    }

    private void hide(){
        MLogs.d("Has shortcut, hide icon");
        PackageManager pm = getPackageManager();
        if (pm.getComponentEnabledSetting(new ComponentName(this, SplashActivity.class))
          != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            MLogs.d("disable activity");
            pm.setComponentEnabledSetting(new ComponentName(this, SplashActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }

    private void enterHome(){
        if(!AndroidUtil.hasShortcut(this)) {
            PreferencesUtils.setShortCutCreated();
            CommonUtils.createLaunchShortcut(this);
            created = true;
        }
        HomeActivity.enter(this, needUpdate());
        finish();
    }

    private boolean needUpdate() {
        try {
            PackageInfo vinfo = getPackageManager().getPackageInfo(getPackageName(),0);
            int versionCode = vinfo.versionCode;
            long pushVersion = RemoteConfig.getLong(AppConstants.CONF_UPDATE_VERSION);
            long latestVersion = RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION);
            long ignoreVersion = PreferencesUtils.getIgnoreVersion();
            MLogs.d("local: " + versionCode + " push: " + pushVersion + " latest: " + latestVersion + " ignore: "+ ignoreVersion);
            if (versionCode <= pushVersion
                    && ignoreVersion < latestVersion) {
                return true;
            }
        }catch (Exception e) {
            MLogs.e(e);
        }
        return false;
    }
}
