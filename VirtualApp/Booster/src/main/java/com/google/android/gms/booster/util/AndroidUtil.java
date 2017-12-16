package com.google.android.gms.booster.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Process;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by guojia on 2017/12/9.
 */


public class AndroidUtil {
    public static final int STATE_OFF = 0;
    public static final int STATE_ON = 1;
    public static final int STATE_UNKNOWN = 2;
    public static final String BUCKET_ID_DEBUG_FILE = "debug_bucket_id.txt";
    static final int PROCESS_STATE_TOP = 2;
    static Field sFieldProcessState = null;
    static final String PROC_MEMORY_INFO = "/proc/meminfo";

    public AndroidUtil() {
    }

    public static String getImei(Context context) {
        try {
            TelephonyManager e = (TelephonyManager)context.getSystemService("phone");
            String ret = e.getDeviceId();
            return ret != null?ret:"";
        } catch (Exception var3) {
            var3.printStackTrace();
            return "";
        }
    }

    public static String getImsi(Context ctx) {
        try {
            TelephonyManager e = (TelephonyManager)ctx.getSystemService("phone");
            String ret = e.getSubscriberId();
            return ret != null?ret:"";
        } catch (Exception var3) {
            var3.printStackTrace();
            return "";
        }
    }

    public static String getNetworkOperator(Context context) {
        try {
            TelephonyManager e = (TelephonyManager)context.getSystemService("phone");
            String networkOperator = e.getNetworkOperator();
            return networkOperator;
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static String getAndroidId(Context ctx) {
        try {
            return Settings.Secure.getString(ctx.getContentResolver(), "android_id");
        } catch (Exception var2) {
            return "";
        }
    }

    public static String getDeviceId(Context context) {
        String androidId = "";

        try {
            androidId = Settings.Secure.getString(context.getContentResolver(), "android_id");
            TelephonyManager e = (TelephonyManager)context.getSystemService("phone");
            String imei = e.getDeviceId();
            return imei + androidId;
        } catch (Exception var4) {
            return androidId;
        }
    }

    public static String getMacAddress(Context ctx) {
        try {
            WifiManager e = (WifiManager)ctx.getSystemService("wifi");
            WifiInfo wifiInfo = e.getConnectionInfo();
            String ret = wifiInfo.getMacAddress();
            return ret != null?ret:"";
        } catch (Exception var4) {
            var4.printStackTrace();
            return "";
        }
    }

    public static Address geoDecodeAddress(Context context, double latitude, double longitude) {
        if(latitude == 0.0D && longitude == 0.0D) {
            return null;
        } else {
            try {
                Geocoder e = new Geocoder(context, Locale.US);
                List addresses = e.getFromLocation(latitude, longitude, 1);
                return addresses != null && addresses.size() > 0?(Address)addresses.get(0):null;
            } catch (Exception var7) {
                var7.printStackTrace();
                return null;
            }
        }
    }

    public static String getLauncherPackageName(Context ctx) {
        try {
            Intent e = new Intent("android.intent.action.MAIN");
            e.addCategory("android.intent.category.HOME");
            ResolveInfo resolveInfo = ctx.getPackageManager().resolveActivity(e, 65536);
            if(resolveInfo == null) {
                return "";
            } else {
                String launcherPackageName = resolveInfo.activityInfo.packageName;
                return launcherPackageName != null?launcherPackageName:"";
            }
        } catch (Exception var4) {
            var4.printStackTrace();
            return "";
        }
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager e = context.getPackageManager();
            return e.getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception var2) {
            var2.printStackTrace();
            return "";
        }
    }

    public static String getVersionName(Context context, String packageName) {
        try {
            PackageManager e = context.getPackageManager();
            return e.getPackageInfo(packageName, 0).versionName;
        } catch (Exception var3) {
            var3.printStackTrace();
            return "";
        }
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager e = context.getPackageManager();
            return e.getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception var2) {
            var2.printStackTrace();
            return -1;
        }
    }

    public static int getVersionCode(Context context, String packageName) {
        try {
            PackageManager e = context.getPackageManager();
            return e.getPackageInfo(packageName, 0).versionCode;
        } catch (Exception var3) {
            var3.printStackTrace();
            return -1;
        }
    }

    public static int getAppIconResId(Context context) {
        try {
            PackageManager e = context.getPackageManager();
            PackageInfo pi = e.getPackageInfo(context.getPackageName(), 0);
            return pi.applicationInfo.icon;
        } catch (Exception var3) {
            var3.printStackTrace();
            return 0;
        }
    }

