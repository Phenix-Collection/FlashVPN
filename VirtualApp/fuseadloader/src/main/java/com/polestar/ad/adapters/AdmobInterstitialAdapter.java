package com.polestar.ad.adapters;

import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdUtils;
import com.polestar.ad.AdLog;

/**
 * Created by guojia on 2016/12/11.
 */

public class AdmobInterstitialAdapter extends AdAdapter {
    private InterstitialAd rawAd;
    private String key;

    public AdmobInterstitialAdapter(Context context, String key) {
        this.key = key;
        LOAD_TIMEOUT = 20*1000;
    }
    @Override
    public Object getAdObject() {
        return rawAd;
    }

    @Override
    public String getAdType() {
        return AdConstants.AdType.AD_SOURCE_ADMOB_INTERSTITIAL;
    }

    @Override
    public void registerPrivacyIconView(View view) {

    }

    @Override
    public boolean isInterstitialAd() {
        return true;
    }

    @Override
    public void show() {
        registerViewForInteraction(null);
        rawAd.show();
    }

    @Override
    public void loadAd(Context context, int num, IAdLoadListener listener) {
        adListener = listener;
        if (listener == null){
            AdLog.e("listener is null!!");
            return;
        }
        rawAd = new InterstitialAd(context);
        rawAd.setAdUnitId(key);
        rawAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                if (adListener != null) {
                    adListener.onError("ErrorCode: " + i);
                }
                stopMonitor();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mLoadedTime = System.currentTimeMillis();
                if (adListener != null) {
                    adListener.onAdLoaded(AdmobInterstitialAdapter.this);
                }
                stopMonitor();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (adListener != null) {
                    adListener.onAdClicked(AdmobInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                AdLog.d("ad interstitial onAdClosed");
                if (adListener != null) {
                    adListener.onAdClosed(AdmobInterstitialAdapter.this);
                }
            }
        });

        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID(context);
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            rawAd.loadAd(request);
            boolean isTestDevice = request.isTestDevice(context);
            AdLog.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
        } else {
            rawAd.loadAd(new AdRequest.Builder().build());
        }
        startMonitor();
    }

    @Override
    protected void onTimeOut() {
        if (adListener != null) {
            adListener.onError("TIME_OUT");
        }
    }
}
