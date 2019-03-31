package com.polestar.clone.client.env;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.helper.utils.VLog;
import com.polestar.clone.CloneAgent64;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import mirror.android.webkit.IWebViewUpdateService;
import mirror.android.webkit.WebViewFactory;

/**
 * @author Lody
 */
public final class SpecialComponentList {

    private static final List<String> ACTION_BLACK_LIST = new ArrayList<String>(1);
    private static final Map<String, String> PROTECTED_ACTION_MAP = new HashMap<>(5);
    private static final HashSet<String> WHITE_PERMISSION = new HashSet<>(3);
    private static final HashSet<String> INSTRUMENTATION_CONFLICTING = new HashSet<>(2);
    private static final HashSet<String> SPEC_SYSTEM_APP_LIST = new HashSet<>(3);
    private static final Set<String> SYSTEM_BROADCAST_ACTION = new HashSet<>(7);
    private static String PROTECT_ACTION_PREFIX = "_VA_protected_";
    public static String INTENT_CATEGORY_RESOLVE = "_PC_resolve_";
    public static final String REFERRER_ACTION = "com.android.vending.INSTALL_REFERRER";

    private static final HashSet<String> IO_REDIRECT_BLACK_LIST = new HashSet<>(1);

    private static final HashSet<String> BROADCAST_START_WHITE_LIST = new HashSet<>();

    private static final HashSet<String> PRE_INSTALL_PACKAGE_LIST = new HashSet<>();

    public static String APP_LOADING_ACTIVITY = null;
    static {
        PRE_INSTALL_PACKAGE_LIST.add("com.huawei.hwid");
        SYSTEM_BROADCAST_ACTION.add(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_SCREEN_ON);
//        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_SCREEN_OFF);
//        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_NEW_OUTGOING_CALL);
//        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_TIME_TICK);
        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_BOOT_COMPLETED);
//        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_TIME_CHANGED);
        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_TIMEZONE_CHANGED);
        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_BATTERY_CHANGED);
//        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_BATTERY_LOW);
//        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_BATTERY_OKAY);
//        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_POWER_CONNECTED);
//        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_POWER_DISCONNECTED);
        SYSTEM_BROADCAST_ACTION.add(Intent.ACTION_USER_PRESENT);
        SYSTEM_BROADCAST_ACTION.add("android.provider.Telephony.SMS_RECEIVED");
        //SYSTEM_BROADCAST_ACTION.add(REFERRER_ACTION);
        //SYSTEM_BROADCAST_ACTION.add("android.provider.Telephony.SMS_DELIVER");
        SYSTEM_BROADCAST_ACTION.add("android.net.wifi.STATE_CHANGE");
        //SYSTEM_BROADCAST_ACTION.add("android.net.wifi.SCAN_RESULTS");
        SYSTEM_BROADCAST_ACTION.add("android.net.wifi.WIFI_STATE_CHANGED");
        SYSTEM_BROADCAST_ACTION.add("android.net.conn.CONNECTIVITY_CHANGE");
        SYSTEM_BROADCAST_ACTION.add("android.intent.action.ANY_DATA_STATE");
        SYSTEM_BROADCAST_ACTION.add("android.intent.action.SIM_STATE_CHANGED");
        SYSTEM_BROADCAST_ACTION.add("android.location.PROVIDERS_CHANGED");
        SYSTEM_BROADCAST_ACTION.add("android.location.MODE_CHANGED");
        SYSTEM_BROADCAST_ACTION.add("android.media.RINGER_MODE_CHANGED");
        SYSTEM_BROADCAST_ACTION.add("android.media.action.HDMI_AUDIO_PLUG");
        SYSTEM_BROADCAST_ACTION.add("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");


