package com.polestar.superclone.component.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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

import com.google.android.gms.ads.AdSize;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.os.VUserHandle;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.clone.CloneAgent64;
import com.polestar.superclone.BuildConfig;
import com.polestar.superclone.MApp;
import com.polestar.superclone.R;
import com.polestar.superclone.component.AppMonitorService;
import com.polestar.superclone.component.BaseActivity;
import com.polestar.superclone.constant.AppConstants;
import com.polestar.superclone.db.DbManager;
import com.polestar.superclone.model.AppModel;
import com.polestar.clone.CustomizeAppData;
import com.polestar.superclone.utils.AppManager;
import com.polestar.superclone.utils.CommonUtils;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.PreferencesUtils;
import com.polestar.superclone.utils.RemoteConfig;
import com.polestar.superclone.utils.ToastUtils;
import com.polestar.superclone.widgets.UpDownDialog;

import java.util.HashSet;
import java.util.List;

/**
 * Created by guojia on 2016/12/5.
 */

public class AppStartActivity extends BaseActivity {

    private ProgressBar mLoadingView;
    private ImageView mImgCircle;
    private ImageView mImgAppIcon;
    private TextView mTxtTips;
    private TextView mFirstStartTips;
    private LinearLayout nativeAdContainer;

    private  AppModel appModel;
    private String from;
    private boolean needDoUpGrade;
    private FuseAdLoader mAdLoader;
    private static final String SLOT_APP_START_NATIVE = "slot_app_start_native";
    private static final String CONFIG_APP_START_NATIVE_AD_FREQ = "slot_app_start_native_freq_min";
    private static final String CONFIG_APP_START_NATIVE_AD_RAMP = "slot_app_start_native_ramp_min";
    private static final String CONFIG_APP_START_AD_FILTER = "slot_app_start_filter";
    private static final String CONFIG_APP_START_AD_STYLE = "slot_app_start_style"; //native,interstitial,all
    public final static String CONFIG_NEED_PRELOAD_LOADING = "conf_need_preload_start_ad";
    private static HashSet<String> filterPkgs ;
    private boolean needAbiSupport;
    private boolean mFirstStart;
    private Handler  mainHandler ;

    public static boolean needLoadNativeAd(boolean preload, String pkg) {
        if (PreferencesUtils.isAdFree()) {
            return false;
        }
        if (BuildConfig.DEBUG) {
            return true;
        }
        String style = RemoteConfig.getString(CONFIG_APP_START_AD_STYLE);
        if (!("native".equals(style) || "all".equals(style) || BuildConfig.DEBUG)) {
            return false;
        }
        long interval = RemoteConfig.getLong(CONFIG_APP_START_NATIVE_AD_FREQ)*60*1000;
        long ramp = RemoteConfig.getLong(CONFIG_APP_START_NATIVE_AD_RAMP)*60*1000;
        long last = getLastShowTime();
        if (last == 0 && TextUtils.isEmpty(pkg)) {
            return false;
        }
        long actualLast = last == 0? CommonUtils.getInstallTime(MApp.getApp(), MApp.getApp().getPackageName()): last;
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
        return (need && (!filterPkgs.contains(pkg) || pkg ==null)) ;
    }

    public static AdSize getBannerSize() {
        return AdSize.MEDIUM_RECTANGLE;
    }
    public static void preloadAd(Context context) {
        if (needLoadNativeAd(true, null)) {
            FuseAdLoader.get(SLOT_APP_START_NATIVE,context).setBannerAdSize(getBannerSize()).preloadAd();
        }
    }

    private void inflateNativeAd(IAdAdapter ad) {
        final AdViewBinder viewBinder =  new AdViewBinder.Builder(R.layout.native_ad_applist)
                .titleId(R.id.ad_title)
                .textId(R.id.ad_subtitle_text)
                .mainMediaId(R.id.ad_cover_image)
                .fbMediaId(R.id.ad_fb_mediaview)
                .admMediaId(R.id.ad_adm_mediaview)
                .iconImageId(R.id.ad_icon_image)
                .callToActionId(R.id.ad_cta_text)
                .privacyInformationId(R.id.ad_choices_image)
                .build();
        View adView = ad.getAdView(viewBinder);
        if (adView != null) {
            nativeAdContainer.removeAllViews();
            nativeAdContainer.addView(adView);
            nativeAdContainer.setVisibility(View.VISIBLE);
        }
    }

    public void loadNativeAd() {
        mAdLoader = FuseAdLoader.get(SLOT_APP_START_NATIVE, this).setBannerAdSize(getBannerSize());
        if(mAdLoader.hasValidAdSource()){
            mAdLoader.loadAd(1, new IAdLoadListener() {
                @Override
                public void onRewarded(IAdAdapter ad) {

                }

                @Override
                public void onAdClicked(IAdAdapter ad) {

                }

                @Override
                public void onAdClosed(IAdAdapter ad) {

                }

                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    updateShowTime();
                    inflateNativeAd(ad);
                    mAdLoader.preloadAd();
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {
                    MLogs.d(SLOT_APP_START_NATIVE + " load error:" + error);
                    mAdLoader.preloadAd();
                }
            });
        }
    }

