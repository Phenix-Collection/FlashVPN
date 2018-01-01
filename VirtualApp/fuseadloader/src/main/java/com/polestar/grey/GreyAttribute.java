package com.polestar.grey;

import android.content.Context;

import nativesdk.ad.common.utils.DeviceUtil;

/**
 * Created by guojia on 2018/1/1.
 */

public class GreyAttribute {

    //TODO get from remote config
    private final static String FETCH_URL =
            "http://api.c.avazunativeads.com/appwall?sourceid=29026&adpkg={adpkg}&req_type=3&market=google"
                    +"&deviceid={devId}&sdkversion=2.2.7.092217&pkg={mypkg}&ua={ua}&os=android&language={lang}&" +
                    "reqId=%s&maid={maid}&gpid={gpid}";
    //fetch ad and do click, and get referrer
    public static void checkAndClick(String pkg) {
        String adpkg = pkg;
        //String devId = DeviceUtil.getDeviceId()
        //String deviceId =

    }

    //send referrer to package
    public static void sendAttributor(String pkg) {

    }
}
