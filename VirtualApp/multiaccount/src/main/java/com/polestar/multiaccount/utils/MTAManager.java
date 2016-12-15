package com.polestar.multiaccount.utils;

import android.content.Context;

import com.lody.virtual.helper.utils.VLog;
import com.polestar.multiaccount.constant.AppConstants;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;

import java.util.Properties;

/**
 * Created by hxx on 8/2/16.
 */
public class MTAManager {

    private static final String APP_KEY = "AP546TPUIQ4X";

    public static void init(Context context) {
        //StatConfig.init(context);
        VLog.setKeyLogger(new VLog.IKeyLogger() {
            @Override
            public void keyLog(Context context, String tag, String log) {
                if (context != null) {
                    Properties prop = new Properties();
                    prop.setProperty(tag, log);
                    StatService.trackCustomKVEvent(context, "key_log", prop);
                }
            }
        });
        StatConfig.setDebugEnable(!AppConstants.IS_RELEASE_VERSION);
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        StatConfig.setInstallChannel(context, channel);
        StatConfig.setAutoExceptionCaught(true);
        Logs.e("MTA channel: " + channel);

        try {
            StatService.startStatService(context, APP_KEY,
                    com.tencent.stat.common.StatConstants.VERSION);
        } catch (MtaSDkException e) {
            // MTA初始化失败
            Logs.e("MTA start failed.");
            e.printStackTrace();
        }
    }

    class KeyLogTag {
        public static final String AERROR = "aerror";
    }

    public static void keyLog(Context context, String tag, String log) {
        if (context != null) {
            Properties prop = new Properties();
            prop.setProperty(tag, log);
            StatService.trackCustomKVEvent(context, "key_log", prop);
        }
    }
    public static void homeAdd(Context context) {
        StatService.trackCustomEvent(context, "home_add");
    }

    public static void applistClone(Context context, String packageName) {
        Properties prop = new Properties();
        prop.setProperty("package", packageName);
        StatService.trackCustomKVEvent(context, "applist_clone", prop);
    }

    public static void deleteClonedApp(Context context, String packageName) {
        Properties prop = new Properties();
        prop.setProperty("package", packageName);
        StatService.trackCustomKVEvent(context, "home_delete", prop);
    }

    public static void launchApp(Context context, String packageName, String from) {
        Properties prop = new Properties();
        prop.setProperty("package", packageName);
        if (from.equals(AppConstants.VALUE_FROM_HOME)) {
            StatService.trackCustomKVEvent(context, "home_launch", prop);
        } else if (from.equals(AppConstants.VALUE_FROM_SHORTCUT)) {
            StatService.trackCustomKVEvent(context, "mobile_launch", prop);
        }
    }

    public static void addShortCut(Context context, String packageName) {
        Properties prop = new Properties();
        prop.setProperty("package", packageName);
        StatService.trackCustomKVEvent(context, "home_shortcut", prop);
    }

    public static void reportCrash(Context context, String packageName) {
        Properties prop = new Properties();
        prop.setProperty("package", packageName);
        StatService.trackCustomKVEvent(context, "app_crash", prop);
    }

    public static void homeMenu(Context context) {
        StatService.trackCustomEvent(context, "home_menu");
    }

    public static void menuNotification(Context context) {
        StatService.trackCustomEvent(context, "menu_notifications");
    }

    public static void menuFAQ(Context context) {
        StatService.trackCustomEvent(context, "menu_faq");
    }

    public static void menuFeedback(Context context) {
        StatService.trackCustomEvent(context, "menu_feedback");
    }

    public static void menuRate(Context context) {
        StatService.trackCustomEvent(context, "menu_rate");
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
