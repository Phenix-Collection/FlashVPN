package nova.fast.free.vpn.ui;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import nova.fast.free.vpn.AppConstants;
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

    private static boolean created;
    public final static String EXTRA_FROM_SHORTCUT = "extra_from_shortcut";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MLogs.d(this.getClass().getName() +" launching from intent: " +getIntent());
        long time = System.currentTimeMillis();
        setContentView(R.layout.splash_activity_layout);
//        mainLayout.setBackgroundResource(R.mipmap.launcher_bg_main);
        if (!NovaUser.getInstance(this).isVIP()) {
            HomeActivity.preloadAd(this);
//            FuseAdLoader adLoader = FuseAdLoader.get(HomeActivity.SLOT_HOME_NATIVE, this.getApplicationContext());
//            adLoader.setBannerAdSize(HomeActivity.getBannerAdSize());
//            adLoader.preloadAd();
        }
        Handler handler = new Handler();

        long delta = System.currentTimeMillis() - time;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                enterHome();
            }
        }, 2500 - delta);

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

        if (!PreferenceUtils.isShortCutCreated()) {
            PreferenceUtils.setShortCutCreated();
            CommonUtils.createLaunchShortcut(this);
            created = true;
        }
        HomeActivity.enter(this, needUpdate());
        finish();
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
