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
    private IAdLoadListener mListener;

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
                if (mListener != null) {
                    mListener.onAdClosed(AdmobExpressAdapter.this);
                }
                AdLog.d(TAG, "onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                AdLog.d(TAG, "onAdFailedToLoad " + i);
                stopMonitor();
                if (mListener != null) {
                    mListener.onError("ErrorCode " + i);
                }
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (mListener != null) {
                    mListener.onAdClicked(AdmobExpressAdapter.this);
                }
            }

            @Override
            public void onAdLoaded() {
                AdLog.d(TAG, "onAdLoaded");
                mLoadedTime = System.currentTimeMillis();
                stopMonitor();
                super.onAdLoaded();
                if (mListener != null) {
                    mListener.onAdLoaded(AdmobExpressAdapter.this);
                }
            }
        });
    }

    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        mListener = listener;
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
        if (mListener != null) {
            mListener.onError("TIME_OUT");
        }
    }
}
