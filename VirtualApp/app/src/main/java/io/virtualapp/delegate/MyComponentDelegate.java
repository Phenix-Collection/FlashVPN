package io.virtualapp.delegate;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.lody.virtual.client.hook.delegate.ComponentDelegate;
import com.lody.virtual.helper.utils.Reflect;

import java.io.File;


public class MyComponentDelegate implements ComponentDelegate {
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

    }

    @Override
    public void beforeActivityPause(String pkg) {

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
        return false;
    }

    @Override
    public void reloadLockerSetting(String newKey, boolean adFree, long interval) {

    }
}
