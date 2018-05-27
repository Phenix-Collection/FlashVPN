package com.polestar.domultiple.components;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.polestar.domultiple.components.ui.AppLockActivity;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.IAppMonitor;
/**
 * Created by guojia on 2018/5/27.
 */

public class AppMonitorService extends Service {
    //This service in charge of showing interstitial ads and locker
    private final static String TAG = "AppLockMonitor";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new AppMonitor();
    }

    private class AppMonitor extends IAppMonitor.Stub {
        public void onAppSwitchForeground(String pkg, int userId){
            MLogs.d(TAG, "OnAppForeground: " + pkg + " user: " + userId);
        }

        public void onAppLock(String pkg, int userId){
            MLogs.d(TAG, "onAppLock: " + pkg + " user: " + userId);
            AppLockActivity.start(AppMonitorService.this, pkg, userId);
        }
    }
}
