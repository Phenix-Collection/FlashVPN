package com.polestar.domultiple.components.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.polestar.domultiple.utils.MLogs;

/**
 * Created by guojia on 2017/7/23.
 */

public class WakeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //
        MLogs.logBug("Awake for " + intent);
        ServiceManagerNative.getService(ServiceManagerNative.APP);
    }
}
