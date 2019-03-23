package com.polestar.superclone.component;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import com.polestar.clone.CustomizeAppData;
import com.polestar.superclone.MApp;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.SuperConfig;

/**
 * Created by yxx on 2016/9/7.
 */
public class LocalActivityLifecycleCallBacks implements Application.ActivityLifecycleCallbacks {

    private MApp mApp;
    private boolean isForground;
    private boolean isMainApp;

    public LocalActivityLifecycleCallBacks(MApp app, boolean isMainApp) {
        this.mApp = app;
        this.isMainApp = isMainApp;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CustomizeAppData appData = MApp.getApp().getCurrentCustomizeData();
            if (appData != null && SuperConfig.get().isHandleInterstitial(activity.getClass().getName())) {
                activity.setTaskDescription(new ActivityManager.TaskDescription(appData.label, appData.getCustomIcon()));
            }
        }
        MApp.getApp().setCurrentAdClone(null, -1);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        MLogs.e("onActivityStarted "  + activity.getComponentName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        MLogs.e("onActivityResumed " +  activity.getComponentName());
        isForground = true;

        //MTA
    }

    @Override
    public void onActivityPaused(Activity activity) {
        MLogs.e("onActivityPaused " + activity.getComponentName());
        isForground = false;
        //MTA
    }

    @Override
    public void onActivityStopped(Activity activity) {
		MLogs.e("onActivityStopped "  + activity.getComponentName());
        if(!isForground ){
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
