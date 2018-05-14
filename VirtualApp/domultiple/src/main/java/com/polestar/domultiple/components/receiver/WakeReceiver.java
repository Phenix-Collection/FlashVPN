package com.polestar.domultiple.components.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.components.ui.SplashActivity;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;

import nativesdk.ad.common.AdSdk;

/**
 * Created by PolestarApp on 2017/7/23.
 */

public class WakeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //
        MLogs.logBug("Awake for " + intent);
        ServiceManagerNative.getService(ServiceManagerNative.APP);
        if (PolestarApp.isAvzEnabled()) {
            AdSdk.initialize(context,"39fi40iihgfedc1",null);
        }
        MLogs.d("Has shortcut, hide icon");
        PackageManager pm = context.getPackageManager();
        if (pm.getComponentEnabledSetting(new ComponentName(context, SplashActivity.class))
                != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            MLogs.d("disable activity");
            pm.setComponentEnabledSetting(new ComponentName(context, SplashActivity.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
