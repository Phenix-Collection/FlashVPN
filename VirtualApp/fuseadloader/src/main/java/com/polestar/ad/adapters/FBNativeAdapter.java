package com.polestar.ad.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.MediaView;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.view.StarLevelLayoutView;
import com.polestar.imageloader.widget.BasicLazyLoadImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guojia on 2016/10/31.
 */

public class FBNativeAdapter extends AdAdapter {

    private com.facebook.ads.NativeAd mRawAd ;
    private Context mContext;
    private IAdLoadListener mListener;

    public FBNativeAdapter(Context context, String key) {
        mContext = context;
        mKey = key;
    }
    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        if (AdConstants.DEBUG) {
            SharedPreferences sp = mContext.getSharedPreferences("FBAdPrefs", Context.MODE_PRIVATE);
            String deviceIdHash = sp.getString("deviceIdHash", "");
            AdSettings.addTestDevice(deviceIdHash);
            boolean isTestDevice = AdSettings.isTestMode(mContext);
            AdLog.d( "is FB Test Device ? "+deviceIdHash+" "+isTestDevice);
        }
        mRawAd = new com.facebook.ads.NativeAd(mContext, mKey);
        mListener = listener;
        mRawAd.setAdListener(new AdListener() {
            @Override
            public void onLoggingImpression(com.facebook.ads.Ad ad) {

            }

            @Override
            public void onError(com.facebook.ads.Ad ad, AdError adError) {
                if(mListener != null) {
                    mListener.onError(adError.getErrorMessage());
                }
                stopMonitor();
            }

            @Override
            public void onAdLoaded(com.facebook.ads.Ad ad) {
                mLoadedTime = System.currentTimeMillis();
                if(mListener != null) {
                    mListener.onAdLoaded(FBNativeAdapter.this);
                }
                stopMonitor();
            }

            @Override
            public void onAdClicked(com.facebook.ads.Ad ad) {
                //TODO
                if (mListener != null) {
                    mListener.onAdClicked(FBNativeAdapter.this);
                }
            }
        });
        mRawAd.loadAd();
        startMonitor();
    }

    @Override
    public String getAdType() {
        return AdConstants.NativeAdType.AD_SOURCE_FACEBOOK;
    }

    @Override
    public String getBody() {
        return mRawAd == null ? null : mRawAd.getAdBody();
    }

    @Override
    public String getCoverImageUrl() {
        return mRawAd == null ? null : mRawAd.getAdCoverImage().getUrl().toString();
    }

    @Override
    public String getIconImageUrl() {
        return mRawAd == null ? null : mRawAd.getAdIcon().getUrl().toString();
    }

    @Override
    public String getSubtitle() {
        return mRawAd == null ? null : mRawAd.getAdSubtitle();
    }

    @Override
    public double getStarRating() {
        return (mRawAd == null || mRawAd.getAdStarRating() == null) ? 0 : mRawAd.getAdStarRating().getValue();
    }

    @Override
    public String getTitle() {
        return mRawAd == null ? null : mRawAd.getAdTitle();
    }

    @Override
    public String getCallToActionText() {
        return mRawAd == null ? null : mRawAd.getAdCallToAction();
    }

    @Override
    public Object getAdObject() {
        return mRawAd;
    }

    @Override
    public String getId() {
        return mRawAd == null ? null : mRawAd.getId();
    }

    @Override
    public void registerViewForInteraction(View view) {
        super.registerViewForInteraction(view);
        if (mRawAd != null) {
            mRawAd.registerViewForInteraction(view);
        }
    }

    @Override
    public void registerPrivacyIconView(View view) {
        //AdChoicesView choicesView = new AdChoicesView(mContext, mRawAd, true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRawAd != null && mRawAd.getAdChoicesLinkUrl() != null) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(mRawAd.getAdChoicesLinkUrl());
                    intent.setData(content_url);
                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public String getPrivacyIconUrl() {
        return mRawAd == null? null : mRawAd.getAdChoicesIcon().getUrl().toString();
    }

    @Override
    public View getAdView(AdViewBinder viewBinder) {
        View adView = LayoutInflater.from(mContext).inflate(viewBinder.layoutId, null);
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        adView.setLayoutParams(params);
        if (adView != null) {
            MediaView  coverView = (MediaView) adView.findViewById(viewBinder.mainMediaId);
            coverView.setNativeAd(mRawAd);
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
            List<View> clickableViews = new ArrayList<>();
            if (coverView != null) {
                clickableViews.add(coverView);
            }
            if (iconView != null) {
                clickableViews.add(iconView);
            }
            if (titleView != null) {
                clickableViews.add(titleView);
            }
            if (subtitleView != null) {
                clickableViews.add(subtitleView);
            }
            if (ctaView != null) {
                clickableViews.add(ctaView);
            }
            mRawAd.registerViewForInteraction(adView,clickableViews);
            LinearLayout adChoicesContainer = (LinearLayout) adView.findViewById(viewBinder.privacyInformationId);
            AdChoicesView adChoicesView = new AdChoicesView(mContext, mRawAd, true);
            adChoicesContainer.addView(adChoicesView);
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
