package com.polestar.minesweeperclassic;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.polestar.minesweeperclassic.utils.BugReporter;
import com.polestar.minesweeperclassic.utils.EventReporter;
import com.polestar.minesweeperclassic.utils.MLogs;
import com.polestar.minesweeperclassic.utils.RemoteConfig;

import java.io.File;

/**
 * Created by doriscoco on 2017/4/4.
 */

public class MApp extends Application {
    private static MApp gDefault;

    public static MApp getApp() {
        return gDefault;
    }

    public static boolean isOpenLog(){
        File file = new File(Environment.getExternalStorageDirectory() + "/polelog");
        boolean ret =  file.exists();
        if(ret) {
            Log.d(MLogs.DEFAULT_TAG, "log opened by file");
        }
        return  ret;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gDefault = this;

        if (isOpenLog() || BuildConfig.DEBUG) {
            MLogs.DEBUG = true;
        }
        FirebaseApp.initializeApp(gDefault);
        RemoteConfig.init();
        EventReporter.init(gDefault);
        BugReporter.init(gDefault);
    }
}
