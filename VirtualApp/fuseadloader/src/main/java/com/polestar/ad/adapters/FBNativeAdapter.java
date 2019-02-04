package com.polestar.ad.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdSettings;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.formats.NativeAdOptions;
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

    private NativeAd mRawAd ;

    public FBNativeAdapter(Context context, String key) {
        mKey = key;
    }
    @Override
    public void loadAd(Context context, int num, IAdLoadListener listener) {
        if (AdConstants.DEBUG) {
            SharedPreferences sp = context.getSharedPreferences("FBAdPrefs", Context.MODE_PRIVATE);
            String deviceIdHash = sp.getString("deviceIdHash", "");
            AdSettings.addTestDevice(deviceIdHash);
            boolean isTestDevice = AdSettings.isTestMode(context);
            AdLog.d( "is FB Test Device ? "+deviceIdHash+" "+isTestDevice);
        }
        mRawAd = new NativeAd(context, mKey);
        adListener = listener;

        mRawAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                AdLog.d("FB onMediaDownloaded");

            }

            @Override
            public void onLoggingImpression(com.facebook.ads.Ad ad) {

                AdLog.d("FB onLoggingImpression");

            }

            @Override
            public void onError(com.facebook.ads.Ad ad, AdError adError) {
                AdLog.d("FB onError");

                if(adListener != null) {
                    adListener.onError(adError.getErrorMessage());
                }
                stopMonitor();
            }

            @Override
            public void onAdLoaded(com.facebook.ads.Ad ad) {
                AdLog.d("FB onAdLoaded");

                if(ad == null || ad!= mRawAd) {
                    AdLog.d("FB onAdLoaded race condition");
                }

                mLoadedTime = System.currentTimeMillis();
                if(adListener != null) {
                    adListener.onAdLoaded(FBNativeAdapter.this);
                }
                stopMonitor();
            }

            @Override
            public void onAdClicked(com.facebook.ads.Ad ad) {
                //TODO
                AdLog.d("FB onAdClicked");
                if (adListener != null) {
                    adListener.onAdClicked(FBNativeAdapter.this);
                }
            }
        });
        mRawAd.loadAd(NativeAd.MediaCacheFlag.ALL);

        startMonitor();
    }

    @Override
    public String getAdType() {
        return AdConstants.AdType.AD_SOURCE_FACEBOOK;
    }

    @Override
    public String getBody() {
        return mRawAd == null ? null : mRawAd.getAdBodyText();
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
        return mRawAd == null ? null : mRawAd.getAdBodyText();
    }

    @Override
    public double getStarRating() {
        return (mRawAd == null || mRawAd.getAdStarRating() == null) ? 0 : mRawAd.getAdStarRating().getValue();
    }

    @Override
    public String getTitle() {
        return mRawAd == null ? null : mRawAd.getAdHeadline();
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
//        if (mRawAd != null) {
//            mRawAd.registerViewForInteraction(view);
//        }
    }

    @Override
    public void registerPrivacyIconView(final View view) {
        //AdChoicesView choicesView = new AdChoicesView(mContext, mRawAd, true);
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mRawAd != null && mRawAd.getAdChoicesLinkUrl() != null) {
//                    Intent intent = new Intent();
//                    intent.setAction("android.intent.action.VIEW");
//                    Uri content_url = Uri.parse(mRawAd.getAdChoicesLinkUrl());
//                    intent.setData(content_url);
//                    intent.setFlags(
//                            Intent.FLAG_ACTIVITY_NEW_TASK
//                                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                    view.getContext().startActivity(intent);
//                }
//            }
//        });
    }

    @Override
    public String getPrivacyIconUrl() {
        return null;
//        return mRawAd == null? null : mRawAd.getAdChoicesIcon().getUrl().toString();
    }

    @Override
    public View getAdView(Context context, AdViewBinder viewBinder) {
        NativeAdLayout adView = new NativeAdLayout(context);

         View inflateView = LayoutInflater.from(context).inflate(viewBinder.layoutId, null);
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        adView.setLayoutParams(params);
        adView.addView(inflateView);
//        // Create and add Facebook's AdOptions to the overlay view.
//        ((ViewGroup)inflateView).addView(adOptionsView);
//        // We know that the overlay view is a FrameLayout, so we get the FrameLayout's
//        // LayoutParams from the AdOptionsView.
//        FrameLayout.LayoutParams params =
//                (FrameLayout.LayoutParams) adOptionsView.getLayoutParams();
//
//            params.gravity = Gravity.TOP | Gravity.RIGHT;
        if (adView != null) {
            View main = adView.findViewById(viewBinder.mainMediaId);
            MediaView  coverView;
            if (main instanceof MediaView) {
                coverView = (MediaView)main;
            } else if (viewBinder.fbMediaId != -1) {
                coverView =(MediaView) adView.findViewById(viewBinder.fbMediaId);
            } else {
                AdLog.e("Wrong layoutid " + viewBinder.layoutId);
                return null;
            }
            coverView.setVisibility(View.VISIBLE);
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
            List<View> clickableViews = new ArrayList<>();
//            if (coverView != null) {
//                clickableViews.add(coverView);
//            }
//            if (iconView != null) {
//                clickableViews.add(iconView);
//            }
            if (titleView != null) {
                clickableViews.add(titleView);
            }
            if (subtitleView != null) {
                clickableViews.add(subtitleView);
            }
            if (ctaView != null) {
                clickableViews.add(ctaView);
            }
            LinearLayout adChoicesContainer = (LinearLayout) adView.findViewById(viewBinder.privacyInformationId);
            if (adChoicesContainer != null) {
                AdOptionsView adOptionsView = new AdOptionsView(context,mRawAd,adView);
                adChoicesContainer.addView(adOptionsView);
            }
//            mRawAd.registerViewForInteraction(inflateView,coverView, iconView, clickableViews);
            mRawAd.registerViewForInteraction(inflateView,coverView, iconView);
            registerViewForInteraction(adView);

            adView.requestLayout();

        }
        return  adView;
    }

    @Override
    protected void onTimeOut() {
        if (adListener != null) {
            adListener.onError("TIME_OUT");
        }
    }
}
