package com.polestar.ad.adapters;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.ironsource.mediationsdk.IronSource;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.BuildConfig;
import com.polestar.ad.SDKConfiguration;
import com.polestar.imageloader.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * Created by guojia on 2016/11/15.
 */

public class FuseAdLoader {
    private Context mAppContext;
    private List<AdConfig> mNativeAdConfigList = new ArrayList();
    private HashMap<String, IAdAdapter> mNativeAdCache = new HashMap<>();
    private IAdLoadListener mListener;
    private int lastIdx =0;
    private String mSlot;
    private AdSize mBannerAdSize;
    private long mProtectOverTime = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mLoadingBits;
    private boolean mAdReturned;
    private static ConfigFetcher sConfigFetcher;
    private static SDKConfiguration sConfiguration;
    private static String sUserId;
    //Some SDK e.g. Mopub, IronSource needs to be initialized with Activity context
    private static boolean sInitializedWithActivity;

    private static HashMap<String, FuseAdLoader> sAdLoaderMap = new HashMap<>();
    public synchronized static FuseAdLoader get(String slot, Context context) {
        FuseAdLoader adLoader = sAdLoaderMap.get(slot);
        if (adLoader == null) {
            adLoader = new FuseAdLoader(slot, context.getApplicationContext());
            sAdLoaderMap.put(slot, adLoader);
        }
        if (context instanceof Activity && !sInitializedWithActivity){
            if(sConfiguration.hasMopub()){
                initMopub((Activity) context);
            }
            if(sConfiguration.hasIronSource()) {
                initIronSource((Activity) context);
            }
            sInitializedWithActivity = true;
        }
        return adLoader;
    }

    public static void setUserId(String userId) {
        sUserId = userId;
    }

    private static void initMopub (Activity activity) {
        SdkConfiguration.Builder builder = new SdkConfiguration.Builder(sConfiguration.mopubInitAdId);
        MoPub.initializeSdk(activity, builder.build(), new SdkInitializationListener() {
            @Override
            public void onInitializationFinished() {
                AdLog.d("Mopub initialized");
                MoPub.getPersonalInformationManager().grantConsent();
            }
        });
    }

    private static void initIronSource(Activity activity) {
        IronSource.init(activity, sConfiguration.ironSourceAppKey);
        AdLog.d("init IronSourcce " );
        if (BuildConfig.DEBUG) {
            IronSource.setAdaptersDebug(true);
        }
        IronSource.setConsent(true);
        if (!TextUtils.isEmpty(sUserId)) {
            IronSource.setUserId(sUserId);
        }
        IronSource.shouldTrackNetworkState(activity , true);
    }
//    public static void init( ConfigFetcher depends) {
//        sConfigFetcher = depends;
//    }

    public static void init (final ConfigFetcher depends, Context context, final SDKConfiguration configuration) {
        sConfigFetcher = depends;
        sConfiguration = configuration;
        if(sConfiguration.hasAdmob()) {
            MobileAds.initialize(context, configuration.admobAppId);
        }
        if (context instanceof Activity) {
            sInitializedWithActivity = true;
            if (sConfiguration.hasMopub()) {
                initMopub((Activity) context);
            }
            if (sConfiguration.hasIronSource()) {
                initIronSource((Activity) context);
            }
        }
        Application application = (Application) context.getApplicationContext();
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
//                if (sInitializedWithActivity && sConfiguration.hasIronSource()) {
//                    IronSource.onResume(activity);
//                }
            }

