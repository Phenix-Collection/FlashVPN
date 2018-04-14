package com.polestar.ad.adapters;

import android.content.Context;
import android.view.View;

import com.batmobi.AdError;
import com.batmobi.BatAdBuild;
import com.batmobi.BatAdType;
import com.batmobi.BatInterstitialAd;
import com.batmobi.BatmobiLib;
import com.batmobi.IAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;

/**
 * Created by guojia on 2018/2/3.
 */

public class BtInterstitialAdapter extends AdAdapter  {
    private BatInterstitialAd rawAd;
    private IAdLoadListener adLoadListener;
    private Context mContext;

    public BtInterstitialAdapter(Context context, String key) {
        mContext = context;
        mKey = key;
        LOAD_TIMEOUT = 20*1000;
    }
    @Override
    public Object getAdObject() {
        return rawAd;
    }

    @Override
    public String getAdType() {
        return AdConstants.NativeAdType.AD_SOURCE_BT_INTERSTITIAL;
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
        AdLog.d("BT show");
        rawAd.show();
    }

    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        adLoadListener = listener;
        if (listener == null){
            AdLog.e("listener is null!!");
            return;
        }
        BatAdBuild.Builder build = new BatAdBuild.Builder(mContext,
                mKey,
                BatAdType.INTERSTITIAL_320X480.getType(),
                new IAdListener() {
                    @Override
                    public void onAdLoadFinish(Object obj) {
                        mLoadedTime = System.currentTimeMillis();
                        if (obj == null) {
                            AdLog.d("Error:Ad object is null");
                            return;
                        }
                        if (obj instanceof BatInterstitialAd) {
                            rawAd = (BatInterstitialAd) obj;
                            if (adLoadListener != null) {
                                AdLog.d("Message:onAdLoadFinish");
                                adLoadListener.onAdLoaded(BtInterstitialAdapter.this);
                            }
                        }
                        stopMonitor();
                    }

                    @Override
                    public void onAdError(AdError error) {
                        AdLog.d("Message:onAdError " + error);
                        if (adLoadListener != null) {
                            adLoadListener.onError(error.getMsg());
                        }
                        stopMonitor();
                    }

                    @Override
                    public void onAdClosed() {
                        AdLog.d("Message:onAdClosed");
                        if (adLoadListener != null) {
                            adLoadListener.onAdClosed(BtInterstitialAdapter.this);
                        }
                    }


                    @Override
                    public void onAdShowed() {
                        AdLog.d("Message:onAdShowed");
                    }

                    @Override
                    public void onAdClicked() {
                        AdLog.d("Message:onAdClicked");
                        if (adLoadListener != null) {
                            adLoadListener.onAdClicked(BtInterstitialAdapter.this);
                        }
                    }
                });

        BatmobiLib.load(build.build());
        startMonitor();
    }

    @Override
    protected void onTimeOut() {
        if (adLoadListener != null) {
            adLoadListener.onError("TIME_OUT");
        }
    }
}
