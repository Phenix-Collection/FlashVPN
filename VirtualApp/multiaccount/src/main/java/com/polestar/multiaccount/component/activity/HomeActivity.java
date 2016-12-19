package com.polestar.multiaccount.component.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.duapps.ad.offerwall.ui.OfferWallAct;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAd;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.component.adapter.NavigationAdapter;
import com.polestar.multiaccount.component.fragment.HomeFragment;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.AppListUtils;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.DrawerBlurHelper;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.MTAManager;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.polestar.multiaccount.utils.RenderScriptManager;
import com.polestar.multiaccount.utils.UpdateSDKManager;
import com.polestar.multiaccount.widgets.GifView;

import java.util.List;
import java.util.Random;

import static com.tencent.stat.StatConfig.getCustomProperty;

public class HomeActivity extends BaseActivity {

    private ViewGroup contentLayout;
    private View forgroundLayout;
    private HomeFragment mHomeFragment;
    private DrawerLayout drawer;
    private ListView navigationList;
    private View navigationLayout;
    private TextView appNameTv;
    private DrawerBlurHelper drawerBlurHelper;
    private GifView giftView;
    private FuseAdLoader adLoader;
    private boolean isInterstitialAdClicked;
    private boolean isInterstitialAdLoaded;

    private static final String KEY_HOME_GIFT_OFFERWALL_PERCENTAGE = "home_gift_offerwall_percentage";
    private static final String SLOT_HOME_GIFT_INTERSTITIAL = "slot_home_gift_interstitial";
    private boolean isShowOfferWall = true;

