package com.polestar.domultiple.components.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.lody.virtual.client.core.VirtualCore;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.BuildConfig;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.CustomizeAppData;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.DisplayUtils;
import com.polestar.domultiple.utils.EventReporter;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;

import java.util.HashSet;
import java.util.List;

/**
 * Created by PolestarApp on 2017/7/16.
 */

/**
 * Created by PolestarApp on 2016/12/5.
 */

public class AppLoadingActivity extends BaseActivity {

    private ProgressBar mLoadingView;
    private ImageView mImgCircle;
    private ImageView mImgAppIcon;
    private TextView mTxtTips;
    private TextView mFirstStartTips;

    private CloneModel appModel;
    private String from;
    private boolean needDoUpGrade;
    private boolean firstStart;

    private static final String EXTRA_FIRST_START = "first_start";

    private FuseAdLoader mInterstitialAdLoader;
    private FuseAdLoader mNativeAdLoader;
    private static final String SLOT_APP_START_INTERSTITIAL = "slot_app_start";
    private static final String SLOT_APP_START_NATIVE = "slot_app_start_native";
    private static final String CONFIG_APP_START_AD_FREQ = "slot_app_start_freq_hour";
    private static final String CONFIG_APP_START_AD_RAMP = "slot_app_start_ramp_hour";
    private static final String CONFIG_APP_START_AD_FILTER = "slot_app_start_filter";
    private static final String CONFIG_APP_START_AD_STYLE = "slot_app_start_style"; //native,interstitial,all
    private static final String CONFIG_APP_START_NATIVE_AD_FREQ = "slot_app_start_native_freq_min";
    private static final String CONFIG_APP_START_NATIVE_AD_RAMP = "slot_app_start_native_ramp_min";
    private static final long INTERSTITIAL_SHOW_DELAY = 2000;
    private static HashSet<String> filterPkgs ;
    private boolean hasShownInterstitialAd;
    private boolean launched;
    private LinearLayout mNativeContainer;

    public static boolean needLoadNativeAd(boolean preload, String pkg) {
        if (PreferencesUtils.isAdFree()) {
            return false;
        }
        String style = RemoteConfig.getString(CONFIG_APP_START_AD_STYLE);
        if (!("native".equals(style) || "all".equals(style))) {
            return false;
        }
        long interval = RemoteConfig.getLong(CONFIG_APP_START_NATIVE_AD_FREQ)*60*1000;
        long ramp = RemoteConfig.getLong(CONFIG_APP_START_NATIVE_AD_RAMP)*60*1000;
        long last = getLastShowTime(false);
        if (last == 0 && TextUtils.isEmpty(pkg)) {
            return false;
        }
        long actualLast = last == 0? CommonUtils.getInstallTime(PolestarApp.getApp(), PolestarApp.getApp().getPackageName()): last;
        long delta =
                preload? System.currentTimeMillis() - actualLast + 15*60*1000: System.currentTimeMillis()-actualLast;
        boolean need =  last == 0? delta > ramp: delta > interval;
        if (filterPkgs == null) {
            filterPkgs = new HashSet<>();
            String[] arr = RemoteConfig.getString(CONFIG_APP_START_AD_FILTER).split(":");
            if(arr !=null) {
                for (String s : arr) {
                    filterPkgs.add(s);
                }
            }
        }
        MLogs.d("needLoad start app native ad: " + need);
        return (need && (!filterPkgs.contains(pkg) || pkg ==null)) || BuildConfig.DEBUG;
    }

    public static boolean needLoadInterstitialAd(boolean preload, String pkg) {
        if (PreferencesUtils.isAdFree()) {
            return false;
        }
        String style = RemoteConfig.getString(CONFIG_APP_START_AD_STYLE);
        if (!("interstitial".equals(style) || "all".equals(style))) {
            return false;
        }
        long interval = RemoteConfig.getLong(CONFIG_APP_START_AD_FREQ)*60*60*1000;
        long ramp = RemoteConfig.getLong(CONFIG_APP_START_AD_RAMP)*60*60*1000;
        long last = getLastShowTime(true);
        if (last == 0 && TextUtils.isEmpty(pkg)) {
            return false;
        }
        long actualLast = last == 0? CommonUtils.getInstallTime(PolestarApp.getApp(), PolestarApp.getApp().getPackageName()): last;
        long delta =
                preload? System.currentTimeMillis() - actualLast + 15*60*1000: System.currentTimeMillis()-actualLast;
        boolean need =  last == 0? delta > ramp: delta > interval;
        if (filterPkgs == null) {
            filterPkgs = new HashSet<>();
            String[] arr = RemoteConfig.getString(CONFIG_APP_START_AD_FILTER).split(":");
            if(arr !=null) {
                for (String s : arr) {
                    filterPkgs.add(s);
                }
            }
        }
        MLogs.d("needLoad start app ad: " + need);
        return (need && (!filterPkgs.contains(pkg)||pkg == null)) || BuildConfig.DEBUG;
    }



