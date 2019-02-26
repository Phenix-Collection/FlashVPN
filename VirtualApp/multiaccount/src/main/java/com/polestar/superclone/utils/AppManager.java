package com.polestar.superclone.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.polestar.clone.CloneAgent64;
import com.polestar.clone.client.core.InstallStrategy;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.ipc.VActivityManager;
import com.polestar.clone.client.ipc.VPackageManager;
import com.polestar.clone.os.VUserInfo;
import com.polestar.clone.os.VUserManager;
import com.polestar.clone.remote.InstallResult;
import com.polestar.clone.os.VUserHandle;
import com.polestar.clone.remote.InstalledAppInfo;
import com.polestar.clone.BitmapUtils;
import com.polestar.superclone.MApp;
import com.polestar.superclone.constant.AppConstants;
import com.polestar.superclone.db.DbManager;
import com.polestar.superclone.model.AppModel;
import com.polestar.clone.CustomizeAppData;
import com.polestar.superclone.model.PackageConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppManager {
    private final static String TAG = "AppManager";
    private static AppManager instance;

    private static HashMap<String, Long> mPackageLaunchTime = new HashMap<>();
    private static HashMap<String, PackageConfig> mPackageConfigMap = new HashMap<>();
    private static HashMap<String, Integer> mPackageIndexMap = new HashMap<>();

    private static String PACKAGE_CONFIG_VERSION_KEY = "pkg_config_version";
    private static String PACKAGE_CONFIG_KEY = "pkg_config";
    private static String PACKAGE_DEFAULT_ALLOWED_COUNT_KEY = "pkg_default_allow_cnt";

    private static String PACKAGE_INDEX_PREFERENCE = "package_index";

    public static List<AppModel> getClonedApp(Context context) {
        List<AppModel> appList = DbManager.queryAppList(context);
        List<AppModel> uninstalledApp = new ArrayList<>();
        for (AppModel model : appList) {
            if (!CommonUtils.isAppInstalled(context, model.getPackageName())) {
                uninstalledApp.add(model);
                continue;
            }
            try {
                if (!VirtualCore.get().isAppInstalledAsUser(model.getPkgUserId(), model.getPackageName())) {
                    uninstalledApp.add(model);
                    continue;
                }
            }catch (Exception e) {
                MLogs.logBug(MLogs.getStackTraceString(e));
            }
            if (model.getCustomIcon() == null) {
                model.setCustomIcon(BitmapUtils.getCustomIcon(context, model.getPackageName(), model.getPkgUserId()));
            }
        }
        deleteApp(context, uninstalledApp);
        appList.removeAll(uninstalledApp);
        return appList;
    }

    public synchronized static void deleteApp(Context context, List<AppModel> uninstalledApp) {
        if (uninstalledApp != null && uninstalledApp.size() > 0) {
            for (AppModel model : uninstalledApp) {
                CommonUtils.removeShortCut(context, model);
                CustomizeAppData.removePerf(model.getPackageName(), model.getPkgUserId());
                uninstallApp(model.getPackageName(), model.getPkgUserId());
            }
            DbManager.deleteAppModeList(context, uninstalledApp);
            DbManager.notifyChanged();
        }
    }

    public synchronized static void deleteApp(Context context, AppModel model) {
        List<AppModel> list = new ArrayList<>();
        list.add(model);
        deleteApp(context, list);
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

    public static void launchApp(String packageName, int userId) {
        //Check app version and trying to upgrade if necessary
        try {
            MLogs.d(TAG, "launchApp packageName = " + packageName);
            mPackageLaunchTime.put(getMapKey(packageName, userId), System.currentTimeMillis());
            Intent intent = VirtualCore.get().getLaunchIntent(packageName, userId);
            VActivityManager.get().startActivity(intent, userId);
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

    public static String[] getPreInstalledPkgs() {
        return GMS_PKG;
    }

    @Deprecated
    public static boolean isAppInstalled(String packageName) {
        return VirtualCore.get().isAppInstalled(packageName);
    }

    public static boolean isAppInstalled(String packageName, int userId) {
        return VirtualCore.get().isAppInstalledAsUser(userId, packageName);
    }

    @Deprecated
    public static boolean installApp(Context context, AppModel appModel) {
        MLogs.d(TAG, "apkPath = " + appModel.getApkPath());
        InstallResult result = VirtualCore.get().installPackage(appModel.getPackageName(), appModel.getApkPath(),
                InstallStrategy.COMPARE_VERSION | InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
        if (result.isSuccess && PreferencesUtils.getBoolean(context, AppConstants.KEY_AUTO_CREATE_SHORTCUT, false)) {
            CommonUtils.createShortCut(context, appModel);
        }
        return result.isSuccess;
    }

    public static boolean installApp(Context context, AppModel appModel, int userId) {
        MLogs.d(TAG, "apkPath = " + appModel.getApkPath());
        InstalledAppInfo info = VirtualCore.get().getInstalledAppInfo(appModel.getPackageName(), 0);
        boolean success;
        if (info != null) {
            if (VUserManager.get().getUserInfo(userId) == null) {
                // user not exist, create it automatically.
                String nextUserName = "User " + (userId + 1);
                VUserInfo newUserInfo = VUserManager.get().createUser(nextUserName, VUserInfo.FLAG_ADMIN);
                if (newUserInfo == null) {
                    MLogs.e(TAG, "create user failure");
                    return false;
                }
            }
            success = VirtualCore.get().installPackageAsUser(userId, appModel.getPackageName());
        } else {
            InstallResult result = VirtualCore.get().installPackage(appModel.getPackageName(), appModel.getApkPath(),
                    InstallStrategy.COMPARE_VERSION | InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
            success = result.isSuccess;
        }

        if (success) {
            appModel.setClonedTime(System.currentTimeMillis());
            appModel.formatIndex(CloneHelper.getInstance(MApp.getApp()).getClonedApps().size(), userId);
        }

        if (success && PreferencesUtils.getBoolean(context, AppConstants.KEY_AUTO_CREATE_SHORTCUT, false)) {
            CommonUtils.createShortCut(context, appModel);
        }
        return success;
    }

    public static boolean uninstallApp(String packageName, int userId) {
        MLogs.d(TAG, "uninstall packageName = " + packageName + " userId " + userId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                CloneAgent64 agent64 = new CloneAgent64(MApp.getApp());
                if(agent64.hasSupport() && agent64.isCloned(packageName, userId)) {
                    agent64.deleteClone(packageName, userId);
                }
            }
        }).start();
        return VirtualCore.get().uninstallPackageAsUser(packageName, userId);
    }
//    public static Collection<ProcessRecord> getProcessList(){
//        return VirtualCore.get().getProcessList();
//    }

    public static void killApp(String packageName) {
        MLogs.d(TAG, "packageName = " + packageName);
        VirtualCore.get().killApp(packageName, VUserHandle.USER_ALL);
    }

    public static void reloadLockerSetting() {
        String key = PreferencesUtils.getEncodedPatternPassword(MApp.getApp());
        boolean adFree = PreferencesUtils.isAdFree();
        long interval = PreferencesUtils.getLockInterval();
       // VirtualCore.get().reloadLockerSetting(key, adFree, interval);
    }

    public static boolean isAppLaunched(String pkg, int userId) {
        String key = getMapKey(pkg, userId);
        long time = mPackageLaunchTime.get(key) == null ? 0:  mPackageLaunchTime.get(key);
        boolean hasSupportLib= MApp.isSupportPkgExist();
        return (VirtualCore.get().isAppRunning(pkg, userId)
                || (hasSupportLib && new CloneAgent64(MApp.getApp()).isAppRunning(pkg, userId)))
                && ((System.currentTimeMillis()-time) < 60*60*1000);
    }

    public static void updateLaunchTime(String pkg, int userId) {
        mPackageLaunchTime.put(getMapKey(pkg, userId), System.currentTimeMillis());
    }

    public static boolean isAppLaunched(AppModel model) {
        return isAppLaunched(model.getPackageName(), model.getPkgUserId());
    }
    /*
     * the configure format:
     * pkg1:cnt1;pkg2:cnt2;pkg3:cnt3
     */
    public static List<PackageConfig> getPackageConfig() {
        long version = RemoteConfig.getLong(PACKAGE_CONFIG_VERSION_KEY);
        List<PackageConfig> list = new ArrayList<>();
        String cfg = RemoteConfig.getString(PACKAGE_CONFIG_KEY);
        if (!TextUtils.isEmpty(cfg)) {
            String[] cfgs = cfg.split(";");
            for (String c : cfgs) {
                String[] s = c.split(":");
                if (s.length != 2)
                    continue;
                PackageConfig pc = new PackageConfig();
                pc.packageName = s[0];
                pc.allowedCloneCount = Integer.parseInt(s[1]);
                list.add(pc);
            }
        } else {
            list.add(new PackageConfig("com.whatsapp", 5));
            list.add(new PackageConfig("com.facebook.katana", 5));
        }

        return list;
    }

    public static int getDefaultAllowedCloneCount() {
        long count = RemoteConfig.getLong(PACKAGE_DEFAULT_ALLOWED_COUNT_KEY);
        return (count == 0) ? AppConstants.DEFAULT_ALLOWED_CLONE_COUNT : (int)count;
    }

    public static void buildPackageConfigMap() {
        List<PackageConfig> list = getPackageConfig();
        for (PackageConfig cfg : list) {
            mPackageConfigMap.put(cfg.packageName, cfg);
        }
    }

    private static int getAllowedCloneCount(String packageName) {
        if (mPackageConfigMap.containsKey(packageName)) {
            PackageConfig cfg = mPackageConfigMap.get(packageName);
            return cfg.allowedCloneCount;
        }
        return getDefaultAllowedCloneCount();
    }

    public static int getClonedCount(String packageName) {
        InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(packageName, 0);
        if (installedAppInfo != null) {
            int[] userIds = installedAppInfo.getInstalledUsers();
            return userIds != null ? userIds.length : 0;
        }
        return 0;
    }

    public static boolean isAllowedToClone(String packageName) {
        int allowed = getAllowedCloneCount(packageName);
        int cloned = getClonedCount(packageName);
        return allowed > cloned;
    }

    public static int getNextAvailableUserId(String packageName) {
        InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(packageName, 0);
        if (installedAppInfo != null) {
            int[] userIds = installedAppInfo.getInstalledUsers();
            int nextUserId = userIds.length;
        /*
         * Input : userIds = {0, 1, 3}
         * Output: nextUserId = 2
         */
            for (int i = 0; i < userIds.length; i++) {
                if (userIds[i] != i) {
                    nextUserId = i;
                    break;
                }
            }
            return nextUserId;
        }
        return 0;
    }

    // the name for new cloned app
    public static String getDefaultName(String packageName) {
        PackageManager pm = MApp.getApp().getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            CharSequence label = pm.getApplicationLabel(ai);
            if (getAllowedCloneCount(packageName) > 1) {
                int index = getPackageIndex(packageName);
                return label + " " + (index + 1);
            } else {
                return label.toString();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    // for compatible use
    public static String getCompatibleName(String name, int userId) {
        return (userId != 0) ? name + " " + (userId + 1): name + " +" ;
    }

    // the name store in database
    public static String getModelName(String pkg, int userId) {
        AppModel model = CloneHelper.getInstance(MApp.getApp()).getAppModel(pkg, userId);
        if (model != null)
            return model.getName();
        return null;
    }

    //DO NOT SAVE MAP KEY TO PERSISTENT FILE
    public static String getMapKey(String pkg, int userId) {
        return pkg + ":" + userId;
    }

    public static int getUserIdFromKey(String key) {
        if (key != null) {
            String arr[] = key.split(":");
            if (arr.length == 2) {
                return Integer.valueOf(arr[1]);
            }
        }
        return 0;
    }

    public static String getNameFromKey(String key) {
        if (key != null) {
            String arr[] = key.split(":");
            if (arr.length == 2) {
                return arr[0];
            }
        }
        return key;
    }

    public static void buildPackageIndexMap() {
        SharedPreferences settings = MApp.getApp().getSharedPreferences(PACKAGE_INDEX_PREFERENCE, Context.MODE_PRIVATE);
        Map<String, ?> map = settings.getAll();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            mPackageIndexMap.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
        }
    }

    public static int getPackageIndex(String pkg) {
        if (mPackageIndexMap.containsKey(pkg))
            return mPackageIndexMap.get(pkg);
        return 0;
    }

    public static void incPackageIndex(String pkg) {
        int next = 1;
        if (mPackageIndexMap.containsKey(pkg))
            next = mPackageIndexMap.get(pkg) + 1;
        mPackageIndexMap.put(pkg, next);
        SharedPreferences settings = MApp.getApp().getSharedPreferences(PACKAGE_INDEX_PREFERENCE, Context.MODE_PRIVATE);
        settings.edit().putInt(pkg, next).commit();
    }
}
