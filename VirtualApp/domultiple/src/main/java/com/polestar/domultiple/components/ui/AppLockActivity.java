package com.polestar.domultiple.components.ui;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.polestar.clone.client.core.VirtualCore;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.clone.CustomizeAppData;
import com.polestar.domultiple.components.AppMonitorService;
import com.polestar.domultiple.utils.DisplayUtils;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;
import com.polestar.domultiple.widget.locker.AppLockPasswordLogic;
import com.polestar.domultiple.widget.locker.BlurBackground;
import com.polestar.domultiple.widget.locker.LockIconImageView;

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
    private LockIconImageView mCenterIcon;
    private LinearLayout mAdInfoContainer;
    private ImageView mToolbarIcon;
    private TextView mToolbarText;

    private TextView mCenterAppText;

    private AppLockPasswordLogic mAppLockPasswordLogic = null;

    public final static String EXTRA_USER_ID = "extra_clone_userid";
    public final static String CONFIG_SLOT_APP_LOCK_PROTECT_TIME = "slot_app_lock_protect_time";
    public final static String CONFIG_SLOT_APP_LOCK = "slot_app_lock";

    private FingerprintManager fingerprintManager;
    private CancellationSignal cancellationSignal;

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
        int dpWidth = DisplayUtils.px2dip(VirtualCore.get().getContext(), DisplayUtils.getScreenWidth(VirtualCore.get().getContext()));
        dpWidth = Math.max(280, dpWidth*9/10);
        return  new AdSize(dpWidth, 250);
    }

    private void updateTitleBar() {
        mToolbarIcon.setImageDrawable(mCenterIcon.getDrawable());
        mToolbarIcon.setBackground(null);
        mToolbarText.setText(mCenterAppText.getText());
    }

    private void inflatNativeAd(IAdAdapter ad) {
        final AdViewBinder viewBinder;
        switch (ad.getAdType()) {
            default:
                viewBinder =  new AdViewBinder.Builder(R.layout.lock_window_native_ad)
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
            ViewGroup parentViewGroup = (ViewGroup) adView.getParent();
            if (parentViewGroup != null ) {
                parentViewGroup.removeView(adView);
            }
            mAdInfoContainer.removeAllViews();
            mAdInfoContainer.addView(adView);
            updateTitleBar();
        }
    }
    private void loadNative(){
        final FuseAdLoader adLoader = FuseAdLoader.get(CONFIG_SLOT_APP_LOCK, PolestarApp.getApp());
        adLoader.setBannerAdSize(getBannerSize());
//        adLoader.addAdConfig(new AdConfig(AdConstants.AdType.AD_SOURCE_FACEBOOK, "1713507248906238_1787756514814644", -1));
//        adLoader.addAdConfig(new AdConfig(AdConstants.AdType.AD_SOURCE_MOPUB, "ea31e844abf44e3690e934daad125451", -1));
        if (adLoader != null) {
            adLoader.loadAd(this, 2, RemoteConfig.getLong(CONFIG_SLOT_APP_LOCK_PROTECT_TIME), new IAdLoadListener() {
                @Override
                public void onRewarded(IAdAdapter ad) {

                }

                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    MLogs.d("Applock native ad loaded. showing ");
                        inflatNativeAd(ad);
                        //loadAdmobNativeExpress();
                        adLoader.preloadAd(AppLockActivity.this);

                }

                @Override
                public void onAdClicked(IAdAdapter ad) {

                }

                @Override
                public void onAdClosed(IAdAdapter ad) {

                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {
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


    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
        initView();
        if (!PreferencesUtils.isAdFree()) {
            loadNative();
        }
        ImageView fingerprintIcon = (ImageView) findViewById(R.id.fingerprint_icon);
        fingerprintIcon.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);
            cancellationSignal = new CancellationSignal();
            if (PreferencesUtils.isFingerprintEnable()
                    && fingerprintManager!= null &&
                    fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
                fingerprintIcon.setVisibility(View.VISIBLE);
                fingerprintManager.authenticate(null, cancellationSignal, 0,
                        new FingerprintManager.AuthenticationCallback() {
                            @Override
                            public void onAuthenticationError(int errorCode, CharSequence errString) {
                                if (errorCode != FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                                    Toast.makeText(AppLockActivity.this, "" + errString, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                                Toast.makeText(AppLockActivity.this, "" + helpString, Toast.LENGTH_SHORT).show();
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
        MLogs.d("AppLockWindow initialized 0");
        mAdInfoContainer = (LinearLayout)findViewById(R.id.layout_appinfo_container);

        mCenterIcon = (LockIconImageView) findViewById(R.id.window_applock_icon);
        mCenterAppText = (TextView) findViewById(R.id.window_applock_name);
        CustomizeAppData data = CustomizeAppData.loadFromPref(mPkgName, mUserId);
        mCenterIcon.setImageBitmap(data.getCustomIcon());
        if (!data.customized) {
            mCenterAppText.setText(
                    CloneManager.getInstance(PolestarApp.getApp()).getModelName(mPkgName, mUserId));
        } else {
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
        finish();
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }
}
