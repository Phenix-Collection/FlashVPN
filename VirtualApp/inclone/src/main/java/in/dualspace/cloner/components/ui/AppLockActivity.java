package in.dualspace.cloner.components.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import in.dualspace.cloner.DualApp;
import in.dualspace.cloner.R;
import in.dualspace.cloner.clone.CloneManager;
import com.polestar.clone.CustomizeAppData;
import in.dualspace.cloner.components.AppMonitorService;
import in.dualspace.cloner.utils.DisplayUtils;
import in.dualspace.cloner.utils.MLogs;
import in.dualspace.cloner.utils.PreferencesUtils;
import in.dualspace.cloner.utils.RemoteConfig;
import in.dualspace.cloner.widget.locker.AppLockPasswordLogic;
import in.dualspace.cloner.widget.locker.BlurBackground;
import in.dualspace.cloner.widget.locker.LockIconImageView;

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
    private TextView mForgotPasswordTv;
    private LockIconImageView mCenterIcon;
    private LinearLayout mAdInfoContainer;
    private ImageView mToolbarIcon;
    private TextView mToolbarText;

    private TextView mCenterAppText;

    private AppLockPasswordLogic mAppLockPasswordLogic = null;

    private final static String EXTRA_USER_ID = "extra_clone_userid";
    private final static String EXTRA_TITLE = "extra_lock_title";
    private final static String EXTRA_CANCELABLE = "extra_lock_can_cancel";
    private final static String EXTRA_SHOW_AD = "extra_lock_show_ad";
    public final static String CONFIG_SLOT_APP_LOCK_PROTECT_TIME = "slot_app_lock_protect_time";
    public final static String CONFIG_SLOT_APP_LOCK = "slot_app_lock";

    private boolean showAd;
    private String title;
    private boolean cancelOnBackPressed;
    private Drawable icon;

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

    public static final void start(Activity activity,  String pkg, int userId, String title, boolean showAd, boolean cancelOnBackPressed) {
        MLogs.d("ApplockActivity start " + pkg + " userId " + userId);
        if (pkg == null) {
            return;
        }
        Intent intent = new Intent(activity, AppLockActivity.class);
        intent.putExtra(Intent.EXTRA_PACKAGE_NAME, pkg);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_CANCELABLE, cancelOnBackPressed);
        intent.putExtra(EXTRA_SHOW_AD, showAd);
        intent.putExtra(EXTRA_TITLE, title);
        intent.setFlags(FLAG_ACTIVITY_SINGLE_TOP|FLAG_ACTIVITY_NO_HISTORY
                |FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS|FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }


    public static AdSize getBannerSize() {
        int dpWidth = DisplayUtils.px2dip(VirtualCore.get().getContext(), DisplayUtils.getScreenWidth(VirtualCore.get().getContext()));
        dpWidth = Math.max(280, dpWidth*9/10);
        return  new AdSize(dpWidth, 250);
    }

    private void updateTitleBar() {
        mToolbarText.setVisibility(View.VISIBLE);
        mToolbarIcon.setVisibility(View.VISIBLE);
        mToolbarIcon.setImageDrawable(mCenterIcon.getDrawable());
        mToolbarIcon.setBackground(null);
        mToolbarText.setText(title);
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
        final FuseAdLoader adLoader = FuseAdLoader.get(CONFIG_SLOT_APP_LOCK, DualApp.getApp());
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
        if (!cancelOnBackPressed) {
            mBlurBackground.onIncorrectPassword(mAdInfoContainer);
        } else {
            finish();
        }
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
        if (showAd && !PreferencesUtils.isAdFree()) {
            loadNative();
        }
    }

    private void initData() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        mPkgName = getIntent().getStringExtra(Intent.EXTRA_PACKAGE_NAME);
        mUserId = getIntent().getIntExtra(EXTRA_USER_ID, 0);
        cancelOnBackPressed = getIntent().getBooleanExtra(EXTRA_CANCELABLE, false);
        showAd = getIntent().getBooleanExtra(EXTRA_SHOW_AD, true);
        title = getIntent().getStringExtra(EXTRA_TITLE);
        if (mPkgName.equals(getPackageName())) {
            icon = getApplicationInfo().loadIcon(getPackageManager());
            if (TextUtils.isEmpty(title)) {
                title = getString(R.string.unlock_main_app);
            }
        } else {
            CustomizeAppData data = CustomizeAppData.loadFromPref(mPkgName, mUserId);
            icon = new BitmapDrawable(data.getCustomIcon());
            if (TextUtils.isEmpty(title)){
                if (!data.customized) {
                    title = CloneManager.getInstance(DualApp.getApp()).getModelName(mPkgName, mUserId);
                } else {
                    title = data.label;
                }
            }
        }
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
        mToolbarText.setText(title);
        if (mPkgName.equals(getPackageName())) {
            mToolbarIcon.setVisibility(View.INVISIBLE);
            mToolbarText.setVisibility(View.INVISIBLE);
        }

        mAdInfoContainer = (LinearLayout)findViewById(R.id.layout_appinfo_container);

        mCenterIcon = (LockIconImageView) findViewById(R.id.window_applock_icon);
        mCenterAppText = (TextView) findViewById(R.id.window_applock_name);
        mCenterAppText.setText(title);
        mCenterIcon.setImageDrawable(icon);

        mForgotPasswordTv = (TextView)findViewById(R.id.forgot_password_tv);
        mForgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });
        MLogs.d("AppLockWindow initialized");

        mBlurBackground.init();
        if (! mPkgName.equals(getPackageName())) {
            mBlurBackground.reloadWithTheme(mPkgName, icon);
        }
        mAppLockPasswordLogic.onShow();
    }

    private void forgotPassword() {
        if (PreferencesUtils.isSafeQuestionSet(VirtualCore.get().getContext())) {
            Intent intent = new Intent(VirtualCore.get().getContext(), LockSecureQuestionActivity.class);
            intent.putExtra(LockSecureQuestionActivity.EXTRA_IS_SETTING, false);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            DualApp.getApp().getApplicationContext().startActivity(intent);
        }
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
        finish();
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }
}
