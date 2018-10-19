package com.polestar.booster.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdSize;
import com.polestar.booster.BoosterLog;
import com.polestar.booster.BoosterSdk;
import com.polestar.booster.R;
import com.polestar.booster.mgr.BoostMgr;
import com.polestar.booster.util.AndroidUtil;
import com.polestar.booster.util.BoostUtil;
import com.polestar.booster.util.FontUtil;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class BoostView extends RelativeLayout implements WindowView, View.OnClickListener {


    private final Context mContext;
    private final boolean mShortcut;
    private final String mSlotId;
    private final String mBackgroundFilePath;
    private final boolean mShowTopAnimation;

    private RelativeLayout mRoot;

    private RelativeLayout mLayoutCleanerView;

    private View mBackground;

    private ViewGroup mLayoutTitle;
    private ImageView mTitleIcon;
    private TextView mTitleText;
    private ImageView mBtnSettings;
    private ImageView mBtnClose;

    private ViewGroup mLayoutClickArea;

    private ViewGroup mLayoutAnimationTop;
    private NebulaAnimationLayout mNebulaAnimationLayout;
    private ImageView mWheelInside;
    private ImageView mWheelOutside;

    private ViewGroup mLayoutRelease;
    private TextView mTextReleaseMemoryNumber;

    private ViewGroup mLayoutAvailable;
    private TextView mTextAvailableMemory;
    private TextView mTextPercent;
    private TextView mTextAvailable;

    private TextView mTextKillPackageName;

    private ViewGroup mLayoutCleanerViewAd;
    private ViewGroup mLayoutCleanerViewAds;
    private long mAttachWindowSession = 0L;
    private CleanerViewListener mListener;
    private IAdAdapter loadedAd;
    private FuseAdLoader adLoader;
    private int cleanPercentage;

    public BoostView(Context context, boolean shortcut, String slotId, CleanerViewListener listener) {
        super(context);
        mContext = context;
        mShortcut = shortcut;

        mSlotId = slotId;
        mListener = listener;
        mShowTopAnimation = true;//mShortcut ? ConfigUtil.showAnimationTopOnCleanShortcut(mConfigInfo) : ConfigUtil.showAnimationTopOnClean(mConfigInfo);
//        String backgroundUrl = this.mShortcut ? ConfigUtil.getCleanShortcutBackgroundUrl(this.mConfigInfo) : ConfigUtil.getCleanBackgroundUrl(this.mConfigInfo);
//        File bgFile = Booster.getCachedFile(context, backgroundUrl);
        this.mBackgroundFilePath = null;

        this.mAttachWindowSession = System.currentTimeMillis();

        initView(context);
    }

    private void inflateNativeAd(IAdAdapter ad) {
        final AdViewBinder viewBinder;
        switch (ad.getAdType()) {
//            case AdConstants.NativeAdType.AD_SOURCE_FACEBOOK:
//                viewBinder = new AdViewBinder.Builder(R.layout.booster_native_ad_fb)
//                        .titleId(R.id.ad_title)
//                        .textId(R.id.ad_subtitle_text)
//                        .mainMediaId(R.id.ad_cover_image)
//                        .callToActionId(R.id.ad_cta_text)
//                        .privacyInformationId(R.id.ad_choices_container)
//                        .build();
//                break;
            default:
                viewBinder = new AdViewBinder.Builder(R.layout.booster_native_ad)
                        .titleId(R.id.ad_title)
                        .textId(R.id.ad_subtitle_text)
                        .mainMediaId(R.id.ad_cover_image)
                        .fbMediaId(R.id.ad_fb_mediaview)
                        .admMediaId(R.id.ad_adm_mediaview)
                       //.iconImageId(R.id.ad_icon_image)
                        .callToActionId(R.id.ad_cta_text)
                        .privacyInformationId(R.id.ad_choices_container)
                        .build();
                break;
        }
        View adView = ad.getAdView(viewBinder);
        if (adView != null) {
            adView.setBackgroundColor(0);
            mLayoutCleanerViewAd.addView(adView);
        }
    }

    public static int px2dip(Context context, float px) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int getScreenWidth(Context context){
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;
        return width;
    }

    public AdSize getBannerSize() {
        int dpWidth = px2dip(mContext, getScreenWidth(mContext));
        dpWidth = Math.max(320, dpWidth*9/10);
        return new AdSize(dpWidth, 300);
    }

//    public static AdSize getBannerSize() {
//        return  new AdSize(320, 280);
//    }

    public void loadAd() {
        final String slotId = mSlotId;
        if (TextUtils.isEmpty(slotId)) {
            return;
        }
        loadedAd = null;
        final long begin = System.currentTimeMillis();
        //TODO JJJJ
        adLoader = FuseAdLoader.get(mSlotId, mContext.getApplicationContext());
        adLoader.setBannerAdSize(getBannerSize());
        adLoader.loadAd(2, 2000, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAdAdapter ad) {
                loadedAd = ad;
            }

            @Override
            public void onAdListLoaded(List<IAdAdapter> ads) {

            }

            @Override
            public void onAdClicked(IAdAdapter ad) {
                
            }

            @Override
            public void onAdClosed(IAdAdapter ad) {

            }

            @Override
            public void onError(String error) {

            }
        });
    }

    public void loadAds() {
        final String slotId = BoosterSdk.boosterConfig.boostAdSlot;
        final int adCount = 1;
        if (TextUtils.isEmpty(slotId) || adCount <= 0) {
            return;
        }

        for (int i = 0; i < adCount; i++) {
            final ViewGroup layoutAd = (ViewGroup) mLayoutCleanerViewAds.getChildAt(i);
            final long begin = System.currentTimeMillis();
            //TODO JJJJ
//            Ad ad = new Ad.Builder(mContext, slotId)
//                    .setParentViewGroup(layoutAd)
//                    .setWidth(340).setHight(260)
//                    .isPreLoad(false)
//                    .setTransparent(true)
//                    .setTitleColor(R.color.cleanersdk_ad_title_color)
//                    .setSubTitleColor(R.color.cleanersdk_ad_subtitle_color)
//                    .setCtaBackground(R.color.cleanersdk_available_memory_color)
//                    .setCtaTextColor(R.color.cleanersdk_ad_button_color)
//                    .build();
            //TODO JJJJ
//            AdAgent.getInstance().loadAd(mContext, ad, new OnAdLoadListener() {
//                @Override
//                public void onLoad(IAd iAd) {
//                    if (log.isDebugEnabled())
//                        log.debug("loadAds onLoad" + " used:" + (System.currentTimeMillis() - begin) + "ms");
//                    addAdView(slotId, iAd);
//                }
//
//                @Override
//                public void onLoadFailed(AdError adError) {
//                    if (log.isDebugEnabled())
//                        log.debug("loadAds onLoadFailed" + " used:" + (System.currentTimeMillis() - begin) + "ms");
//                }
//
//                @Override
//                public void onLoadInterstitialAd(WrapInterstitialAd wrapInterstitialAd) {
//                    if (log.isDebugEnabled())
//                        log.debug("loadAds onLoadInterstitialAd" + " used:" + (System.currentTimeMillis() - begin) + "ms");
//                    Analytics.onAdLoadInterstitialLoaded(slotId, configInfo);
//                }
//            });
        }
    }

    private static int getMarginBottom(View view) {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    public static void verticalShowFromBottom(View view){
        int translationY = 500 + getMarginBottom(view);
        BoosterLog.log("translationY  " + translationY + " height " + view.getHeight());
        ViewHelper.setTranslationY(view, translationY);
        ViewPropertyAnimator.animate(view)
                .setDuration(400)
                .translationY(0);
    }

    private void initView(Context context) {
        mRoot = new RelativeLayout(context);
        addView(mRoot, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mLayoutCleanerView = (RelativeLayout) View.inflate(context, R.layout.booster_view, null);

        mBackground = mLayoutCleanerView.findViewById(R.id.cleanersdk_bg);
//        Bitmap localBackground = ImageUtil.decodeFile(this.mBackgroundFilePath);
//        if (localBackground != null) {
//            this.mBackground.setBackgroundDrawable(new BitmapDrawable(this.mContext.getResources(), localBackground));
//        }

        mLayoutTitle = (ViewGroup) mLayoutCleanerView.findViewById(R.id.cleanersdk_layout_title);
        mTitleIcon = (ImageView) mLayoutCleanerView.findViewById(R.id.boostersdk_icon);
        mTitleText = (TextView) mLayoutCleanerView.findViewById(R.id.boostersdk_title);
        mTitleIcon.setVisibility(View.VISIBLE );
        mTitleIcon.setImageResource(AndroidUtil.getAppIconResId(mContext));
        mTitleText.setVisibility(View.VISIBLE );
        mTitleText.setText(BoosterSdk.boosterRes.titleString);

        mBtnSettings = (ImageView) mLayoutCleanerView.findViewById(R.id.boostersdk_btn_settings);
        mBtnSettings.setOnClickListener(this);
        mBtnClose = (ImageView) mLayoutCleanerView.findViewById(R.id.boostersdk_btn_close);
        mBtnClose.setOnClickListener(this);

        mLayoutClickArea = (ViewGroup) mLayoutCleanerView.findViewById(R.id.boostersdk_layout_click_area);
        mLayoutClickArea.setOnClickListener(this);

        mLayoutAnimationTop = (ViewGroup) mLayoutCleanerView.findViewById(R.id.boostersdk_layout_wheel);
        mNebulaAnimationLayout = (NebulaAnimationLayout) mLayoutCleanerView.findViewById(R.id.boostersdk_nebula_animation_layout);
        mWheelInside = (ImageView) mLayoutCleanerView.findViewById(R.id.boostersdk_wheel_inside);
        mWheelInside.setImageResource(BoosterSdk.boosterRes.innerWheelImage);
        mWheelOutside = (ImageView) mLayoutCleanerView.findViewById(R.id.boostersdk_wheel_outside);
        mWheelOutside.setImageResource(BoosterSdk.boosterRes.outterWheelImage);

        if (!mShowTopAnimation)
            mLayoutAnimationTop.setVisibility(View.INVISIBLE);

        mLayoutAvailable = (ViewGroup) mLayoutCleanerView.findViewById(R.id.layout_available);
        mTextAvailableMemory = (TextView) mLayoutCleanerView.findViewById(R.id.boostersdk_txt_available_memory);
        mTextPercent = (TextView) mLayoutCleanerView.findViewById(R.id.available_percentage_txt);
        mTextAvailable = (TextView) mLayoutCleanerView.findViewById(R.id.boostersdk_txt_available);
        mTextAvailableMemory.setText(String.valueOf(AndroidUtil.getAvailMemory(mContext)) + "M");

        mLayoutRelease = (ViewGroup) mLayoutCleanerView.findViewById(R.id.cleanersdk_layout_release);
        mTextReleaseMemoryNumber = (TextView) mLayoutCleanerView.findViewById(R.id.cleanersdk_txt_release_memory_number);
        mTextReleaseMemoryNumber.setText("0M");

        mTextKillPackageName = (TextView) mLayoutCleanerView.findViewById(R.id.cleanersdk_txt_kill_package_name);

        mLayoutCleanerViewAd = (ViewGroup) mLayoutCleanerView.findViewById(R.id.cleanersdk_layout_cleaner_view_ad);
        mLayoutCleanerViewAd.setVisibility(INVISIBLE);
       // TouchCollector.collectTouch(mSlotId, mLayoutCleanerViewAd);

        mLayoutCleanerViewAds = (ViewGroup) mLayoutCleanerView.findViewById(R.id.cleanersdk_layout_cleaner_view_ads);
       // for (int i = 0; i < ConfigUtil.getFakeAdCount(mConfigInfo); i++) {
        for (int i = 0; i < 1; i++) {
            View.inflate(context, R.layout.booster_view_ad, mLayoutCleanerViewAds);
        }
        //if (!ConfigUtil.isFakeAdClickable(mConfigInfo))
        if (!false)
            TouchBlockableFrameLayout.blockTouch(mLayoutCleanerViewAds);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        p.gravity = Gravity.CENTER_HORIZONTAL;
        mRoot.addView(mLayoutCleanerView, p);

        initTypeface();

        BoostUtil.CleanStatus status = BoostUtil.CleanStatus.create(mContext, AndroidUtil.getTotalMemory(mContext), "");
        updateCleanStatus(status, status);
    }

    private void initTypeface() {
        Typeface thinFont = FontUtil.getThinFont();
        Typeface lightFont = FontUtil.getLightFont();
        mTextAvailableMemory.setTypeface(thinFont);
        mTextPercent.setTypeface(thinFont);
        mTextAvailable.setTypeface(lightFont);
    }

//    private void addAdView(final String slotId, IAd ad) {
//        if (ad == null) {
//            log.warn("addAdView" + " ad:" + ad);
//            return;
//        }
//
//        mAdLoaded = true;
//

        //TODO JJJJ
//        Analytics.onAdAdded(slotId, configInfo);
//        ad.setOnAdClickListener(new OnAdClickListener() {
//            @Override
//            public void onAdClicked() {
//                if (log.isDebugEnabled())
//                    log.debug("addAdView" + " onAdClicked");
//                Analytics.onAdClicked(slotId, configInfo);
//                closeByAnimation();
//            }
//        });
//        ad.setOnCancelAdListener(new OnCancelAdListener() {
//            @Override
//            public void cancelAd() {
//                if (log.isDebugEnabled())
//                    log.debug("addAdView" + " cancelAd");
//                Analytics.onAdCancel(slotId, configInfo);
//                closeByAnimation();
//            }
//        });
//        ad.setOnPrivacyIconClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (log.isDebugEnabled())
//                    log.debug("addAdView" + " onPrivacyIconClick");
//                Analytics.onAdPrivacyIconClicked(slotId, configInfo);
//                closeByAnimation();
//            }
//        });
//    }

    @Override
    public WindowManager.LayoutParams createLayoutParams() {
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            lp.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        lp.gravity = Gravity.TOP;
        lp.width = dm.widthPixels;
        lp.height = dm.heightPixels;
        lp.x = 0;
        lp.y = 0;
        return lp;
    }

    @Override
    public void closeImmediate() {
        setVisibility(GONE);
        removeCallbacks(this.mAutoCloseJob);
        try
        {
            if ((getParent() == null) || ( this.mListener == null)) {
                return;
            }
            this.mListener.closeViewCallback();
        }
        catch (Exception e) {
        }

    }

    static Animator createActionAnimator(final Runnable action) {
        ValueAnimator animator = ValueAnimator.ofInt(0).setDuration(0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (action != null) action.run();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });
        return animator;
    }

    AnimatorSet mCloseAnimator;

    private void closeByAnimation() {
        if (mCloseAnimator != null && mCloseAnimator.isRunning())
            return;

        mCloseAnimator = new AnimatorSet();
        mCloseAnimator.playSequentially(createCloseAnimator(), createActionAnimator(new Runnable() {
            @Override
            public void run() {
                closeImmediate();
            }
        }));
        mCloseAnimator.start();
    }

    private AnimatorSet createInitAnimator() {
        // init scale and alpha
        mBackground.setAlpha(0);
        mWheelOutside.setScaleX(0);
        mWheelOutside.setScaleY(0);
        mWheelInside.setScaleX(0);
        mWheelInside.setScaleY(0);
        mLayoutAvailable.setAlpha(0);
        mLayoutRelease.setAlpha(0);

        ValueAnimator bgAlphaAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(300);
        bgAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                mBackground.setAlpha(alpha);
            }
        });

        ValueAnimator scaleAnimator = new ValueAnimator().ofFloat(0f, 1f).setDuration(300);
        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                mWheelInside.setScaleX(scale);
                mWheelInside.setScaleY(scale);
                mWheelOutside.setScaleX(scale);
                mWheelOutside.setScaleY(scale);
            }
        });

        ValueAnimator transAnimator = ValueAnimator.ofFloat(mLayoutCleanerViewAd.getHeight(), 0).setDuration(300);
        transAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLayoutCleanerViewAd.setTranslationY((Float) animation.getAnimatedValue());
                mLayoutCleanerViewAds.setTranslationY((Float) animation.getAnimatedValue());
            }
        });
        AnimatorSet stage1 = new AnimatorSet();
        stage1.playTogether(bgAlphaAnimator, scaleAnimator, transAnimator);

        ValueAnimator availAlphaAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(300);
        availAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                mLayoutAvailable.setAlpha(alpha);
                mLayoutRelease.setAlpha(alpha);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(stage1, availAlphaAnimator, createActionAnimator(new Runnable() {
            @Override
            public void run() {
                runCleanTask();
            }
        }), createStartAnimator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                onCleanStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }
        });

        return animatorSet;
    }

    private void onCleanStart() {
//        int btnCloseStrategy = ConfigUtil.getBtnCloseStrategy(mConfigInfo);
//        int areaCloseStrategy = ConfigUtil.getAreaCloseStrategy(mConfigInfo);

        mBtnClose.setVisibility(View.INVISIBLE);
        mLayoutClickArea.setClickable( false);
    }

    private void onCleanEnd() {

        BoosterLog.log("onCleanEnd");
        mBtnClose.setVisibility(View.VISIBLE);
        mLayoutClickArea.setClickable(true);
        mTextPercent.setVisibility(VISIBLE);
        mTextPercent.setText(mContext.getString(R.string.booster_release)+String.valueOf(cleanPercentage)
                + "%"+mContext.getString(R.string.booster_ram));
        mLayoutRelease.setVisibility(GONE);
        ObjectAnimator localObjectAnimator = ObjectAnimator.ofFloat(mTextPercent, "translationY", new float[] { -120.0F, 0.0F });
        localObjectAnimator.setDuration(800L);
        AnimatorSet localAnimatorSet = new AnimatorSet();
        localAnimatorSet.play(localObjectAnimator);
        if(loadedAd != null) {
            inflateNativeAd(loadedAd);
            mLayoutCleanerViewAd.setVisibility(VISIBLE);
            verticalShowFromBottom(mLayoutCleanerViewAd);
        }
        BoosterSdk.checkCreateCleanShortcut();
    }

    private AnimatorSet createStartAnimator() {
        if (!mShowTopAnimation)
            return new AnimatorSet();

        ObjectAnimator rotation1 = ObjectAnimator.ofFloat(mWheelOutside, "rotation", 0, -360).setDuration(800);
        ObjectAnimator rotation2 = ObjectAnimator.ofFloat(mWheelOutside, "rotation", 0, -360 * 6).setDuration(3000);
        ObjectAnimator rotation3 = ObjectAnimator.ofFloat(mWheelOutside, "rotation", 0, -15, 0).setDuration(600);
        AnimatorSet rotations = new AnimatorSet();
        rotations.playSequentially(rotation1, rotation2, rotation3);

        ObjectAnimator translation4 = ObjectAnimator.ofFloat(mWheelInside, "translationX", 0, 0, 10, -10, 0, 0, 0, 0, 0, 10, -10, 0).setDuration(800);
        ObjectAnimator translation5 = ObjectAnimator.ofFloat(mWheelInside, "translationY", 0, 10, -10, 0, 0, 0, 0, 0, 10, -10, 0, 0).setDuration(800);
        ObjectAnimator translation6 = ObjectAnimator.ofFloat(mWheelOutside, "translationX", 0, 0, 10, -10, 0, 0, 0, 0, 0, 10, -10, 0).setDuration(800);
        ObjectAnimator translation7 = ObjectAnimator.ofFloat(mWheelOutside, "translationY", 0, 10, -10, 0, 0, 0, 0, 0, 10, -10, 0, 0).setDuration(800);
        AnimatorSet translations = new AnimatorSet();
        translations.setStartDelay(1000);
        translations.playTogether(translation4, translation5, translation6, translation7);

        final ValueAnimator nebulaAnimator = mNebulaAnimationLayout.createNebulaAnimator(800);

        AnimatorSet startAnimator = new AnimatorSet();
        startAnimator.playTogether(rotations, translations, nebulaAnimator);
        rotations.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                nebulaAnimator.cancel();
                onCleanEnd();
            }
        });
        startAnimator.setInterpolator(new LinearInterpolator());
        return startAnimator;
    }

    private AnimatorSet createCloseAnimator() {
        ValueAnimator bgAlphaAnimator = ValueAnimator.ofFloat(1f, 0f).setDuration(300);
        bgAlphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                mBackground.setAlpha(alpha);
            }
        });

        ValueAnimator scaleAnimator = new ValueAnimator().ofFloat(1f, 0f).setDuration(300);
        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                mWheelInside.setScaleX(scale);
                mWheelInside.setScaleY(scale);
                mWheelOutside.setScaleX(scale);
                mWheelOutside.setScaleY(scale);
                mNebulaAnimationLayout.setScaleX(scale);
                mNebulaAnimationLayout.setScaleY(scale);
            }
        });

        ObjectAnimator adTransAnimator = ObjectAnimator.ofFloat(mLayoutCleanerViewAd, "translationY", 0, mLayoutCleanerViewAd.getHeight()).setDuration(300);
        ObjectAnimator adsTransAnimator = ObjectAnimator.ofFloat(mLayoutCleanerViewAds, "translationY", 0, mLayoutCleanerViewAd.getHeight()).setDuration(300);

        AnimatorSet stage1 = new AnimatorSet();
        stage1.playTogether(bgAlphaAnimator, scaleAnimator, adTransAnimator, adsTransAnimator);

        ValueAnimator alphaAnimator = ValueAnimator.ofFloat(1f, 0f).setDuration(300);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                mLayoutAvailable.setAlpha(alpha);
                mLayoutRelease.setAlpha(alpha);
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(stage1, alphaAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mBtnClose.setClickable(false);
                mLayoutClickArea.setClickable(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });
        return animatorSet;
    }

    final Runnable mAutoCloseJob = new Runnable() {
        @Override
        public void run() {
            closeByAnimation();
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        createInitAnimator().start();

        //Analytics.onAutoCleanAttachWindow(mConfig != null ? AdAgent.getInstance().isHavaADCache(mSlotId) : false, mConfigInfo);

        final long autoCloseTime = BoosterSdk.boosterConfig.autoDismissTime;
        if (autoCloseTime <= 0)
            return;
        postDelayed(mAutoCloseJob, autoCloseTime);

        loadAds();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        closeImmediate();

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.boostersdk_btn_settings) {
            closeImmediate();
            BoosterSdk.showSettings(mContext);
            return;
        }

        if (view.getId() == R.id.boostersdk_btn_close) {
            closeByAnimation();
            return;
        }

        if (view.getId() == R.id.boostersdk_layout_click_area) {
            closeByAnimation();
            return;
        }
    }

    void updateCleanStatus(BoostUtil.CleanStatus initCleanStatus, BoostUtil.CleanStatus cleanStatus) {
        if (initCleanStatus == null || cleanStatus == null)
            return;

        long released = (cleanStatus.availMemory - initCleanStatus.availMemory);
        cleanPercentage = (int)(released * 100 / cleanStatus.totalMemory);
        mTextReleaseMemoryNumber.setText((released / 1024/1024) + "M");

        boolean hasPackageName = cleanStatus != null && !TextUtils.isEmpty(cleanStatus.packageName);
        if (hasPackageName) {
            if (mTextKillPackageName.getVisibility() != View.VISIBLE)
                mTextKillPackageName.setVisibility(View.VISIBLE);
            mTextKillPackageName.setText("Free memory: " + cleanStatus.packageName);
        } else {
            if (mTextKillPackageName.getVisibility() != View.INVISIBLE)
                mTextKillPackageName.setVisibility(View.INVISIBLE);
        }
    }

    static final Random sCleanResultStrategyRandom = new Random();

    static List<BoostUtil.CleanStatus> applyCleanResultStrategy(List<BoostUtil.CleanStatus> cleanStatuses, long lastCleanTime) {
        if (cleanStatuses == null || cleanStatuses.size() < 2) {
            return cleanStatuses;
        }

        long current = System.currentTimeMillis();
        long diff = (current - lastCleanTime);
        boolean outInterval = diff > 60*1000;
        int threshold = 0;
        int min = 10;
        int max = 60;
        if (outInterval) {
            min = 20;
            max = 60;
            threshold = 20;
        } else {
            min = 5;
            max = 15;
            threshold = 5;
        }

        BoostUtil.CleanStatus start = cleanStatuses.get(0);
        BoostUtil.CleanStatus end = cleanStatuses.get(cleanStatuses.size() - 1);
        int startPercentage = (int) (start.availMemory * 100 / end.totalMemory);
        int endPercentage = (int) (end.availMemory * 100 / end.totalMemory);
        int diffPercentage = endPercentage - startPercentage;
        if (diffPercentage > threshold)
            return cleanStatuses;

        int newEndPercentage = startPercentage + sCleanResultStrategyRandom.nextInt(max - min) + min;
        long newEndAvailMemory = end.totalMemory * newEndPercentage / 100;
        BoostUtil.CleanStatus newEnd = new BoostUtil.CleanStatus(end.totalMemory, newEndAvailMemory, end.packageName);
        List<BoostUtil.CleanStatus> newCleanStatuses = new ArrayList<BoostUtil.CleanStatus>(cleanStatuses.size());
        newCleanStatuses.add(start);
        long newAvailMemory = start.availMemory;
        for (int i = 1; i < cleanStatuses.size() - 1; i++) {
            BoostUtil.CleanStatus oldCleanStatus = cleanStatuses.get(i);
            newAvailMemory += (newEnd.availMemory - start.availMemory) / (cleanStatuses.size() - 2);
            newCleanStatuses.add(new BoostUtil.CleanStatus(oldCleanStatus.totalMemory, newAvailMemory, oldCleanStatus.packageName));
        }
        newCleanStatuses.add(newEnd);
        return newCleanStatuses;
    }

    private void runCleanTask() {

        new AsyncTask<String, BoostUtil.CleanStatus, List<BoostUtil.CleanStatus>>() {

            BoostUtil.CleanStatus mInitCleanStatus;

            @Override
            protected List<BoostUtil.CleanStatus> doInBackground(String... params) {
                mInitCleanStatus = BoostUtil.CleanStatus.create(mContext, AndroidUtil.getTotalMemory(mContext), "");
                final List<BoostUtil.CleanStatus> cleanStatuses = new ArrayList<BoostUtil.CleanStatus>();
                cleanStatuses.add(mInitCleanStatus);
                BoostUtil.clean(mContext, new BoostUtil.CleanStatusListener() {
                    @Override
                    public void onCleanStatus(final BoostUtil.CleanStatus cleanStatus) {
                        cleanStatuses.add(cleanStatus);
                    }
                });
                List<BoostUtil.CleanStatus> appliedCleanStatuses = applyCleanResultStrategy(cleanStatuses, BoostMgr.getLastTimeDoClean(mContext));
                BoostMgr.setLastTimeDoClean(mContext, System.currentTimeMillis());
                for (BoostUtil.CleanStatus cleanStatus : appliedCleanStatuses) {
                    publishProgress(cleanStatus);
                    try {
                        Thread.sleep(100L);
                    } catch (Exception e) {
                    }
                }
                BoosterLog.log("finish clean");
                return appliedCleanStatuses;
            }

            @Override
            protected void onProgressUpdate(BoostUtil.CleanStatus... values) {
                if (values == null || values.length <= 0)
                    return;

                updateCleanStatus(mInitCleanStatus, values[0]);
            }

            @Override
            protected void onPostExecute(List<BoostUtil.CleanStatus> cleanStatuses) {
                if (cleanStatuses == null || cleanStatuses.size() <= 0)
                    return;

                updateCleanStatus(mInitCleanStatus, cleanStatuses.get(cleanStatuses.size() - 1));
                BoosterLog.log("finish clean 2");
            }
        }.executeOnExecutor(Executors.newSingleThreadExecutor());
    }

    public static abstract interface CleanerViewListener {
        public abstract void closeViewCallback();
    }
}
