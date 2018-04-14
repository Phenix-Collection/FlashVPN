package com.polestar.booster.util;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by su on 2017/7/15.
 */

public class SpUtils {

    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences settings = context.getSharedPreferences("clean", Context.MODE_PRIVATE);
        return settings.getString(key, defaultValue);
    }

    public static boolean putString(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences("clean", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        return editor.commit();
    }
}
