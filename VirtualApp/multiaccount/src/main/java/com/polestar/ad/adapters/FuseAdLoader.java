package com.polestar.ad.adapters;

import android.content.Context;
import android.text.TextUtils;

import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.imageloader.ImageLoader;
import com.polestar.multiaccount.utils.RemoteConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    private boolean mIsLoading = false;
    private String mSlot;
    private IAd mReadyAd;

    private static HashMap<String, FuseAdLoader> sAdLoaderMap = new HashMap<>();
    public synchronized static FuseAdLoader get(String slot, Context appContext) {
        FuseAdLoader adLoader = sAdLoaderMap.get(slot);
        if (adLoader == null) {
            adLoader = new FuseAdLoader(slot, appContext.getApplicationContext());
            sAdLoaderMap.put(slot, adLoader);
        }
        return adLoader;
    }

    private FuseAdLoader(String slot, Context context) {
        this.mContext = context;
        mSlot = slot;
        List<AdConfig> adSources = RemoteConfig.getAdConfigList(mSlot);
        addAdConfigList(adSources);
    }

    public static final HashSet<String> SUPPORTED_TYPES = new HashSet<>();
    static {
        SUPPORTED_TYPES.add(AdConstants.NativeAdType.AD_SOURCE_ADMOB);
        SUPPORTED_TYPES.add(AdConstants.NativeAdType.AD_SOURCE_ADMOB_CONTENT);
        SUPPORTED_TYPES.add(AdConstants.NativeAdType.AD_SOURCE_ADMOB_INSTALL);
        SUPPORTED_TYPES.add(AdConstants.NativeAdType.AD_SOURCE_ADMOB_INTERSTITIAL);
        SUPPORTED_TYPES.add(AdConstants.NativeAdType.AD_SOURCE_FACEBOOK);
        SUPPORTED_TYPES.add(AdConstants.NativeAdType.AD_SOURCE_FACEBOOK_INTERSTITIAL);
        SUPPORTED_TYPES.add(AdConstants.NativeAdType.AD_SOURCE_MOPUB);
        SUPPORTED_TYPES.add(AdConstants.NativeAdType.AD_SOURCE_MOPUB_INTERSTITIAL);

    }

    public void addAdConfig(AdConfig adConfig) {
        if (adConfig != null && !TextUtils.isEmpty(adConfig.source) && !TextUtils.isEmpty(adConfig.key)) {
            if (SUPPORTED_TYPES.contains(adConfig.source)) {
                mNativeAdConfigList.add(adConfig);
                AdLog.d("add adConfig : " + adConfig.toString());
            }
        }
    }

    public void addAdConfigList(List<AdConfig> adConfigList) {
        if (adConfigList != null) {
            for(AdConfig adConfig: adConfigList) {
                addAdConfig(adConfig);
            }
        }
    }

    public boolean hasValidAdSource() {
        return mNativeAdConfigList!=null && mNativeAdConfigList.size() > 0;
    }

    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        AdLog.d("FuseAdLoader :" + mSlot + " load ad: " + num + " listener: " + listener);
        if ( num  < 0 || mNativeAdConfigList.size() == 0) {
            AdLog.d("FuseAdLoader :" + mSlot + " load num wrong: " + num);
            if (listener != null) {
                listener.onError("Wrong config");
            }
            return;
        }
        mListener = listener;
        if (!mIsLoading) {
            currentLoadingIdx = 0;
            mIsLoading = true;
            loadNextNativeAd();
        } else {
            if (mReadyAd!= null && mListener!=null) {
                mListener.onAdLoaded(mReadyAd);
                mReadyAd = null;
            }
            AdLog.d(mSlot + " is already loading...");
        }
    }

    private void loadNextNativeAd() {
        if ( currentLoadingIdx >= mNativeAdConfigList.size()) {
            AdLog.e(mSlot + " tried to load all source, no fill. Index : " + currentLoadingIdx);
            mIsLoading = false;
            if (mListener != null) {
                mListener.onError("No Fill");
            }
            return;
        }
        AdConfig config = mNativeAdConfigList.get(currentLoadingIdx);
        //Find cache
        IAd ad = mNativeAdCache.get(config.key);
        if (ad != null) {
            if (ad.isShowed() || ((System.currentTimeMillis() - ad.getLoadedTime())/1000) > config.cacheTime) {
                AdLog.d("Ad cache time out : " + ad.getTitle() + " type: " + ad.getAdType());
                mNativeAdCache.remove(config.key);
            } else {
                mIsLoading = false;
                if (mListener != null) {
                    mReadyAd = null;
                    mListener.onAdLoaded(ad);
                } else {
                    mReadyAd = ad;
                }
                return;
            }
        }
        //Do load
        IAdLoader loader = getNativeAdAdapter(config);
        if (loader == null) {
            mIsLoading = false;
            if (mListener != null) {
                mListener.onError("Wrong config");
            }
            return;
        }
        loader.loadAd(1, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAd ad) {
                if (currentLoadingIdx < mNativeAdConfigList.size()) {
                    mNativeAdCache.put(mNativeAdConfigList.get(currentLoadingIdx).key, ad);
                } else {
                    AdLog.e("Ad loaded but not put into cache");
                }
                AdLog.d(mSlot + " ad loaded " + ad.getAdType());
                if (ad.getCoverImageUrl() != null) {
                    AdLog.d("preload " + ad.getCoverImageUrl());
                    ImageLoader.getInstance().doPreLoad(mContext, ad.getCoverImageUrl());
                }
                if (ad.getIconImageUrl() != null) {
                    AdLog.d("preload " + ad.getIconImageUrl());
                    ImageLoader.getInstance().doPreLoad(mContext, ad.getIconImageUrl());
                }
                mIsLoading = false;
                if (mListener != null) {
                    mReadyAd = null;
                    mListener.onAdLoaded(ad);
                } else {
                    mReadyAd = ad;
                }
            }

            @Override
            public void onAdListLoaded(List<IAd> ads) {
                //not support list yet
            }

            @Override
            public void onError(String error) {
                if(currentLoadingIdx >= mNativeAdConfigList.size()) {
                    AdLog.e("Tried to load all source, no fill. Index : " + currentLoadingIdx);
                    mIsLoading = false;
                    if (mListener != null) {
                        mListener.onError("No Fill");
                    }
                    return;
                }
                AdLog.e("Load current source " + mNativeAdConfigList.get(currentLoadingIdx).source + " error : " + error);
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
            case AdConstants.NativeAdType.AD_SOURCE_MOPUB:
                return new MopubNativeAdapter(mContext, config.key);
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
            case AdConstants.NativeAdType.AD_SOURCE_MOPUB_INTERSTITIAL:
                return new MopubInterstitialAdapter(mContext, config.key);
            case AdConstants.NativeAdType.AD_SOURCE_VK:
            default:
                AdLog.e("not suppported source " + config.source);
                return null;
        }
    }
}
