package com.polestar.superclone.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.polestar.billing.BillingProvider;
import com.polestar.superclone.MApp;
import com.polestar.superclone.constant.AppConstants;
import com.polestar.superclone.reward.AppUser;

import java.io.File;

/**
 * Created by yxx on 2016/7/15.
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

    public static void setCloneGuideShowed(Context c){
        putBoolean(c, AppConstants.PreferencesKey.SHOWN_CLONE_GUIDE, true);
    }

    public static boolean hasShownCloneGuide(Context c){
        return getBoolean(c, AppConstants.PreferencesKey.SHOWN_CLONE_GUIDE, false);
    }

    public static boolean hasShownLongClickGuide(Context c){
        return getBoolean(c, AppConstants.PreferencesKey.SHOWN_LONG_CLICK_GUIDE, false);
    }

    public static void setLongClickGuideShowed(Context c){
        putBoolean(c, AppConstants.PreferencesKey.SHOWN_LONG_CLICK_GUIDE, true);
    }

    public static String getEncodedPatternPassword(Context c) {
        return getString(c, AppConstants.PreferencesKey.ENCODED_PATTERN_PWD);
    }

    public static void setEncodedPatternPassword(Context c, String pwd) {
        putString(c, AppConstants.PreferencesKey.ENCODED_PATTERN_PWD, pwd );
    }

    public static void setCustomizedQuestion(Context c, String strQuestion) {
        putString(c, AppConstants.PreferencesKey.CUSTOMIZED_SAFE_QUESTION, strQuestion);
    }

    public static String getCustomizedQuestion(Context c) {
        return getString(c, AppConstants.PreferencesKey.CUSTOMIZED_SAFE_QUESTION, null);
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
        putInt(MApp.getApp(),"love_app", b? 1:-1);
    }

    public static int getLoveApp() {
        return getInt(MApp.getApp(),"love_app", 0);
    }

    public static void setRated(boolean b) {
        putBoolean(MApp.getApp(),"is_rated", b);
    }

    public static boolean isRated() {
        return getBoolean(MApp.getApp(),"is_rated", false);
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
        long last = getLong(MApp.getApp(), "auto_interstitial",-1);
        if (last == -1) {
            last = CommonUtils.getInstallTime(MApp.getApp(), MApp.getApp().getPackageName());
        }
        return last;
    }

    public static void updateAutoInterstialTime() {
        putLong(MApp.getApp(),"auto_interstitial", System.currentTimeMillis() );
    }

    public static long getLastIconAdClickTime(Context c) {
        return getLong(c, "last_icon_ad_click",-1);
    }

    public static boolean isFirstStart(String name) {
        return getLong(MApp.getApp(), name+"_first_start", -1) == -1;
    }

    public static void setStarted(String name) {
        putLong(MApp.getApp(), name+"_first_start", System.currentTimeMillis());
    }

    public static void resetStarted(String name) {
        putLong(MApp.getApp(), name+"_first_start", -1);
    }

    public static void setShortCutCreated() {
        putBoolean(MApp.getApp(),"super_clone_shortcut",true);
    }

    public static boolean isShortCutCreated() {
        return getBoolean(MApp.getApp(),"super_clone_shortcut",false)
                || hasShownCloneGuide(MApp.getApp());
        //avoid create short cut for updated user
    }

    public static void setInstallChannel(String channel) {
        putString(MApp.getApp(), "install_channel", channel);
    }

    public static String getInstallChannel() {
        return getString(MApp.getApp(), "install_channel", "not_set");
    }

    public static void setGMSEnable(boolean enable) {
        File stateFile = new File(MApp.getApp().getFilesDir(), "gms_disable");
        try {
            if (!enable) {
                stateFile.createNewFile();
            } else {
                stateFile.delete();
            }
        } catch (Throwable e) {
            MLogs.logBug(MLogs.getStackTraceString(e));
        }
        EventReporter.setUserProperty(EventReporter.PROP_GMS, enable?"on":"off");
    }

    public static boolean isGMSEnable() {
        File stateFile = new File(MApp.getApp().getFilesDir(), "gms_disable");
        return !stateFile.exists();
    }

    public static boolean isAdFree() {
        boolean purchased =  isVIP();

        boolean rewarded = AppUser.isRewardEnabled() && AppUser.getInstance().checkAdFree();
        return purchased || rewarded;
    }

    public static boolean isVIP() {
        return getBoolean(MApp.getApp(), "isVIP" , false);
    }

    public static void setVIP(boolean enable) {
        putBoolean(MApp.getApp(), "isVIP" , enable);
    }

    public static long getLastAdFreeDialogTime() {
        return getLong(MApp.getApp(), "ad_free_dialog_time", CommonUtils.getInstallTime(MApp.getApp(), MApp.getApp().getPackageName()));
    }
    public static void updateLastAdFreeDialogTime() {
        putLong(MApp.getApp(), "ad_free_dialog_time", System.currentTimeMillis());
    }
    public static void updateAdFreeClickStatus(boolean status) {
        putBoolean(MApp.getApp(), "ad_free_dialog_click", status);
    }

    public static boolean getAdFreeClickStatus() {
        return getBoolean(MApp.getApp(), "ad_free_dialog_click", true);
    }

    public static boolean hasCloned() {
        return getBoolean(MApp.getApp(), "spc_ever_cloned", false)
                || hasShownLongClickGuide(MApp.getApp());
    }

    public static void setHasCloned() {
        putBoolean(MApp.getApp(), "spc_ever_cloned", true);
    }

    public static long getLockInterval() {
        return getLong(MApp.getApp(), "relock_interval", 5*1000);
    }

    public static void setLockInterval(long val) {
        putLong(MApp.getApp(), "relock_interval", val);
    }

    public static void ignoreVersion(long code) {
        putLong(MApp.getApp(),"ignore_version", code);
    }

    public static long getIgnoreVersion() {
        return getLong(MApp.getApp(), "ignore_version", -1);
    }

    public static void updateActiveTime(boolean fg) {
        putLong(MApp.getApp(), fg? "fg_active": "bg_active", System.currentTimeMillis());
    }

    public static boolean needReportActive(boolean fg) {
        long time =  getLong(MApp.getApp(), fg? "fg_active": "bg_active", 0);
        return System.currentTimeMillis() - time > 12*60*60*1000;
    }

    public static boolean hasShownPermissionGuide() {
        return getBoolean(MApp.getApp(), "shown_permission_guide");
    }


    public static void setShownPermissionGuide(boolean shown) {
        putBoolean(MApp.getApp(), "shown_permission_guide", shown);
    }

    public static void setShownDeleteDialog() {
        putBoolean(MApp.getApp(), "shown_delete_dialog", true);
    }

    public static boolean hasShownDeleteDialog() {
        return getBoolean(MApp.getApp(), "shown_delete_dialog", false);
    }

    public static void setFingerprint(boolean enable) {
        putBoolean(MApp.getApp(), "use_fingerprint", enable);
    }

    public static boolean isFingerprintEnable() {
        return getBoolean(MApp.getApp(), "use_fingerprint", true);
    }

    public static long getLastGuideQuickSwitchTime() {
        return getLong(MApp.getApp(), "guide_fast_switch_last", 0);
    }

    public static void updateLastGuideQuickSwitchTime() {
        putLong(MApp.getApp(), "guide_fast_switch_last", System.currentTimeMillis());
    }

    public static void incGuideQuickSwitchTimes() {
        putInt(MApp.getApp(), "guide_fast_switch_cnt", 1 + getGuideQuickSwitchTimes());
    }

    public static int getGuideQuickSwitchTimes( ) {
        return getInt(MApp.getApp(), "guide_fast_switch_cnt", 0);
    }

    public static boolean isIntercepted() {
        return getBoolean(MApp.getApp(), "key_intercepted" , false);
    }

    public static boolean setIntercepted() {
        return putBoolean(MApp.getApp(), "key_intercepted", true);
    }

    public static void setGlobalNotification(boolean status) {
        PreferencesUtils.putBoolean(MApp.getApp(), "key_server_push", status);
    }

    public static boolean isGlobalNotificationEnabled() {
        return PreferencesUtils.getBoolean(MApp.getApp(), "key_server_push", true);
    }
}
