package com.polestar.domultiple.components.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;

/**
 * Created by guojia on 2017/7/15.
 */

public class SplashActivity extends BaseActivity {

    private static boolean created;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    }


    public void createShortCut(){
        //创建快捷方式的Intent
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //不允许重复创建
        shortcutintent.putExtra("duplicate", false);
        //需要现实的名称
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        //快捷图片
        Parcelable icon = Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.mipmap.ic_launcher);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        //点击快捷图片，运行的程序主入口
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(getApplicationContext() , SplashActivity.class));
        //发送广播。OK
        sendBroadcast(shortcutintent);
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
        if(!PreferencesUtils.isShortCutCreated() && !created) {
            PreferencesUtils.setShortCutCreated();
            createShortCut();
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
