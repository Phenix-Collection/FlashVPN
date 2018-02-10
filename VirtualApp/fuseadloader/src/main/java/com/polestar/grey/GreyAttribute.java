package com.polestar.grey;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

import com.polestar.ad.AdLog;

import java.util.Random;

/**
 * Created by guojia on 2018/1/1.
 */

public class GreyAttribute {
    private static final String TAG = "GreyAttribute";
    public static final String ACTION_CLICK = "act_click";
    public static final String ACTION_ATTRIBUTE = "act_attribute";
    //TODO get from remote config

    public static boolean putReferrer(Context context , String pkg, String value ) {
        SharedPreferences settings = context.getSharedPreferences(pkg, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("rf", value);
        editor.putLong("click", System.currentTimeMillis());
        //editor.putLong("install", System.currentTimeMillis() - 25*1000 - randomClick);
        return editor.commit();
    }

    public static String getReferrer(Context context , String pkg ) {
        SharedPreferences settings = context.getSharedPreferences(pkg, Context.MODE_PRIVATE);
        return settings.getString("rf", null);
    }

    public static long getClickTimeStamp(Context context , String pkg ) {
        SharedPreferences settings = context.getSharedPreferences(pkg, Context.MODE_PRIVATE);
        return settings.getLong("click", 0);
    }

    //fetch ad and do click, and get referrer
    public static void checkAndClick(final Context ctx, final String pkg) {
        AdLog.d(TAG, "checkAndClick");
        Intent intent = new Intent(ctx, GreyAttributeService.class);
        intent.putExtra(Intent.EXTRA_PACKAGE_NAME, pkg);
        intent.setAction(ACTION_CLICK);
        ctx.startService(intent);
    }
    //send referrer to package
    public static void sendAttributor(final Context ctx,String pkg) {

        Intent intent = new Intent(ctx, GreyAttributeService.class);
        intent.putExtra(Intent.EXTRA_PACKAGE_NAME, pkg);
        intent.setAction(ACTION_ATTRIBUTE);
        ctx.startService(intent);
    }
}
