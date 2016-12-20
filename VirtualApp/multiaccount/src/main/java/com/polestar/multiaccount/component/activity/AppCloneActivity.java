package com.polestar.multiaccount.component.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.utils.L;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.utils.BitmapUtils;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.MTAManager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

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
    private ImageView mImgCloned;
    private ImageView mImgSuccessNotification;
    private TextView mTxtInstalling;
    private TextView mTxtInstalled;
    private RelativeLayout mLayoutInstalled;
    private ProgressBar mProgressBar;

    private Timer mTimer = new Timer();
    private static final double INIT_PROGRESS_THRESHOLD = 50.0;
    private static final double INIT_PROGRESS_SPEED = 0.5;
    private static final int SPEED_STEPS = 20;
    private static final int STEP_INTERVAL = 20;
    private static final int ANIMATION_STEP = 333;

    private static final int MSG_ANIM_PROGRESS_FINISHED = 0;
    private static final int MSG_INSTALL_FINISHED = 1;

    private AppModel appModel;
    private boolean isInstallSuccess;
    private boolean isInstallDone;
    private boolean isCanceled;

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
                    if (!AppManager.isAppInstalled(mPkgName)) {
                        L.d("To install app " + mPkgName);
                        isInstallSuccess = AppManager.installApp(AppCloneActivity.this, appModel);
                        isInstallDone = true;
                        if (isInstallSuccess) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CloneHelper.getInstance(AppCloneActivity.this).installApp(AppCloneActivity.this, appModel);
                                    AppManager.setAppNotificationFlag(mPkgName, true);
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
        mLayoutCancel = (RelativeLayout) findViewById(R.id.layout_cancel);
        mTxtAppLabel = (TextView) findViewById(R.id.txt_app_name);
        mImgAppIcon = (ImageView) findViewById(R.id.img_app_icon);
        mImgSuccessBg = (ImageView) findViewById(R.id.img_success_bg);
        mTxtInstalling = (TextView) findViewById(R.id.txt_installing);
        mTxtInstalled = (TextView) findViewById(R.id.txt_installed);
        mProgressBar = (ProgressBar) findViewById(R.id.circularProgressbar);
        mLayoutInstalled = (RelativeLayout) findViewById(R.id.layout_installed);
        mImgSuccessNotification = (ImageView) findViewById(R.id.img_install_success_notification);

        mLayoutInstalled.setVisibility(View.INVISIBLE);
        mBtnStart.setVisibility(View.INVISIBLE);

        mTxtAppLabel.setText(mPkgLabel);
        appModel.setIcon(appModel.initDrawable(this));
        mImgAppIcon.setBackground(appModel.getIcon());

        mTxtInstalling.setText(String.format(getString(R.string.cloning_tips), mPkgLabel));
        mTxtInstalled.setText(String.format(getString(R.string.clone_success), mPkgLabel));
        mTxtInstalled.setVisibility(View.INVISIBLE);
        mProgressBar.setSecondaryProgress(100);
        mProgressBar.setProgress(0);
        mLayoutCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cleanBeforeFinish();
                finish();
            }
        });
    }

    public static Drawable getAppIcon(String pkgName){
        Drawable icon;
        Context context = MApp.getApp().getApplicationContext();
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(pkgName, 0);
            icon = pm.getApplicationIcon(pi.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            icon = pm.getDefaultActivityIcon();
        }
        return icon;
    }

    public static void startAppCloneActivity(Activity activity, AppModel appModel) {
        Intent intent = new Intent(activity, AppCloneActivity.class);
        intent.putExtra(AppConstants.EXTRA_APP_MODEL, appModel);
        activity.startActivityForResult(intent, AppConstants.REQUEST_INSTALL_APP);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_install_layout);
        initData();
        initView();

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

        ScaleAnimation clonedMarkScaleUp = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        clonedMarkScaleUp.setDuration(ANIMATION_STEP);
        clonedMarkScaleUp.setFillAfter(true);
        clonedMarkScaleUp.setFillBefore(true);
        TranslateAnimation startBtnMoveUp = new TranslateAnimation(0, 0, DisplayUtils.dip2px(this, 70), 0);
        startBtnMoveUp.setDuration(ANIMATION_STEP);
        startBtnMoveUp.setFillAfter(true);
        startBtnMoveUp.setFillBefore(true);
        final AnimationSet clonedMarkSet = new AnimationSet(true);
        clonedMarkSet.addAnimation(clonedMarkScaleUp);
        clonedMarkSet.addAnimation(successBgFadeIn);
        final AnimationSet startBtnSet = new AnimationSet(true);
        startBtnSet.addAnimation(startBtnMoveUp);
        startBtnSet.addAnimation(successBgFadeIn);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppManager.launchApp(mPkgName);
                finish();
            }
        });
        mImgSuccessBg.setVisibility(View.VISIBLE);
        appModel.setCustomIcon(BitmapUtils.createCustomIcon(AppCloneActivity.this, appModel.getIcon() ));
        mImgAppIcon.setBackground(null);
        mImgAppIcon.setImageBitmap(appModel.getCustomIcon());
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mImgAppIcon, "scaleX", 0.7f, 1.3f, 1.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mImgAppIcon, "scaleY", 0.7f, 1.3f, 1.1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY);
        animSet.setInterpolator(new BounceInterpolator());
        animSet.setDuration(800).start();
        mProgressBar.startAnimation(progressFadeOut);
        mImgSuccessBg.startAnimation(successBgFadeIn);
        installingFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTxtInstalled.startAnimation(successBgFadeIn);

                mBtnStart.startAnimation(startBtnSet);
                mBtnStart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mLayoutCancel.startAnimation(installingFadeOut);
        mTxtInstalling.startAnimation(installingFadeOut);
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
