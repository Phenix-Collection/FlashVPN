package com.polestar.grey;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.polestar.ad.AdLog;

/**
 * Created by guojia on 2018/1/1.
 */

public class GreyAttribute {
    private static final String TAG = "GreyAttribute";
    public static final String ACTION_CLICK = "act_click";
    public static final String ACTION_ATTRIBUTE = "act_attribute";
    //TODO get from remote config

    public static boolean putReferrer(Context context , String pkg, String value) {
        SharedPreferences settings = context.getSharedPreferences("refer", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(pkg, value);
        return editor.commit();
    }

    public static String getReferrer(Context context , String pkg ) {
        SharedPreferences settings = context.getSharedPreferences("refer", Context.MODE_PRIVATE);
        return settings.getString(pkg, null);
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
    public static boolean sendAttributor(final Context ctx,String pkg) {
        String refer = getReferrer(ctx, pkg);
        if (!TextUtils.isEmpty(refer)) {
            Intent intent = new Intent(ctx, GreyAttributeService.class);
            intent.putExtra(Intent.EXTRA_PACKAGE_NAME, pkg);
            intent.setAction(ACTION_ATTRIBUTE);
            ctx.startService(intent);
            return true;
        }
        return false;
    }
}
