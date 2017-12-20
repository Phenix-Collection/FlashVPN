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
 * Created by PolestarApp on 8/2/16.
 */
public class EventReporter {

    private static FirebaseAnalytics mFirebaseAnalytics;

    public static void init(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(PolestarApp.getApp());
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        mFirebaseAnalytics.setUserProperty("channel", channel);
        mFirebaseAnalytics.setUserProperty("lite_mode", String.valueOf(PreferencesUtils.isLiteMode()));
        mFirebaseAnalytics.setUserProperty("adfree", String.valueOf(PreferencesUtils.isAdFree()));
        MLogs.e("MTA channel: " + channel);
    }

    public static void reportReferrer(String utm_source, String utm_medium, String utm_campaign, String utm_content, String utm_term, String gclid) {
        Bundle bundle = new Bundle();
        bundle.putString("utm_source", utm_source);
        bundle.putString("utm_medium", utm_medium);
        bundle.putString("utm_campaign", utm_campaign);
        bundle.putString("utm_content", utm_content);
        bundle.putString("utm_term", utm_term);
        bundle.putString("gclid", gclid);
        mFirebaseAnalytics.logEvent("refer", bundle);
    }

    public class KeyLogTag {
        public static final String AERROR = "aerror";
    }

    public static void keyLog(Context context, String tag, String log) {
        FirebaseCrash.log(tag + log);
    }

    public static void homeShow() {
        mFirebaseAnalytics.logEvent("home_show", null);
    }

    public static void appStart(boolean coldStart, boolean locker, String from, String pkg, int userId) {
        Bundle bundle = new Bundle();
        bundle.putString("package", pkg);
        bundle.putBoolean("coldStart", coldStart);
        bundle.putBoolean("locker", locker);
        bundle.putString("from", from);
        bundle.putInt("userId", userId);
        mFirebaseAnalytics.logEvent("app_start", bundle);
    }

    public static void reportCrash(Throwable ex, String packageName, boolean forground) {
        FirebaseCrash.report(ex);
        Bundle bundle = new Bundle();
        bundle.putString("package", packageName);
        bundle.putBoolean("forground", forground);
        mFirebaseAnalytics.logEvent("crash_event", bundle);
    }

    public static void reportRate(String status, String from) {
        Bundle bundle = new Bundle();
        bundle.putString("status", status);
        bundle.putString("from", from);
        long installTime = CommonUtils.getInstallTime(PolestarApp.getApp(), PolestarApp.getApp().getPackageName());
        long hour = (System.currentTimeMillis() - installTime)/(1000*60*60);
        bundle.putLong("install_hour", hour);
        mFirebaseAnalytics.logEvent("rate", bundle);
    }

    public static void generalClickEvent(String event) {
        Bundle bundle = new Bundle();
        bundle.putString("event", event);
        mFirebaseAnalytics.logEvent("general_event", bundle);
    }

}
