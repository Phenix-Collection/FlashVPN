package com.polestar.ad.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdUtils;
import com.polestar.ad.L;


/**
 * Created by guojia on 2016/10/31.
 */

public class AdmobNativeAdapter extends Ad implements IAdLoader {

    private String mFilter;
    private Context mContext;
    private IAdLoadListener mListener;

    private com.google.android.gms.ads.formats.NativeAd mRawAd;

    public AdmobNativeAdapter(Context context, String key) {
        mContext = context;
        mKey = key;
    }

    public void setFilter(String filter) {
        mFilter = filter;
    }
    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        if (listener == null) {
            L.e("listener not set.");
            return;
        }
        mListener = listener;
        if (num > 1) {
            L.d("Admob not support load for more than 1 ads. Only return 1 ad");
        }
        AdLoader.Builder  builder = new AdLoader.Builder(mContext, mKey);
        boolean isContent = false;
        boolean isInstall = false;
        if (TextUtils.isEmpty(mFilter) || mFilter.equals(AdConstants.AdMob.FILTER_BOTH_INSTALL_AND_CONTENT)){
            isContent = true;
            isInstall = true;
        } else if (mFilter.equals(AdConstants.AdMob.FILTER_ONLY_CONTENT)){
            isContent = true;
        }else if (mFilter.equals(AdConstants.AdMob.FILTER_ONLY_INSTALL)) {
            isInstall = true;
        }

        if (isContent) {
            builder.forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
                @Override
                public void onContentAdLoaded(NativeContentAd nativeContentAd) {
                    postOnAdLoaded(nativeContentAd);
                }
            });
        }
        if (isInstall) {
            builder.forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
                @Override
                public void onAppInstallAdLoaded(NativeAppInstallAd nativeAppInstallAd) {
                    postOnAdLoaded(nativeAppInstallAd);
                }
            });
        }

        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                postOnAdLoadFail(i);
            }
        });
        AdLoader adLoader = builder.build();
        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID(mContext);
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            adLoader.loadAd(request);
            boolean isTestDevice = request.isTestDevice(mContext);
            L.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
        } else {
            adLoader.loadAd(new AdRequest.Builder().build());
        }
    }

    private void postOnAdLoaded(com.google.android.gms.ads.formats.NativeAd ad) {
        mRawAd = ad;
        mLoadedTime = System.currentTimeMillis();
        if (mListener != null) {
            mListener.onAdLoaded(this);
        }
    }

    private void postOnAdLoadFail(int i) {
        if (mListener != null) {
            mListener.onError("" + i);
        }
    }

    @Override
    public void registerPrivacyIconView(View view) {
    }

    @Override
    public String getAdType() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return AdConstants.NativeAdType.AD_SOURCE_ADMOB_INSTALL;
        }
        if (mRawAd instanceof NativeContentAd) {
            return AdConstants.NativeAdType.AD_SOURCE_ADMOB_INSTALL;
        }
        return null;
    }

    @Override
    public String getCoverImageUrl() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return ((NativeAppInstallAd) mRawAd).getImages().get(0).getUri().toString();
        }
        if (mRawAd instanceof NativeContentAd) {
            return((NativeContentAd) mRawAd).getImages().get(0).getUri().toString();
        }
        return null;
    }

    @Override
    public String getIconImageUrl() {
        if (mRawAd instanceof NativeAppInstallAd && ((NativeAppInstallAd) mRawAd).getIcon() != null) {
            return ((NativeAppInstallAd) mRawAd).getIcon().getUri().toString();
        }
        if (mRawAd instanceof NativeContentAd
                && ((NativeContentAd) mRawAd).getLogo() != null) {
            return ((NativeContentAd) mRawAd).getLogo().getUri().toString();
        }
        return  null;
    }

    @Override
    public String getBody() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return ((NativeAppInstallAd) mRawAd).getBody().toString();
        }
        if (mRawAd instanceof NativeContentAd) {
            return((NativeContentAd) mRawAd).getBody().toString();
        }
        return  null;
    }

    @Override
    public String getSubtitle() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return ((NativeAppInstallAd) mRawAd).getBody().toString();
        }
        if (mRawAd instanceof NativeContentAd) {
            return((NativeContentAd) mRawAd).getBody().toString();
        }
        return null;
    }

    @Override
    public double getStarRating() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return ((NativeAppInstallAd) mRawAd).getStarRating();
        }
        return super.getStarRating();
    }

    @Override
    public String getTitle() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return ((NativeAppInstallAd) mRawAd).getHeadline().toString();
        }
        if (mRawAd instanceof NativeContentAd) {
            return((NativeContentAd) mRawAd).getHeadline().toString();
        }
        return null;
    }

    @Override
    public String getCallToActionText() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return ((NativeAppInstallAd) mRawAd).getCallToAction().toString();
        }
        if (mRawAd instanceof NativeContentAd) {
            return((NativeContentAd) mRawAd).getCallToAction().toString();
        }
        return null;
    }

    @Override
    public Object getAdObject() {
        return mRawAd;
    }

    @Override
    public String getId() {
        return null;
    }
}
