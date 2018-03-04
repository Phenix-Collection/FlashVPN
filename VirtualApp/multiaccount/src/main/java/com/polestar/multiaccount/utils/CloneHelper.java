package com.polestar.multiaccount.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.os.VUserInfo;
import com.lody.virtual.os.VUserManager;
import com.lody.virtual.remote.InstallResult;
import com.lody.virtual.remote.InstalledAppInfo;
import com.polestar.grey.GreyAttribute;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;

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

    public void installApp(Context context,AppModel appModel){
        try {
            // moved to AppManager.installApp
            // appModel.setClonedTime(System.currentTimeMillis());
            // appModel.setIndex(mClonedApps.size());
            DbManager.insertAppModel(context, appModel);
            DbManager.notifyChanged();
            AppManager.incPackageIndex(appModel.getPackageName());
            synchronized (mClonedApps) {
                mClonedApps.add(appModel);
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (loadedListener != null) {
                        loadedListener.onInstalled(mClonedApps);
                    }
                    //GreyAttribute.checkAndClick(appModel.getPackageName());
                }
            });
        }catch (Exception ex) {
            MLogs.logBug(MLogs.getStackTraceString(ex));
            EventReporter.keyLog(MApp.getApp(), EventReporter.KeyLogTag.AERROR, "installError:" + appModel.getPackageName());
        }
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

    public void unInstallApp(Context context, String packageName, int userId) {
        if (packageName != null) {
            AppModel model = DbManager.queryAppModelByPackageName(context, packageName, userId);
            AppManager.deleteApp(context, model);
        }
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
}
