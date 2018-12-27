package com.polestar.ad.adapters;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.ads.mediation.facebook.FacebookAdapter;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.NativeAdView;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.google.android.gms.ads.formats.NativeContentAd;
import com.google.android.gms.ads.formats.NativeContentAdView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.BuildConfig;
import com.polestar.ad.view.StarLevelLayoutView;

import java.util.List;


/**
 * Created by guojia on 2016/10/31.
 */

public class AdmobNativeAdapter extends AdAdapter {

    private String mFilter;
    private Context mContext;

    private UnifiedNativeAd mRawAd;

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
////        if (BuildConfig.DEBUG) {
//            mKey = "ca-app-pub-3940256099942544/1044960115";
////        }
//        mKey = "ca-app-pub-3940256099942544/2247696110";
        AdLoader.Builder  builder = new AdLoader.Builder(mContext, mKey);
        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                if (isValidAd(unifiedNativeAd)) {
                    postOnAdLoaded(unifiedNativeAd);
                } else {
                    postOnAdLoadFail(999);
                }
            }
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();
        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions).setRequestMultipleImages(false).setReturnUrlsForImageAssets(false)
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

    private void postOnAdLoaded(UnifiedNativeAd ad) {
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
        return AdConstants.NativeAdType.AD_SOURCE_ADMOB;
    }

    @Override
    public String getCoverImageUrl() {
      return mRawAd.getImages() != null && mRawAd.getImages().size() > 0 ?
              mRawAd.getImages().get(0).getUri().toString() : null;
    }

    @Override
    public String getIconImageUrl() {
        return mRawAd.getIcon() == null? null: mRawAd.getIcon().getUri().toString();
    }

    @Override
    public String getBody() {
        return( mRawAd).getBody()!=null?( mRawAd).getBody().toString():null;

    }

    @Override
    public String getSubtitle() {
        Bundle extras = ( mRawAd).getExtras();
        if (extras.containsKey(FacebookAdapter.KEY_SUBTITLE_ASSET)) {
            return extras.getString(FacebookAdapter.KEY_SUBTITLE_ASSET,"");
        }
        return( mRawAd).getBody()!=null?mRawAd.getBody().toString():null;
    }

    @Override
    public double getStarRating() {
        return (mRawAd).getStarRating();
    }

    @Override
    public String getTitle() {
        return (mRawAd).getHeadline() != null ? (mRawAd).getHeadline().toString() : null;
    }

    @Override
    public String getCallToActionText() {
            return( mRawAd).getCallToAction() != null? (mRawAd).getCallToAction().toString() : null;
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

    private boolean isValidAd(UnifiedNativeAd ad) {
            return (ad.getHeadline() != null && ad.getBody() != null
                    && ad.getCallToAction() != null);
    }

    @Override
    public View getAdView(AdViewBinder viewBinder) {
        View actualAdView = LayoutInflater.from(mContext).inflate(viewBinder.layoutId, null);
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        adView.setLayoutParams(params);
        UnifiedNativeAdView nativeAdView = new UnifiedNativeAdView(mContext);
        if (actualAdView != null) {
            ImageView iconView = (ImageView) actualAdView.findViewById(viewBinder.iconImageId);
            TextView titleView = (TextView) actualAdView.findViewById(viewBinder.titleId);
            titleView.setText(getTitle());
            TextView subtitleView = (TextView) actualAdView.findViewById(viewBinder.textId);
            subtitleView.setText(getBody());
            TextView ctaView = (TextView) actualAdView.findViewById(viewBinder.callToActionId);
            ctaView.setText(getCallToActionText());

            MediaView mediaView = null;
            ImageView coverImageView = null;

            View main = actualAdView.findViewById(viewBinder.mainMediaId);
            if (main instanceof MediaView) {
                mediaView = (MediaView) main;
            } else if (main instanceof ImageView) {
                coverImageView = (ImageView) main;
            }
            if (mediaView == null && viewBinder.admMediaId != -1) {
                mediaView = actualAdView.findViewById(viewBinder.admMediaId);
            }
            if (mediaView == null && coverImageView == null) {
                AdLog.d("Wrong ad layout " + viewBinder.layoutId);
                return null;
            }
            if (mediaView != null) {
                mediaView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                    @Override
                    public void onChildViewAdded(View parent, View child) {
                        if (child instanceof ImageView) {
                            ImageView imageView = (ImageView) child;
                            imageView.setAdjustViewBounds(true);
                        }
                    }

                    @Override
                    public void onChildViewRemoved(View parent, View child) {
                    }
                });
            }

            StarLevelLayoutView starLevelLayout = null;
            if (viewBinder.starLevelLayoutId != -1) {
                starLevelLayout = (StarLevelLayoutView) actualAdView.findViewById(viewBinder.starLevelLayoutId);
                if (starLevelLayout != null && getStarRating() != 0) {
                    starLevelLayout.setRating((int) getStarRating());
                }
            }
            nativeAdView.setCallToActionView(ctaView);
            nativeAdView.setHeadlineView(titleView);
            nativeAdView.setBodyView(subtitleView);
            VideoController vc = mRawAd.getVideoController();
            if (vc.hasVideoContent()) {
                if (mediaView == null) {
                    return null;
                }
                mediaView.setVisibility(View.VISIBLE);
                if (coverImageView != null) {
                    coverImageView.setVisibility(View.GONE);
                }
                vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                    @Override
                    public void onVideoEnd() {
                        AdLog.d("onVideoEnd");
                    }
                });
                nativeAdView.setMediaView(mediaView);
            } else {
                if (coverImageView == null) {
                    return null;
                }
                coverImageView.setVisibility(View.VISIBLE);
                if (mediaView != null) {
                    mediaView.setVisibility(View.GONE);
                }
                nativeAdView.setImageView(coverImageView);
                List<NativeAd.Image> images = mRawAd.getImages();
                coverImageView.setImageDrawable(images.get(0).getDrawable());
            }
            if (iconView != null ) {
                nativeAdView.setIconView(iconView);
                if (mRawAd.getIcon() == null) {
                    nativeAdView.getIconView().setVisibility(View.GONE);
                } else {
                    ((ImageView) nativeAdView.getIconView()).setImageDrawable(
                            mRawAd.getIcon().getDrawable());
                    nativeAdView.getIconView().setVisibility(View.VISIBLE);
                }
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
        mRawAd.destroy();
    }
}
