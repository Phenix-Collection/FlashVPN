package com.polestar.ad;

/**
 * Created by guojia on 2016/12/18.
 */

public class AdConfig {
    public String key;
    public String source;
    public long cacheTime;
    public AdConfig(String source, String key, long cacheTime) {
        this.key = key;
        this.source = source;
        this.cacheTime = cacheTime;
    }
    public String toString () {
        return "source: " + source + " key:" + key + " cache_time:" + cacheTime;
    }
}
