package com.polestar.clone.server.accounts;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class VAccountVisibility implements Parcelable {
    public static final Creator<VAccountVisibility> CREATOR = new Creator<VAccountVisibility>() {
        @Override
        public VAccountVisibility createFromParcel(Parcel source) {
            return new VAccountVisibility(source);
        }

        @Override
        public VAccountVisibility[] newArray(int size) {
            return new VAccountVisibility[size];
        }
    };
    public int userId;
    public String name;
    public String type;
    public Map<String, Integer> visibility;

    public VAccountVisibility(int userId, Account account, Map<String, Integer> visibility) {
        this.userId = userId;
        name = account.name;
        type = account.type;
        this.visibility = new HashMap<>();
        if (visibility != null) {
            this.visibility.putAll(visibility);
        }
    }

    public VAccountVisibility(Parcel in) {
        userId = in.readInt();
        name = in.readString();
        type = in.readString();
        int visibilitySize = in.readInt();
        visibility = new HashMap<>(visibilitySize);
        for (int i = 0; i < visibilitySize; i++) {
            String key = in.readString();
            int value = in.readInt();
            visibility.put(key, value);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeInt(visibility.size());
        for (Map.Entry<String, Integer> entry : visibility.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeInt(entry.getValue());
        }
    }
}

