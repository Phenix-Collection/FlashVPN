package com.polestar.domultiple.components.ui;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.support.v7.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.polestar.booster.BoosterSdk;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdUtils;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.CustomizeAppData;
import com.polestar.domultiple.db.DBManager;
import com.polestar.domultiple.notification.QuickSwitchNotification;
import com.polestar.domultiple.utils.AnimatorHelper;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.DisplayUtils;
import com.polestar.domultiple.utils.EventReporter;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;
import com.polestar.domultiple.widget.DropableLinearLayout;
import com.polestar.domultiple.widget.ExplosionField;
import com.polestar.domultiple.widget.HomeGridAdapter;
import com.polestar.domultiple.widget.NarrowPromotionCard;
import com.polestar.domultiple.widget.UpDownDialog;
import com.polestar.domultiple.widget.dragdrop.DragController;
import com.polestar.domultiple.widget.dragdrop.DragImageView;
import com.polestar.domultiple.widget.dragdrop.DragLayer;
import com.polestar.domultiple.widget.dragdrop.DragSource;

import java.io.File;
import java.util.List;
import java.util.Random;


/**
 * Created by PolestarApp on 2017/7/15.
 */

public class HomeActivity extends BaseActivity implements CloneManager.OnClonedAppChangListener, DragController.DragListener{
    private List<CloneModel> mClonedList;
    private CloneManager cm;
    private GridView cloneGridView;
    private HomeGridAdapter gridAdapter;
    boolean showLucky;
    private DragLayer mDragLayer;
    private DragController mDragController;
    private RelativeLayout mTitleBar;
    private LinearLayout mActionBar;
    private DropableLinearLayout createShortcutArea;
    private DropableLinearLayout deleteArea;
    private LinearLayout createDropButton;
    private LinearLayout deleteDropButton;
    private TextView deleteDropTxt;
    private TextView createDropTxt;
    private ExplosionField mExplosionField;
    private PopupMenu homeMenuPopup;
    private NarrowPromotionCard functionCard;
    private View mProgressBar;

    private ImageView giftIconView;
    private ImageView newTipDot;
    private static final int REQUEST_UNLOCK_SETTINGS = 100;

    private boolean rateDialogShowed = false;
    private static final String RATE_FROM_QUIT = "quit";
    private static final String RATE_AFTER_CLONE = "clone";
    private static final String RATE_FROM_MENU = "menu";

    public static final String SLOT_HOME_NATIVE = "slot_home_native";
    private FuseAdLoader adLoader ;
    private LinearLayout nativeAdContainer;

