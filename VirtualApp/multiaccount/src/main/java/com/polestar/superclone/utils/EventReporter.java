package com.polestar.superclone.utils;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.polestar.superclone.MApp;
import com.polestar.superclone.component.receiver.ReferrerReceiver;
import com.polestar.superclone.constant.AppConstants;
import com.tencent.bugly.crashreport.BuglyLog;

/**
 * Created by hxx on 8/2/16.
 */
public class EventReporter {

    private static final String APP_KEY = "Aqc1105890917";
    private static FirebaseAnalytics mFirebaseAnalytics;

    private static final String PROP_CHANNEL = "channel";
    private static final String PROP_CAMP = "campaign";

    public static void init(Context context) {
        //StatConfig.asyncInit(context);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MApp.getApp());
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        String referChannel = PreferencesUtils.getInstallChannel();
        mFirebaseAnalytics.setUserProperty(PROP_CHANNEL, channel);
        MLogs.e("MTA channel: " + channel + " refer: " + referChannel);
    }

    public class KeyLogTag {
        public static final String AERROR = "aerror";
    }

    public static void keyLog(Context context, String tag, String log) {
        BuglyLog.e(tag,log);
        if (context != null) {
            Bundle prop = new Bundle();
            prop.putString(tag, log);
            mFirebaseAnalytics.logEvent("key_log", prop);
        }
    }
    public static void homeAdd(Context context) {
        generalEvent( "home_add");
    }

    public static void homeShow(Context context) {
        generalEvent("home_show");
    }

    public static void homeGiftClick(Context context, String adSource) {
        Bundle prop = new Bundle();
        prop.putString("ad_source", adSource);
        MLogs.d("home_gift_click: " + adSource);
        mFirebaseAnalytics.logEvent("home_gift_click", prop);
    }

    public static void applistClone(Context context, String packageName) {
        Bundle prop = new Bundle();
        prop.putString("package", packageName);
        mFirebaseAnalytics.logEvent("applist_clone", prop);
    }

    public static void reportsAdsLaunch(Context context, String name) {
        Bundle prop = new Bundle();
        prop.putString("name", name);
        mFirebaseAnalytics.logEvent("ads_launch", prop);
    }
    public static void appCloneAd(Context context, String ad) {
        Bundle prop = new Bundle();
        prop.putString("AD", ad);
        mFirebaseAnalytics.logEvent("app_clone_ad", prop);
    }

    public static void deleteClonedApp(Context context, String packageName) {
        Bundle prop = new Bundle();
        prop.putString("package", packageName);
        mFirebaseAnalytics.logEvent("home_delete", prop);
    }

    public static void launchApp(Context context, String packageName, String from, boolean hasLocker) {
        Bundle prop = new Bundle();
        prop.putString("package", packageName);
        prop.putString("locker", ""+hasLocker);
        if (from.equals(AppConstants.VALUE_FROM_HOME)) {
            mFirebaseAnalytics.logEvent("home_launch", prop);
        } else if (from.equals(AppConstants.VALUE_FROM_SHORTCUT)) {
            mFirebaseAnalytics.logEvent("mobile_launch", prop);
        }
    }

    public static void addShortCut(Context context, String packageName) {
        Bundle prop = new Bundle();
        prop.putString("package", packageName);
        mFirebaseAnalytics.logEvent("home_shortcut", prop);
    }

    public static void reportCrash(Context context, String packageName, boolean forground) {
        Bundle prop = new Bundle();
        if(packageName == null) packageName = "null";
        prop.putString("package", packageName);
        prop.putString("forground", forground?"true":"false");
        mFirebaseAnalytics.logEvent("app_crash", prop);
    }

    public static void reportArm64(String pkg, String status) {
        Bundle prop = new Bundle();
        prop.putString("status", status+ "_" + pkg);
        mFirebaseAnalytics.logEvent( "arm64", prop);
    }

    public static void lockerEnable(Context context, String status, String pkg, String from) {
        Bundle prop = new Bundle();
        prop.putString("status", status);
        prop.putString("package", pkg);
        prop.putString("from", from == null? "null": from);
        mFirebaseAnalytics.logEvent("locker_enable", prop);
    }

    public static void settingAfterClone(Context context, String pkg, boolean notification, boolean locker, boolean shortCut){
        Bundle prop = new Bundle();
        prop.putString("notification", String.valueOf(notification));
        prop.putString("package", pkg);
        prop.putString("locker", String.valueOf(locker));
        prop.putString("shortcut", String.valueOf(shortCut));
        mFirebaseAnalytics.logEvent("setting_after_clone", prop);
    }

