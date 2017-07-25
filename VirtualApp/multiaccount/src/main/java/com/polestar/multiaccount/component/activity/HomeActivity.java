package com.polestar.multiaccount.component.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.mobvista.msdk.MobVistaConstans;
import com.mobvista.msdk.out.MvWallHandler;
import com.polestar.ad.AdUtils;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.billing.BillingConstants;
import com.polestar.billing.BillingProvider;
import com.polestar.multiaccount.BuildConfig;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.component.adapter.NavigationAdapter;
import com.polestar.multiaccount.component.fragment.HomeFragment;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.AppListUtils;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.MTAManager;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.widgets.UpDownDialog;
import com.polestar.multiaccount.utils.RemoteConfig;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class HomeActivity extends BaseActivity {

    private HomeFragment mHomeFragment;
    private DrawerLayout drawer;
    private ListView navigationList;
    private View navigationLayout;
    private TextView appNameTv;
    private ImageView giftIconView;
    private FuseAdLoader adLoader;
    private boolean isAutoInterstitialShown;
    private boolean isInterstitialAdLoaded;
    private boolean autoShowInterstitial;

    private static final String SLOT_HOME_GIFT_INTERSTITIAL = "slot_home_gift_interstitial_1026";
    //private static final String SLOT_HOME_GIFT_INTERSTITIAL = "slot_test";
    private static final String CONFIG_AUTO_SHOW_INTERSTITIAL = "auto_home_interstitial_rate";
    private static final String CONFIG_AUTO_SHOW_INTERSTITIAL_INTERVAL = "auto_home_interstitial_interval";
    private static final String CONFIG_CLONE_RATE_PACKAGE = "clone_rate_package";
    private static final String CONFIG_CLONE_RATE_INTERVAL = "clone_rate_interval";
    private static final String CONFIG_AD_FREE_DIALOG_INTERVAL = "ad_free_dialog_interval";
    private static final String CONFIG_AD_FREE_DIALOG_INTERVAL_2 = "ad_free_dialog_interval_2";

    private static final String RATE_FROM_QUIT = "quit";
    private static final String RATE_AFTER_CLONE = "clone";
    private static final String RATE_FROM_MENU = "menu";

    private static final int REQUEST_UNLOCK_SETTINGS = 100;

    private static final String CONFIG_APP_WALL_PERCENTAGE = "home_appwall_percentage";
    private static final String CONFIG_AVAZU_CLICK_RATE = "avazu_click_rate";
    private static final String CONFIG_AVAZU_IMP_RATE = "avazu_imp_rate";
    private boolean showAppWall;

    private String cloningPackage;
    private RelativeLayout iconAdLayout;
    private RelativeLayout giftIconLayout;
    private RelativeLayout wallButtonLayout;
    private IAdAdapter interstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MTAManager.homeShow(this);
        setContentView(R.layout.activity_home);
        initView();
        AppListUtils.getInstance(this); // init AppListUtils
        long wallPercent = RemoteConfig.getLong(CONFIG_APP_WALL_PERCENTAGE);
        int random = new Random().nextInt(100);
        showAppWall = random < wallPercent;
        random = new Random().nextInt(100);
        long interval = System.currentTimeMillis() - PreferencesUtils.getAutoInterstialTime();
        autoShowInterstitial = (random < RemoteConfig.getLong(CONFIG_AUTO_SHOW_INTERSTITIAL)
                && (interval > RemoteConfig.getLong(CONFIG_AUTO_SHOW_INTERSTITIAL_INTERVAL))) || BuildConfig.DEBUG;
        MLogs.d("showAppWall: " + showAppWall + " autoInterstitial: " + autoShowInterstitial);
        //showAppWall = true;
    }

    private void initView() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setScrimColor(Color.TRANSPARENT);
        navigationList = (ListView) findViewById(R.id.navigation_list);
        navigationLayout = findViewById(R.id.navigation_layout);
        appNameTv = (TextView) findViewById(R.id.app_name);
        giftIconView = (ImageView) findViewById(R.id.gift_icon);
        giftIconLayout = (RelativeLayout) findViewById(R.id.gift_icon_layout);
        giftIconView.setVisibility(View.GONE);
        giftIconLayout.setVisibility(View.GONE);

        iconAdLayout = (RelativeLayout)findViewById(R.id.icon_ad);
        wallButtonLayout = (RelativeLayout)findViewById(R.id.wall_button);

        navigationList.setAdapter(new NavigationAdapter(this));
