package com.polestar.clone;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.polestar.clone.client.core.VirtualCore;

/**
 * Created by PolestarApp on 2017/7/29.
 */

// TODO add user id support
public class CustomizeAppData implements Parcelable{
    public final static String CUSTOMIZE_PREF = "app_custom_";
    public final static int COLOR_MID_VALUE = 127;

    public int hue;
    public int sat;
    public int light;
    public boolean badge;
    public String label;
    public String pkg;
    public boolean customized;
    public int userId;
    public String spName;
    public boolean isNotificationEnable;
    public long lockInterval;

    private CustomizeAppData() {
    }

    protected CustomizeAppData(Parcel in) {
        hue = in.readInt();
        sat = in.readInt();
        light = in.readInt();
        badge = in.readByte() != 0;
        label = in.readString();
        pkg = in.readString();
        customized = in.readByte() != 0;
        userId = in.readInt();
        spName = in.readString();
        isNotificationEnable = in.readByte() != 0;
        lockInterval = in.readLong();
    }
    public static boolean hasLaunched(String pkg, int userId) {
        String spName = VirtualCore.get().getCompatibleName(CUSTOMIZE_PREF + pkg, userId);
        SharedPreferences settings = VirtualCore.get().getContext().getSharedPreferences(spName, Context.MODE_PRIVATE);

        return settings.getBoolean("launched", false);
    }

    public static void setLaunched(String pkg, int userId){
        String spName = VirtualCore.get().getCompatibleName(CUSTOMIZE_PREF + pkg, userId);
        SharedPreferences settings = VirtualCore.get().getContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
        settings.edit().putBoolean("launched", true).commit();

    }
    public static final Creator<CustomizeAppData> CREATOR = new Creator<CustomizeAppData>() {
        @Override
        public CustomizeAppData createFromParcel(Parcel in) {
            return new CustomizeAppData(in);
        }

        @Override
        public CustomizeAppData[] newArray(int size) {
            return new CustomizeAppData[size];
        }
    };

    @Deprecated
    public static CustomizeAppData loadFromPref(String pkg) {
        return loadFromPref(pkg, 0);
    }

    public static CustomizeAppData loadFromPref(String pkg, int userId) {
        CustomizeAppData data = new CustomizeAppData();
        data.spName = VirtualCore.getCompatibleName(CUSTOMIZE_PREF + pkg, userId);
        SharedPreferences settings = VirtualCore.get().getContext().getSharedPreferences(data.spName, Context.MODE_PRIVATE);
        data.badge = settings.getBoolean("badge", true);
        data.hue = settings.getInt("hue", COLOR_MID_VALUE);
        data.sat = settings.getInt("sat", COLOR_MID_VALUE);
        data.light = settings.getInt("light", COLOR_MID_VALUE);
        data.label = settings.getString("label", null);
        data.isNotificationEnable = settings.getBoolean("notification", true);
        data.lockInterval = settings.getLong("lock", 0);
        data.pkg = pkg;
        data.userId = userId;
        data.customized = data.label != null;
        if (TextUtils.isEmpty(data.label)) {
            try {
                ApplicationInfo ai = VirtualCore.get().getUnHookPackageManager().getApplicationInfo(pkg, 0);
                data.label =  ai.loadLabel(VirtualCore.get().getUnHookPackageManager()) + "+";
            } catch (Exception ex){

            }
        }
        return data;
    }

    public Bitmap getCustomIcon() {
        return  BitmapUtils.getCustomIcon(VirtualCore.get().getContext(), pkg, userId);
    }
    public void saveToPref() {
        SharedPreferences settings = VirtualCore.get().getContext().getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("badge", badge);
        editor.putInt("hue", hue);
        editor.putInt("light", light);
        editor.putString("label", label);
        editor.putBoolean("notification", isNotificationEnable);
        editor.putLong("lock", lockInterval);
        editor.commit();
    }

    public static void removePerf(String pkg, int userId) {
        String sp = VirtualCore.getCompatibleName(CUSTOMIZE_PREF + pkg, userId);
        SharedPreferences settings = VirtualCore.get().getContext().getSharedPreferences(sp, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear().commit();
        BitmapUtils.removeCustomIcon(VirtualCore.get().getContext(), pkg, userId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(hue);
        parcel.writeInt(sat);
        parcel.writeInt(light);
        parcel.writeByte((byte) (badge ? 1 : 0));
        parcel.writeString(label);
        parcel.writeString(pkg);
        parcel.writeByte((byte) (customized ? 1 : 0));
        parcel.writeInt(userId);
        parcel.writeString(spName);
        parcel.writeByte((byte) (isNotificationEnable ? 1 : 0));
        parcel.writeLong(lockInterval);
    }
}
