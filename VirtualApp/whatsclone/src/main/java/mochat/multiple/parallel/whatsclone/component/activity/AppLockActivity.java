package mochat.multiple.parallel.whatsclone.component.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdSize;
import com.lody.virtual.client.core.VirtualCore;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import mochat.multiple.parallel.whatsclone.MApp;
import mochat.multiple.parallel.whatsclone.R;
import mochat.multiple.parallel.whatsclone.component.AppLockMonitor;
import mochat.multiple.parallel.whatsclone.component.BaseActivity;
import mochat.multiple.parallel.whatsclone.model.CustomizeAppData;
import mochat.multiple.parallel.whatsclone.utils.AppManager;
import mochat.multiple.parallel.whatsclone.utils.BitmapUtils;
import mochat.multiple.parallel.whatsclone.utils.DisplayUtils;
import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.PreferencesUtils;
import mochat.multiple.parallel.whatsclone.utils.RemoteConfig;
import mochat.multiple.parallel.whatsclone.widgets.FeedbackImageView;
import mochat.multiple.parallel.whatsclone.widgets.locker.AppLockPasswordLogic;
import mochat.multiple.parallel.whatsclone.widgets.locker.BlurBackground;

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
    private LinearLayout mAdInfoContainer;
    private ImageView mToolbarIcon;
    private TextView mToolbarText;

    private FeedbackImageView mCenterIcon;
    private TextView mCenterAppText;


    private AppLockPasswordLogic mAppLockPasswordLogic = null;

    public final static String EXTRA_USER_ID = "extra_clone_userid";
    public final static String CONFIG_SLOT_APP_LOCK_PROTECT_TIME = "slot_app_lock_protect_time";
    public final static String CONFIG_SLOT_APP_LOCK = "slot_app_lock";

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
        return  new AdSize(dpWidth, 280);
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
                .build();
        View adView = ad.getAdView(viewBinder);
        if (adView != null) {
            adView.setBackgroundColor(0);
            mAdInfoContainer.removeAllViews();
            mAdInfoContainer.addView(adView);
            updateTitleBar();
        }
    }
    private void updateTitleBar() {
        mToolbarIcon.setImageDrawable(mCenterIcon.getDrawable());
        mToolbarIcon.setBackground(null);
        mToolbarText.setText(mCenterAppText.getText());
    }
    private void loadNative(){
        final FuseAdLoader adLoader = AppLockMonitor.getInstance().getAdLoader();
        adLoader.setBannerAdSize(getBannerSize());
//        adLoader.addAdConfig(new AdConfig(AdConstants.NativeAdType.AD_SOURCE_FACEBOOK, "1713507248906238_1787756514814644", -1));
//        adLoader.addAdConfig(new AdConfig(AdConstants.NativeAdType.AD_SOURCE_MOPUB, "ea31e844abf44e3690e934daad125451", -1));
        if (adLoader != null) {
            adLoader.loadAd(2, RemoteConfig.getLong(CONFIG_SLOT_APP_LOCK_PROTECT_TIME), new IAdLoadListener() {
                @Override
                public void onRewarded(IAdAdapter ad) {

                }

                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    MLogs.d("Applock native ad loaded. showing ");
                        inflatNativeAd(ad);
                        //loadAdmobNativeExpress();
                        adLoader.loadAd(1, null);

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
                AppLockMonitor.getInstance().unlocked(mPkgName, mUserId);
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
        mForgotPasswordTv = (TextView)findViewById(R.id.forgot_password_tv);
        mForgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });

        mBlurBackground.init();
        mBlurBackground.reloadWithTheme(mPkgName, mUserId);
        mAppLockPasswordLogic.onShow();
    }

    private void forgotPassword() {
        if (PreferencesUtils.isSafeQuestionSet(VirtualCore.get().getContext())) {
            Intent intent = new Intent(VirtualCore.get().getContext(), LockSecureQuestionActivity.class);
            intent.putExtra(LockSecureQuestionActivity.EXTRA_IS_SETTING, false);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            MApp.getApp().getApplicationContext().startActivity(intent);
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