    private static final int OFFER_WALL_SHOW_DELAY = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        int random = new Random().nextInt(100);
        isShowOfferWall = (random < RemoteConfig.getLong(KEY_HOME_GIFT_OFFERWALL_PERCENTAGE));
        initView();
        AppListUtils.getInstance(this); // init AppListUtils
    }

    private void showOfferWall(){
        giftView.setGifResource(R.drawable.front_page_gift_icon);
        giftView.setVisibility(View.VISIBLE);
        giftView.play();
        giftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MTAManager.homeGiftClick(HomeActivity.this, "duapps_offer_wall");
                Intent intent3 = new Intent(HomeActivity.this, OfferWallAct.class);
                Bundle b = new Bundle();
                b.putInt("pid", 131003);
                b.putInt(OfferWallAct.KEY_TITLE_ID, R.string.appwall_title); // 可选
                b.putString(OfferWallAct.KEY_TAB_BACKGROUND_COLOR, "#4164ef"); // 可 选
                b.putString(OfferWallAct.KEY_TAB_INDICATOR_COLOR, "#000000"); // 可选
                b.putString(OfferWallAct.KEY_TAB_TEXT_COLOR, "#FFFFFF"); // 可选
                intent3.putExtras(b);
                startActivity(intent3);
            }
        });
    }

    private void initView() {
        contentLayout = (ViewGroup) findViewById(R.id.content_home);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setScrimColor(Color.TRANSPARENT);
        navigationList = (ListView) findViewById(R.id.navigation_list);
        navigationLayout = findViewById(R.id.navigation_layout);
        appNameTv = (TextView) findViewById(R.id.app_name);
        forgroundLayout = findViewById(R.id.blur_forground);
        giftView = (GifView) findViewById(R.id.gifView);
        giftView.setVisibility(View.INVISIBLE);

        navigationList.setAdapter(new NavigationAdapter(this));
        navigationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onNavigationItemSelected(i);
            }
        });
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                MTAManager.homeMenu(HomeActivity.this);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        mHomeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_content, mHomeFragment).commitAllowingStateLoss();
        UpdateCheck();
        drawerBlurHelper = new DrawerBlurHelper(HomeActivity.this, drawer, contentLayout, forgroundLayout, navigationLayout);
        appNameTv.post(new Runnable() {
            @Override
            public void run() {

//                drawerBlurHelper = new DrawerBlurHelper(HomeActivity.this, drawer, contentLayout, forgroundLayout, navigationLayout);
                float contentWidth = appNameTv.getWidth();
                float totalWidth = navigationLayout.getWidth();
                drawerBlurHelper.setContentPercentage(contentWidth / totalWidth);
                drawerBlurHelper.blur();
            }
        });
        if (isShowOfferWall) {
            giftView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showOfferWall();
                }
            }, OFFER_WALL_SHOW_DELAY);
        }
    }

    private void loadAd() {
        MLogs.e("start INTERSTITIAL loadAd");
        isInterstitialAdClicked = false;
        isInterstitialAdLoaded = false;
        adLoader = new FuseAdLoader(this);
        List<AdConfig> adSources = RemoteConfig.getAdConfigList(SLOT_HOME_GIFT_INTERSTITIAL);
        for(AdConfig adConfig: adSources) {
            adLoader.addAdConfig(adConfig);
            MLogs.d(SLOT_HOME_GIFT_INTERSTITIAL + " "+ adConfig.toString());
        }
        //adLoader.addAdSource(AdConstants.NativeAdType.AD_SOURCE_ADMOB_INTERSTITIAL, "ca-app-pub-5490912237269284/5384537050", -1);
        if (adLoader.hasValidAdSource()) {
            adLoader.loadAd(1, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAd ad) {
                    isInterstitialAdLoaded = true;
                    giftView.setGifResource(R.drawable.front_page_gift_icon);
                    giftView.setVisibility(View.VISIBLE);
                    giftView.play();
                    giftView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            isInterstitialAdClicked = true;
                            MTAManager.homeGiftClick(HomeActivity.this, ad.getAdType());
                            ad.show();
                        }
                    });
                }
                @Override
                public void onAdListLoaded(List<IAd> ads) {

                }
                @Override
                public void onError(String error) {
                    showOfferWall();
                }
            });
        } else {
            giftView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showOfferWall();
                }
            }, OFFER_WALL_SHOW_DELAY);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MTAManager.homeShow(this);
        if (! isShowOfferWall) {
            MLogs.d("isInterstitialAdLoaded " + isInterstitialAdLoaded + " isInterstitialAdClicked " + isInterstitialAdClicked);
            if (!isInterstitialAdLoaded || (isInterstitialAdClicked && isInterstitialAdLoaded)) {
                giftView.setVisibility(View.GONE);
                loadAd();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RenderScriptManager.destroy();
    }

    /**
     * 启动已安装的App列表页面
     */
    public void startAppListActivity() {
        Intent i = new Intent(this, AppListActivity.class);
        doAnimationExit();
        startActivityForResult(i, AppConstants.REQUEST_SELECT_APP);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void doAnimationExit() {
    }

    private void doAnimationExter() {
        mHomeFragment.showFromBottom();
    }

    public void onNavigationClick(View view) {
        int drawerLockMode = drawer.getDrawerLockMode(GravityCompat.START);
        if (drawer.isDrawerVisible(GravityCompat.START)
                && (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_OPEN)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            drawerBlurHelper.createCacheBitmap();
            drawer.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean onNavigationItemSelected(int position) {
        switch (position) {
            case 0:
                MTAManager.menuNotification(this);
                Intent notification = new Intent(this, NotificationActivity.class);
                startActivity(notification);
                break;
            case 1:
                MTAManager.menuFAQ(this);
                Intent intentToFAQ = new Intent(this, FaqActivity.class);
                startActivity(intentToFAQ);
                break;
            case 2:
                MTAManager.menuFeedback(this);
                Intent feedback = new Intent(this, FeedbackActivity.class);
                startActivity(feedback);
                break;
            case 3:
                MTAManager.menuRate(this);
                CommonUtils.jumpToMarket(this, getPackageName());
                break;
            case 4:
                MTAManager.menuShare(this);
                CommonUtils.shareWithFriends(this);
                break;
            case 5:
                MTAManager.menuSettings(this);
                Intent intentToSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentToSettings);
                break;
        }

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                drawer.closeDrawer(GravityCompat.START);
//            }
//        },600);

        return true;
    }

    private void loadInstallAd() {
//        LocalAdUtils.showFullScreenAd(this, false, new OnAdLoadListener() {
//            @Override
//            public void onLoad(IAd iAd) {
//
//            }
//
//            @Override
//            public void onLoadFailed(AdError adError) {
//
//            }
//
//            @Override
//            public void onLoadInterstitialAd(WrapInterstitialAd wrapInterstitialAd) {
//                HomeActivity.this.installAd = wrapInterstitialAd;
//            }
//        });

    }

    private void showAd(Object ad) {

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (!needShowGuide()) {
//                    if (ad != null) {
//                        ad.show();
//                    }
//                }
//            }
//        }, AnimatorHelper.DURATION_LONG * 2);
    }

    public void startAppLaunchActivity(String packageName) {
        //AppLaunchActivity.startAppLaunchActivity(this, packageName, drawerBlurHelper.createBitmap());
        AppStartActivity.startAppStartActivity(this, packageName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK
                && data != null) {
            if (requestCode == AppConstants.REQUEST_SELECT_APP) {
                MLogs.e("install time2 = " + System.currentTimeMillis());
                AppModel model = data.getParcelableExtra(AppConstants.EXTRA_APP_MODEL);
               // AppInstallActivity.startAppInstallActivity(this, model, drawerBlurHelper.createBitmap());
                AppCloneActivity.startAppCloneActivity(this, model);
                loadInstallAd();
                mHomeFragment.hideToBottom();
            } else if (requestCode == AppConstants.REQUEST_INSTALL_APP) {

            }
        } else {
            doAnimationExter();
        }
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }

    private void UpdateCheck() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UpdateSDKManager.init(HomeActivity.this);
            }
        }).start();
    }

}
