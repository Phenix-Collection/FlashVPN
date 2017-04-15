package com.polestar.multiaccount.component.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.Campaign;
import com.mobvista.msdk.out.Frame;
import com.mobvista.msdk.out.MobVistaSDKFactory;
import com.mobvista.msdk.out.MvNativeHandler;
import com.mobvista.msdk.out.MvNativeHandler.NativeAdListener;
import com.mobvista.msdk.out.MvNativeHandler.Template;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAd;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.imageloader.widget.BasicLazyLoadImageView;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.polestar.multiaccount.widgets.StarLevelLayoutView;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Native display interstitial
 *
 * @author
 *
 */
public class NativeInterstitialActivity extends Activity {

    private static final String TAG = NativeInterstitialActivity.class.getName();
    private static final String UNIT_ID = "8998";
    public int BIG_IMG_REQUEST_AD_NUM = 1;
    private BasicLazyLoadImageView mIvIcon;
    private BasicLazyLoadImageView mIvImage;
    private BasicLazyLoadImageView mChoiceImage;
    private TextView mTvAppName;
    private TextView mTvAppDesc;
    private TextView mTvCta;
    private MvNativeHandler nativeHandle;
    private RelativeLayout mRlClose;
    private StarLevelLayoutView mStarLayout;
    private ProgressBar mProgressBar;
    private LinearLayout mLl_Root;
    private FuseAdLoader mFuseLoader;
    private NativeExpressAdView mAdmobExpressView;

    private static final String CONFIG_SLOT_HOME_LUCKY = "slot_home_lucky";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobvista_native_interstitial);
        initView();
        showLoadding();
        setlistener();
        //mvLoadNative();
        mFuseLoader = FuseAdLoader.get(CONFIG_SLOT_HOME_LUCKY, this);
        fuseLoadNative();
    }

    private void showLoadding() {
        mProgressBar.setVisibility(View.VISIBLE);
        mLl_Root.setVisibility(View.GONE);
    }

    private void hideLoadding() {
        mProgressBar.setVisibility(View.GONE);
        mLl_Root.setVisibility(View.VISIBLE);
    }

    private void setlistener() {
        mRlClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        mIvIcon = (BasicLazyLoadImageView) findViewById(R.id.mobvista_interstitial_iv_icon);
        mIvImage = (BasicLazyLoadImageView) findViewById(R.id.mobvista_interstitial_iv_image);
        mTvAppName = (TextView) findViewById(R.id.mobvista_interstitial_iv_app_name);
        mTvAppDesc = (TextView) findViewById(R.id.mobvista_interstitial_tv_app_desc);
        mTvCta = (TextView) findViewById(R.id.mobvista_interstitial_tv_cta);
        mRlClose = (RelativeLayout) findViewById(R.id.mobvista_interstitial_rl_close);
        mStarLayout = (StarLevelLayoutView) findViewById(R.id.mobvista_interstitial_star);
        mProgressBar = (ProgressBar) findViewById(R.id.mobvista_interstitial_progress);
        mLl_Root = (LinearLayout) findViewById(R.id.mobvista_interstitial_ll_root);
        mChoiceImage = (BasicLazyLoadImageView) findViewById(R.id.ad_choices_image);
        initAdmobBannerView();
    }

    private void initAdmobBannerView() {
        mAdmobExpressView = new NativeExpressAdView(this);
        List<AdConfig> adConfigs = RemoteConfig.getAdConfigList(CONFIG_SLOT_HOME_LUCKY);
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
        mAdmobExpressView.setAdSize(new AdSize(360, 320));
//        mAdmobExpressView.setAdUnitId("ca-app-pub-5490912237269284/2431070657");
        mAdmobExpressView.setAdUnitId(adunit);
        mAdmobExpressView.setVisibility(View.GONE);
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
                hideLoadding();
                mLl_Root.removeAllViews();
                mLl_Root.addView(mAdmobExpressView);
                mAdmobExpressView.setVisibility(View.VISIBLE);
                AdLog.d("on Banner AdLoaded ");
            }
        });
    }
    public void fuseLoadNative() {
        mFuseLoader.loadAd(1, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAd ad) {
//                hideLoadding();
//                fillInterstitialLayout(ad);
//                mFuseLoader.loadAd(1, null);
                loadAdmobNativeExpress();
            }

            @Override
            public void onAdListLoaded(List<IAd> ads) {

            }

            @Override
            public void onError(String error) {
                MLogs.e("Lucky load native error " + error);
                loadAdmobNativeExpress();
            }
        });
    }
    private void loadAdmobNativeExpress(){
        if (mAdmobExpressView == null) {
            return;
        }
        MLogs.d("Home loadAdmobNativeExpress");
        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID(this);
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            boolean isTestDevice = request.isTestDevice(this);
            AdLog.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
            AdLog.d( "Admob unit id "+ mAdmobExpressView.getAdUnitId());
            mAdmobExpressView.loadAd(request );
        } else {
            mAdmobExpressView.loadAd(new AdRequest.Builder().build());
        }
    }
    protected void fillInterstitialLayout(IAd ad) {
        if (!TextUtils.isEmpty(ad.getIconImageUrl())) {
            mIvIcon.setDefaultResource(0);
            mIvIcon.requestDisplayURL(ad.getIconImageUrl());
        }
        if (!TextUtils.isEmpty(ad.getCoverImageUrl())) {
            mIvImage.setDefaultResource(0);
            mIvImage.requestDisplayURL(ad.getCoverImageUrl());
        }
        if (!TextUtils.isEmpty(ad.getPrivacyIconUrl())) {
            mChoiceImage.setDefaultResource(0);
            mChoiceImage.requestDisplayURL(ad.getPrivacyIconUrl());
        }

        mTvAppName.setText(ad.getTitle() + "");
        mTvAppDesc.setText(ad.getBody() + "");
        mTvCta.setText(ad.getCallToActionText());
        int rating = (int) ad.getStarRating();
        mStarLayout.setRating(rating);
        ad.registerViewForInteraction(mLl_Root);
        ad.registerPrivacyIconView(mChoiceImage);
    }
}