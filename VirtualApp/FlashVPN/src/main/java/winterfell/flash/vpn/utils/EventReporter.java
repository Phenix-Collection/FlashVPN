package winterfell.flash.vpn.utils;

/**
 * Created by doriscoco on 2017/4/4.
 */

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.polestar.task.ADErrorCode;

import winterfell.flash.vpn.FlashApp;

/**
 * Created by hxx on 8/2/16.
 */
public class EventReporter {
    public static final String PROP_REWARDED = "rewarded";
    public static final String REWARD_OPEN = "open";
    public static final String PROP_FAST_SWITCH = "fast_switch";
    public static final String REWARD_ACTIVE = "active";
    private static FirebaseAnalytics mFirebaseAnalytics;

    public static void init(Context context) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(FlashApp.getApp());
    }
    public static void setUserProperty(String name, String prop) {
        if(mFirebaseAnalytics != null) {
            mFirebaseAnalytics.setUserProperty(name, prop);
        }
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

    public static void reportConnectted(Context context, String name) {
        Bundle bundle = new Bundle();
        bundle.putString("server", name);
        mFirebaseAnalytics.logEvent("connectted", bundle);
    }

    public static void reportDisConnectted(Context context, String name) {
        Bundle bundle = new Bundle();
        bundle.putString("server", name);
        mFirebaseAnalytics.logEvent("disconnetted", bundle);
    }

    public static void reportConnect(Context context, String name) {
        Bundle bundle = new Bundle();
        bundle.putString("server", name);
        mFirebaseAnalytics.logEvent("connect", bundle);
    }

    public static void reportDisConnect(Context context, String name) {
        Bundle bundle = new Bundle();
        bundle.putString("server", name);
        mFirebaseAnalytics.logEvent("disconnect", bundle);
    }

    public static int getSpeedGrade(float speed) {
        if (speed > 100000) {
            return 10;
        } else {
            return ((int)(speed/10000));
        }
    }

    public static void reportNoPingBinary() {
        Bundle bundle = new Bundle();
        bundle.putString("event", "noping");
        mFirebaseAnalytics.logEvent("ping", bundle);
    }
    public static void reportPingLevel(String locale, boolean isConnectted, String serv, String pingTime) {
        Bundle bundle = new Bundle();
        String value = "" + isConnectted + "_" + locale + "_" + serv + "_" + pingTime;
        MLogs.d("EventReporter-- reportPingLevel:" + value);
        bundle.putString("event", value);
        mFirebaseAnalytics.logEvent("ping", bundle);
    }
    public static void reportPingFailed(int status) {
        Bundle bundle = new Bundle();
        bundle.putString("event", "pingfail_" + status);
        mFirebaseAnalytics.logEvent("ping", bundle);
    }

    public static void reportSpeed(Context context, String server, float avgDownload,
                                        float avgUpload, float maxDownload, float maxUpload) {
        Bundle bundle = new Bundle();
        bundle.putString("avgDownload", server + "_" + getSpeedGrade(avgDownload));
        bundle.putString("avgUpload", server + "_" + getSpeedGrade(avgUpload));

        bundle.putString("maxDownload", server + "_" + getSpeedGrade(maxDownload));
        bundle.putString("maxUpload", server + "_" + getSpeedGrade(maxUpload));
        mFirebaseAnalytics.logEvent("speed", bundle);
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

    public static void rewardEvent(String event) {
        Bundle prop = new Bundle();
        prop.putString("name", event);
        mFirebaseAnalytics.logEvent("reward_event", prop);
    }

    private static String sWakeSrc;
    public static void reportWake(Context context, String src){
        if (sWakeSrc == null && !TextUtils.isEmpty(src)) {
            sWakeSrc = src;
            Bundle prop = new Bundle();
            prop.putString("wake_src", src);
            mFirebaseAnalytics.logEvent("track_wake", prop);
        }
        MLogs.d("Wake from " + src + " original: " + sWakeSrc);
    }

    public static void reportEstablishTime(Context context, long timeInMilli) {
        long toreport = timeInMilli/1000;
        Bundle bundle = new Bundle();
        bundle.putLong("time", toreport);
        mFirebaseAnalytics.logEvent("establish_time", bundle);
    }

    public static void reportTunnelConnectTime(String locale, String address, long timeInMilli) {
        long toreport = timeInMilli/100;
        Bundle bundle = new Bundle();
        bundle.putString("time", locale + "_" + address + "_" + toreport);
        mFirebaseAnalytics.logEvent("tunnel_time", bundle);
    }

    public static void reportTunnelConnectFail(String locale, String address) {
        Bundle bundle = new Bundle();
        bundle.putString("fail", locale + "_" + address);
        mFirebaseAnalytics.logEvent("tunnel_c_fail", bundle);
    }

    private static void reportAwsEvent(String event) {
        Bundle bundle = new Bundle();
        bundle.putString("event", event);
        mFirebaseAnalytics.logEvent("aws", bundle);
    }

    public static void reportNoServer() {
        reportAwsEvent("no_server");
    }

    public static void reportGetPortFailed(String ip) {
        reportAwsEvent(ip + "get_port_failed");
    }

    public static void reportRleasePortFailed(String ip) {
        reportAwsEvent(ip + "release_port_failed");
    }

    public static void reportRleasePortSucceed(String ip) {
        reportAwsEvent(ip + "release_port_succeeded");
    }

    public static void reportCheckServerFailed(String ip) {
        reportAwsEvent(ip + "check_port_failed");
    }

    public static void reportAderror(ADErrorCode adErrorCode) {
        reportAwsEvent("api_error_" + adErrorCode.getErrCode());
    }

    public static void reportReleasing() {
        reportAwsEvent("releasing");
    }

    public static void reportFailedToAddProxy() {
        reportAwsEvent("add_port_failed");
    }

    public static void taskEvent(long id, int code) {
        Bundle prop = new Bundle();
        prop.putString("name", ""+id+"_"+code);
        mFirebaseAnalytics.logEvent("task_event", prop);
    }

    public static void productEvent(String event) {
        Bundle prop = new Bundle();
        prop.putString("name", event);
        mFirebaseAnalytics.logEvent("product_event", prop);
    }
}
