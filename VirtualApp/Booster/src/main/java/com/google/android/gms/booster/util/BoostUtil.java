package com.google.android.gms.booster.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import com.google.android.gms.booster.BoosterLog;
import com.google.android.gms.booster.util.proc.AndroidAppProcess;
import com.google.android.gms.booster.util.proc.AndroidProcessManager;

import java.util.ArrayList;
import java.util.List;

public class BoostUtil {
    public static final String CLONE_PREFIX = "clone";

    public static class CleanStatus {
        public final long totalMemory;
        public final long availMemory;
        public final String packageName;

        public CleanStatus(long totalMemory, long availMemory, String packageName) {
            this.totalMemory = totalMemory;
            this.availMemory = availMemory;
            this.packageName = packageName;
            BoosterLog.log( "Total: " + totalMemory + " avail: " + availMemory + " pkg" + packageName);
        }

        public static CleanStatus create(Context context, long totalMemory, String packageName) {
            return new CleanStatus(totalMemory, AndroidUtil.getAvailMemory(context), packageName);
        }
    }

    public interface CleanStatusListener {
        void onCleanStatus(CleanStatus cleanStatus);
    }

    public static void clean(final Context context, final CleanStatusListener cleanStatusListener) {
        final long totalMemory = AndroidUtil.getTotalMemory(context);

        CleanStatus lastCleanStatus = CleanStatus.create(context, totalMemory, "");
        cleanStatusListener.onCleanStatus(lastCleanStatus);

        List<String> runningPackageNames = getRunningPackageNames(context, true);
        List<String> autoStartPackageNames = getAutoStartPackageNames(context, false);

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (String runningPackageName : runningPackageNames) {
            String actualName = runningPackageName;
            if (runningPackageName.startsWith(CLONE_PREFIX)) {
                //cloned
                String arr[] = runningPackageName.split(":");
                if (arr == null || arr.length != 3) {
                    continue;
                }
                Process.killProcess(Integer.valueOf(arr[1]));
                actualName = CLONE_PREFIX + " " + arr[2];

                BoosterLog.log("clean " + runningPackageName);
            } else {
                if (autoStartPackageNames.contains(runningPackageName))
                    continue;

                am.killBackgroundProcesses(runningPackageName);

            }
            CleanStatus cleanStatus = CleanStatus.create(context, totalMemory, actualName);
            cleanStatusListener.onCleanStatus(cleanStatus);
        }
        cleanStatusListener.onCleanStatus(CleanStatus.create(context, totalMemory, ""));
    }

