package com.polestar.superclone.utils;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.polestar.superclone.MApp;
import com.polestar.superclone.component.receiver.ReferrerReceiver;
import com.polestar.superclone.constant.AppConstants;
import com.polestar.superclone.reward.ShareActions;
import com.polestar.superclone.reward.TaskPreference;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

import java.net.URLDecoder;

/**
 * Created by hxx on 8/2/16.
 */
public class EventReporter {

    private static FirebaseAnalytics mFirebaseAnalytics;

    public static final String PROP_CHANNEL = "channel";
    public static final String PROP_CAMP = "campaign";
    public static final String PROP_SOURCE = "source";
    public static final String PROP_MEDIUM = "medium";
    public static final String PROP_PERMISSION = "granted_permission";
    public static final String PROP_ADFREE = "adfree";
    public static final String PROP_GMS = "gms";
    public static final String PROP_REFERRED = "referred";
    public static final String PROP_REWARDED = "rewarded";
    public static final String REWARD_OPEN = "open";
    public static final String REWARD_ACTIVE = "active";

    public static void init(Context context) {
        //StatConfig.asyncInit(context);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(MApp.getApp());
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        String referChannel = PreferencesUtils.getInstallChannel();
        mFirebaseAnalytics.setUserProperty(PROP_CHANNEL, channel);
        MLogs.e("MTA channel: " + channel + " refer: " + referChannel);
        int referStatus = getReferrerStatus();
        if (referStatus != REFERRER_STATUS_API || referStatus != REFERRER_STATUS_API_FAIL) {
            initReferFromApi();
        }
    }

    public class KeyLogTag {
        public static final String AERROR = "aerror";
    }