    public static int getAppLabelResId(Context context) {
        try {
            PackageManager e = context.getPackageManager();
            PackageInfo pi = e.getPackageInfo(context.getPackageName(), 0);
            return pi.applicationInfo.labelRes;
        } catch (Exception var3) {
            var3.printStackTrace();
            return 0;
        }
    }

    public static String getAppLabel(Context context) {
        try {
            PackageManager e = context.getPackageManager();
            PackageInfo pi = e.getPackageInfo(context.getPackageName(), 0);
            CharSequence label = pi.applicationInfo.loadLabel(e);
            return label != null?label.toString():null;
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public static String getAppLabel(Context context, String packageName) {
        try {
            PackageManager e = context.getPackageManager();
            PackageInfo pi = e.getPackageInfo(packageName, 0);
            CharSequence label = pi.applicationInfo.loadLabel(e);
            return label != null?label.toString():null;
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public static String getMeta(Context context, String key) {
        try {
            PackageManager e = context.getPackageManager();
            ApplicationInfo applicationInfo = e.getApplicationInfo(context.getPackageName(), 128);
            return applicationInfo.metaData.getString(key);
        } catch (Exception var4) {
            return "";
        }
    }

    public static String queryMeta(Context context, String... keys) {
        try {
            if(keys != null && keys.length > 0) {
                PackageManager e = context.getPackageManager();
                ApplicationInfo applicationInfo = e.getApplicationInfo(context.getPackageName(), 128);
                Bundle meta = applicationInfo.metaData;
                String[] var5 = keys;
                int var6 = keys.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    String key = var5[var7];
                    Object m = meta.get(key);
                    String value = m != null?m.toString():null;
                    if(value != null && !value.isEmpty()) {
                        return value;
                    }
                }

                return null;
            } else {
                return null;
            }
        } catch (Exception var11) {
            return null;
        }
    }

    public static String getChannel(Context context) {
        return queryMeta(context, new String[]{"channel", "CHANNEL"});
    }

    public static String getTrafficId(Context context) {
        return queryMeta(context, new String[]{"traffic_id", "TRAFFIC_ID"});
    }

    public static long getFirstInstallTime(Context context) {
        try {
            PackageManager e = context.getPackageManager();
            PackageInfo pi = e.getPackageInfo(context.getPackageName(), 0);
            return pi.firstInstallTime;
        } catch (Exception var3) {
            var3.printStackTrace();
            return 0L;
        }
    }

    public static long getFirstInstallTimeInSecond(Context context) {
        try {
            PackageManager e = context.getPackageManager();
            PackageInfo pi = e.getPackageInfo(context.getPackageName(), 0);
            return pi.firstInstallTime >= 1000000000000L?pi.firstInstallTime / 1000L:pi.firstInstallTime;
        } catch (Exception var3) {
            var3.printStackTrace();
            return 0L;
        }
    }

    public static long getLastUpdateTime(Context context) {
        try {
            PackageManager e = context.getPackageManager();
            PackageInfo pi = e.getPackageInfo(context.getPackageName(), 0);
            return pi.lastUpdateTime;
        } catch (Exception var3) {
            var3.printStackTrace();
            return 0L;
        }
    }

    public static long getLastUpdateTimeInSecond(Context context) {
        try {
            PackageManager e = context.getPackageManager();
            PackageInfo pi = e.getPackageInfo(context.getPackageName(), 0);
            return pi.lastUpdateTime >= 1000000000000L?pi.lastUpdateTime / 1000L:pi.lastUpdateTime;
        } catch (Exception var3) {
            var3.printStackTrace();
            return 0L;
        }
    }

    public static void safeRegisterBroadcastReceiver(Context context, BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
        try {
            context.registerReceiver(broadcastReceiver, intentFilter);
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public static void safeUnregisterBroadcastReceiver(Context context, BroadcastReceiver broadcastReceiver) {
        try {
            context.unregisterReceiver(broadcastReceiver);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public static int getKeyguardLockedState(Context context) {
        try {
            if(Build.VERSION.SDK_INT >= 16) {
                KeyguardManager e = (KeyguardManager)context.getSystemService("keyguard");
                return e.isKeyguardLocked()?1:0;
            } else {
                return 2;
            }
        } catch (Throwable var2) {
            return 2;
        }
    }

    public static int getKeyguardSecureState(Context context) {
        try {
            if(Build.VERSION.SDK_INT >= 16) {
                KeyguardManager e = (KeyguardManager)context.getSystemService("keyguard");
                return e.isKeyguardSecure()?1:0;
            } else {
                return 2;
            }
        } catch (Throwable var2) {
            return 2;
        }
    }

    public static int getKeyguardRestrictedInputModeState(Context context) {
        try {
            KeyguardManager e = (KeyguardManager)context.getSystemService("keyguard");
            return e.inKeyguardRestrictedInputMode()?1:0;
        } catch (Throwable var2) {
            return 2;
        }
    }

    public static int getScreenState(Context context) {
        try {
            PowerManager e = (PowerManager)context.getSystemService("power");
            return e.isScreenOn()?1:0;
        } catch (Throwable var2) {
            return 2;
        }
    }

    @SuppressLint({"NewApi"})
    public static void getScreenSize(Context context, Point point) {
        if(Build.VERSION.SDK_INT < 14) {
            DisplayMetrics wm = context.getResources().getDisplayMetrics();
            point.set(wm.widthPixels, wm.heightPixels);
        } else {
            WindowManager wm1 = (WindowManager)context.getSystemService("window");
            wm1.getDefaultDisplay().getSize(point);
        }

    }

    public static boolean isAirplaneModeOn(Context context) {
        try {
            return Build.VERSION.SDK_INT < 17? Settings.System.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0: Settings.Global.getInt(context.getContentResolver(), "airplane_mode_on", 0) != 0;
        } catch (Exception var2) {
            return false;
        }
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            PackageManager e = context.getPackageManager();
            PackageInfo pi = e.getPackageInfo(packageName, 128);
            return pi != null;
        } catch (Exception var4) {
            return false;
        }
    }

    public static String getInstaller(Context c) {
        try {
            PackageManager e = c.getPackageManager();
            String installer = e.getInstallerPackageName(c.getPackageName());
            return installer != null?installer:"";
        } catch (Throwable var3) {
            var3.printStackTrace();
            return "";
        }
    }
//
//    public static int getBucketId(Context context) {
//        try {
//            File e = new File(Environment.getExternalStorageDirectory(), "debug_bucket_id.txt");
//            if(e.exists()) {
//                return Integer.parseInt(StringUtil.toString(e, "utf-8").trim());
//            } else {
//                String md5 = Md5.md5((getAndroidId(context) + Build.SERIAL).getBytes("utf-8"));
//                return Integer.parseInt(md5.substring(0, 4), 16) * 100 / 65536;
//            }
//        } catch (Exception var3) {
//            var3.printStackTrace();
//            return -1;
//        }
//    }

    public static boolean existInExternalStorage(String file) {
        try {
            return (new File(Environment.getExternalStorageDirectory(), file)).exists();
        } catch (Exception var2) {
            return false;
        }
    }

    public static boolean existOneFileIn(String[] paths) {
        try {
            String[] e = paths;
            int var2 = paths.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                String path = e[var3];
                if((new File(path)).exists()) {
                    return true;
                }
            }
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return false;
    }

    public static String getDisplayDpi(Context context) {
        try {
            float e = context.getResources().getDisplayMetrics().density;
            return e == 0.75F?"ldpi":(e == 1.0F?"mdpi":(e == 1.5F?"hdpi":(e == 2.0F?"xhdpi":(e == 3.0F?"xxhdpi":"density:" + e))));
        } catch (Exception var2) {
            return "unknown";
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager e = (ConnectivityManager)context.getSystemService("connectivity");
            NetworkInfo netInfo = e.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } catch (Exception var3) {
            var3.printStackTrace();
            return true;
        }
    }

    public static String getNetworkOperatorName(Context context) {
        try {
            TelephonyManager e = (TelephonyManager)context.getSystemService("phone");
            return e.getNetworkOperatorName();
        } catch (Exception var2) {
            var2.printStackTrace();
            return "";
        }
    }

    public static String getNetworkType(Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService("phone");
        int type = tm.getNetworkType();
        switch(type) {
            case 0:
                return "unkown";
            case 1:
                return "gprs";
            case 2:
                return "edge";
            case 3:
                return "umts";
            case 4:
                return "cdma";
            case 5:
                return "evdo_0";
            case 6:
                return "evdo_a";
            case 7:
                return "1xrtt";
            case 8:
                return "hsdpa";
            case 9:
                return "hsupa";
            case 10:
                return "hspa";
            case 11:
                return "iden";
            case 12:
                return "evdo_b";
            case 13:
            default:
                return "unkown";
            case 14:
                return "ehrpd";
        }
    }

    public static Address loadAddress(Context context) {
        try {
            double e = 0.0D;
            double longitude = 0.0D;
            LocationManager lm = (LocationManager)context.getSystemService("location");
            List providers = lm.getProviders(new Criteria(), true);
            if(providers != null && providers.size() > 0) {
                Location address = lm.getLastKnownLocation((String)providers.get(0));
                if(address != null) {
                    e = address.getLatitude();
                    longitude = address.getLongitude();
                }
            }

            if(e == 0.0D && longitude == 0.0D) {
                return null;
            } else {
                Address address1 = null;

                try {
                    Geocoder e1 = new Geocoder(context.getApplicationContext(), Locale.ENGLISH);
                    List addresses = e1.getFromLocation(e, longitude, 1);
                    if(addresses != null && addresses.size() > 0) {
                        address1 = (Address)addresses.get(0);
                    }
                } catch (Exception var10) {
                    var10.printStackTrace();
                }

                if(address1 != null) {
                    return address1;
                } else {
                    address1 = new Address(Locale.getDefault());
                    address1.setLatitude(e);
                    address1.setLongitude(longitude);
                    return address1;
                }
            }
        } catch (Exception var11) {
            var11.printStackTrace();
            return null;
        }
    }

    public static String getGoogleAdId(Context context) {
        try {
            AdvertisingIdClient.Info e = AdvertisingIdClient.getAdvertisingIdInfo(context);
            return e.getId();
        } catch (Throwable var2) {
            return null;
        }
    }

    public static long getSystemAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService("activity");
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    public static long getSystemAvailableMemoryInMegabytes(Context context) {
        return getSystemAvailableMemory(context) / 1048576L;
    }

    public static String getProcessName(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService("activity");
        List runningAppProcessInfos = am.getRunningAppProcesses();
        if(runningAppProcessInfos == null) {
            return null;
        } else {
            int pid = Process.myPid();
            Iterator var4 = runningAppProcessInfos.iterator();

            ActivityManager.RunningAppProcessInfo runningAppProcessInfo;
            do {
                if(!var4.hasNext()) {
                    return null;
                }

                runningAppProcessInfo = (ActivityManager.RunningAppProcessInfo)var4.next();
            } while(runningAppProcessInfo == null || runningAppProcessInfo.pid != pid);

            return runningAppProcessInfo.processName;
        }
    }

    public static List<String> getAllRunningPackageNames(Context context) {
        ActivityManager am;
        List runningServiceInfos;
        ArrayList packageNames;
        Iterator var4;
        if(Build.VERSION.SDK_INT >= 23) {
            am = (ActivityManager)context.getApplicationContext().getSystemService("activity");
            runningServiceInfos = am.getRunningServices(2147483647);
            if(runningServiceInfos != null && runningServiceInfos.size() > 0) {
                packageNames = new ArrayList();
                var4 = runningServiceInfos.iterator();

                while(var4.hasNext()) {
                    ActivityManager.RunningServiceInfo var10 = (ActivityManager.RunningServiceInfo)var4.next();
                    String var11 = var10.service.getPackageName();
                    if(!packageNames.contains(var11)) {
                        packageNames.add(var11);
                    }
                }

                return packageNames;
            } else {
                return new ArrayList();
            }
        } else {
            am = (ActivityManager)context.getApplicationContext().getSystemService("activity");
            runningServiceInfos = am.getRunningAppProcesses();
            if(runningServiceInfos != null && runningServiceInfos.size() > 0) {
                packageNames = new ArrayList(runningServiceInfos.size());
                var4 = runningServiceInfos.iterator();

                while(true) {
                    ActivityManager.RunningAppProcessInfo runningServiceInfo;
                    do {
                        do {
                            if(!var4.hasNext()) {
                                return packageNames;
                            }

                            runningServiceInfo = (ActivityManager.RunningAppProcessInfo)var4.next();
                        } while(runningServiceInfo.pkgList == null);
                    } while(runningServiceInfo.pkgList.length <= 0);

                    String[] packageName = runningServiceInfo.pkgList;
                    int var7 = packageName.length;

                    for(int var8 = 0; var8 < var7; ++var8) {
                        String pkg = packageName[var8];
                        if(!packageNames.contains(pkg)) {
                            packageNames.add(pkg);
                        }
                    }
                }
            } else {
                return new ArrayList();
            }
        }
    }

    @TargetApi(1)
    public static String getTopRunningPackageName(ActivityManager am) {
        try {
            List e = am.getRunningTasks(1);
            if(e != null && e.size() > 0) {
                ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo)e.get(0);
                return runningTaskInfo != null && runningTaskInfo.topActivity != null?runningTaskInfo.topActivity.getPackageName():null;
            } else {
                return null;
            }
        } catch (Throwable var3) {
            return null;
        }
    }

    @TargetApi(21)
    public static String getTopRunningPackageNameLollipop(ActivityManager am) {
        try {
            List e = am.getRunningAppProcesses();
            if(e != null && e.size() > 0) {
                Iterator var2 = e.iterator();

                while(var2.hasNext()) {
                    ActivityManager.RunningAppProcessInfo runningAppProcessInfo = (ActivityManager.RunningAppProcessInfo)var2.next();
                    if(runningAppProcessInfo != null && runningAppProcessInfo.importance == 100 && runningAppProcessInfo.importanceReasonCode == 0 && runningAppProcessInfo.pkgList != null && runningAppProcessInfo.pkgList.length > 0) {
                        Integer state = null;

                        try {
                            state = Integer.valueOf(sFieldProcessState.getInt(runningAppProcessInfo));
                        } catch (Throwable var6) {
                            ;
                        }

                        if(state != null && 2 == state.intValue()) {
                            return runningAppProcessInfo.pkgList != null && runningAppProcessInfo.pkgList.length > 0?runningAppProcessInfo.pkgList[0]:null;
                        }
                    }
                }

                return null;
            } else {
                return null;
            }
        } catch (Throwable var7) {
            return null;
        }
    }

    public static String getTopRunningPackageName(Context context) {
        ActivityManager activityManager = (ActivityManager)context.getSystemService("activity");
        return Build.VERSION.SDK_INT < 21?getTopRunningPackageName(activityManager):getTopRunningPackageNameLollipop(activityManager);
    }

    @TargetApi(21)
    public static String getTopRunningPackageNameUsageStats(UsageStatsManager usageStatsManager) {
        try {
            long e = java.lang.System.currentTimeMillis();
            UsageEvents usageEvents = usageStatsManager.queryEvents(e - 10000L, e);
            if(usageEvents == null) {
                return null;
            } else {
                String packageName = null;
                UsageEvents.Event event = new UsageEvents.Event();

                while(usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event);
                    if(event.getEventType() == 1) {
                        packageName = event.getPackageName();
                    }
                }

                return packageName;
            }
        } catch (Throwable var6) {
            return null;
        }
    }

    public static String getTopRunningPackageName(ActivityManager activityManager, Object usageStatsManager) {
        if(Build.VERSION.SDK_INT < 21) {
            return getTopRunningPackageName(activityManager);
        } else {
            String topPackageName = getTopRunningPackageNameLollipop(activityManager);
            return topPackageName != null?topPackageName:getTopRunningPackageNameUsageStats((UsageStatsManager)usageStatsManager);
        }
    }

    public static List<String> getLauncherPackageNames(Context ctx) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        return queryIntentPackageNames(ctx, intent);
    }

    public static List<String> getCallPackageNames(Context ctx) {
        Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:111111111"));
        return queryIntentPackageNames(ctx, intent);
    }

    public static List<String> getSmsPackageNames(Context ctx) {
        Intent intent = new Intent("android.intent.action.SENDTO", Uri.parse("smsto:13410177756"));
        return queryIntentPackageNames(ctx, intent);
    }

    public static List<String> getCameraPackageNames(Context ctx) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        return queryIntentPackageNames(ctx, intent);
    }

    private static List<String> queryIntentPackageNames(Context ctx, Intent intent) {
        ArrayList result = new ArrayList();

        try {
            PackageManager e = ctx.getPackageManager();
            List resolveInfos = e.queryIntentActivities(intent, 0);
            if(resolveInfos != null && resolveInfos.size() > 0) {
                Iterator var5 = resolveInfos.iterator();

                while(var5.hasNext()) {
                    ResolveInfo resolveInfo = (ResolveInfo)var5.next();
                    ActivityInfo activityInfo = resolveInfo != null?resolveInfo.activityInfo:null;
                    if(activityInfo != null && !TextUtils.isEmpty(activityInfo.packageName)) {
                        result.add(activityInfo.packageName);
                    }
                }

                return result;
            } else {
                return result;
            }
        } catch (Exception var8) {
            var8.printStackTrace();
            return result;
        }
    }

    public static boolean isScreenOn(Context context) {
        try {
            PowerManager e = (PowerManager)context.getSystemService("power");
            return e.isScreenOn();
        } catch (Throwable var2) {
            var2.printStackTrace();
            return false;
        }
    }

    public static int getCallState(Context context) {
        try {
            TelephonyManager e = (TelephonyManager)context.getSystemService("phone");
            return e.getCallState();
        } catch (Throwable var2) {
            var2.printStackTrace();
            return 0;
        }
    }

    public static boolean isBatteryPlugged(Context context) {
        Intent intent = context.registerReceiver((BroadcastReceiver)null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        int plugged = intent.getIntExtra("plugged", -1);
        return Build.VERSION.SDK_INT > 16?plugged == 1 || plugged == 2 || plugged == 4:plugged == 1 || plugged == 2;
    }

    public static long getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService("activity");
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    public static long getTotalMemoryBeforeJellyBean(Context context) {
        FileReader fr = null;
        BufferedReader br = null;

        long var4;
        try {
            fr = new FileReader("/proc/meminfo");
            br = new BufferedReader(fr, 8192);
            long e = Long.parseLong(br.readLine().split("\\s+")[1]) * 1024L;
            long var5 = e;
            return var5;
        } catch (Throwable var10) {
            var10.printStackTrace();
            var4 = getAvailMemory(context);
        } finally {
            IOUtil.closeQuietly(fr);
            IOUtil.closeQuietly(br);
        }

        return var4;
    }

    public static long getTotalMemory(Context context) {
        if(Build.VERSION.SDK_INT >= 16) {
            ActivityManager am = (ActivityManager)context.getSystemService("activity");
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            return mi.totalMem;
        } else {
            return getTotalMemoryBeforeJellyBean(context);
        }
    }

    public static int getUsedMemoryPercentage(Context context) {
        long avail = getAvailMemory(context);
        long total = getTotalMemory(context);
        return (int)((float)(total - avail) * 100.0F / (float)total);
    }

    public static boolean addShortcut(Context ctx, Class<?> clazz, int iconResId) {
        try {
            Intent e = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            Intent shortcutIntent = new Intent(ctx, clazz);
            shortcutIntent.setAction(clazz.getName());
            shortcutIntent.addCategory("android.intent.category.LAUNCHER");
            e.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
            PackageManager pm = ctx.getPackageManager();
            String appName = pm.getApplicationLabel(pm.getApplicationInfo(ctx.getPackageName(), 128)).toString();
            e.putExtra("android.intent.extra.shortcut.NAME", appName);
            e.putExtra("duplicate", false);
            Intent.ShortcutIconResource shortcutIcon = Intent.ShortcutIconResource.fromContext(ctx, iconResId);
            e.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", shortcutIcon);
            ctx.sendBroadcast(e);
            return true;
        } catch (Exception var8) {
            var8.printStackTrace();
            return false;
        }
    }

    public static boolean addShortcut(Context ctx, Class<?> clazz, int iconResId, String name) {
        try {
            Intent e = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            Intent shortcutIntent = new Intent(ctx, clazz);
            shortcutIntent.setAction(clazz.getName());
            shortcutIntent.addFlags(268435456);
            e.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
            e.putExtra("android.intent.extra.shortcut.NAME", name);
            e.putExtra("duplicate", false);
            Intent.ShortcutIconResource shortcutIcon = Intent.ShortcutIconResource.fromContext(ctx, iconResId);
            e.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", shortcutIcon);
            ctx.sendBroadcast(e);
            return true;
        } catch (Exception var7) {
            var7.printStackTrace();
            return false;
        }
    }

    public static boolean addShortcut(Context ctx, Intent shortcutIntent, Bitmap iconBitmap, String name) {
        try {
            Intent e = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            e.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
            e.putExtra("android.intent.extra.shortcut.NAME", name);
            e.putExtra("duplicate", false);
            e.putExtra("android.intent.extra.shortcut.ICON", iconBitmap);
            ctx.sendBroadcast(e);
            return true;
        } catch (Exception var5) {
            var5.printStackTrace();
            return false;
        }
    }

    public static boolean addShortcut(Context ctx, Intent shortcutIntent, int iconResId, String name) {
        try {
            Intent e = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            e.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
            e.putExtra("android.intent.extra.shortcut.NAME", name);
            e.putExtra("duplicate", false);
            Intent.ShortcutIconResource shortcutIcon = Intent.ShortcutIconResource.fromContext(ctx, iconResId);
            e.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", shortcutIcon);
            ctx.sendBroadcast(e);
            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public static boolean addLauncherShortcut(Context ctx, Class<?> clazz, int iconResId) {
        try {
            Intent e = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            String brand = Build.BRAND.toLowerCase(Locale.US);
            Intent pm;
            if(brand.indexOf("htc") != -1) {
                pm = new Intent(ctx, clazz);
                pm.setAction(clazz.getName());
                pm.addCategory("android.intent.category.LAUNCHER");
                e.putExtra("android.intent.extra.shortcut.INTENT", pm);
            } else {
                pm = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
                e.putExtra("android.intent.extra.shortcut.INTENT", pm);
            }

            PackageManager pm1 = ctx.getPackageManager();
            String appName = pm1.getApplicationLabel(pm1.getApplicationInfo(ctx.getPackageName(), 128)).toString();
            e.putExtra("android.intent.extra.shortcut.NAME", appName);
            e.putExtra("duplicate", false);
            Intent.ShortcutIconResource shortcutIcon = Intent.ShortcutIconResource.fromContext(ctx, iconResId);
            e.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", shortcutIcon);
            ctx.sendBroadcast(e);
            return true;
        } catch (Exception var8) {
            var8.printStackTrace();
            return false;
        }
    }

    public static void delShortcut(Context ctx, Class<?> clazz, String name) {
        try {
            Intent e = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
            Intent shortcutIntent = new Intent(ctx, clazz);
            shortcutIntent.setAction(clazz.getName());
            shortcutIntent.addFlags(268435456);
            e.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
            e.putExtra("android.intent.extra.shortcut.NAME", name);
            e.putExtra("duplicate", false);
            ctx.sendBroadcast(e);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    public static boolean hasShortcut(Context ctx) {
        try {
            PackageManager e = ctx.getPackageManager();
            String name = e.getApplicationLabel(e.getApplicationInfo(ctx.getPackageName(), 128)).toString();
            return hasShortcut(ctx, name);
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    public static boolean hasShortcut(Context ctx, String name) {
        Cursor cursor = null;

        boolean authority;
        try {
            String e = "content://com.android.launcher.settings/favorites?notify=true";
            Uri queryUri1 = Uri.parse(e);
            cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
            if(cursor != null) {
                authority = cursor.getCount() > 0;
                return authority;
            }

            e = "content://com.android.launcher2.settings/favorites?notify=true";
            queryUri1 = Uri.parse(e);
            cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
            if(cursor == null) {
                e = "content://com.huawei.android.launcher.settings/favorites?notify=true";
                queryUri1 = Uri.parse(e);
                cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
                if(cursor != null) {
                    authority = cursor.getCount() > 0;
                    return authority;
                }

                e = "content://com.miui.home.launcher.settings/favorites?notify=true";
                queryUri1 = Uri.parse(e);
                cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
                if(cursor != null) {
                    authority = cursor.getCount() > 0;
                    return authority;
                }

                e = "content://com.sonymobile.home.configprovider/favorites?notify=true";
                queryUri1 = Uri.parse(e);
                cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
                if(cursor != null) {
                    authority = cursor.getCount() > 0;
                    return authority;
                }

                e = "content://com.android.launcher3.settings/favorites?notify=true";
                queryUri1 = Uri.parse(e);
                cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
                if(cursor != null) {
                    authority = cursor.getCount() > 0;
                    return authority;
                }

                e = "content://com.google.android.launcher.settings/favorites?notify=true";
                queryUri1 = Uri.parse(e);
                cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
                if(cursor != null) {
                    authority = cursor.getCount() > 0;
                    return authority;
                }

                e = "content://com.qihoo360.home2.settings/favorites?notify=true";
                queryUri1 = Uri.parse(e);
                cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
                if(cursor != null) {
                    authority = cursor.getCount() > 0;
                    return authority;
                }

                e = "content://com.qihoo360.home.launcher.settings/favorites?notify=true";
                queryUri1 = Uri.parse(e);
                cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
                if(cursor != null) {
                    authority = cursor.getCount() > 0;
                    return authority;
                }

                e = "content://com.qihoo360.home.launcher2.settings/favorites?notify=true";
                queryUri1 = Uri.parse(e);
                cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
                if(cursor != null) {
                    authority = cursor.getCount() > 0;
                    return authority;
                }

                e = "content://com.android.launcher2.settings/favorites?notify=true";
                queryUri1 = Uri.parse(e);
                cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
                if(cursor != null) {
                    authority = cursor.getCount() > 0;
                    return authority;
                }

                String authority1 = findAuthorityByPermission(ctx, "launcher.permission.READ_SETTINGS");
                boolean var6;
                if(authority1 == null) {
                    var6 = false;
                    return var6;
                }

                e = "content://" + authority1 + "/favorites?notify=true";
                queryUri1 = Uri.parse(e);
                cursor = ctx.getContentResolver().query(queryUri1, (String[])null, "title=?", new String[]{name}, (String)null);
                if(cursor != null) {
                    var6 = cursor.getCount() > 0;
                    return var6;
                }

                var6 = false;
                return var6;
            }

            authority = cursor.getCount() > 0;
        } catch (Exception var10) {
            var10.printStackTrace();
            boolean queryUri = false;
            return queryUri;
        } finally {
            IOUtil.closeQuietly(cursor);
        }

        return authority;
    }

    public static String findAuthorityByPermission(Context ctx, String permission) {
        if(permission == null) {
            return null;
        } else {
            List packs = ctx.getPackageManager().getInstalledPackages(8);
            if(packs != null && packs.size() > 0) {
                Iterator var3 = packs.iterator();

                while(true) {
                    PackageInfo pack;
                    do {
                        do {
                            if(!var3.hasNext()) {
                                return null;
                            }

                            pack = (PackageInfo)var3.next();
                        } while(pack == null);
                    } while(pack.providers == null);

                    ProviderInfo[] var5 = pack.providers;
                    int var6 = var5.length;

                    for(int var7 = 0; var7 < var6; ++var7) {
                        ProviderInfo provider = var5[var7];
                        if(provider != null) {
                            if(provider.readPermission != null && provider.readPermission.contains(permission)) {
                                return provider.authority;
                            }

                            if(provider.writePermission != null && provider.writePermission.contains(permission)) {
                                return provider.authority;
                            }
                        }
                    }
                }
            } else {
                return null;
            }
        }
    }

    public static int openCameraStillImageOrSecure(Context context) {
        try {
            Intent e;
            if(Build.VERSION.SDK_INT < 17) {
                e = new Intent("android.media.action.STILL_IMAGE_CAMERA");
                e.addFlags(335544320);
                context.startActivity(e);
                return 1;
            }

            try {
                e = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE");
                e.addFlags(335544320);
                context.startActivity(e);
                return 2;
            } catch (Exception var3) {
                var3.printStackTrace();

                try {
                    e = new Intent("android.media.action.STILL_IMAGE_CAMERA");
                    e.addFlags(335544320);
                    context.startActivity(e);
                    return 3;
                } catch (Exception var2) {
                    var2.printStackTrace();
                }
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

        return -1;
    }

    public static boolean tryAction(Context context, String action) {
        try {
            Intent e = new Intent(action);
            if(!(context instanceof Activity)) {
                e.setFlags(268435456);
            }

            context.startActivity(e);
            return true;
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }

    public static boolean tryActions(Context context, String... actions) {
        String[] var2 = actions;
        int var3 = actions.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String action = var2[var4];
            if(tryAction(context, action)) {
                return true;
            }
        }

        return false;
    }

    public static boolean tryStartMobileDataSetting(Context context) {
        return tryActions(context, new String[]{"android.settings.WIRELESS_SETTINGS", "android.settings.SETTINGS"});
    }

    public static boolean tryStartCalculator(Context context) {
        try {
            PackageManager e = context.getPackageManager();
            List packageInfos = e.getInstalledPackages(0);
            if(packageInfos == null) {
                return false;
            } else {
                Iterator var3 = packageInfos.iterator();

                while(var3.hasNext()) {
                    PackageInfo pi = (PackageInfo)var3.next();
                    if(pi != null && pi.packageName != null && pi.packageName.toLowerCase(Locale.US).contains("calcul")) {
                        Intent i = e.getLaunchIntentForPackage(pi.packageName);
                        if(i != null) {
                            i.setFlags(268435456);
                            context.startActivity(i);
                            break;
                        }
                    }
                }

                return true;
            }
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        int screenHeight = metric.heightPixels;
        return screenHeight;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        int screenWidth = metric.widthPixels;
        return screenWidth;
    }

    static {
        try {
            sFieldProcessState = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Throwable var1) {
            var1.printStackTrace();
        }

    }
}
