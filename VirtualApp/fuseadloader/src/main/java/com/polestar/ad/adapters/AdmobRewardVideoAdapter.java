package com.polestar.ad.adapters;

import android.app.Activity;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;

/**
 * Created by guojia on 2018/3/25.
 */

public class AdmobRewardVideoAdapter extends AdAdapter  {
    private RewardedVideoAd rawAd;
    private IAdLoadListener adLoadListener;
    private Context mContext;
    private String key;

    public AdmobRewardVideoAdapter(Context context, String key) {
        mContext = context;
        this.key = key;
        LOAD_TIMEOUT = 20*1000;
    }
    @Override
    public Object getAdObject() {
        return rawAd;
    }

    @Override
    public String getAdType() {
        return AdConstants.NativeAdType.AD_SOURCE_ADMOB_REWARD_VIDEO;
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
    public void loadAd(int num, IAdLoadListener listener) {
        adLoadListener = listener;
        if (listener == null){
            AdLog.e("listener is null!!");
            return;
        }
        rawAd = MobileAds.getRewardedVideoAdInstance(mContext);
        AdRequest request;
        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID(mContext);
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            request = new AdRequest.Builder().addTestDevice(deviceId).build();
            boolean isTestDevice = request.isTestDevice(mContext);
            AdLog.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
        } else {
            request = new AdRequest.Builder().build();
        }
        rawAd.loadAd(key, request);
        rawAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                mLoadedTime = System.currentTimeMillis();
                if (adLoadListener != null) {
                    adLoadListener.onAdLoaded(AdmobRewardVideoAdapter.this);
                }
                stopMonitor();
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {

            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                if (adLoadListener != null) {
                    adLoadListener.onRewarded(AdmobRewardVideoAdapter.this);
                }
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                if (adLoadListener != null) {
                    adLoadListener.onError("ErrorCode: " + i);
                }
                stopMonitor();
            }
        });
        startMonitor();
    }

    @Override
    protected void onTimeOut() {
        if (adLoadListener != null) {
            adLoadListener.onError("TIME_OUT");
        }
    }

    @Override
    public void resume(Activity activity) {
        if (rawAd != null)
            rawAd.resume(activity);
    }

    @Override
    public void pause(Activity activity) {
        if (rawAd != null)
            rawAd.pause(activity);
    }

    @Override
    public void destroy(Activity activity) {
        if (rawAd != null)
            rawAd.destroy(activity);
    }
}
