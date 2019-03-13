package in.dualspace.cloner.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import in.dualspace.cloner.AppConstants;
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.billing.BillingProvider;

import java.io.File;

/**
 * Created by DualApp on 2016/7/15.
 */
public class PreferencesUtils {

    public static String PREFERENCE_NAME = AppConstants.PREFERENCE_NAME;

    private PreferencesUtils() {
        throw new AssertionError();
    }

    /**
     * put string preferences
     *
     * @param context
     * @param key     The name of the preference to modify
     * @param value   The new value for the preference
     * @return True if the new values were successfully written to persistent storage.
     */
    public static boolean putString(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * get string preferences
     *
     * @param context
     * @param key     The name of the preference to retrieve
     * @return The preference value if it exists, or null. Throws ClassCastException if there is a preference with this
     * name that is not a string
     * @see #getString(Context, String, String)
     */
    public static String getString(Context context, String key) {
        return getString(context, key, null);
    }

    /**
     * get string preferences
     *
     * @param context
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a string
     */
    public static String getString(Context context, String key, String defaultValue) {
        if (context == null) return defaultValue;
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getString(key, defaultValue);
    }

    /**
     * put int preferences
     *
     * @param context
     * @param key     The name of the preference to modify
     * @param value   The new value for the preference
     * @return True if the new values were successfully written to persistent storage.
     */
    public static boolean putInt(Context context, String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    /**
     * get int preferences
     *
     * @param context
     * @param key     The name of the preference to retrieve
     * @return The preference value if it exists, or -1. Throws ClassCastException if there is a preference with this
     * name that is not a int
     * @see #getInt(Context, String, int)
     */
    public static int getInt(Context context, String key) {
        return getInt(context, key, -1);
    }

    /**
     * get int preferences
     *
     * @param context
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a int
     */
    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getInt(key, defaultValue);
    }

    /**
     * put long preferences
     *
     * @param context
     * @param key     The name of the preference to modify
     * @param value   The new value for the preference
     * @return True if the new values were successfully written to persistent storage.
     */
    public static boolean putLong(Context context, String key, long value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    /**
     * get long preferences
     *
     * @param context
     * @param key     The name of the preference to retrieve
     * @return The preference value if it exists, or -1. Throws ClassCastException if there is a preference with this
     * name that is not a long
     * @see #getLong(Context, String, long)
     */
    public static long getLong(Context context, String key) {
        return getLong(context, key, -1);
    }

    /**
     * get long preferences
     *
     * @param context
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a long
     */
    public static long getLong(Context context, String key, long defaultValue) {
        if (context == null) return defaultValue;
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getLong(key, defaultValue);
    }

    /**
     * put float preferences
     *
     * @param context
     * @param key     The name of the preference to modify
     * @param value   The new value for the preference
     * @return True if the new values were successfully written to persistent storage.
     */
    public static boolean putFloat(Context context, String key, float value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    /**
     * get float preferences
     *
     * @param context
     * @param key     The name of the preference to retrieve
     * @return The preference value if it exists, or -1. Throws ClassCastException if there is a preference with this
     * name that is not a float
     * @see #getFloat(Context, String, float)
     */
    public static float getFloat(Context context, String key) {
        return getFloat(context, key, -1);
    }

    /**
     * get float preferences
     *
     * @param context
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a float
     */
    public static float getFloat(Context context, String key, float defaultValue) {
        if (context == null) return defaultValue;
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getFloat(key, defaultValue);
    }

    /**
     * put boolean preferences
     *
     * @param context
     * @param key     The name of the preference to modify
     * @param value   The new value for the preference
     * @return True if the new values were successfully written to persistent storage.
     */
    public static boolean putBoolean(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /**
     * get boolean preferences, default is false
     *
     * @param context
     * @param key     The name of the preference to retrieve
     * @return The preference value if it exists, or false. Throws ClassCastException if there is a preference with this
     * name that is not a boolean
     * @see #getBoolean(Context, String, boolean)
     */
    public static boolean getBoolean(Context context, String key) {
        return getBoolean(context, key, false);
    }

    /**
     * get boolean preferences
     *
     * @param context
     * @param key          The name of the preference to retrieve
     * @param defaultValue Value to return if this preference does not exist
     * @return The preference value if it exists, or defValue. Throws ClassCastException if there is a preference with
     * this name that is not a boolean
     */
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        if (context == null) return  defaultValue;
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return settings.getBoolean(key, defaultValue);
    }

    public static String getEncodedPatternPassword(Context c) {
        return getString(c, AppConstants.PreferencesKey.ENCODED_PATTERN_PWD);
    }

    public static void setEncodedPatternPassword(Context c, String pwd) {
        putString(c, AppConstants.PreferencesKey.ENCODED_PATTERN_PWD, pwd );
    }

    public static void setLockScreen(Context c, boolean b) {
        putBoolean(c,AppConstants.PreferencesKey.IS_LOCKER_SCREEN, b);
    }

    public static boolean getAppLockInVisiablePatternPath(Context c) {
        return getBoolean(c, AppConstants.PreferencesKey.APP_LOCK_INVISIBLE_PATTERN_PATH, false);
    }

    public static void setLockerEnabled(Context c, boolean enabled) {
        putBoolean(c,AppConstants.PreferencesKey.LOCKER_FEATURE_ENABLED,enabled);
    }

    public static boolean isLockerEnabled(Context c) {
        return getBoolean(c,AppConstants.PreferencesKey.LOCKER_FEATURE_ENABLED,false);
    }

    public static void setLoveApp(  boolean b) {
        putInt(DualApp.getApp(),"love_app", b? 1:-1);
    }

    public static int getLoveApp() {
        return getInt(DualApp.getApp(),"love_app", 0);
    }

    public static int getGuideQuickSwitchTimes() {
        return getInt(DualApp.getApp(),"guide_quick_switch_times", 0);
    }

    public static void setGuideQuickSwitchTimes(int times) {
        putInt(DualApp.getApp(),"guide_quick_switch_times", times);
    }

    public static void updateLastGuideQuickSwitchTime() {
        putLong(DualApp.getApp(),"guide_quick_switch_last", System.currentTimeMillis());
    }

    public static long getLastGuideQuickSwitchTime() {
        return getLong(DualApp.getApp(),"guide_quick_switch_last", 0);
    }

    public static void setRated(boolean b) {
        putBoolean(DualApp.getApp(),"is_rated", b);
    }

    public static boolean isRated() {
        return getBoolean(DualApp.getApp(),"is_rated", false);
    }

    public static void updateRateDialogTime(Context c) {
        putLong(c, "last_rate_dialog", System.currentTimeMillis());
    }

    public static void updateIconAdClickTime(Context c) {
        putLong(c, "last_icon_ad_click", System.currentTimeMillis());
    }


    public static long getRateDialogTime(Context c) {
        long last = getLong(c, "last_rate_dialog",-1);
        if (last == -1) {
            last = CommonUtils.getInstallTime(c, c.getPackageName());
        }
        return last;
    }

    public static long getAutoInterstialTime() {
        long last = getLong(DualApp.getApp(), "auto_interstitial",-1);
        if (last == -1) {
            last = CommonUtils.getInstallTime(DualApp.getApp(), DualApp.getApp().getPackageName());
        }
        return last;
    }

    public static void updateAutoInterstialTime() {
        putLong(DualApp.getApp(),"auto_interstitial", System.currentTimeMillis() );
    }

    public static long getLastIconAdClickTime(Context c) {
        return getLong(c, "last_icon_ad_click",-1);
    }

    public static boolean isFirstStart(String name) {
        return getLong(DualApp.getApp(), name+"_first_start", -1) == -1;
    }

    public static void resetStarted(String name) {
        putLong(DualApp.getApp(), name+"_first_start", -1);
    }

    public static void setStarted(String name) {
        putLong(DualApp.getApp(), name+"_first_start", System.currentTimeMillis());
    }

    public static void setShortCutCreated() {
        putBoolean(DualApp.getApp(),"super_clone_shortcut",true);
    }

    public static boolean isShortCutCreated() {
        return getBoolean(DualApp.getApp(),"super_clone_shortcut",false);
        //avoid create short cut for updated user
    }

    public static void setInstallChannel(String channel) {
        putString(DualApp.getApp(), "install_channel", channel);
    }

    public static String getInstallChannel() {
        return getString(DualApp.getApp(), "install_channel", null);
    }

    public static void setLiteMode(boolean enable) {
        File stateFile = new File(DualApp.getApp().getFilesDir(), "lite_mode");
        try {
            if (enable) {
                stateFile.createNewFile();
            } else {
                stateFile.delete();
            }
        } catch (Throwable e) {
            MLogs.logBug(MLogs.getStackTraceString(e));
        }
    }

    public static boolean isLiteMode() {
        File stateFile = new File(DualApp.getApp().getFilesDir(), "lite_mode");
        return stateFile.exists();
    }

    public static void setStartPageStatus(boolean shown) {
        putBoolean(DualApp.getApp(), "start_page_status", shown);
    }

    public static boolean hasShownStartPage() {
        //return false;
        return getBoolean(DualApp.getApp(), "start_page_status");
    }

    public static boolean isAdFree() {
        return BillingProvider.get().isAdFreeVIP()? getBoolean(DualApp.getApp(), "ad_free") : false;
    }

    public static void setAdFree(boolean enable) {
        putBoolean(DualApp.getApp(), "ad_free" , enable);
    }

    public static long getLastAdFreeDialogTime() {
        return getLong(DualApp.getApp(), "ad_free_dialog_time", CommonUtils.getInstallTime(DualApp.getApp(), DualApp.getApp().getPackageName()));
    }
    public static void updateLastAdFreeDialogTime() {
        putLong(DualApp.getApp(), "ad_free_dialog_time", System.currentTimeMillis());
    }
    public static void updateAdFreeClickStatus(boolean status) {
        putBoolean(DualApp.getApp(), "ad_free_dialog_click", status);
    }

    public static boolean getAdFreeClickStatus() {
        return getBoolean(DualApp.getApp(), "ad_free_dialog_click", true);
    }

    public static void setHasCloned() {
        putBoolean(DualApp.getApp(), "spc_ever_cloned", true);
    }

    public static long getLockInterval() {
        return getLong(DualApp.getApp(), "relock_interval", 5*1000);
    }

    public static void setLockInterval(long val) {
        putLong(DualApp.getApp(), "relock_interval", val);
    }

    public static void ignoreVersion(long code) {
        putLong(DualApp.getApp(),"ignore_version", code);
    }

    public static long getIgnoreVersion() {
        return getLong(DualApp.getApp(), "ignore_version", -1);
    }

    public static boolean hasShownPermissionGuide() {
        return getBoolean(DualApp.getApp(), "shown_permission_guide");
    }


    public static void setShownPermissionGuide(boolean shown) {
        putBoolean(DualApp.getApp(), "shown_permission_guide", shown);
    }

    public static int getDeleteDialogTimes() {
        return getInt(DualApp.getApp(), "delete_dialog_times", 0);
    }

    public static void incDeleteDialogTimes() {
        putInt(DualApp.getApp(), "delete_dialog_times",
                1 + getInt(DualApp.getApp(), "delete_dialog_times", 0));
    }

    public static boolean isMainAppLocked() {
        return getBoolean(DualApp.getApp(), "lock_main_app", false);
    }

    public static void setLockMainApp(boolean enabled) {
         putBoolean(DualApp.getApp(), "lock_main_app", enabled);
    }

    public static boolean useFingerprint() {
        return getBoolean(DualApp.getApp(), "use_fingerprint", true);
    }

    public static void setUseFingerprint(boolean enabled) {
        putBoolean(DualApp.getApp(), "use_fingerprint", enabled);
    }
}
