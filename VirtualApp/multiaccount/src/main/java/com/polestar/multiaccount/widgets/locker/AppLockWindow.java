package com.polestar.multiaccount.widgets.locker;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.lody.virtual.client.core.VirtualCore;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAd;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.imageloader.widget.BasicLazyLoadImageView;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.AppLockMonitor;
import com.polestar.multiaccount.component.activity.LockSecureQuestionActivity;
import com.polestar.multiaccount.utils.BitmapUtils;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.MTAManager;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.polestar.multiaccount.utils.ResourcesUtil;
import com.polestar.multiaccount.widgets.FeedbackImageView;
import com.polestar.multiaccount.widgets.FloatWindow;
import com.polestar.multiaccount.widgets.PopupMenu;
import com.polestar.multiaccount.widgets.StarLevelLayoutView;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by guojia on 2017/1/3.
 */

public class AppLockWindow implements PopupMenu.OnMenuItemSelectedListener {

    private PopupMenu mPopupMenu;

    private String mPkgName;
    private Handler mHandler;
    private FloatWindow mWindow;
    private View mContentView;
    private TextView mForgotPasswordTv;
    private NativeExpressAdView mAdmobExpressView;
    private LinearLayout mAdInfoContainer;
    private ImageView mToolbarIcon;
    private TextView mToolbarText;

    private FeedbackImageView mCenterIcon;
    private TextView mCenterAppText;

    private boolean mIsShowing;

    private AppLockPasswordLogic mAppLockPasswordLogic = null;

    public final static String CONFIG_SLOT_APP_LOCK = "slot_app_lock";

    public AppLockWindow(String pkgName, Handler handler) {
        MLogs.d("AppLockWindow initialize for : " + pkgName);
        mPkgName = pkgName;
        mHandler = handler;

        mWindow = new FloatWindow(MApp.getApp());

        mContentView = LayoutInflater.from(MApp.getApp()).inflate(R.layout.applock_window_layout, null);

        mWindow.setContentView(mContentView);
        mWindow.setOnBackPressedListener(new FloatWindow.OnBackPressedListener() {
            @Override
            public void onBackPressed() {
                MLogs.d("AppLockWindow onBackPressed");
            }
        });

        mAppLockPasswordLogic = new AppLockPasswordLogic(mContentView, new AppLockPasswordLogic.EventListener() {
            @Override
            public void onCorrectPassword() {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AppLockWindow window = AppLockWindowManager.getInstance().get(mPkgName);
                        if (window != null && window.isShowing()) {
                            window.dismiss();
                        }
                    }
                }, 500);
                mHandler.sendMessage(mHandler.obtainMessage(AppLockMonitor.MSG_PACKAGE_UNLOCKED, mPkgName));
            }

            @Override
            public void onIncorrectPassword() {
            }

