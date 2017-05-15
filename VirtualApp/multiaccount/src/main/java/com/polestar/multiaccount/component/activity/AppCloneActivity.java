package com.polestar.multiaccount.component.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.lody.virtual.client.core.VirtualCore;
import com.nostra13.universalimageloader.utils.L;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAd;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.imageloader.widget.BasicLazyLoadImageView;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.utils.BitmapUtils;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.MTAManager;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.polestar.multiaccount.utils.ToastUtils;
import com.polestar.multiaccount.widgets.BlueSwitch;
import com.polestar.multiaccount.widgets.StarLevelLayoutView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import mirror.android.widget.Toast;

/**
 * Created by guojia on 2016/12/4.
 */

public class AppCloneActivity extends BaseActivity {

    private String mPkgName;
    private String mPkgLabel;

    private Button mBtnStart;
    private RelativeLayout mLayoutInstalling;
    private RelativeLayout mLayoutCancel;
    private TextView mTxtAppLabel;
    private ImageView mImgAppIcon;
    private ImageView mImgSuccessBg;
    private TextView mTxtInstalling;
    private TextView mTxtInstalled;
    private ProgressBar mProgressBar;
    private TextView mTitleText;
    private BlueSwitch mShortcutSwitch;
    private BlueSwitch mLockerSwitch;
    private BlueSwitch mNotificationSwitch;

    private Timer mTimer = new Timer();
    private static final double INIT_PROGRESS_THRESHOLD = 50.0;
    private static final double INIT_PROGRESS_SPEED = 0.5;
    private static final int SPEED_STEPS = 20;
    private static final int STEP_INTERVAL = 20;
    private static final int ANIMATION_STEP = 333;

    private static final int MSG_ANIM_PROGRESS_FINISHED = 0;
    private static final int MSG_INSTALL_FINISHED = 1;
    private static final String CONFIG_KEY_SHOW_AD_AFTER_CLONE = "show_ad_after_clone";
    private static final String SLOT_AD_AFTER_CLONE = "slot_ad_after_clone";

    private List<AdConfig> adConfigList;
    private AppModel appModel;
    private boolean isInstallSuccess;
    private boolean isInstallDone;
    private boolean isCanceled;
    private NativeExpressAdView mAdmobExpressView;
    private boolean admobReady;
    private boolean fbReady;
    private IAd nativeAd;
    private boolean animateEnd;
    private LinearLayout nativeAdContainer;
    private FuseAdLoader mNativeAdLoader;

    private RelativeLayout mCloneSettingLayout;
    private boolean isDBUpdated ;

