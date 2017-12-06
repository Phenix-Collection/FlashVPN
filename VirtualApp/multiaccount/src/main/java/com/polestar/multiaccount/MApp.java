package com.polestar.multiaccount;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.helper.utils.VLog;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdUtils;
import com.polestar.billing.BillingProvider;
import com.polestar.multiaccount.component.AppLockMonitor;
import com.polestar.multiaccount.component.LocalActivityLifecycleCallBacks;
import com.polestar.multiaccount.component.MComponentDelegate;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.EventReporter;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nativesdk.ad.common.AdSdk;
import nativesdk.ad.common.app.Constants;


public class MApp extends Application {

    private static MApp gDefault;

    public static MApp getApp() {
        return gDefault;
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
            VASettings.ENABLE_GMS = PreferencesUtils.isGMSEnable();
            Log.d(MLogs.DEFAULT_TAG, "GMS state: " + VASettings.ENABLE_GMS);
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        VirtualCore virtualCore = VirtualCore.get();
        virtualCore.initialize(new VirtualCore.VirtualInitializer() {

            @Override
            public void onMainProcess() {
                MLogs.d("Main process create");

                FirebaseApp.initializeApp(gDefault);
                RemoteConfig.init();
                initRawData();
                MobileAds.initialize(gDefault, "ca-app-pub-5490912237269284~8477604259");
                registerActivityLifecycleCallbacks(new LocalActivityLifecycleCallBacks(MApp.this, true));
                EventReporter.init(gDefault);
                BillingProvider.get();
                String conf = RemoteConfig.getString(AppConstants.CONF_WALL_SDK);
                boolean av = "all".equals(conf) || "avz".equals(conf);
                if (av) {
                    AdSdk.initialize(gDefault, AppConstants.AV_APP_ID, null );
                }
               //
            }

            @Override
            public void onVirtualProcess() {
                MLogs.d("Virtual process create");
                MComponentDelegate delegate = new MComponentDelegate();
                delegate.init();
                virtualCore.setComponentDelegate(delegate);
                virtualCore.setPhoneInfoDelegate(new MyPhoneInfoDelegate());

                virtualCore.setAppApiDelegate(new AppApiDelegate());
            }

            @Override
            public void onServerProcess() {
                MLogs.d("Server process create");
                VirtualCore.get().setAppRequestListener(new VirtualCore.AppRequestListener() {
                    @Override
                    public void onRequestInstall(String path) {
                        //We can start AppInstallActivity TODO
                        Toast.makeText(MApp.this, "Installing: " + path, Toast.LENGTH_SHORT).show();
                        InstallResult res = VirtualCore.get().installPackage("", path, InstallStrategy.UPDATE_IF_EXIST);
                        if (res.isSuccess) {
                            try {
                                VirtualCore.get().preOpt(res.packageName);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (res.isUpdate) {
                                Toast.makeText(MApp.this, "Update: " + res.packageName + " success!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MApp.this, "Install: " + res.packageName + " success!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MApp.this, "Install failed: " + res.error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onRequestUninstall(String pkg) {
                        Toast.makeText(MApp.this, "Uninstall: " + pkg, Toast.LENGTH_SHORT).show();

                    }
                });
                FirebaseApp.initializeApp(gDefault);
                RemoteConfig.init();
                MLogs.d("Server process app onCreate 0");
                MComponentDelegate delegate = new MComponentDelegate();
                delegate.init();
                MLogs.d("Server process app onCreate 1");
                VirtualCore.get().setComponentDelegate(delegate);
                MLogs.d("Server process app onCreate 2");
//                virtualCore.addVisibleOutsidePackage("com.tencent.mobileqq");
//                virtualCore.addVisibleOutsidePackage("com.tencent.mobileqqi");
//                virtualCore.addVisibleOutsidePackage("com.tencent.minihd.qq");
//                virtualCore.addVisibleOutsidePackage("com.tencent.qqlite");
//                virtualCore.addVisibleOutsidePackage("com.facebook.katana");
//                virtualCore.addVisibleOutsidePackage("com.whatsapp");
//                virtualCore.addVisibleOutsidePackage("com.tencent.mm");
//                virtualCore.addVisibleOutsidePackage("com.immomo.momo");
                MLogs.d("Server process app onCreate done");
                MobileAds.initialize(gDefault, "ca-app-pub-5490912237269284~8477604259");
                String conf = RemoteConfig.getString(AppConstants.CONF_WALL_SDK);
                boolean av = "all".equals(conf) || "avz".equals(conf);
                if (av) {
                    AdSdk.initialize(gDefault, AppConstants.AV_APP_ID, null);
                }
                AppLockMonitor.getInstance();
            }
        });

        try {
            // init exception handler and bugly before attatchBaseContext and appOnCreate
            setDefaultUncaughtExceptionHandler(this);
            initBugly(gDefault);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (isOpenLog() || !AppConstants.IS_RELEASE_VERSION ) {
            VLog.openLog();
            VLog.d(MLogs.DEFAULT_TAG, "VLOG is opened");
            MLogs.DEBUG = true;
            AdConstants.DEBUG = true;
        }
        VLog.setKeyLogger(new VLog.IKeyLogger() {
            @Override
            public void keyLog(Context context, String tag, String log) {
                MLogs.logBug(tag,log);
                EventReporter.keyLog(MApp.gDefault, tag, log);
            }

            @Override
            public void logBug(String tag, String log) {
                MLogs.logBug(tag, log);
            }
        });
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                String localFilePath = getApplicationContext().getFilesDir().toString();
                String path = localFilePath + "/" + AppConstants.POPULAR_FILE_NAME;
                copyRawDataToLocal(path, R.raw.popular_apps);
            }
        }).start();
    }

    private void copyRawDataToLocal(String filePath, int resourceId) {
        try {
            File file = new File(filePath);
            // already copied
            if (file.exists()) {
                return;
            } else {
                if (file.createNewFile()) {
                    InputStream in = getResources().openRawResource(resourceId);
                    OutputStream out = new FileOutputStream(file);
                    byte[] buff = new byte[4096];
                    int count = 0;
                    while ((count = in.read(buff)) > 0) {
                        out.write(buff, 0, count);
                    }
                    out.close();
                    in.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        CrashReport.initCrashReport(context, "900060178", !AppConstants.IS_RELEASE_VERSION, strategy);
        // close auto report, manual control
        MLogs.e("bugly channel: " + channel + " referrer: "+ referChannel);
        CrashReport.closeCrashReport();
    }

    class MyPhoneInfoDelegate implements PhoneInfoDelegate {

        @Override
        public String getDeviceId(String oldDeviceId, int userId) {
            return oldDeviceId;
        }

        @Override
        public String getBluetoothAddress(String oldAddress, int userId) {
            return oldAddress;
        }

        @Override
        public String getMacAddress(String oldMacAddress, int userId) {
            if (oldMacAddress == null || oldMacAddress.startsWith("00-00-00-00-00-00") ){
                    return "00:00:08:76:54:32";
            }
            return oldMacAddress;
        }

    }
}

