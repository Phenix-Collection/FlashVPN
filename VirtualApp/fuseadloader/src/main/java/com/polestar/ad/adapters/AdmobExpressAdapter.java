package com.polestar.ad.adapters;

import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.AdViewBinder;

/**
 * Created by guojia on 2017/6/30.
 */

public class AdmobExpressAdapter  extends AdAdapter {
    private NativeExpressAdView mRawAd;
    private Context mContext;
    private AdSize mSize;
    private final static String TAG = "AdmobExpressAdapter";

    public AdmobExpressAdapter(Context context, String key, AdSize bannerSize) {
        mContext = context;
        mKey = key;
        mSize = bannerSize;
        initAdView();
    }

    @Override
    public String getAdType() {
        return AdConstants.NativeAdType.AD_SOURCE_ADMOB_NAVTIVE_BANNER;
    }

    @Override
    public View getAdView(AdViewBinder viewBinder) {
        registerViewForInteraction(mRawAd);
        return mRawAd;
    }

    private void initAdView() {
        if (mRawAd == null) {
            mRawAd = new NativeExpressAdView(mContext);
        }
        mRawAd.setAdSize(mSize);
//        mAdmobExpressView.setAdUnitId("ca-app-pub-5490912237269284/2431070657");
        mRawAd.setAdUnitId(mKey);
        mRawAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                if (adListener != null) {
                    adListener.onAdClosed(AdmobExpressAdapter.this);
                }
                AdLog.d(TAG, "onAdClosed");
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
                    adListener.onAdClicked(AdmobExpressAdapter.this);
                }
            }

            @Override
            public void onAdLoaded() {
                AdLog.d(TAG, "onAdLoaded");
                mLoadedTime = System.currentTimeMillis();
                stopMonitor();
                super.onAdLoaded();
                if (adListener != null) {
                    adListener.onAdLoaded(AdmobExpressAdapter.this);
                }
            }
        });
    }

    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        adListener = listener;
        AdLog.d("loadAdmobNativeExpress");
        startMonitor();
        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID(mContext);
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            boolean isTestDevice = request.isTestDevice(mContext);
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
