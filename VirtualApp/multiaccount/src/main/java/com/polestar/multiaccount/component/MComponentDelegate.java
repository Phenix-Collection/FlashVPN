package com.polestar.multiaccount.component;

import android.app.Activity;
import android.content.Intent;

import com.lody.virtual.client.hook.delegate.ComponentDelegate;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.utils.PreferencesUtils;

/**
 * Created by guojia on 2016/12/16.
 */

public class MComponentDelegate implements ComponentDelegate {
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
}
