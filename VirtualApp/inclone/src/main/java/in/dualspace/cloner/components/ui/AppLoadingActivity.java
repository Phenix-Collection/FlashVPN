package in.dualspace.cloner.components.ui;

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
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.clone.os.VUserHandle;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;

import in.dualspace.cloner.AppConstants;
import in.dualspace.cloner.BuildConfig;
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.R;
import in.dualspace.cloner.clone.CloneManager;
import in.dualspace.cloner.components.AppMonitorService;
import in.dualspace.cloner.db.CloneModel;
import com.polestar.clone.CustomizeAppData;
import in.dualspace.cloner.utils.CommonUtils;
import in.dualspace.cloner.utils.DisplayUtils;
import in.dualspace.cloner.utils.EventReporter;
import in.dualspace.cloner.utils.MLogs;
import in.dualspace.cloner.utils.PreferencesUtils;
import in.dualspace.cloner.utils.RemoteConfig;

import java.util.HashSet;
import java.util.List;

/**
 * Created by DualApp on 2017/7/16.
 */

/**
 * Created by DualApp on 2016/12/5.
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

    private FuseAdLoader mNativeAdLoader;
    private static final String SLOT_APP_START_NATIVE = "slot_app_start_native";
    private static final String CONFIG_APP_START_AD_FILTER = "slot_app_start_filter";
    private static final String CONFIG_APP_START_AD_STYLE = "slot_app_start_style"; //native,interstitial,all
    private static final String CONFIG_APP_START_NATIVE_AD_FREQ = "slot_app_start_native_freq_min";
    private static final String CONFIG_APP_START_NATIVE_AD_RAMP = "slot_app_start_native_ramp_min";
    public final static String CONFIG_NEED_PRELOAD_LOADING = "conf_need_preload_start_ad";
    private static HashSet<String> filterPkgs ;
    private LinearLayout mNativeContainer;
    private Handler mainHandler;
    private boolean isAppRunning;

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
        long last = getLastShowTime();
        if (last == 0 && TextUtils.isEmpty(pkg)) {
            return false;
        }
        long actualLast = last == 0? CommonUtils.getInstallTime(DualApp.getApp(), DualApp.getApp().getPackageName()): last;
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

    private static long getLastShowTime() {
        return PreferencesUtils.getLong(DualApp.getApp(), "app_start_last_native", 0);
    }

    private static void updateShowTime() {

        PreferencesUtils.putLong(DualApp.getApp(), "app_start_last_native", System.currentTimeMillis());
    }


    public static void startAppStartActivity(Activity activity, CloneModel model) {
        String packageName = model.getPackageName();
        if (CloneManager.needUpgrade(packageName)) {
            CloneManager.killApp(packageName);
        }
        Intent intent = new Intent(activity, AppLoadingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, packageName);
        intent.putExtra(AppConstants.EXTRA_FROM, AppConstants.FROM_HOME);
        intent.putExtra(EXTRA_FIRST_START, model.getLaunched() == 0);
        intent.putExtra(AppConstants.EXTRA_CLONED_APP_USERID, model.getPkgUserId());

        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

    }

    private boolean initData() {
        Intent intent = getIntent();
        if (intent != null) {
            String packageName = intent.getStringExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME);
            int userId = intent.getIntExtra(AppConstants.EXTRA_CLONED_APP_USERID, VUserHandle.myUserId());
            from = intent.getStringExtra(AppConstants.EXTRA_FROM);
            appModel = CloneManager.getInstance(this).getCloneModel(packageName, userId);
            firstStart = intent.getBooleanExtra(EXTRA_FIRST_START, false);
            
        }
        if (appModel == null) {
            Toast.makeText(this, getString(R.string.toast_shortcut_invalid), Toast.LENGTH_LONG);
            finish();
            return false;
        } else {
            needDoUpGrade = CloneManager.needUpgrade(appModel.getPackageName());
            isAppRunning = CloneManager.isAppLaunched(appModel);
            MLogs.d("isAppRunning " + isAppRunning);
        }
        if (AppMonitorService.needLoadCoverAd(true, appModel.getPackageName())) {
            AppMonitorService.preloadCoverAd();
        }
        EventReporter.appStart(isAppRunning, appModel.getLockerState() != AppConstants.AppLockState.DISABLED, from, appModel.getPackageName(), appModel.getPkgUserId());
        return true;
    }

    private void finishIfTimeout(){
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 5000);
    }

    private void doLaunch(){
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                CloneManager.launchApp(AppLoadingActivity.this, appModel, firstStart);
            }
        }, isAppRunning? 0: 500);
        finishIfTimeout();
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
            firstStart = PreferencesUtils.isFirstStart(appModel.getName());
            if (firstStart){
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
        EventReporter.reportWake(this, "app_shortcut");
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.app_loading_activity);

        if (initData()) {
            if(!isAppRunning) {
                initView();
                if (needLoadNativeAd(false, appModel.getPackageName())) {
                    loadNativeAd();
                }
            }
            doLaunch();
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
            default:
                viewBinder =  new AdViewBinder.Builder(R.layout.app_start_native_ad)
                        .titleId(R.id.ad_title)
                        .textId(R.id.ad_subtitle_text)
                        .mainMediaId(R.id.ad_cover_image)
                        .fbMediaId(R.id.ad_fb_mediaview)
                        .admMediaId(R.id.ad_adm_mediaview)
                        .iconImageId(R.id.ad_icon_image)
                        .callToActionId(R.id.ad_cta_text)
                        .privacyInformationId(R.id.ad_choices_container)
                        .adFlagId(R.id.ad_flag)
                        .build();
                break;
        }


        View adView = ad.getAdView(this, viewBinder);
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
        if(mNativeAdLoader.hasValidAdSource()){
            mNativeAdLoader.loadAd(this, 2, 1000, new IAdLoadListener() {
                @Override
                public void onRewarded(IAdAdapter ad) {

                }

                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    updateShowTime();
                    inflateNativeAd(ad);
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onAdClicked(IAdAdapter ad) {

                }

                @Override
                public void onAdClosed(IAdAdapter ad) {

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
        finish();
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
    }

    public static void preloadAd(Context context) {
        if (needLoadNativeAd(true, null)) {
            FuseAdLoader.get(SLOT_APP_START_NATIVE, context).setBannerAdSize(getBannerSize()).preloadAd(context);
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

