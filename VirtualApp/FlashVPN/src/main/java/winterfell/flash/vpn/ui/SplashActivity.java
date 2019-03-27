package winterfell.flash.vpn.ui;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.WindowManager;

import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;

import java.util.List;

import winterfell.flash.vpn.AppConstants;
import winterfell.flash.vpn.FlashApp;
import winterfell.flash.vpn.FlashUser;
import winterfell.flash.vpn.R;
import winterfell.flash.vpn.utils.CommonUtils;
import winterfell.flash.vpn.utils.EventReporter;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.PreferenceUtils;
import winterfell.flash.vpn.utils.RemoteConfig;

/**
 * Created by NovaApp on 2017/7/15.
 */

public class SplashActivity extends BaseActivity {

    private boolean adShown = false;
    private boolean loadTimeout = false;
    private boolean enteredHome ;
    public final static String EXTRA_FROM_SHORTCUT = "extra_from_shortcut";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MLogs.d(this.getClass().getName() +" launching from intent: " +getIntent());
        EventReporter.reportWake(this, "user_launch");
        adShown = false;
        loadTimeout = false;
        enteredHome = false;
        long time = System.currentTimeMillis();
        setContentView(R.layout.splash_activity_layout);
//        mainLayout.setBackgroundResource(R.mipmap.launcher_bg_main);
            HomeActivity.preloadAd(this);
//            FuseAdLoader adLoader = FuseAdLoader.get(HomeActivity.SLOT_HOME_NATIVE, this.getApplicationContext());
//            adLoader.setBannerAdSize(HomeActivity.getBannerAdSize());
//            adLoader.preloadAd();

        boolean stateOn = true;
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        /**
         * Gets the state of the display, such as whether it is on or off.
         *
         * @return The state of the display: one of {@link #STATE_OFF}, {@link #STATE_ON},
         * {@link #STATE_DOZE}, {@link #STATE_DOZE_SUSPEND}, or {@link #STATE_UNKNOWN}.
         */
        if (Build.VERSION.SDK_INT >= 20) {
            stateOn = display.getState() == Display.STATE_ON;
        }
        boolean needEnterAd = stateOn && FlashApp.getApp().needEnterAd();
        if (needEnterAd) {
            FuseAdLoader.get(FlashApp.SLOT_ENTER_AD, this).loadAd(this, 2, 2000, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    if (!loadTimeout) {
                        ad.show();
                        adShown = true;
                        MLogs.d("show home enter ad");
                    } else {
                        MLogs.d("loaded time out, not show ad");
                    }
                }

                @Override
                public void onAdClicked(IAdAdapter ad) {

                }

                @Override
                public void onAdClosed(IAdAdapter ad) {
                    enterHome();
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {

                }

                @Override
                public void onRewarded(IAdAdapter ad) {

                }
            });
        }
        Handler handler = new Handler();

        long delta = System.currentTimeMillis() - time;
        long timeout =  needEnterAd?5000:2500;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!adShown) {
                    enterHome();
                }
                loadTimeout = true;
            }
        }, timeout - delta);

//        if(!NovaApp.isSupportPkg()) {
//            if (getIntent().getBooleanExtra(EXTRA_FROM_SHORTCUT, false)) {
//                MLogs.d("Launching from shortcut");
//                if (AndroidUtil.hasShortcut(this)) {
//                    PreferenceUtils.setAbleToDetectShortcut(true);
//                } else {
//                    PreferenceUtils.setAbleToDetectShortcut(false);
//                    MLogs.d("Failed to detect shortcut!");
//                }
//            }
//        }//        }
    }

    @Override
    protected void onResume() {
        MLogs.d("Splash onResume");
        super.onResume();
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }

    private void enterHome(){
        if (!enteredHome) {
            enteredHome = true;
            if (!PreferenceUtils.isShortCutCreated()) {
                PreferenceUtils.setShortCutCreated();
                CommonUtils.createLaunchShortcut(this);
            }
            HomeActivity.enter(this, needUpdate());
            finish();
        }
    }

    private boolean needUpdate() {
        try {
            PackageInfo vinfo = getPackageManager().getPackageInfo(getPackageName(),0);
            int versionCode = vinfo.versionCode;
            long pushVersion = RemoteConfig.getLong(AppConstants.CONF_UPDATE_VERSION);
            long latestVersion = RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION);
            long ignoreVersion = PreferenceUtils.getIgnoreVersion();
            MLogs.d("local: " + versionCode + " push: " + pushVersion + " latest: " + latestVersion + " ignore: "+ ignoreVersion);
            if (versionCode <= pushVersion
                    && ignoreVersion < latestVersion) {
                return true;
            }
        }catch (Exception e) {
            MLogs.e(e);
        }
        return false;
    }
}