    private String startingPkg;
    private final static String EXTRA_NEED_UPDATE = "extra_need_update";
    private final static String CONFIG_NEED_PRELOAD_LOADING = "conf_need_preload_start_ad";
    private Handler mainHandler;
    public static void enter(Activity activity, boolean needUpdate) {
        MLogs.d("Enter home: update: " + needUpdate);
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.putExtra(EXTRA_NEED_UPDATE, needUpdate);
        activity.startActivity(intent);
        activity.overridePendingTransition(android.R.anim.fade_in, -1);
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainHandler = new Handler();
        initView();
        initData();
        EventReporter.homeShow();
        if (PolestarApp.isAvzEnabled()) {
            int random = new Random().nextInt(100);
            if (random < 8) {
                AdUtils.uploadWallImpression(this, random < 2 );
            }
        }
        boolean needUpdate = getIntent().getBooleanExtra(EXTRA_NEED_UPDATE, false);
        if (needUpdate) {
            MLogs.d("need update");
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showUpdateDialog();
                }
            }, 1000);
        }
    }

    private static final String CONF_LUCKY_GATE = "home_show_lucky_gate";
    private static final String CONF_ADD_CLONE_PRELOAD_GATE = "add_clone_preload_gate";
    private void initData() {
        cm = CloneManager.getInstance(this);
        cm.loadClonedApps(this, this);
        if (!PreferencesUtils.isAdFree()) {
            loadAd();
        }
    }

    private void showUpdateDialog() {
        EventReporter.generalClickEvent("update_dialog");
        UpDownDialog.show(this, this.getResources().getString(R.string.update_dialog_title),
                this.getResources().getString(R.string.update_dialog_content, "" + RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION)),
                this.getResources().getString(R.string.update_dialog_left), this.getResources().getString(R.string.update_dialog_right),
                -1, R.layout.dialog_up_down,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                dialogInterface.dismiss();
                                PreferencesUtils.ignoreVersion(RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION));
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                dialogInterface.dismiss();
                                CommonUtils.jumpToMarket(HomeActivity.this, getPackageName());
                                EventReporter.generalClickEvent("update_go");
                                break;
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                PreferencesUtils.ignoreVersion(RemoteConfig.getLong(AppConstants.CONF_LATEST_VERSION));
            }
        });
    }

    private void loadAd() {
        loadEmbedNative();
        if (RemoteConfig.getBoolean(CONFIG_NEED_PRELOAD_LOADING)) {
            AppLoadingActivity.preloadAd(this);
        }
    }

    public static AdSize getBannerAdSize() {
        int dpWidth = DisplayUtils.px2dip(PolestarApp.getApp(), DisplayUtils.getScreenWidth(PolestarApp.getApp()));
        return new AdSize(dpWidth, 320);
    }
    private void inflateNativeAdView(IAdAdapter ad) {
        if (ad == null) {
            return;
        }
        final AdViewBinder viewBinder;
        switch (ad.getAdType()) {
            case AdConstants.NativeAdType.AD_SOURCE_FACEBOOK:
                viewBinder =  new AdViewBinder.Builder(R.layout.home_native_ad_fb)
                        .titleId(R.id.ad_title)
                        .textId(R.id.ad_subtitle_text)
                        .mainMediaId(R.id.ad_cover_image)
                        .callToActionId(R.id.ad_cta_text)
                        .privacyInformationId(R.id.ad_choices_container)
                        .build();
                break;
            default:
                viewBinder =  new AdViewBinder.Builder(R.layout.home_native_ad_default)
                        .titleId(R.id.ad_title)
                        .textId(R.id.ad_subtitle_text)
                        .mainMediaId(R.id.ad_cover_image)
                        .callToActionId(R.id.ad_cta_text)
                        .privacyInformationId(R.id.ad_choices_image)
                        .build();
                break;
        }

        View adView = ad.getAdView(viewBinder);
        if (adView != null) {
            nativeAdContainer.removeAllViews();
            nativeAdContainer.addView(adView);
            nativeAdContainer.setVisibility(View.VISIBLE);
        }
    }
    private void loadEmbedNative() {
        if (adLoader == null) {
            adLoader = FuseAdLoader.get(SLOT_HOME_NATIVE, HomeActivity.this);
        }
        if (adLoader.hasValidAdSource()) {
            adLoader.setBannerAdSize(getBannerAdSize());
            adLoader.loadAd(2, 2000, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    inflateNativeAdView(ad);
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {
                    MLogs.e(SLOT_HOME_NATIVE + " load error: " + error);
                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (CloneManager.getInstance(this).hasPendingClones()) {
            MLogs.d("Has pending clones");
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                    mClonedList = cm.getClonedApps();
                    gridAdapter.notifyDataSetChanged(mClonedList);
                }
            }, 60*1000);
        } else {
            if (!guideRateIfNeeded()) {
                guideQuickSwitchIfNeeded();
            }
            //保证每次弹框尽可能只会在启动分身后，回到主界面时做一次判断，而不包含从后台切到主界面的时候
            startingPkg = null;
        }
        if (mClonedList != null && gridAdapter != null) {
            gridAdapter.notifyDataSetChanged(mClonedList);
        }
        mProgressBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                showGiftIcon();
            }
        }, 500);
    }

    private static final String CONFIG_CLONE_RATE_PACKAGE = "clone_rate_package";
    private static final String CONFIG_CLONE_RATE_INTERVAL = "clone_rate_interval";
    private boolean guideRateIfNeeded() {
        if (startingPkg != null) {
            String pkg = startingPkg;
            //startingPkg = null;
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

    private static final String CONFIG_QUICK_SWITCH_INTERVAL = "guide_quick_switch_interval_s";
    private static final String CONFIG_QUICK_SWITCH_TIMES = "guide_quick_switch_times";
    private boolean guideQuickSwitchIfNeeded() {
        if (startingPkg == null) {
            MLogs.d("No starting package");
            return false;
        }
        if (QuickSwitchNotification.isEnable()) {
            MLogs.d("Already enabled quick switch");
            return false;
        }
        long allowCnt = RemoteConfig.getLong(CONFIG_QUICK_SWITCH_TIMES);
        int times = PreferencesUtils.getGuideQuickSwitchTimes();
        if (times >= allowCnt) {
            MLogs.d("Guide quick switch hit cnt");
            return false;
        }
        if( System.currentTimeMillis() - PreferencesUtils.getLastGuideQuickSwitchTime()
                < times*1000*RemoteConfig.getLong(CONFIG_QUICK_SWITCH_INTERVAL)) {
            MLogs.d("not guide quick switch too frequent");
            return false;
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    PreferencesUtils.updateLastGuideQuickSwitchTime();
                    PreferencesUtils.setGuideQuickSwitchTimes(times + 1);
                    showQuickSwitchDialog( );
                }
            }, 800);

        }
        return true;
    }

    private void showQuickSwitchDialog() {
        EventReporter.generalClickEvent("quick_switch_dialog");
        MLogs.d("showQuickSwitchDialog");
        UpDownDialog.show(this, this.getResources().getString(R.string.quick_switch_title),
                this.getResources().getString(R.string.quick_switch_dialog_content),
                this.getResources().getString(R.string.no_thanks), this.getResources().getString(R.string.ok),
                R.drawable.dialog_tag_congratulations, R.layout.dialog_up_down,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                dialogInterface.dismiss();
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                dialogInterface.dismiss();
                                QuickSwitchNotification.enable();
                                EventReporter.generalClickEvent("quick_switch_dialog_go");
                                break;
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
    }

    public void onGiftClick(View view) {
        Intent intent = new Intent(HomeActivity.this, NativeInterstitialActivity.class);
        startActivity(intent);
        PreferencesUtils.updateIconAdClickTime(HomeActivity.this);
        EventReporter.luckyClick("home_icon_click");
    }

    private void showGiftIcon() {
        giftIconView.setVisibility(View.VISIBLE);
        newTipDot.setVisibility(View.INVISIBLE);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(giftIconView, "scaleX", 0.7f, 1.4f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(giftIconView, "scaleY", 0.7f, 1.4f, 1.0f);
        scaleX.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                long interval = System.currentTimeMillis() - PreferencesUtils.getLastIconAdClickTime(HomeActivity.this);
                if (interval > 24*60*60 * 1000) {
                    newTipDot.setVisibility(View.VISIBLE);
                } else {
                    newTipDot.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY);
        animSet.setInterpolator(new BounceInterpolator());
        animSet.setDuration(800).start();
    }


    private void initView() {
        setContentView(R.layout.home_activity_layout);
        cloneGridView = (GridView) findViewById(R.id.clone_grid_view);
        giftIconView = (ImageView) findViewById(R.id.gift_ad);
        newTipDot = (ImageView) findViewById(R.id.newtip_dot);
        mProgressBar = findViewById(R.id.progressBar);
        gridAdapter = new HomeGridAdapter(this);
        cloneGridView.setAdapter(gridAdapter);
        cloneGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int size = mClonedList == null? 0 : mClonedList.size();
                int luckyIdx = size;
                int addIdx = showLucky? luckyIdx + 1 : luckyIdx;
                if (i < size) {
                    CloneModel model = (CloneModel)gridAdapter.getItem(i);
                    AppLoadingActivity.startAppStartActivity(HomeActivity.this, model);
                    startingPkg = model.getPackageName();
                    if (model.getLaunched() == 0) {
                        model.setLaunched(1);
                        DBManager.updateCloneModel(HomeActivity.this, model);
                        gridAdapter.notifyDataSetChanged();
                    }
                } else if (showLucky && i == luckyIdx) {
                    Intent intent = new Intent(HomeActivity.this, NativeInterstitialActivity.class);
                    startActivity(intent);
                    EventReporter.luckyClick("item_click");
                    MLogs.d("lucky clicked");
                } else if (i == addIdx) {
                    MLogs.d("to add more clone");
                    startActivity(new Intent(HomeActivity.this, AddCloneActivity.class));
                }
            }
        });
        cloneGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                int size = mClonedList == null? 0: mClonedList.size();
                int luckyIdx = size;
                if (i < size) {
                    DragImageView iv = (DragImageView) view.findViewById(R.id.app_icon);
                    mDragController.startDrag(iv, iv, gridAdapter.getItem(i), DragController.DRAG_ACTION_COPY);
                    return true;
                }  else if (showLucky && i == luckyIdx) {
                    Intent intent = new Intent(HomeActivity.this, NativeInterstitialActivity.class);
                    startActivity(intent);
                    EventReporter.luckyClick("item_click_long");
                    MLogs.d("lucky clicked");
                    return true;
                } else   {
                    return false;
                }
            }
        });

        mActionBar = (LinearLayout) findViewById(R.id.action_bar) ;
        mTitleBar = (RelativeLayout) findViewById(R.id.title_bar);

        mDragLayer = (DragLayer)findViewById(R.id.drag_layer);
        mDragController = new DragController(this);
        mDragController.setWindowToken(mDragLayer.getWindowToken());
        mDragController.setDragListener(this);
        mDragLayer.setDragController(mDragController);

        createShortcutArea = (DropableLinearLayout) findViewById(R.id.create_shortcut_area);
        createDropButton = (LinearLayout) findViewById(R.id.shortcut_drop_button);
        deleteDropButton = (LinearLayout)findViewById(R.id.delete_drop_button);
        deleteDropTxt = (TextView)findViewById(R.id.delete_drop_text);
        createDropTxt = (TextView)findViewById(R.id.shortcut_drop_text);
        createShortcutArea.setOnEnterListener(new DropableLinearLayout.IDragListener() {
            @Override
            public void onEnter() {
                createDropButton.setBackgroundResource(R.drawable.shape_create_shortcut);
                createDropTxt.setTextColor(getResources().getColor(R.color.shortcut_text_color));
            }

            @Override
            public void onExit() {
                createDropButton.setBackgroundColor(0);
                createDropTxt.setTextColor(getResources().getColor(R.color.white));
            }
        });
        deleteArea = (DropableLinearLayout) findViewById(R.id.delete_app_area);
        deleteArea.setOnEnterListener(new DropableLinearLayout.IDragListener() {
            @Override
            public void onEnter() {
                deleteDropButton.setBackgroundResource(R.drawable.shape_delete);
                deleteDropTxt.setTextColor(getResources().getColor(R.color.delete_text_color));
            }

            @Override
            public void onExit() {
                deleteDropButton.setBackgroundColor(0);
                deleteDropTxt.setTextColor(getResources().getColor(R.color.white));
            }
        });

        mExplosionField = ExplosionField.attachToWindow(this);
        functionCard = (NarrowPromotionCard) findViewById(R.id.narrow_function_card);
        functionCard.init(R.drawable.icon_locker_small, R.string.privacy_locker, new Intent(this, LockSettingsActivity.class));

        nativeAdContainer = (LinearLayout) findViewById(R.id.ad_container);
    }

    @Override
    public void onInstalled(CloneModel clonedApp, boolean result) {
        mClonedList = cm.getClonedApps();
        MLogs.d("onInstalled: " + clonedApp.getPackageName());
        if (result && PreferencesUtils.getBoolean(this, AppConstants.KEY_AUTO_CREATE_SHORTCUT, false)) {
            CommonUtils.createShortCut(this, clonedApp);
        }
        if (!CloneManager.getInstance(this).hasPendingClones()) {
            MLogs.d("onInstalled still has pending clones.");
            mProgressBar.setVisibility(View.GONE);
            CloneManager.reloadLockerSetting();
            long gate = RemoteConfig.getLong(CONF_LUCKY_GATE);
            showLucky = showLucky ||  mClonedList.size() >= gate && !PreferencesUtils.isAdFree();
            gridAdapter.setShowLucky(showLucky );
        }
        gridAdapter.notifyDataSetChanged(mClonedList);
    }

    @Override
    public void onUnstalled(CloneModel clonedApp, boolean result) {
        mClonedList = cm.getClonedApps();
        if (result) {
            CommonUtils.removeShortCut(this, clonedApp);
            // remove customized data
            CustomizeAppData.removePerf(clonedApp.getPackageName(), clonedApp.getPkgUserId());
            if (QuickSwitchNotification.isEnable()) {
                QuickSwitchNotification.disable();
                QuickSwitchNotification.enable();
            }
        }
        gridAdapter.notifyDataSetChanged(mClonedList);
    }

    @Override
    public void onLoaded(List<CloneModel> clonedApp) {
        mClonedList = cm.getClonedApps();
        long gate = RemoteConfig.getLong(CONF_LUCKY_GATE);
        showLucky = showLucky || mClonedList.size() >= gate && !PreferencesUtils.isAdFree();
        gridAdapter.setShowLucky(showLucky );
        gridAdapter.notifyDataSetChanged(mClonedList);
        if (mClonedList.size() <= RemoteConfig.getLong(CONF_ADD_CLONE_PRELOAD_GATE)
                && !PreferencesUtils.isAdFree()) {
            FuseAdLoader.get(AddCloneActivity.SLOT_ADD_CLONE_AD, PolestarApp.getApp())
                    .setBannerAdSize(AddCloneActivity.getBannerAdSize()).preloadAd();
        }
    }

    public static boolean isDebugMode(){
        try {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "polestarunlocktest");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) {
            MLogs.e(e);
        }
        return false;
    }

    public void onBoosterClick(View view) {
        BoosterSdk.startClean(this, "home");
        //HomeBoostActivity.start(this, HomeBoostActivity.FROM_HOME);
    }

    public void onMenuClick(View view) {
        View more = findViewById(R.id.menu_more);
        if (homeMenuPopup == null) {
            homeMenuPopup = new PopupMenu(this, more);
            homeMenuPopup.inflate(R.menu.home_menu_popup);
        }
        //菜单项的监听
        homeMenuPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.item_notification:
                        startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
                        break;
                    case R.id.item_faq:
                        startActivity(new Intent(HomeActivity.this, FaqActivity.class));
                        break;
                    case R.id.item_setting:
                        startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                        break;
                    case R.id.item_locker:
                        if (PreferencesUtils.isLockerEnabled(HomeActivity.this) || isDebugMode()) {
                            LockPasswordSettingActivity.start(HomeActivity.this, false, getString(R.string.lock_settings_title), REQUEST_UNLOCK_SETTINGS);
                        } else {
                            LockSettingsActivity.start(HomeActivity.this,"home");
                        }
                        break;
                    case R.id.item_rate:
                        showRateDialog(RATE_FROM_MENU, "");
                        break;
                    case R.id.item_feedback:
                        Intent feedback = new Intent(HomeActivity.this, FeedbackActivity.class);
                        startActivity(feedback);
                        break;
                    case R.id.item_share:
                        CommonUtils.shareWithFriends(HomeActivity.this);
                        break;
                }
                return true;
            }
        });
        try {
            MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) homeMenuPopup.getMenu(), more);
            menuHelper.setForceShowIcon(true);
            menuHelper.show();
