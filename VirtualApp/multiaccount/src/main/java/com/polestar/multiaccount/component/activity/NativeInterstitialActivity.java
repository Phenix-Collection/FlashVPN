package com.polestar.multiaccount.component.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.MobVistaSDK;
import com.mobvista.msdk.out.Campaign;
import com.mobvista.msdk.out.Frame;
import com.mobvista.msdk.out.MobVistaSDKFactory;
import com.mobvista.msdk.out.MvNativeHandler;
import com.mobvista.msdk.out.MvNativeHandler.NativeAdListener;
import com.mobvista.msdk.out.MvNativeHandler.Template;
import com.polestar.imageloader.widget.BasicLazyLoadImageView;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.widgets.StarLevelLayoutView;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Native display interstitial
 *
 * @author
 *
 */
public class NativeInterstitialActivity extends Activity {

    private static final String TAG = NativeInterstitialActivity.class.getName();
    private static final String UNIT_ID = "8998";
    public int BIG_IMG_REQUEST_AD_NUM = 1;
    private BasicLazyLoadImageView mIvIcon;
    private BasicLazyLoadImageView mIvImage;
    private TextView mTvAppName;
    private TextView mTvAppDesc;
    private TextView mTvCta;
    private MvNativeHandler nativeHandle;
    private RelativeLayout mRlClose;
    private StarLevelLayoutView mStarLayout;
    private ProgressBar mProgressBar;
    private LinearLayout mLl_Root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobvista_native_interstitial);
        initView();
        showLoadding();
        setlistener();
        loadNative();
    }

    private void showLoadding() {
        mProgressBar.setVisibility(View.VISIBLE);
        mLl_Root.setVisibility(View.GONE);
    }

    private void hideLoadding() {
        mProgressBar.setVisibility(View.GONE);
        mLl_Root.setVisibility(View.VISIBLE);
    }

    private void setlistener() {
        mRlClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        mIvIcon = (BasicLazyLoadImageView) findViewById(R.id.mobvista_interstitial_iv_icon);
        mIvImage = (BasicLazyLoadImageView) findViewById(R.id.mobvista_interstitial_iv_image);
        mTvAppName = (TextView) findViewById(R.id.mobvista_interstitial_iv_app_name);
        mTvAppDesc = (TextView) findViewById(R.id.mobvista_interstitial_tv_app_desc);
        mTvCta = (TextView) findViewById(R.id.mobvista_interstitial_tv_cta);
        mRlClose = (RelativeLayout) findViewById(R.id.mobvista_interstitial_rl_close);
        mStarLayout = (StarLevelLayoutView) findViewById(R.id.mobvista_interstitial_star);
        mProgressBar = (ProgressBar) findViewById(R.id.mobvista_interstitial_progress);
        mLl_Root = (LinearLayout) findViewById(R.id.mobvista_interstitial_ll_root);
    }

    public void loadNative() {
        Map<String, Object> properties = MvNativeHandler.getNativeProperties(UNIT_ID);
        nativeHandle = new MvNativeHandler(properties, this);
        nativeHandle.addTemplate(new Template(MobVistaConstans.TEMPLATE_BIG_IMG, BIG_IMG_REQUEST_AD_NUM));
        nativeHandle.setAdListener(new NativeAdListener() {

            @Override
            public void onAdLoaded(List<Campaign> campaigns, int template) {
                hideLoadding();
                fillInterstitialLayout(campaigns);
                preloadNative();
            }

            @Override
            public void onAdLoadError(String message) {
                Log.e(TAG, "onAdLoadError:" + message);
            }

            @Override
            public void onAdFramesLoaded(List<Frame> list) {

            }

            @Override
            public void onAdClick(Campaign campaign) {
                Log.e(TAG, "onAdClick");
            }
        });
        nativeHandle.load();
    }

    protected void fillInterstitialLayout(List<Campaign> campaigns) {
        if (campaigns != null && campaigns.size() > 0) {
            Campaign campaign = campaigns.get(0);
            if (!TextUtils.isEmpty(campaign.getIconUrl())) {
                mIvIcon.setDefaultResource(0);
                mIvIcon.requestDisplayURL(campaign.getIconUrl());
            }
            if (!TextUtils.isEmpty(campaign.getImageUrl())) {
                mIvImage.setDefaultResource(0);
                mIvImage.requestDisplayURL(campaign.getImageUrl());
            }

            mTvAppName.setText(campaign.getAppName() + "");
            mTvAppDesc.setText(campaign.getAppDesc() + "");
            mTvCta.setText(campaign.getAdCall());
            int rating = (int) campaign.getRating();
            mStarLayout.setRating(rating);
            nativeHandle.registerView(mLl_Root, campaign);
        }
    }

    public void preloadNative() {
        MobVistaSDK sdk = MobVistaSDKFactory.getMobVistaSDK();
        Map<String, Object> preloadMap = new HashMap<String, Object>();
        preloadMap.put(MobVistaConstans.PROPERTIES_LAYOUT_TYPE, MobVistaConstans.LAYOUT_NATIVE);
        preloadMap.put(MobVistaConstans.PROPERTIES_UNIT_ID, UNIT_ID);
        List<Template> list = new ArrayList<Template>();
        list.add(new Template(MobVistaConstans.TEMPLATE_BIG_IMG, BIG_IMG_REQUEST_AD_NUM));
        preloadMap.put(MobVistaConstans.NATIVE_INFO, MvNativeHandler.getTemplateString(list));
        sdk.preload(preloadMap);

    }

}