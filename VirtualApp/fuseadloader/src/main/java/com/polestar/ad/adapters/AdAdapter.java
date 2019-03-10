package com.polestar.ad.adapters;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import com.polestar.ad.AdUtils;
import com.polestar.ad.AdViewBinder;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by guojia on 2016/10/31.
 */

public abstract class AdAdapter implements IAdAdapter {
    protected String mKey;
    protected long mLoadedTime = -1;
    protected int mShowCount = 0;
    protected long LOAD_TIMEOUT = 10*1000;
    protected IAdLoadListener adListener;


    protected Handler mHandler = new Handler(Looper.myLooper()) ;

    private Runnable timeoutRunner = new Runnable() {
        @Override
        public void run() {
            onTimeOut();
        }
    };

    protected void startMonitor() {
        mHandler.postDelayed(timeoutRunner, LOAD_TIMEOUT);
    }

    protected void stopMonitor() {
        mHandler.removeCallbacks(timeoutRunner);
    }

    protected void onTimeOut() {

    }

    @Override
    public void setAdListener(IAdLoadListener listener) {
        adListener = listener;
    }

    @Override
    public boolean isInterstitialAd() {
        return false;
    }

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


    @Override
    public void show() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public View getAdView(Context context, AdViewBinder viewBinder) {
        return null;
    }

    @Override
    public void onActivityResume(Activity activity){}

    @Override
    public void onActivityPause(Activity activity){
    }

    protected void trackImpression() {
        AdUtils.trackAdEvent(mKey, "imp_" + getId());
    }

    protected void trackClick() {
        AdUtils.trackAdEvent(mKey, "clk_" + getId());
    }
}
