package winterfell.flash.vpn.utils;

import android.content.Context;
import android.content.SharedPreferences;

import winterfell.flash.vpn.reward.network.datamodels.VpnServer;

import winterfell.flash.vpn.FlashApp;

/**
 * Created by doriscoco on 2017/4/4.
 */

public class PreferenceUtils {

    public static String PREFERENCE_NAME = "mine_sweeper_classic";

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



    public static void setLoveApp(  boolean b) {
        putInt(FlashApp.getApp(),"love_app", b? 1:-1);
    }

    public static int getLoveApp() {
        return getInt(FlashApp.getApp(),"love_app", 0);
    }

    public static void setRated(boolean b) {
        putBoolean(FlashApp.getApp(),"is_rated", b);
    }

    public static boolean isRated() {
        return getBoolean(FlashApp.getApp(),"is_rated", false);
    }

    public static void updateRateDialogTime(Context c) {
        putLong(c, "last_rate_dialog", System.currentTimeMillis());
    }

    public static long getRateDialogTime(Context c) {
        long last = getLong(c, "last_rate_dialog",-1);
        if (last == -1) {
            last = CommonUtils.getInstallTime(c, c.getPackageName());
        }
        return last;
    }

    public static boolean hasShownRateDialog(Context c) {
        return getLong(c, "last_rate_dialog",-1) != -1;
    }

    public static boolean isShortCutCreated() {
        return getBoolean(FlashApp.getApp(),"super_clone_shortcut",false);
        //avoid create short cut for updated user
    }

    public static void setShortCutCreated() {
        putBoolean(FlashApp.getApp(),"super_clone_shortcut",true);
    }

    public static void ignoreVersion(long code) {
        putLong(FlashApp.getApp(),"ignore_version", code);
    }

    public static long getIgnoreVersion() {
        return getLong(FlashApp.getApp(), "ignore_version", -1);
    }

    public static boolean isGlobalVPN() {
        return  getBoolean(FlashApp.getApp(), "global_vpn", true);
    }

    public static void setGlobalVPN(boolean enable) {
        putBoolean(FlashApp.getApp(), "global_vpn", enable);
    }

    public  static String getServerList(){
        return getString(FlashApp.getApp(), "server_list", null);
    }

    public static void setServerList(String list) {
        putString(FlashApp.getApp(),"server_list", list);
    }

    public static int getPreferServer(){
        return getInt(FlashApp.getApp(), "prefer_server", VpnServer.SERVER_ID_AUTO);
    }

    public static void setPreferServer(int id) {
        putInt(FlashApp.getApp(), "prefer_server", id);
    }

    public static void updateLastVipDialogTime() {
        putLong(FlashApp.getApp(),"vip_dialog_time", System.currentTimeMillis());
    }

    public static void updateVipClickStatus(boolean b) {
        putBoolean(FlashApp.getApp(),"vip_dialog_click", b);
    }

    public static boolean isStartOnBoot() {
        return getBoolean(FlashApp.getApp(),"is_start_on_boot", false);
    }

    public static void setStartOnBoot(boolean enable) {
        putBoolean(FlashApp.getApp(), "is_start_on_boot", enable);
    }

    public static void setEnterAdTime(){
        putLong(FlashApp.getApp(),"enter_ad_time",System.currentTimeMillis());
    }

    public static long getEnterAdTime() {
        return getLong(FlashApp.getApp(), "enter_ad_time", 0);
    }

    public static void addReceiveBytes(long received) {
        putLong(FlashApp.getApp(), "received_bytes", getLong(FlashApp.getApp(), "received_bytes") + received);
    }

    public static long getReceiveBytes() {
        return getLong(FlashApp.getApp(), "received_bytes");
    }

    public static void addSentBytes(long sent) {
        putLong(FlashApp.getApp(), "sent_bytes", getLong(FlashApp.getApp(), "sent_bytes") + sent);
    }

    public static long getSentBytes() {
        return getLong(FlashApp.getApp(), "sent_bytes");
    }

    public static long getConnectedTimeSec() {
        return getLong(FlashApp.getApp(), "connected_time_sec", 0);
    }

    public static void addConnectedTimeSec(long sec) {
        putLong(FlashApp.getApp(), "connected_time_sec", getLong(FlashApp.getApp(), "connected_time_sec") + sec);
    }

    public static void updateLastUpdateTime() {
        putLong(FlashApp.getApp(),"last_update_time", System.currentTimeMillis());
    }

    public static long getLastUpdateTime() {
        return  getLong(FlashApp.getApp(),"last_update_time", 0);
    }
}