    private Handler mAnimateHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ANIM_PROGRESS_FINISHED:
                    handleFakeInstallFinished();
                    break;
                case MSG_INSTALL_FINISHED:
                    handleInstallFinished();
                    break;
            }
        }
    };

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            appModel = intent.getParcelableExtra(AppConstants.EXTRA_APP_MODEL);
        }
        if (appModel == null) {
            Intent intentFail = new Intent();
            intentFail.putExtra(AppConstants.EXTRA_IS_INSTALL_SUCCESS, isInstallSuccess);
            intentFail.putExtra(AppConstants.EXTRA_APP_MODEL, appModel);
            setResult(RESULT_OK, intentFail);
            finish();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean installed = false;
                    try{
                        installed = AppManager.isAppInstalled(mPkgName);
                    } catch (Exception e) {
                        MLogs.logBug(MLogs.getStackTraceString(e));
                    }
                    if (!installed) {
                        L.d("To install app " + mPkgName);
                        isInstallSuccess = AppManager.installApp(AppCloneActivity.this, appModel);
                        isInstallDone = true;
                        if (isInstallSuccess) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CloneHelper.getInstance(AppCloneActivity.this).installApp(AppCloneActivity.this, appModel);
                                    AppManager.setAppNotificationFlag(mPkgName, true);
                                    isDBUpdated = true;
                                }
                            });
                            MTAManager.applistClone(AppCloneActivity.this, appModel.getPackageName());
                            // showAd(installAd);
                        } else {
                            MTAManager.keyLog(AppCloneActivity.this, MTAManager.KeyLogTag.AERROR, "cloneError:"+ mPkgName);
                        }
                    } else {
                        isInstallSuccess = true;
                        isInstallDone = true;
                    }
                }
            }).start();
        }
    }

    private void initView() {
        mPkgName = appModel.getPackageName();
        mPkgLabel = appModel.getName();

        mBtnStart = (Button) findViewById(R.id.btn_start);
        mLayoutInstalling = (RelativeLayout) findViewById(R.id.layout_installing);
        mLayoutCancel = (RelativeLayout) findViewById(R.id.layout_title);
        mTitleText = (TextView)mLayoutCancel.findViewById(R.id.title_text);
        mTxtAppLabel = (TextView) findViewById(R.id.txt_app_name);
        mImgAppIcon = (ImageView) findViewById(R.id.img_app_icon);
        mImgSuccessBg = (ImageView) findViewById(R.id.img_success_bg);
        mTxtInstalling = (TextView) findViewById(R.id.txt_installing);
        mTxtInstalled = (TextView) findViewById(R.id.txt_installed);
        mProgressBar = (ProgressBar) findViewById(R.id.circularProgressbar);
        nativeAdContainer = (LinearLayout) findViewById(R.id.ad_container);

        mBtnStart.setVisibility(View.INVISIBLE);

        mTxtAppLabel.setText(mPkgLabel);
        appModel.setIcon(appModel.initDrawable(this));
        mImgAppIcon.setBackground(appModel.getIcon());

        mCloneSettingLayout = (RelativeLayout) findViewById(R.id.clone_setting_layout);

        mTxtInstalling.setText(String.format(getString(R.string.cloning_tips), mPkgLabel));

        mProgressBar.setSecondaryProgress(100);
        mProgressBar.setProgress(0);
        mLayoutCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cleanBeforeFinish();
                finish();
            }
        });

        mShortcutSwitch = (BlueSwitch) findViewById(R.id.shortcut_swichbtn);
        mLockerSwitch = (BlueSwitch) findViewById(R.id.locker_swichbtn);
        mNotificationSwitch = (BlueSwitch) findViewById(R.id.notification_swichbtn);
        initSwitchStatus(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSwitchStatus(false);
    }

    private void initSwitchStatus(boolean firstTime) {
        mShortcutSwitch.setChecked(false);
//        if(firstTime) {
//            mLockerSwitch.setChecked(PreferencesUtils.isLockerEnabled(this));
//        } else {
//            mLockerSwitch.setChecked(appModel.getLockerState() != AppConstants.AppLockState.DISABLED);
//        }
        mLockerSwitch.setChecked(appModel.getLockerState() != AppConstants.AppLockState.DISABLED);
        mLockerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLockerSwitch.isChecked() && !PreferencesUtils.isLockerEnabled(AppCloneActivity.this)) {
                    mLockerSwitch.setChecked(false);
                    ToastUtils.ToastBottow(AppCloneActivity.this, "Please enable locker function and set password at first!");
                    LockSettingsActivity.start(AppCloneActivity.this,"clone");
                }
            }
        });
        mNotificationSwitch.setChecked(appModel.isNotificationEnable());
    }

    private void doSwitchStateChange() {
        if (appModel != null && isDBUpdated) {
            appModel.setNotificationEnable(mNotificationSwitch.isChecked());
            appModel.setLockerState(mLockerSwitch.isChecked() ? AppConstants.AppLockState.ENABLED_FOR_CLONE : AppConstants.AppLockState.DISABLED);
            DbManager.updateAppModel(this, appModel);
            if (mShortcutSwitch.isChecked()) {
                CommonUtils.createShortCut(this, appModel);
            }
            VirtualCore.get().reloadLockerSetting(null);
            MTAManager.settingAfterClone(this, appModel.getPackageName(), mNotificationSwitch.isChecked(), mLockerSwitch.isChecked(), mShortcutSwitch.isChecked());
        }
    }

    public static void startAppCloneActivity(Activity activity, AppModel appModel) {
        Intent intent = new Intent(activity, AppCloneActivity.class);
        intent.putExtra(AppConstants.EXTRA_APP_MODEL, appModel);
        activity.startActivityForResult(intent, AppConstants.REQUEST_INSTALL_APP);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void initAd(){
        boolean showAd = RemoteConfig.getBoolean(CONFIG_KEY_SHOW_AD_AFTER_CLONE);
        MLogs.d(CONFIG_KEY_SHOW_AD_AFTER_CLONE + showAd);
        adConfigList = RemoteConfig.getAdConfigList(SLOT_AD_AFTER_CLONE);
        if (showAd && adConfigList!= null && adConfigList.size() > 0) {
            initAdmobBannerView();
            loadAd();
        }
    }

    private void initAdmobBannerView() {
        mAdmobExpressView = new NativeExpressAdView(this);
        String adunit  = null;
        if (adConfigList != null) {
            for (AdConfig adConfig: adConfigList) {
                if (adConfig.source != null && adConfig.source.equals(AdConstants.NativeAdType.AD_SOURCE_ADMOB_NAVTIVE_BANNER)){
                    adunit = adConfig.key;
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(adunit)) {
            mAdmobExpressView = null;
            return;
        }
        mAdmobExpressView.setAdSize(new AdSize(360, 260));
//        mAdmobExpressView.setAdUnitId("ca-app-pub-5490912237269284/2431070657");
        mAdmobExpressView.setAdUnitId(adunit);
        mAdmobExpressView.setVisibility(View.GONE);
        mAdmobExpressView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                AdLog.d("onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                AdLog.d("Clone admob onAdFailedToLoad " + i);
                mAdmobExpressView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                AdLog.d("on Banner AdLoaded ");
                admobReady = true;
                showAdIfNeeded();
            }
        });
    }

    private void showAdIfNeeded(){
        MLogs.d("Animate end: " + animateEnd + " fb: " + fbReady + " admob: " + admobReady);
        TextView sponsor = (TextView) findViewById(R.id.sponsor_text);
        if(animateEnd) {
            if (fbReady) {
                sponsor.setVisibility(View.VISIBLE);
                MTAManager.appCloneAd(this, "FB");
                inflateFbNativeAdView(nativeAd);
            } else if(admobReady){
                sponsor.setVisibility(View.VISIBLE);
                showBannerAd();
                MTAManager.appCloneAd(this, "Admob");
            }
        }

    }

    private void showBannerAd(){
        MLogs.d("AppClone showBannerAd");
        nativeAdContainer.removeAllViews();
        mAdmobExpressView.setVisibility(View.VISIBLE);
//        LinearLayout admobContainer = new LinearLayout(this);
//        admobContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        admobContainer.setOrientation(LinearLayout.VERTICAL);
//        admobContainer.addView(mAdmobExpressView);
//        nativeAdContainer.addView(admobContainer);
        nativeAdContainer.addView(mAdmobExpressView);
        nativeAdContainer.setVisibility(View.VISIBLE);
    }

    private void inflateFbNativeAdView(IAd ad) {
        View adView = LayoutInflater.from(this).inflate(R.layout.after_clone_native_ad, null);
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        adView.setLayoutParams(params);
        if (ad != null && adView != null) {
            BasicLazyLoadImageView coverView = (BasicLazyLoadImageView) adView.findViewById(R.id.ad_cover_image);
            coverView.setDefaultResource(0);
            coverView.requestDisplayURL(ad.getCoverImageUrl());
            BasicLazyLoadImageView iconView = (BasicLazyLoadImageView) adView.findViewById(R.id.ad_icon_image);
            iconView.setDefaultResource(0);
            iconView.requestDisplayURL(ad.getIconImageUrl());
            TextView titleView = (TextView) adView.findViewById(R.id.ad_title);
            titleView.setText(ad.getTitle());
            TextView subtitleView = (TextView) adView.findViewById(R.id.ad_subtitle_text);
            subtitleView.setText(ad.getBody());
            TextView ctaView = (TextView) adView.findViewById(R.id.ad_cta_text);
            ctaView.setText(ad.getCallToActionText());
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(ctaView, "scaleX", 0.7f, 1.2f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(ctaView, "scaleY", 0.7f, 1.2f, 1.0f);
            AnimatorSet animSet = new AnimatorSet();
            animSet.play(scaleX).with(scaleY);
            animSet.setInterpolator(new BounceInterpolator());
            animSet.setDuration(1200).start();
            animSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                }
            });

            StarLevelLayoutView starLevelLayout = (StarLevelLayoutView)adView.findViewById(R.id.star_rating_layout);
            starLevelLayout.setRating((int)ad.getStarRating());
            nativeAdContainer.removeAllViews();
            nativeAdContainer.addView(adView);
            ad.registerViewForInteraction(nativeAdContainer);
            if (ad.getPrivacyIconUrl() != null) {
                BasicLazyLoadImageView choiceIconImage = (BasicLazyLoadImageView) adView.findViewById(R.id.ad_choices_image);
                choiceIconImage.setDefaultResource(0);
                choiceIconImage.requestDisplayURL(ad.getPrivacyIconUrl());
                ad.registerPrivacyIconView(choiceIconImage);
            }
        }
    }

    private void loadAd() {
        if (mNativeAdLoader == null) {
            mNativeAdLoader = FuseAdLoader.get(SLOT_AD_AFTER_CLONE, this);
            ///mNativeAdLoader.addAdSource(AdConstants.NativeAdType.AD_SOURCE_FACEBOOK, "1700354860278115_1702636763383258", -1);
        }
        if ( mNativeAdLoader.hasValidAdSource()) {
            mNativeAdLoader.loadAd(1, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAd ad) {
                    if (ad.getAdType().equals(AdConstants.NativeAdType.AD_SOURCE_FACEBOOK)
                            || ad.getAdType().equals(AdConstants.NativeAdType.AD_SOURCE_VK)) {
                        fbReady = true;
                        nativeAd = ad;
                        showAdIfNeeded();
                    }
                }

                @Override
                public void onAdListLoaded(List<IAd> ads) {

                }

                @Override
                public void onError(String error) {
                    loadAdmobNativeExpress();
                }
            });
        } else {
            loadAdmobNativeExpress();
        }
    }

    private void loadAdmobNativeExpress(){
        if (mAdmobExpressView == null) {
            return;
        }
        MLogs.d("AppClone loadAdmobNativeExpress");
        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID(this);
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            boolean isTestDevice = request.isTestDevice(this);
            AdLog.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
            AdLog.d( "Admob unit id "+ mAdmobExpressView.getAdUnitId());
            mAdmobExpressView.loadAd(request );
        } else {
            mAdmobExpressView.loadAd(new AdRequest.Builder().build());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_install_layout);
        initData();
        initView();
        initAd();

        final TimerTask task = new TimerTask() {
            double speed = INIT_PROGRESS_SPEED;
            double threshold = INIT_PROGRESS_THRESHOLD;
            double nextSpeed;
            double speedStep;
            double progress = 0;
            boolean inDecelerationStatus = false;

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setProgress((int)progress);
                    }
                });

                if(inDecelerationStatus){
                    double tempSpeed = speed - speedStep;
                    if(tempSpeed < nextSpeed){
                        speed = nextSpeed;
                    }else{
                        speed = tempSpeed;
                    }
                }else{
                    speed = INIT_PROGRESS_SPEED;
                    threshold = INIT_PROGRESS_THRESHOLD;
                }

                if(isInstallDone){
                    speed = INIT_PROGRESS_SPEED;
                }else if(progress > threshold){
                    inDecelerationStatus = true;
                    threshold = 100.0 - (100 - threshold) / 2.0;
                    nextSpeed = speed / 2;
                    speedStep = (speed - nextSpeed) / SPEED_STEPS;
                }

                progress += speed;

                if(progress > 100){
                    mTimer.cancel();
                    mAnimateHandler.sendEmptyMessageDelayed(MSG_ANIM_PROGRESS_FINISHED, ANIMATION_STEP);
                }
            }
        };
        Animation progressFadeIn = AnimationUtils.loadAnimation(AppCloneActivity.this, R.anim.progress_fade_in);
        progressFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                try {
                    mTimer.scheduleAtFixedRate(task, 0, STEP_INTERVAL);
                }catch (Exception e) {
                    MLogs.e(e);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mProgressBar.startAnimation(progressFadeIn);
    }

    @Override
    public void onDestroy(){
        cleanBeforeFinish();
        doSwitchStateChange();
        super.onDestroy();
    }

    private void cleanBeforeFinish(){
        if(mTimer != null){
            mTimer.cancel();
        }
    }

    private void handleFakeInstallFinished(){

        AlphaAnimation progressFadeOut = new AlphaAnimation(1, 0);
        progressFadeOut.setDuration(ANIMATION_STEP * 3);
        progressFadeOut.setFillAfter(true);
        final AlphaAnimation successBgFadeIn = new AlphaAnimation(0, 1);
        successBgFadeIn.setDuration(ANIMATION_STEP);
        successBgFadeIn.setFillAfter(true);
        successBgFadeIn.setFillBefore(true);
        AlphaAnimation installingFadeOut = new AlphaAnimation(1, 0);
        installingFadeOut.setDuration(ANIMATION_STEP * 2);
        installingFadeOut.setFillAfter(true);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //AppManager.launchApp(mPkgName);
                AppStartActivity.startAppStartActivity(AppCloneActivity.this, mPkgName);
                finish();
            }
        });
        //mImgSuccessBg.setVisibility(View.VISIBLE);
        appModel.setCustomIcon(BitmapUtils.createCustomIcon(AppCloneActivity.this, appModel.getIcon() ));
        mImgAppIcon.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);

        //mLayoutCancel.startAnimation(installingFadeOut);

        //mTxtInstalling.setVisibility(View.INVISIBLE);
        mLayoutCancel.setVisibility(View.INVISIBLE);
        mTxtInstalling.setVisibility(View.INVISIBLE);
        mTxtAppLabel.setVisibility(View.INVISIBLE);
        mTxtInstalled.setText(String.format(getString(R.string.clone_success), mPkgLabel));
        showCloneSetting();
    }

    private void showCloneSetting() {
        initSwitchStatus(true);
        final AlphaAnimation successBgFadeIn = new AlphaAnimation(0, 1);
        successBgFadeIn.setDuration(ANIMATION_STEP);
        successBgFadeIn.setFillAfter(true);
        successBgFadeIn.setFillBefore(true);
        mCloneSettingLayout.setVisibility(View.VISIBLE);
        mCloneSettingLayout.startAnimation(successBgFadeIn);
        ImageView icon = (ImageView)mCloneSettingLayout.findViewById(R.id.img_app_icon_done);
        icon.setBackground(null);
        icon.setImageBitmap(appModel.getCustomIcon());
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 0.7f, 1.3f, 1.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 0.7f, 1.3f, 1.1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY);
        animSet.setInterpolator(new BounceInterpolator());
        animSet.setDuration(800).start();
        TranslateAnimation startBtnMoveUp = new TranslateAnimation(0, 0, DisplayUtils.dip2px(this, 70), 0);
        startBtnMoveUp.setDuration(ANIMATION_STEP);
        startBtnMoveUp.setFillAfter(true);
        startBtnMoveUp.setFillBefore(true);
        final AnimationSet startBtnSet = new AnimationSet(true);
        startBtnSet.addAnimation(startBtnMoveUp);
        startBtnSet.addAnimation(successBgFadeIn);
        mBtnStart.startAnimation(startBtnSet);

        successBgFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mBtnStart.setVisibility(View.VISIBLE);
                animateEnd = true;
                showAdIfNeeded();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void handleInstallFinished(){

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isCanceled = true;
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }


}
