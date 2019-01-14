package com.polestar.ad.adapters;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.mopub.nativeads.AdapterHelper;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.NativeAd;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.ViewBinder;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdViewBinder;

import java.util.EnumSet;

/**
 * Created by guojia on 2017/5/31.
 */

public class MopubNativeAdapter extends AdAdapter {
    private String adUnit;
    private RequestParameters parameters;
    private MoPubNative moPubNative;
    private NativeAd rawAd;
    private MoPubAdRendererProxy rendererProxy;

    public MopubNativeAdapter(Context context, String adUnit) {
        this.adUnit = adUnit;
        if (AdConstants.DEBUG) {
            AdLog.d( "Mopub test mode");
            this.adUnit = "11a17b188668469fb0412708c3d16813";
        }
        final EnumSet<RequestParameters.NativeAdAsset> desiredAssets = EnumSet.of(
                RequestParameters.NativeAdAsset.TITLE,
                RequestParameters.NativeAdAsset.TEXT,
                RequestParameters.NativeAdAsset.ICON_IMAGE,
                RequestParameters.NativeAdAsset.MAIN_IMAGE,
                RequestParameters.NativeAdAsset.CALL_TO_ACTION_TEXT);

        parameters = new RequestParameters.Builder()
                .desiredAssets(desiredAssets)
                .build();

    }

    @Override
    public void registerPrivacyIconView(View view) {

    }

    @Override
    public void loadAd(Context context, int num, IAdLoadListener listener) {
        adListener = listener;
        AdLog.d("Mopub loadAd " + listener);
        moPubNative = new MoPubNative(context, this.adUnit, new MoPubNative.MoPubNativeNetworkListener() {
            @Override
            public void onNativeLoad(NativeAd nativeAd) {
                AdLog.d("Mopub onNativeLoad " );
                rawAd = nativeAd;
                mLoadedTime = System.currentTimeMillis();
                if (adListener != null) {
                    adListener.onAdLoaded(MopubNativeAdapter.this);
                }
                stopMonitor();
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                AdLog.d("Mopub  onNativeFail " + errorCode.toString() );
                if (adListener != null) {
                    adListener.onError(errorCode.toString());
                }
                stopMonitor();
            }
        });
        rendererProxy = new MoPubAdRendererProxy();
        moPubNative.registerAdRenderer(rendererProxy);
        moPubNative.makeRequest(parameters);
        startMonitor();
    }

    @Override
    public String getAdType() {
        return AdConstants.AdType.AD_SOURCE_MOPUB;
    }

    @Override
    public Object getAdObject() {
        return rawAd;
    }

    @Override
    public void destroy() {
        if (isShowed()) {
            if (moPubNative != null) {
                moPubNative.destroy();
            }
            if (rawAd != null) {
                rawAd.destroy();
            }
        }
    }

    @Override
    public View getAdView(Context context, AdViewBinder viewBinder) {
        final ViewBinder mpViewBinder =  new ViewBinder.Builder(viewBinder.layoutId)
                .titleId(viewBinder.titleId)
                .textId(viewBinder.textId)
                .mainImageId(viewBinder.mainMediaId)
                .iconImageId(viewBinder.iconImageId)
                .callToActionId(viewBinder.callToActionId)
              // .privacyInformationIconImageId(viewBinder.privacyInformationId)
                .addExtras(viewBinder.extras)
                .build();
        final MoPubStaticNativeAdRenderer staticAdRender = new MoPubStaticNativeAdRenderer(mpViewBinder);
        rendererProxy.setRenderImpl(staticAdRender);
        AdapterHelper helper = new AdapterHelper(context, 0, 5);
        View adview = helper.getAdView(null, null, rawAd, mpViewBinder);
        ImageView iv = adview.findViewById(viewBinder.mainMediaId);
        if (iv != null) {
            iv.setVisibility(View.VISIBLE);
        }
        registerViewForInteraction(adview);
        return adview;
    }

    @Override
    protected void onTimeOut() {
        if (adListener != null) {
            adListener.onError("TIME_OUT");
        }
    }
}
