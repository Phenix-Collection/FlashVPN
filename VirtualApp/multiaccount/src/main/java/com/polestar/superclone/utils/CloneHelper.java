package com.polestar.superclone.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import com.polestar.superclone.MApp;
import com.polestar.superclone.constant.AppConstants;
import com.polestar.superclone.db.DbManager;
import com.polestar.superclone.model.AppModel;
import com.polestar.superclone.notification.FastSwitch;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by yxx on 2016/8/29.
 */
public class CloneHelper {
    private static CloneHelper instance;
    private List<AppModel> mClonedApps;
    private OnClonedAppChangListener loadedListener;
    private boolean isPreLoad;
    public interface OnClonedAppChangListener {
        void onInstalled(List<AppModel> clonedApp);
        void onUnstalled(List<AppModel> clonedApp);
        void onLoaded(List<AppModel> clonedApp);
    }

    public static CloneHelper getInstance(Context context){
        if(instance == null){
            instance = new CloneHelper(context);
        }
        return instance;
    }

    public void loadClonedApps(Context context,OnClonedAppChangListener loadedListener){
        this.loadedListener = loadedListener;
        loadClonedApp(context);
//        new LoadClonedAppTask(context).execute();
    }

    public boolean installApp(Context context,AppModel appModel){
        try {
            // moved to AppManager.installApp
            // appModel.setClonedTime(System.currentTimeMillis());
            // appModel.setIndex(mClonedApps.size());
            int userId = AppListUtils.getInstance(context).isCloned(appModel.getPackageName())?
                    AppManager.getNextAvailableUserId(appModel.getPackageName()):0;
            if (!AppManager.installApp(context,appModel,userId)){
                return false;
            }
            PackageManager pm = context.getPackageManager();
            try {
                ApplicationInfo ai = pm.getApplicationInfo(appModel.getPackageName(), 0);
                CharSequence label = pm.getApplicationLabel(ai);
                appModel.setName(AppManager.getCompatibleName("" + label, userId));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            appModel.setClonedTime(System.currentTimeMillis());
            appModel.formatIndex(CloneHelper.getInstance(MApp.getApp()).getClonedApps().size(), userId);

            if (PreferencesUtils.getBoolean(context, AppConstants.KEY_AUTO_CREATE_SHORTCUT, false)) {
                CommonUtils.createShortCut(context, appModel);
            }
            DbManager.insertAppModel(context, appModel);
            AppManager.incPackageIndex(appModel.getPackageName());
            if (mClonedApps.size() < FastSwitch.LRU_PACKAGE_CNT
                    && FastSwitch.isEnable()) {
                FastSwitch.getInstance(context).updateLruPackages(AppManager.getMapKey(appModel.getPackageName(), appModel.getPkgUserId()));
            }
            synchronized (mClonedApps) {
                mClonedApps.add(appModel);
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (loadedListener != null) {
                        DbManager.notifyChanged();
                        List ret = new ArrayList();
                        ret.add(appModel);
                        loadedListener.onInstalled(ret);
                    }
                }
            });
        }catch (Exception ex) {
            MLogs.logBug(MLogs.getStackTraceString(ex));
            EventReporter.keyLog(MApp.getApp(), EventReporter.KeyLogTag.AERROR, "installError:" + appModel.getPackageName());
            return false;
        }
        return true;
    }

    public void unInstallApp(Context context,String packageName){
        if(packageName != null){
            List<AppModel> models = DbManager.queryAppModelByPackageName(context,packageName);
            AppManager.deleteApp(context,models);
        }
        synchronized (mClonedApps) {
            ListIterator<AppModel> iter = mClonedApps.listIterator();
            while (iter.hasNext()) {
                AppModel cm = iter.next();
                if (cm.getPackageName().equals(packageName) ) {
                    iter.remove();
                }
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (loadedListener != null) {
                    loadedListener.onUnstalled(mClonedApps);
                }
            }
        });
    }

    public void unInstallApp(Context context, AppModel model) {
        if (model != null) {
            String packageName = model.getPackageName();
            int userId = model.getPkgUserId();
            synchronized (mClonedApps) {
                ListIterator<AppModel> iter = mClonedApps.listIterator();
                while (iter.hasNext()) {
                    AppModel cm = iter.next();
                    if (cm.getPackageName().equals(packageName) &&
                            cm.getPkgUserId() == userId) {
                        iter.remove();
                        break;
                    }
                }
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (loadedListener != null) {
                        loadedListener.onUnstalled(mClonedApps);
                    }
                }
            });
            AppManager.deleteApp(context, model);
        }
    }

    public void unInstallApp(Context context, String packageName, int userId) {
        if (packageName != null) {
            AppModel model = DbManager.queryAppModelByPackageName(context, packageName, userId);
            unInstallApp(context, model);
        }

    }

    public void preLoadClonedApp(Context context){
        List<AppModel> tempList = AppManager.getClonedApp(context);
        if(tempList != null){
            synchronized (mClonedApps) {
                mClonedApps.clear();
                mClonedApps.addAll(tempList);
            }
        }
        isPreLoad = true;
    }

    private void loadClonedApp(Context context){
        if(!isPreLoad){
            List<AppModel> tempList = AppManager.getClonedApp(context);
            if(tempList != null){
                synchronized (mClonedApps) {
                    mClonedApps.clear();
                    mClonedApps.addAll(tempList);
                }
            }
        }
        isPreLoad = false;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(loadedListener != null) {
                    loadedListener.onLoaded(mClonedApps);
                }
            }
        });
    }

    private CloneHelper(Context context){
        mClonedApps = new ArrayList<>();
        AppManager.buildPackageConfigMap();
        AppManager.buildPackageIndexMap();
    }

    public List<AppModel> getClonedApps(){
        return mClonedApps;
    }

    public boolean isCloned(String pkg) {
        List<AppModel> list = DbManager.queryAppModelByPackageName(MApp.getApp(), pkg);
        return list != null && list.size() != 0;
    }

    public final AppModel getAppModel(String packageName, int userId) {
        if (packageName == null) {
            return null;
        }
        synchronized (mClonedApps){
            if (mClonedApps.size() > 0) {
                for (AppModel model : mClonedApps) {
                    if (model != null && model.getPackageName().equals(packageName) &&
                            model.getPkgUserId() == userId) {
                        return model;
                    }
                }
            }
        }
        return DbManager.queryAppModelByPackageName(MApp.getApp(), packageName, userId);
    }


    public int getCloneNumber() {
        return mClonedApps == null? 0: mClonedApps.size();
    }
}
