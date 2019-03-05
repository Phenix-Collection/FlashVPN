package winterfell.flash.vpn.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;

/**
 * Created by guojia on 2017/5/14.
 */

public class WakeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //
        MLogs.logBug("Awake for " + intent);
        EventReporter.reportWake(FlashApp.getApp(), intent.getAction());
    }
}
