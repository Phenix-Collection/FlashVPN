package com.polestar.multiaccount.component;

import android.app.Activity;
import android.content.Intent;

import com.lody.virtual.client.hook.delegate.ComponentDelegate;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.PreferencesUtils;

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
    public void beforeActivityCreate(Activity activity) {

    }

    @Override
    public void beforeActivityResume(Activity activity) {
        if (PreferencesUtils.isLockerEnabled(MApp.getApp())) {
            AppLockMonitor.getInstance().onActivityResume(activity);
        }
    }

    @Override
    public void beforeActivityPause(Activity activity) {
        if (PreferencesUtils.isLockerEnabled(MApp.getApp())) {
            AppLockMonitor.getInstance().onActivityPause(activity);
        }
    }

    @Override
    public void beforeActivityDestroy(Activity activity) {

    }

    @Override
    public void onSendBroadcast(Intent intent) {

    }

    @Override
    public boolean isNotificationEnabled(String pkg) {
        MLogs.d("isNotificationEnabled pkg: " + pkg + " " + pkgs.contains(pkg) );
        return pkgs.contains(pkg);
    }
}
