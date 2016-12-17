package com.polestar.ad.adapters;

import android.content.Context;

import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by guojia on 2016/11/15.
 */

public class FuseAdLoader implements IAdLoader {
    private Context mContext;
    private List<AdConfig> mNativeAdConfigList = new ArrayList();
    private HashMap<String, IAd> mNativeAdCache = new HashMap<>();
    private IAdLoadListener mListener;
    private int currentLoadingIdx = -1;

    public FuseAdLoader(Context context) {
        this.mContext = context;
    }

    /**
     *  Add native ad sources
     * @param source source of the native ad: ab, ab_install, ab_content, fb, apx,vk ... see AdConstants.NativeAdType
     * @param key the key of the ad source,
     * @param cacheTime cache time of the ad source, or you can set it -1 to use the default one.
     */
    public void addAdSource(String source, String key, long cacheTime) {
        mNativeAdConfigList.add(new AdConfig(source, key, cacheTime));
    }

    public void addAdConfig(AdConfig adConfig) {
        if (adConfig != null) {
            mNativeAdConfigList.add(adConfig);
        }
    }

    public boolean hasValidAdSource() {
        return mNativeAdConfigList!=null && mNativeAdConfigList.size() > 0;
    }

    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        if (listener == null) {
            return;
        }
        if ( num  < 0 || mNativeAdConfigList.size() == 0) {
            L.d("FuseAdLoader :" + "load num wrong: " + num);
            listener.onError("Wrong config");
            return;
        }
        mListener = listener;
        currentLoadingIdx = 0;
        loadNextNativeAd();

    }

    private void loadNextNativeAd() {
        if ( currentLoadingIdx >= mNativeAdConfigList.size()) {
            L.e("Tried to load all source, no fill. Index : " + currentLoadingIdx);
            mListener.onError("No Fill");
            return;
        }
        AdConfig config = mNativeAdConfigList.get(currentLoadingIdx);
        //Find cache
        IAd ad = mNativeAdCache.get(config.key);
        if (ad != null) {
            if (ad.isShowed() || (System.currentTimeMillis() - ad.getLoadedTime()) > config.cacheTime) {
                L.d("Ad cache time out : " + ad.getTitle() + " type: " + ad.getAdType());
                mNativeAdCache.remove(config.key);
            } else {
                mListener.onAdLoaded(ad);
                return;
            }
        }
        //Do load
        IAdLoader loader = getNativeAdAdapter(config);
        if (loader == null) {
            mListener.onError("Wrong config");
            return;
        }
        loader.loadAd(1, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAd ad) {
                if (currentLoadingIdx < mNativeAdConfigList.size()) {
                    mNativeAdCache.put(mNativeAdConfigList.get(currentLoadingIdx).key, ad);
                } else {
                    L.e("Ad loaded but not put into cache");
                }
                L.d("ad loaded " + ad.getAdType());
                mListener.onAdLoaded(ad);
            }

            @Override
            public void onAdListLoaded(List<IAd> ads) {
                //not support list yet
            }

            @Override
            public void onError(String error) {
                if(currentLoadingIdx >= mNativeAdConfigList.size()) {
                    L.e("Tried to load all source, no fill. Index : " + currentLoadingIdx);
                    mListener.onError("No Fill");
                    return;
                }
                L.e("Load current source " + mNativeAdConfigList.get(currentLoadingIdx).source + " error : " + error);
                currentLoadingIdx++;
                loadNextNativeAd();

            }
        });
    }

    private IAdLoader getNativeAdAdapter(AdConfig config){
        if (config == null || config.source == null) {
            return null;
        }
        switch (config.source) {
            case AdConstants.NativeAdType.AD_SOURCE_ADMOB:
                return new AdmobNativeAdapter(mContext, config.key);
            case AdConstants.NativeAdType.AD_SOURCE_ADMOB_CONTENT:
                AdmobNativeAdapter adapter = new AdmobNativeAdapter(mContext, config.key);
                adapter.setFilter(AdConstants.AdMob.FILTER_ONLY_CONTENT);
                return adapter;
            case AdConstants.NativeAdType.AD_SOURCE_ADMOB_INSTALL:
                AdmobNativeAdapter adapterInstall = new AdmobNativeAdapter(mContext, config.key);
                adapterInstall.setFilter(AdConstants.AdMob.FILTER_ONLY_INSTALL);
                return adapterInstall;
            case AdConstants.NativeAdType.AD_SOURCE_FACEBOOK:
                return new FBNativeAdapter(mContext, config.key);
            case AdConstants.NativeAdType.AD_SOURCE_FACEBOOK_INTERSTITIAL:
                return new FBInterstitialAdapter(mContext, config.key);
            case AdConstants.NativeAdType.AD_SOURCE_ADMOB_INTERSTITIAL:
                return new AdmobInterstitialAdapter(mContext, config.key);
            case AdConstants.NativeAdType.AD_SOURCE_VK:
            default:
                L.e("not suppported source " + config.source);
                return null;
        }
    }
}
