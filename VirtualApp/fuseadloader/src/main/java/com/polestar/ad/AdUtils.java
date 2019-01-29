package com.polestar.ad;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.util.List;
import java.util.Random;
import java.util.UUID;


/**
 * Created by guojia on 2016/12/11.
 */

public class AdUtils {
//
//    public static void preloadAppWall(String WALL_UNIT_ID) {
//        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
//        Map<String,Object> preloadMap = new HashMap<String,Object>();
//        preloadMap.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_APPWALL);
//        preloadMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, WALL_UNIT_ID);
//        sdk.preload(preloadMap);
//    }
//
//    public static void initMVSDK(String id, String key, Context context) {
//        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
//        // test appId and appKey
//        String appId = "33047";
//        String appKey = "e4a6e0bf98078d3fa81ca6d315c28123";
//        Map<String, String> map = sdk.getMVConfigurationMap(appId, appKey);
//
//        // if you modify applicationId, please add the following attributes,
//        // otherwise it will crash
//        // map.put(MobVistaConstans.PACKAGE_NAME_MANIFEST, "your AndroidManifest
//        // package value");
//        sdk.init(map, context);
//    }

    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static String getAndroidID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getDeviceID(Context context) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();
            return imei;
        } else {
            return null;
        }
    }


    private static void setGaid(Context var0, String var1) {
        SharedPreferences var2 = var0.getSharedPreferences("sdk_preference", 0);
        var2.edit().putString("gaid", var1).apply();
    }

    private static String getGaid(Context var0) {
        SharedPreferences var1 = var0.getSharedPreferences("sdk_preference", 0);
        return var1.getString("gaid", "");
    }

    public static final String getGoogleAdvertisingId(Context var0) {
        if (var0 == null) {
            return "";
        } else {
            String var1 = getGaid(var0);
            if (!TextUtils.isEmpty(var1)) {
                return var1;
            } else {
                try {
                    String var2 = AdvertisingIdClient.getAdvertisingIdInfo(var0).getId();
                    if (!TextUtils.isEmpty(var2)) {
                        setGaid(var0, var2);
                        return var2;
                    }
                } catch (Throwable var3) {
                    var3.printStackTrace();
                }

                return "";
            }
        }
    }
}
