package com.polestar.multiaccount.component;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.MTAManager;

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
        MTAManager.onResume(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        MLogs.e("onActivityPaused " + activity.getComponentName());
        isForground = false;
        //MTA
        MTAManager.onPause(activity);
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
