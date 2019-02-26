package nova.fast.free.vpn;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.SDKConfiguration;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.booster.BoosterSdk;

import java.io.File;
import java.util.List;

import nova.fast.free.vpn.billing.BillingProvider;
import nova.fast.free.vpn.utils.BugReporter;
import nova.fast.free.vpn.utils.EventReporter;
import nova.fast.free.vpn.utils.MLogs;
import nova.fast.free.vpn.utils.PreferenceUtils;
import nova.fast.free.vpn.utils.RemoteConfig;

public class NovaApp extends MultiDexApplication {

    private static NovaApp gDefault;

    public static NovaApp getApp() {
        return gDefault;
    }

    public static boolean isOpenLog(){
        boolean ret = false;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/polelog");
            ret = file.exists();
            if (ret) {
                Log.d(MLogs.DEFAULT_TAG, "log opened by file");
            }
        }catch (Exception ex){

        }
        return  ret;
    }

    @Override
    protected void attachBaseContext(Context base) {
        Log.d(MLogs.DEFAULT_TAG, "APP version: " + BuildConfig.VERSION_NAME + " Type: " + BuildConfig.BUILD_TYPE);

        super.attachBaseContext(base);
        gDefault = this;
    }

    private void initAd() {
        SDKConfiguration.Builder builder = new SDKConfiguration.Builder();

        builder.mopubAdUnit("8e77a1b50d5c4a9fb204d212e2bd530a")
                .admobAppId("ca-app-pub-5490912237269284~7387660650")
                .ironSourceAppKey("8a16ee3d");
        FuseAdLoader.init(new FuseAdLoader.ConfigFetcher() {
            @Override
            public boolean isAdFree() {
                return NovaUser.getInstance(getApp()).isVIP();
            }

            @Override
            public List<AdConfig> getAdConfigList(String slot) {
                return RemoteConfig.getAdConfigList(slot);
            }
        }, getApp(), builder.build());
    }


    private final static String CONF_ENTER_AD_INTERVAL_SEC = "slot_home_enter_ad_interval_sec";
    public final static String SLOT_ENTER_AD = "slot_home_enter_ad";

    public boolean needEnterAd(){
        long interval = RemoteConfig.getLong(CONF_ENTER_AD_INTERVAL_SEC);
        return !NovaUser.getInstance(this).isVIP() && PreferenceUtils.hasShownRateDialog(this)
                && (System.currentTimeMillis() - PreferenceUtils.getEnterAdTime()) > interval*1000 ;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(gDefault);
        RemoteConfig.init();
        EventReporter.init(gDefault);
        BugReporter.init(gDefault);
       initAd();
        BillingProvider.get().updateStatus(null);
        if (!getPackageName().contains("fast.free")){
            System.exit(0);
        }
        if (isOpenLog() || BuildConfig.DEBUG ) {
            MLogs.DEBUG = true;
            MLogs.d(MLogs.DEFAULT_TAG, "VLOG is opened");
            AdConstants.DEBUG = true;
           //BoosterSdk.DEBUG = true;
        }

        if(needEnterAd()) {
            FuseAdLoader.get(SLOT_ENTER_AD, getApp()).preloadAd(getApp());
        }

        BoosterSdk.BoosterRes res = new BoosterSdk.BoosterRes();
//        res.titleString = R.string.booster_title;
//        res.boosterShorcutIcon = R.drawable.boost_icon;
//        res.innerWheelImage = R.drawable.boost_out_wheel;
//        res.outterWheelImage = R.drawable.boost_inner_wheel;
        BoosterSdk.BoosterConfig boosterConfig = new BoosterSdk.BoosterConfig();
        if (BuildConfig.DEBUG) {
            boosterConfig.autoAdFirstInterval = 0;
            boosterConfig.autoAdInterval = 0;
            boosterConfig.isUnlockAd = true;
            boosterConfig.isInstallAd = true;
            boosterConfig.avoidShowIfHistory = false;
            boosterConfig.showNotification = false;
            boosterConfig.accountName = "VPN Premium";
        } else {
            boosterConfig.autoAdFirstInterval = RemoteConfig.getLong("auto_ad_first_interval") * 1000;
            boosterConfig.autoAdInterval = RemoteConfig.getLong("auto_ad_interval") * 1000;
            boosterConfig.isUnlockAd = RemoteConfig.getBoolean("allow_unlock_ad");
            boosterConfig.isInstallAd = RemoteConfig.getBoolean("allow_install_ad");
            boosterConfig.avoidShowIfHistory = RemoteConfig.getBoolean("avoid_ad_if_history");
            boosterConfig.showNotification = false;
            boosterConfig.accountName = "VPN Premium";
        }
        BoosterSdk.init(gDefault, boosterConfig, res, new BoosterSdk.IEventReporter() {
            @Override
            public void reportEvent(String s, Bundle b) {
                FirebaseAnalytics.getInstance(NovaApp.getApp()).logEvent(s, b);
            }

            @Override
            public void reportWake(String s) {
                EventReporter.reportWake(NovaApp.getApp(), s);
            }
        });
    }
}