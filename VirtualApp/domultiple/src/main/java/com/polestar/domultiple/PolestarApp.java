package com.polestar.domultiple;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.batmobi.BatmobiLib;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.booster.BoosterSdk;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.CrashHandler;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.helper.utils.VLog;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.domultiple.billing.BillingProvider;
import com.polestar.domultiple.clone.CloneApiDelegate;
import com.polestar.domultiple.clone.CloneComponentDelegate;
import com.polestar.domultiple.notification.QuickSwitchNotification;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.EventReporter;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;
import com.polestar.domultiple.widget.locker.AppLockMonitor;
import com.polestar.grey.GreyAttribute;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.util.List;

import nativesdk.ad.common.AdSdk;

/**
 * Created by PolestarApp on 2017/7/15.
 */

public class PolestarApp extends MultiDexApplication {

    private static PolestarApp gDefault;

    public static PolestarApp getApp() {
        return gDefault;
    }

    public static boolean isAvzEnabled() {
        String conf = RemoteConfig.getString(AppConstants.CONF_WALL_SDK);
        return  "all".equals(conf) || "avz".equals(conf);
    }

    public static boolean isOpenLog(){
        File file = new File(Environment.getExternalStorageDirectory() + "/polelog");
        boolean ret =  file.exists();
        if(ret) {
            Log.d(MLogs.DEFAULT_TAG, "log opened by file");
        }
        return  ret;
    }

