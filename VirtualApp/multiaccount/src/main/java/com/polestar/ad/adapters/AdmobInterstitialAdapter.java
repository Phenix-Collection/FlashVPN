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
    private IAdLoadListener adLoadListener;
    private Context mContext;
    private String key;

    public AdmobInterstitialAdapter(Context context, String key) {
        mContext = context;
        this.key = key;
    }
    @Override
    public Object getAdObject() {
        return rawAd;
    }

    @Override
    public String getAdType() {
        return AdConstants.NativeAdType.AD_SOURCE_ADMOB_INTERSTITIAL;
    }

    @Override
    public void registerPrivacyIconView(View view) {

    }

    @Override
    public void show() {
        rawAd.show();
    }

    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        adLoadListener = listener;
        if (listener == null){
            AdLog.e("listener is null!!");
            return;
        }
        rawAd = new InterstitialAd(mContext);
        rawAd.setAdUnitId(key);
        rawAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                if (adLoadListener != null) {
                    adLoadListener.onError("ErrorCode: " + i);
                }
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (adLoadListener != null) {
                    adLoadListener.onAdLoaded(AdmobInterstitialAdapter.this);
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }
        });

        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID(mContext);
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            rawAd.loadAd(request);
            boolean isTestDevice = request.isTestDevice(mContext);
            AdLog.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
        } else {
            rawAd.loadAd(new AdRequest.Builder().build());
        }
    }
}
