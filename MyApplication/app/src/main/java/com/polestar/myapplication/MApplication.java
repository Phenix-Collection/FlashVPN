package com.polestar.myapplication;

import android.app.Application;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.util.Log;

/**
 * Created by guojia on 2017/6/3.
 */

public class MApplication extends Application {
    private static final String TAG = "SPC";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MApplication onCreate");
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentProviderClient test3 = getContentResolver().acquireContentProviderClient("com.facebook.katana.provider.PlatformProvider");
                ContentProviderClient test4 = getContentResolver().acquireUnstableContentProviderClient("com.facebook.katana.provider.PlatformProvider");
                Log.d(TAG, "test3 aclient: " + test3 + " test 4 " + test4);
            }
        }).start();
//        ContentProviderClient test = this.getContentResolver().acquireContentProviderClient("com.facebook.katana.provider.PlatformProvider");
//        ContentProviderClient test2 = this.getContentResolver().acquireUnstableContentProviderClient("com.facebook.katana.provider.PlatformProvider");
//        Log.d(TAG, "test aclient: " + test + " test 2 " + test2);


    }
}
