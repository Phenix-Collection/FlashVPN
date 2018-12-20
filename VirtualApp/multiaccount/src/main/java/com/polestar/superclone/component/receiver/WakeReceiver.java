package com.polestar.superclone.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.polestar.clone.client.ipc.ServiceManagerNative;
import com.polestar.superclone.MApp;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.MLogs;

/**
 * Created by guojia on 2017/5/14.
 */

public class WakeReceiver extends BroadcastReceiver {
    private static boolean isRegistered;

    @Override
    public void onReceive(Context context, Intent intent) {
        //
        MLogs.logBug("Awake for " + intent);
        ServiceManagerNative.getService(ServiceManagerNative.APP);
        EventReporter.reportActive(MApp.getApp(), false, intent.getAction());
        EventReporter.reportWake(MApp.getApp(), intent.getAction());
        if (!isRegistered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            context.getApplicationContext().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    isRegistered = false;
                    context.getApplicationContext().unregisterReceiver(this);
                }
            }, filter);
            isRegistered = true;
        }
    }
}
