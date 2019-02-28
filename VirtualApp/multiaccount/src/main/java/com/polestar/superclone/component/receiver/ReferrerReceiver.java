package com.polestar.superclone.component.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.gms.measurement.AppMeasurementInstallReferrerReceiver;
import com.polestar.superclone.MApp;
import com.polestar.superclone.reward.ShareActions;
import com.polestar.superclone.reward.TaskPreference;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.PreferencesUtils;
import com.tencent.bugly.crashreport.CrashReport;

import java.net.URLDecoder;

/**
 * Created by guojia on 2017/5/4.
 */

public class ReferrerReceiver extends BroadcastReceiver {

    //am broadcast -a com.android.vending.INSTALL_REFERRER -n com.polestar.multiaccount/.component.receiver.ReferrerReceiver --es "referrer" "utm_source=test&utm_campaign=abc"
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
            return;
        }
        String referrer = intent.getStringExtra("referrer");
        if(referrer == null) {
            MLogs.logBug("Install referrer extras are null");
            return;
        }

        EventReporter.reportReferrer(EventReporter.REFERRER_TYPE_BROADCAST, referrer);
//        new AppMeasurementInstallReferrerReceiver().onReceive(context, intent);
    }
}
