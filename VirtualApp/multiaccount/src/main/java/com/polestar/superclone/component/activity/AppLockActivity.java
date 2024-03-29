package com.polestar.superclone.component.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdSize;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.superclone.MApp;
import com.polestar.superclone.R;
import com.polestar.superclone.component.AppMonitorService;
import com.polestar.superclone.component.BaseActivity;
import com.polestar.clone.CustomizeAppData;
import com.polestar.superclone.utils.AppManager;
import com.polestar.clone.BitmapUtils;
import com.polestar.superclone.utils.DisplayUtils;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.PreferencesUtils;
import com.polestar.superclone.utils.RemoteConfig;
import com.polestar.superclone.utils.ToastUtils;
import com.polestar.superclone.widgets.FeedbackImageView;
import com.polestar.superclone.widgets.locker.AppLockPasswordLogic;
import com.polestar.superclone.widgets.locker.BlurBackground;

import java.util.List;


import static android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NO_HISTORY;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

/**
 * Created by guojia on 2017/12/19.
 */

public class AppLockActivity extends BaseActivity {
    private BlurBackground mBlurBackground;

    private String mPkgName;
    private int mUserId;
    private Handler mHandler;
    private LinearLayout mAdInfoContainer;
    private ImageView mToolbarIcon;
    private TextView mToolbarText;

    private FeedbackImageView mCenterIcon;
    private TextView mCenterAppText;


    private AppLockPasswordLogic mAppLockPasswordLogic = null;

    public final static String EXTRA_USER_ID = "extra_clone_userid";
    public final static String CONFIG_SLOT_APP_LOCK_PROTECT_TIME = "slot_app_lock_protect_time";
    public final static String CONFIG_SLOT_APP_LOCK = "slot_app_lock";

    private FingerprintManager fingerprintManager;
    private CancellationSignal cancellationSignal;

    private boolean shownAd;
    public static long AD_SHOW_TIME;


