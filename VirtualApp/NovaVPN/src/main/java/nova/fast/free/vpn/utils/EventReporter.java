package nova.fast.free.vpn.utils;

/**
 * Created by doriscoco on 2017/4/4.
 */

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import nova.fast.free.vpn.NovaApp;
import nova.fast.free.vpn.ui.AboutActivity;

/**
 * Created by hxx on 8/2/16.
 */
public class EventReporter {

    private static FirebaseAnalytics mFirebaseAnalytics;

    public static void init(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(NovaApp.getApp());
    }


    public static void subscribeClick(Context context, String from, String id) {
        Bundle bundle = new Bundle();
        bundle.putString("from", from);
        bundle.putString("id", id);
        mFirebaseAnalytics.logEvent("subs_click", bundle);
    }

    public static void reportRate(Context context, String status, String from) {
        long installTime = CommonUtils.getInstallTime(context, context.getPackageName());
        long hour = (System.currentTimeMillis() - installTime)/(1000*60*60);
        Bundle bundle = new Bundle();
        bundle.putString("status", status);
        bundle.putString("from", from);
        bundle.putInt("install_hour", (int)hour);
        mFirebaseAnalytics.logEvent("rate", bundle);
    }

    public static void reportConnect(Context context, String name) {
        Bundle bundle = new Bundle();
        bundle.putString("server", name);
        mFirebaseAnalytics.logEvent("connect", bundle);
    }

    public static void loveApp(Context context, boolean love, String from) {
        if (from == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("love", love);
        bundle.putString("from", from);
        mFirebaseAnalytics.logEvent("love_app", bundle);
    }

    public static void generalEvent(Context context, String event) {
        Bundle bundle = new Bundle();
        bundle.putString("event", event);
        mFirebaseAnalytics.logEvent("general_event", bundle);
    }
}
