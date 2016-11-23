package com.polestar.multiaccount.component;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.utils.EventReportManager;
import com.polestar.multiaccount.utils.Logs;
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
        Logs.e("onActivityStarted");
        //Analytics
        EventReportManager.onStart(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Logs.e("onActivityResumed");
        isForground = true;
        //MTA
        MTAManager.onResume(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Logs.e("onActivityPaused");
        isForground = false;
        //MTA
        MTAManager.onPause(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
		Logs.e("onActivityStopped");
        if(!isForground ){
        }
        //Analytics
        EventReportManager.onStop(activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
