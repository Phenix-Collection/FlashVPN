package com.polestar.ad.adapters;

import android.content.Context;
import android.view.View;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.multiaccount.component.activity.NativeInterstitialActivity;

/**
 * Created by guojia on 2017/6/7.
 */

public class MopubInterstitialAdapter extends AdAdapter implements MoPubInterstitial.InterstitialAdListener {
    private MoPubInterstitial mInterstitial;
    private IAdLoadListener mAdListener;
    private Context mContext;
    private String key;

    public MopubInterstitialAdapter(Context context, String key) {
        mContext = context;
        this.key = key;
        LOAD_TIMEOUT = 20*1000;

    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        mLoadedTime = System.currentTimeMillis();
        if (mAdListener != null) {
            mAdListener.onAdLoaded(this);
        }
        stopMonitor();
        AdLog.d("Mopub interstitial loaded");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        AdLog.d("Mopub interstitial load error: " + errorCode);
        if (mAdListener != null) {
            mAdListener.onError("" + errorCode);
        }
        stopMonitor();
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {

    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {

    }

    @Override
    public void registerPrivacyIconView(View view) {

    }

    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        mAdListener = listener;
        if (listener == null) {
            AdLog.e("Not set listener!");
            return;
        }
        if (AdConstants.DEBUG) {
            mKey = "24534e1901884e398f1253216226017e";
        }
        if (NativeInterstitialActivity.getInstance() == null) {
            mAdListener.onError("No activity context found!");
            return;
        }
        mInterstitial = new MoPubInterstitial(NativeInterstitialActivity.getInstance(), key);
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
        return AdConstants.NativeAdType.AD_SOURCE_MOPUB_INTERSTITIAL;
    }

    @Override
    protected void onTimeOut() {
        if (mAdListener != null) {
            mAdListener.onError("TIME_OUT");
        }
    }
}
