package com.polestar.domultiple.components.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.polestar.domultiple.PolestarApp;
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
        if (RemoteConfig.getBoolean("auto_hide_shortcut")
                && PreferencesUtils.isAbleToDetectShortcut()
                && (System.currentTimeMillis() - PreferencesUtils.getAutoShortcutTime()) > RemoteConfig.getLong("auto_shortcut_interval_hour")*3600*1000)
        {
            PreferencesUtils.updateAutoShortcutTime();
            CommonUtils.createLaunchShortcut(context);
        }
    }
}
