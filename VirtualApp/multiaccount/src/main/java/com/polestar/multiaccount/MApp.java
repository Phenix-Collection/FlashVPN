package com.polestar.multiaccount;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate;
import com.lody.virtual.client.stub.StubManifest;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.helper.utils.VLog;
import com.polestar.ad.AdConstants;
import com.polestar.multiaccount.component.LocalActivityLifecycleCallBacks;
import com.polestar.multiaccount.component.MComponentDelegate;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.ImageLoaderUtil;
import com.polestar.multiaccount.utils.LocalExceptionCollectUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.MTAManager;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MApp extends Application {

    private static final String[] GMS_PKG = {
            "com.android.vending",

            "com.google.android.gsf",
            "com.google.android.gsf.login",
            "com.google.android.gms",
            "com.google.android.play.games",
            "com.google.android.backuptransport",
            "com.google.android.backup",
            "com.google.android.configupdater",
            "com.google.android.syncadapters.contacts",
            "com.google.android.feedback",
            "com.google.android.onetimeinitializer",
            "com.google.android.partnersetup",
            "com.google.android.setupwizard",
            "com.google.android.syncadapters.calendar",};

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
        try {
            StubManifest.ENABLE_IO_REDIRECT = true;
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        gDefault = this;
        super.onCreate();
        if (VirtualCore.get().isServerProcess()) {
            VirtualCore.get().setAppRequestListener(new VirtualCore.AppRequestListener() {
                @Override
                public void onRequestInstall(String path) {
                    //We can start AppInstallActivity TODO
                    Toast.makeText(MApp.this, "Installing: " + path, Toast.LENGTH_SHORT).show();
                    InstallResult res = VirtualCore.get().installApp(path, InstallStrategy.UPDATE_IF_EXIST);
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
        } else if (VirtualCore.get().isMainProcess()) {
//            Once.initialise(this);
            installGms();

        } else if (VirtualCore.get().isVAppProcess()) {
            VirtualCore.get().setComponentDelegate(new MComponentDelegate());
            VirtualCore.get().setPhoneInfoDelegate(new MyPhoneInfoDelegate());
        }

        try {
            // init exception handler and bugly before attatchBaseContext and appOnCreate
            setDefaultUncaughtExceptionHandler(this);
            initBugly(this);

            if (VirtualCore.get().isMainProcess()) {
                MTAManager.init(this);
                ImageLoaderUtil.init(this);
                initRawData();
                registerActivityLifecycleCallbacks(new LocalActivityLifecycleCallBacks(MApp.this, true));
//                FirebaseApp.initializeApp(this);
                RemoteConfig.init();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        VirtualCore.get().setAppApiDelegate(new AppApiDelegate());
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
            }

            @Override
            public void logBug(String tag, String log) {
                MLogs.logBug(tag, log);
            }
        });
    }

    /**
     * Install the Google mobile service.
     */
    private void installGms() {
        VirtualCore virtualCore = VirtualCore.get();
        PackageManager pm = virtualCore.getUnHookPackageManager();
        for (String pkg : GMS_PKG) {
            if (virtualCore.isAppInstalled(pkg)) {
                continue;
            }
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                String apkPath = appInfo.sourceDir;
                InstallResult res = VirtualCore.get().installApp(apkPath,
                        InstallStrategy.DEPEND_SYSTEM_IF_EXIST | InstallStrategy.TERMINATE_IF_EXIST);
                if (!res.isSuccess) {
                    VLog.e(getClass().getSimpleName(), "Warning: Unable to install app %s: %s.", appInfo.packageName, res.error);
                }
            } catch (Throwable e) {
                // Ignore
            }
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
            MLogs.e(ex);
            CrashReport.startCrashReport();
            Context innerContext = VClientImpl.get().getCurrentApplication();
            //1. innerContext = null, internal error in Pb
            if (innerContext == null) {
                MLogs.logBug("MApp internal exception, exit.");
                CrashReport.setUserSceneTag(context, AppConstants.CrashTag.MAPP_CRASH);
                CrashReport.postCatchedException(ex);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                LocalExceptionCollectUtils.saveExceptionToLocalFile(context, ex);

                //2. innerContext != null, error in third App, bugly and MTA will report.
                MLogs.e("cur process id:" + android.os.Process.myPid());
                MLogs.e("cur thread name:" + Thread.currentThread().getName());
                ActivityManager.RunningAppProcessInfo info = CommonUtils.getForegroundProcess(context);
                if (info != null) {
                    MLogs.e("foreground process: " + info.pid);
                    MLogs.e("foreground process: " + info.processName);
                }

                //2.1 crash and app exit

                if (info != null && android.os.Process.myPid() == info.pid) {
                    // Toast
                    Intent crash = new Intent("appclone.intent.action.SHOW_CRASH_DIALOG");
                    MLogs.logBug("inner packagename: " + innerContext.getPackageName());
                    crash.putExtra("package", innerContext.getPackageName());
                    crash.putExtra("exception", ex);
                    sendBroadcast(crash);
                } else {
                    //2.2 crash but app not exit
                    MLogs.logBug("report crash, but app not exit.");
                    CrashReport.postCatchedException(ex);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
//            if (orig != null) {
//                orig.uncaughtException(thread, ex);
//            } else {
//
//            }
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
        MLogs.e("bugly channel: " + channel);
        MLogs.e("versioncode: " + CommonUtils.getCurrentVersionCode(context) + ", versionName:" + CommonUtils.getCurrentVersionName(context));
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setAppChannel(channel);
        CrashReport.initCrashReport(context, "900060178", !AppConstants.IS_RELEASE_VERSION, strategy);
        // close auto report, manual control
        CrashReport.closeCrashReport();
    }

    class MyPhoneInfoDelegate implements PhoneInfoDelegate {

        @Override
        public String getDeviceId(String oldDeviceId) {
            return oldDeviceId;
        }

        @Override
        public String getBluetoothAddress(String oldAddress) {
            return oldAddress;
        }

    }
}

