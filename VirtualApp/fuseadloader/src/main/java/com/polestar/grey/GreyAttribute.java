package com.polestar.grey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

import com.polestar.ad.AdLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by guojia on 2018/1/1.
 */

public class GreyAttribute {
    private static final String TAG = "GreyAttribute";
    public static final String ACTION_CLICK = "act_click";
    public static final String ACTION_ATTRIBUTE = "act_attribute";
    public static final String ACTION_GET_PACKAGES = "get_packages";

    public static final String ACTION_PACKAGE_READY = "act_pkg_ready";
    public static final String EXTRA_PACKAGE_LIST = "extra_pkg_list";
    public static final String EXTRA_PACKAGE_DESC_LIST = "extra_pkg_desc_list";
    //TODO get from remote config;

    public static void init(String source) {
        GreyAttributeService.init(source);

    }

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
        try {
            Intent intent = new Intent(ctx, GreyAttributeService.class);
            intent.putExtra(Intent.EXTRA_PACKAGE_NAME, pkg);
            intent.setAction(ACTION_CLICK);
            ctx.startService(intent);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //send referrer to package
    public static void sendAttributor(final Context ctx,String pkg) {
        try {
            Intent intent = new Intent(ctx, GreyAttributeService.class);
            intent.putExtra(Intent.EXTRA_PACKAGE_NAME, pkg);
            intent.setAction(ACTION_ATTRIBUTE);
            ctx.startService(intent);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public interface IAdPackageLoadCallback {
        void onAdPackageListReady(List<String> packages, List<String> des);
    }
    public static void getAdPackages(final Context ctx, final IAdPackageLoadCallback cb, ArrayList<String> availList) {
        if (ctx == null || cb == null) {
            return;
        }
        try {
            Intent intent = new Intent(ctx, GreyAttributeService.class);
            intent.setAction(ACTION_GET_PACKAGES);
            intent.putStringArrayListExtra(EXTRA_PACKAGE_LIST, availList);
            ctx.startService(intent);

            IntentFilter filter = new IntentFilter(ACTION_PACKAGE_READY);
            ctx.getApplicationContext().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ArrayList<String> list = intent.getStringArrayListExtra(EXTRA_PACKAGE_LIST);
                    ArrayList<String> desclist = intent.getStringArrayListExtra(EXTRA_PACKAGE_DESC_LIST);
                    if (list != null && desclist != null) {
                        cb.onAdPackageListReady(list, desclist);
                    }
                    ctx.getApplicationContext().unregisterReceiver(this);
                }
            }, filter);
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
