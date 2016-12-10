package com.polestar.ad.adapters;


import android.view.View;

/**
 * Created by guojia on 2016/10/31.
 */

public abstract class NativeAd implements INativeAd {
    protected String mKey;
    protected long mLoadedTime = -1;
    protected int mShowCount = 0;

    @Override
    public long getLoadedTime() {
        return mLoadedTime;
    }

    @Override
    public int getShowCount() {
        return mShowCount;
    }

    @Override
    public boolean isShowed() {
        return mShowCount > 0;
    }

    @Override
    public void registerViewForInteraction(View view) {
        mShowCount ++;
    }

    @Override
    public String getAdType() {
        return "";
    }

    @Override
    public String getBody() {
        return null;
    }

    @Override
    public String getCoverImageUrl() {
        return null;
    }

    @Override
    public String getIconImageUrl() {
        return null;
    }

    @Override
    public String getSubtitle() {
        return null;
    }

    @Override
    public double getStarRating() {
        return 5.0;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getCallToActionText() {
        return null;
    }

    @Override
    public Object getAdObject() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getPrivacyIconUrl() {
        return null;
    }

    @Override
    public String getPlacementId() {
        return mKey;
    }


}
