package com.polestar.superclone.component.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.superclone.MApp;
import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseFragment;
import com.polestar.superclone.component.activity.AppListActivity;
import com.polestar.superclone.component.activity.AppStartActivity;
import com.polestar.superclone.component.activity.HomeActivity;
import com.polestar.superclone.component.activity.NativeInterstitialActivity;
import com.polestar.superclone.constant.AppConstants;
import com.polestar.superclone.db.DbManager;
import com.polestar.superclone.model.AppModel;
import com.polestar.clone.CustomizeAppData;
import com.polestar.superclone.reward.IconAdConfig;
import com.polestar.superclone.reward.TaskExecutor;
import com.polestar.superclone.utils.AnimatorHelper;
import com.polestar.superclone.utils.CloneHelper;
import com.polestar.superclone.utils.CommonUtils;
import com.polestar.superclone.utils.DisplayUtils;
import com.polestar.superclone.utils.ExplosionField;
import com.polestar.superclone.utils.MLogs;
import com.polestar.superclone.utils.EventReporter;
import com.polestar.superclone.utils.PermissionManager;
import com.polestar.superclone.utils.PreferencesUtils;
import com.polestar.superclone.utils.RemoteConfig;
import com.polestar.superclone.widgets.LeftRightDialog;
import com.polestar.superclone.widgets.CustomFloatView;
import com.polestar.superclone.widgets.GridAppCell;
import com.polestar.superclone.widgets.HeaderGridView;
import com.polestar.superclone.widgets.TutorialGuides;
import com.polestar.superclone.widgets.dragdrop.DragController;
import com.polestar.superclone.widgets.dragdrop.DragImageView;
import com.polestar.superclone.widgets.dragdrop.DragLayer;
import com.polestar.superclone.widgets.dragdrop.DragSource;
import com.polestar.task.database.datamodels.AdTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


/**
 * Created by yxx on 2016/7/19.
 */
public class HomeFragment extends BaseFragment {
    private View contentView;
    private HeaderGridView pkgGridView;
    private PackageGridAdapter pkgGridAdapter;
    private List<AppModel> appInfos = new ArrayList<>();
    private CustomFloatView floatView;
    private ExplosionField mExplosionField;
    private DragController mDragController;
    private DragLayer mDragLayer;
    private FrameLayout nativeAdContainer;
    private AppModel mPendingStart;
    private AppModel mLastClone;

    private IAdAdapter nativeAd;
    private long adShowTime = 0;

    private FuseAdLoader mNativeAdLoader;
    private FuseAdLoader mApplistAdLoader;
    private static final String CONFIG_HOME_NATIVE_PRIOR_TIME = "home_native_prior_time";
    private static final String CONFIG_HOME_SHOW_LUCKY_RATE = "home_show_lucky_rate";
    private static final String CONFIG_HOME_PRELOAD_APPLIST_GATE= "home_preload_applist_gate";


    private boolean showLucky;

    public static final String SLOT_APP_ICON_AD = "slot_app_icon";

    private IAdAdapter iconAd;
    private IconAdConfig iconAdConfig;

    private List<HomeGridItem> gridItems;


    public void inflateNativeAd(IAdAdapter ad) {
//        if (adShowed) {
//            return;
//        }
        if (mActivity != null && ad != null) {
            int layout = RemoteConfig.getBoolean("conf_new_home_adlayout") ?
                    R.layout.new_front_page_native_ad:R.layout.front_page_native_ad;
            final AdViewBinder viewBinder = new AdViewBinder.Builder(layout)
                    .titleId(R.id.ad_title)
                    .textId(R.id.ad_subtitle_text)
                    .mainMediaId(R.id.ad_cover_image)
                    .iconImageId(R.id.ad_icon_image)
                    .fbMediaId(R.id.ad_fb_mediaview)
                    .admMediaId(R.id.ad_adm_mediaview)
                    .callToActionId(R.id.ad_cta_text)
                    .privacyInformationId(R.id.ad_choices_image)
                    .adFlagId(R.id.ad_flag)
                    .build();
            View adView = ad.getAdView(mActivity, viewBinder);
            nativeAdContainer.removeAllViews();
            nativeAdContainer.addView(adView);
            nativeAdContainer.setVisibility(View.VISIBLE);
            adShowTime = System.currentTimeMillis();
            pkgGridAdapter.notifyDataSetChanged();
            dismissLongClickGuide();
        }
    }

