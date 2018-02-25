package com.polestar.multiaccount.component.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdSize;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.component.adapter.AppGridAdapter;
import com.polestar.multiaccount.component.adapter.AppListAdapter;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.pbinterface.DataObserver;
import com.polestar.multiaccount.utils.AppListUtils;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.polestar.multiaccount.widgets.FixedGridView;
import com.polestar.multiaccount.widgets.FixedListView;

import java.util.List;

/**
 * Created by yxx on 2016/7/15.
 */
public class AppListActivity extends BaseActivity implements DataObserver {
    private TextView mTextPopular;
    private TextView mTextMore;
    private TextView mTextRecommand;
    private FixedListView mListView;
    private FixedListView mRecommandListView;
    private FixedGridView mGradView;
    private AppListAdapter mAppListAdapter;
    private AppListAdapter mRecommandListAdapter;
    private AppGridAdapter mAppGridAdapter;
    private List<AppModel> mPopularModels;
    private List<AppModel> mInstalledModels;
    private List<AppModel> mRecommandModels;
    private Context mContext;
    private LinearLayout adContainer;
    private FuseAdLoader mNativeAdLoader;
    private TextView sponsorText;
    public static final String SLOT_APPLIST_NATIVE = "slot_applist_native";

    private static final String CONFIG_APPLIST_NATIVE_PRIOR_TIME = "applist_native_prior_time";
    private IAdAdapter nativeAd;

    public static AdSize getBannerAdSize() {
        int dpWidth = DisplayUtils.px2dip(MApp.getApp(), DisplayUtils.getScreenWidth(MApp.getApp()));
        return new AdSize(dpWidth, 320);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        mContext = this;
        initView();
        if (!PreferencesUtils.isAdFree() ) {
            loadNativeAd();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppListUtils.getInstance(this).registerObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppListUtils.getInstance(this).unregisterObserver(this);
        if (nativeAd != null) {
            nativeAd.destroy();
        }
    }

    private void initView() {
        setTitle(getResources().getString(R.string.clone_apps_title));

        mTextPopular = (TextView) findViewById(R.id.text_popular);
        mTextRecommand = (TextView) findViewById(R.id.text_recommand);
        mTextMore = (TextView) findViewById(R.id.text_more);
        mListView = (FixedListView) findViewById(R.id.app_list_popular);
        mRecommandListView = (FixedListView) findViewById(R.id.app_list_recommand);
        mGradView = (FixedGridView) findViewById(R.id.app_list_more);

        mTextMore.setVisibility(View.INVISIBLE);

        mAppListAdapter = new AppListAdapter(mContext);
        mRecommandListAdapter = new AppListAdapter(mContext);
        mAppGridAdapter = new AppGridAdapter(mContext);
        mListView.setAdapter(mAppListAdapter);
        mRecommandListView.setAdapter(mRecommandListAdapter);
        mGradView.setAdapter(mAppGridAdapter);

        mPopularModels = AppListUtils.getInstance(this).getPopularModels();
        mInstalledModels = AppListUtils.getInstance(this).getInstalledModels();
        mRecommandModels = AppListUtils.getInstance(this).getRecommandModels();

        if (mPopularModels == null || mPopularModels.size() == 0) {
            mTextPopular.setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
        } else {
            mAppListAdapter.setModels(mPopularModels);
        }

        if (mRecommandModels == null || mRecommandModels.size() == 0) {
            mTextRecommand.setVisibility(View.GONE);
            mRecommandListView.setVisibility(View.GONE);
        } else {
            mTextRecommand.setVisibility(View.VISIBLE);
            mRecommandListAdapter.setModels(mRecommandModels);
        }

        if (mInstalledModels == null || mInstalledModels.size() == 0) {
            mTextMore.setVisibility(View.GONE);
            mGradView.setVisibility(View.GONE);
        } else {
            mTextMore.setVisibility(View.VISIBLE);
            showMoreApps();
        }

        mListView.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mInstalledModels.size() != 0) {
                    mTextMore.setVisibility(View.VISIBLE);
                    showMoreApps();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent data = new Intent();
                data.putExtra(AppConstants.EXTRA_APP_MODEL, mPopularModels.get(position));
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        mRecommandListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent data = new Intent();
                data.putExtra(AppConstants.EXTRA_APP_MODEL, mRecommandModels.get(i));
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        mGradView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent data = new Intent();
                data.putExtra(AppConstants.EXTRA_APP_MODEL, mInstalledModels.get(position));
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
        adContainer = (LinearLayout) findViewById(R.id.ad_container);
        sponsorText = (TextView) findViewById(R.id.sponsor_text);
    }

    private void showMoreApps() {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mTextMore, "alpha", 0.0f, 1.0f);
        alpha.setDuration(300);
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mInstalledModels.size() != 0) {
                    mAppGridAdapter.setModels(mInstalledModels);
                }
            }
        });
        alpha.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
        watch AppListUtils state
     */
    @Override
    public void onChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAppListAdapter.notifyDataSetChanged();
                mAppGridAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onInvalidated() {

    }

    private void loadNativeAd() {
        if (mNativeAdLoader == null) {
            mNativeAdLoader = FuseAdLoader.get(SLOT_APPLIST_NATIVE, this.getApplicationContext());
            mNativeAdLoader.setBannerAdSize(getBannerAdSize());
        }
//        mNativeAdLoader.addAdConfig(new AdConfig(AdConstants.NativeAdType.AD_SOURCE_FACEBOOK, "1713507248906238_1787756514814644", -1));
//        mNativeAdLoader.addAdConfig(new AdConfig(AdConstants.NativeAdType.AD_SOURCE_MOPUB, "ea31e844abf44e3690e934daad125451", -1));
        if (mNativeAdLoader.hasValidAdSource()) {
            mNativeAdLoader.loadAd(2, RemoteConfig.getLong(CONFIG_APPLIST_NATIVE_PRIOR_TIME), new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                   inflateNativeAdView(ad);
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {
                    MLogs.d("AppList load ad error " + error);
                }
            });
        }
    }

    private void inflateNativeAdView(IAdAdapter ad) {
        final AdViewBinder viewBinder =  new AdViewBinder.Builder(R.layout.native_ad_applist)
                .titleId(R.id.ad_title)
                .textId(R.id.ad_subtitle_text)
                .mainMediaId(R.id.ad_cover_image)
                .iconImageId(R.id.ad_icon_image)
                .callToActionId(R.id.ad_cta_text)
                .privacyInformationId(R.id.ad_choices_image)
                .build();
        View adView = ad.getAdView(viewBinder);
        nativeAd = ad;
        if (adView != null) {
            adContainer.removeAllViews();
            adContainer.addView(adView);
            adContainer.setVisibility(View.VISIBLE);
            sponsorText.setVisibility(View.VISIBLE);
        }
    }
}
