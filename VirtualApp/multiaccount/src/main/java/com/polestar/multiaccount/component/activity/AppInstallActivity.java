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
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.utils.BitmapUtils;
import com.polestar.multiaccount.utils.LocalAdUtils;
import com.polestar.multiaccount.widgets.CustomDotView;
import com.polestar.multiaccount.widgets.GifView;

/**
 * Created by yxx on 2016/8/1.
 */
public class AppInstallActivity extends BaseActivity {

    private static final int UPDATE_PROGRESS_AND_DOTVIEW = 0x101;
    private static final int SHOW_CLONED_ICON = 0x102;
    private static final int FINISH_PROGRESS_AND_EXIT = 0x103;
    private static final int SHOW_DOT_VIEW_IN_CYCLE = 0x104;
    private static final int PAUSE_DOT_VIEW = 0x105;
    private ProgressBar progress;
    private ImageView orgIcon;
    private CustomDotView dotOne;
    private CustomDotView dotTwo;
    private GifView pbLogo;
    private ImageView clonedIcon;
    private TextView progressTv;
    private AppModel appModel;
    private boolean installSuccess;
    private static Bitmap bgBitmap;
    private Bitmap localBgBitmap;
    private View mainLayout;
    private FrameLayout adContent;
    private MyHandler mHandler = new MyHandler();
    private boolean finishInstallPro = false;
    private boolean isDotPaused = false;
    private boolean isCanceled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        initData();
        initView();
    }

    public static void startAppInstallActivity(Activity activity, AppModel appModel, Bitmap bgBitmap) {
        AppInstallActivity.bgBitmap = bgBitmap;
        Intent intent = new Intent(activity, AppInstallActivity.class);
        intent.putExtra(Constants.EXTRA_APP_MODEL, appModel);
        activity.startActivityForResult(intent, Constants.REQUEST_INSTALL_APP);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            appModel = intent.getParcelableExtra(Constants.EXTRA_APP_MODEL);
        }
        if (appModel == null) {
            Intent intentFail = new Intent();
            intentFail.putExtra(Constants.EXTRA_IS_INSTALL_SUCCESS, installSuccess);
            intentFail.putExtra(Constants.EXTRA_APP_MODEL, appModel);
            setResult(RESULT_OK, intentFail);
            finish();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!AppManager.isAppInstalled(appModel.getPackageName())) {
                                installSuccess = AppManager.installApp(AppInstallActivity.this, appModel);
                            } else {
                                installSuccess = true;
                            }
                            if(isCanceled){
                                AppManager.uninstallApp(appModel.getPackageName());
                                return;
                            }
                            if (!installSuccess){
                                AppManager.uninstallApp(appModel.getPackageName());
                            }
                            Intent intent = new Intent();
                            intent.putExtra(Constants.EXTRA_IS_INSTALL_SUCCESS, installSuccess);
                            intent.putExtra(Constants.EXTRA_APP_MODEL, appModel);
                            setResult(RESULT_OK, intent);
                            finishInstallPro = true;
                            mHandler.sendEmptyMessage(FINISH_PROGRESS_AND_EXIT);
                        }
                    }, 0);
                    Looper.loop();
                }
            }).start();
        }
    }

    private void initView() {
        progress = (ProgressBar) findViewById(R.id.install_progress);
        dotOne = (CustomDotView) findViewById(R.id.dotview_one);
        dotTwo = (CustomDotView) findViewById(R.id.dotview_two);
        orgIcon = (ImageView) findViewById(R.id.app_icon);
        pbLogo = (GifView) findViewById(R.id.pb_logo);
        clonedIcon = (ImageView) findViewById(R.id.cloned_app_icon);
        progressTv = (TextView) findViewById(R.id.tv_progress);
        mainLayout = findViewById(R.id.install_layout);
        adContent = (FrameLayout) findViewById(R.id.ad_content);
        if (bgBitmap != null) {
            localBgBitmap = bgBitmap;
            bgBitmap = null;
            if (Build.VERSION.SDK_INT < 16) {
                mainLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), localBgBitmap));
            } else {
                mainLayout.setBackground(new BitmapDrawable(getResources(), localBgBitmap));
            }
        }
        if (appModel == null) {
            return;
        }
        if (appModel.getCustomIcon() == null) {
            appModel.setCustomIcon(BitmapUtils.createCustomIcon(this, appModel.initDrawable(this)));
        }
        if (appModel.getIcon() == null) {
            appModel.setIcon(appModel.initDrawable(this));
        }
        clonedIcon.setImageBitmap(appModel.getCustomIcon());
        clonedIcon.setVisibility(View.INVISIBLE);
        orgIcon.setImageDrawable(appModel.getIcon());
        pbLogo.setGifResource(R.mipmap.logo);
        pbLogo.setVisibility(View.INVISIBLE);

        LocalAdUtils.showAd(this, adContent, LocalAdUtils.AD_STYLE[2], true);

        dotOne.addListener(new CustomDotView.OnceAnimatorListener() {
            @Override
            public void onAnimationEnd() {
                dotTwo.playOnce();
            }
        });

        // org Icon scale
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(orgIcon, "scaleX", 1.0f, 1.7f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(orgIcon, "scaleY", 1.0f, 1.7f, 1.0f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY);
        animSet.setInterpolator(new BounceInterpolator());
        animSet.setDuration(800).start();

        // progressbar translateY
        ObjectAnimator.ofFloat(progress, "translationY", -progress.getMinimumHeight(), 0f).setDuration(800).start();
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // show dot view in cycle
                        mHandler.sendEmptyMessage(SHOW_DOT_VIEW_IN_CYCLE);

                        // show logo gif with alpha from 0 to 1
                        pbLogo.setVisibility(View.VISIBLE);
                        pbLogo.play();
                        ObjectAnimator pblogoAlpha = ObjectAnimator.ofFloat(pbLogo, "alpha", 0f, 1f);
                        pblogoAlpha.setDuration(500).start();
                        pblogoAlpha.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                // show progressbar + cloned progress
                                mHandler.sendEmptyMessage(UPDATE_PROGRESS_AND_DOTVIEW);
                                // show cloned icon with alpha from 0 -> 1 -> 0.2 -> 1
                                mHandler.sendEmptyMessage(SHOW_CLONED_ICON);
                            }
                        });
                    }
                }, 0);
            }
        });

    }

    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS_AND_DOTVIEW:
                    if (progress.getProgress() == 99) {
                        if (finishInstallPro) {
                            mHandler.sendEmptyMessage(FINISH_PROGRESS_AND_EXIT);
                            break;
                        } else {
                            break;
                        }
                    }
                    progress.incrementProgressBy(1);
                    progressTv.setText("Cloning..." + progress.getProgress() + "%");
                    if (progress.getProgress() < progress.getMax()) {
                        mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS_AND_DOTVIEW, 1);
                    }
                    break;
                case SHOW_CLONED_ICON:
                    clonedIcon.setVisibility(View.VISIBLE);
                    ObjectAnimator cIconAlpha = ObjectAnimator.ofFloat(clonedIcon, "alpha", 0f, 1f, 0.2f, 1f);
                    cIconAlpha.setDuration(2000).start();
                    break;
                case SHOW_DOT_VIEW_IN_CYCLE:
                    if (!isDotPaused) {
                        dotOne.setDuration(300);
                        dotTwo.setDuration(300);
                        dotTwo.pause();
                        dotOne.playOnce();
                        mHandler.sendEmptyMessageDelayed(SHOW_DOT_VIEW_IN_CYCLE, 1200);
                    }
                    break;
                case PAUSE_DOT_VIEW:
                    isDotPaused = true;
                    break;
                case FINISH_PROGRESS_AND_EXIT:
                    if (progress.getProgress() == 99 && finishInstallPro) {
                        progress.incrementProgressBy(1);
                        progressTv.setText("Cloning..." + progress.getProgress() + "%");
                        mHandler.sendEmptyMessage(PAUSE_DOT_VIEW);
                        ObjectAnimator.ofFloat(clonedIcon, "alpha", 0f, 1f, 0f, 1f, 0f, 1f).setDuration(200).start();
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 200);
                    }
                    break;
            }
        }
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