    public static void setUserProperty(String name, String prop) {
        if(mFirebaseAnalytics != null) {
            mFirebaseAnalytics.setUserProperty(name, prop);
        }
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

    public static void rewardEvent(String event) {
        Bundle prop = new Bundle();
        prop.putString("name", event);
        mFirebaseAnalytics.logEvent("reward_event", prop);
    }

    public static void productEvent(String event) {
        Bundle prop = new Bundle();
        prop.putString("name", event);
        mFirebaseAnalytics.logEvent("product_event", prop);
    }

    public static void taskEvent(long id, int code) {
        Bundle prop = new Bundle();
        prop.putString("name", ""+id+"_"+code);
        mFirebaseAnalytics.logEvent("task_event", prop);
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

    public static void generalClickEvent(Context context, String event) {
        Bundle prop = new Bundle();
        prop.putString("event", event);
        mFirebaseAnalytics.logEvent("click_event", prop);
    }

    public static void reportActive(Context context, boolean fg, String action){
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
        prop.putString("action", action);
        mFirebaseAnalytics.logEvent("track_active", prop);
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

    public static void boostFrom(Context context, String from){
        Bundle prop = new Bundle();
        prop.putString("from", from);
        mFirebaseAnalytics.logEvent("boost_from", prop);
    }

    private static final String UTM_SOURCE = "utm_source";
    private static final String UTM_MEDIUM = "utm_medium";
    private static final String UTM_CAMPAIGN = "utm_campaign";
    private static final String UTM_TERM = "utm_term";
    private static final String UTM_CONTENT = "utm_content";
    private static final String GCLID = "gclid";
    private static final String CTIT = "ctit";
    private static final String CTAT = "ctat";
    public static final String REFERRER_TYPE_BROADCAST = "br";
    private static final String REFERRER_TYPE_API = "api";
    private static final int REFERRER_STATUS_NO = 0;
    private static final int REFERRER_STATUS_BROADCAST = 1;
    private static final int REFERRER_STATUS_API = 2;
    private static final int REFERRER_STATUS_API_FAIL = 3;


    private static void reportReferrer(String type, String referrer, long ctit, long ctat ){
        try {
            if(REFERRER_TYPE_API.equals(type)){
                updateReferrerStatus(REFERRER_STATUS_API);
            }else if(REFERRER_TYPE_BROADCAST.equals(type)) {
                updateReferrerStatus(REFERRER_STATUS_BROADCAST);
            }
            String referUrl = URLDecoder.decode(referrer, "UTF-8");
            String[] parms = referUrl.split("&");
            if (parms == null || parms.length == 0) {
                return;
            }
            String utm_source = null, utm_medium = null, utm_campaign = null, utm_term = null, utm_content = null, gclid = null;
            for (String s : parms) {
                String arr[] = s.split("=");
                if (arr == null || arr.length != 2) {
                    continue;
                }
                MLogs.d(arr[0], " " + URLDecoder.decode(arr[1], "UTF-8"));
                switch (arr[0]) {
                    case UTM_CAMPAIGN:
                        utm_campaign = URLDecoder.decode(arr[1], "UTF-8");
                        break;
                    case GCLID:
                        gclid = URLDecoder.decode(arr[1], "UTF-8");
                        break;
                    case UTM_SOURCE:
                        utm_source = URLDecoder.decode(arr[1], "UTF-8");
                        break;
                    case UTM_TERM:
                        utm_term = URLDecoder.decode(arr[1], "UTF-8");
                        break;
                    case UTM_MEDIUM:
                        utm_medium = URLDecoder.decode(arr[1], "UTF-8");
                        break;
                    case UTM_CONTENT:
                        utm_content = URLDecoder.decode(arr[1], "UTF-8");
                        break;
                }
            }

            MLogs.d("Receive refer: " + referrer + " utm_source : " + utm_source);
            if (!TextUtils.isEmpty(utm_source)) {
                PreferencesUtils.setInstallChannel(utm_source);
                CrashReport.setAppChannel(MApp.getApp(), utm_source);
                Bundle prop = new Bundle();
                prop.putString(UTM_CONTENT, utm_content == null? "" : type+"_"+utm_content);
                prop.putString(GCLID, gclid == null? "" : type+"_"+gclid);
                prop.putString(UTM_TERM, utm_term == null? "" : type+"_"+utm_term);
                prop.putString(UTM_SOURCE, utm_source == null? "" : type+"_"+utm_source);
                prop.putString(UTM_MEDIUM, utm_medium == null? "" : type+"_"+utm_medium);
                prop.putString(UTM_CAMPAIGN, utm_campaign == null? "" : type+"_"+utm_campaign);
                prop.putString(UTM_CAMPAIGN, utm_campaign == null? "" : type+"_"+utm_campaign);
                prop.putLong(CTIT, ctit);
                prop.putLong(CTAT, ctat);
                if (!TextUtils.isEmpty(utm_campaign)) {
                    mFirebaseAnalytics.setUserProperty(PROP_CAMP, utm_campaign);
                }
                if (!TextUtils.isEmpty(utm_source)) {
                    mFirebaseAnalytics.setUserProperty(PROP_SOURCE, utm_source);
                }
                if (!TextUtils.isEmpty(utm_medium)) {
                    mFirebaseAnalytics.setUserProperty(PROP_MEDIUM, utm_medium);
                }
                if (utm_source.equals(ShareActions.SOURCE_USER_SHARE)) {
                    if (!TextUtils.isEmpty(utm_content)) {
                        EventReporter.setUserProperty(EventReporter.PROP_REFERRED, "true");
                        TaskPreference.setReferrerHint(utm_content);
                    }
                }
                mFirebaseAnalytics.logEvent("install_referrer", prop);
            }
        }catch (Exception ex) {

        }
    }

    private static void updateReferrerStatus(int s) {
        PreferencesUtils.putInt(MApp.getApp(),"referrer_status", s);
    }

    private static int getReferrerStatus() {
        return PreferencesUtils.getInt(MApp.getApp(),"referrer_status", REFERRER_STATUS_NO);
    }

    public static void reportReferrer(String type, String referrer) {
        reportReferrer(type, referrer, 0, 0);
    }

    private static void initReferFromApi() {
        InstallReferrerClient referrerClient;
        referrerClient = InstallReferrerClient.newBuilder(MApp.getApp()).build();
        try {
            referrerClient.startConnection(new InstallReferrerStateListener() {
                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {
                    switch (responseCode) {
                        case InstallReferrerClient.InstallReferrerResponse.OK:
                            // Connection established
                            try {
                                ReferrerDetails response = referrerClient.getInstallReferrer();
                                long clickTime = response.getReferrerClickTimestampSeconds();
                                long installStartTime = response.getInstallBeginTimestampSeconds();
                                MLogs.d("Refer got: " + response);
                                long now = System.currentTimeMillis() / 1000;
                                reportReferrer(REFERRER_TYPE_API, response.getInstallReferrer(), installStartTime - clickTime, now - clickTime);
                            } catch (Exception ex) {

                            }
                            referrerClient.endConnection();
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                            // API not available on the current Play Store app
                            MLogs.d("Refer API FEATURE_NOT_SUPPORTED");
                            updateReferrerStatus(REFERRER_STATUS_API_FAIL);
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                            updateReferrerStatus(REFERRER_STATUS_API_FAIL);
                            MLogs.d("Refer API SERVICE_UNAVAILABLE");
                            // Connection could not be established
                            break;
                    }
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            });
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
