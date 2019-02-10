package com.polestar.minesweeperclassic.utils;

/**
 * Created by doriscoco on 2017/4/4.
 */

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.polestar.minesweeperclassic.MApp;
/**
 * Created by hxx on 8/2/16.
 */
public class EventReporter {

    private static FirebaseAnalytics mFirebaseAnalytics;

    public static String PROP_REWARD_USER = "rewarded";

    public static void init(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MApp.getApp());
    }


    public static void setUserProperty(String name, String prop) {
        if(mFirebaseAnalytics != null) {
            mFirebaseAnalytics.setUserProperty(name, prop);
        }
    }

    public static void homeShow(Context context) {
        mFirebaseAnalytics.logEvent("home_show", null);
    }

    public static void newGame(Context context, int difficulty, int mines) {
        Bundle bundle = new Bundle();
        bundle.putInt("difficulty", difficulty);
        bundle.putInt("mines", mines);
        mFirebaseAnalytics.logEvent("new_game", bundle);
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

    public static void loveApp(Context context, boolean love, String from) {
        if (from == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("love", love);
        bundle.putString("from", from);
        mFirebaseAnalytics.logEvent("love_app", bundle);
    }

    private static String sWakeSrc = null;

    public static void reportWake(Context context, String src){
        if (sWakeSrc == null && !TextUtils.isEmpty(src)) {
            sWakeSrc = src;
            Bundle prop = new Bundle();
            prop.putString("wake_src", src);
            mFirebaseAnalytics.logEvent("track_wake", prop);
        }
        MLogs.d("Wake from " + src + " original: " + sWakeSrc);
    }

    public static void generalEvent(String event) {
        Bundle prop = new Bundle();
        prop.putString("name", event);
        mFirebaseAnalytics.logEvent("general_event", prop);
    }

    public static void reportReward(String name) {
        Bundle prop = new Bundle();
        prop.putString("name", name);
        mFirebaseAnalytics.logEvent("reward_event", prop);
    }
}
