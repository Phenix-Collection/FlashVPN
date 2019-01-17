package com.polestar.superclone.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.polestar.clone.BitmapUtils;
import com.polestar.superclone.MApp;
import com.polestar.superclone.R;
import com.polestar.superclone.component.activity.AppStartActivity;
import com.polestar.superclone.constant.AppConstants;
import com.polestar.superclone.model.AppModel;
import com.polestar.clone.CustomizeAppData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * Created by yxx on 2016/7/21.
 */
public class CommonUtils {
    public static void gotoHomeScreen() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        MApp.getApp().startActivity(intent);
    }
    public static void createShortCut(Context context, AppModel appModel) {
        Bitmap iconBitmap = BitmapUtils.getCustomIcon(context, appModel.getPackageName(), appModel.getPkgUserId());
        CustomizeAppData data = CustomizeAppData.loadFromPref(appModel.getPackageName(), appModel.getPkgUserId());
        String appName = data.label;
        Intent actionIntent = new Intent(Intent.ACTION_DEFAULT);
        actionIntent.setClassName(context.getPackageName(), AppStartActivity.class.getName());
        actionIntent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, appModel.getPackageName());
        actionIntent.putExtra(AppConstants.EXTRA_FROM, AppConstants.VALUE_FROM_SHORTCUT);
        actionIntent.putExtra(AppConstants.EXTRA_CLONED_APP_USERID, appModel.getPkgUserId());
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        createShortcut(context, actionIntent, appName, appModel.getPackageName(), appModel.getPkgUserId(), false, iconBitmap);
//        iconBitmap.recycle();
    }

    public static void removeShortCut(Context context, AppModel appModel) {
        String appName = appModel.getName();
        Intent actionIntent = new Intent(Intent.ACTION_DEFAULT);
        actionIntent.setClassName(context.getPackageName(), AppStartActivity.class.getName());
        actionIntent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, appModel.getPackageName());
        actionIntent.putExtra(AppConstants.EXTRA_FROM, AppConstants.VALUE_FROM_SHORTCUT);
        actionIntent.putExtra(AppConstants.EXTRA_CLONED_APP_USERID, appModel.getPkgUserId());
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        removeShortcut(context, actionIntent, appName, appModel.getPackageName(), appModel.getPkgUserId());
    }

    private static String getIconId(String pkg, int userId){
        return pkg+"_"+userId;
    }

    public static void createShortcut(Context context, Intent actionIntent, String name, String pkg, int userId,
                                      boolean allowRepeat, Bitmap iconBitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            if (shortcutManager.isRequestPinShortcutSupported()) {
                ShortcutInfo shortcut = new ShortcutInfo.Builder(context, getIconId(pkg, userId))
                        .setShortLabel(name)
                        .setLongLabel(name)
                        .setIcon(Icon.createWithBitmap(iconBitmap))
                        .setIntent(actionIntent)
                        .build();
                try {
                    shortcutManager.requestPinShortcut(shortcut, null);
                }catch (Exception ex){
                    MLogs.logBug(ex.getMessage());
                    try{
                        shortcutManager.enableShortcuts(Arrays.asList(getIconId(pkg, userId)));
                    }catch (Exception ex2){
                        MLogs.logBug(ex2.getMessage());
                    }
                }
                return;
            }
        }

        Intent addShortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        // 是否允许重复创建
        addShortcutIntent.putExtra("duplicate", allowRepeat);
        // 快捷方式的标题
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        // 快捷方式的图标
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconBitmap);
        // 快捷方式的动作
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, actionIntent);
        context.sendBroadcast(addShortcutIntent);
    }

    public static void removeShortcut(Context context, Intent actionIntent, String name, String pkg, int userId) {
        Intent intent = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra("duplicate", false);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, actionIntent);
        context.sendBroadcast(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            if (shortcutManager.isRequestPinShortcutSupported()) {
                try{
                    shortcutManager.disableShortcuts(Arrays.asList(getIconId(pkg, userId)));
                }catch (Exception ex){
                    MLogs.logBug(ex.getMessage());
                }
            }
        }
    }

    public static boolean isArab(Context c) {
        Locale locale = c.getResources().getConfiguration().locale;
        return locale.getLanguage().equalsIgnoreCase("ar");
    }

    private static boolean hasShortcut(Context context, String appName) {
        boolean isInstallShortcut = false;
        final ContentResolver cr = context.getContentResolver();
        final String AUTHORITY = "com.android.launcher.settings";
        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
        Cursor c = cr.query(CONTENT_URI, new String[]{"title", "iconResource"}, "title=?",
                new String[]{appName}, null);
        if (c != null && c.getCount() > 0) {
            isInstallShortcut = true;
        }
        return isInstallShortcut;
    }

    public static void jumpToUrl(Context context, String url){
        if (!TextUtils.isEmpty(url)) {
            Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
            context.startActivity(viewIntent);
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

    public static void shareWithFriends(Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String appName = context.getResources().getString(R.string.app_name);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName);
        String shareContent = context.getResources().getString(R.string.share_with_friends_tip, appName);
        shareContent = shareContent + "https://play.google.com/store/apps/details?id="
                + context.getPackageName() + "&referrer=utm_source%3Duser_share";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        context.startActivity(Intent.createChooser(shareIntent, context.getResources().getText(R.string.share_with_friends)));
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            return context.getPackageManager().getPackageInfo(packageName, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            MLogs.e(packageName + " isn't installed.");
            return false;
        }
    }

    public static ActivityManager.RunningTaskInfo getTopTask(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && !tasks.isEmpty()) {
            return tasks.get(0);
        }
        return null;
    }

    public static ActivityManager.RunningAppProcessInfo getForegroundProcess(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> pros = am.getRunningAppProcesses();
        if (pros != null) {
            for (ActivityManager.RunningAppProcessInfo info : pros) {
                if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return info;
                }
            }
        }
        return null;
    }

    public static boolean isTopActivity(Context context, ComponentName componentName) {
        if (context == null || componentName == null) {
            return false;
        }
        ActivityManager.RunningTaskInfo topTask = getTopTask(context);
        if (topTask != null) {
            ComponentName topActivity = topTask.topActivity;
            if (topActivity.getPackageName().equals(componentName.getPackageName()) &&
                    topActivity.getClassName().equals(componentName.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context ctx) {

        ConnectivityManager manager = (ConnectivityManager) ctx
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        return !(networkinfo == null || !networkinfo.isAvailable());
    }


    public static long getCurrentVersionCode(Context context) {
        long version = 0;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String getCurrentVersionName(Context context) {
        String version = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String getMetaDataInApplicationTag(Context context,String key){
        ApplicationInfo appInfo = null;
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

    public static boolean isSamsungDevice() {
        final String manufacturer = Build.MANUFACTURER.toLowerCase();
        if (manufacturer.equals("samsung")) {
            return true;
        }
        return false;
    }

    private static HashSet<String> SOCIAL_APP_LIST ;
    private static final String CONF_SOCIAL_APP = "conf_social_app_list";

    public static boolean isSocialApp(String pkg) {
        if (SOCIAL_APP_LIST == null) {
            SOCIAL_APP_LIST = new HashSet<>();
            String conf = RemoteConfig.getString(CONF_SOCIAL_APP);
            String[] arr = conf.split(":");
            for (String s:arr) {
                SOCIAL_APP_LIST.add(s);
            }
        }
        return SOCIAL_APP_LIST.contains(pkg);
    }

    public static boolean isWiFiActive(Context inContext) {
        Context context = inContext.getApplicationContext();
        ConnectivityManager connectivity = (ConnectivityManager) context  .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getTypeName().equalsIgnoreCase("WIFI") && info[i].isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Drawable getAppIcon(String packageName) {
        PackageManager pm = MApp.getApp().getPackageManager();
        try {
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