    public void hideAd() {
        nativeAdContainer.removeAllViews();
        showLucky = false;
        if (pkgGridAdapter != null) {
            pkgGridAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iconAdConfig = new IconAdConfig();
        if (gridItems == null) {
            gridItems = new ArrayList<>();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (contentView == null ) {
            contentView = inflater.inflate(R.layout.fragment_home, null);
            initView();
            mDragController = new DragController(mActivity);
            mDragController.setDragListener(mDragListener);
            mDragController.setWindowToken(contentView.getWindowToken());
            mDragLayer.setDragController(mDragController);
        }
        initData();
        return contentView;
    }

    DragController.DragListener mDragListener = new DragController.DragListener() {
        @Override
        public void onDragStart(DragSource source, Object info, int dragAction) {
            AppModel model = (AppModel)((HomeGridItem)info).obj;
            MLogs.d("onDragStart " + model.getName() + " ");
            floatView.animToExpand();
            mDragController.addDropTarget(floatView);
        }

        @Override
        public void onDragEnd(DragSource source, Object info, int action) {
            AppModel model = (AppModel)((HomeGridItem)info).obj;
            MLogs.d("onDragEnd "+ model.getName() + " " + floatView.getSelectedState());
            int state = floatView.getSelectedState();
            floatView.animToIdel();
            ((HomeGridItem)info).onDragDrop((View) source, state);
            mDragController.removeDropTarget(floatView);
        }
    };

    private class PackageGridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            int size = gridItems.size();
            return size + 3;
        }

        @Override
        public Object getItem(int position) {
            if (position < gridItems.size() && position >= 0) {
                return  gridItems.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            HomeGridItem item = (HomeGridItem) getItem(i);
            if (item != null) {
                return item.getView();
            } else {
                View itemView = new GridAppCell(mActivity);
                itemView.setVisibility(View.INVISIBLE);
                return itemView;
            }
        }
    }

    private void initView() {
        //nativeAdContainer = (LinearLayout) mActivity.findViewById(R.id.native_ad_container);
        nativeAdContainer = new FrameLayout(mActivity);
        nativeAdContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        nativeAdContainer.setOrientation(LinearLayout.VERTICAL);

        mDragLayer = (DragLayer)contentView.findViewById(R.id.drag_layer);
        pkgGridView = (HeaderGridView) contentView.findViewById(R.id.grid_app);
        pkgGridView.addHeaderView(nativeAdContainer);
        pkgGridAdapter = new PackageGridAdapter();
//        pkgGridView.setLayoutAnimation(getGridLayoutAnimController());
        pkgGridView.setAdapter(pkgGridAdapter);

        floatView = (CustomFloatView) contentView.findViewById(R.id.addApp_btn);
        floatView.startBreath();

        floatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAppListActivity();
                PreferencesUtils.setCloneGuideShowed(mActivity);
                EventReporter.homeAdd(mActivity);
            }
        });

        pkgGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                //final int i = pkgGridAdapter.getNatureIndex(position);
                int i =pos - pkgGridView.getGridItemStartOffset();
                MLogs.d("onItemClick " + i);
                HomeGridItem item = (HomeGridItem) pkgGridAdapter.getItem(i);
                if (item != null) {
                    item.onClick(view);
                }
            }
        });
        pkgGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
                int i = pos - pkgGridView.getGridItemStartOffset();
                MLogs.d("onItemLongClick " + i);
                HomeGridItem item = (HomeGridItem) pkgGridAdapter.getItem(i);
                return item == null? false : item.onLongClick(view);

            }
        });
    }

    private class HomeGridItem{
        public static final int TYPE_LUCKY = 0;
        public static final int TYPE_CLONE = 1;
        public static final int TYPE_ICON_AD = 2;
        int type;
        Object obj;
        private View inflateView;

        public HomeGridItem(int type, Object obj) {
            this.type = type;
            this.obj = obj;
        }

        public View getView() {
            View inflateView = null;
            switch (type) {
                case TYPE_CLONE:
                case TYPE_LUCKY:
                    inflateView = new GridAppCell(mActivity);
                    break;
                case TYPE_ICON_AD:
                    AdViewBinder viewBinder = new AdViewBinder.Builder(R.layout.item_app)
                            .iconImageId(R.id.app_icon)
                            .titleId(R.id.app_name).build();
                    inflateView = ((IAdAdapter)obj).getAdView(mActivity, viewBinder);
                    break;
            }
            ImageView appIcon = (ImageView) inflateView.findViewById(R.id.app_icon);
            TextView appName = (TextView) inflateView.findViewById(R.id.app_name);
            TextView adFlag = (TextView) inflateView.findViewById(R.id.ad_flag);
            if (type == TYPE_ICON_AD || type == TYPE_LUCKY) {
                adFlag.setVisibility(View.VISIBLE);
            } else {
                adFlag.setVisibility(View.INVISIBLE);
            }
            if (type == TYPE_CLONE) {
                AppModel appModel = (AppModel)obj;
                CustomizeAppData data = CustomizeAppData.loadFromPref(appModel.getPackageName(),
                        appModel.getPkgUserId());
                Bitmap bmp = data.getCustomIcon();
                appIcon.setImageBitmap(bmp);
                appModel.setCustomIcon(bmp);
                appName.setText(data.customized? data.label: appModel.getName());
            } else if (type == TYPE_LUCKY) {
                boolean showAdFlag = RemoteConfig.getBoolean("conf_adflag_for_icon");
                if (!showAdFlag) {
                    adFlag.setVisibility(View.INVISIBLE);
                }
                appIcon.setImageResource(R.drawable.icon_feel_lucky);
                appName.setText(R.string.feel_lucky);
                appName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                appName.setTextColor(getResources().getColor(R.color.lucky_red));
            } else if (type == TYPE_ICON_AD) {
                inflateView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return HomeGridItem.this.onLongClick(v);
                    }
                });
            }
            this.inflateView = inflateView;
            return inflateView;
        }

        public void onClick(View view) {
            switch (type) {
                case TYPE_CLONE:
                    AppModel model = (AppModel) obj;
                    PermissionManager permissionManager = new PermissionManager(mActivity, HomeActivity.REQUEST_APPLY_PERMISSION);
                    if (permissionManager.applyPermissionIfNeeded()) {
                        mPendingStart = model;
                    } else {

                        if (floatView.isIdle()) {
                            startAppLaunchActivity(model.getPackageName(), model.getPkgUserId());
                        } else {
                            floatView.restore();
                            floatView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startAppLaunchActivity(model.getPackageName(), model.getPkgUserId());
                                }
                            }, 100);
                        }
                    }
                    break;
                case TYPE_LUCKY:
