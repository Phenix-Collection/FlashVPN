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

public class FBInterstitialAdapter extends Ad implements InterstitialAdListener, IAdLoader {
    private InterstitialAd interstitialAd;
    private IAdLoadListener adListener;


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
    }

    private String key;
    private Context mContext;

    public FBInterstitialAdapter(Context context, String key) {
        mContext = context;
        this.key = key;

    }
    @Override
    public void show() {
        if (interstitialAd != null) {
            interstitialAd.show();
        }
    }
    @Override
    public void onError(com.facebook.ads.Ad ad, AdError error) {
        // Ad failed to load
        if (adListener != null) {
            adListener.onError(error.getErrorMessage());
        }
    }

    @Override
    public void onAdLoaded(com.facebook.ads.Ad ad) {
        // Ad is loaded and ready to be displayed
        // You can now display the full screen add using this code:
        if(adListener != null) {
            adListener.onAdLoaded(this);
        }
    }

    @Override
    public void onAdClicked(com.facebook.ads.Ad ad) {

    }

    @Override
    public void onInterstitialDisplayed(com.facebook.ads.Ad ad) {

    }

    @Override
    public void onInterstitialDismissed(com.facebook.ads.Ad ad) {

    }


    @Override
    public void registerPrivacyIconView(View view) {

    }

    @Override
    public Object getAdObject() {
        return interstitialAd;
    }

    @Override
    public String getAdType() {
        return AdConstants.NativeAdType.AD_SOURCE_FACEBOOK_INTERSTITIAL;
    }
}
