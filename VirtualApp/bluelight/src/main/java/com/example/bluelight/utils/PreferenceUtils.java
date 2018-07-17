package com.example.bluelight.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;

import com.example.bluelight.R;

public class PreferenceUtils {
    public static final String KEY_AUTO_ENABLE_START_TIME = "auto_enable_start_time";
    public static final String KEY_AUTO_ENABLE_STOP_TIME = "auto_enable_stop_time";
    public static final String KEY_AUTO_ENABLE_SWITCH = "auto_enable_switch";
    public static final String KEY_COLOR_TEMPERATURE = "color_temperature";
    public static final String KEY_COLOR_TEMPERATURE_INDEX = "color_temperature_index";
    public static final String KEY_FILTER = "filter";
    public static final String KEY_FIRST_USE = "first_use";
    public static final String KEY_INTENSITY = "intensity";
    public static final String KEY_LAST_PROACTIVE_REPORT_TIME = "last_service_active_report_time";
    public static final String KEY_NOTIFICATION = "notification";
    public static final String KEY_SCREEN_DIM = "screen_dim";
    public static final String KEY_TORCH_STATE = "torch_state";
    private static final int INTENSITY_DEF = 30;
    private static final String PREFERENCE_NAME = "settings";
    private static final int START_TIME_DEF = 22 << 16 | 0;
    private static final int STOP_TIME_DEF = 7 << 16 | 0;

    public PreferenceUtils() {
    }

    public static int formatTime(int hour, int minutes) {
        return hour << 16 | minutes;
    }

    public static int getHour(int time) {
        return time >> 16 & 0xffff;
    }

    public static int getMinutes(int time) {
        return time & 0xffff;
    }

    public static SharedPreferences getInstance(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static int getValue(Context context, String key, int def) {
        return PreferenceUtils.getInstance(context).getInt(key, def);
    }

    public static long getValue(Context context, String key, long def) {
        return PreferenceUtils.getInstance(context).getLong(key, def);
    }

    public static String getValue(Context context, String key, String def) {
        return PreferenceUtils.getInstance(context).getString(key, def);
    }

    public static boolean getValue(Context context, String key, boolean def) {
        return PreferenceUtils.getInstance(context).getBoolean(key, def);
    }

    public static void putValue(Context context, String key, int def) {
        PreferenceUtils.getInstance(context).edit().putInt(key, def).commit();
    }

    public static void putValue(Context context, String key, long def) {
        PreferenceUtils.getInstance(context).edit().putLong(key, def).commit();
    }

    public static void putValue(Context context, String key, String def) {
        PreferenceUtils.getInstance(context).edit().putString(key, def).commit();
    }

    public static void putValue(Context context, String key, boolean def) {
        PreferenceUtils.getInstance(context).edit().putBoolean(key, def).commit();
    }

    public static boolean isAutoEnableOn(Context context) {
        return PreferenceUtils.getValue(context, KEY_AUTO_ENABLE_SWITCH, false);
    }

    public static boolean isFilterEnable(Context context) {
        return PreferenceUtils.getValue(context, KEY_FILTER, false);
    }

    public static boolean isFirstUse(Context context) {
        return PreferenceUtils.getValue(context, KEY_FIRST_USE, true);
    }

    public static boolean isNotificationEnable(Context context) {
        return PreferenceUtils.getValue(context, KEY_NOTIFICATION, true);
    }

    public static boolean isTorchOn(Context context) {
        return PreferenceUtils.getValue(context, KEY_TORCH_STATE, false);
    }

    public static int getAutoEnableStartTime(Context context) {
        return PreferenceUtils.getValue(context, KEY_AUTO_ENABLE_START_TIME, START_TIME_DEF);
    }

    public static int getAutoEnableStopTime(Context context) {
        return PreferenceUtils.getValue(context, KEY_AUTO_ENABLE_STOP_TIME, STOP_TIME_DEF);
    }

    public static int getColorTemperature(Context context) {
        int[] color_temperature = context.getResources().getIntArray(R.array.color_temperature);
        return PreferenceUtils.getValue(context, KEY_COLOR_TEMPERATURE, color_temperature[0]);
    }

    public static int getColorTemperatureIndex(Context context) {
        return PreferenceUtils.getValue(context, KEY_COLOR_TEMPERATURE_INDEX, 0);
    }

    public static int getIntensity(Context context) {
        return PreferenceUtils.getValue(context, KEY_INTENSITY, INTENSITY_DEF);
    }

    public static long getLastProactiveReportTime(Context context) {
        return PreferenceUtils.getValue(context, KEY_LAST_PROACTIVE_REPORT_TIME, 0);
    }

    public static int getScreenDim(Context context) {
        return PreferenceUtils.getValue(context, KEY_SCREEN_DIM, 0);
    }

    public static void setAutoEnableStartTime(Context context, int time) {
        PreferenceUtils.putValue(context, KEY_AUTO_ENABLE_START_TIME, time);
    }

    public static void setAutoEnableStatus(Context context, boolean status) {
        PreferenceUtils.putValue(context, KEY_AUTO_ENABLE_SWITCH, status);
    }

    public static void setAutoEnableStopTime(Context context, int time) {
        PreferenceUtils.putValue(context, KEY_AUTO_ENABLE_STOP_TIME, time);
    }

    public static void setColorTemperature(Context context, int color) {
        PreferenceUtils.putValue(context, KEY_COLOR_TEMPERATURE, color);
    }

    public static void setColorTemperatureIndex(Context context, int index) {
        PreferenceUtils.putValue(context, KEY_COLOR_TEMPERATURE_INDEX, index);
    }

    public static void setFilterStatus(Context context, boolean stauts) {
        PreferenceUtils.putValue(context, KEY_FILTER, stauts);
    }

    public static void setFirstUse(Context context, boolean use) {
        PreferenceUtils.putValue(context, KEY_FIRST_USE, use);
    }

    public static void setIntensity(Context context, int intensity) {
        PreferenceUtils.putValue(context, KEY_INTENSITY, intensity);
    }

    public static void setLastProactiveReportTime(Context context, long time) {
        PreferenceUtils.putValue(context, KEY_LAST_PROACTIVE_REPORT_TIME, time);
    }

    public static void setNotificationStatus(Context context, boolean status) {
        PreferenceUtils.putValue(context, KEY_NOTIFICATION, status);
    }

    public static void setScreenDim(Context context, int dim) {
        PreferenceUtils.putValue(context, KEY_SCREEN_DIM, dim);
    }

    public static void setTorchState(Context context, boolean state) {
        PreferenceUtils.putValue(context, KEY_TORCH_STATE, state);
    }

    public static void registerOnSharedPreferenceChangeListener(Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceUtils.getInstance(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(Context context,
                                                                  SharedPreferences.OnSharedPreferenceChangeListener listener) {
        PreferenceUtils.getInstance(context).unregisterOnSharedPreferenceChangeListener(listener);
    }
}

