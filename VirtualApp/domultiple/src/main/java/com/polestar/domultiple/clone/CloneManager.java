package com.polestar.domultiple.clone;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.secondary.GmsSupport;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.os.VUserHandle;
import com.lody.virtual.remote.InstallResult;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.DBManager;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.EventReporter;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guojia on 2017/7/16.
 */

public class CloneManager {
    private static final String TAG = "CloneManager";
    private static CloneManager instance;
    private List<CloneModel> mClonedApps;
    private OnClonedAppChangListener loadedListener;
    private boolean isLoading;
    private Context mContext;
    private HandlerThread mWorkThread;
    private Handler mWorkHandler;
    private List<CloneModel> mPendingClones = new ArrayList<>();

    public boolean hasPendingClones() {
        return mPendingClones.size() > 0;
    }

    public interface OnClonedAppChangListener {
        void onInstalled(CloneModel clonedApp, boolean result);

        void onUnstalled(CloneModel clonedApp, boolean result);

        void onLoaded(List<CloneModel> clonedApp);
    }
    private Handler mHandler;

    public static CloneManager getInstance(Context context){
        if(instance == null){
            instance = new CloneManager(context);
        }
        return instance;
    }

    public void loadClonedApps(Context context,OnClonedAppChangListener loadedListener){
        this.loadedListener = loadedListener;
        if (isLoading ) {
            return;
        }
        isLoading = true;
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                loadClonedApp(context);
                isLoading = false;
            }
        });
//        new LoadClonedAppTask(context).execute();
    }

    public void createClone(Context context, CloneModel appModel) {
        mPendingClones.add(appModel);
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    appModel.setClonedTime(System.currentTimeMillis());
                    appModel.setIndex(mClonedApps.size());
                    InstallResult result = VirtualCore.get().installPackage(appModel.getPackageName(), appModel.getApkPath(context),
                            InstallStrategy.COMPARE_VERSION | InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
                    if(result.isSuccess) {
                        DBManager.insertCloneModel(context, appModel);
                        mClonedApps.add(appModel);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPendingClones.remove(appModel);
                            if (loadedListener != null) {
                                loadedListener.onInstalled(appModel, result.isSuccess);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void deleteClone(Context context, String packageName) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if(!TextUtils.isEmpty(packageName)){
                    List<CloneModel> models = DBManager.queryCloneModelByPackageName(context,packageName);
                    if (models != null && models.size() != 0) {
                        boolean res = VirtualCore.get().uninstallPackage(packageName);
                        if (res) {
                            DBManager.deleteAppModeList(context,models);
                            for (int i = 0;i >= 0 && i < mClonedApps.size();i ++){
                                if(mClonedApps.get(i).getPackageName().equals(packageName)){
                                    mClonedApps.remove(i);
                                    i --;
                                }
                            }
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (loadedListener != null) {
                                    loadedListener.onUnstalled(models.get(0), res);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void loadClonedApp(Context context){
        List<CloneModel> tempList = DBManager.queryAppList(context);
        List<CloneModel> uninstalledApp = new ArrayList<>();
        for (CloneModel model : tempList) {
            if (!VirtualCore.get().isAppInstalled(model.getPackageName())) {
                uninstalledApp.add(model);
                continue;
            }
            if (model.getCustomIcon() == null) {
                model.setCustomIcon(CommonUtils.createCustomIcon(context, model.getIconDrawable(context)));
            }
        }
        DBManager.deleteAppModeList(context, uninstalledApp);
        tempList.removeAll(uninstalledApp);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(tempList != null){
                    mClonedApps.clear();
                    mClonedApps.addAll(tempList);
                }
                if(loadedListener != null) {
                    loadedListener.onLoaded(mClonedApps);
                }
            }
        });
    }

    private CloneManager(Context context){
        mClonedApps = new ArrayList<>();
        mHandler = new Handler(Looper.getMainLooper());
        mContext = context;
        mWorkThread = new HandlerThread("clone-worker");
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper());
    }

    public List<CloneModel> getClonedApps(){
        return mClonedApps;
    }

    public static boolean needUpgrade(String packageName) {
        try {
            PackageInfo vinfo = VPackageManager.get().getPackageInfo(packageName, 0 ,0);
            PackageInfo info = VirtualCore.get().getUnHookPackageManager().getPackageInfo(packageName,0);
            if (vinfo == null || info == null) {
                return false;
            }
            return vinfo.versionCode != info.versionCode;
        }catch (Exception e) {
            MLogs.e(e);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return false;
    }

    public static void upgradeApp(String packageName) {
        try {
            PackageInfo info = VirtualCore.get().getUnHookPackageManager().getPackageInfo(packageName, 0);
            InstallResult result = VirtualCore.get().upgradePackage(info.packageName, info.applicationInfo.sourceDir,
                    InstallStrategy.DEPEND_SYSTEM_IF_EXIST | InstallStrategy.UPDATE_IF_EXIST);
            MLogs.logBug("package upgrade result: " + result.toString());
        }catch (Exception e) {
            MLogs.e(e);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void launchApp(String packageName) {
        //Check app version and trying to upgrade if necessary
        try {
            MLogs.d(TAG, "launchApp packageName = " + packageName);
            Intent intent = VirtualCore.get().getLaunchIntent(packageName, VUserHandle.myUserId());
            VActivityManager.get().startActivity(intent, VUserHandle.myUserId());
        } catch (Exception e) {
            MLogs.e(e);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private static final String[] GMS_PKG = {
            "com.android.vending",

            "com.google.android.gsf",
            "com.google.android.gsf.login",
            "com.google.android.gms",

            "com.google.android.backuptransport",
            "com.google.android.backup",
            "com.google.android.configupdater",
            "com.google.android.syncadapters.contacts",
            "com.google.android.feedback",
            "com.google.android.onetimeinitializer",
            "com.google.android.partnersetup",
            "com.google.android.setupwizard",
            "com.google.android.syncadapters.calendar",};

    public boolean isClonable(String pkg) {
        if ( GmsSupport.isGmsFamilyPackage(pkg) ) {
            return false;
        }
        try {
            PackageManager pm = mContext.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
            if (!GmsSupport.hasDexFile(ai.sourceDir)) {
                return false;
            }
            if (ai.sourceDir.contains("/system/priv-app")) {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    public static String[] getPreInstalledPkgs() {
        return GMS_PKG;
    }

    public static boolean isAppInstalled(String packageName) {
        return VirtualCore.get().isAppInstalled(packageName);
    }


    public static void killApp(String packageName) {
        MLogs.d(TAG, "packageName = " + packageName);
        VirtualCore.get().killApp(packageName, VUserHandle.USER_ALL);
    }

    public static void reloadLockerSetting() {
        String key = PreferencesUtils.getEncodedPatternPassword(PolestarApp.getApp());
        boolean adFree = PreferencesUtils.isAdFree();
        long interval = PreferencesUtils.getLockInterval();
        VirtualCore.get().reloadLockerSetting(key, adFree, interval);
    }
}
