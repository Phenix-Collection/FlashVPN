package com.polestar.domultiple.clone;

/**
 * Created by PolestarApp on 2017/7/16.
 */

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.delegate.ComponentDelegate;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.DBManager;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.widget.locker.AppLockMonitor;

import java.util.HashSet;
import java.util.List;

/**
 * Created by PolestarApp on 2016/12/16.
 */

public class CloneComponentDelegate implements ComponentDelegate {

    private HashSet<String> pkgs = new HashSet<>();
    public void asyncInit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<CloneModel> list = DBManager.queryAppList(PolestarApp.getApp());
                for(CloneModel app:list) {
                    if (app.getNotificationEnable()) {
                        pkgs.add(app.getPackageName());
                    }
                }
            }
        }).start();

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
    public void beforeActivityResume(String pkg) {
        MLogs.d("beforeActivityResume " + pkg);
        AppLockMonitor.getInstance().onActivityResume(pkg);
    }

    @Override
    public void beforeActivityPause(String pkg) {
        MLogs.d("beforeActivityPause " + pkg);
        AppLockMonitor.getInstance().onActivityPause(pkg);
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
    public boolean isNotificationEnabled(String pkg) {
        MLogs.d("isNotificationEnabled pkg: " + pkg + " " + pkgs.contains(pkg) );
        return pkgs.contains(pkg);
    }

    @Override
    public void reloadLockerSetting(String newKey, boolean adFree, long interval) {
       AppLockMonitor.getInstance().reloadSetting(newKey, adFree, interval);
    }
}
