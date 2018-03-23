package com.polestar.minesweeperclassic.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

/**
 * Created by doriscoco on 2017/4/4.
 */

public class CommonUtils {
    public static String getMetaDataInApplicationTag(Context context, String key){
        ApplicationInfo appInfo ;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            if(appInfo.metaData != null){
                String value = appInfo.metaData.getString(key);
                if (value == null) {
                    value = String.valueOf(appInfo.metaData.getInt(key));
                }
                return value;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getInstallTime(Context context, String pkg) {
        try {
            PackageManager packageManager=context.getPackageManager();
            PackageInfo packageInfo=packageManager.getPackageInfo(pkg, 0);
            return packageInfo.firstInstallTime;//应用第一次安装的时间
        } catch (Exception e) {
            MLogs.logBug(MLogs.getStackTraceString(e));
            return System.currentTimeMillis();
        }
    }

    public static void jumpToMarket(Context context, String packageName) {
        try {
            Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + packageName));
//            viewIntent.putExtra("START_OUTTER_APP_FLAG", true);
            context.startActivity(viewIntent);
        } catch (Exception e) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
//            i.putExtra("START_OUTTER_APP_FLAG", true);
            context.startActivity(i);
        }
    }
}
