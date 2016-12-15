package com.polestar.multiaccount.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

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
    private List<AppModel> mClonedModels;
    private Context mContext;

    public static synchronized AppListUtils getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AppListUtils(context);
            DbManager.registerObserver(sInstance);
        }
        return sInstance;
    }

    public AppListUtils(Context context) {
        mContext = context;
        update();
    }

    private void update() {
        Logs.e("update app list");
        synchronized (this) {
            mClonedModels = DbManager.queryAppList(mContext);
            getPopularApps(mPopularModels);
            getIntalledApps(mInstalledModels);
        }
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
                if (isAlreadyClonedApp(packageInfo.packageName)) {
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
            //Skip system app
            if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM )!=0) {
                continue;
            }
            if (isAlreadyClonedApp(pkgName)) {
                continue;
            }
            if (isPopularApp(pkgName)) {
                continue;
            }

            //Todo:workground re-check here: [Bug] if app install failed, then kill recent appclone process,
            //and restart PB, will see two same apps, need core level root cause.
            if (isAlreadyContains(list, pkgName)) {
                Logs.e("same app:" + pkgName);
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
            Logs.e(pName + " is not installed.");
            return false;
        }
    }

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
