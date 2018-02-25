package com.polestar.multiaccount.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Process;
import android.text.TextUtils;

import com.lody.virtual.GmsSupport;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.polestar.grey.GreyAttribute;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.pbinterface.DataObserver;
import com.polestar.multiaccount.pbinterface.PbObservable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * Created by hxx on 9/7/16.
 */
public class AppListUtils implements DataObserver {
    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);
    private static AppListUtils sInstance;
    private List<AppModel> mPopularModels = new ArrayList<>();
    private List<AppModel> mInstalledModels = new ArrayList<>();
    private List<AppModel> mRecommandModels = new ArrayList<>();
    private List<AppModel> mClonedModels;
    private Context mContext;
    private static HashSet<String> blackList = new HashSet<>();

    public static synchronized AppListUtils getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AppListUtils(context);
            DbManager.registerObserver(sInstance);
        }
        return sInstance;
    }

    public AppListUtils(Context context) {
        mContext = context;
        new Thread(new Runnable() {
            @Override
            public void run() {
                update();
            }
        }).start();
    }

    private void update() {
        MLogs.e("update app list");
        //TO START server process
        ServiceManagerNative.getService(ServiceManagerNative.APP);
        synchronized (this) {
            blackList.add("com.google.android.music");
            blackList.add("com.google.android.dialer");
            String conf = RemoteConfig.getString("black_list");
            if (!TextUtils.isEmpty(conf)) {
                String[] arr = conf.split(";");
                for (String s: arr) {
                    blackList.add(s);
                }
            }
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            List<ResolveInfo> list  = mContext.getPackageManager().queryIntentActivities(home, 0);
            for (ResolveInfo ri: list) {
                blackList.add(ri.activityInfo.applicationInfo.packageName);
                MLogs.logBug("Add black: " + ri.activityInfo.applicationInfo.packageName);
            }
            Intent input = new Intent("android.view.InputMethod");
            list = mContext.getPackageManager().queryIntentActivities(input, 0);
            for (ResolveInfo ri: list) {
                blackList.add(ri.serviceInfo.applicationInfo.packageName);
                MLogs.logBug("Add black: " + ri.serviceInfo.applicationInfo.packageName);
            }
            mClonedModels = DbManager.queryAppList(mContext);
            getPopularApps(mPopularModels);
            getIntalledApps(mInstalledModels);
            loadRecommandAppsFromFile(mRecommandModels);
            loadRecommandAppsFromAds();
        }
        MLogs.e("update app list done");
    }

    private void loadRecommandAppsFromAds() {
        ArrayList<String> availPkgs = new ArrayList<>();
        for (AppModel model: mInstalledModels) {
            if (!VirtualCore.get().isAppInstalled(model.getPackageName())) {
                availPkgs.add(model.getPackageName());
            }
        }
        GreyAttribute.getAdPackages(mContext, new GreyAttribute.IAdPackageLoadCallback() {
            @Override
            public void onAdPackageListReady(List<String> packages, List<String> des) {
                mRecommandModels.clear();
                for (int i = 0; i < packages.size(); i++){
                    PackageInfo packageInfo = null;
                    PackageManager pm = mContext.getPackageManager();
                    try {
                        packageInfo = pm.getPackageInfo(packages.get(i), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (packageInfo != null) {
                        AppModel model = new AppModel(mContext, packageInfo);
                        model.setDescription(des.get(i));
                        mRecommandModels.add(model);
                    }
                }
                writeRecommandAppsToFile(mRecommandModels);
            }
        }, availPkgs);
    }

    public List<AppModel> getRecommandModels() {
        return mRecommandModels;
    }
    public List<AppModel> getPopularModels() {
        synchronized (this) {
            return mPopularModels;
        }
    }

    public List<AppModel> getInstalledModels() {
        synchronized (this) {
            return mInstalledModels;
        }
    }

    private void writeRecommandAppsToFile(List<AppModel> list) {
        if (list == null) {
            return;
        }
        try{
            JSONArray jarr = new JSONArray();
            for(AppModel model: list) {
                JSONObject jobj = new JSONObject();
                jobj.put("package_name",model.getPackageName());
                jobj.put("description", model.getDescription());
                jarr.put(jobj);
            }
            String localFilePath = mContext.getApplicationContext().getFilesDir().toString();
            String path = localFilePath + "/" + AppConstants.RECOMMAND_FILE_NAME;
            FileUtils.writeToFile(path, jarr.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadRecommandAppsFromFile(List<AppModel> list) {
        if (list == null) {
            return;
        } else {
            list.clear();
        }
        String str = FileUtils.readFromFile(mContext.getFilesDir().toString() + "/" + AppConstants.RECOMMAND_FILE_NAME);
        if (str == null) {
            return;
        }
        try {
            JSONArray jarr = new JSONArray(str);
            int length = jarr.length();
            for (int i = 0; i < length; i++) {
                JSONObject jobj = (JSONObject) jarr.get(i);
                String pName = jobj.getString("package_name");
                if (!isAppInstalled(pName)) {
                    continue;
                }
                String description = jobj.getString("description");
                PackageInfo packageInfo = null;
                try {
                    packageInfo = mContext.getPackageManager().getPackageInfo(pName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
                if (!AppManager.isAllowedToClone(packageInfo.packageName)) {
                    continue;
                }

                if (VirtualCore.get().isAppInstalled(packageInfo.packageName)) {
                    continue;
                }

                AppModel model = new AppModel(mContext, packageInfo);
                model.setDescription(description);
                list.add(model);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void getPopularApps(List<AppModel> list) {
        if (list == null) {
            return;
        } else {
            list.clear();
        }
        String str = FileUtils.readFromFile(mContext.getFilesDir().toString() + "/" + AppConstants.POPULAR_FILE_NAME);
        if (str == null) {
            return;
        }
        try {
            JSONArray jarr = new JSONArray(str);
            int length = jarr.length();
            for (int i = 0; i < length; i++) {
                JSONObject jobj = (JSONObject) jarr.get(i);
                String pName = jobj.getString("package_name");
                if (!isAppInstalled(pName)) {
                    continue;
                }
                String description = jobj.getString("description");
                PackageInfo packageInfo = null;
                try {
                    packageInfo = mContext.getPackageManager().getPackageInfo(pName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
                if (!AppManager.isAllowedToClone(packageInfo.packageName)) {
                    continue;
                }

                AppModel model = new AppModel(mContext, packageInfo);
                model.setDescription(description);
                list.add(model);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            if ((!RemoteConfig.getBoolean("allow_system_app")
                     && (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0)) {
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

            if ("com.polestar.domultiple".equals(ai.packageName)) {
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    // NOT include host APP itself, already cloned APP in core and popular APP.
    public void getIntalledApps(List<AppModel> list) {
        if (list == null) {
            return;
        } else {
            list.clear();
        }
        PackageManager pm = mContext.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        String hostPkg = mContext.getPackageName();

        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.packageName;
            if (hostPkg.equals(pkgName)) {
                continue;
            }
            if (isPreInstalledPkg(pkgName)) {
                continue;
            }

            if (!isClonable(pkgName)) {
                continue;
            }
            //Skip system app
//            if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM )!=0) {
//                continue;
//            }
            if (!AppManager.isAllowedToClone(pkgName)) {
                continue;
            }
            if (isPopularApp(pkgName)) {
                continue;
            }


            //Todo:workground re-check here: [Bug] if app install failed, then kill recent appclone process,
            //and restart PB, will see two same apps, need core level root cause.
            if (isAlreadyContains(list, pkgName)) {
                MLogs.e("same app:" + pkgName);
                continue;
            }

            PackageInfo packageInfo = null;
            try {
                packageInfo = pm.getPackageInfo(pkgName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (packageInfo != null) {
                AppModel model = new AppModel(mContext, packageInfo);
                list.add(model);
            }
        }

        Collections.sort(list, (lhs, rhs) -> COLLATOR.compare(lhs.getName(), rhs.getName()));
    }

    private boolean isAppInstalled(String pName) {
        try {
            return mContext.getPackageManager().getPackageInfo(pName, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            MLogs.e(pName + " is not installed.");
            return false;
        }
    }

    @Deprecated
    private boolean isAlreadyClonedApp(String packageName) {
        for (AppModel model : mClonedModels) {
            if (model.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPopularApp(String packageName) {
        for (AppModel model : mPopularModels) {
            if (model.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPreInstalledPkg(String packageName) {
        String[] pkgs = AppManager.getPreInstalledPkgs();
        int length = pkgs.length;
        for (int i = 0; i < length; i++) {
            if (pkgs[i].equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAlreadyContains(List<AppModel> list, String packageName) {
        if (list == null || packageName == null) {
            return false;
        }
        for (AppModel model : list) {
            if (model.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onChanged() {
        update();
        notifyChanged();
    }

    @Override
    public void onInvalidated() {
        update();
        notifyInvalidated();
    }

    /*
        AppListUtils watch DB's state, and also monitored by others(AppListActivity).
     */
    private PbObservable sPbObservable = new PbObservable();

    public void registerObserver(DataObserver observer) {
        sInstance.sPbObservable.registerObserver(observer);
    }

    public void unregisterObserver(DataObserver observer) {
        sInstance.sPbObservable.unregisterObserver(observer);
    }

    private void notifyChanged() {
        sPbObservable.notifyChanged();
    }

    private void notifyInvalidated() {
        sPbObservable.notifyInvalidated();
    }

}
