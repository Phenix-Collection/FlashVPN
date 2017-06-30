package com.polestar.ad.adapters;

import java.util.List;

/**
 * Created by guojia on 2016/10/31.
 */

public interface IAdLoadListener {
    void onAdLoaded(IAdAdapter ad);
    void onAdListLoaded(List<IAdAdapter> ads);
    void onError(String error);
}
