package winterfell.flash.vpn;

import android.content.Context;
import android.os.Environment;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.SDKConfiguration;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.task.network.AppUser;
import com.polestar.task.network.Configuration;

import java.io.File;
import java.util.List;

import winterfell.flash.vpn.billing.BillingProvider;
import winterfell.flash.vpn.utils.BugReporter;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.PreferenceUtils;
import winterfell.flash.vpn.utils.RemoteConfig;

public class FlashApp extends MultiDexApplication {

    private static FlashApp gDefault;
    private AppUser mAppUser;

    public static FlashApp getApp() {
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

        builder.admobAppId("ca-app-pub-7413370106066330~8449358900");
//        builder.mopubAdUnit("8e77a1b50d5c4a9fb204d212e2bd530a")
//                .admobAppId("ca-app-pub-5490912237269284~7387660650")
//                .ironSourceAppKey("8a16ee3d");
        FuseAdLoader.init(new FuseAdLoader.ConfigFetcher() {
            @Override
            public boolean isAdFree() {
                //return true;
                return FlashUser.getInstance(getApp()).isVIP();
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
        return !FlashUser.getInstance(this).isVIP() && PreferenceUtils.hasShownRateDialog(this)
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
        if (!getPackageName().contains("flash")){
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

        mAppUser = AppUser.getInstance();
        if (AppUser.check()) {
            Configuration.URL_PREFIX = RemoteConfig.getString("config_task_server");
            Configuration.APP_VERSION_CODE = BuildConfig.VERSION_CODE;
            Configuration.PKG_NAME = BuildConfig.APPLICATION_ID;
            FuseAdLoader.setUserId(AppUser.getInstance().getMyId());
        }
    }
}