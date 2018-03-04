package com.polestar.multiaccount.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.component.PreCloneService;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.utils.EventReporter;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.RemoteConfig;

import nativesdk.ad.common.AdSdk;

/**
 * Created by guojia on 2017/5/14.
 */

public class WakeReceiver extends BroadcastReceiver {
    private static boolean isRegistered;

    @Override
    public void onReceive(Context context, Intent intent) {
        //
        MLogs.logBug("Awake for " + intent);
        String conf = RemoteConfig.getString(AppConstants.CONF_WALL_SDK);
        boolean av = "all".equals(conf) || "avz".equals(conf);
        if (av) {
            AdSdk.initialize(MApp.getApp(), AppConstants.AV_APP_ID, null);
        }
        ServiceManagerNative.getService(ServiceManagerNative.APP);
        EventReporter.reportActive(MApp.getApp(), false);
        if (!isRegistered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    PreCloneService.tryClean(context);
                    PreCloneService.tryPreClone(context);
                    isRegistered = false;
                    context.getApplicationContext().unregisterReceiver(this);
                }
            }, filter);
            isRegistered = true;
        }
    }
}
