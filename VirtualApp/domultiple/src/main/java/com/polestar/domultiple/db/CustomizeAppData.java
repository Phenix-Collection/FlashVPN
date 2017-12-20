package com.polestar.domultiple.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.utils.BitmapUtils;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.ResourcesUtil;

/**
 * Created by PolestarApp on 2017/7/29.
 */

// TODO add user id support
public class CustomizeAppData {
    public int hue;
    public int sat;
    public int light;
    public boolean badge;
    public String label;
    public String pkg;
    public boolean customized;
    public int userId;
    public String spName;

    private CustomizeAppData() {
    }

    @Deprecated
    public static CustomizeAppData loadFromPref(String pkg) {
        return loadFromPref(pkg, 0);
    }

    public static CustomizeAppData loadFromPref(String pkg, int userId) {
        CustomizeAppData data = new CustomizeAppData();
        data.spName = CloneManager.getCompatibleName(AppConstants.CUSTOMIZE_PREF + pkg, userId);
        SharedPreferences settings = PolestarApp.getApp().getSharedPreferences(data.spName, Context.MODE_PRIVATE);
        data.badge = settings.getBoolean("badge", true);
        data.hue = settings.getInt("hue", AppConstants.COLOR_MID_VALUE);
        data.sat = settings.getInt("sat", AppConstants.COLOR_MID_VALUE);
        data.light = settings.getInt("light", AppConstants.COLOR_MID_VALUE);
        data.label = settings.getString("label", null);
        data.pkg = pkg;
        data.userId = userId;
        data.customized = data.label != null;
        if (TextUtils.isEmpty(data.label)) {
            data.label = String.format(ResourcesUtil.getString(R.string.clone_label_tag),
                    CloneManager.getInstance(PolestarApp.getApp()).getModelName(pkg, userId));
        }
        return data;
    }

    public Bitmap getCustomIcon() {
        return  BitmapUtils.getCustomIcon(PolestarApp.getApp(), pkg, userId);
    }
    public void saveToPref() {
        SharedPreferences settings = PolestarApp.getApp().getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("badge", badge);
        editor.putInt("hue", hue);
        editor.putInt("light", light);
        editor.putString("label", label);
        editor.commit();
    }
}
