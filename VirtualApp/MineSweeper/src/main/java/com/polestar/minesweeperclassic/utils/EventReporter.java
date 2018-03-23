package com.polestar.minesweeperclassic.utils;

/**
 * Created by doriscoco on 2017/4/4.
 */

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.polestar.minesweeperclassic.BuildConfig;
import com.polestar.minesweeperclassic.MApp;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;

import java.util.Properties;

/**
 * Created by hxx on 8/2/16.
 */
public class EventReporter {

    private static final String APP_KEY = "AUW257B6YWQA";
    private static FirebaseAnalytics mFirebaseAnalytics;

    public static void init(Context context) {
        //StatConfig.init(context);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MApp.getApp());
        StatConfig.setDebugEnable(BuildConfig.DEBUG);
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        StatConfig.setInstallChannel(context, channel);
        StatConfig.setAutoExceptionCaught(true);
        MLogs.e("MTA channel: " + channel);

        try {
            StatService.startStatService(context, APP_KEY,
                    com.tencent.stat.common.StatConstants.VERSION);
        } catch (MtaSDkException e) {
            // MTA初始化失败
            MLogs.e("MTA start failed.");
            e.printStackTrace();
        }
    }

    public class KeyLogTag {
        public static final String AERROR = "aerror";
    }

    public static void keyLog(Context context, String tag, String log) {
        BuglyLog.e(tag,log);
        if (context != null) {
            Properties prop = new Properties();
            prop.setProperty(tag, log);
            StatService.trackCustomKVEvent(context, "key_log", prop);
        }
    }

    public static void homeShow(Context context) {
        StatService.trackCustomEvent(context, "home_show");
        mFirebaseAnalytics.logEvent("home_show", null);
    }

    public static void newGame(Context context, int difficulty, int mines) {
        Properties prop = new Properties();
        prop.setProperty("difficulty", ""+difficulty);
        prop.setProperty("mines", ""+mines);
        StatService.trackCustomKVEvent(context, "new_game", prop);
        Bundle bundle = new Bundle();
        bundle.putInt("difficulty", difficulty);
        bundle.putInt("mines", mines);
        mFirebaseAnalytics.logEvent("new_game", bundle);
    }

    public static void reportCrash(Context context, String packageName, boolean forground) {
        Properties prop = new Properties();
        if(packageName == null) packageName = "null";
        prop.setProperty("package", packageName);
        prop.setProperty("forground", forground?"true":"false");
        StatService.trackCustomKVEvent(context, "app_crash", prop);
    }

    public static void menuFAQ(Context context) {
        StatService.trackCustomEvent(context, "menu_faq");
    }

    public static void menuFeedback(Context context) {
        StatService.trackCustomEvent(context, "menu_feedback");
    }

    public static void reportRate(Context context, String status, String from) {
        Properties prop = new Properties();
        prop.setProperty("status", status);
        prop.setProperty("from", from);
        long installTime = CommonUtils.getInstallTime(context, context.getPackageName());
        long hour = (System.currentTimeMillis() - installTime)/(1000*60*60);
        prop.setProperty("install_hour",""+hour);
        // prop.setProperty("model", Build.FINGERPRINT);
        StatService.trackCustomKVEvent(context, "menu_rate", prop);
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
        Properties prop = new Properties();
        if (love) {
            prop.setProperty("love", from);
        } else {
            prop.setProperty("not_love", from);
        }
        StatService.trackCustomKVEvent(context, "love_app", prop);
        Bundle bundle = new Bundle();
        bundle.putBoolean("love", love);
        bundle.putString("from", from);
        mFirebaseAnalytics.logEvent("love_app", bundle);
    }

    public static void menuShare(Context context) {
        StatService.trackCustomEvent(context, "menu_share");
    }

    public static void menuSettings(Context context) {
        StatService.trackCustomEvent(context, "menu_settings");
    }

    public static void onResume(Context context) {
        StatService.onResume(context);
    }

    public static void onPause(Context context) {
        StatService.onPause(context);
    }

    public static void onStop(Context context) {
        StatService.onStop(context);
    }

    public static void trackBeginPage(Context context, String ext){
        StatService.trackBeginPage(context, ext);
    }
    public static void trackEndPage(Context context, String ext){
        StatService.trackEndPage(context, ext);
    }
}
