package com.polestar.multiaccount.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.utils.BitmapUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.ResourcesUtil;

/**
 * Created by guojia on 2017/7/29.
 */

public class CustomizeAppData {
    public int hue;
    public int sat;
    public int light;
    public boolean badge;
    public String label;
    public String pkg;
    public boolean customized;

    private CustomizeAppData() {
    }

    public static  CustomizeAppData loadFromPref(String pkg) {
        CustomizeAppData data = new CustomizeAppData();
        SharedPreferences settings = MApp.getApp().getSharedPreferences(AppConstants.CUSTOMIZE_PREF+pkg, Context.MODE_PRIVATE);
        data.badge = settings.getBoolean("badge", true);
        data.hue = settings.getInt("hue", AppConstants.COLOR_MID_VALUE);
        data.sat = settings.getInt("sat", AppConstants.COLOR_MID_VALUE);
        data.light = settings.getInt("light", AppConstants.COLOR_MID_VALUE);
        data.label = settings.getString("label", null);
        data.pkg = pkg;
        data.customized = data.label != null;
        if (TextUtils.isEmpty(data.label)) {
            try{
                PackageManager pm = MApp.getApp().getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                data.label = String.format(ResourcesUtil.getString(R.string.clone_label_tag),pm.getApplicationLabel(ai));
            } catch (Exception ex) {
                MLogs.logBug(ex);
            }
        }
        return data;
    }

    public Bitmap getCustomIcon() {
        return  BitmapUtils.getCustomIcon(MApp.getApp(), pkg);
    }
    public void saveToPref() {
        SharedPreferences settings = MApp.getApp().getSharedPreferences(AppConstants.CUSTOMIZE_PREF+pkg, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("badge", badge);
        editor.putInt("hue", hue);
        editor.putInt("light", light);
        editor.putString("label", label);
        editor.commit();
    }
}