//        int width = DisplayUtils.getScreenWidth(this);
//        int listWidth = DisplayUtils.px2dip(this, width*2/3);
//        MLogs.d("width set to " + listWidth);
//        navigationList.setLayoutParams(new LinearLayout.LayoutParams(listWidth, ViewGroup.LayoutParams.MATCH_PARENT));
        navigationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onNavigationItemSelected(i);
                drawer.closeDrawer(GravityCompat.START);
//                int drawerLockMode = drawer.getDrawerLockMode(GravityCompat.START);
//                if (drawer.isDrawerVisible(GravityCompat.START)
//                        && (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_OPEN)) {
//                    drawer.closeDrawer(GravityCompat.START);
//                }
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
        View view = findViewById(R.id.content_home);
        setImmerseLayout(view);
        mHomeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frag_content, mHomeFragment).commitAllowingStateLoss();
    }

    private void preloadAppWall() {
        AdUtils.preloadAppWall(AppConstants.WALL_UNIT_ID);
    }

    private MvWallHandler mvHandler;
    private boolean wallClickReported = false;
    public void loadMVWallHandler(){
        MLogs.d("loadMVWallHandler");
        wallButtonLayout.setVisibility(View.VISIBLE);
        giftIconLayout.setVisibility(View.GONE);
        giftIconView.setVisibility(View.GONE);
        wallClickReported = false;
        Map<String,Object> properties = MvWallHandler.getWallProperties(AppConstants.WALL_UNIT_ID);
        properties.put(MobVistaConstans.PROPERTIES_WALL_STATUS_COLOR, R.color.theme_color2);
        properties.put(MobVistaConstans.PROPERTIES_WALL_TITLE_LOGO_TEXT,  getString(R.string.appwall_title));
        properties.put(MobVistaConstans.PROPERTIES_WALL_TITLE_LOGO_TEXT_TYPEFACE,  MobVistaConstans.TITLE_TYPEFACE_DEFAULT_BOLD);
        properties.put(MobVistaConstans.PROPERTIES_WALL_TITLE_LOGO_TEXT_SIZE, DisplayUtils.dip2px(this,20));
        properties.put(MobVistaConstans.PROPERTIES_WALL_TITLE_BACKGROUND_COLOR, R.color.theme_color2);
        properties.put(MobVistaConstans.PROPERTIES_WALL_NAVIGATION_COLOR, R.color.theme_color2);
        properties.put(MobVistaConstans.PROPERTIES_WALL_TAB_BACKGROUND_ID, R.color.theme_color2);
        mvHandler = new MvWallHandler(properties, this, wallButtonLayout);//nat为点击事件的vg，请确保改vg的点击事件不被拦截
        wallButtonLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!wallClickReported) {
                    MTAManager.homeGiftClick(HomeActivity.this, "mv_app_wall");
                }
                wallClickReported = true;
                return false;
            }
        });
        //customer entry layout begin 该部分代码可以不用
        View view = getLayoutInflater().inflate(R.layout.mv_wall_button, null);
        view.findViewById(R.id.imageview).setTag(MobVistaConstans.WALL_ENTRY_ID_IMAGEVIEW_IMAGE);
        view.findViewById(R.id.newtip_area).setTag(MobVistaConstans.WALL_ENTRY_ID_VIEWGROUP_NEWTIP);
        mvHandler.setHandlerCustomerLayout(view);
        //customer entry layout end */
        mvHandler.load();
    }

    public void openWall(){
        try {
            Class<?> aClass = Class
                    .forName("com.mobvista.msdk.shell.MVActivity");
            Intent intent = new Intent(this, aClass);
            intent.putExtra(MobVistaConstans.PROPERTIES_UNIT_ID, AppConstants.WALL_UNIT_ID);
            intent.putExtra(MobVistaConstans.PROPERTIES_WALL_STATUS_COLOR, R.color.theme_color2);
            intent.putExtra(MobVistaConstans.PROPERTIES_WALL_TITLE_LOGO_TEXT,  getString(R.string.appwall_title));
            intent.putExtra(MobVistaConstans.PROPERTIES_WALL_TITLE_LOGO_TEXT_TYPEFACE,  MobVistaConstans.TITLE_TYPEFACE_DEFAULT_BOLD);
            intent.putExtra(MobVistaConstans.PROPERTIES_WALL_TITLE_LOGO_TEXT_SIZE, DisplayUtils.dip2px(this,20));
            intent.putExtra(MobVistaConstans.PROPERTIES_WALL_TITLE_BACKGROUND_COLOR, R.color.theme_color2);
            intent.putExtra(MobVistaConstans.PROPERTIES_WALL_NAVIGATION_COLOR, R.color.theme_color2);
            intent.putExtra(MobVistaConstans.PROPERTIES_WALL_TAB_BACKGROUND_ID, R.color.theme_color2);
            this.startActivity(intent);
        } catch (Exception e) {
            MLogs.e("MVActivity", e.toString());
        }
    }

    private void showGiftIcon() {
        int iconId = new Random().nextInt(3);
        int giftRes;
        switch (iconId) {
            case 0:
                giftRes = R.drawable.ring_ad;
                break;
            default:
                giftRes = R.drawable.ad_taged_gift_icon;
                break;
        }
        giftIconView.setImageResource(giftRes);
        giftIconView.setVisibility(View.VISIBLE);
        giftIconLayout.setVisibility(View.VISIBLE);
        long interval = System.currentTimeMillis() - PreferencesUtils.getLastIconAdClickTime(HomeActivity.this);
        RelativeLayout layout = (RelativeLayout) giftIconLayout.findViewById(R.id.gift_new_tip);
        if (interval > 24 * 60 * 60 * 1000) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.INVISIBLE);
        }
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(giftIconView, "scaleX", 0.7f, 1.3f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(giftIconView, "scaleY", 0.7f, 1.3f, 1.0f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY);
        animSet.setInterpolator(new BounceInterpolator());
        animSet.setDuration(800).start();
    }

    private void loadHomeInterstitial() {
        MLogs.e("start INTERSTITIAL loadHomeInterstitial");
        isInterstitialAdLoaded = false;
        interstitialAd = null;
        adLoader = FuseAdLoader.get(SLOT_HOME_GIFT_INTERSTITIAL, this);
        //adLoader.addAdSource(AdConstants.NativeAdType.AD_SOURCE_ADMOB_INTERSTITIAL, "ca-app-pub-5490912237269284/5384537050", -1);
        if (adLoader.hasValidAdSource()) {
            adLoader.loadAd(1, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    isInterstitialAdLoaded = true;
                    interstitialAd = ad;
                   // giftGifView.setGifResource(R.drawable.front_page_gift_icon);

                }
                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }
                @Override
                public void onError(String error) {
                    if (!showAppWall) {
                        loadMVWallHandler();
                        showAppWall = true;
                        MLogs.e(error);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MLogs.d("isInterstitialAdLoaded " + isInterstitialAdLoaded + " isAutoInterstitialShown " + isAutoInterstitialShown);
        preloadAppWall();
        if (!PreferencesUtils.isAdFree()) {
            if (showAppWall) {
                giftIconLayout.setVisibility(View.GONE);
                giftIconView.setVisibility(View.GONE);
                wallButtonLayout.setVisibility(View.GONE);
                loadMVWallHandler();

            }else {
                giftIconLayout.setVisibility(View.GONE);
                giftIconView.setVisibility(View.GONE);
                giftIconView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showGiftIcon();
                    }
                },800);

            }
            if (autoShowInterstitial && !isAutoInterstitialShown) {
                loadHomeInterstitial();
            }
            if (requestAdFree) {
                updateBillingStatus();
            }
        } else {
            hideAd();
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

    private boolean guideRateIfNeeded() {
        if (cloningPackage != null) {
            String pkg = cloningPackage;
            cloningPackage = null;
            MLogs.d("Cloning package: " + pkg);
            if (PreferencesUtils.isRated()) {
                return false;
            }
            String config = RemoteConfig.getString(CONFIG_CLONE_RATE_PACKAGE);
            if ("off".equalsIgnoreCase(config)) {
                MLogs.d("Clone rate off");
                return false;
            }
            if(PreferencesUtils.getLoveApp() == -1) {
                // not love, should wait for interval
                long interval = RemoteConfig.getLong(CONFIG_CLONE_RATE_INTERVAL) * 60 * 60 * 1000;
                if ((System.currentTimeMillis() - PreferencesUtils.getRateDialogTime(this)) < interval) {
                    MLogs.d("Not love, need wait longer");
                    return false;
                }
            }
            boolean match = "*".equals(config);
            if (!match) {
                String[] pkgList = config.split(":");
                if (pkgList != null && pkgList.length > 0) {
                    for (String s: pkgList) {
                        if(s.equalsIgnoreCase(pkg)) {
                            match = true;
                            break;
                        }
                    }
                }
            }
            if (match) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showRateDialog(RATE_AFTER_CLONE, pkg);
                    }
                }, 800);
                return true;
            } else {
                MLogs.d("No matching package for clone rate");
            }
        }
        return false;
    }

    private void doAnimationEnter() {
        mHomeFragment.showFromBottom();
        MTAManager.generalClickEvent(this, "home_animate_enter");
        if (!PreferencesUtils.hasShownLongClickGuide(this)) {
            MLogs.d("Not show long click guide.");
            return;
        }
        boolean needShowRate = guideRateIfNeeded();
        boolean showAdFree = false;
        if (!needShowRate && !BillingProvider.get().isAdFreeVIP()) {
            long interval = RemoteConfig.getLong(CONFIG_AD_FREE_DIALOG_INTERVAL) * 60 * 60 * 1000;
            long interval2 = RemoteConfig.getLong(CONFIG_AD_FREE_DIALOG_INTERVAL_2) * 60 * 60 * 1000;
            interval = PreferencesUtils.getAdFreeClickStatus() ? interval : interval2;
            long last = PreferencesUtils.getLastAdFreeDialogTime();
            if ((System.currentTimeMillis() - last) > interval ) {
                showAdFree = true;
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showAdFreeDialog();
                    }
                }, 800);
                PreferencesUtils.updateLastAdFreeDialogTime();
            }
        }
        if (!showAdFree && !isAutoInterstitialShown && autoShowInterstitial) {
            if (interstitialAd != null) {
                try {
                    interstitialAd.show();
                    isAutoInterstitialShown = true;
                    PreferencesUtils.updateAutoInterstialTime();
                    MTAManager.homeGiftClick(this, interstitialAd.getAdType() + "_auto_home");
                    interstitialAd = null;
                } catch (Exception ex) {
                    MLogs.logBug("Show interstitial fail: " + MLogs.getStackTraceString(ex));
                }
            }
        }
    }

    private boolean requestAdFree = false;
    private void showAdFreeDialog() {
        MTAManager.generalClickEvent(HomeActivity.this, "ad_free_dialog_from_home");
        UpDownDialog.show(HomeActivity.this, getString(R.string.adfree_dialog_title), getString(R.string.adfree_dialog_content),
                getString(R.string.no_thanks), getString(R.string.yes), -1, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case UpDownDialog.POSITIVE_BUTTON:
                                BillingProvider.get().getBillingManager()
                                        .initiatePurchaseFlow(HomeActivity.this, BillingConstants.SKU_AD_FREE, BillingClient.SkuType.INAPP);
                                requestAdFree = true;
                                PreferencesUtils.updateAdFreeClickStatus(true);
                                MTAManager.generalClickEvent(HomeActivity.this, "click_home_ad_free_dialog_yes");
                                break;
                            case UpDownDialog.NEGATIVE_BUTTON:
                                PreferencesUtils.updateAdFreeClickStatus(false);
                                MTAManager.generalClickEvent(HomeActivity.this, "click_home_ad_free_dialog_no");
                                break;
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                PreferencesUtils.updateAdFreeClickStatus(false);
                MTAManager.generalClickEvent(HomeActivity.this, "click_home_ad_free_dialog_no");
            }
        });
    }

    private void updateBillingStatus() {
        BillingProvider.get().updateStatus(new BillingProvider.OnStatusUpdatedListener() {
            @Override
            public void onStatusUpdated() {
                MLogs.d("Billing onStatusUpdated");
                if (requestAdFree) {
                    if (BillingProvider.get().isAdFreeVIP()) {
                        PreferencesUtils.setAdFree(true);
                        hideAd();
                    }
                    requestAdFree = false;
                }
            }
        });
    }

    private void hideAd() {
        giftIconLayout.setVisibility(View.GONE);
        giftIconView.setVisibility(View.GONE);
        wallButtonLayout.setVisibility(View.GONE);
        if (mHomeFragment != null) {
            mHomeFragment.hideAd();
        }
    }

    public void onNavigationClick(View view) {
        int drawerLockMode = drawer.getDrawerLockMode(GravityCompat.START);
        if (drawer.isDrawerVisible(GravityCompat.START)
                && (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_OPEN)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (drawerLockMode != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    public void onLockSettingClick(View view) {
        if (PreferencesUtils.isLockerEnabled(HomeActivity.this) || isDebugMode()) {
            LockPasswordSettingActivity.start(HomeActivity.this, false, getString(R.string.lock_settings_title), REQUEST_UNLOCK_SETTINGS);
        } else {
            LockSettingsActivity.start(HomeActivity.this,"home");
        }
    }

    public void onIconAdClick(View view) {
        MLogs.d("onIconAdClick showAppWall: " + showAppWall);
        PreferencesUtils.updateIconAdClickTime(this);
        if (PreferencesUtils.isAdFree()) {
            return;
        }
        if (showAppWall) {
            openWall();
            MTAManager.homeGiftClick(this, "mv_app_wall");
        } else {
            Intent intent = new Intent(this, NativeInterstitialActivity.class);
            startActivity(intent);
            MTAManager.homeGiftClick(this, "lucky");
        }
    }

    private final static String QUIT_RATE_RANDOM = "quit_rating_random";
    private final static String QUIT_RATE_INTERVAL = "quit_rating_interval";
    private final static String QUIT_RATE_CLONED_APP_GATE = "quit_rating_cloned_app_gate";
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            boolean showRate = false;
            if (! PreferencesUtils.isRated()) {
                MLogs.d("Quit Rate config:" +  RemoteConfig.getLong(QUIT_RATE_INTERVAL)+" , "
                        + RemoteConfig.getLong(QUIT_RATE_RANDOM) + " , gate " +RemoteConfig.getLong(QUIT_RATE_CLONED_APP_GATE));
                long interval = RemoteConfig.getLong(QUIT_RATE_INTERVAL) * 60 * 60 * 1000;
                long lastTime = PreferencesUtils.getRateDialogTime(this);
                if (PreferencesUtils.getLoveApp() != -1) {
                    //Don't love app
                    int random = new Random().nextInt(100);
                    int clonedCnt = CloneHelper.getInstance(this).getClonedApps().size();
                    boolean isShowRateDialog = PreferencesUtils.getLoveApp() == 1 ||
                            ((random < RemoteConfig.getLong(QUIT_RATE_RANDOM)) && clonedCnt >= RemoteConfig.getLong(QUIT_RATE_CLONED_APP_GATE));
                    if (isShowRateDialog && (System.currentTimeMillis() - lastTime) > interval) {
                        showRate = true;
                        showRateDialog(RATE_FROM_QUIT, null);
                    }
                }
                if (!showRate) {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    static boolean existDebugFile = false;
    public static boolean isDebugMode(){
        if (existDebugFile) return  true;
        try {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "polestarunlocktest");
            if (file.exists()) {
                existDebugFile = true;
                return true;
            }
        } catch (Exception e) {
            MLogs.e(e);
        }
        return false;
    }
    public boolean onNavigationItemSelected(int position) {
        switch (position) {
            case 0:
                if (PreferencesUtils.isLockerEnabled(this) || isDebugMode()) {
                    LockPasswordSettingActivity.start(this, false, getString(R.string.lock_settings_title), REQUEST_UNLOCK_SETTINGS);
                } else {
                    LockSettingsActivity.start(this,"home");
                }
                MTAManager.menuPrivacyLocker(this);
                break;
            case 1:
                MTAManager.menuNotification(this);
                Intent notification = new Intent(this, NotificationActivity.class);
                startActivity(notification);
                break;
            case 2:
                MTAManager.menuFAQ(this);
                Intent intentToFAQ = new Intent(this, FaqActivity.class);
                startActivity(intentToFAQ);
                break;
            case 3:
                MTAManager.menuFeedback(this);
                Intent feedback = new Intent(this, FeedbackActivity.class);
                startActivity(feedback);
                break;
            case 4:
                showRateDialog(RATE_FROM_MENU, null);
                break;
            case 5:
                MTAManager.menuShare(this);
                CommonUtils.shareWithFriends(this);
                break;
            case 6:
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

    private boolean rateDialogShowed = false;
    private void showRateDialog(String from, String pkg){
        if (RATE_AFTER_CLONE.equals(from) || RATE_FROM_QUIT.equals(from)){
            if (rateDialogShowed ) {
                MLogs.d("Already showed dialog this time");
                return;
            }
            rateDialogShowed= true;
        }
        MTAManager.reportRate(this,"start", from);
        PreferencesUtils.updateRateDialogTime(this);
        String title = RATE_AFTER_CLONE.equals(from) ? getString(R.string.congratulations) : getString(R.string.rate_us);
        UpDownDialog.show(this, title,
                getString(R.string.dialog_rating_us_content), getString(R.string.not_really),
                getString(R.string.yes), R.drawable.dialog_tag_congratulations,
                R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                PreferencesUtils.setLoveApp(false);
                                if (!RATE_AFTER_CLONE.equals(from)) {
                                    MTAManager.loveCloneApp(HomeActivity.this, false, from );
                                } else {
                                    MTAManager.loveCloneApp(HomeActivity.this, false,pkg);
                                }
                                UpDownDialog.show(HomeActivity.this, getString(R.string.feedback),
                                        getString(R.string.dialog_feedback_content),
                                        getString(R.string.no_thanks),
                                        getString(R.string.ok), R.drawable.dialog_tag_comment,
                                        R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case UpDownDialog.POSITIVE_BUTTON:
                                                        MTAManager.reportRate(HomeActivity.this, "go_faq", from);
                                                        Intent feedback = new Intent(HomeActivity.this, FeedbackActivity.class);
                                                        startActivity(feedback);
                                                        break;
                                                }
                                            }
                                        });
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                PreferencesUtils.setLoveApp(true);
                                if (!RATE_AFTER_CLONE.equals(from)) {
                                    MTAManager.loveCloneApp(HomeActivity.this, true, from );
                                } else {
                                    MTAManager.loveCloneApp(HomeActivity.this, true,pkg);
                                }
                                UpDownDialog.show(HomeActivity.this, getString(R.string.dialog_love_title),
                                        getString(R.string.dialog_love_content),
                                        getString(R.string.remind_me_later),
                                        getString(R.string.star_rating), R.drawable.dialog_tag_love,
                                        R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case UpDownDialog.POSITIVE_BUTTON:
                                                        MTAManager.reportRate(HomeActivity.this, "go_rating",from);
                                                        PreferencesUtils.setRated(true);
                                                        CommonUtils.jumpToMarket(HomeActivity.this, getPackageName());
                                                        break;
                                                }
                                            }
                                        });
                                break;
                        }
                    }
                });

    }

    public void startAppLaunchActivity(String packageName) {
        //AppLaunchActivity.startAppLaunchActivity(this, packageName, drawerBlurHelper.createBitmap());
        AppStartActivity.startAppStartActivity(this, packageName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_UNLOCK_SETTINGS) {
            switch (resultCode) {
                case RESULT_OK:
                    LockSettingsActivity.start(this, "home");
                    break;
                case RESULT_CANCELED:
                    break;
            }
            return;
        }
        if (resultCode == RESULT_OK
                && data != null) {
            if (requestCode == AppConstants.REQUEST_SELECT_APP) {
                MLogs.e("install time2 = " + System.currentTimeMillis());
                AppModel model = data.getParcelableExtra(AppConstants.EXTRA_APP_MODEL);
               // AppInstallActivity.startAppInstallActivity(this, model, drawerBlurHelper.createBitmap());
                AppCloneActivity.startAppCloneActivity(this, model);
                cloningPackage = model.getPackageName();
                mHomeFragment.hideToBottom();
            } else if (requestCode == AppConstants.REQUEST_INSTALL_APP) {

            }
        } else {
            doAnimationEnter();
        }
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }


}