    private static long getLastShowTime() {
        return PreferencesUtils.getLong(MApp.getApp(), "app_start_last_native", 0);
    }

    private static void updateShowTime() {
        PreferencesUtils.putLong(MApp.getApp(), "app_start_last_native", System.currentTimeMillis());
    }

    public static void startAppStartActivity(Activity activity, String packageName, int userId) {
        if (AppManager.needUpgrade(packageName)) {
            VirtualCore.get().killApp(packageName, VUserHandle.USER_ALL);
        } else {
            if (AppManager.isAppLaunched(packageName, userId)) {
                AppManager.launchApp(packageName, userId);
                return;
            }
        }
        Intent intent = new Intent(activity, AppStartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, packageName);
        intent.putExtra(AppConstants.EXTRA_FROM, AppConstants.VALUE_FROM_HOME);
        intent.putExtra(AppConstants.EXTRA_CLONED_APP_USERID, userId);

        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            String packageName = intent.getStringExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME);
            from = intent.getStringExtra(AppConstants.EXTRA_FROM);
            int userId = intent.getIntExtra(AppConstants.EXTRA_CLONED_APP_USERID, VUserHandle.myUserId());

            if (packageName != null) {
                appModel = DbManager.queryAppModelByPackageName(this, packageName, userId);
            }
        }
        if (appModel == null) {
            ToastUtils.ToastDefult(this, getString(R.string.toast_shortcut_invalid));
            finish();
        } else {
            needAbiSupport = CloneAgent64.needArm64Support(this, appModel.getPackageName());
            needDoUpGrade = AppManager.needUpgrade(appModel.getPackageName());
            if (AppMonitorService.needLoadCoverAd(true, appModel.getPackageName())) {
                AppMonitorService.preloadCoverAd();
            }
        }
    }

    private void doLaunchMyself(){
        // Todo: if app is already launched, just switch it to front, no need re-launch
        if (needDoUpGrade) {
            AppManager.upgradeApp(appModel.getPackageName());
        }
        AppManager.launchApp(appModel.getPackageName(), appModel.getPkgUserId());
        finishIfTimeout();
    }

    private void finishIfTimeout(){
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 10000);
    }

    private void doLaunchFromAgent() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CloneAgent64 agent = new CloneAgent64(AppStartActivity.this);
                if(agent.hasSupport()) {
                    if(agent.isCloned(appModel.getPackageName(), appModel.getPkgUserId())) {
                        if (agent.isNeedUpgrade(appModel.getPackageName())) {
                            agent.upgradeApp(appModel.getPackageName());
                        }
                    } else {
                        agent.createClone(appModel.getPackageName(), appModel.getPkgUserId());
                    }
                    agent.launchApp(appModel.getPackageName(), appModel.getPkgUserId());
                    finishIfTimeout();
                } else{
                    //Guide download support package
                    EventReporter.reportArm64(appModel.getPackageName(), "start");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpDownDialog.show(AppStartActivity.this, getString(R.string.arm64_dialog_title), getString(R.string.arm64_dialog_content, appModel.getName()),
                                    getString(R.string.no_thanks), getString(R.string.install), -1, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            switch (i) {
                                                case UpDownDialog.NEGATIVE_BUTTON:
                                                    EventReporter.reportArm64(appModel.getPackageName(), "cancel");
                                                    doLaunchMyself();
                                                    break;
                                                case UpDownDialog.POSITIVE_BUTTON:
                                                    CommonUtils.jumpToMarket(AppStartActivity.this, getPackageName()+".arm64");
                                                    EventReporter.reportArm64(appModel.getPackageName(), "go");
                                                    break;
                                            }
                                        }
                                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialogInterface) {
                                    doLaunchMyself();
                                }
                            });
                        }
                    });

                }
                agent.destroy();
            }
        }).start();
    }

    private void doLaunch(){
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!needAbiSupport) {
                    doLaunchMyself();
                } else{
                    doLaunchMyself();
                    //doLaunchFromAgent();
                }
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
        nativeAdContainer = (LinearLayout) findViewById(R.id.ad_container);


        try{
            CustomizeAppData data = CustomizeAppData.loadFromPref(appModel.getPackageName(), appModel.getPkgUserId());
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
            String title = (data.customized) ? data.label : appModel.getName();
            mTxtTips.setText(String.format(getString(R.string.app_starting_tips), title));
            mFirstStart = PreferencesUtils.isFirstStart(appModel.getName());
            if (mFirstStart){
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
        mainHandler = new Handler();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_start);

        initData();
        initView();
        if (appModel != null && needLoadNativeAd(false, appModel.getPackageName())) {
            loadNativeAd();
        }
        EventReporter.reportActive(this, true,"shortcut");

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        doLaunch();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MLogs.d("AppStartActivity onDestroy");
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }
}