    public static void generalEvent(String event) {
        Bundle prop = new Bundle();
        prop.putString("name", event);
        mFirebaseAnalytics.logEvent("general_event", prop);
    }

    public static void homeMenu(Context context) {
        generalEvent("home_menu");
    }

    public static void menuNotification(Context context) {
        generalEvent("menu_notifications");
    }

    public static void menuPrivacyLocker(Context context) {
        generalEvent("menu_privacy_locker");
    }

    public static void menuFAQ(Context context) {
        generalEvent("menu_faq");
    }

    public static void applistAdLoad(Context context, String status) {
        Bundle prop = new Bundle();
        prop.putString("status", status);
        mFirebaseAnalytics.logEvent("applist_native_ad", prop);
    }

    public static void menuFeedback(Context context) {
        generalEvent("menu_feedback");
    }

    public static void reportRate(Context context, String status, String from) {
        Bundle prop = new Bundle();
        prop.putString("status", status);
        prop.putString("from", from);
        int clonedCnt = CloneHelper.getInstance(context).getClonedApps().size();
        prop.putString("clone_num",""+clonedCnt);
        long installTime = CommonUtils.getInstallTime(context, context.getPackageName());
        long hour = (System.currentTimeMillis() - installTime)/(1000*60*60);
        prop.putString("install_hour",""+hour);
       // prop.putString("model", Build.FINGERPRINT);
        mFirebaseAnalytics.logEvent("menu_rate", prop);
    }

    public static void loveCloneApp(Context context, boolean love, String pkg) {
        if (pkg == null) {
            return;
        }
        Bundle prop = new Bundle();
        if (love) {
            prop.putString("love", pkg);
        } else {
            prop.putString("not_love", pkg);
        }
        mFirebaseAnalytics.logEvent("love_clone_app", prop);
    }

    public static void setGMS(Context context, boolean status, String from) {
        Bundle prop = new Bundle();
        prop.putString("GMS", "" + status);
        prop.putString("from", from);
        mFirebaseAnalytics.logEvent("set_gms", prop);
    }

    public static void menuShare(Context context) {
        generalEvent("menu_share");
    }

    public static void menuSettings(Context context) {
        generalEvent("menu_settings");
    }

    public static void reportReferrer(Context context, String utm_source, String utm_medium, String utm_campaign,
                                      String utm_content, String utm_term, String gclid) {
        Bundle prop = new Bundle();
        prop.putString(ReferrerReceiver.UTM_CONTENT, utm_content == null? "" : utm_content);
        prop.putString(ReferrerReceiver.GCLID, gclid == null? "" : gclid);
        prop.putString(ReferrerReceiver.UTM_TERM, utm_term == null? "" : utm_term);
        prop.putString(ReferrerReceiver.UTM_SOURCE, utm_source == null? "" : utm_source);
        prop.putString(ReferrerReceiver.UTM_MEDIUM, utm_medium == null? "" : utm_medium);
        prop.putString(ReferrerReceiver.UTM_CAMPAIGN, utm_campaign == null? "" : utm_campaign);
        if (!TextUtils.isEmpty(utm_campaign)) {
            mFirebaseAnalytics.setUserProperty(PROP_CAMP, utm_campaign);
        }
        mFirebaseAnalytics.logEvent("install_referrer", prop);
    }

    public static void greyAttribute(Context context, String event, String pkg) {
        Bundle prop = new Bundle();
        prop.putString(event, pkg);
        mFirebaseAnalytics.logEvent("grey_attribute", prop);
    }

    public static void generalClickEvent(Context context, String event) {
        Bundle prop = new Bundle();
        prop.putString("event", event);
        mFirebaseAnalytics.logEvent("click_event", prop);
    }

    public static void reportActive(Context context, boolean fg){
        if (!PreferencesUtils.needReportActive(fg)) {
            return;
        }
        PreferencesUtils.updateActiveTime(fg);
        Bundle prop = new Bundle();
        prop.putString("fg", fg?"user":"service");
        prop.putString("locker", ""+ PreferencesUtils.isLockerEnabled(context));
        prop.putString("adfree", "" + PreferencesUtils.isAdFree());
        prop.putString("rated", "" + PreferencesUtils.isRated());
        prop.putString("channel", "" + PreferencesUtils.getInstallChannel());
        mFirebaseAnalytics.logEvent("track_active", prop);
    }

    public static void boostFrom(Context context, String from){
        Bundle prop = new Bundle();
        prop.putString("from", from);
        mFirebaseAnalytics.logEvent("boost_from", prop);
    }
}