//        SYSTEM_STICKY_BROADCAST_ACTION.add("android.net.conn.CONNECTIVITY_CHANGE");
//        SYSTEM_STICKY_BROADCAST_ACTION.add("android.net.wifi.WIFI_STATE_CHANGED");
//        SYSTEM_STICKY_BROADCAST_ACTION.add(Intent.ACTION_BATTERY_CHANGED);
//        SYSTEM_STICKY_BROADCAST_ACTION.add("android.intent.action.ANY_DATA_STATE");
//        SYSTEM_STICKY_BROADCAST_ACTION.add("android.media.RINGER_MODE_CHANGED");
//        SYSTEM_STICKY_BROADCAST_ACTION.add("android.media.action.HDMI_AUDIO_PLUG");
//        SYSTEM_STICKY_BROADCAST_ACTION.add("android.media.ACTION_SCO_AUDIO_STATE_UPDATED");
//        SYSTEM_STICKY_BROADCAST_ACTION.add(Intent.ACTION_DEVICE_STORAGE_LOW);
//        SYSTEM_STICKY_BROADCAST_ACTION.add(Intent.ACTION_DOCK_EVENT);
//        SYSTEM_STICKY_BROADCAST_ACTION.add("android.intent.action.DEVICE_STORAGE_FULL");

        IO_REDIRECT_BLACK_LIST.add("com.snapchat.android");

        ACTION_BLACK_LIST.add("android.appwidget.action.APPWIDGET_UPDATE");
        ACTION_BLACK_LIST.add("android.appwidget.action.APPWIDGET_CONFIGURE");
        ACTION_BLACK_LIST.add("android.provider.Telephony.SMS_RECEIVED");

//        ACTION_BLACK_LIST.add("com.facebook.GET_PHONE_ID");

        WHITE_PERMISSION.add("com.google.android.gms.settings.SECURITY_SETTINGS");
        WHITE_PERMISSION.add("com.google.android.apps.plus.PRIVACY_SETTINGS");
        WHITE_PERMISSION.add(Manifest.permission.ACCOUNT_MANAGER);

        PROTECTED_ACTION_MAP.put(Intent.ACTION_PACKAGE_ADDED, Constants.ACTION_PACKAGE_ADDED);
        PROTECTED_ACTION_MAP.put(Intent.ACTION_PACKAGE_REMOVED, Constants.ACTION_PACKAGE_REMOVED);
        PROTECTED_ACTION_MAP.put(Intent.ACTION_PACKAGE_CHANGED, Constants.ACTION_PACKAGE_CHANGED);
        PROTECTED_ACTION_MAP.put("android.intent.action.USER_ADDED", Constants.ACTION_USER_ADDED);
        PROTECTED_ACTION_MAP.put("android.intent.action.USER_REMOVED", Constants.ACTION_USER_REMOVED);
