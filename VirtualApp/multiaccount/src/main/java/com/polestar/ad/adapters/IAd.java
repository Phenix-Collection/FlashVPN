package com.polestar.ad.adapters;

import android.view.View;

import com.polestar.ad.AdViewBinder;

/**
 * Created by guojia on 2016/10/31.
 */

public interface IAd {
    String getAdType();

    String getCoverImageUrl();

    String getIconImageUrl();

    String getSubtitle();

    double getStarRating();

    String getTitle();

    String getCallToActionText();

    Object getAdObject();

    String getId();

    String getBody();

    void registerViewForInteraction(View view);

    void registerPrivacyIconView(View view);

    String getPrivacyIconUrl();

    String getPlacementId();

    int getShowCount();

    boolean isShowed();

    long getLoadedTime();

    void show();

    void destroy();

    View getAdView(AdViewBinder viewBinder);
}
