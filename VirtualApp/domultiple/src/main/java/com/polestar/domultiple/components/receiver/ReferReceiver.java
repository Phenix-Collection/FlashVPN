package com.polestar.domultiple.components.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.gms.measurement.AppMeasurementInstallReferrerReceiver;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.utils.EventReporter;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by guojia on 2017/7/23.
 */

public class ReferReceiver extends BroadcastReceiver {
    public static final String UTM_SOURCE = "utm_source";
    public static final String UTM_MEDIUM = "utm_medium";
    public static final String UTM_CAMPAIGN = "utm_campaign";
    public static final String UTM_TERM = "utm_term";
    public static final String UTM_CONTENT = "utm_content";
    public static final String GCLID = "gclid";

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
        Uri referUri = Uri.parse(referrer);
        String[] parms = referrer.split("&");
        if (parms == null || parms.length == 0) {
            return;
        }
        String utm_source =null,utm_medium =null,utm_campaign =null,utm_term =null,utm_content =null,gclid =null;
        for(String s: parms) {
            String arr[] = s.split("=");
            if (arr == null || arr.length!=2) {
                continue;
            }
            MLogs.d(arr[0], " " + arr[1]);
            switch (arr[0]){
                case UTM_CAMPAIGN:
                    utm_campaign = arr[1];
                    break;
                case GCLID:
                    gclid = arr[1];
                    break;
                case UTM_SOURCE:
                    utm_source = arr[1];
                    break;
                case UTM_TERM:
                    utm_term = arr[1];
                    break;
                case UTM_MEDIUM:
                    utm_medium = arr[1];
                    break;
                case UTM_CONTENT:
                    utm_content = arr[1];
                    break;
            }
        }

        MLogs.d("Receive refer: " + referrer + " utm_source : " + utm_source);
        if(!TextUtils.isEmpty(utm_source)) {
            PreferencesUtils.setInstallChannel(utm_source);
            CrashReport.setAppChannel(PolestarApp.getApp(), utm_source);
            EventReporter.reportReferrer(utm_source,utm_medium,utm_campaign,utm_content,utm_term,gclid);
        }
        new AppMeasurementInstallReferrerReceiver().onReceive(context, intent);
    }
}
