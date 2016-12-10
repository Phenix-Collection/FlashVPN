package com.polestar.ad.adapters;

import android.view.View;

/**
 * Created by guojia on 2016/10/31.
 */

public interface INativeAd {
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
}
