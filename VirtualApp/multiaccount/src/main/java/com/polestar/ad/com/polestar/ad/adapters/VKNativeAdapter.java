package com.polestar.ad.com.polestar.ad.adapters;

import android.content.Context;
import android.view.View;

import nativesdk.ad.adsdk.app.Constants;

/**
 * Created by guojia on 2016/10/31.
 */

public class VKNativeAdapter extends NativeAd implements INativeAdLoader{

    private Context mContext;
    private com.my.target.nativeads.NativeAd mRawAd;

    private INativeAdLoadListener mListener;

    public VKNativeAdapter(Context context, String key) {
        mKey = key;
        mContext = context;
    }
    @Override
    public void registerViewForInteraction(View view) {
        super.registerViewForInteraction(view);
        if (mRawAd != null) {
            mRawAd.registerView(view);
        }
    }

    @Override
    public String getAdType() {
        return Constants.NativeAdType.AD_SOURCE_VK;
    }

    @Override
    public String getCoverImageUrl() {
        if (mRawAd != null) {
            return mRawAd.getBanner() == null ? null : mRawAd.getBanner().getImage().getUrl();
        }
        return null;
    }

    @Override
    public String getIconImageUrl() {
        if (mRawAd != null) {
            return mRawAd.getBanner() == null ? null : mRawAd.getBanner().getIcon().getUrl();
        }
        return null;
    }

    @Override
    public String getSubtitle() {
        if (mRawAd != null) {
            return mRawAd.getBanner() == null ? null : mRawAd.getBanner().getDescription();
        }
        return null;
    }

    @Override
    public double getStarRating() {
        if (mRawAd != null) {
            return mRawAd.getBanner() == null ? 5.0 : mRawAd.getBanner().getRating();
        }
        return 5.0;
    }

    @Override
    public String getTitle() {
        if (mRawAd != null) {
            return mRawAd.getBanner() == null ? null : mRawAd.getBanner().getTitle();
        }
        return null;
    }

    @Override
    public String getCallToActionText() {
        if (mRawAd != null) {
            return mRawAd.getBanner() == null ? null : mRawAd.getBanner().getCtaText();
        }
        return null;
    }

    @Override
    public String getBody() {
        if (mRawAd != null) {
            return mRawAd.getBanner() == null ? null : mRawAd.getBanner().getDescription();
        }
        return null;
    }

    @Override
    public Object getAdObject() {
        return mRawAd;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void loadAd(int num, INativeAdLoadListener listener) {
        if ( listener == null) {
            return;
        }
        mListener = listener;
        if (mKey == null) {
            mListener.onError("No slot_id");
            return;
        }
        int slot_id ;
        try {
            slot_id = Integer.valueOf(mKey);
        } catch (Exception e) {
            mListener.onError("Wrong slot_id");
            return;
        }
        mRawAd = new com.my.target.nativeads.NativeAd(slot_id, mContext);
        //TODO whether we need load image ourselves
        //note: if set to auto load, it will decode image into bitmap and cache in memory
        mRawAd.setAutoLoadImages(true);
        mRawAd.setListener(new com.my.target.nativeads.NativeAd.NativeAdListener() {

            @Override
            public void onLoad(com.my.target.nativeads.NativeAd nativeAd) {
                mRawAd = nativeAd;
                mLoadedTime = System.currentTimeMillis();
                mListener.onAdLoaded(VKNativeAdapter.this);
            }

            @Override
            public void onNoAd(String s, com.my.target.nativeads.NativeAd nativeAd) {
                mListener.onError("No Ad");
            }

            @Override
            public void onClick(com.my.target.nativeads.NativeAd nativeAd) {

            }
        });
        mRawAd.load();
    }

    @Override
    public void registerPrivacyIconView(View view) {

    }
}
