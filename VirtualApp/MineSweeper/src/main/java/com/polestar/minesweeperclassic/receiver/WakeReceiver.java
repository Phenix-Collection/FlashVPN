package com.polestar.minesweeperclassic.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.polestar.minesweeperclassic.Service.DaemonService;
import com.polestar.minesweeperclassic.utils.EventReporter;
import com.polestar.minesweeperclassic.utils.MLogs;


public class WakeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //
        MLogs.logBug("Awake for " + intent);
        EventReporter.reportWake(context, intent.getAction());
        try {
            DaemonService.startup(context);
        } catch (Exception ex) {
            MLogs.e(ex);
        }
    }
}
