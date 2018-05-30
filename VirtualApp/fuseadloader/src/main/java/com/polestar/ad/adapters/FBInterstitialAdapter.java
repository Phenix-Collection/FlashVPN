package com.polestar.ad.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;

/**
 * Created by guojia on 2016/12/11.
 */

public class FBInterstitialAdapter extends AdAdapter implements InterstitialAdListener {
    private InterstitialAd interstitialAd;

    @Override
    public boolean isInterstitialAd() {
        return true;
    }

    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        adListener = listener;
        if (listener == null) {
            AdLog.e("Not set listener!");
            return;
        }
        if (AdConstants.DEBUG) {
            SharedPreferences sp = mContext.getSharedPreferences("FBAdPrefs", Context.MODE_PRIVATE);
            String deviceIdHash = sp.getString("deviceIdHash", "");
            AdSettings.addTestDevice(deviceIdHash);
            boolean isTestDevice = AdSettings.isTestMode(mContext);
            AdLog.d( "is FB Test Device ? "+deviceIdHash+" "+isTestDevice);
        }
        interstitialAd = new InterstitialAd(mContext, key);
        interstitialAd.setAdListener(this);
        interstitialAd.loadAd();
        startMonitor();
    }

    private String key;
    private Context mContext;

    public FBInterstitialAdapter(Context context, String key) {
        mContext = context;
        this.key = key;
        LOAD_TIMEOUT = 20*1000;

    }
    @Override
    public void show() {
        if (interstitialAd != null) {
            registerViewForInteraction(null);
            interstitialAd.show();
        }
    }
    @Override
    public void onError(com.facebook.ads.Ad ad, AdError error) {
        // AdAdapter failed to load
        if (adListener != null) {
            adListener.onError(error.getErrorMessage());
        }
        stopMonitor();
    }

    @Override
    public void onAdLoaded(com.facebook.ads.Ad ad) {
        // AdAdapter is loaded and ready to be displayed
        // You can now display the full screen add using this code:
        mLoadedTime = System.currentTimeMillis();
        if(adListener != null) {
            adListener.onAdLoaded(this);
        }
        stopMonitor();
    }

    @Override
    public void onAdClicked(com.facebook.ads.Ad ad) {
        if(adListener != null) {
            adListener.onAdClicked(this);
        }
    }

    @Override
    public void onInterstitialDisplayed(com.facebook.ads.Ad ad) {

    }

    @Override
    public void onInterstitialDismissed(com.facebook.ads.Ad ad) {
        AdLog.d("onInterstitialDismissed");
        if (adListener != null) {
            AdLog.d("call onAdClockedcc " + adListener);
            adListener.onAdClosed(this);
        }
    }


    @Override
    public void registerPrivacyIconView(View view) {

    }

    @Override
    public void onLoggingImpression(com.facebook.ads.Ad ad) {

    }

    @Override
    public Object getAdObject() {
        return interstitialAd;
    }

    @Override
    public String getAdType() {
        return AdConstants.NativeAdType.AD_SOURCE_FACEBOOK_INTERSTITIAL;
    }

    @Override
    protected void onTimeOut() {
        if (adListener != null) {
            adListener.onError("TIME_OUT");
        }
    }
}
