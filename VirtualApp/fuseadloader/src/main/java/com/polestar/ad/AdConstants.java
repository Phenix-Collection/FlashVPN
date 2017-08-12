package com.polestar.ad;


/**
 * Created by guojia on 2016/12/11.
 */

public class AdConstants {
    public static boolean DEBUG = BuildConfig.DEBUG ;

    public static final class AdMob {
        public static final String FILTER_BOTH_INSTALL_AND_CONTENT = "both";
        public static final String FILTER_ONLY_INSTALL = "install";
        public static final String FILTER_ONLY_CONTENT = "content";
    }

    public static final class NativeAdType {
        public static final String AD_SOURCE_ADMOB_INSTALL = "ab_install";
        public static final String AD_SOURCE_ADMOB_CONTENT = "ab_content";
        public static final String AD_SOURCE_ADMOB = "ab";
        public static final String AD_SOURCE_FACEBOOK = "fb";
        public static final String AD_SOURCE_MOPUB = "mp";
        public static final String AD_SOURCE_FACEBOOK_INTERSTITIAL = "fb_interstitial";
        public static final String AD_SOURCE_ADMOB_INTERSTITIAL = "ab_interstitial";
        public static final String AD_SOURCE_MOPUB_INTERSTITIAL = "mp_interstitial";
        public static final String AD_SOURCE_ADMOB_NAVTIVE_BANNER = "ab_native_banner";
        public static final String AD_SOURCE_VK = "vk";
    }
}
