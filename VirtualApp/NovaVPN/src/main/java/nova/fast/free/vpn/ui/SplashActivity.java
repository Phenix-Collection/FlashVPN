package nova.fast.free.vpn.ui;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;

import java.util.List;

import nova.fast.free.vpn.AppConstants;
import nova.fast.free.vpn.NovaApp;
import nova.fast.free.vpn.NovaUser;
import nova.fast.free.vpn.R;
import nova.fast.free.vpn.utils.CommonUtils;
import nova.fast.free.vpn.utils.MLogs;
import nova.fast.free.vpn.utils.PreferenceUtils;
import nova.fast.free.vpn.utils.RemoteConfig;

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
        adShown = false;
        loadTimeout = false;
        enteredHome = false;
        long time = System.currentTimeMillis();
        setContentView(R.layout.splash_activity_layout);
//        mainLayout.setBackgroundResource(R.mipmap.launcher_bg_main);
        if (!NovaUser.getInstance(this).isVIP()) {
            HomeActivity.preloadAd(this);
//            FuseAdLoader adLoader = FuseAdLoader.get(HomeActivity.SLOT_HOME_NATIVE, this.getApplicationContext());
//            adLoader.setBannerAdSize(HomeActivity.getBannerAdSize());
//            adLoader.preloadAd();
        }

        if (NovaApp.getApp().needEnterAd()) {
            FuseAdLoader.get(NovaApp.SLOT_ENTER_AD, this).loadAd(2, 2000, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    if (!loadTimeout) {
                        ad.show();
                        adShown = true;
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!adShown) {
                    enterHome();
                }
                loadTimeout = true;
            }
        }, 3000 - delta);

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
