package com.polestar.ad.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;

/**
 * Created by guojia on 2017/6/7.
 */

public class MopubInterstitialAdapter extends AdAdapter implements MoPubInterstitial.InterstitialAdListener {
    private MoPubInterstitial mInterstitial;
    private String key;

    public MopubInterstitialAdapter(Context context, String key) {
        this.key = key;
        LOAD_TIMEOUT = 20*1000;

    }

    @Override
    public boolean isInterstitialAd() {
        return true;
    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        mLoadedTime = System.currentTimeMillis();
        if (adListener != null) {
            adListener.onAdLoaded(this);
        }
        stopMonitor();
        AdLog.d("Mopub interstitial loaded");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        AdLog.d("Mopub interstitial load error: " + errorCode);
        if (adListener != null) {
            adListener.onError("" + errorCode);
        }
        stopMonitor();
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        if (adListener != null) {
            adListener.onAdClicked(MopubInterstitialAdapter.this);
        }

    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        if (adListener != null) {
            adListener.onAdClosed(MopubInterstitialAdapter.this);
        }

    }

    @Override
    public void registerPrivacyIconView(View view) {

    }

    @Override
    public void loadAd(Context context, int num, IAdLoadListener listener) {
        adListener = listener;
        if (listener == null) {
            AdLog.e("Not set listener!");
            return;
        }
        if (AdConstants.DEBUG) {
            mKey = "24534e1901884e398f1253216226017e";
        }
        if (!(context instanceof Activity) ){
            adListener.onError("No activity context found!");
            return;
        }
        mInterstitial = new MoPubInterstitial((Activity)context, key);
        mInterstitial.setInterstitialAdListener(this);
        mInterstitial.load();
        startMonitor();
    }

    @Override
    public void show() {
        if (mInterstitial.isReady()) {
            registerViewForInteraction(null);
            mInterstitial.show();
        }
    }

    @Override
    public String getAdType() {
        return AdConstants.AdType.AD_SOURCE_MOPUB_INTERSTITIAL;
    }

    @Override
    protected void onTimeOut() {
        if (adListener != null) {
            adListener.onError("TIME_OUT");
        }
    }
}
