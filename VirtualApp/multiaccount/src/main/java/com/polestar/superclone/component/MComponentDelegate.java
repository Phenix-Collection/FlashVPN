package com.polestar.superclone.component;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.lody.virtual.client.hook.delegate.ComponentDelegate;
import com.polestar.superclone.MApp;
import com.polestar.superclone.db.DbManager;
import com.polestar.superclone.model.AppModel;
import com.polestar.superclone.utils.MLogs;

import java.util.HashSet;
import java.util.List;

/**
 * Created by guojia on 2016/12/16.
 */

public class MComponentDelegate implements ComponentDelegate {

    private HashSet<String> pkgs = new HashSet<>();
    public void init() {
        List<AppModel> list = DbManager.queryAppList(MApp.getApp());
        for(AppModel app:list) {
            if (app.isNotificationEnable()) {
                pkgs.add(app.getPackageName());
            }
        }
    }

    @Override
    public void beforeApplicationCreate(Application application) {

    }

    @Override
    public void afterApplicationCreate(Application application) {

    }

    @Override
    public void beforeActivityCreate(Activity activity) {

    }

    @Override
    public void beforeActivityResume(String pkg, int userId) {
        MLogs.d("beforeActivityResume " + pkg);
        //if (PreferencesUtils.isLockerEnabled(VirtualCore.get().getContext())) {
            AppLockMonitor.getInstance().onActivityResume(pkg, userId);
        //}
    }

    @Override
    public void beforeActivityPause(String pkg, int userId) {
        MLogs.d("beforeActivityPause " + pkg);
       // if (PreferencesUtils.isLockerEnabled(VirtualCore.get().getContext())) {
            AppLockMonitor.getInstance().onActivityPause(pkg, userId);
       // }
    }

    @Override
    public void beforeActivityDestroy(Activity activity) {

    }

    @Override
    public void afterActivityCreate(Activity activity) {

    }

    @Override
    public void afterActivityResume(Activity activity) {

    }

    @Override
    public void afterActivityPause(Activity activity) {

    }

    @Override
    public void afterActivityDestroy(Activity activity) {

    }

    @Override
    public void onSendBroadcast(Intent intent) {

    }

    @Override
    public boolean isNotificationEnabled(String pkg, int userId) {
        MLogs.d("isNotificationEnabled pkg: " + pkg + " " + pkgs.contains(pkg) );
        return pkgs.contains(pkg);
    }

    @Override
    public void reloadSetting(String lockKey, boolean adFree, long lockInterval, boolean quickSwitch) {
        //AppLockMonitor.getInstance().reloadSetting(newKey, adFree, interval);
    }
}
