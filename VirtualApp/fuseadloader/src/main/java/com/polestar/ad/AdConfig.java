package com.polestar.ad;

import com.google.android.gms.ads.AdSize;

/**
 * Created by guojia on 2016/12/18.
 */

public class AdConfig {
    public String key;
    public String source;
    public long cacheTime;
    public AdSize bannerAdSize;
    public final static int BANNER = 0;
    public final static int LARGE_BANNER = 1;
    public final static int MEDIUM_RECTANGLE = 2;
    public final static int SMART_BANNER = 3;
    public AdConfig(String source, String key, long cacheTime) {
        this.key = key;
        this.source = source;
        this.cacheTime = cacheTime;
    }
    public AdConfig(String source, String key, long cacheTime, int bannerType) {
        this.key = key;
        this.source = source;
        this.cacheTime = cacheTime;
        switch (bannerType) {
            case BANNER:
                this.bannerAdSize = AdSize.BANNER;
                break;
            case LARGE_BANNER:
                this.bannerAdSize = AdSize.LARGE_BANNER;
                break;
            case MEDIUM_RECTANGLE:
                this.bannerAdSize = AdSize.MEDIUM_RECTANGLE;
                break;
            case SMART_BANNER:
                this.bannerAdSize = AdSize.SMART_BANNER;
                break;
        }
    }
    public String toString () {
        return "source: " + source + " key:" + key + " cache_time:" + cacheTime;
    }
}
