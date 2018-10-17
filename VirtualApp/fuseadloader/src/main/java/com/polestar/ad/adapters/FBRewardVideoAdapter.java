package com.polestar.ad.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSettings;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;

public class FBRewardVideoAdapter extends AdAdapter {
    private Context mContext;
    private RewardedVideoAd rewardedVideoAd;

    public FBRewardVideoAdapter(Context context, String key) {
        mKey = key;
        mContext = context;
        LOAD_TIMEOUT = 20*1000;
    }
    @Override
    public void registerPrivacyIconView(View view) {

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
        rewardedVideoAd = new RewardedVideoAd(mContext, mKey);
        rewardedVideoAd.setAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoCompleted() {
                AdLog.d("onRewardedVideoCompleted");
                if (adListener != null) {
                    adListener.onRewarded(FBRewardVideoAdapter.this);
                }
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                AdLog.d("onLoggingImpression");
            }

            @Override
            public void onRewardedVideoClosed() {
                if(adListener != null) {
                    adListener.onAdClosed(FBRewardVideoAdapter.this);
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (adListener != null) {
                    adListener.onError(adError.getErrorMessage());
                }
                stopMonitor();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                AdLog.d("onAdLoaded");
                stopMonitor();
                mLoadedTime = System.currentTimeMillis();
                if(adListener != null) {
                    adListener.onAdLoaded(FBRewardVideoAdapter.this);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
                if(adListener != null) {
                    adListener.onAdClicked(FBRewardVideoAdapter.this);
                }
            }
        });
        rewardedVideoAd.loadAd();
        startMonitor();
    }

    @Override
    public boolean isInterstitialAd() {
        return true;
    }


    @Override
    public Object getAdObject() {
        return rewardedVideoAd;
    }

    @Override
    public String getAdType() {
        return AdConstants.NativeAdType.AD_SOURCE_FB_REWARD;
    }

    @Override
    protected void onTimeOut() {
        if (adListener != null) {
            adListener.onError("TIME_OUT");
        }
    }

    @Override
    public void show() {
        if (rewardedVideoAd != null
                && rewardedVideoAd.isAdLoaded()) {
            registerViewForInteraction(null);
            rewardedVideoAd.show();
        }
    }
}
