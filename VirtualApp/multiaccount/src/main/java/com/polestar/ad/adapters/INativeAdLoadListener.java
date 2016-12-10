package com.polestar.ad.adapters;

import java.util.List;

/**
 * Created by guojia on 2016/10/31.
 */

public interface INativeAdLoadListener {
    void onAdLoaded(INativeAd ad);
    void onAdListLoaded(List<INativeAd> ads);
    void onError(String error);
}
