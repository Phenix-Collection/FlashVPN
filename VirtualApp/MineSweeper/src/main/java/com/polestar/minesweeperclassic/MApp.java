package com.polestar.minesweeperclassic;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.polestar.ad.SDKConfiguration;
import com.polestar.booster.BoosterSdk;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
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
            public boolean isAdFree() {
                return false;
            }

            @Override
            public List<AdConfig> getAdConfigList(String slot) {
                return RemoteConfig.getAdConfigList(slot);
            }
        }, gDefault, sdkConfiguration);
        BoosterSdk.BoosterRes res = new BoosterSdk.BoosterRes();
        res.titleString = R.string.app_name;
        res.boosterShorcutIcon = R.drawable.ic_launcher;
        res.innerWheelImage = R.drawable.ic_launcher;
        res.outterWheelImage = R.drawable.ic_launcher;
        BoosterSdk.BoosterConfig boosterConfig = new BoosterSdk.BoosterConfig();
        boosterConfig.showNotification = false;
        if (BuildConfig.DEBUG) {
            boosterConfig.autoAdFirstInterval = 0;
            boosterConfig.autoAdInterval = 0;
            boosterConfig.isUnlockAd = true;
            boosterConfig.isInstallAd = true;
            boosterConfig.avoidShowIfHistory = false;
        } else {
            boosterConfig.autoAdFirstInterval = RemoteConfig.getLong("auto_ad_first_interval") * 1000;
            boosterConfig.autoAdInterval = RemoteConfig.getLong("auto_ad_interval") * 1000;
            boosterConfig.isUnlockAd = RemoteConfig.getBoolean("allow_unlock_ad");
            boosterConfig.isInstallAd = RemoteConfig.getBoolean("allow_install_ad");
            boosterConfig.avoidShowIfHistory = RemoteConfig.getBoolean("avoid_ad_if_history");
        }
        BoosterSdk.init(gDefault, boosterConfig, res, new BoosterSdk.IEventReporter() {
            @Override
            public void reportEvent(String s, Bundle b) {
                FirebaseAnalytics.getInstance(MApp.getApp()).logEvent(s, b);
            }

            @Override
            public void reportWake(String s) {
                EventReporter.reportWake(gDefault, s);;
            }
        });
        if (isOpenLog() || BuildConfig.DEBUG ) {
            MLogs.DEBUG = true;
            MLogs.d(MLogs.DEFAULT_TAG, "VLOG is opened");
            AdConstants.DEBUG = true;
            BoosterSdk.DEBUG = true;
        }
        if (GameActivity.needAppStartAd()) {
            FuseAdLoader.get(GameActivity.SLOT_ENTER_INTERSTITIAL, gDefault).preloadAd(gDefault);
        }
    }
}
