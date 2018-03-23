package com.polestar.minesweeperclassic.utils;

import android.support.annotation.NonNull;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.polestar.minesweeperclassic.BuildConfig;
import com.polestar.minesweeperclassic.R;

/**
 * Created by guojia on 2016/12/17.
 */

public class RemoteConfig {

    private static FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    private static String TAG = "RemoteConfig";

    public static void init() {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        int cacheTime = BuildConfig.DEBUG ? 0 : 8 * 60 * 60;
        mFirebaseRemoteConfig.setDefaults(R.xml.default_remote_config);
        mFirebaseRemoteConfig.fetch(cacheTime).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                MLogs.d(TAG, "Fetch Succeeded");
                mFirebaseRemoteConfig.activateFetched();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                MLogs.d(TAG, "Fetch failed");
            }
        });
        mFirebaseRemoteConfig.activateFetched();
    }

    public static boolean getBoolean(String key) {
        return mFirebaseRemoteConfig.getBoolean(key);
    }

    public static long getLong(String key) {
        return mFirebaseRemoteConfig.getLong(key);
    }

    public static String getString(String key) {
        return mFirebaseRemoteConfig.getString(key);
    }
}
