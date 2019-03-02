package com.polestar.domultiple.utils;

/**
 * Created by doriscoco on 2017/4/4.
 */

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.polestar.domultiple.PolestarApp;
import com.tencent.bugly.crashreport.CrashReport;

import java.net.URLDecoder;
import java.util.Properties;

/**
 * Created by PolestarApp on 8/2/16.
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
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(PolestarApp.getApp());
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        mFirebaseAnalytics.setUserProperty("channel", channel);
        mFirebaseAnalytics.setUserProperty("lite_mode", String.valueOf(PreferencesUtils.isLiteMode()));
        mFirebaseAnalytics.setUserProperty("adfree", String.valueOf(PreferencesUtils.isAdFree()));
        MLogs.e("MTA channel: " + channel);
        int referStatus = getReferrerStatus();
        if (referStatus != REFERRER_STATUS_API || referStatus != REFERRER_STATUS_API_FAIL) {
            initReferFromApi();
        }
    }

    public static void setUserProperty(String name, String prop) {
        if(mFirebaseAnalytics != null) {
            mFirebaseAnalytics.setUserProperty(name, prop);
        }
    }

    public class KeyLogTag {
        public static final String AERROR = "aerror";
    }

    public static void keyLog(Context context, String tag, String log) {
    }

    public static void homeShow() {
        mFirebaseAnalytics.logEvent("home_show", null);
    }

    public static void appStart(boolean coldStart, boolean locker, String from, String pkg, int userId) {
        Bundle bundle = new Bundle();
        bundle.putString("package", pkg);
        bundle.putString("coldStart", ""+coldStart);
        bundle.putString("locker", ""+locker);
        bundle.putString("from", from);
        bundle.putString("userId", ""+userId);
        mFirebaseAnalytics.logEvent("app_start", bundle);
    }

    public static void reportCrash(Throwable ex, String packageName, boolean forground) {
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

    public static void reportArm64(String pkg, String status) {
        Bundle bundle = new Bundle();
        bundle.putString("status", status+ "_" + pkg);
        mFirebaseAnalytics.logEvent("arm64", bundle);
    }

    public static void generalEvent(String event) {
        Bundle bundle = new Bundle();
        bundle.putString("event", event);
        mFirebaseAnalytics.logEvent("general_event", bundle);
    }

    public static void luckyClick(String from) {
        Bundle bundle = new Bundle();
        bundle.putString("from", from);
        mFirebaseAnalytics.logEvent("lucky_click", bundle);
    }

    public static void quickSwitchSetting(boolean enable) {
        Bundle bundle = new Bundle();
        bundle.putString("status", ""+enable);
        mFirebaseAnalytics.logEvent("quick_switch", bundle);
    }

    public static void reportsAdsLaunch(String name) {
        Bundle bundle = new Bundle();
        bundle.putString("name", ""+name);
        mFirebaseAnalytics.logEvent("ads_launch", bundle);
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

    public static void reportReferrer(String type, String referrer) {
        reportReferrer(type, referrer, 0, 0);
    }

    private static void initReferFromApi() {
        InstallReferrerClient referrerClient;
        referrerClient = InstallReferrerClient.newBuilder(PolestarApp.getApp()).build();
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
        }catch (Exception ex) {
            ex.printStackTrace();
        }
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
                CrashReport.setAppChannel(PolestarApp.getApp(), utm_source);
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
                if (utm_source.contains("user_share")) {
                    if (!TextUtils.isEmpty(utm_content)) {
                        EventReporter.setUserProperty(EventReporter.PROP_REFERRED, "true");
                    }
                }
                mFirebaseAnalytics.logEvent("install_referrer", prop);
            }
        }catch (Exception ex) {

        }
    }

    private static void updateReferrerStatus(int s) {
        PreferencesUtils.putInt(PolestarApp.getApp(),"referrer_status", s);
    }

    private static int getReferrerStatus() {
        return PreferencesUtils.getInt(PolestarApp.getApp(),"referrer_status", REFERRER_STATUS_NO);
    }
}
