package com.polestar.ad.adapters;

import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.AdViewBinder;

/**
 * Created by guojia on 2017/6/30.
 */

public class AdmobBannerAdapter extends AdAdapter {
    private AdView mRawAd;
    private AdSize mSize;
    private final static String TAG = "AdmobBannerAdapter";

    public AdmobBannerAdapter(Context context, String key, AdSize bannerSize) {
        mKey = key;
        mSize = bannerSize;
    }

    @Override
    public String getAdType() {
        return AdConstants.AdType.AD_SOURCE_ADMOB_BANNER;
    }

    @Override
    public View getAdView(Context context, AdViewBinder viewBinder) {
        registerViewForInteraction(mRawAd);
        return mRawAd;
    }

    private void initAdView(Context context) {
        if (mRawAd == null) {
            mRawAd = new AdView(context);
            mRawAd.setAdSize(mSize);
//        mAdmobExpressView.setAdUnitId("ca-app-pub-5490912237269284/2431070657");
            mRawAd.setAdUnitId(mKey);
            mRawAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    AdLog.d(TAG, "onAdClosed");
                    if (adListener != null) {
                        adListener.onAdClosed(AdmobBannerAdapter.this);
                    }
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    AdLog.d(TAG, "onAdFailedToLoad " + i);
                    stopMonitor();
                    if (adListener != null) {
                        adListener.onError("ErrorCode " + i);
                    }
                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    if (adListener != null) {
                        adListener.onAdClicked(AdmobBannerAdapter.this);
                    }
                }

                @Override
                public void onAdLoaded() {
                    AdLog.d(TAG, "onAdLoaded");
                    mLoadedTime = System.currentTimeMillis();
                    stopMonitor();
                    super.onAdLoaded();
                    if (adListener != null) {
                        adListener.onAdLoaded(AdmobBannerAdapter.this);
                    }
                }
            });
        }

    }

    @Override
    public void loadAd(Context context, int num, IAdLoadListener listener) {
        adListener = listener;
        initAdView(context);
        AdLog.d("loadAdmobNativeExpress");
        startMonitor();
        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID(context);
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            boolean isTestDevice = request.isTestDevice(context);
            AdLog.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
            AdLog.d( "Admob unit id "+ mRawAd.getAdUnitId());
            mRawAd.loadAd(request );
        } else {
            mRawAd.loadAd(new AdRequest.Builder().build());
        }

    }

    @Override
    public void registerPrivacyIconView(View view) {

    }

    @Override
    public Object getAdObject() {
        return mRawAd;
    }

    @Override
    protected void onTimeOut() {
        if (adListener != null) {
            adListener.onError("TIME_OUT");
        }
    }
}
