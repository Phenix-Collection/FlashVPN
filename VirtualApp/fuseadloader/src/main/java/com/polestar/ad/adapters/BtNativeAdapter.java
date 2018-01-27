package com.polestar.ad.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.batmobi.Ad;
import com.batmobi.AdError;
import com.batmobi.BatAdBuild;
import com.batmobi.BatAdType;
import com.batmobi.BatNativeAd;
import com.batmobi.BatmobiLib;
import com.batmobi.IAdListener;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.view.StarLevelLayoutView;
import com.polestar.imageloader.widget.BasicLazyLoadImageView;

/**
 * Created by guojia on 2018/1/27.
 */

public class BtNativeAdapter extends AdAdapter {

    private Context mContext;
    private IAdLoadListener mListener;
    private Ad mRawAd = null;
    private BatNativeAd mBatNativeAd = null;

    public BtNativeAdapter(Context context, String key) {
        mContext = context;
        mKey = key;
    }

    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        mListener = listener;
        AdLog.d("BtNativeAdapter loadAd " + listener);
        IAdListener btListener = new IAdListener(){
            @Override
            public void onAdLoadFinish(Object o) {
                mLoadedTime = System.currentTimeMillis();
                mBatNativeAd = (BatNativeAd) o;
                mRawAd = mBatNativeAd.getAds().get(0);
                if(mBatNativeAd != null && mRawAd != null && mListener != null) {
                    mListener.onAdLoaded(BtNativeAdapter.this);
                } else if (mListener != null) {
                    mListener.onError("No available ad");
                }
                stopMonitor();
                AdLog.d("BT: onAdLoadFinish");
            }

            @Override
            public void onAdError(AdError adError) {
                if(mListener != null) {
                    mListener.onError(adError.getMsg());
                }
                stopMonitor();
                AdLog.d("BT: onAdError " + adError.getMsg());
            }

            @Override
            public void onAdShowed() {
                AdLog.d("BT: onAdShowed");
            }

            @Override
            public void onAdClosed() {
                AdLog.d("BT: onAdClosed");
            }

            @Override
            public void onAdClicked() {
                AdLog.d("BT: onAdClicked");
            }
        };
        BatAdBuild.Builder builder = new BatAdBuild.Builder(mContext, mKey, BatAdType.NATIVE.getType(), btListener)
                .setAdsNum(1).setCreatives(Ad.AD_CREATIVE_SIZE_320X200);
        BatmobiLib.load(builder.build());
        startMonitor();
    }


    @Override
    public String getAdType() {
        return AdConstants.NativeAdType.AD_SOURCE_BT;
    }

    @Override
    public String getBody() {
        return mRawAd == null ? null : mRawAd.getDescription();
    }

    @Override
    public String getCoverImageUrl() {
        return mRawAd == null ? null : mRawAd.getCreatives(Ad.AD_CREATIVE_SIZE_320X200).get(0);
    }

    @Override
    public String getIconImageUrl() {
        return mRawAd == null ? null : mRawAd.getIcon();
    }

    @Override
    public String getSubtitle() {
        return mRawAd == null ? null : mRawAd.getRecommendMessage();
    }

    @Override
    public double getStarRating() {
        return mRawAd == null  ? 0 : mRawAd.getStoreRating();
    }

    @Override
    public String getTitle() {
        return mRawAd == null ? null : mRawAd.getName();
    }

    @Override
    public String getCallToActionText() {
        return mRawAd == null ? null : mRawAd.getAdCallToAction();
    }

    @Override
    public Object getAdObject() {
        return mBatNativeAd;
    }

    @Override
    public String getId() {
        return mRawAd == null ? null : mRawAd.getCampId();
    }

    @Override
    public void registerViewForInteraction(View view) {
        super.registerViewForInteraction(view);
        if (mBatNativeAd != null) {
            mBatNativeAd.registerView(view, mRawAd);
        }
    }

    @Override
    public void registerPrivacyIconView(View view) {
        //AdChoicesView choicesView = new AdChoicesView(mContext, mRawAd, true);
    }

    @Override
    public String getPrivacyIconUrl() {
        return null;
    }

    @Override
    public View getAdView(AdViewBinder viewBinder) {
        View adView = LayoutInflater.from(mContext).inflate(viewBinder.layoutId, null);
        if (adView != null) {
            BasicLazyLoadImageView  coverView = (BasicLazyLoadImageView) adView.findViewById(viewBinder.mainMediaId);
            if (coverView != null) {
                coverView.setDefaultResource(0);
                coverView.requestDisplayURL(getIconImageUrl());
            }
            ImageView iconView = (ImageView) adView.findViewById(viewBinder.iconImageId);
            if (iconView instanceof BasicLazyLoadImageView) {
                BasicLazyLoadImageView lazyLoadImageView = (BasicLazyLoadImageView) iconView;
                lazyLoadImageView.setDefaultResource(0);
                lazyLoadImageView.requestDisplayURL(getIconImageUrl());
            }
            TextView titleView = (TextView) adView.findViewById(viewBinder.titleId);
            titleView.setText(getTitle());
            TextView subtitleView = (TextView) adView.findViewById(viewBinder.textId);
            subtitleView.setText(getBody());
            TextView ctaView = (TextView) adView.findViewById(viewBinder.callToActionId);
            ctaView.setText(getCallToActionText());

            if (viewBinder.starLevelLayoutId != -1) {
                StarLevelLayoutView starLevelLayout = (StarLevelLayoutView) adView.findViewById(viewBinder.starLevelLayoutId);
                if (starLevelLayout != null && getStarRating() != 0) {
                    starLevelLayout.setRating((int) getStarRating());
                }
            }
            registerViewForInteraction(adView);
        }
        return  adView;
    }

    @Override
    protected void onTimeOut() {
        if (mListener != null) {
            mListener.onError("TIME_OUT");
        }
    }
}
