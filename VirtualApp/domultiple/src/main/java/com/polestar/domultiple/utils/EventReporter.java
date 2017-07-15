package com.polestar.domultiple.utils;

/**
 * Created by doriscoco on 2017/4/4.
 */

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.polestar.domultiple.PolestarApp;

/**
 * Created by hxx on 8/2/16.
 */
public class EventReporter {

    private static FirebaseAnalytics mFirebaseAnalytics;

    public static void init(Context context) {
        //StatConfig.init(context);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(PolestarApp.getApp());
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        mFirebaseAnalytics.setUserProperty("channel", channel);
        MLogs.e("MTA channel: " + channel);
    }

    public class KeyLogTag {
        public static final String AERROR = "aerror";
    }

    public static void keyLog(Context context, String tag, String log) {
        FirebaseCrash.log(tag + log);
    }

    public static void homeShow(Context context) {
        mFirebaseAnalytics.logEvent("home_show", null);
    }

    public static void reportCrash(Context context, String packageName, boolean forground) {
        FirebaseCrash.report(new Exception());
    }

    public static void reportRate(Context context, String status, String from) {
        int hour = 0;
        Bundle bundle = new Bundle();
        bundle.putString("status", status);
        bundle.putString("from", from);
        bundle.putInt("install_hour", (int)hour);
        mFirebaseAnalytics.logEvent("rate", bundle);
    }

    public static void loveApp(Context context, boolean love, String from) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("love", love);
        bundle.putString("from", from);
        mFirebaseAnalytics.logEvent("love_app", bundle);
    }

}
