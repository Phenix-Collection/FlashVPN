package com.polestar.ad.adapters;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.ads.mediation.facebook.FacebookAdapter;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.polestar.ad.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAdView;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.view.StarLevelLayoutView;
import com.polestar.imageloader.widget.BasicLazyLoadImageView;


/**
 * Created by guojia on 2016/10/31.
 */

public class AdmobNativeAdapter extends AdAdapter {

    private String mFilter;
    private Context mContext;

    private com.google.android.gms.ads.formats.NativeAd mRawAd;

    public AdmobNativeAdapter(Context context, String key) {
        mContext = context;
        mKey = key;
    }

    public void setFilter(String filter) {
        mFilter = filter;
    }
    @Override
    public void loadAd(int num, IAdLoadListener listener) {
        if (listener == null) {
            AdLog.e("listener not set.");
            return;
        }
        adListener = listener;
        if (num > 1) {
            AdLog.d("Admob not support load for more than 1 ads. Only return 1 ad");
        }
        AdLoader.Builder  builder = new AdLoader.Builder(mContext, mKey);
        boolean isContent = false;
        boolean isInstall = false;
        if (TextUtils.isEmpty(mFilter) || mFilter.equals(AdConstants.AdMob.FILTER_BOTH_INSTALL_AND_CONTENT)){
            isContent = true;
            isInstall = true;
        } else if (mFilter.equals(AdConstants.AdMob.FILTER_ONLY_CONTENT)){
            isContent = true;
        }else if (mFilter.equals(AdConstants.AdMob.FILTER_ONLY_INSTALL)) {
            isInstall = true;
        }

        if (isContent) {
            builder.forContentAd(new NativeContentAd.OnContentAdLoadedListener() {
                @Override
                public void onContentAdLoaded(NativeContentAd nativeContentAd) {
                    if (isValidAd(nativeContentAd)) {
                        postOnAdLoaded(nativeContentAd);
                    } else {
                        postOnAdLoadFail(999);
                    }
                }
            });
        }
        if (isInstall) {
            builder.forAppInstallAd(new NativeAppInstallAd.OnAppInstallAdLoadedListener() {
                @Override
                public void onAppInstallAdLoaded(NativeAppInstallAd nativeAppInstallAd) {
                    if (isValidAd(nativeAppInstallAd)) {
                        postOnAdLoaded(nativeAppInstallAd);
                    } else {
                        postOnAdLoadFail(999);
                    }
                }
            });
        }

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions).setRequestMultipleImages(false).setReturnUrlsForImageAssets(true)
                .build();
        builder.withNativeAdOptions(adOptions);
        builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                postOnAdLoadFail(i);
            }
        });
        AdLoader adLoader = builder.build();
        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID(mContext);
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            adLoader.loadAd(request);
            boolean isTestDevice = request.isTestDevice(mContext);
            AdLog.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
        } else {
            adLoader.loadAd(new AdRequest.Builder().build());
        }
        startMonitor();
    }

    private void postOnAdLoaded(com.google.android.gms.ads.formats.NativeAd ad) {
        mRawAd = ad;
        mLoadedTime = System.currentTimeMillis();
        if (adListener != null) {
            adListener.onAdLoaded(this);
        }
        stopMonitor();
    }

    private void postOnAdLoadFail(int i) {
        if (adListener != null) {
            adListener.onError("" + i);
        }
        stopMonitor();
    }

    @Override
    public void registerPrivacyIconView(View view) {
    }

    @Override
    public String getAdType() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return AdConstants.NativeAdType.AD_SOURCE_ADMOB_INSTALL;
        }
        if (mRawAd instanceof NativeContentAd) {
            return AdConstants.NativeAdType.AD_SOURCE_ADMOB_INSTALL;
        }
        return null;
    }

    @Override
    public String getCoverImageUrl() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return ((NativeAppInstallAd) mRawAd).getImages().get(0).getUri().toString();
        }
        if (mRawAd instanceof NativeContentAd) {
            return((NativeContentAd) mRawAd).getImages().get(0).getUri().toString();
        }
        return null;
    }

    @Override
    public String getIconImageUrl() {
        if (mRawAd instanceof NativeAppInstallAd && ((NativeAppInstallAd) mRawAd).getIcon() != null) {
            return ((NativeAppInstallAd) mRawAd).getIcon().getUri().toString();
        }
        if (mRawAd instanceof NativeContentAd
                && ((NativeContentAd) mRawAd).getLogo() != null) {
            return ((NativeContentAd) mRawAd).getLogo().getUri().toString();
        }
        return  null;
    }

    @Override
    public String getBody() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return((NativeAppInstallAd) mRawAd).getBody()!=null?((NativeAppInstallAd) mRawAd).getBody().toString():null;
        }
        if (mRawAd instanceof NativeContentAd) {
            return((NativeContentAd) mRawAd).getBody()!=null?((NativeContentAd) mRawAd).getBody().toString():null;
        }
        return  null;
    }

    @Override
    public String getSubtitle() {
        if (mRawAd instanceof NativeAppInstallAd) {
            Bundle extras = ((NativeAppInstallAd) mRawAd).getExtras();
            if (extras.containsKey(FacebookAdapter.KEY_SUBTITLE_ASSET)) {
                return extras.getString(FacebookAdapter.KEY_SUBTITLE_ASSET,"");
            }
            return((NativeAppInstallAd) mRawAd).getBody()!=null?((NativeAppInstallAd) mRawAd).getBody().toString():null;
        }
        if (mRawAd instanceof NativeContentAd) {
            Bundle extras = ((NativeContentAd) mRawAd).getExtras();
            if (extras.containsKey(FacebookAdapter.KEY_SUBTITLE_ASSET)) {
                return extras.getString(FacebookAdapter.KEY_SUBTITLE_ASSET,"");
            }
            return((NativeContentAd) mRawAd).getBody()!=null?((NativeContentAd) mRawAd).getBody().toString():null;
        }
        return null;
    }

    @Override
    public double getStarRating() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return ((NativeAppInstallAd) mRawAd).getStarRating();
        }
        return super.getStarRating();
    }

    @Override
    public String getTitle() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return ((NativeAppInstallAd) mRawAd).getHeadline() != null ? ((NativeAppInstallAd) mRawAd).getHeadline().toString() : null;
        }
        if (mRawAd instanceof NativeContentAd) {
            return ((NativeContentAd) mRawAd).getHeadline() != null ? ((NativeContentAd) mRawAd).getHeadline().toString() : null;
        }
        return null;
    }

    @Override
    public String getCallToActionText() {
        if (mRawAd instanceof NativeAppInstallAd) {
            return((NativeAppInstallAd) mRawAd).getCallToAction() != null? ((NativeAppInstallAd) mRawAd).getCallToAction().toString() : null;
        }
        if (mRawAd instanceof NativeContentAd) {
            return((NativeContentAd) mRawAd).getCallToAction() != null? ((NativeContentAd) mRawAd).getCallToAction().toString() : null;
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
    protected void onTimeOut() {
        if (adListener != null) {
            adListener.onError("TIME_OUT");
        }
    }

    private boolean isValidAd(NativeAd ad) {
        if ( ad instanceof NativeContentAd) {
            NativeContentAd contentAd = (NativeContentAd) ad;
            return (contentAd.getHeadline() != null && contentAd.getBody() != null
                    && contentAd.getImages() != null && contentAd.getImages().size() > 0
                    && contentAd.getImages().get(0) != null
                    && contentAd.getCallToAction() != null);
        } else if (ad instanceof NativeAppInstallAd){
            NativeAppInstallAd appInstallAd = (NativeAppInstallAd) ad;
            return (appInstallAd.getHeadline() != null && appInstallAd.getBody() != null
                    && appInstallAd.getImages() != null && appInstallAd.getImages().size() > 0
                    && appInstallAd.getImages().get(0) != null
                    && appInstallAd.getCallToAction() != null);
        }
        return false;
    }

    @Override
    public View getAdView(AdViewBinder viewBinder) {
        View actualAdView = LayoutInflater.from(mContext).inflate(viewBinder.layoutId, null);
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        adView.setLayoutParams(params);
        NativeAdView nativeAdView = null;
        if (actualAdView != null) {
            ImageView coverView = (ImageView) actualAdView.findViewById(viewBinder.mainMediaId);
            ImageView iconView = (ImageView) actualAdView.findViewById(viewBinder.iconImageId);
            if (iconView instanceof BasicLazyLoadImageView) {
                BasicLazyLoadImageView lazyLoadImageView = (BasicLazyLoadImageView) iconView;
                lazyLoadImageView.setDefaultResource(0);
                lazyLoadImageView.requestDisplayURL(getIconImageUrl());
            }

            TextView titleView = (TextView) actualAdView.findViewById(viewBinder.titleId);
            titleView.setText(getTitle());
            TextView subtitleView = (TextView) actualAdView.findViewById(viewBinder.textId);
            subtitleView.setText(getBody());
            TextView ctaView = (TextView) actualAdView.findViewById(viewBinder.callToActionId);
            ctaView.setText(getCallToActionText());

            MediaView mediaView = (MediaView) actualAdView.findViewById(viewBinder.subMediaId);

            StarLevelLayoutView starLevelLayout = null;
            if (viewBinder.starLevelLayoutId != -1) {
                starLevelLayout = (StarLevelLayoutView) actualAdView.findViewById(viewBinder.starLevelLayoutId);
                if (starLevelLayout != null && getStarRating() != 0) {
                    starLevelLayout.setRating((int) getStarRating());
                }
            }
            if (mRawAd instanceof NativeContentAd) {
                nativeAdView = new NativeContentAdView(mContext);
                NativeContentAdView adView = (NativeContentAdView) nativeAdView;
                adView.setCallToActionView(ctaView);
                adView.setHeadlineView(titleView);
                adView.setLogoView(iconView);
                adView.setBodyView(subtitleView);
                VideoController vc = ((NativeContentAd) mRawAd).getVideoController();
                if (vc.hasVideoContent() && mediaView != null) {
                    coverView.setVisibility(View.GONE);
                    adView.setMediaView(mediaView);
                } else {
                    if (mediaView != null) {
                        mediaView.setVisibility(View.GONE);
                    }
                    adView.setImageView(coverView);
                    if (coverView instanceof BasicLazyLoadImageView) {
                        BasicLazyLoadImageView lazyLoadImageView = (BasicLazyLoadImageView) coverView;
                        lazyLoadImageView.setDefaultResource(R.drawable.native_default);
                        lazyLoadImageView.requestDisplayURL(getCoverImageUrl());
                    }
                }
            } else if (mRawAd instanceof NativeAppInstallAd) {
                nativeAdView = new NativeAppInstallAdView(mContext);
                NativeAppInstallAdView adView = (NativeAppInstallAdView) nativeAdView;
                adView.setCallToActionView(ctaView);
                adView.setHeadlineView(titleView);
                adView.setIconView(iconView);
                adView.setBodyView(subtitleView);
                if (starLevelLayout != null) {
                    adView.setStarRatingView(starLevelLayout);
                }
                VideoController vc = ((NativeAppInstallAd) mRawAd).getVideoController();
                if (vc.hasVideoContent() && mediaView != null) {
                    coverView.setVisibility(View.GONE);
                    adView.setMediaView(mediaView);
                } else {
                    if(mediaView !=null ) {
                        mediaView.setVisibility(View.GONE);
                    }
                    adView.setImageView(coverView);
                    if (coverView instanceof BasicLazyLoadImageView) {
                        BasicLazyLoadImageView lazyLoadImageView = (BasicLazyLoadImageView) coverView;
                        lazyLoadImageView.setDefaultResource(R.drawable.native_default);
                        lazyLoadImageView.requestDisplayURL(getCoverImageUrl());
                    }
                }
            } else {
                return null;
            }
            // Google native ad view renders the AdChoices icon in one of the four corners of
            // its view. If a margin is specified on the actual ad view, the AdChoices view
            // might be rendered outside the actual ad view. Moving the margins from the
            // actual ad view to Google native ad view will make sure that the AdChoices icon
            // is being rendered within the bounds of the actual ad view.
            registerViewForInteraction(actualAdView);
            FrameLayout.LayoutParams googleNativeAdViewParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            AdLog.d("admob:" + actualAdView.toString());
            ViewGroup.MarginLayoutParams actualViewParams = (ViewGroup.MarginLayoutParams) actualAdView.getLayoutParams();
            if (actualViewParams != null) {
                googleNativeAdViewParams.setMargins(actualViewParams.leftMargin,
                        actualViewParams.topMargin,
                        actualViewParams.rightMargin,
                        actualViewParams.bottomMargin);

                nativeAdView.setLayoutParams(googleNativeAdViewParams);
                actualViewParams.setMargins(0, 0, 0, 0);
            }
            nativeAdView.addView(actualAdView);
            nativeAdView.setNativeAd(mRawAd);
            return nativeAdView;
        }
        return null;
    }

    @Override
    public void registerViewForInteraction(View view) {
        super.registerViewForInteraction(view);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mRawAd instanceof NativeContentAd){
            ((NativeContentAd) mRawAd).destroy();
        }
        if (mRawAd instanceof NativeAppInstallAd){
            ((NativeAppInstallAd) mRawAd).destroy();
        }
    }
}
