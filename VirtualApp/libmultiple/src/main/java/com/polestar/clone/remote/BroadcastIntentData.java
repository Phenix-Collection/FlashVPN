package com.polestar.clone.remote;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by guojia on 2019/4/5.
 */

public class BroadcastIntentData implements Parcelable {

    public int userId;
    public Intent intent;
    public String pkg;
    public ComponentName componentName;
    public BroadcastIntentData(int userId, Intent intent, String pkg, ComponentName componentName) {
        this.userId = userId;
        this.intent = intent;
        this.pkg = pkg;
        this.componentName = componentName;
    }

    public BroadcastIntentData(BroadcastIntentData data) {
        this.userId = data.userId;
        this.intent = data.intent;
        this.pkg = data.pkg;
        this.componentName = data.componentName;
    }
    protected BroadcastIntentData(Parcel arg2) {
        super();
        this.userId = arg2.readInt();
        this.intent = arg2.readParcelable(Intent.class.getClassLoader());
        this.pkg = arg2.readString();
        this.componentName = arg2.readParcelable(ComponentName.class.getClassLoader());
    }

    public static final Creator<BroadcastIntentData> CREATOR = new Creator<BroadcastIntentData>() {
        @Override
        public BroadcastIntentData createFromParcel(Parcel in) {
            return new BroadcastIntentData(in);
        }

        @Override
        public BroadcastIntentData[] newArray(int size) {
            return new BroadcastIntentData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeParcelable(intent, flags);
        dest.writeString(pkg);
        dest.writeParcelable(componentName, flags);
    }
}