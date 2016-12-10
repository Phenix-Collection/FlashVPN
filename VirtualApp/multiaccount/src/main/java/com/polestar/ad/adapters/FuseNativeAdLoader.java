package com.polestar.ad.adapters;

import android.content.Context;

import com.polestar.ad.AdConstants;
import com.polestar.ad.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by guojia on 2016/11/15.
 */

public class FuseNativeAdLoader implements INativeAdLoader {
    private Context mContext;
    private List<NativeAdConfig> mNativeAdConfigList = new ArrayList();
    private HashMap<String, INativeAd> mNativeAdCache = new HashMap<>();
    private INativeAdLoadListener mListener;
    private int currentLoadingIdx = -1;

    private class NativeAdConfig {
        public String key;
        public String source;
        public long cacheTime;
        public NativeAdConfig(String source, String key, long cacheTime) {
            this.key = key;
            this.source = source;
            this.cacheTime = cacheTime;
        }
    }

    public FuseNativeAdLoader(Context context) {
        this.mContext = context;
    }

    /**
     *  Add native ad sources
     * @param source source of the native ad: ab, ab_install, ab_content, fb, apx,vk ... see AdConstants.NativeAdType
     * @param key the key of the ad source,
     * @param cacheTime cache time of the ad source, or you can set it -1 to use the default one.
     */
    public void addNativeAdSource(String source, String key, long cacheTime) {
        mNativeAdConfigList.add(new NativeAdConfig(source, key, cacheTime));
    }

    @Override
    public void loadAd(int num, INativeAdLoadListener listener) {
        if (listener == null) {
            return;
        }
        if ( num  < 0 || mNativeAdConfigList.size() == 0) {
            L.d("FuseNativeAdLoader :" + "load num wrong: " + num);
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
        NativeAdConfig config = mNativeAdConfigList.get(currentLoadingIdx);
        //Find cache
        INativeAd ad = mNativeAdCache.get(config.key);
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
        INativeAdLoader loader = getNativeAdAdapter(config);
        if (loader == null) {
            mListener.onError("Wrong config");
            return;
        }
        loader.loadAd(1, new INativeAdLoadListener() {
            @Override
            public void onAdLoaded(INativeAd ad) {
                if (currentLoadingIdx < mNativeAdConfigList.size()) {
                    mNativeAdCache.put(mNativeAdConfigList.get(currentLoadingIdx).key, ad);
                } else {
                    L.e("Ad loaded but not put into cache");
                }
                mListener.onAdLoaded(ad);
            }

            @Override
            public void onAdListLoaded(List<INativeAd> ads) {
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

    private INativeAdLoader getNativeAdAdapter(NativeAdConfig config){
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
            case AdConstants.NativeAdType.AD_SOURCE_VK:
            default:
                L.e("not suppported source " + config.source);
                return null;
        }
    }
}
