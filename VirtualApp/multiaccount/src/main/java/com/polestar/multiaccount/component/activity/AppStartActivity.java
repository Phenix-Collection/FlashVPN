package com.polestar.multiaccount.component.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.os.VUserHandle;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.multiaccount.BuildConfig;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.model.CustomizeAppData;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.EventReporter;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.polestar.multiaccount.utils.ToastUtils;

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

    private  AppModel appModel;
    private String from;
    private boolean needDoUpGrade;
    private FuseAdLoader mAdLoader;
    private static final String SLOT_APP_START = "slot_app_start";
    private static final String CONFIG_APP_START_AD_FREQ = "slot_app_start_freq_hour";
    private static final String CONFIG_APP_START_AD_RAMP = "slot_app_start_ramp_hour";
    private static final String CONFIG_APP_START_AD_FILTER = "slot_app_start_filter";
    private static HashSet<String> filterPkgs ;
    private boolean hasShownAd;
    private boolean launched;

    public static boolean needLoadAd(boolean preload, String pkg) {
        long interval = RemoteConfig.getLong(CONFIG_APP_START_AD_FREQ)*60*60*1000;
        long ramp = RemoteConfig.getLong(CONFIG_APP_START_AD_RAMP)*60*60*1000;
        long last = getLastShowTime();
        if (last == 0 && TextUtils.isEmpty(pkg)) {
            return false;
        }
        long actualLast = last == 0? CommonUtils.getInstallTime(MApp.getApp(), pkg): last;
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
        return (need && !filterPkgs.contains(pkg)) || BuildConfig.DEBUG;
    }

    public void loadAd() {
        mAdLoader = FuseAdLoader.get(SLOT_APP_START, this);
        hasShownAd = false;
        if(mAdLoader.hasValidAdSource()){
            mAdLoader.loadAd(1, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    if (!launched) {
                        ad.show();
                        EventReporter.generalClickEvent(AppStartActivity.this, "app_start_ad_show");
                        hasShownAd = true;
                        updateShowTime();
                    }
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {
                    MLogs.d(SLOT_APP_START + " load error:" + error);
                    doLaunch();
                }
            });
        }
    }

    private static long getLastShowTime() {
        return PreferencesUtils.getLong(MApp.getApp(), "app_start_last", 0);
    }

    private static void updateShowTime() {
        PreferencesUtils.putLong(MApp.getApp(), "app_start_last", System.currentTimeMillis());
    }

    public static void startAppStartActivity(Activity activity, String packageName) {
        if (AppManager.needUpgrade(packageName)) {
            VirtualCore.get().killApp(packageName, VUserHandle.USER_ALL);
        } else {
            if (AppManager.isAppLaunched(packageName)) {
                AppManager.launchApp(packageName);
                return;
            }
        }
        Intent intent = new Intent(activity, AppStartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, packageName);
        intent.putExtra(AppConstants.EXTRA_FROM, AppConstants.VALUE_FROM_HOME);

        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

    }

    private void initData() {

        Intent intent = getIntent();
        launched = false;
        if (intent != null) {
            String packageName = intent.getStringExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME);
            from = intent.getStringExtra(AppConstants.EXTRA_FROM);

            if (packageName != null) {
                List<AppModel> appModels = DbManager.queryAppModelByPackageName(this, packageName);
                if (appModels != null && appModels.size() > 0) {
                    appModel = appModels.get(0);
                }
            }
        }
        if (appModel == null) {
            ToastUtils.ToastDefult(this, getString(R.string.toast_shortcut_invalid));
            finish();
        } else {
            needDoUpGrade = AppManager.needUpgrade(appModel.getPackageName());
        }
    }

    private void doLaunch(){
        if (launched) return;
        launched = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                EventReporter.launchApp(AppStartActivity.this, appModel.getPackageName(), from, appModel.getLockerState() != AppConstants.AppLockState.DISABLED);
                // Todo: if app is already launched, just switch it to front, no need re-launch
                if (needDoUpGrade) {
                    AppManager.upgradeApp(appModel.getPackageName());
                }
                AppManager.launchApp(appModel.getPackageName());
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
        setContentView(R.layout.activity_start);

        initData();
        initView();

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
        if (!hasShownAd && needLoadAd(false, appModel.getPackageName())) {
            loadAd();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //not have ad loaded
                    if (!hasShownAd) {
                        doLaunch();
                    }
                }
                //wait 4000ms
            }, 3000);
        } else {
            doLaunch();
        }
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
