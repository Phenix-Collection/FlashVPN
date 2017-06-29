package com.polestar.myapplication;

import android.app.Application;
import android.app.NotificationManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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


//        ContentProviderClient test = this.getContentResolver().acquireContentProviderClient("com.facebook.katana.provider.PlatformProvider");
//        ContentProviderClient test2 = this.getContentResolver().acquireUnstableContentProviderClient("com.facebook.katana.provider.PlatformProvider");
//        Log.d(TAG, "test aclient: " + test + " test 2 " + test2);


    }
}
