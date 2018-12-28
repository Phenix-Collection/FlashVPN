package nova.fast.free.vpn.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import nova.fast.free.vpn.NovaApp;
import nova.fast.free.vpn.utils.EventReporter;
import nova.fast.free.vpn.utils.MLogs;

/**
 * Created by guojia on 2017/5/14.
 */

public class WakeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //
        MLogs.logBug("Awake for " + intent);
        EventReporter.reportWake(NovaApp.getApp(), intent.getAction());
    }
}