    private static long getLastShowTime(boolean interstitial) {
        return interstitial? PreferencesUtils.getLong(PolestarApp.getApp(), "app_start_last", 0)
                : PreferencesUtils.getLong(PolestarApp.getApp(), "app_start_last_native", 0);
    }

    private static void updateShowTime(boolean interstitial) {
        if (interstitial) {
            PreferencesUtils.putLong(PolestarApp.getApp(), "app_start_last", System.currentTimeMillis());
        } else {
            PreferencesUtils.putLong(PolestarApp.getApp(), "app_start_last_native", System.currentTimeMillis());
        }
    }


    public static void startAppStartActivity(Activity activity, CloneModel model) {
        String packageName = model.getPackageName();
        if (CloneManager.needUpgrade(packageName)) {
            CloneManager.killApp(packageName);
        } else {
            if (CloneManager.isAppLaunched(packageName)) {
                CloneManager.launchApp(packageName);
                EventReporter.appStart(true, model.getLockerState() != AppConstants.AppLockState.DISABLED, "home", model.getPackageName());
                return;
            }
        }
        Intent intent = new Intent(activity, AppLoadingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, packageName);
        intent.putExtra(AppConstants.EXTRA_FROM, AppConstants.VALUE_FROM_HOME);
        intent.putExtra(EXTRA_FIRST_START, model.getLaunched() == 0);

        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

    }