//        PROTECTED_ACTION_MAP.put("android.intent.action.MEDIA_SCANNER_SCAN_FILE", "android.intent.action.MEDIA_SCANNER_SCAN_FILE");

        INSTRUMENTATION_CONFLICTING.add("com.qihoo.magic");
        INSTRUMENTATION_CONFLICTING.add("com.qihoo.magic_mutiple");
        //INSTRUMENTATION_CONFLICTING.add("com.facebook.katana");

        SPEC_SYSTEM_APP_LIST.add("android");
        SPEC_SYSTEM_APP_LIST.add("com.google.android.webview");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                String webViewPkgN = IWebViewUpdateService.getCurrentWebViewPackageName.call(WebViewFactory.getUpdateService.call());
                if (webViewPkgN != null) {
                    SPEC_SYSTEM_APP_LIST.add(webViewPkgN);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

//        BROADCAST_START_WHITE_LIST.add("com.facebook.orca");
//        BROADCAST_START_WHITE_LIST.add("com.facebook.katana");
//        BROADCAST_START_WHITE_LIST.add("com.tencent.mm");
//        BROADCAST_START_WHITE_LIST.add("com.whatsapp");
        BROADCAST_START_WHITE_LIST.add("com.google.android.gsf");
//        BROADCAST_START_WHITE_LIST.add("com.google.android.gms");
//        BROADCAST_START_WHITE_LIST.add("com.google.android.gsf.login");
//        BROADCAST_START_WHITE_LIST.add("com.android.vending");
//        BROADCAST_START_WHITE_LIST.add("com.google.android.play.games");
    }

    public static boolean needIORedirect(String pkg) {
        if (IO_REDIRECT_BLACK_LIST.contains(pkg)) {
            return false;
        }
        return  true;
    }

    public static boolean canStartFromBroadcast(String pkg, String action) {
//        if (GmsSupport.isGmsFamilyPackage(pkg)) {
//            return true;
//        }
        if (action != null && action.contains("com.android.vending.INSTALL_REFERRER")){
            return true;
        }
        if (!BROADCAST_START_WHITE_LIST.contains(pkg)) {
            return false;
        }
        boolean arm64 = CloneAgent64.needArm64Support(VirtualCore.get().getContext(), pkg);
        if (arm64 && !VirtualCore.get().getContext().getPackageName().endsWith("arm64")) {
            BROADCAST_START_WHITE_LIST.remove(pkg);
            VLog.logbug("CloneAgent","Remove not supported: " + pkg);
            return false;
        }
        return true;
    }
    public static boolean isSpecSystemPackage(String pkg) {
        return SPEC_SYSTEM_APP_LIST.contains(pkg);
    }

    public static boolean isConflictingInstrumentation(String packageName) {
        return INSTRUMENTATION_CONFLICTING.contains(packageName);
    }

    public static Set<String> getPreInstallPackages() {
        return PRE_INSTALL_PACKAGE_LIST;
    }

    /**
     * Check if the action in the BlackList.
     *
     * @param action Action
     */
    public static boolean isActionInBlackList(String action) {
        return ACTION_BLACK_LIST.contains(action);
    }

    /**
     * Add an action to the BlackList.
     *
     * @param action action
     */
    public static void addBlackAction(String action) {
        ACTION_BLACK_LIST.add(action);
    }

    public static void protectIntentFilter(IntentFilter filter, String pkg) {
        if (filter != null) {
            List<String> actions = mirror.android.content.IntentFilter.mActions.get(filter);
            ListIterator<String> iterator = actions.listIterator();
            while (iterator.hasNext()) {
                String action = iterator.next();
                if (SpecialComponentList.isActionInBlackList(action)) {
                    iterator.remove();
                    continue;
                }
                if (SYSTEM_BROADCAST_ACTION.contains(action) && !Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                    continue;
                }
                String newAction = SpecialComponentList.protectAction(action);
                if (newAction != null) {
                    iterator.set(newAction);
                }
            }
        }
    }

    public static void protectIntent(Intent intent) {
        String protectAction = protectAction(intent.getAction());
        if (protectAction != null) {
            intent.setAction(protectAction);
        }
    }

    public static void unprotectIntent(Intent intent) {
        String unprotectAction = unprotectAction(intent.getAction());
        if (unprotectAction != null) {
            intent.setAction(unprotectAction);
        }
    }

    public static String protectAction(String originAction) {
        if (originAction == null) {
            return null;
        }
        if (originAction.startsWith("_VA_") || originAction.startsWith(Constants.VIRTUAL_PROTECT_INTENT_PREFIX)) {
            return originAction;
        }
        String newAction = PROTECTED_ACTION_MAP.get(originAction);
        if (newAction == null) {
            newAction = PROTECT_ACTION_PREFIX + originAction;
        }
        return newAction;
    }

    public static String unprotectAction(String action) {
        if (action == null) {
            return null;
        }
        if (action.startsWith(PROTECT_ACTION_PREFIX)) {
            return action.substring(PROTECT_ACTION_PREFIX.length());
        }
        for (Map.Entry<String, String> next : PROTECTED_ACTION_MAP.entrySet()) {
            String modifiedAction = next.getValue();
            if (modifiedAction.equals(action)) {
                return next.getKey();
            }
        }
        return action;
    }

    public static boolean isWhitePermission(String permission) {
        return WHITE_PERMISSION.contains(permission) || permission.startsWith("com.google");
    }
}
