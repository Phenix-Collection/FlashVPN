package com.polestar.domultiple.components.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.utils.MLogs;

/**
 * Created by guojia on 2017/7/16.
 */

/**
 * Created by guojia on 2016/12/5.
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


    public static void startAppStartActivity(Activity activity, String packageName, boolean firstStart) {
        if (CloneManager.needUpgrade(packageName)) {
            CloneManager.killApp(packageName);
        } else {
            if (CloneManager.isAppLaunched(packageName)) {
                CloneManager.launchApp(packageName);
                return;
            }
        }
        Intent intent = new Intent(activity, AppLoadingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra(AppConstants.EXTRA_CLONED_APP_PACKAGENAME, packageName);
        intent.putExtra(AppConstants.EXTRA_FROM, AppConstants.VALUE_FROM_HOME);
        intent.putExtra(EXTRA_FIRST_START, firstStart);

        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

    }

    private void initData() {

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
        } else {
            boolean isInstalled = CloneManager.isAppInstalled(appModel.getPackageName());
            needDoUpGrade = CloneManager.needUpgrade(appModel.getPackageName());
            int delay = 500;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Todo: if app is already launched, just switch it to front, no need re-launch
                    if (!isInstalled) {
                        //CloneManager.getInstance(AppLoadingActivity.this).createClone(AppLoadingActivity.this, appModel);
                        MLogs.logBug("App model is not installed " + appModel.getPackageName());
                    }
                    if (needDoUpGrade) {
                        CloneManager.upgradeApp(appModel.getPackageName());
                    }
                    CloneManager.launchApp(appModel.getPackageName());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 5000);
//                            finish();
                }
            }, delay);
        }
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

            mImgAppIcon.setImageBitmap(appModel.getCustomIcon());
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mImgAppIcon, "scaleX", 0.8f, 1.15f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mImgAppIcon, "scaleY", 0.8f, 1.15f, 1.0f);
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
            if (firstStart){
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

