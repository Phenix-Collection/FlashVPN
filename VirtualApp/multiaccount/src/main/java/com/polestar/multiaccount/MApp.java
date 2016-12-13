package com.polestar.multiaccount;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.InstallResult;
import com.lody.virtual.helper.utils.VLog;
import com.polestar.multiaccount.component.LocalActivityLifecycleCallBacks;
import com.polestar.multiaccount.constant.Constants;
import com.polestar.multiaccount.utils.EventReportManager;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.ImageLoaderUtil;
import com.polestar.multiaccount.utils.LocalExceptionCollectUtils;
import com.polestar.multiaccount.utils.Logs;
import com.polestar.multiaccount.utils.MTAManager;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class MApp extends Application {

    private static MApp mInstance;

    public static MApp getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        mInstance = this;
        super.onCreate();
        //AppManager.appOnCreate(this);
        Logs.d("E MTAManager.init(this);");
        MTAManager.init(this);
        initEventReport(this);
        Logs.d("X MTAManager.init(this);");

        if (AppManager.isMainProcess()) {
            preInstallPkg();
            ImageLoaderUtil.init(this);
            initRawData();
            registerActivityLifecycleCallbacks(new LocalActivityLifecycleCallBacks(MApp.this,true));
        }else{
//            AppManager.setInitCallback(new IinitCallback() {
//                @Override
//                public void afterApplicationCreate(Application application) {
//                    Logs.e("afterApplicationCreate");
//                    if(application != null){
//                        Logs.e("registerActivityLifecycleCallbacks on other App");
//                        application.registerActivityLifecycleCallbacks(new LocalActivityLifecycleCallBacks(MApp.this,false));
//                    }
//                }
//            });
        }
    }

    private void preInstallPkg() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                VirtualCore virtualCore = VirtualCore.get();
                Logs.d("E preInstallPkg");
                PackageManager pm = virtualCore.getUnHookPackageManager();
                final String[] list = AppManager.getPreInstalledPkgs();
                for (String pkg : list) {
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
        }).start();
        Logs.d("X preInstallPkg");
    }

    private void setDefaultUncaughtExceptionHandler(Context context) {
        //MTA
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Logs.e("uncaughtException");
                Logs.e(ex.toString());
                CrashReport.startCrashReport();
                Context innerContext = AppManager.getInnerContext();

                //1. innerContext = null, internal error in Pb
                if (innerContext == null) {
                    Logs.e("Pb internal exception, exit.");
                    CrashReport.postCatchedException(ex);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    LocalExceptionCollectUtils.saveExceptionToLocalFile(context, ex);

                    //2. innerContext != null, error in third App, bugly and MTA will report.
                    Logs.e("cur process id:" + android.os.Process.myPid());
                    Logs.e("cur process name:" + ActivityThread.currentActivityThread().getProcessName());
                    ActivityManager.RunningAppProcessInfo info = CommonUtils.getForegroundProcess(context);
                    if (info != null) {
                        Logs.e("foreground process: " + info.pid);
                        Logs.e("foreground process: " + info.processName);
                    }

                    //2.1 crash and app exit
                    if (info != null && android.os.Process.myPid() == info.pid) {
                        // Toast
                        Intent crash = new Intent("appclone.intent.action.SHOW_CRASH_DIALOG");
                        Logs.e("inner packagename: " + innerContext.getPackageName());
                        crash.putExtra("package", innerContext.getPackageName());
                        crash.putExtra("exception", ex);
                        sendBroadcast(crash);
                    } else {
                        //2.2 crash but app not exit
                        Logs.e("report crash, but app not exit.");
                        CrashReport.postCatchedException(ex);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Logs.e("process exit...");
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });
    }

    private void initRawData() {
        String localFilePath = getApplicationContext().getFilesDir().toString();
        String path = localFilePath + "/" + Constants.POPULAR_FILE_NAME;
        copyRawDataToLocal(path, R.raw.popular_apps);
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

    @Override
    protected void attachBaseContext(Context base) {
        if (isCurMainProcess()) {
            Logs.e("init autolog");
        }
        super.attachBaseContext(base);

        // init exception handler and bugly before attatchBaseContext and appOnCreate
        setDefaultUncaughtExceptionHandler(this);
        Logs.d("E initBugly");
        initBugly(this);
        Logs.d("X initBugly");

        try {
            AppManager.attatchBaseContext(base);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void initEventReport(Context context) {
        EventReportManager.init(context);
        EventReportManager.reportActive(context);
    }

    private void initBugly(Context context) {
        //Bugly
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        Constants.IS_RELEASE_VERSION = !channel.equals(Constants.DEVELOP_CHANNEL);
        Logs.e("IS_RELEASE_VERSION: " + Constants.IS_RELEASE_VERSION);
        Logs.e("bugly channel: " + channel);
        Logs.e("versioncode: " + CommonUtils.getCurrentVersionCode(context) + ", versionName:" + CommonUtils.getCurrentVersionName(context));
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setAppChannel(channel);
        CrashReport.initCrashReport(context, "900060178", !Constants.IS_RELEASE_VERSION, strategy);
        // close auto report, manual control
        CrashReport.closeCrashReport();
    }

    // Main process: PB
    private boolean isCurMainProcess() {
        return ActivityThread.currentActivityThread().getProcessName().equals("com.polestar.multiaccount");
    }
}