    public static final void start(Context context, String pkg, int userId) {
        MLogs.d("ApplockActivity start " + pkg + " userId " + userId);
        if (pkg == null) {
            return;
        }
        Intent intent = new Intent(context, AppLockActivity.class);
        intent.putExtra(Intent.EXTRA_PACKAGE_NAME, pkg);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.setFlags(FLAG_ACTIVITY_SINGLE_TOP|FLAG_ACTIVITY_NO_HISTORY
                |FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS|FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    public static AdSize getBannerSize() {
        return AdSize.MEDIUM_RECTANGLE;
    }
    private void inflatNativeAd(IAdAdapter ad) {
        final AdViewBinder viewBinder =  new AdViewBinder.Builder(R.layout.lock_window_native_ad)
                .titleId(R.id.ad_title)
                .textId(R.id.ad_subtitle_text)
                .mainMediaId(R.id.ad_cover_image)
                .fbMediaId(R.id.ad_fb_mediaview)
                .admMediaId(R.id.ad_adm_mediaview)
                .iconImageId(R.id.ad_icon_image)
                .callToActionId(R.id.ad_cta_text)
                .privacyInformationId(R.id.ad_choices_image)
                .adFlagId(R.id.ad_flag)
                .build();
        View adView = ad.getAdView(this,viewBinder);
        if (adView != null) {
            adView.setBackgroundColor(0);
            ViewGroup parentViewGroup = (ViewGroup) adView.getParent();
            if (parentViewGroup != null ) {
                parentViewGroup.removeView(adView);
            }
            mAdInfoContainer.removeAllViews();
            mAdInfoContainer.addView(adView);
            shownAd = true;
            updateTitleBar();
            EventReporter.generalEvent("app_lock_load_ad_show");
        }
    }
    private void updateTitleBar() {
        mToolbarIcon.setImageDrawable(mCenterIcon.getDrawable());
        mToolbarIcon.setBackground(null);
        mToolbarText.setText(mCenterAppText.getText());
    }
    private void loadNative(){
        final FuseAdLoader adLoader = FuseAdLoader.get(CONFIG_SLOT_APP_LOCK, this);
        adLoader.setBannerAdSize(getBannerSize());
//        adLoader.addAdConfig(new AdConfig(AdConstants.AdType.AD_SOURCE_FACEBOOK, "1713507248906238_1787756514814644", -1));
//        adLoader.addAdConfig(new AdConfig(AdConstants.AdType.AD_SOURCE_MOPUB, "ea31e844abf44e3690e934daad125451", -1));
        if (adLoader != null) {
            EventReporter.generalEvent("app_lock_load_ad");
            adLoader.loadAd(this, 2, RemoteConfig.getLong(CONFIG_SLOT_APP_LOCK_PROTECT_TIME), new IAdLoadListener() {
                @Override
                public void onAdClicked(IAdAdapter ad) {

                }

                @Override
                public void onRewarded(IAdAdapter ad) {

                }

                @Override
                public void onAdClosed(IAdAdapter ad) {

                }

                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    MLogs.d("Applock native ad loaded. showing ");
                        inflatNativeAd(ad);
                        //loadAdmobNativeExpress();
                        adLoader.preloadAd(AppLockActivity.this);

                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {
                    EventReporter.generalEvent("app_lock_load_ad_" + error);
                    MLogs.d("Lock window load ad error: " + error);
                }
            });
        }
    }
    @Override
    public void onBackPressed() {
        mBlurBackground.onIncorrectPassword(mAdInfoContainer);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventReporter.generalEvent("app_lock_create");
    }



    @Override
    public void onResume() {
        super.onResume();
        initData();
        initView();
        if (!PreferencesUtils.isAdFree()) {
            loadNative();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
            cancellationSignal = new CancellationSignal();
            if (PreferencesUtils.isFingerprintEnable()
                    && fingerprintManager!= null &&
                    fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
                fingerprintManager.authenticate(null, cancellationSignal, 0,
                        new FingerprintManager.AuthenticationCallback() {
                            @Override
                            public void onAuthenticationError(int errorCode, CharSequence errString) {
                                if (errorCode != FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                                    ToastUtils.ToastDefult(AppLockActivity.this, "" + errString);
                                }
                            }

                            @Override
                            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                                ToastUtils.ToastDefult(AppLockActivity.this, ""+helpString);
                            }

                            @Override
                            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                }, 200);
                                AppMonitorService.unlocked(mPkgName, mUserId);
                            }

                            @Override
                            public void onAuthenticationFailed() {
                                mBlurBackground.onIncorrectPassword(mAdInfoContainer);
                            }
                        }, null);
            }
        }
    }

    private void initData() {
        shownAd = false;
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        mPkgName = getIntent().getStringExtra(Intent.EXTRA_PACKAGE_NAME);
        mUserId = getIntent().getIntExtra(EXTRA_USER_ID, 0);
    }

    private void initToolbar() {
    }

    private void initView() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.applock_window_layout);

        MLogs.d("AppLockActiviey initialized for " + mPkgName);
        mBlurBackground = (BlurBackground)findViewById(R.id.applock_window);
        mAppLockPasswordLogic = new AppLockPasswordLogic(mBlurBackground, new AppLockPasswordLogic.EventListener() {
            @Override
            public void onCorrectPassword() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 200);
                AppMonitorService.unlocked(mPkgName, mUserId);
            }

            @Override
            public void onIncorrectPassword() {
                mBlurBackground.onIncorrectPassword(mAdInfoContainer);
            }

            @Override
            public void onCancel() {
                mBlurBackground.onIncorrectPassword(mAdInfoContainer);
            }
        });
        mAppLockPasswordLogic.onFinishInflate();

        mToolbarIcon = (ImageView) findViewById(R.id.lock_bar_icon);
        mToolbarText = (TextView) findViewById(R.id.lock_bar_text);

        initToolbar();
        mAdInfoContainer = (LinearLayout)findViewById(R.id.layout_appinfo_container);

        mCenterIcon = (FeedbackImageView) findViewById(R.id.window_applock_icon);
        mCenterAppText = (TextView) findViewById(R.id.window_applock_name);
        CustomizeAppData data = CustomizeAppData.loadFromPref(mPkgName, mUserId);
        if (TextUtils.isEmpty(data.label)) {
            data.label = AppManager.getModelName(mPkgName, mUserId);
        }
        Bitmap icon = BitmapUtils.getCustomIcon(MApp.getApp(), mPkgName, mUserId);
        if (icon != null) {
            mCenterIcon.setImageBitmap( icon);
        }
        if (data.label != null) {
            mCenterAppText.setText(data.label);
        }

        mBlurBackground.init();
        mBlurBackground.reloadWithTheme(mPkgName, mUserId);
        mAppLockPasswordLogic.onShow();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initData();
        initView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && cancellationSignal != null && fingerprintManager != null) {
            cancellationSignal.cancel();
        }
        if (shownAd) {
            AD_SHOW_TIME = System.currentTimeMillis();
        }
        finish();
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }
}
