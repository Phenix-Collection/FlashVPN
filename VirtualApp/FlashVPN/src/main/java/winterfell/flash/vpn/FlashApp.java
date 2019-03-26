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
import com.polestar.task.database.DatabaseImplFactory;

import winterfell.flash.vpn.reward.AppUser;

import java.io.File;
import java.util.List;

import winterfell.flash.vpn.billing.BillingProvider;
import winterfell.flash.vpn.core.ProxyConfig;
import winterfell.flash.vpn.utils.BugReporter;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.PreferenceUtils;
import winterfell.flash.vpn.utils.RemoteConfig;

public class FlashApp extends MultiDexApplication {

    private static FlashApp gDefault;

    public static FlashApp getApp() {
        return gDefault;
    }

    public static boolean isOpenLog(){
        boolean ret = false;
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/flashlog");
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

        builder.admobAppId("ca-app-pub-7413370106066330~8449358900")
                .mopubAdUnit("65fdd6dfb69c43cc933c2dd7d1c03ee4")
                .ironSourceAppKey("8cc57c35");
        FuseAdLoader.init(new FuseAdLoader.ConfigFetcher() {
            @Override
            public boolean isAdFree(String slot) {
                //return true;
                return FlashUser.getInstance().isVIP();
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
        return BuildConfig.DEBUG || (!FlashUser.getInstance().isVIP() && PreferenceUtils.hasShownRateDialog(this)
                && (System.currentTimeMillis() - PreferenceUtils.getEnterAdTime()) > interval*1000 );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseImplFactory.CONF_NEED_PRODUCT = false;
        DatabaseImplFactory.CONF_NEED_TASK = true;
        FirebaseApp.initializeApp(gDefault);
        RemoteConfig.init();
        EventReporter.init(gDefault);
        BugReporter.init(gDefault);
       initAd();
       FlashUser.getInstance().preloadRewardVideoTask();
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
    }
}