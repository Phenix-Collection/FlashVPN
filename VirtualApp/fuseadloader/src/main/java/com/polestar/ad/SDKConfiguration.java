package com.polestar.ad;

import android.text.TextUtils;

import com.ironsource.mediationsdk.IronSource;
import com.mopub.common.MoPub;
import com.polestar.ad.adapters.FuseAdLoader;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by guojia on 2019/1/12.
 */

public class SDKConfiguration {
    public String admobAppId;
    public String mopubInitAdId;
    public Set<String> supportedFuseAdType;
    public String ironSourceAppKey;
    public boolean needReward;

    public boolean hasFAN() {
        return supportedFuseAdType.contains(AdConstants.AdType.AD_SOURCE_FACEBOOK)
                || supportedFuseAdType.contains(AdConstants.AdType.AD_SOURCE_FACEBOOK_INTERSTITIAL)
                || supportedFuseAdType.contains(AdConstants.AdType.AD_SOURCE_FB_REWARD);
    }

    public boolean hasAdmob() {
        return !TextUtils.isEmpty(admobAppId) && (supportedFuseAdType.contains(AdConstants.AdType.AD_SOURCE_ADMOB)
                || supportedFuseAdType.contains(AdConstants.AdType.AD_SOURCE_ADMOB_BANNER)
                || supportedFuseAdType.contains(AdConstants.AdType.AD_SOURCE_ADMOB_INTERSTITIAL)
                || supportedFuseAdType.contains(AdConstants.AdType.AD_SOURCE_ADMOB_REWARD));
    }

    public boolean hasMopub() {
        try {
            Class mopub = Class.forName(MoPub.class.getName());
            return mopub != null && !TextUtils.isEmpty(mopubInitAdId) && (supportedFuseAdType.contains(AdConstants.AdType.AD_SOURCE_MOPUB)
                    || supportedFuseAdType.contains(AdConstants.AdType.AD_SOURCE_MOPUB_INTERSTITIAL));
        }catch (Throwable ex){
            AdLog.e(ex);
        }
        return false;
    }

    public boolean hasIronSource() {
        try{
            Class iron = Class.forName(IronSource.class.getName());
            return  iron != null && !TextUtils.isEmpty(ironSourceAppKey)
                    && (supportedFuseAdType.contains(AdConstants.AdType.AD_SOURCE_IRONSOURCE_INTERSTITIAL)
            || supportedFuseAdType.contains(AdConstants.AdType.AD_SOURCE_IRONSOURCE_REWARD));
        } catch (Throwable ex) {
            AdLog.e(ex);
        }
        return false;
    }

    public boolean hasSupport(String adType) {
        return supportedFuseAdType.contains(adType);
    }

    private SDKConfiguration() {

    }

    static public class Builder{
        private SDKConfiguration configuration;
        public Builder() {
            configuration = new SDKConfiguration();
            configuration.supportedFuseAdType = new HashSet<>(FuseAdLoader.SUPPORTED_TYPES);
        }

        public SDKConfiguration build() {
            return configuration;
        }

        public Builder admobAppId(String s) {
            configuration.admobAppId = s;
            return  this;
        }

        public Builder disableAdType(String adType) {
            configuration.supportedFuseAdType.remove(adType);
            return this;
        }

        public Builder mopubAdUnit(String s) {
            configuration.mopubInitAdId =s ;
            return  this;
        }

        public Builder ironSourceAppKey(String key) {
            configuration.ironSourceAppKey = key;
            return this;
        }
    }

}
