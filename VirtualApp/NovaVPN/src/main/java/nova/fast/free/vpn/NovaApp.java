package nova.fast.free.vpn;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.adapters.FuseAdLoader;

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
        MobileAds.initialize(gDefault, "ca-app-pub-5490912237269284~7387660650");
        FuseAdLoader.init(new FuseAdLoader.ConfigFetcher() {
            @Override
            public boolean isAdFree() {
                return NovaUser.getInstance(NovaApp.getApp()).isVIP();
            }

            @Override
            public List<AdConfig> getAdConfigList(String slot) {
                return RemoteConfig.getAdConfigList(slot);
            }
        });
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
            FuseAdLoader.get(SLOT_ENTER_AD, getApp()).preloadAd();
        }
    }
}