    public static List<String> getRunningPackageNames(Context context, boolean backgroundOnly) {
        ArrayList<String> runningPackageNames = new ArrayList<>();
        String thisPackageName = context.getPackageName();
        List<String> launchers = getLauncherPackageNames(context);
        List<AndroidAppProcess> runningAppProcesses = AndroidProcessManager.getRunningAppProcesses();
        if (runningAppProcesses != null && runningAppProcesses.size() > 0) {
            for (AndroidAppProcess androidAppProcess : runningAppProcesses) {
                if (backgroundOnly && androidAppProcess.foreground)
                    continue;

                String packageName = androidAppProcess.getPackageName();
                if (launchers.contains(packageName) || runningPackageNames.contains(packageName) || thisPackageName.equals(packageName))
                    continue;

                if (packageName.contains("com.google.android.gms") || packageName.contains("com.facebook.ads"))
                    continue;

                if(androidAppProcess.uid == Process.myUid()) {
                    //Cloned package
                    packageName = CLONE_PREFIX +":"+ androidAppProcess.pid + ":"+ packageName;
                }
                BoosterLog.log( "process: "+packageName);
                runningPackageNames.add(packageName);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(50);
            if (runningServiceInfos.size() > 0) {
                for (ActivityManager.RunningServiceInfo appProcess : runningServiceInfos){
                    String packageName = appProcess.service.getPackageName();
                    if (launchers.contains(packageName) || runningPackageNames.contains(packageName) || thisPackageName.equals(packageName))
                        continue;

                    if (packageName.contains("com.google.android.gms") || packageName.contains("com.facebook.ads"))
                        continue;

                    runningPackageNames.add(packageName);
                    BoosterLog.log( "service: "+packageName);
                }
            }
        }

        return runningPackageNames;
    }

    private static List<String> getLauncherPackageNames(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        List<String> packageNames = new ArrayList<>();
        if (resolveInfos == null || resolveInfos.size() <= 0)
            return packageNames;

        for (ResolveInfo resolveInfo : resolveInfos)
            if (resolveInfo != null && resolveInfo.activityInfo != null)
                packageNames.add(resolveInfo.activityInfo.packageName);

        return packageNames;
    }

    public static List<String> getAutoStartPackageNames(Context ctx, boolean includeSystem) {
        ArrayList<String> packageNames = new ArrayList<>();
        PackageManager pm = ctx.getPackageManager();
        if (pm == null)
            return packageNames;

        try {
            List<String> autoStartActions = getAutoStartActions();
            for (String action : autoStartActions) {
                List<ResolveInfo> resolveInfos = pm.queryBroadcastReceivers(new Intent(action), PackageManager.GET_RESOLVED_FILTER | PackageManager.GET_DISABLED_COMPONENTS);
                for (ResolveInfo resolveInfo : resolveInfos)
                    if (resolveInfo != null && resolveInfo.activityInfo != null && !packageNames.contains(resolveInfo.activityInfo.packageName) && (includeSystem || !isSystemApp(pm, resolveInfo.activityInfo.packageName)))
                        packageNames.add(resolveInfo.activityInfo.packageName);
            }
            return packageNames;
        } catch (Exception e) {
            return packageNames;
        }
    }

    public static boolean isSystemApp(PackageManager pm, String arg6) {
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(arg6, PackageManager.GET_UNINSTALLED_PACKAGES);
            return applicationInfo != null && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static List<String> sAutoStartActions;

    public static List<String> getAutoStartActions() {
        if (sAutoStartActions != null)
            return sAutoStartActions;

        List<String> actions = new ArrayList<>();
        actions.add("android.intent.action.PRE_BOOT_COMPLETED");
        actions.add("android.intent.action.BOOT_COMPLETED");
        actions.add("android.net.conn.CONNECTIVITY_CHANGE");
        actions.add("android.intent.action.AIRPLANE_MODE");
        actions.add("android.intent.action.BATTERY_CHANGED");
        actions.add("android.intent.action.BATTERY_LOW");
        actions.add("android.intent.action.BATTERY_OKAY");
        actions.add("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        actions.add("android.intent.action.CONFIGURATION_CHANGED");
        actions.add("android.intent.action.LOCALE_CHANGED");
        actions.add("android.intent.action.DATE_CHANGED");
        actions.add("android.intent.action.DEVICE_STORAGE_LOW");
        actions.add("android.intent.action.DEVICE_STORAGE_OK");
        actions.add("android.intent.action.GTALK_CONNECTED");
        actions.add("android.intent.action.GTALK_DISCONNECTED");
        actions.add("android.intent.action.HEADSET_PLUG");
        actions.add("android.intent.action.INPUT_METHOD_CHANGED");
        actions.add("android.intent.action.MANAGE_PACKAGE_STORAGE");
        actions.add("android.intent.action.CAMERA_BUTTON");
        actions.add("android.intent.action.MEDIA_BUTTON");
        actions.add("android.intent.action.MEDIA_BAD_REMOVAL");
        actions.add("android.intent.action.MEDIA_CHECKING");
        actions.add("android.intent.action.MEDIA_EJECT");
        actions.add("android.intent.action.MEDIA_MOUNTED");
        actions.add("android.intent.action.MEDIA_NOFS");
        actions.add("android.intent.action.MEDIA_REMOVED");
        actions.add("android.intent.action.MEDIA_SCANNER_FINISHED");
        actions.add("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        actions.add("android.intent.action.MEDIA_SCANNER_STARTED");
        actions.add("android.intent.action.MEDIA_SHARED");
        actions.add("android.intent.action.MEDIA_UNMOUNTABLE");
        actions.add("android.intent.action.MEDIA_UNMOUNTED");
        actions.add("android.intent.action.NEW_OUTGOING_CALL");
        actions.add("android.intent.action.PACKAGE_ADDED");
        actions.add("android.intent.action.PACKAGE_CHANGED");
        actions.add("android.intent.action.PACKAGE_DATA_CLEARED");
        actions.add("android.intent.action.PACKAGE_INSTALL");
        actions.add("android.intent.action.PACKAGE_REMOVED");
        actions.add("android.intent.action.PACKAGE_REPLACED");
        actions.add("android.intent.action.PACKAGE_RESTARTED");
        actions.add("android.intent.action.PROVIDER_CHANGED");
        actions.add("android.intent.action.REBOOT");
        actions.add("android.intent.action.SCREEN_OFF");
        actions.add("android.intent.action.SCREEN_ON");
        actions.add("android.intent.action.TIMEZONE_CHANGED");
        actions.add("android.intent.action.TIME_SET");
        actions.add("android.intent.action.TIME_TICK");
        actions.add("android.intent.action.UID_REMOVED");
        actions.add("android.intent.action.UMS_CONNECTED");
        actions.add("android.intent.action.UMS_DISCONNECTED");
        actions.add("android.intent.action.USER_PRESENT");
        actions.add("android.intent.action.WALLPAPER_CHANGED");
        actions.add("android.intent.action.ACTION_POWER_CONNECTED");
        actions.add("android.intent.action.ACTION_POWER_DISCONNECTED");
        actions.add("android.intent.action.ACTION_SHUTDOWN");
        actions.add("android.intent.action.DOCK_EVENT");
        actions.add("android.intent.action.ANR");
        actions.add("android.intent.action.EVENT_REMINDER");
        actions.add("android.accounts.LOGIN_ACCOUNTS_CHANGED");
        actions.add("android.intent.action.STATISTICS_REPORT");
        actions.add("android.intent.action.MASTER_CLEAR");
        actions.add("com.android.sync.SYNC_CONN_STATUS_CHANGED");
        actions.add("android.bluetooth.headset.action.STATE_CHANGED");
        actions.add("android.intent.action.PROXY_CHANGE");
        actions.add("android.search.action.SETTINGS_CHANGED");
        actions.add("android.search.action.SEARCHABLES_CHANGED");
        actions.add("android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED");
        actions.add("android.intent.action.DOWNLOAD_COMPLETED");
        actions.add("android.location.PROVIDERS_CHANGED");
        actions.add("android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION");
        actions.add("android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION");
        actions.add("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
        actions.add("android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE");
        actions.add("android.app.action.ACTION_PASSWORD_CHANGED");
        actions.add("android.app.action.ACTION_PASSWORD_FAILED");
        actions.add("android.app.action.ACTION_PASSWORD_SUCCEEDED");
        actions.add("android.app.action.DEVICE_ADMIN_DISABLED");
        actions.add("android.app.action.DEVICE_ADMIN_DISABLE_REQUESTED");
        actions.add("android.app.action.DEVICE_ADMIN_ENABLED");
        actions.add("android.app.action.ACTION_PASSWORD_EXPIRING");
        actions.add("com.android.launcher.action.INSTALL_SHORTCUT");
        actions.add("com.android.launcher.action.UNINSTALL_SHORTCUT");
        actions.add("com.android.camera.NEW_PICTURE");
        actions.add("android.net.conn.BACKGROUND_DATA_SETTING_CHANGED");
        actions.add("android.intent.action.PHONE_STATE");
        actions.add("android.intent.action.SERVICE_STATE");
        actions.add("android.intent.action.ANY_DATA_STATE");
        actions.add("android.intent.action.SIG_STR");
        actions.add("android.intent.action.DATA_CONNECTION_FAILED");
        actions.add("android.intent.action.NETWORK_SET_TIME");
        actions.add("ndroid.intent.action.NETWORK_SET_TIMEZONE");
        actions.add("android.intent.action.SIM_STATE_CHANGED");
        actions.add("android.provider.Telephony.SIM_FULL");
        actions.add("android.provider.Telephony.SMS_RECEIVED");
        actions.add("android.intent.action.DATA_SMS_RECEIVED");
        actions.add("android.provider.Telephony.SMS_REJECTED");
        actions.add("android.provider.Telephony.WAP_PUSH_RECEIVED");
        actions.add("android.provider.Telephony.SECRET_CODE");
        actions.add("android.provider.Telephony.SPN_STRINGS_UPDATED");
        actions.add("android.net.wifi.WIFI_STATE_CHANGED");
        actions.add("android.net.wifi.NETWORK_IDS_CHANGED");
        actions.add("android.net.wifi.RSSI_CHANGED");
        actions.add("android.net.wifi.SCAN_RESULTS");
        actions.add("android.net.wifi.STATE_CHANGE");
        actions.add("android.net.wifi.supplicant.CONNECTION_CHANGE");
        actions.add("android.net.wifi.supplicant.STATE_CHANGE");
        actions.add("android.media.RINGER_MODE_CHANGED");
        actions.add("android.media.VIBRATE_SETTING_CHANGED");
        actions.add("android.media.AUDIO_BECOMING_NOISY");
        actions.add("android.speech.tts.TTS_QUEUE_PROCESSING_COMPLETED");
        actions.add("android.speech.tts.engine.TTS_DATA_INSTALLED");
        actions.add("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        actions.add("android.bluetooth.adapter.action.DISCOVERY_STARTED");
        actions.add("android.bluetooth.adapter.action.LOCAL_NAME_CHANGED");
        actions.add("android.bluetooth.adapter.action.SCAN_MODE_CHANGED");
        actions.add("android.bluetooth.adapter.action.STATE_CHANGED");
        actions.add("android.bluetooth.device.action.PAIRING_REQUEST");
        actions.add("android.bluetooth.device.action.PAIRING_CANCEL");
        actions.add("android.bluetooth.device.action.ACL_CONNECTED");
        actions.add("android.bluetooth.device.action.ACL_DISCONNECTED");
        actions.add("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED");
        actions.add("android.bluetooth.device.action.BOND_STATE_CHANGED");
        actions.add("android.bluetooth.device.action.CLASS_CHANGED");
        actions.add("android.bluetooth.device.action.FOUND");
        actions.add("android.bluetooth.device.action.NAME_CHANGED");
        actions.add("android.bluetooth.devicepicker.action.DEVICE_SELECTED");
        actions.add("android.bluetooth.devicepicker.action.LAUNCH");
        actions.add("android.bluetooth.headset.action.AUDIO_STATE_CHANGED");
        actions.add("android.bluetooth.headset.action.STATE_CHANGED");
        actions.add("android.bluetooth.a2dp.action.SINK_STATE_CHANGED");
        actions.add("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGE");
        actions.add("android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED");
        actions.add("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED");
        actions.add("android.bluetooth.headset.profile.action.AUDIO_STATE_CHANGED");
        actions.add("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
        actions.add("android.bluetooth.headset.action.VENDOR_SPECIFIC_HEADSET_EVENT");
        actions.add("android.bluetooth.a2dp.intent.action.SINK_STATE_CHANGED");
        actions.add("android.bluetooth.intent.action.DISCOVERY_COMPLETED");
        actions.add("android.bluetooth.intent.action.DISCOVERY_STARTED");
        actions.add("android.bluetooth.intent.action.HEADSET_STATE_CHANGED");
        actions.add("android.bluetooth.intent.action.NAME_CHANGED");
        actions.add("android.bluetooth.intent.action.PAIRING_REQUEST");
        actions.add("android.bluetooth.intent.action.PAIRING_CANCEL");
        actions.add("android.bluetooth.intent.action.REMOTE_DEVICE_CONNECTED");
        actions.add("android.bluetooth.intent.action.REMOTE_DEVICE_DISAPPEARED");
        actions.add("android.bluetooth.intent.action.REMOTE_DEVICE_DISCONNECTED");
        actions.add("android.bluetooth.intent.action.REMOTE_DEVICE_DISCONNECT_REQUESTED");
        actions.add("android.bluetooth.intent.action.REMOTE_DEVICE_FOUND");
        actions.add("android.bluetooth.intent.action.REMOTE_NAME_FAILED");
        actions.add("android.bluetooth.intent.action.REMOTE_NAME_UPDATED");
        actions.add("android.bluetooth.intent.action.BLUETOOTH_STATE_CHANGED");
        actions.add("android.bluetooth.intent.action.BOND_STATE_CHANGED_ACTION");
        actions.add("android.bluetooth.intent.action.HEADSET_ADUIO_STATE_CHANGED");
        actions.add("android.bluetooth.intent.action.SCAN_MODE_CHANGED");
        actions.add("android.bluetooth.intent.action.BONDING_CREATED");
        actions.add("android.bluetooth.intent.action.BONDING_REMOVED");
        actions.add("android.bluetooth.intent.action.DISABLED");
        actions.add("android.bluetooth.intent.action.ENABLED");
        actions.add("android.bluetooth.intent.action.MODE_CHANGED");
        actions.add("android.bluetooth.intent.action.REMOTE_ALIAS_CHANGED");
        actions.add("android.bluetooth.intent.action.REMOTE_ALIAS_CLEARED");

        sAutoStartActions = actions;
        return sAutoStartActions;
    }

}