    private boolean initData() {

        Intent intent = getIntent();
        if (intent != null) {
            String packageName = intent.getStringExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME);
            from = intent.getStringExtra(AppConstants.EXTRA_FROM);
            appModel = CloneManager.getInstance(this).getCloneModel(packageName);
            firstStart = intent.getBooleanExtra(EXTRA_FIRST_START, false);
            
        }
        if (appModel == null) {
            Toast.makeText(this, getString(R.string.toast_shortcut_invalid), Toast.LENGTH_LONG);
            finish();
            return false;
        } else {
            needDoUpGrade = CloneManager.needUpgrade(appModel.getPackageName());
        }
        EventReporter.appStart(CloneManager.isAppLaunched(appModel.getPackageName()), appModel.getLockerState() != AppConstants.AppLockState.DISABLED, from, appModel.getPackageName());
        return true;
    }

    private void doLaunch(){
        MLogs.d("doLaunch " + launched);
        if (launched) return;
        launched = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Todo: if app is already launched, just switch it to front, no need re-launch
                if (needDoUpGrade) {
                    CloneManager.upgradeApp(appModel.getPackageName());
                }
                CloneManager.launchApp(appModel.getPackageName());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MLogs.d("AppStart finish");
                        finish();
                    }
                }, 4000);
            }
        }, 500);
    }

    private void initView() {
        mLoadingView = (ProgressBar) findViewById(R.id.loading);
        //It's the first one trick here, ProgressBar widget will be drawn in indeterminate mode
        //event if the parent view is INVISIBLE.
        mLoadingView.setIndeterminate(true);
        //It's the second trick here, need to receive broadcast to finish itself.

        mImgCircle = (ImageView) findViewById(R.id.img_success_bg2);
        mImgAppIcon = (ImageView) findViewById(R.id.img_app_icon);
        mTxtTips = (TextView) findViewById(R.id.txt_launch_tips);
        mFirstStartTips = (TextView) findViewById(R.id.txt_first_launch_tips);
        mNativeContainer = (LinearLayout) findViewById(R.id.ad_container);


        try{
            CustomizeAppData data = CustomizeAppData.loadFromPref(appModel.getPackageName());
            mImgAppIcon.setImageBitmap(data.getCustomIcon());
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mImgAppIcon, "scaleX", 0.7f, 1.2f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mImgAppIcon, "scaleY", 0.7f, 1.2f, 1.0f);
            AnimatorSet animSet = new AnimatorSet();
            animSet.play(scaleX).with(scaleY);
            animSet.setInterpolator(new BounceInterpolator());
            animSet.setDuration(500).start();
            animSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

                }
            });
            mTxtTips.setText(String.format(getString(R.string.app_starting_tips), appModel.getName()));
            if (PreferencesUtils.isFirstStart(appModel.getName())){
                PreferencesUtils.setStarted(appModel.getName());
                mFirstStartTips.setVisibility(View.VISIBLE);
                mFirstStartTips.setText(getString(R.string.first_start_tips));
            }else if(needDoUpGrade) {
                mFirstStartTips.setVisibility(View.VISIBLE);
                mFirstStartTips.setText(getString(R.string.upgrade_start_tips));
            }


            ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            sa.setDuration(1000);
            sa.setRepeatMode(Animation.RESTART);
            sa.setRepeatCount(Animation.INFINITE);
            AlphaAnimation aa = new AlphaAnimation(0, 1);
            aa.setDuration(1000);
            aa.setRepeatMode(Animation.RESTART);
            aa.setRepeatCount(Animation.INFINITE);
            AnimationSet as = new AnimationSet(true);
            as.addAnimation(sa);
            as.addAnimation(aa);
            mImgCircle.startAnimation(as);

        }catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.app_loading_activity);

        if (initData()) {
            initView();
        }
        if (needLoadNativeAd(false, appModel.getPackageName())) {
            loadNativeAd();
        }
    }

    public static AdSize getBannerSize() {
        int dpWidth = DisplayUtils.px2dip(VirtualCore.get().getContext(), DisplayUtils.getScreenWidth(VirtualCore.get().getContext()));
        dpWidth = Math.max(320, dpWidth*9/10);
        return  new AdSize(dpWidth, 280);
    }

    private void inflateNativeAd(IAdAdapter ad) {
        final AdViewBinder viewBinder;

        switch (ad.getAdType()) {
            case AdConstants.NativeAdType.AD_SOURCE_FACEBOOK:
                viewBinder =  new AdViewBinder.Builder(R.layout.app_start_native_ad_fb)
                        .titleId(R.id.ad_title)
                        .textId(R.id.ad_subtitle_text)
                        .mainMediaId(R.id.ad_cover_image)
                        .callToActionId(R.id.ad_cta_text)
                        .iconImageId(R.id.ad_icon_image)
                        .privacyInformationId(R.id.ad_choices_container)
                        .build();
                break;
            default:
                viewBinder =  new AdViewBinder.Builder(R.layout.app_start_native_ad)
                        .titleId(R.id.ad_title)
                        .textId(R.id.ad_subtitle_text)
                        .mainMediaId(R.id.ad_cover_image)
                        .iconImageId(R.id.ad_icon_image)
                        .callToActionId(R.id.ad_cta_text)
                        .privacyInformationId(R.id.ad_choices_image)
                        .build();
                break;
        }


        View adView = ad.getAdView(viewBinder);
        if (adView != null) {
            adView.setBackgroundColor(0);
            mNativeContainer.removeAllViews();
            mNativeContainer.addView(adView);
            mNativeContainer.setVisibility(View.VISIBLE);
        }
        MLogs.d("Inflate native ad:" + ad.getAdType());
    }

    private void loadNativeAd(){
        mNativeAdLoader = FuseAdLoader.get(SLOT_APP_START_NATIVE, this);
        mNativeAdLoader.setBannerAdSize(getBannerSize());
        hasShownInterstitialAd = false;
        if(mNativeAdLoader.hasValidAdSource()){
            mNativeAdLoader.loadAd(1, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    updateShowTime(false);
                    inflateNativeAd(ad);
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(initData()) {
            initView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!hasShownInterstitialAd && needLoadInterstitialAd(false, appModel.getPackageName())) {
            loadInterstitialAd();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //not have ad loaded
                    if (!hasShownInterstitialAd) {
                        doLaunch();
                    }
                }
                //wait 4000ms
            }, 3000);
        } else {
            doLaunch();
        }
    }

    public static void preloadAd(Context context) {
        if (needLoadInterstitialAd(true, null)) {
            FuseAdLoader.get(SLOT_APP_START_INTERSTITIAL, context).preloadAd();
        }
        if (needLoadNativeAd(true, null)) {
            FuseAdLoader.get(SLOT_APP_START_NATIVE, context).setBannerAdSize(getBannerSize()).preloadAd();
        }
    }

    private long loadingStart = 0;
    public void loadInterstitialAd() {
        mInterstitialAdLoader = FuseAdLoader.get(SLOT_APP_START_INTERSTITIAL, this);
        hasShownInterstitialAd = false;
        loadingStart = System.currentTimeMillis();
        if(mInterstitialAdLoader.hasValidAdSource()){
            mInterstitialAdLoader.loadAd(1, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    if (!launched && !PreferencesUtils.isAdFree()) {
                        long delta = System.currentTimeMillis() - loadingStart;
                        if (delta < 0) {
                            ad.show();
                            EventReporter.generalClickEvent("app_start_ad_show");
                            MLogs.d("Show app start ad");
                            hasShownInterstitialAd = true;
                            updateShowTime(true);
                        } else {
                            mNativeContainer.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ad.show();
                                    EventReporter.generalClickEvent("app_start_ad_show");
                                    MLogs.d("Show app start ad");
                                    hasShownInterstitialAd = true;
                                    updateShowTime(true);
                                }
                            }, INTERSTITIAL_SHOW_DELAY - delta);
                        }
                    }
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {
                    MLogs.d(SLOT_APP_START_INTERSTITIAL + " load error:" + error);
                    doLaunch();
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }
}

