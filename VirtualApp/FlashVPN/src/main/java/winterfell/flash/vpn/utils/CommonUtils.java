package winterfell.flash.vpn.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import winterfell.flash.vpn.R;
import winterfell.flash.vpn.ui.ShortcutActivity;
import winterfell.flash.vpn.ui.SplashActivity;

/**
 * Created by doriscoco on 2017/4/4.
 */

public class CommonUtils {

    private static HashSet<String> blockedApps;
    public static final Set<String>  getBlockedApps() {
        if (blockedApps == null) {
            blockedApps = new HashSet<>();
            String config = RemoteConfig.getString("block_app");
            String arr[] = config.split(";");
            if(arr != null && arr.length > 0) {
                for(String s: arr) {
                    blockedApps.add(s);
                }
            }
        }
        return blockedApps;
    }
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

    public static void createLaunchShortcut(Context context){
        MLogs.d("create shortcut");
        Intent extra = new Intent(context.getApplicationContext(), ShortcutActivity.class);
        extra.putExtra(SplashActivity.EXTRA_FROM_SHORTCUT, true);
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
        Parcelable icon = Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), R.drawable.app_icon);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        //点击快捷图片，运行的程序主入口
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, extra);
        //发送广播。OK
        context.sendBroadcast(shortcutintent);
    }

    public static void shareWithFriends(Context context) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String appName = context.getResources().getString(R.string.app_name);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName);
        String shareContent = context.getResources().getString(R.string.share_with_friends_tip, appName);
        shareContent = shareContent + "https://play.google.com/store/apps/details?id=" + context.getPackageName();
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        context.startActivity(Intent.createChooser(shareIntent, context.getResources().getText(R.string.share_with_friends)));
    }

    public static int getScreenWidth(Context context){
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;
        return width;
    }

    public static void jumpToUrl(Context context, String url){
        if (!TextUtils.isEmpty(url)) {
            Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url));
            context.startActivity(viewIntent);
        }
    }

    /**
     * 秒转化为天小时分秒字符串
     *
     * @param seconds
     * @return String
     */
    public static String formatSeconds(long seconds) {
        String timeStr = seconds + "s";
        if (seconds > 60) {
            long second = seconds % 60;
            long min = seconds / 60;
            timeStr = min + "min " + second + "s";
            if (min > 60) {
                min = (seconds / 60) % 60;
                long hour = (seconds / 60) / 60;
                timeStr = hour + "hour " + min + "min " + second + "s";
                if (hour > 24) {
                    hour = ((seconds / 60) / 60) % 24;
                    long day = (((seconds / 60) / 60) / 24);
                    timeStr = day + "day " + hour + "hour " + min + "min " + second + "s";
                }
            }
        }
        return timeStr;
    }

    public static boolean isNetworkAvailable(Context ctx) {

        ConnectivityManager manager = (ConnectivityManager) ctx
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        return !(networkinfo == null || !networkinfo.isAvailable());
    }

    public static String getIpString(InetSocketAddress inetSocketAddress) {
        InetAddress inetAddress = ((InetSocketAddress)inetSocketAddress).getAddress();
        if (inetAddress instanceof Inet4Address) {
            System.out.println("IPv4: " + inetAddress);
            byte[] ip4bytes = inetAddress.getAddress(); // returns byte[4]
            return inetAddress.toString().split("/")[1];
        }
//            else if (inetAddress instanceof Inet6Address)
//                System.out.println("IPv6: " + inetAddress);
        else {
            MLogs.e("Not an IP address.");
            return "";
        }
    }

    public static void sleepHelper(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (Exception e) {

        }
    }
}
