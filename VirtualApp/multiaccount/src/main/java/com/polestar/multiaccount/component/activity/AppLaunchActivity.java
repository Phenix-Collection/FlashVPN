package com.polestar.multiaccount.component.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.constant.Constants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.EventReportManager;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.utils.BitmapUtils;
import com.polestar.multiaccount.utils.LocalAdUtils;
import com.polestar.multiaccount.utils.Logs;
import com.polestar.multiaccount.utils.MTAManager;
import com.polestar.multiaccount.utils.ToastUtils;
import com.polestar.multiaccount.widgets.GifView;

import java.util.List;

/**
 * Created by yxx on 2016/8/1.
 */
public class AppLaunchActivity extends BaseActivity {

    private static final int SHOW_PROGRESS = 0x101;
    private ProgressBar progress;
    private ImageView appIcon;
    private GifView gear;
    private TextView progressTv;
    private AppModel appModel;
    private String from;
    private static Bitmap bgBitmap;
    private View mainLayout;
    private FrameLayout adContent;
    private Bitmap localBgBitmap;
    private MyHandler mHandler = new MyHandler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        initData();
        initView();
    }

    public static void startAppLaunchActivity(Activity activity, String packageName,Bitmap bgBitmap) {
        AppLaunchActivity.bgBitmap = bgBitmap;
        Intent intent = new Intent(activity, AppLaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra(Constants.EXTRA_CLONED_APP_PACKAGENAME, packageName);
        intent.putExtra(Constants.EXTRA_FROM, Constants.VALUE_FROM_HOME);

        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            String packageName = intent.getStringExtra(Constants.EXTRA_CLONED_APP_PACKAGENAME);
            from = intent.getStringExtra(Constants.EXTRA_FROM);

            List<AppModel> appModels = DbManager.queryAppModelByPackageName(this, packageName);
            if (appModels != null && appModels.size() > 0) {
                appModel = appModels.get(0);
            }
        }
        if (appModel == null) {
            ToastUtils.ToastDefult(this, getString(R.string.toast_shortcut_invalid));
            finish();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MTAManager.launchApp(AppLaunchActivity.this, appModel.getPackageName(), from);
                            EventReportManager.launchApp(AppLaunchActivity.this, appModel.getPackageName(), from);
                            // Todo: if app is already launched, just switch it to front, no need re-launch
                            AppManager.launchApp(appModel.getPackageName());
//                            finish();
                        }
                    }, 1000);
                    Looper.loop();
                }
            }).start();
        }
    }

    private void initView() {
        progress = (ProgressBar) findViewById(R.id.launch_progress);
        appIcon = (ImageView) findViewById(R.id.app_icon);
        gear = (GifView) findViewById(R.id.gear);
        progressTv = (TextView) findViewById(R.id.tv_progress);
        mainLayout = findViewById(R.id.main_layout);
        adContent = (FrameLayout) findViewById(R.id.ad_content);

        if (appModel == null) {
            return;
        }
        if (appModel.getCustomIcon() == null) {
            appModel.setCustomIcon(BitmapUtils.createCustomIcon(this, appModel.initDrawable(this)));
        }
        if (bgBitmap != null) {
            localBgBitmap = bgBitmap;
            bgBitmap = null;
            if (Build.VERSION.SDK_INT < 16) {
                mainLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), localBgBitmap));
            } else {
                mainLayout.setBackground(new BitmapDrawable(getResources(), localBgBitmap));
            }
        }
        if (appModel.getCustomIcon() != null) {
            appIcon.setImageBitmap(appModel.getCustomIcon());
        } else {
            appIcon.setImageDrawable(appModel.initDrawable(this));
        }

        gear.setGifResource(R.mipmap.gear);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(appIcon, "scaleX", 1.0f, 1.7f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(appIcon, "scaleY", 1.0f, 1.7f, 1.0f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY);
        animSet.setInterpolator(new BounceInterpolator());
        animSet.setDuration(800).start();
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                gear.play();
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
            }
        });

        LocalAdUtils.showAd(this, adContent, LocalAdUtils.AD_STYLE[2], true);
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS:
                    if (progress.getProgress() < progress.getMax()) {
                        progress.incrementProgressBy(1);
                        progressTv.setText("Launching..." + progress.getProgress() + "%");
                        mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish(); // make it not resumeable if user press back while launching third apps.
    }

//    @Override
//    public void onBackPressed() {
//        // NOT allow user pause launching operation
//    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }

}
