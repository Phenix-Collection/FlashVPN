package com.polestar.multiaccount.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.polestar.multiaccount.constant.AppConstants;
import com.tencent.bugly.crashreport.BuglyLog;
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

        StatConfig.setDebugEnable(!AppConstants.IS_RELEASE_VERSION);
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
    public static void homeAdd(Context context) {
        StatService.trackCustomEvent(context, "home_add");
    }

    public static void homeShow(Context context) {
        StatService.trackCustomEvent(context, "home_show");
    }

    public static void homeGiftClick(Context context, String adSource) {
        Properties prop = new Properties();
        prop.setProperty("ad_source", adSource);
        MLogs.d("home_gift_click: " + adSource);
        StatService.trackCustomKVEvent(context, "home_gift_click", prop);
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

    public static void lockerEnable(Context context, String status, String pkg) {
        Properties prop = new Properties();
        prop.setProperty("status", status);
        prop.setProperty("package", pkg);
        StatService.trackCustomKVEvent(context, "locker_enable", prop);
    }
    public static void homeMenu(Context context) {
        StatService.trackCustomEvent(context, "home_menu");
    }

    public static void menuNotification(Context context) {
        StatService.trackCustomEvent(context, "menu_notifications");
    }

    public static void menuPrivacyLocker(Context context) {
        StatService.trackCustomEvent(context, "menu_privacy_locker");
    }

    public static void menuFAQ(Context context) {
        StatService.trackCustomEvent(context, "menu_faq");
    }

    public static void applistAdLoad(Context context, String status) {
        Properties prop = new Properties();
        prop.setProperty("status", status);
        StatService.trackCustomKVEvent(context, "applist_native_ad", prop);
    }

    public static void menuFeedback(Context context) {
        StatService.trackCustomEvent(context, "menu_feedback");
    }

    public static void reportRate(Context context, String status, String from) {
        Properties prop = new Properties();
        prop.setProperty("status", status);
        prop.setProperty("from", from);
        int clonedCnt = CloneHelper.getInstance(context).getClonedApps().size();
        prop.setProperty("clone_num",""+clonedCnt);
        long installTime = CommonUtils.getInstallTime(context, context.getPackageName());
        long hour = (System.currentTimeMillis() - installTime)/(1000*60*60);
        prop.setProperty("install_hour",""+hour);
       // prop.setProperty("model", Build.FINGERPRINT);
        StatService.trackCustomKVEvent(context, "menu_rate", prop);
    }

    public static void loveCloneApp(Context context, boolean love, String pkg) {
        if (pkg == null) {
            return;
        }
        Properties prop = new Properties();
        if (love) {
            prop.setProperty("love", pkg);
        } else {
            prop.setProperty("not_love", pkg);
        }
        StatService.trackCustomKVEvent(context, "love_clone_app", prop);
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
