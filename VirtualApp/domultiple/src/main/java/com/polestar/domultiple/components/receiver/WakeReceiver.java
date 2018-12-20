package com.polestar.domultiple.components.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.polestar.clone.client.ipc.ServiceManagerNative;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.components.ui.SplashActivity;
import com.polestar.domultiple.utils.EventReporter;
import com.polestar.domultiple.utils.MLogs;


/**
 * Created by PolestarApp on 2017/7/23.
 */

public class WakeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //
        MLogs.logBug("Awake for " + intent);
        ServiceManagerNative.getService(ServiceManagerNative.APP);
        EventReporter.reportWake(PolestarApp.getApp(), intent.getAction());

        PackageManager pm = context.getPackageManager();
        if (pm.getComponentEnabledSetting(new ComponentName(context, SplashActivity.class))
                != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            pm.setComponentEnabledSetting(new ComponentName(context, SplashActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
