package com.polestar.ad.adapters;

import java.util.List;

/**
 * Created by guojia on 2016/10/31.
 */

public abstract class IAdLoadListener {
    protected abstract void onAdLoaded(IAdAdapter ad);
    protected void onAdClicked(IAdAdapter ad) {

    }
    protected void  onAdClosed(IAdAdapter ad) {

    }
    protected abstract void onAdListLoaded(List<IAdAdapter> ads);
    protected abstract void onError(String error);
    protected void onRewarded(IAdAdapter ad) {

    }
}
