package com.polestar.multiaccount.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lody.virtual.client.ipc.ServiceManagerNative;
import com.polestar.multiaccount.utils.MLogs;

/**
 * Created by guojia on 2017/5/14.
 */

public class WakeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //
        MLogs.logBug("Awake for " + intent);
        ServiceManagerNative.getService(ServiceManagerNative.APP);
    }
}
