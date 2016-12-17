package com.polestar.multiaccount.model;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

@Entity
public class AppModel implements Parcelable {
    @Id
    private Long id;
    private String packageName;
    private String path;
    private String name;
    private String description;
    private int index;

    @NotNull
    private long clonedTime;
    private boolean notificationEnable;
    @Transient
    private PackageInfo packageInfo;
    @Transient
    private Drawable icon;
    @Transient
    private Bitmap customIcon;
    @Transient
    private boolean unEnable;

    public AppModel() {
        //For Database
    }

    public AppModel(Context context, PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
        this.packageName = packageInfo.packageName;
        this.path = packageInfo.applicationInfo.publicSourceDir;
        this.notificationEnable = true;
        loadData(context);
    }

    protected AppModel(Parcel in) {
        packageName = in.readString();
        path = in.readString();
        name = in.readString();
        notificationEnable = in.readByte() != 0;
        packageInfo = in.readParcelable(getClass().getClassLoader());
    }

    @Generated(hash = 396567536)
    public AppModel(Long id, String packageName, String path, String name,
                    String description, int index, long clonedTime, boolean notificationEnable) {
        this.id = id;
        this.packageName = packageName;
        this.path = path;
        this.name = name;
        this.description = description;
        this.index = index;
        this.clonedTime = clonedTime;
        this.notificationEnable = notificationEnable;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeString(path);
        dest.writeString(name);
        dest.writeByte((byte) (notificationEnable ? 1 : 0));
        dest.writeParcelable(packageInfo, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Transient
    public static final Creator<AppModel> CREATOR = new Creator<AppModel>() {
        @Override
        public AppModel createFromParcel(Parcel in) {
            return new AppModel(in);
        }

        @Override
        public AppModel[] newArray(int size) {
            return new AppModel[size];
        }
    };

    public void loadData(Context context) {
        ApplicationInfo appInfo = packageInfo.applicationInfo;
        if (appInfo == null) {
            return;
        }
        PackageManager pm = context.getPackageManager();
        try {
            CharSequence sequence = appInfo.loadLabel(pm);
            if (sequence != null) {
                name = sequence.toString();
            }
            icon = appInfo.loadIcon(pm);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public String getApkPath(){
        ApplicationInfo appInfo = packageInfo.applicationInfo;
        if (appInfo == null) {
            return null;
        }
        return appInfo.sourceDir;
    }

    public Drawable initDrawable(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getClonedTime() {
        return clonedTime;
    }

    public void setClonedTime(long clonedTime) {
        this.clonedTime = clonedTime;
    }

    public boolean isNotificationEnable() {
        return notificationEnable;
    }

    public void setNotificationEnable(boolean notificationEnable) {
        this.notificationEnable = notificationEnable;
    }

    public boolean getNotificationEnable() {
        return this.notificationEnable;
    }

    public Bitmap getCustomIcon() {
        return customIcon;
    }

    public void setCustomIcon(Bitmap customIcon) {
        this.customIcon = customIcon;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isUnEnable() {
        return unEnable;
    }

    public void setUnEnable(boolean unEnable) {
        this.unEnable = unEnable;
    }
}