    @Override
    protected void attachBaseContext(Context base) {
        Log.d(MLogs.DEFAULT_TAG, "APP version: " + BuildConfig.VERSION_NAME + " Type: " + BuildConfig.BUILD_TYPE);
        Log.d(MLogs.DEFAULT_TAG, "LIB version: " + com.lody.virtual.BuildConfig.VERSION_NAME + " Type: " + com.lody.virtual.BuildConfig.BUILD_TYPE );

        super.attachBaseContext(base);
        gDefault = this;
        try {
            VASettings.ENABLE_IO_REDIRECT = true;
            VASettings.ENABLE_INNER_SHORTCUT = false;
            VASettings.ENABLE_GMS = !PreferencesUtils.isLiteMode();
            Log.d(MLogs.DEFAULT_TAG, "GMS state: " + VASettings.ENABLE_GMS);
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void initAd() {
        MobileAds.initialize(gDefault, "ca-app-pub-5490912237269284~6272167416");
        if (isAvzEnabled()) {
            AdSdk.initialize(gDefault,"39fi40iihgfedc1",null);
        }
        FuseAdLoader.init(new FuseAdLoader.ConfigFetcher() {
            @Override
            public boolean isAdFree() {
                return PreferencesUtils.isAdFree();
            }

            @Override
            public List<AdConfig> getAdConfigList(String slot) {
                return RemoteConfig.getAdConfigList(slot);
            }
        });
        BatmobiLib.init(gDefault, "7OO01FES0DJDJRSSETIBBULF");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        VLog.setKeyLogger(new VLog.IKeyLogger() {
            @Override
            public void keyLog(Context context, String tag, String log) {
                MLogs.logBug(tag,log);
                EventReporter.keyLog(gDefault, tag, log);
            }

            @Override
            public void logBug(String tag, String log) {
                MLogs.logBug(tag, log);
            }
        });
        VirtualCore virtualCore = VirtualCore.get();
        virtualCore.initialize(new VirtualCore.VirtualInitializer() {

            @Override
            public void onMainProcess() {
                MLogs.d("Main process create");

                FirebaseApp.initializeApp(gDefault);
                RemoteConfig.init();
                //ImageLoaderUtil.asyncInit(gDefault);
                initRawData();
                //registerActivityLifecycleCallbacks(new LocalActivityLifecycleCallBacks(MApp.this, true));
                EventReporter.init(gDefault);
                BillingProvider.get();
                initAd();
                //CloneManager.getInstance(gDefault).loadClonedApps(gDefault, null);
                //
                BoosterSdk.BoosterConfig boosterConfig = new BoosterSdk.BoosterConfig();
                if (BuildConfig.DEBUG) {
                    boosterConfig.autoAdFirstInterval = 0;
                    boosterConfig.autoAdInterval = 0;
                    boosterConfig.isUnlockAd = true;
                    boosterConfig.isInstallAd = true;
                } else {
                    boosterConfig.autoAdFirstInterval = RemoteConfig.getLong("auto_ad_first_interval") * 1000;
                    boosterConfig.autoAdInterval = RemoteConfig.getLong("auto_ad_interval") * 1000;
                    boosterConfig.isUnlockAd = RemoteConfig.getBoolean("allow_unlock_ad");
                    boosterConfig.isInstallAd = RemoteConfig.getBoolean("allow_install_ad");
                }
                BoosterSdk.BoosterRes res = new BoosterSdk.BoosterRes();
                res.outterWheelImage = R.drawable.booster_ic_wheel_outside;
                res.innerWheelImage = R.drawable.booster_ic_wheel_inside;
                res.titleString = R.string.boost_title;
                res.boosterShorcutIcon = R.drawable.booster_shortcut;
                BoosterSdk.init(gDefault, boosterConfig, res, new BoosterSdk.IEventReporter() {
                    @Override
                    public void reportEvent(String s, Bundle b) {
                        FirebaseAnalytics.getInstance(PolestarApp.getApp()).logEvent(s, b);
                    }
                });
                String coffeeKey = RemoteConfig.getString("coffee_key");
                if (!TextUtils.isEmpty(coffeeKey) && !"off".equals(coffeeKey)) {
                    MLogs.d("coffee key : " + coffeeKey);
                    instantcoffee.Builder.build(getApp(),coffeeKey);
                }

                PreferencesUtils.putString(gDefault, "grey_source_id", RemoteConfig.getString("grey_source_id"));
                //BoosterSdk.setMemoryThreshold(20);
                //BoosterSdk.showSettings(this);
            }

            @Override
            public void onVirtualProcess() {
                MLogs.d("Virtual process create");
                CloneComponentDelegate delegate = new CloneComponentDelegate();
                delegate.asyncInit();
                virtualCore.setComponentDelegate(delegate);

                virtualCore.setAppApiDelegate(new CloneApiDelegate());
                GreyAttribute.init(PreferencesUtils.getString(gDefault, "grey_source_id", "29026"));
            }

            @Override
            public void onServerProcess() {
                MLogs.d("Server process create");
                try {
                    VirtualCore.get().setAppRequestListener(new VirtualCore.AppRequestListener() {
                        @Override
                        public void onRequestInstall(String path) {
                            //We can start AppInstallActivity TODO
                        }

                        @Override
                        public void onRequestUninstall(String pkg) {

                        }
                    });
                }catch (Exception ex) {
                    MLogs.logBug(ex);
                }
                FirebaseApp.initializeApp(gDefault);
                RemoteConfig.init();
                CloneComponentDelegate delegate = new CloneComponentDelegate();
                delegate.asyncInit();
                VirtualCore.get().setComponentDelegate(delegate);
                initAd();
                AppLockMonitor.getInstance();
                if (QuickSwitchNotification.getInstance(gDefault).isEnable()) {
                    QuickSwitchNotification.getInstance(gDefault).init();
                }
            }
        });

        try {
            // asyncInit exception handler and bugly before attatchBaseContext and appOnCreate
            final MAppCrashHandler ch = new MAppCrashHandler(this, Thread.getDefaultUncaughtExceptionHandler());
            Thread.setDefaultUncaughtExceptionHandler(ch);
            VirtualCore.get().setCrashHandler(new CrashHandler() {
                @Override
                public void handleUncaughtException(Thread t, Throwable e) {
                    ch.uncaughtException(t, e);
                }
            });
            initBugly(gDefault);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (isOpenLog() || !AppConstants.IS_RELEASE_VERSION  || BuildConfig.DEBUG) {
            VLog.openLog();
            VLog.d(MLogs.DEFAULT_TAG, "VLOG is opened");
            MLogs.DEBUG = true;
            AdConstants.DEBUG = true;
            BoosterSdk.DEBUG = true;
        }

    }

    private class MAppCrashHandler implements Thread.UncaughtExceptionHandler {

        private Context context;
        private Thread.UncaughtExceptionHandler orig;
        MAppCrashHandler(Context c, Thread.UncaughtExceptionHandler orig) {
            context = c;
            this.orig = orig;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            MLogs.logBug("uncaughtException");

            String pkg;
            int tag;
            //CrashReport.startCrashReport();
            //1. innerContext = null, internal error in Pb
            if (VirtualCore.get() != null
                    && (VirtualCore.get().isMainProcess() )) {
                MLogs.logBug("Super Clone main app exception, exit.");
                pkg = "main";
                tag = AppConstants.CrashTag.MAPP_CRASH;
                //CrashReport.setUserSceneTag(context, AppConstants.CrashTag.MAPP_CRASH);
            } else if(VirtualCore.get()!= null && VirtualCore.get().isServerProcess()){
                MLogs.logBug("Server process crash!");
                pkg = "server";
                tag = AppConstants.CrashTag.SERVER_CRASH;
                //CrashReport.setUserSceneTag(context, AppConstants.CrashTag.SERVER_CRASH);
            } else {
                MLogs.logBug("Client process crash!");
                pkg = VClientImpl.get() == null? null: VClientImpl.get().getCurrentPackage();
                tag = AppConstants.CrashTag.CLONE_CRASH;
                //CrashReport.setUserSceneTag(context, AppConstants.CrashTag.CLONE_CRASH);
            }
            MLogs.logBug(MLogs.getStackTraceString(ex));

            ActivityManager.RunningAppProcessInfo info = CommonUtils.getForegroundProcess(context);
            boolean forground = false;
            if (info != null && android.os.Process.myPid() == info.pid) {
                MLogs.logBug("forground crash");
                forground = true;
                //CrashReport.setUserSceneTag(context, AppConstants.CrashTag.FG_CRASH);
            }
            Intent crash = new Intent("appclone.intent.action.SHOW_CRASH_DIALOG");
            crash.putExtra("package", pkg);
            crash.putExtra("forground", forground);
            crash.putExtra("exception", ex);
            crash.putExtra("tag", tag);
            sendBroadcast(crash);
            //CrashReport.postCatchedException(ex);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }
    private void setDefaultUncaughtExceptionHandler(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new MAppCrashHandler(context, Thread.getDefaultUncaughtExceptionHandler()));
    }

    private void initRawData() {
    }

    private void initBugly(Context context) {
        //Bugly
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        AppConstants.IS_RELEASE_VERSION = !channel.equals(AppConstants.DEVELOP_CHANNEL);
        MLogs.e("IS_RELEASE_VERSION: " + AppConstants.IS_RELEASE_VERSION);

        MLogs.e("versioncode: " + CommonUtils.getCurrentVersionCode(context) + ", versionName:" + CommonUtils.getCurrentVersionName(context));
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        String referChannel = PreferencesUtils.getInstallChannel();
        strategy.setAppChannel(referChannel == null? channel : referChannel);
        CrashReport.initCrashReport(context, "12a06457f1", !AppConstants.IS_RELEASE_VERSION, strategy);
        // close auto report, manual control
        MLogs.e("bugly channel: " + channel + " referrer: "+ referChannel);
        CrashReport.closeCrashReport();
    }
}
