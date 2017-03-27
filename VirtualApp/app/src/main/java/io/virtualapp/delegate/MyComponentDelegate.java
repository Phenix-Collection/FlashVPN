package io.virtualapp.delegate;

import android.app.Activity;
import android.content.Intent;

import com.lody.virtual.client.hook.delegate.ComponentDelegate;
import com.lody.virtual.helper.utils.Reflect;

import java.io.File;


public class MyComponentDelegate implements ComponentDelegate {
    @Override
    public void beforeActivityCreate(Activity activity) {

    }

    @Override
    public void beforeActivityResume(Activity activity) {

    }

    @Override
    public void beforeActivityPause(Activity activity) {

    }

    @Override
    public void beforeActivityDestroy(Activity activity) {

    }


    @Override
    public void onSendBroadcast(Intent intent) {

    }

    @Override
    public boolean isNotificationEnabled(String pkg) {
        return false;
    }
}