//                    PoleNativeAdapter.goUrl(mActivity, "https://play.google.com/store/apps/details?id=com.polestar.super.clone", true);
//                    PoleNativeAdapter.goUrl(mActivity, "market://details?id=com.polestar.minesweeper&referrer?UTM_SOURCE=spc_update", true);
                    MLogs.d("Show lucky");
                    Intent intent = new Intent(mActivity, NativeInterstitialActivity.class);
                    startActivity(intent);
                    EventReporter.homeGiftClick(mActivity, "lucky_item");
                    break;
                case TYPE_ICON_AD:
                    MLogs.d("TYPE_ICON_AD onClick should not be here!");
                    break;
            }
        }

        public boolean onLongClick(View view) {
            DragImageView iv;
            switch (type){
                case TYPE_CLONE:
                    iv = (DragImageView) view.findViewById(R.id.app_icon);
                    floatView.setLeftBtnRes(R.mipmap.icon_add);
                    floatView.setRightBtnRes(R.mipmap.icon_delete);
                    mDragController.startDrag(iv, iv, this, DragController.DRAG_ACTION_COPY);
                    return true;
                case TYPE_ICON_AD:
                    iv = (DragImageView) view.findViewById(R.id.app_icon);
                    floatView.setLeftBtnRes(R.mipmap.icon_download);
                    floatView.setRightBtnRes(R.mipmap.icon_delete);
                    mDragController.startDrag(iv, iv, this, DragController.DRAG_ACTION_COPY);
                    return true;
                case TYPE_LUCKY:
                    MLogs.d("Show lucky");
                    Intent intent = new Intent(mActivity, NativeInterstitialActivity.class);
                    startActivity(intent);
                    EventReporter.homeGiftClick(mActivity, "lucky_item_long");
                    return false;
            }
            return false;
        }

        public void onDragDrop(View dropView, int state) {
            switch (state) {
                case CustomFloatView.SELECT_BTN_LEFT:
                    if (type == TYPE_CLONE) {
                        EventReporter.addShortCut(mActivity, ((AppModel) obj).getPackageName());
                        CommonUtils.createShortCut(mActivity, ((AppModel) obj));
                        floatView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity, R.string.toast_shortcut_added, Toast.LENGTH_SHORT).show();
                                //CustomToastUtils.showImageWithMsg(mActivity, mActivity.getResources().getString(R.string.toast_shortcut_added), R.mipmap.icon_add_success);
                            }
                        }, CustomFloatView.ANIM_DURATION / 2);
                    } else if (type == TYPE_ICON_AD) {
                        if (inflateView != null) {
                            inflateView.performClick();
                        }
                    }
                    break;
                case CustomFloatView.SELECT_BTN_RIGHT:
                    if (type == TYPE_CLONE) {
                       // pkgGridAdapter.notifyDataSetChanged();
                        final View drop = inflateView == null ? null :inflateView.findViewById(R.id.app_icon);
                        floatView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!PreferencesUtils.hasShownDeleteDialog()) {
                                    showDeleteDialog(drop, (AppModel) obj);
                                } else {
                                    deleteAppWithAnim(drop, (AppModel) obj);
                                }
                            }
                        }, 330);
                    } else if (type == TYPE_ICON_AD) {
                        AdTask task = (AdTask)((IAdAdapter)obj).getAdObject();
                        task.ignoreFor(mActivity, SLOT_APP_ICON_AD, iconAdConfig.ignoreInterval);
                        iconAd = null;
                        mExplosionField = ExplosionField.attachToWindow(mActivity);
                        if (inflateView != null) {
                            inflateView.setVisibility(View.INVISIBLE);
                            View view = inflateView.findViewById(R.id.app_icon);
                            mExplosionField.explode(view, new ExplosionField.OnExplodeFinishListener() {
                                @Override
                                public void onExplodeFinish(View v) {
                                    ExplosionField.detachFromWindow(mActivity, mExplosionField);
                                    mExplosionField = null;
//                                    gridItems.remove(HomeGridItem.this);
//                                    pkgGridAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                        floatView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                gridItems.remove(HomeGridItem.this);
                                pkgGridAdapter.notifyDataSetChanged();
                            }
                        }, 1000);


                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static AdSize getBannerSize() {
        int dpWidth = DisplayUtils.px2dip(MApp.getApp(), DisplayUtils.getScreenWidth(MApp.getApp()));
        dpWidth = dpWidth < 290 ? dpWidth : dpWidth-10;
        return new AdSize(dpWidth, 135);
    }

    private TutorialGuides.Builder mTutorialBuilder;

    private void showCloneAppGuide(){
        //TutorialGuidesUtils.removeOnGlobalLayoutListener(pkgGridView,this);
        try {
            if (PreferencesUtils.hasCloned()) {
                PreferencesUtils.setCloneGuideShowed(mActivity);
                return;
            }
            String text = getString(R.string.start_tips);
            mTutorialBuilder = new TutorialGuides.Builder(mActivity);

            mTutorialBuilder.anchorView(floatView);
            mTutorialBuilder.defaultMaxWidth(true);
            mTutorialBuilder.onShowListener(new TutorialGuides.OnShowListener() {
                @Override
                public void onShow(TutorialGuides tooltip) {
                    PreferencesUtils.setCloneGuideShowed(mActivity);
                }
            });
            mTutorialBuilder.text(text)
                    .gravity(Gravity.TOP)
                    .build()
                    .show();
        }catch (Exception e){
            MLogs.e("error to show guides");
            MLogs.e(e);
        }
    }

    private TutorialGuides longClickGuide = null;
    private void showLongClickItemGuide(){
        try {
            String text = getString(R.string.long_press_tips);
            mTutorialBuilder = new TutorialGuides.Builder(mActivity);
            mTutorialBuilder.anchorView(pkgGridView.getChildAt(0+pkgGridView.getGridItemStartOffset()));
            mTutorialBuilder.defaultMaxWidth(true);
            mTutorialBuilder.onShowListener(new TutorialGuides.OnShowListener() {
                @Override
                public void onShow(TutorialGuides tooltip) {
                    PreferencesUtils.setLongClickGuideShowed(mActivity);
                }
            });
            longClickGuide = mTutorialBuilder.text(text)
                    .gravity(Gravity.BOTTOM)
                    .build();
            longClickGuide.show();
        }catch (Exception e){
            MLogs.e("error to showLongClickItemGuide");
            MLogs.e(e);
        }
    }

    private void dismissLongClickGuide() {
        if (longClickGuide !=null) longClickGuide.dismiss();
    }


    private void loadAppIconAd() {
        FuseAdLoader.get(SLOT_APP_ICON_AD, mActivity).loadAd(mActivity, 1, 0, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAdAdapter ad) {
                iconAd = ad;
                insertIconAd();
            }

            @Override
            public void onAdClicked(IAdAdapter ad) {
                MLogs.d("app_icon_ad on click");
            }

            @Override
            public void onAdClosed(IAdAdapter ad) {

            }

            @Override
            public void onAdListLoaded(List<IAdAdapter> ads) {

            }

            @Override
            public void onError(String error) {
                MLogs.d("Load icon ad error: " + error);
            }

            @Override
            public void onRewarded(IAdAdapter ad) {

            }
        });
    }
    private void loadHeadNativeAd() {
        if (mNativeAdLoader == null) {
            mNativeAdLoader = FuseAdLoader.get(SLOT_HOME_HEADER_NATIVE, mActivity.getApplicationContext());
            mNativeAdLoader.setBannerAdSize(getBannerSize());
        }

        if ( mNativeAdLoader.hasValidAdSource()) {
            mNativeAdLoader.loadAd(mActivity, 2, RemoteConfig.getLong(CONFIG_HOME_NATIVE_PRIOR_TIME), new IAdLoadListener() {
                @Override
                public void onAdClicked(IAdAdapter ad) {

                }

                @Override
                public void onAdClosed(IAdAdapter ad) {

                }

                @Override
                public void onRewarded(IAdAdapter ad) {

                }

                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    nativeAd = ad;
                    inflateNativeAd(ad);
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {
                    MLogs.d("Home ad error: " + error);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (appInfos.size() > 0) {
            pkgGridView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mActivity != null) {
                        if (appInfos.size() > 0 && !PreferencesUtils.hasShownLongClickGuide(mActivity)) {
                            showLongClickItemGuide();
                        }
                    }
                }
            }, 1500);
        }
        if (PreferencesUtils.isAdFree()) {
            hideAd();
        } else {
            boolean showHeaderAd = RemoteConfig.getBoolean(KEY_HOME_SHOW_HEADER_AD)
                    && PreferencesUtils.hasCloned();
            MLogs.d(KEY_HOME_SHOW_HEADER_AD + showHeaderAd);
            if (showHeaderAd && System.currentTimeMillis() - adShowTime > RemoteConfig.getLong("home_ad_refresh_interval_s")*1000) {
                loadHeadNativeAd();
            }
            if (RemoteConfig.getBoolean(AppStartActivity.CONFIG_NEED_PRELOAD_LOADING)) {
                AppStartActivity.preloadAd(mActivity);
            }

        }
//        else {
//            boolean showHeaderAd = RemoteConfig.getBoolean(KEY_HOME_SHOW_HEADER_AD)
//                    && PreferencesUtils.hasCloned();
//            MLogs.d(KEY_HOME_SHOW_HEADER_AD + showHeaderAd);
//            headerNativeAdConfigs = RemoteConfig.getAdConfigList(SLOT_HOME_HEADER_NATIVE);
//            if (showHeaderAd && nativeAd == null && headerNativeAdConfigs.size() > 0) {
//                loadHeadNativeAd();
//            }
//
//        }
        if (pkgGridAdapter != null) {
            pkgGridAdapter.notifyDataSetChanged();
        }
        if (mPendingStart != null) {
            startAppLaunchActivity(mPendingStart.getPackageName(),mPendingStart.getPkgUserId());
            mPendingStart = null;
        }
    }

    private static final String KEY_HOME_SHOW_HEADER_AD = "home_show_header_ad";
    public static final String SLOT_HOME_HEADER_NATIVE = "slot_home_header_native";

    private void updateGridItemList(List<AppModel> appModels) {
        gridItems.clear();
        long luckyRate = RemoteConfig.getLong(CONFIG_HOME_SHOW_LUCKY_RATE);
        showLucky = (!PreferencesUtils.isAdFree())
                && PreferencesUtils.hasCloned()
                && new Random().nextInt(100) < luckyRate ;
        if (iconAd != null) {
            gridItems.add(new HomeGridItem(HomeGridItem.TYPE_ICON_AD, iconAd));
        }
        if (RemoteConfig.getBoolean("conf_sort_home_icon")) {
            HashMap<String, ArrayList<AppModel>> sortMap = new HashMap<>();
            ArrayList<String> sortedPackages = new ArrayList<>();
            if (appModels != null) {
                for (AppModel model : appModels) {
                    ArrayList<AppModel> list = sortMap.get(model.getPackageName());
                    if (list == null) {
                        list = new ArrayList();
                        sortedPackages.add(model.getPackageName());
                    }
                    list.add(model);
                    sortMap.put(model.getPackageName(), list);
                    MLogs.d("sort " + model.getPackageName());
                }
            }
            for (String s : sortedPackages) {
                ArrayList<AppModel> list = sortMap.get(s);
                if (list != null) {
                    for(AppModel model:list) {
                        gridItems.add(new HomeGridItem(HomeGridItem.TYPE_CLONE, model));
                    }
                }
            }
        } else {
            if (appModels != null) {
                for (AppModel model: appModels) {
                    gridItems.add(new HomeGridItem(HomeGridItem.TYPE_CLONE, model));
                }
            }
        }
        if (showLucky) {
            gridItems.add(new HomeGridItem(HomeGridItem.TYPE_LUCKY, null));
        }

        if(pkgGridAdapter != null && mExplosionField == null){
            pkgGridAdapter.notifyDataSetChanged();
        }
    }

    private void insertIconAd() {
        gridItems.add(0, new HomeGridItem(HomeGridItem.TYPE_ICON_AD, iconAd));
        if(pkgGridAdapter != null ){
            pkgGridAdapter.notifyDataSetChanged();
        }
    }

    private void initData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                CloneHelper.getInstance(mActivity).loadClonedApps(mActivity, new CloneHelper.OnClonedAppChangListener() {
                    @Override
                    public void onInstalled(List<AppModel> clonedApp) {
                        mLastClone = clonedApp.get(0);
                        PreferencesUtils.setHasCloned();
                        appInfos = CloneHelper.getInstance(mActivity).getClonedApps();
                        updateGridItemList(appInfos);
                    }

                    @Override
                    public void onUnstalled(List<AppModel> clonedApp) {
                        //TODO never been called
                        MLogs.d("onUninstalled");
                        appInfos = CloneHelper.getInstance(mActivity).getClonedApps();
                        updateGridItemList(appInfos);
                        preloadAppListAd(false);
                    }

                    @Override
                    public void onLoaded(List<AppModel> clonedApp) {
                        appInfos = CloneHelper.getInstance(mActivity).getClonedApps();
                        if (appInfos.size() >= 1 && !PreferencesUtils.hasCloned()) {
                            PreferencesUtils.setHasCloned();
                        }
                        updateGridItemList(appInfos);
                        if (appInfos.size() > iconAdConfig.cloneThreshold  && iconAd == null) {
                            long installTime = CommonUtils.getInstallTime(mActivity, mActivity.getPackageName());
                            if (System.currentTimeMillis() - installTime > iconAdConfig.showAfterInstall) {
                                loadAppIconAd();
                            }
                        }
                        preloadAppListAd(false);

                        if (!PreferencesUtils.hasShownCloneGuide(mActivity) && (clonedApp == null || clonedApp.size() == 0)) {
                            pkgGridView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MLogs.d("show clone app guide");
                                    showCloneAppGuide();
                                }
                            }, 1000);

                        }
                    }
                });
            }
        }).start();
    }

    private void showDeleteDialog(View view, AppModel appModel){
        LeftRightDialog.show(mActivity,mActivity.getResources().getString(R.string.delete_dialog_title),
                mActivity.getResources().getString(R.string.delete_dialog_content),
                mActivity.getResources().getString(R.string.delete_dialog_left),mActivity.getResources().getString(R.string.delete_dialog_right),
                new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case LeftRightDialog.LEFT_BUTTON:
                                dialogInterface.dismiss();
                                break;
                            case LeftRightDialog.RIGHT_BUTTON:
                                dialogInterface.dismiss();
                                deleteAppWithAnim(view, appModel);
                                break;
                        }
                    }
                });
        PreferencesUtils.setShownDeleteDialog();
    }

    private void deleteAppWithAnim(View view, AppModel appModel){
        if (appModel == null) return;
        MLogs.d("deleteApp " + appModel.getPackageName());
        appModel.setUnEnable(true);
//        pkgGridAdapter.notifyDataSetChanged();
//        View view = pkgGridView.getChildAt(pkgGridAdapter.getPosition(appModel) + pkgGridView.getGridItemStartOffset());
        if(view != null) {
            mExplosionField = ExplosionField.attachToWindow(mActivity);
            mExplosionField.explode(view, new ExplosionField.OnExplodeFinishListener() {
                @Override
                public void onExplodeFinish(View v) {
                    ExplosionField.detachFromWindow(mActivity, mExplosionField);
                    mExplosionField = null;
                    pkgGridAdapter.notifyDataSetChanged();
                }
            });
            CloneHelper.getInstance(mActivity).unInstallApp(mActivity, appModel);
        }
//
        EventReporter.deleteClonedApp(mActivity, appModel.getPackageName());

//        pkgGridView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                ExplosionField.detachFromWindow(mActivity, mExplosionField);
//                CloneHelper.getInstance(mActivity).unInstallApp(mActivity, appModel);
//            }
//        }, 1100);
    }

    private void startAppLaunchActivity(String packageName, int userId){
        if(mActivity instanceof  HomeActivity){
            ((HomeActivity)mActivity).startAppLaunchActivity(packageName, userId);
        }
    }

    public void showFromBottom(){
        AnimatorHelper.verticalShowFromBottom(contentView);
        floatView.postDelayed(new Runnable() {
            @Override
            public void run() {
                floatView.startRote();
                if (mLastClone == null) {
                    return;
                }
                for (HomeGridItem item: gridItems) {
                    if (item.type == HomeGridItem.TYPE_CLONE) {
                        AppModel model = (AppModel) item.obj;
                        if (model.getPkgUserId() == mLastClone.getPkgUserId()
                                && model.getPackageName().equals(mLastClone.getPackageName())) {
                            if (item.inflateView != null) {
                                View icon = item.inflateView.findViewById(R.id.app_icon);
                                ObjectAnimator scaleX = ObjectAnimator.ofFloat(icon, "scaleX", 0.7f, 1.2f, 1.0f);
                                ObjectAnimator scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 0.7f, 1.2f, 1.0f);
                                AnimatorSet animSet = new AnimatorSet();
                                animSet.play(scaleX).with(scaleY);
                                animSet.setInterpolator(new BounceInterpolator());
                                animSet.setDuration(800).start();
                            }
                        }
                    }
                }
                mLastClone = null;
            }
        },AnimatorHelper.DURATION_NORMAL);
    }

    public void hideToBottom(){
        AnimatorHelper.hideToBottom(contentView);
    }

    /**
     * 启动已安装的App列表页面
     *
     */
    public void startAppListActivity() {
        if(mActivity instanceof  HomeActivity){
            ((HomeActivity)mActivity).startAppListActivity();
        }else {
            Intent i = new Intent(mActivity, AppListActivity.class);
            mActivity.startActivityForResult(i, AppConstants.REQUEST_SELECT_APP);
            mActivity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (nativeAd != null) {
            nativeAd.destroy();
        }
    }

    private void preloadAppListAd(boolean force) {
        if (!force && appInfos.size() >= RemoteConfig.getLong(CONFIG_HOME_PRELOAD_APPLIST_GATE) ) {
            return;
        }
        if (mApplistAdLoader == null) {
            mApplistAdLoader = FuseAdLoader.get(AppListActivity.SLOT_APPLIST_NATIVE, mActivity.getApplicationContext());
            mApplistAdLoader.setBannerAdSize(AppListActivity.getBannerAdSize());
        }
        mApplistAdLoader.preloadAd(mActivity);
    }
}
