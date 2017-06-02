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
import com.polestar.ad.AdViewBinder;
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
    private RelativeLayout mRlClose;
    private ProgressBar mProgressBar;
    private FuseAdLoader mFuseLoader;
    private NativeExpressAdView mAdmobExpressView;
    private LinearLayout mAdContainer;

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
        //mFuseLoader.addAdConfig(new AdConfig(AdConstants.NativeAdType.AD_SOURCE_FACEBOOK, "1713507248906238_1787756514814644", -1));
        //mNativeAdLoader.addAdConfig(new AdConfig(AdConstants.NativeAdType.AD_SOURCE_MOPUB, "ea31e844abf44e3690e934daad125451", -1));
        fuseLoadNative();
    }

    private void showLoadding() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadding() {
        mProgressBar.setVisibility(View.GONE);
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
        mRlClose = (RelativeLayout) findViewById(R.id.mobvista_interstitial_rl_close);
        mProgressBar = (ProgressBar) findViewById(R.id.mobvista_interstitial_progress);
        mAdContainer = (LinearLayout) findViewById(R.id.ad_container);
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
                mAdContainer.removeAllViews();
                mAdContainer.addView(mAdmobExpressView);
                mAdContainer.setVisibility(View.VISIBLE);
                mAdmobExpressView.setVisibility(View.VISIBLE);
                AdLog.d("on Banner AdLoaded ");
            }
        });
    }
    public void fuseLoadNative() {
        mFuseLoader.loadAd(1, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAd ad) {
                hideLoadding();
                fillInterstitialLayout(ad);
                mFuseLoader.loadAd(1, null);
//                loadAdmobNativeExpress();
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
        final AdViewBinder viewBinder =  new AdViewBinder.Builder(R.layout.native_interstitial_layout)
                .titleId(R.id.ad_title)
                .textId(R.id.ad_subtitle_text)
                .mainImageId(R.id.ad_cover_image)
                .iconImageId(R.id.ad_icon_image)
                .callToActionId(R.id.ad_cta_text)
                .privacyInformationIconImageId(R.id.ad_choices_image)
                .starLevelLayoutId(R.id.star_level_layout)
                .build();
        View adView = ad.getAdView(viewBinder);
        mAdContainer.addView(adView);
        mAdContainer.setVisibility(View.VISIBLE);
    }
}