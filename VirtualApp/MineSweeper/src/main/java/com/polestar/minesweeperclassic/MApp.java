package com.polestar.minesweeperclassic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.polestar.ad.SDKConfiguration;
import com.google.firebase.FirebaseApp;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.minesweeperclassic.activity.GameActivity;
import com.polestar.minesweeperclassic.utils.BugReporter;
import com.polestar.minesweeperclassic.utils.EventReporter;
import com.polestar.minesweeperclassic.utils.MLogs;
import com.polestar.minesweeperclassic.utils.RemoteConfig;

import java.io.File;
import java.util.List;
/**
 * Created by doriscoco on 2017/4/4.
 */

public class MApp extends MultiDexApplication {
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
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
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
        SDKConfiguration sdkConfiguration = new SDKConfiguration.Builder()
                .admobAppId("ca-app-pub-5490912237269284~6760289054")
                .disableAdType(AdConstants.AdType.AD_SOURCE_MOPUB)
                .disableAdType(AdConstants.AdType.AD_SOURCE_MOPUB_INTERSTITIAL)
                .ironSourceAppKey("8618670d").build();
        FuseAdLoader.init(new FuseAdLoader.ConfigFetcher() {
            @Override
            public boolean isAdFree(String slot) {
                return false;
            }

            @Override
            public List<AdConfig> getAdConfigList(String slot) {
                return RemoteConfig.getAdConfigList(slot);
            }
        }, gDefault, sdkConfiguration);
        if (isOpenLog() || BuildConfig.DEBUG ) {
            MLogs.DEBUG = true;
            MLogs.d(MLogs.DEFAULT_TAG, "VLOG is opened");
            AdConstants.DEBUG = true;
        }
        if (GameActivity.needAppStartAd()) {
            FuseAdLoader.get(GameActivity.SLOT_ENTER_INTERSTITIAL, gDefault).preloadAd(gDefault);
        }
    }
}