            @Override
            public void onCancel() {
            }
        });
        mAppLockPasswordLogic.onFinishInflate();

        mToolbarIcon = (ImageView) mContentView.findViewById(R.id.lock_bar_icon);
        mToolbarText = (TextView) mContentView.findViewById(R.id.lock_bar_text);

        initToolbar();
        initAdmobBannerView();
  //      loadAdmobNativeExpress();
        MLogs.d("AppLockWindow initialized 0");
        mAdInfoContainer = (LinearLayout)mContentView.findViewById(R.id.layout_appinfo_container);

        mCenterIcon = (FeedbackImageView) mContentView.findViewById(R.id.window_applock_icon);
        mCenterAppText = (TextView) mContentView.findViewById(R.id.window_applock_name);
        PackageManager pm = MApp.getApp().getPackageManager();
        ApplicationInfo ai = null;
        try {
            ai = pm.getApplicationInfo(mPkgName, 0);
        }catch (Exception e) {
            MLogs.logBug(MLogs.getStackTraceString(e));
        }
        if ( ai != null) {
            Drawable drawable = pm.getApplicationIcon(ai);
            if (drawable != null) {
                mCenterIcon.setImageBitmap( BitmapUtils.createCustomIcon(MApp.getApp(), drawable));
            }
            CharSequence title = pm.getApplicationLabel(ai);
            if (title != null) {
                mCenterAppText.setText(String.format(ResourcesUtil.getString(R.string.applock_window_title),title));
            }
        }
        MLogs.d("AppLockWindow initialized 1");
        mForgotPasswordTv = (TextView)mContentView.findViewById(R.id.forgot_password_tv);
        mForgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });
        MLogs.d("AppLockWindow initialized");
    }

    private void initAdmobBannerView() {
        mAdmobExpressView = new NativeExpressAdView(VirtualCore.get().getContext());
        List<AdConfig> adConfigs = RemoteConfig.getAdConfigList(CONFIG_SLOT_APP_LOCK);
        String adunit  = null;
        if (adConfigs != null) {
            for (AdConfig adConfig: adConfigs) {
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
        mAdmobExpressView.setAdSize(new AdSize(360, 280));
        mAdmobExpressView.setAdUnitId("ca-app-pub-5490912237269284/7955343852");
        //mAdmobExpressView.setAdUnitId("ca-app-pub-5490912237269284/7540311850");
       // mAdmobExpressView.setVisibility(View.GONE);
        mAdmobExpressView.setBackgroundColor(0);
        mAdmobExpressView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                AdLog.d("onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                AdLog.d("onAdFailedToLoad " + i);
                mAdmobExpressView.setVisibility(View.GONE);
                AppLockMonitor.getInstance().getAdLoader().loadAd(1, null);
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
                mAdInfoContainer.removeAllViews();
                mAdInfoContainer.addView(mAdmobExpressView);
                mAdmobExpressView.setVisibility(View.VISIBLE);
                updateTitleBar();
                AppLockMonitor.getInstance().getAdLoader().loadAd(1, null);
                AdLog.d("LockWindow on Banner AdLoaded ");
            }
        });
        mAdmobExpressView.setBackgroundColor(0);
    }

    private void updateTitleBar() {
        mToolbarIcon.setImageDrawable(mCenterIcon.getDrawable());
        mToolbarIcon.setBackground(null);
        mToolbarText.setText(mCenterAppText.getText());
    }

    private void inflatNativeAd(IAd ad) {
        View adView = LayoutInflater.from(mContentView.getContext()).inflate(R.layout.lock_window_native_ad, null);
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
            StarLevelLayoutView starLevelLayout = (StarLevelLayoutView)adView.findViewById(R.id.star_rating_layout);
            starLevelLayout.setRating((int)ad.getStarRating());

            mAdInfoContainer.removeAllViews();
            mAdInfoContainer.addView(adView);
            ad.registerViewForInteraction(mAdInfoContainer);
            if (ad.getPrivacyIconUrl() != null) {
                BasicLazyLoadImageView choiceIconImage = (BasicLazyLoadImageView) adView.findViewById(R.id.ad_choices_image);
                choiceIconImage.setDefaultResource(0);
                choiceIconImage.requestDisplayURL(ad.getPrivacyIconUrl());
                ad.registerPrivacyIconView(choiceIconImage);
            }
            updateTitleBar();
        }
    }
    private void loadNative(){
        final FuseAdLoader adLoader = AppLockMonitor.getInstance().getAdLoader();
        if (adLoader != null) {
            adLoader.loadAd(1, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAd ad) {
                    inflatNativeAd(ad);
                    adLoader.loadAd(1, null);
                }

                @Override
                public void onAdListLoaded(List<IAd> ads) {

                }

                @Override
                public void onError(String error) {
                    MLogs.d("Lock window load ad error: " + error);
                    loadAdmobNativeExpress();
                    adLoader.loadAd(1, null);
                }
            });
        }
    }

    private void loadAdmobNativeExpress(){
        if (mAdmobExpressView == null) {
            return;
        }
        MLogs.d("Lock loadAdmobNativeExpress");
        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID( VirtualCore.get().getContext());
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            boolean isTestDevice = request.isTestDevice( VirtualCore.get().getContext());
            AdLog.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
            AdLog.d( "Admob unit id "+ mAdmobExpressView.getAdUnitId());
            mAdmobExpressView.loadAd(request );
        } else {
            mAdmobExpressView.loadAd(new AdRequest.Builder().build());
        }
        MLogs.d("X Lock loadAdmobNativeExpress");
    }

    private void initToolbar() {
        if (mContentView == null) return;

        View menuLayout = LayoutInflater.from(MApp.getApp()).inflate(R.layout.menu_applock_toolbar, null);
        menuLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mPopupMenu = new PopupMenu((ViewGroup)menuLayout);
        mPopupMenu.setOnMenuItemSelectedListener(this);

        final float offsetX = DisplayUtils.dip2px(MApp.getApp(),3);
        final float offsetY = DisplayUtils.dip2px(MApp.getApp(),-10);
        final float menuWidth = menuLayout.getMeasuredWidth();
        final View menu = mContentView.findViewById(R.id.cmx_toolbar_applock_menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPopupMenu.isShowing()) {
                    mPopupMenu.dismiss();
                } else {
                    mPopupMenu.show(menu, (int) (menu.getWidth() - offsetX - menuWidth), (int) offsetY);
                }
            }
        });
    }

    public void show() {
        if (!mIsShowing) {
            MLogs.d("LockWindow show");
            mAppLockPasswordLogic.onShow();
            mWindow.show();
            MTAManager.showLockWindow(mContentView.getContext(), mPkgName);
            loadNative();
            //loadAdmobNativeExpress();
            mIsShowing = true;
        }
    }

    public void dismiss() {
        if (mIsShowing && mPopupMenu != null && mWindow != null) {
            mAppLockPasswordLogic.onBeforeHide();
            mPopupMenu.dismiss();
            mWindow.hide();
            mIsShowing = false;
        }
    }

    private void forgotPassword() {
        if (PreferencesUtils.isSafeQuestionSet(VirtualCore.get().getContext())) {
            Intent intent = new Intent(VirtualCore.get().getContext(), LockSecureQuestionActivity.class);
            intent.putExtra(LockSecureQuestionActivity.EXTRA_IS_SETTING, false);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            MApp.getApp().getApplicationContext().startActivity(intent);
        }
    }

    public boolean isShowing() {
        return mIsShowing;
    }

    @Override
    public void onMenuItemSelected(View menuItem) {

    }
}