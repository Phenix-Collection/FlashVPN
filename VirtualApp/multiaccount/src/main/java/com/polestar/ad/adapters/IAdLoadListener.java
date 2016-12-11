package com.polestar.ad.adapters;

import java.util.List;

/**
 * Created by guojia on 2016/10/31.
 */

public interface IAdLoadListener {
    void onAdLoaded(IAd ad);
    void onAdListLoaded(List<IAd> ads);
    void onError(String error);
}
