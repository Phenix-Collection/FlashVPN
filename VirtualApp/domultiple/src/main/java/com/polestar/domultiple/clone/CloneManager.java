package com.polestar.domultiple.clone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;

import com.polestar.clone.CloneAgent64;
import com.polestar.clone.GmsSupport;
import com.polestar.clone.client.core.InstallStrategy;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.client.ipc.VActivityManager;
import com.polestar.clone.client.ipc.VPackageManager;
import com.polestar.clone.os.VUserHandle;
import com.polestar.clone.os.VUserInfo;
import com.polestar.clone.os.VUserManager;
import com.polestar.clone.remote.InstallResult;
import com.polestar.clone.remote.InstalledAppInfo;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.clone.CustomizeAppData;
import com.polestar.domultiple.db.DBManager;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by PolestarApp on 2017/7/16.
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
    private static HashSet<String> blackList = new HashSet<>();
    // key is package name or package name + userId
    private static HashMap<String, Long> mPackageLaunchTime = new HashMap<>();
    private HashMap<String, PackageConfig> mPackageConfigMap = new HashMap<>();
    private HashMap<String, Integer> mPackageIndexMap = new HashMap<>();

    private static String PACKAGE_CONFIG_VERSION_KEY = "pkg_config_version";
    private static String PACKAGE_CONFIG_KEY = "pkg_config";
    private static String PACKAGE_DEFAULT_ALLOWED_COUNT_KEY = "pkg_default_allow_cnt";

    private static String PACKAGE_INDEX_PREFERENCE = "package_index";

    public boolean hasPendingClones() {
        return mPendingClones.size() > 0;
    }

    public void clearPendingClones() {
        mPendingClones.clear();
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

    @Deprecated
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

    public void createClone(Context context, CloneModel appModel, int userId) {
        mPendingClones.add(appModel);
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                try {
                    appModel.setClonedTime(System.currentTimeMillis());
                    appModel.formatIndex(mClonedApps.size(), userId);
                    InstalledAppInfo info = VirtualCore.get().getInstalledAppInfo(appModel.getPackageName(), 0);
                    if (info != null) {
                        if (VUserManager.get().getUserInfo(userId) == null) {
                            // user not exist, create it automatically.
                            String nextUserName = "User " + (userId + 1);
                            VUserInfo newUserInfo = VUserManager.get().createUser(nextUserName, VUserInfo.FLAG_ADMIN);
                            if (newUserInfo == null) {
                                throw new IllegalStateException();
                            }
                        }
                        success = VirtualCore.get().installPackageAsUser(userId, appModel.getPackageName());
                    } else {
                        InstallResult result = VirtualCore.get().installPackage(appModel.getPackageName(), appModel.getApkPath(context),
                                InstallStrategy.COMPARE_VERSION | InstallStrategy.DEPEND_SYSTEM_IF_EXIST);
                        success = result.isSuccess;
                    }

                    if(success) {
                        DBManager.insertCloneModel(context, appModel);
                        mClonedApps.add(appModel);
                        incPackageIndex(appModel.getPackageName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    boolean finalSuccess = success;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mPendingClones.remove(appModel);
                            if (loadedListener != null) {
                                loadedListener.onInstalled(appModel, finalSuccess);
                            }
                        }
                    });
                }
            }
        });
    }

    @Deprecated
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

    public void deleteClone(Context context, CloneModel model) {
        deleteClone(context, model.getPackageName(), model.getPkgUserId());
    }

    public void deleteClone(Context context, String packageName, int userId) {
        mWorkHandler.post(new Runnable() {
            @Override
            public void run() {
                if(!TextUtils.isEmpty(packageName)){
                    CloneModel model = DBManager.queryCloneModelByPackageName(context,packageName, userId);
                    if (model != null) {
                        boolean res = VirtualCore.get().uninstallPackageAsUser(packageName, userId);
                        if (res) {
                            DBManager.deleteCloneModel(context,model);
                            ListIterator<CloneModel> iter = mClonedApps.listIterator();
                            while (iter.hasNext()) {
                                CloneModel cm = iter.next();
                                if (cm.getPackageName().equals(packageName) &&
                                        cm.getPkgUserId() == userId) {
                                    iter.remove();
                                    break;
                                }
                            }
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (loadedListener != null) {
                                    loadedListener.onUnstalled(model, res);
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
            try {
                if (!VirtualCore.get().isAppInstalledAsUser(model.getPkgUserId(), model.getPackageName())) {
                    uninstalledApp.add(model);
                    continue;
                }
            }catch (Exception ex) {
                MLogs.logBug(ex);
            }
            if (model.getCustomIcon() == null) {
                CustomizeAppData data = CustomizeAppData.loadFromPref(model.getPackageName(), model.getPkgUserId());
                Bitmap bmp = data.getCustomIcon();
                //appIcon.setImageBitmap(bmp);
                model.setCustomIcon(bmp);
                //model.setCustomIcon(CommonUtils.createCustomIcon(context, model.getIconDrawable(context)));
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
        blackList.add("com.google.android.music");
        blackList.add("com.google.android.dialer");
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> list  = context.getPackageManager().queryIntentActivities(home, 0);
        for (ResolveInfo ri: list) {
            blackList.add(ri.activityInfo.applicationInfo.packageName);
            MLogs.logBug("Add black: " + ri.activityInfo.applicationInfo.packageName);
        }
        Intent input = new Intent("android.view.InputMethod");
        list = context.getPackageManager().queryIntentActivities(input, 0);
        for (ResolveInfo ri: list) {
            blackList.add(ri.serviceInfo.applicationInfo.packageName);
            MLogs.logBug("Add black: " + ri.serviceInfo.applicationInfo.packageName);
        }
        buildPackageConfigMap();
        buildPackageIndexMap();
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

    @Deprecated
    public static void launchApp(String packageName) {
        //Check app version and trying to upgrade if necessary
        try {
            MLogs.d(TAG, "launchApp packageName = " + packageName);
            mPackageLaunchTime.put(packageName, System.currentTimeMillis());
            Intent intent = VirtualCore.get().getLaunchIntent(packageName, VUserHandle.myUserId());
            VActivityManager.get().startActivity(intent, VUserHandle.myUserId());
        } catch (Exception e) {
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

    public static void launchApp(CloneModel model) {
        launchApp(model.getPackageName(), model.getPkgUserId());
    }

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
            if (ai.uid < Process.FIRST_APPLICATION_UID) {
                return false;
            }
            if (ai.processName.equals("android.process.acore")){
                return false;
            }
            if (blackList.contains(ai.packageName)) {
                return false;
            }
            if (ai.packageName.equals("com.polestar.multiaccount")) {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
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
        //AppLockMonitor.updateSetting(key, adFree, interval);
        //VirtualCore.get().reloadLockerSetting(key, adFree, interval);
    }

    @Deprecated
    public final CloneModel getCloneModel(String packageName) {
        if (packageName == null) {
            return null;
        }
        if (mClonedApps.size() > 0) {
            for (CloneModel model:mClonedApps) {
                if (model.getPackageName().equals(packageName)) {
                    return model;
                }
            }
        } else {
            List<CloneModel> appModels = DBManager.queryCloneModelByPackageName(mContext, packageName);
            if (appModels != null && appModels.size() > 0) {
                return appModels.get(0);
            }
        }
        return null;
    }

    public final CloneModel getCloneModel(String packageName, int userId) {
        if (packageName == null) {
            return null;
        }
        if (mClonedApps.size() > 0) {
            for (CloneModel model:mClonedApps) {
                if (model!= null && model.getPackageName().equals(packageName) &&
                        model.getPkgUserId() == userId) {
                    return model;
                }
            }
        }
        return DBManager.queryCloneModelByPackageName(mContext, packageName, userId);
    }

    public static boolean isAppRunning(String pkg, int userId) {
        return VirtualCore.get().isAppRunning(pkg, userId);
    }

    public static boolean isAppLaunched(String pkg, int userId) {
        String key = getMapKey(pkg, userId);
        long time = mPackageLaunchTime.get(key) == null ? 0:  mPackageLaunchTime.get(key);
        boolean hasSupportLib= PolestarApp.isSupportPkgExist();
        return (VirtualCore.get().isAppRunning(pkg, userId)
                || (hasSupportLib && new CloneAgent64(PolestarApp.getApp()).isAppRunning(pkg, userId)))
                && ((System.currentTimeMillis()-time) < 60*60*1000);
    }

    public static boolean isAppLaunched(CloneModel model) {
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

    private void buildPackageConfigMap() {
        List<PackageConfig> list = getPackageConfig();
        for (PackageConfig cfg : list) {
            mPackageConfigMap.put(cfg.packageName, cfg);
        }
    }

    private int getAllowedCloneCount(String packageName) {
        if (mPackageConfigMap.containsKey(packageName)) {
            PackageConfig cfg = mPackageConfigMap.get(packageName);
            return cfg.allowedCloneCount;
        }
        return getDefaultAllowedCloneCount();
    }

    public int getClonedCount(String packageName) {
        InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(packageName, 0);
        if (installedAppInfo != null) {
            int[] userIds = installedAppInfo.getInstalledUsers();
            return userIds != null ? userIds.length : 0;
        }
        return 0;
    }

    public boolean isAllowedToClone(String packageName) {
        int allowed = getAllowedCloneCount(packageName);
        int cloned = getClonedCount(packageName);
        return allowed > cloned;
    }

    public int getNextAvailableUserId(String packageName) {
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
    public String getDefaultName(String packageName) {
        PackageManager pm = mContext.getPackageManager();
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

    // the name store in database
    public String getModelName(String pkg, int userId) {
        CloneModel model = getCloneModel(pkg, userId);
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

    private void buildPackageIndexMap() {
        SharedPreferences settings = PolestarApp.getApp().getSharedPreferences(PACKAGE_INDEX_PREFERENCE, Context.MODE_PRIVATE);
        Map<String, ?> map = settings.getAll();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            mPackageIndexMap.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
        }
    }

    public int getPackageIndex(String pkg) {
        if (mPackageIndexMap.containsKey(pkg))
            return mPackageIndexMap.get(pkg);
        return 0;
    }

    public void incPackageIndex(String pkg) {
        int next = 1;
        if (mPackageIndexMap.containsKey(pkg))
            next = mPackageIndexMap.get(pkg) + 1;
        mPackageIndexMap.put(pkg, next);
        SharedPreferences settings = PolestarApp.getApp().getSharedPreferences(PACKAGE_INDEX_PREFERENCE, Context.MODE_PRIVATE);
        settings.edit().putInt(pkg, next).commit();
    }
}
