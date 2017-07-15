package com.polestar.ad.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.nativeads.BaseNativeAd;
import com.mopub.nativeads.MoPubAdRenderer;

import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.StaticNativeAd;

/**
 * Created by guojia on 2017/5/31.
 */

public class MoPubAdRendererProxy implements MoPubAdRenderer<StaticNativeAd> {

    private MoPubStaticNativeAdRenderer rendererImpl;

    public void setRenderImpl(MoPubStaticNativeAdRenderer render) {
        rendererImpl = render;
    }

    public MoPubStaticNativeAdRenderer getRendererImpl() {
        return rendererImpl;
    }

    @NonNull
    @Override
    public View createAdView(@NonNull Context context, @Nullable ViewGroup parent) {
        return rendererImpl.createAdView(context, parent);
    }

    @Override
    public void renderAdView(@NonNull View view, @NonNull StaticNativeAd ad) {
        rendererImpl.renderAdView(view, ad);
    }

    @Override
    public boolean supports(@NonNull BaseNativeAd nativeAd) {
        return true;
    }
}
