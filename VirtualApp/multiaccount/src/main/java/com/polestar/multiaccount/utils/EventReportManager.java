package com.polestar.multiaccount.utils;

import android.app.Activity;
import android.content.Context;

import com.polestar.multiaccount.constant.Constants;

/**
 * Created by hxx on 8/3/16.
 */
public class EventReportManager {

    private static final String COMMON_EVENT = "common_event";

    public static void init(Context context) {

        String trafficId = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_TID");
        String channel = CommonUtils.getMetaDataInApplicationTag(context, "CHANNEL_NAME");
        String installChannel = channel;
        String referrer = null;
        String googleAdId = null;
        String source = "1"; //安装来源    0 未知来源    1 google play

        Logs.e("Analytics channel:" + channel + ", tid:" + trafficId);
    }


    // category:"normal event", action:"event id", lable:"event parameter name", value:"event parameter value"
    public static void sendEvent(Context context, String category, String action, String label, String value, String extra) {
        Logs.e("[EventReport] sendEvent: category:" + category + ", action:" + action + ", label:" + label + ", value:" + value);
    }

    // 上报日活
    public static void reportActive(Context context) {
        Logs.e("[EventReport] reportActive");
    }

/* This is a demo:

    {
        // 初始化
        //String url = "http://192.168.5.222:11011";
        String url = "http://st.yellowbooster.info"; //找运维要？如果测试，发送到测试服务器，找刘达

        String referrer = "gp广播返回的 referrer";
        String trafficId = "trafficId 流量跟踪的id";//公司分配的
        String channel = "gp包渠道"; //dev? play? 现在不用了？
        String installChannel = "安装渠道";  //和channel值一样？
        String googleAdId = "google 广告id";
        String source = "1 or 0"; //安装来源    0 未知来源    1 google play
        EventReport.Configuration.Builder builder = new EventReport.Configuration.Builder();
        builder.setUrl(url);
        builder.setReferrer(referrer);
        builder.setTrafficId(trafficId);
        builder.setChannel(channel);
        builder.setInstallChannel(installChannel);
        builder.setSource(source);
        builder.setGoogleAdId(googleAdId);
        EventReport.getInstance(context).init(builder.build());

        // 发送事件
        EventReport.getInstance(this).sendEvent("cat", "act", "lab", "0", "test");

        EventReport.getInstance(this).reportActive();//一天无论调用多少次只上报一次。日活
    }*/


    public static void homeAdd(Context context) {
        sendEvent(context, COMMON_EVENT, "home_add", null, null, null);
    }

    public static void applistClone(Context context, String packageName) {
        sendEvent(context, COMMON_EVENT, "applist_clone", "package", packageName, null);
    }

    public static void deleteClonedApp(Context context, String packageName) {
        sendEvent(context, COMMON_EVENT, "home_delete", "package", packageName, null);
    }

    public static void launchApp(Context context, String packageName, String from) {
        if (from.equals(Constants.VALUE_FROM_HOME)) {
            sendEvent(context, COMMON_EVENT, "home_launch", "package", packageName, null);
        } else if (from.equals(Constants.VALUE_FROM_SHORTCUT)) {
            sendEvent(context, COMMON_EVENT, "mobile_launch", "package", packageName, null);
        }
    }

    public static void addShortCut(Context context, String packageName) {
        sendEvent(context, COMMON_EVENT, "home_shortcut", "package", packageName, null);
    }

    public static void reportCrash(Context context, String packageName) {
        sendEvent(context, COMMON_EVENT, "app_crash", "package", packageName, null);
    }

    public static void homeMenu(Context context) {
        sendEvent(context, COMMON_EVENT, "home_menu", null, null, null);
    }

    public static void menuNotification(Context context) {
        sendEvent(context, COMMON_EVENT, "menu_notifications", null, null, null);
    }

    public static void menuFAQ(Context context) {
        sendEvent(context, COMMON_EVENT, "menu_faq", null, null, null);
    }

    public static void menuFeedback(Context context) {
        sendEvent(context, COMMON_EVENT, "menu_feedback", null, null, null);
    }

    public static void menuRate(Context context) {
        sendEvent(context, COMMON_EVENT, "menu_rate", null, null, null);
    }

    public static void menuShare(Context context) {
        sendEvent(context, COMMON_EVENT, "menu_share", null, null, null);
    }

    public static void menuSettings(Context context) {
        sendEvent(context, COMMON_EVENT, "menu_settings", null, null, null);
    }

    public static void onStart(Activity activity) {
        Logs.e("[EventReport] onStart");
        onStart(activity, null);
    }

    public static void onStart(Activity activity, String ext) {
        Logs.e("[EventReport] onStart: ext: " + ext);
    }

    public static void onStop(Activity activity) {
        Logs.e("[EventReport] onStop");
    }

}
