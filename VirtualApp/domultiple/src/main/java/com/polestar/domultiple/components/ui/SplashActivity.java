package com.polestar.domultiple.components.ui;

import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.polestar.booster.util.AndroidUtil;
import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.EventReporter;
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
        EventReporter.reportWake(this, "user_launch");

        if (!PreferencesUtils.isAdFree() && !PolestarApp.isArm64()) {
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

        if(!PolestarApp.isArm64()) {
            if (getIntent().getBooleanExtra(EXTRA_FROM_SHORTCUT, false)) {
                MLogs.d("Launching from shortcut");
                if (AndroidUtil.hasShortcut(this)) {
                    PreferencesUtils.setAbleToDetectShortcut(true);
                } else {
                    PreferencesUtils.setAbleToDetectShortcut(false);
                    MLogs.d("Failed to detect shortcut!");
                }
            }
        }//        }
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
        if(PolestarApp.isArm64()) {
            getPackageManager().setComponentEnabledSetting(new ComponentName(this, SplashActivity.class.getName()),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } else {
            if (!PreferencesUtils.isShortCutCreated()) {
                PreferencesUtils.setShortCutCreated();
                CommonUtils.createLaunchShortcut(this);
                created = true;
            }
            HomeActivity.enter(this, needUpdate());
        }
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
