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
        public static final String AD_SOURCE_ADMOB_INSTALL = "adm_install";
        public static final String AD_SOURCE_ADMOB_CONTENT = "adm_content";
        public static final String AD_SOURCE_ADMOB = "adm";
        public static final String AD_SOURCE_FACEBOOK = "fb";
        public static final String AD_SOURCE_MOPUB = "mp";
        public static final String AD_SOURCE_FACEBOOK_INTERSTITIAL = "fb_interstitial";
        public static final String AD_SOURCE_ADMOB_INTERSTITIAL = "ab_interstitial";
        public static final String AD_SOURCE_BT_INTERSTITIAL = "bt_interstitial";
        public static final String AD_SOURCE_MOPUB_INTERSTITIAL = "mp_interstitial";
        public static final String AD_SOURCE_ADMOB_BANNER = "ab_banner";
        public static final String AD_SOURCE_BT = "bt";
        public static final String AD_SOURCE_FB_REWARD = "fb_reward";
        public static final String AD_SOURCE_ADMOB_REWARD = "adm_reward";
    }
}