            @Override
            public void onActivityPaused(Activity activity) {
//                if (sInitializedWithActivity && sConfiguration.hasIronSource()) {
//                    IronSource.onPause(activity);
//                }
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

//    public static void initIronSource

    public interface ConfigFetcher {
        public boolean isAdFree();
        public List<AdConfig> getAdConfigList(String slot);
    }

    private FuseAdLoader(String slot, Context context) {
        this.mAppContext = context;
        mSlot = slot;
        List<AdConfig> adSources ;
        if (sConfigFetcher != null ) {
           adSources = sConfigFetcher.getAdConfigList(mSlot);
        }  else {
            adSources = new ArrayList<>(0);
        }
        addAdConfigList(adSources);
    }

    public static final HashSet<String> SUPPORTED_TYPES = new HashSet<>();

    public void preloadAd(Context context) {
        loadAd(context, 1, null);
    }

    public FuseAdLoader setBannerAdSize(AdSize adSize) {
        mBannerAdSize = adSize;
        return this;
    }

    public void loadAd(Context context, int burstNum, long protectTime, IAdLoadListener listener) {
        AdLog.d("FuseAdLoader :" + mSlot + " load ad: " + burstNum + " listener: " + listener);
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException("Load ad not from main thread");
        }
        if (sConfigFetcher.isAdFree()) {
            AdLog.d("FuseAdLoader : AD free version");
            if (listener != null) {
                listener.onError("AD free version");
            }
            return;
        }
        if ( burstNum  <= 0 || mNativeAdConfigList.size() == 0) {
            AdLog.d("FuseAdLoader :" + mSlot + " load num wrong: " + burstNum);
            if (listener != null) {
                listener.onError("Wrong config");
            }
            return;
        }
        if (burstNum == 1) { protectTime = 0;}
        mProtectOverTime = System.currentTimeMillis() + protectTime;
        mListener = listener;
        mAdReturned = false;
        lastIdx = 0;
        if (protectTime > 0) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null) {
                        IAdAdapter cache = getValidCache();
                        if (cache != null) {
                            mAdReturned = true;
                            cache.setAdListener(mListener);
                            mListener.onAdLoaded(cache);
                            //mListener = null;
                        }
                    }
                }
            }, protectTime);
        }
        for (int i = 0; i < burstNum; i ++ ) {
            if (loadNextNativeAd(context)) {
                AdLog.d("Stop burst as already find cache at: " + i);
                break;
            }
        }
    }

    private boolean isLoading (int idx) {
        return (mLoadingBits & (0x1 << idx)) != 0;
    }

    private void markLoading(int idx) {
        mLoadingBits |= (0x1 << idx);
    }

    private void finishLoading(Context context, int idx) {
        mLoadingBits &= (~(0x1 << idx));
        if (mAdReturned) {
            AdLog.d("Ad already returned " + mSlot);
            return;
        }
        long now = System.currentTimeMillis();
        IAdAdapter ad = getValidCache();
        if (ad == null ) {
            //need load next or no fill;
            AdLog.d("No valid ad returned " + mSlot);
            if (idx == mNativeAdConfigList.size() - 1) {
                boolean betterLoading = false;
                for (int i = idx - 1; i >= 0; i--) {
                    if (isLoading(i)){
                        betterLoading = true;
                        break;
                    }
                }
                if (!betterLoading && mListener != null) {
                    AdLog.d("Loaded all adapter, no fill in time");
                    mListener.onError("No Fill");
                    //In case ad loaded after time out
                    //mListener = null;
                }
            } else {
                loadNextNativeAd(context);
            }
        } else {
            // no need load next, fill or just wait timeout;
            int i ;
            for (i = idx -1; i >= 0; i --) {
                if (isLoading(i)) {
                    break;
                }
            }
            AdLog.d("loaded index: " + idx + " i: " + i + " wait: " + (now-mProtectOverTime));
            if (now >= mProtectOverTime || i < 0) {
                if (mListener != null) {
                    mAdReturned = true;
                    AdLog.d(mSlot + " return to " + mListener);
                    ad.setAdListener(mListener);
                    mListener.onAdLoaded(ad);
                    //mListener = null;
                }
            } else {
                AdLog.d("Wait for protect time over");
            }
        }
    }

    private int nextLoadingIdx() {
        return  lastIdx ++;
    }

    private IAdAdapter getValidCache() {
        for (AdConfig config: mNativeAdConfigList) {
            IAdAdapter cache = mNativeAdCache.get(config.key);
            if (cache != null) {
                if (cache.isShowed() || ((System.currentTimeMillis() - cache.getLoadedTime())/1000) > config.cacheTime) {
                    long delta = (System.currentTimeMillis() - cache.getLoadedTime())/1000;
                    AdLog.d("AdAdapter cache time out : " + delta + " config: " +config.cacheTime + " type: " + cache.getAdType());
                    mNativeAdCache.remove(config.key);
                } else {
                    return cache;
                }
            }
        }
        return null;
    }

    public boolean hasValidCache() {
        for (AdConfig config: mNativeAdConfigList) {
            if (hasValidCache(config)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasValidCache(AdConfig config) {
        IAdAdapter cache = mNativeAdCache.get(config.key);
        if (cache != null) {
            if (cache.isShowed() || ((System.currentTimeMillis() - cache.getLoadedTime())/1000) > config.cacheTime) {
                AdLog.d("AdAdapter cache time out : " + cache.getTitle() + " type: " + cache.getAdType());
                mNativeAdCache.remove(config.key);
            } else {
                return true;
            }
        }
        return false;
    }

    public void loadAd(Context context, int burstNum, IAdLoadListener listener) {
        AdLog.d("load " + mSlot + " listen: " + listener);
        loadAd(context, burstNum, 0 , listener);
    }

    public void addAdConfig(AdConfig adConfig) {
        if (adConfig != null && !TextUtils.isEmpty(adConfig.source) && !TextUtils.isEmpty(adConfig.key)) {
            if (sConfiguration.supportedFuseAdType.contains(adConfig.source)) {
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

    class IndexAdListener implements IAdLoadListener {
        int index;
        Context loadingContext;
        public IndexAdListener(Context context, int index) {
            this.index = index;
            loadingContext = context;
        }

        @Override
        public void onRewarded(IAdAdapter ad) {
            if(mListener != null) {
                mListener.onRewarded(ad);
            }
        }

        @Override
        public void onAdLoaded(IAdAdapter ad) {
            mNativeAdCache.put(mNativeAdConfigList.get(index).key, ad);
            AdLog.d(mSlot + " ad loaded " + ad.getAdType() + " index: " + index);
            if (ad.getCoverImageUrl() != null) {
                AdLog.d("preload " + ad.getCoverImageUrl());
                ImageLoader.getInstance().doPreLoad(mAppContext, ad.getCoverImageUrl());
            }
            if (ad.getIconImageUrl() != null) {
                AdLog.d("preload " + ad.getIconImageUrl());
                ImageLoader.getInstance().doPreLoad(mAppContext, ad.getIconImageUrl());
            }
            finishLoading(loadingContext, index);
        }

        @Override
        public void onAdClicked(IAdAdapter ad) {
            if(mListener != null) {
                mListener.onAdClicked(ad);
            }
        }

        @Override
        public void onAdClosed(IAdAdapter ad) {
            if(mListener != null) {
                AdLog.d("Ad closed");
                mListener.onAdClosed(ad);
            }
        }

        @Override
        public void onAdListLoaded(List<IAdAdapter> ads) {
            //not support list yet
        }

        @Override
        public void onError(String error) {
            AdLog.e("Load current source " + mNativeAdConfigList.get(index).source + " error : " + error);
            finishLoading(loadingContext, index);
        }

    }
    private boolean loadNextNativeAd(Context context) {
        final int idx = nextLoadingIdx();
        if (idx < 0 || idx >= mNativeAdConfigList.size()) {
            AdLog.d(mSlot + " tried to load all source . Index : " + idx);
            return false;
        }
        if (isLoading(idx)) {
            AdLog.d(mSlot + " already loading . Index : " + idx);
            return false;
        }
        AdLog.d("loadNextNativeAd for " + idx);
        markLoading(idx);
        AdConfig config = mNativeAdConfigList.get(idx);
        if (hasValidCache(config)) {
            AdLog.d(mSlot + " already have cache for : " + config.key);
            finishLoading(context, idx);
            return true;
        }
        //Do load
        IAdAdapter loader = getNativeAdAdapter(config);
        if (loader == null) {
            finishLoading(context, idx);
            return false;
        }
        AdLog.d(mSlot + " start load for : " + config.source + " index : " + idx );
        loader.loadAd(context, 1, new IndexAdListener(context, idx));
        return false;
    }

    private IAdAdapter getNativeAdAdapter(AdConfig config){
        if (config == null || config.source == null) {
            return null;
        }
        if (!sConfiguration.hasSupport(config.source)) {
            return null;
        }
        try {
            switch (config.source) {
                case AdConstants.AdType.AD_SOURCE_ADMOB:
                    return new AdmobNativeAdapter(mAppContext, config.key);
                case AdConstants.AdType.AD_SOURCE_MOPUB:
                    return new MopubNativeAdapter(mAppContext, config.key);
                case AdConstants.AdType.AD_SOURCE_ADMOB_BANNER:
                    AdSize bannerSize = config.bannerAdSize == null ? mBannerAdSize : config.bannerAdSize;
                    return bannerSize == null ? null : new AdmobBannerAdapter(mAppContext, config.key, bannerSize);
                case AdConstants.AdType.AD_SOURCE_FACEBOOK:
                    return new FBNativeAdapter(mAppContext, config.key);
                case AdConstants.AdType.AD_SOURCE_FACEBOOK_INTERSTITIAL:
                    return new FBInterstitialAdapter(mAppContext, config.key);
                case AdConstants.AdType.AD_SOURCE_ADMOB_INTERSTITIAL:
                    return new AdmobInterstitialAdapter(mAppContext, config.key);
//            case AdConstants.AdType.AD_SOURCE_BT_INTERSTITIAL:
//                return new BtInterstitialAdapter(mAppContext, config.key);
                case AdConstants.AdType.AD_SOURCE_MOPUB_INTERSTITIAL:
                    return new MopubInterstitialAdapter(mAppContext, config.key);
//            case AdConstants.AdType.AD_SOURCE_BT :
//                return new BtNativeAdapter(mAppContext, config.key);
                case AdConstants.AdType.AD_SOURCE_ADMOB_REWARD:
                    return new AdmobRewardVideoAdapter(mAppContext, config.key);
                case AdConstants.AdType.AD_SOURCE_FB_REWARD:
                    return new FBRewardVideoAdapter(mAppContext, config.key);
                case AdConstants.AdType.AD_SOURCE_IRONSOURCE_REWARD:
                    return new IronSourceRewardVideoAdapter(mAppContext, config.key);
                case AdConstants.AdType.AD_SOURCE_IRONSOURCE_INTERSTITIAL:
                    return new IronSourceInterstitialAdapter(mAppContext, config.key);
                default:
                    AdLog.e("not suppported source " + config.source);
                    return null;
            }
        }catch (Throwable ex) {
            AdLog.e("Error to get loader for " +config);
            return null;
        }
    }

    static {
        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_ADMOB);
        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_ADMOB_INTERSTITIAL);
//        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_BT_INTERSTITIAL);
        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_FACEBOOK);
//        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_BT);
        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_FACEBOOK_INTERSTITIAL);
        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_MOPUB);
        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_MOPUB_INTERSTITIAL);
        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_ADMOB_BANNER);
        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_ADMOB_REWARD);
        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_FB_REWARD);
        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_IRONSOURCE_INTERSTITIAL);
        SUPPORTED_TYPES.add(AdConstants.AdType.AD_SOURCE_IRONSOURCE_REWARD);
    }
}