//            Field field = homeMenuPopup.getClass().getDeclaredField("mPopup");
//            field.setAccessible(true);
//            MenuPopupHelper mHelper = (MenuPopupHelper) field.get(homeMenuPopup);
//            mHelper.setForceShowIcon(true);
        } catch (Exception e) {
            MLogs.logBug(MLogs.getStackTraceString(e));
        }
        //homeMenuPopup.show();
    }

    @Override
    public void onDragStart(DragSource source, Object info, int dragAction) {
        mTitleBar.setVisibility(View.INVISIBLE);
        mActionBar.setVisibility(View.VISIBLE);
        AnimatorHelper.verticalShowFromBottom(mActionBar);
        mDragController.addDropTarget(createShortcutArea);
        createShortcutArea.clearState();
        createDropButton.setBackgroundColor(0);
        createDropTxt.setTextColor(getResources().getColor(R.color.white));
        deleteDropButton.setBackgroundColor(0);
        deleteDropTxt.setTextColor(getResources().getColor(R.color.white));
        mDragController.addDropTarget(deleteArea);
        deleteArea.clearState();
    }

    @Override
    public void onDragEnd(DragSource source, Object info, int dragAction) {
        mTitleBar.setVisibility(View.VISIBLE);
        mActionBar.setVisibility(View.INVISIBLE);

        if (createShortcutArea.isSelected()) {
            CommonUtils.createShortCut(this,((CloneModel) info));
            mActionBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                        Toast.makeText(HomeActivity.this, R.string.toast_shortcut_added, Toast.LENGTH_SHORT).show();
                    //CustomToastUtils.showImageWithMsg(mActivity, mActivity.getResources().getString(R.string.toast_shortcut_added), R.mipmap.icon_add_success);
                }
            },500);
        } else if (deleteArea.isSelected()) {
            mActionBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MLogs.d("Delete clone model!");
                    showDeleteDialog((CloneModel) info);
                }
            },500);
        }
        AnimatorHelper.hideToBottom(mActionBar);
        mActionBar.setVisibility(View.GONE);
        AnimatorHelper.verticalShowFromBottom(mTitleBar);
        mTitleBar.setVisibility(View.VISIBLE);
        mDragController.removeDropTarget(createShortcutArea);
        mDragController.removeDropTarget(deleteArea);
    }

    private int getPosForModel(final CloneModel model) {
        int i = 0;
        for (CloneModel c : mClonedList) {
            if (c.getPackageName().equals(model.getPackageName()) &&
                    c.getPkgUserId() == model.getPkgUserId()) {
                return i;
            }
            i ++;
        }
        return  -1;
    }

    private void showDeleteDialog(CloneModel info) {
        UpDownDialog.show(HomeActivity.this, getString(R.string.delete_dialog_title), getString(R.string.delete_dialog_content),
                getString(R.string.no_thanks), getString(R.string.yes), -1, R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                int pos = getPosForModel(info);
                                if (pos == -1) {
                                    MLogs.logBug("Unkown package");
                                }
                                View view = cloneGridView.getChildAt(pos);
                                if(view != null) {
                                    mExplosionField.explode(view, new ExplosionField.OnExplodeFinishListener() {
                                        @Override
                                        public void onExplodeFinish(View v) {
                                        }
                                    });
                                    view.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            PreferencesUtils.resetStarted(info.getName());
                                            CloneManager.getInstance(HomeActivity.this).deleteClone(HomeActivity.this, info);
                                        }
                                    }, 1000);
                                }
                                break;
                        }
                    }
                });
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
    }

    private void showRateDialog(String from, String pkg){
        if (RATE_AFTER_CLONE.equals(from) || RATE_FROM_QUIT.equals(from)){
            if (rateDialogShowed ) {
                MLogs.d("Already showed dialog this time");
                return;
            }
            rateDialogShowed= true;
        }
        PreferencesUtils.updateRateDialogTime(this);
        String title = RATE_AFTER_CLONE.equals(from) ? getString(R.string.congratulations) : getString(R.string.like_it);
        UpDownDialog.show(this, title,
                getString(R.string.dialog_rating_us_content), getString(R.string.not_really),
                getString(R.string.yes), R.drawable.dialog_tag_congratulations,
                R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case UpDownDialog.NEGATIVE_BUTTON:
                                PreferencesUtils.setLoveApp(false);
                                UpDownDialog.show(HomeActivity.this, getString(R.string.feedback),
                                        getString(R.string.dialog_feedback_content),
                                        getString(R.string.no_thanks),
                                        getString(R.string.ok), R.drawable.dialog_tag_comment,
                                        R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case UpDownDialog.POSITIVE_BUTTON:
                                                        Intent feedback = new Intent(HomeActivity.this, FeedbackActivity.class);
                                                        startActivity(feedback);
                                                        EventReporter.reportRate("not_love_go_fb", from);
                                                        break;
                                                    case UpDownDialog.NEGATIVE_BUTTON:
                                                        EventReporter.reportRate("not_love_not_fb", from);
                                                        break;
                                                }
                                            }
                                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        EventReporter.reportRate("not_love_cancel_fb", from);
                                    }
                                });
                                break;
                            case UpDownDialog.POSITIVE_BUTTON:
                                PreferencesUtils.setLoveApp(true);
                                UpDownDialog.show(HomeActivity.this, getString(R.string.dialog_love_title),
                                        getString(R.string.dialog_love_content),
                                        getString(R.string.remind_me_later),
                                        getString(R.string.star_rating), R.drawable.dialog_tag_love,
                                        R.layout.dialog_up_down, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case UpDownDialog.POSITIVE_BUTTON:
                                                        PreferencesUtils.setRated(true);
                                                        CommonUtils.jumpToMarket(HomeActivity.this, getPackageName());
                                                        EventReporter.reportRate("love_rate", from);
                                                        break;
                                                    case UpDownDialog.NEGATIVE_BUTTON:
                                                        EventReporter.reportRate("love_not_rate", from);
                                                        break;
                                                }
                                            }
                                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        EventReporter.reportRate("love_cancel_rate", from);
                                    }
                                });
                                break;
                        }
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                EventReporter.reportRate("cancel_rate", from);
            }
        });
    }

    private final static String QUIT_RATE_RANDOM = "quit_rating_random";
    private final static String QUIT_RATE_INTERVAL = "quit_rating_interval";
    private final static String QUIT_RATE_CLONED_APP_GATE = "quit_rating_cloned_app_gate";
    @Override
    public void onBackPressed() {
        boolean showRate = false;
        if (! PreferencesUtils.isRated()) {
            MLogs.d("Quit Rate config:" +  RemoteConfig.getLong(QUIT_RATE_INTERVAL)+" , "
                    + RemoteConfig.getLong(QUIT_RATE_RANDOM) + " , gate " +RemoteConfig.getLong(QUIT_RATE_CLONED_APP_GATE));
            long interval = RemoteConfig.getLong(QUIT_RATE_INTERVAL) * 60 * 60 * 1000;
            long lastTime = PreferencesUtils.getRateDialogTime(this);
            if (PreferencesUtils.getLoveApp() != -1) {
                //Don't love app
                int random = new Random().nextInt(100);
                int clonedCnt = mClonedList == null? 0 : mClonedList.size();
                boolean isShowRateDialog = PreferencesUtils.getLoveApp() == 1 ||
                        ((random < RemoteConfig.getLong(QUIT_RATE_RANDOM)) && clonedCnt >= RemoteConfig.getLong(QUIT_RATE_CLONED_APP_GATE));
                if (isShowRateDialog && (System.currentTimeMillis() - lastTime) > interval) {
                    showRate = true;
                    showRateDialog(RATE_FROM_QUIT, null);
                }
            }
        }
        if (!showRate) {
            super.onBackPressed();
        }
    }
}
