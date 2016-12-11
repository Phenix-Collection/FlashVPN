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

import com.polestar.ad.AdConstants;
import com.polestar.ad.adapters.FBInterstitialAdapter;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAd;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.component.adapter.NavigationAdapter;
import com.polestar.multiaccount.component.fragment.HomeFragment;
import com.polestar.multiaccount.constant.Constants;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.EventReportManager;
import com.polestar.multiaccount.utils.AppListUtils;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.DrawerBlurHelper;
import com.polestar.multiaccount.utils.Logs;
import com.polestar.multiaccount.utils.MTAManager;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.RenderScriptManager;
import com.polestar.multiaccount.utils.UpdateSDKManager;
import com.polestar.multiaccount.widgets.GifView;
import com.polestar.multiaccount.widgets.GuideForLongPressPopWindow;

import java.util.List;

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
    private CloneHelper cloneHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
        loadAd();
        cloneHelper = CloneHelper.getInstance(this);
        AppListUtils.getInstance(this); // init AppListUtils
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
//        giftView.play();
        //giftView.setVisibility(View.GONE);
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
                EventReportManager.homeMenu(HomeActivity.this);
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

        giftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (wrapInterstitialAd != null) {
//                    giftView.setVisibility(View.GONE);
//                    wrapInterstitialAd.show();
//                    loadAd();
//                }
            }
        });

//        if(Build.VERSION.SDK_INT == 18){
//            homeBgCoverImg.setVisibility(View.VISIBLE);
//            homeBgCoverImg.setImageResource(R.mipmap.main_bg_min);
//            homeBgView.setCoverImg(homeBgCoverImg);
//        }
    }

    private void loadAd() {
        Logs.e("start INTERSTITIAL loadAd");
        adLoader = new FuseAdLoader(this);
        adLoader.addAdSource(AdConstants.NativeAdType.AD_SOURCE_FACEBOOK_INTERSTITIAL, "1700354860278115_1702702800043321", -1);
        adLoader.addAdSource(AdConstants.NativeAdType.AD_SOURCE_ADMOB_INTERSTITIAL, "ca-app-pub-5490912237269284/5384537050", -1);
        adLoader.loadAd(1, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAd ad) {
                giftView.setGifResource(R.drawable.front_page_gift_icon);
                giftView.setVisibility(View.VISIBLE);
                giftView.play(10*1000);
                giftView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad.show();
                    }
                });
            }

            @Override
            public void onAdListLoaded(List<IAd> ads) {

            }

            @Override
            public void onError(String error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

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
        startActivityForResult(i, Constants.REQUEST_SELECT_APP);
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
                EventReportManager.menuNotification(this);
                Intent notification = new Intent(this, NotificationActivity.class);
                startActivity(notification);
                break;
            case 1:
                MTAManager.menuFAQ(this);
                EventReportManager.menuFAQ(this);
                Intent intentToFAQ = new Intent(this, FaqActivity.class);
                startActivity(intentToFAQ);
                break;
            case 2:
                MTAManager.menuFeedback(this);
                EventReportManager.menuFeedback(this);
                Intent feedback = new Intent(this, FeedbackActivity.class);
                startActivity(feedback);
                break;
            case 3:
                MTAManager.menuRate(this);
                CommonUtils.jumpToMarket(this, getPackageName());
                EventReportManager.menuRate(this);
                break;
            case 4:
                MTAManager.menuShare(this);
                EventReportManager.menuShare(this);
                CommonUtils.shareWithFriends(this);
                break;
            case 5:
                MTAManager.menuSettings(this);
                EventReportManager.menuSettings(this);
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
            if (requestCode == Constants.REQUEST_SELECT_APP) {
                Logs.e("install time2 = " + System.currentTimeMillis());
                AppModel model = data.getParcelableExtra(Constants.EXTRA_APP_MODEL);
               // AppInstallActivity.startAppInstallActivity(this, model, drawerBlurHelper.createBitmap());
                AppCloneActivity.startAppCloneActivity(this, model);
                loadInstallAd();
                mHomeFragment.hideToBottom();
            } else if (requestCode == Constants.REQUEST_INSTALL_APP) {

            }
        } else {
            doAnimationExter();
        }
    }

    private boolean needShowGuide() {
        if (cloneHelper.getClonedApps().size() == 1 && PreferencesUtils.getBoolean(this, Constants.KEY_SHOW_GUIDE_FOR_LONG_PRESS, true)) {
            PreferencesUtils.putBoolean(this, Constants.KEY_SHOW_GUIDE_FOR_LONG_PRESS, false);
            mHomeFragment.showGuidePopWindow();
            return true;
        }
        return false;
    }

    public void showGuidePopWindow(int locationX, int locationY) {
        if(isFinishing())
            return;
        GuideForLongPressPopWindow popWindow = new GuideForLongPressPopWindow(this, locationX, locationY);
        popWindow.show(contentLayout);
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
