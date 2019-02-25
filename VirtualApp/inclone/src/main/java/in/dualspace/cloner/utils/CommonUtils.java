package in.dualspace.cloner.utils;

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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;

import in.dualspace.cloner.AppConstants;
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.R;
import in.dualspace.cloner.components.ui.AppLoadingActivity;
import in.dualspace.cloner.components.ui.MainActivity;
import in.dualspace.cloner.db.CloneModel;
import com.polestar.clone.CustomizeAppData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * Created by DualApp on 2016/7/21.
 */
public class CommonUtils {
    public static void gotoHomeScreen() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        DualApp.getApp().startActivity(intent);
    }

    public static void createLaunchShortcut(Context context){
        MLogs.d("create shortcut");
        Intent extra = new Intent(context.getApplicationContext(), MainActivity.class);
        extra.putExtra(AppConstants.EXTRA_FROM, AppConstants.FROM_SHORTCUT);
        extra.setAction(Intent.ACTION_MAIN);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
//            if (shortcutManager.isRequestPinShortcutSupported()) {
//
//                ShortcutInfo shortcut = new ShortcutInfo.Builder(context, getIconId(context.getPackageName(), 0))
//                        .setShortLabel(context.getString(R.string.app_name))
//                        .setLongLabel(context.getString(R.string.app_name))
//                        .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
//                        .setIntent(extra)
//                        .build();
//                try {
//                    shortcutManager.requestPinShortcut(shortcut, null);
//                }catch (Exception ex){
//                    MLogs.logBug(ex.getMessage());
//                    try{
//                        shortcutManager.enableShortcuts(Arrays.asList(getIconId(context.getPackageName(), 0)));
//                    }catch (Exception ex2){
//                        MLogs.logBug(ex2.getMessage());
//                    }
//                }
//                return;
//            }
//        }
            Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            //不允许重复创建
            shortcutintent.putExtra("duplicate", false);
            //需要现实的名称
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name));
            //快捷图片
            Parcelable icon = Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), R.mipmap.ic_launcher);
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
            //点击快捷图片，运行的程序主入口
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, extra);
            //发送广播。OK
            context.sendBroadcast(shortcutintent);
    }

    private static String getIconId(String pkg, int userId){
        return pkg+"_"+userId;
    }

    public static void createShortCut(Context context, CloneModel appModel) {
        CustomizeAppData customizeAppData = CustomizeAppData.loadFromPref(appModel.getPackageName(), appModel.getPkgUserId());
        Bitmap iconBitmap = customizeAppData.getCustomIcon();
        String appName = customizeAppData.label;
        Intent actionIntent = new Intent(Intent.ACTION_DEFAULT);
        actionIntent.setClassName(context.getPackageName(), AppLoadingActivity.class.getName());
        actionIntent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, appModel.getPackageName());
        actionIntent.putExtra(AppConstants.EXTRA_FROM, AppConstants.FROM_SHORTCUT);
        actionIntent.putExtra(AppConstants.EXTRA_CLONED_APP_USERID, appModel.getPkgUserId());
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        createShortcut(context, actionIntent, appName, appModel.getPackageName(), appModel.getPkgUserId(), false, iconBitmap);
//        iconBitmap.recycle();
    }

    public static void removeShortCut(Context context, CloneModel appModel) {
        String appName = appModel.getName();
        Intent actionIntent = new Intent(Intent.ACTION_DEFAULT);
        actionIntent.setClassName(context.getPackageName(), AppLoadingActivity.class.getName());
        actionIntent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, appModel.getPackageName());
        actionIntent.putExtra(AppConstants.EXTRA_FROM, AppConstants.FROM_SHORTCUT);
        actionIntent.putExtra(AppConstants.EXTRA_CLONED_APP_USERID, appModel.getPkgUserId());
        actionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        removeShortcut(context, actionIntent, appName, appModel.getPackageName(), appModel.getPkgUserId());
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


    public static void jumpToMarket(Context context, String packageName) {
        try {
            Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + packageName));
//            viewIntent.putExtra("START_OUTTER_APP_FLAG", true);
            viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(viewIntent);
        } catch (Exception e) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
//            i.putExtra("START_OUTTER_APP_FLAG", true);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

    public static void jumpToUrl(Context context, String url){
        if (!TextUtils.isEmpty(url)) {
            Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
            context.startActivity(viewIntent);
        }
    }

    public static void shareWithFriends(Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String appName = context.getResources().getString(R.string.app_name);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName);
        String shareContent = context.getResources().getString(R.string.share_with_friends_tip);
        shareContent = String.format(shareContent, appName) + "https://play.google.com/store/apps/details?id="
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

    private static HashSet<String> SOCIAL_APP_LIST = new HashSet<>();
    static {
        SOCIAL_APP_LIST.add("com.whatsapp");
        SOCIAL_APP_LIST.add("com.instagram.android");
        SOCIAL_APP_LIST.add("com.facebook.orca");
        SOCIAL_APP_LIST.add("com.facebook.katana");
        SOCIAL_APP_LIST.add("com.imo.android.imoim");
        SOCIAL_APP_LIST.add("com.linecorp.LGGRTHN");
        SOCIAL_APP_LIST.add("com.bsb.hike");
        SOCIAL_APP_LIST.add("com.snapchat.android");
        SOCIAL_APP_LIST.add("jp.naver.line.android");
        SOCIAL_APP_LIST.add("net.one97.paytm");
        SOCIAL_APP_LIST.add("com.facebook.lite");
        SOCIAL_APP_LIST.add("com.cmcm.whatscall");
        SOCIAL_APP_LIST.add("com.tencent.mm");
        SOCIAL_APP_LIST.add("com.bsb.hike");
        SOCIAL_APP_LIST.add("com.bbm");
        SOCIAL_APP_LIST.add("com.viber.voip");
        SOCIAL_APP_LIST.add("com.twitter.android");
        SOCIAL_APP_LIST.add("sg.bigo.live");
        SOCIAL_APP_LIST.add("com.UCMobile.intl");
    }

    public static boolean isSocialApp(String pkg) {
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
        PackageManager pm = DualApp.getApp().getPackageManager();
        try {
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
