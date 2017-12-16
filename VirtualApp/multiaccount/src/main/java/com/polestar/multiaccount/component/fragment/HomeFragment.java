package com.polestar.multiaccount.component.fragment;

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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.multiaccount.MApp;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseFragment;
import com.polestar.multiaccount.component.activity.AppListActivity;
import com.polestar.multiaccount.component.activity.AppStartActivity;
import com.polestar.multiaccount.component.activity.HomeActivity;
import com.polestar.multiaccount.component.activity.NativeInterstitialActivity;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.model.CustomizeAppData;
import com.polestar.multiaccount.utils.AnimatorHelper;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.ExplosionField;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.EventReporter;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.utils.RemoteConfig;
import com.polestar.multiaccount.widgets.LeftRightDialog;
import com.polestar.multiaccount.widgets.CustomFloatView;
import com.polestar.multiaccount.widgets.GridAppCell;
import com.polestar.multiaccount.widgets.HeaderGridView;
import com.polestar.multiaccount.widgets.TutorialGuides;
import com.polestar.multiaccount.widgets.dragdrop.DragController;
import com.polestar.multiaccount.widgets.dragdrop.DragImageView;
import com.polestar.multiaccount.widgets.dragdrop.DragLayer;
import com.polestar.multiaccount.widgets.dragdrop.DragSource;

import java.util.ArrayList;
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
    private LinearLayout nativeAdContainer;

    private IAdAdapter nativeAd;

    private FuseAdLoader mNativeAdLoader;
    private FuseAdLoader mApplistAdLoader;
    private View mLockSettingIcon;
    private static final String CONFIG_HOME_NATIVE_PRIOR_TIME = "home_native_prior_time";
    private static final String CONFIG_HOME_SHOW_LUCKY_RATE = "home_show_lucky_rate";
    private static final String CONFIG_HOME_SHOW_LUCKY_GATE= "home_show_lucky_gate";
    private static final String CONFIG_HOME_PRELOAD_APPLIST_GATE= "home_preload_applist_gate";
    private final static String CONFIG_NEED_PRELOAD_LOADING = "conf_need_preload_start_ad";
    private final static String CONFIG_SHOW_BOOSTER = "conf_show_booster_in_home";

    private boolean showLucky;
    private boolean showBooster;

    private boolean adShowed = false;

    public void inflateNativeAd(IAdAdapter ad) {
        if (adShowed) {
            return;
        }
        adShowed = true;
        final AdViewBinder viewBinder =  new AdViewBinder.Builder(R.layout.front_page_native_ad)
                .titleId(R.id.ad_title)
                .textId(R.id.ad_subtitle_text)
                .mainImageId(R.id.ad_cover_image)
                .iconImageId(R.id.ad_icon_image)
                .callToActionId(R.id.ad_cta_text)
                .privacyInformationIconImageId(R.id.ad_choices_image)
                .build();
        View adView = ad.getAdView(viewBinder);
        nativeAdContainer.removeAllViews();
        nativeAdContainer.addView(adView);
        pkgGridAdapter.notifyDataSetChanged();
        dismissLongClickGuide();
    }

    public void hideAd() {
        nativeAdContainer.removeAllViews();
        showLucky = false;
        if (pkgGridAdapter != null) {
            pkgGridAdapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_home, null);
        mLockSettingIcon = mActivity.findViewById(R.id.lock_setting_icon);
        mExplosionField = ExplosionField.attachToWindow(mActivity);
        initView();
        initData();
        boolean showHeaderAd = RemoteConfig.getBoolean(KEY_HOME_SHOW_HEADER_AD)
                && PreferencesUtils.hasCloned();
        MLogs.d(KEY_HOME_SHOW_HEADER_AD + showHeaderAd);
        headerNativeAdConfigs = RemoteConfig.getAdConfigList(SLOT_HOME_HEADER_NATIVE);
        if (showHeaderAd && headerNativeAdConfigs.size() > 0
                && (!PreferencesUtils.isAdFree())) {
            loadHeadNativeAd();
        }
        if (!PreferencesUtils.isAdFree() && RemoteConfig.getBoolean(CONFIG_NEED_PRELOAD_LOADING)) {
            AppStartActivity.preloadAd(mActivity);
        }
        mDragController = new DragController(mActivity);
        mDragController.setDragListener(mDragListener);
        mDragController.setWindowToken(contentView.getWindowToken());
        mDragLayer.setDragController(mDragController);
        return contentView;
    }

    DragController.DragListener mDragListener = new DragController.DragListener() {
        @Override
        public void onDragStart(DragSource source, Object info, int dragAction) {
            MLogs.d("onDragStart");
            floatView.animToExpand();
            mDragController.addDropTarget(floatView);
        }

        @Override
        public void onDragEnd(DragSource source, Object info, int action) {
            MLogs.d("onDragEnd + " + floatView.getSelectedState());
            switch (floatView.getSelectedState()) {
                case CustomFloatView.SELECT_BTN_LEFT:
                    EventReporter.addShortCut(mActivity, ((AppModel) info).getPackageName());
                    CommonUtils.createShortCut(mActivity,((AppModel) info));
                    floatView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mActivity, R.string.toast_shortcut_added, Toast.LENGTH_SHORT).show();
                            //CustomToastUtils.showImageWithMsg(mActivity, mActivity.getResources().getString(R.string.toast_shortcut_added), R.mipmap.icon_add_success);
                        }
                    },CustomFloatView.ANIM_DURATION / 2);
                    break;
                case CustomFloatView.SELECT_BTN_RIGHT:
                    pkgGridAdapter.notifyDataSetChanged();
                    floatView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showDeleteDialog((AppModel) info);
                        }
                    },CustomFloatView.ANIM_DURATION / 2);
                    break;
                default:
                    break;
            }
            floatView.animToIdel();
            mDragController.removeDropTarget(floatView);
        }
    };

    private class PackageGridAdapter extends BaseAdapter {

        public int getPosition(AppModel appModel) {
            int ret = 0;
            if (appModel != null ) {
                for (AppModel m : appInfos) {
                    if (m.getPackageName().equals(appModel.getPackageName())) {
                        return ret;
                    }
                    ret++;
                }
            }
            return  -1;
        }
        @Override
        public int getCount() {
            int size = appInfos == null ? 0 : appInfos.size();
            if (showBooster && size != 0) {
                size ++;
                //for booster icon
            }
            if (showLucky) {
                size ++;
            }
            if ( size < 15 ) {
                if (adShowed) {
                    size = 15;
                } else {
                    size = 18;
                }

            } else {
                size = size + 3 - (size % 3);
            }
            return size;
        }

        @Override
        public Object getItem(int position) {
            if ( appInfos == null) {
                return  null;
            }
            if (position < appInfos.size() && position >= 0) {
                return  appInfos.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = new GridAppCell(mActivity);

            ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);
            TextView appName = (TextView) view.findViewById(R.id.app_name);

            AppModel appModel = (AppModel) getItem(i);
            if (appModel != null) {
                CustomizeAppData data = CustomizeAppData.loadFromPref(appModel.getPackageName());
                Bitmap bmp = data.getCustomIcon();
                appIcon.setImageBitmap(bmp);
                appModel.setCustomIcon(bmp);
                appName.setText(data.customized? data.label: appModel.getName());
            } else {
                int luckIdx = showBooster? appInfos.size() + 1: appInfos.size();
                int boosterIdx = appInfos.size();
                if (showLucky && i == luckIdx) {
                    appIcon.setImageResource(R.drawable.icon_feel_lucky);
                    appName.setText(R.string.feel_lucky);
                    appName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    appName.setTextColor(getResources().getColor(R.color.lucky_red));
                } else if(showBooster && i == boosterIdx) {
                    appIcon.setImageResource(R.drawable.boost_icon);
                    appName.setText(R.string.booster_title);
                }
            }

            return view;
        }
    }

    private void initView() {
        //nativeAdContainer = (LinearLayout) mActivity.findViewById(R.id.native_ad_container);
        nativeAdContainer = new LinearLayout(mActivity);
        nativeAdContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        nativeAdContainer.setOrientation(LinearLayout.VERTICAL);

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
                if(i >= 0 && i < appInfos.size()){
                    if(floatView.isIdle()){
                        startAppLaunchActivity(appInfos.get(i).getPackageName());
                    }else{
                        floatView.restore();
                        floatView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startAppLaunchActivity(appInfos.get(i).getPackageName());
                            }
                        },100);
                    }
                }else{
                    int luckIdx = showBooster? appInfos.size() + 1: appInfos.size();
                    int boosterIdx = appInfos.size();
                    if(showLucky && i == luckIdx){
                        MLogs.d("Show lucky");
                        Intent intent = new Intent(mActivity, NativeInterstitialActivity.class);
                        startActivity(intent);
                        EventReporter.homeGiftClick(mActivity, "lucky_item");
                    } else if (showBooster && i==boosterIdx) {
                        MLogs.d("Start booster");
                        EventReporter.boostFrom(mActivity, "item_click");
                    }
                }
            }
        });
        pkgGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
                int i = pos - pkgGridView.getGridItemStartOffset();
                MLogs.d("onItemLongClick " + i);
                if (pkgGridAdapter.getItem(i) != null) {
                    DragImageView iv = (DragImageView) view.findViewById(R.id.app_icon);
                    mDragController.startDrag(iv, iv, pkgGridAdapter.getItem(i), DragController.DRAG_ACTION_COPY);
                    return true;
                } else {
                    int luckIdx = showBooster? appInfos.size() + 1: appInfos.size();
                    int boosterIdx = appInfos.size();
                    if(showLucky && i == luckIdx){
                        MLogs.d("Show lucky");
                        Intent intent = new Intent(mActivity, NativeInterstitialActivity.class);
                        startActivity(intent);
                        EventReporter.homeGiftClick(mActivity, "lucky_item_long");
                    } else if (showBooster && i==boosterIdx) {
                        MLogs.d("Start booster");
                        EventReporter.boostFrom(mActivity, "item_long_click");
                    }
                    return  false;
                }

            }
        });
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


    private void loadHeadNativeAd() {
        if (mNativeAdLoader == null) {
            mNativeAdLoader = FuseAdLoader.get(SLOT_HOME_HEADER_NATIVE, getActivity().getApplicationContext());
            mNativeAdLoader.setBannerAdSize(getBannerSize());
        }

        if ( mNativeAdLoader.hasValidAdSource()) {
            mNativeAdLoader.loadAd(2, RemoteConfig.getLong(CONFIG_HOME_NATIVE_PRIOR_TIME), new IAdLoadListener() {
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
        }
        if (pkgGridAdapter != null) {
            pkgGridAdapter.notifyDataSetChanged();
        }
    }

    private static final String KEY_HOME_SHOW_HEADER_AD = "home_show_header_ad";
    public static final String SLOT_HOME_HEADER_NATIVE = "slot_home_header_native";
    private List<AdConfig> headerNativeAdConfigs ;

    private void initData(){
        showBooster = RemoteConfig.getBoolean(CONFIG_SHOW_BOOSTER);
        new Thread(new Runnable() {
            @Override
            public void run() {
                CloneHelper.getInstance(mActivity).loadClonedApps(mActivity, new CloneHelper.OnClonedAppChangListener() {
                    @Override
                    public void onInstalled(List<AppModel> clonedApp) {
                        if (!PreferencesUtils.hasCloned()) {
                            PreferencesUtils.setHasCloned();
                        }
                        appInfos = clonedApp;
                        if(pkgGridAdapter != null){
                            pkgGridAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onUnstalled(List<AppModel> clonedApp) {
                        MLogs.d("onUninstalled");
                        appInfos = clonedApp;
                        if(pkgGridAdapter != null){
                            pkgGridAdapter.notifyDataSetChanged();
                        }
                        preloadAppListAd(false);
                    }

                    @Override
                    public void onLoaded(List<AppModel> clonedApp) {
                        appInfos = clonedApp;
                        preloadAppListAd(false);
                        MLogs.d("onLoaded applist");
                        long luckyRate = RemoteConfig.getLong(CONFIG_HOME_SHOW_LUCKY_RATE);
                        long gate = RemoteConfig.getLong(CONFIG_HOME_SHOW_LUCKY_GATE);
                        showLucky = (!PreferencesUtils.isAdFree()) && appInfos.size() >= gate
                                && PreferencesUtils.hasCloned()
                                && new Random().nextInt(100) < luckyRate ;
                        if (!showLucky) {
                            MLogs.d("Not show lucky. Rate: " + luckyRate);
                        }
                        if (appInfos.size() >= 1 && !PreferencesUtils.hasCloned()) {
                            PreferencesUtils.setHasCloned();
                        }
                        if(pkgGridAdapter != null){
                            pkgGridAdapter.notifyDataSetChanged();
                        }
                        if (!PreferencesUtils.hasShownCloneGuide(mActivity) && (clonedApp == null || clonedApp.size() == 0)) {
                            pkgGridView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    showCloneAppGuide();
                                }
                            }, 1000);

                        }
                    }
                });
            }
        }).start();
    }

    private void showDeleteDialog(AppModel appModel){
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
                                deleteAppWithAnim(appModel);
                                break;
                        }
                    }
                });
    }

    private void deleteAppWithAnim(AppModel appModel){
        if (appModel == null) return;
        appModel.setUnEnable(true);
        pkgGridAdapter.notifyDataSetChanged();
        View view = pkgGridView.getChildAt(pkgGridAdapter.getPosition(appModel) + pkgGridView.getGridItemStartOffset());
        if(view != null) {
            mExplosionField.explode(view, new ExplosionField.OnExplodeFinishListener() {
                @Override
                public void onExplodeFinish(View v) {
                }
            });
        }
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                deleteApp(appModel);
            }
        }, 1000);

    }

    private void deleteApp(AppModel appModel){
        MLogs.d("deleteApp " + appModel.getPackageName());
        appInfos.remove(appModel);
        EventReporter.deleteClonedApp(mActivity, appModel.getPackageName());
//        updateModelIndex(itemPosition,appInfos.size() - 1);
        AppManager.uninstallApp(appModel.getPackageName());
        CommonUtils.removeShortCut(mActivity,appModel);
        DbManager.deleteAppModel(mActivity, appModel);
        DbManager.notifyChanged();
        pkgGridAdapter.notifyDataSetChanged();
        //adapter.deleteComplete();
    }

    private void startAppLaunchActivity(String packageName){
        if(mActivity instanceof  HomeActivity){
            ((HomeActivity)mActivity).startAppLaunchActivity(packageName);
        }
    }

    public void showFromBottom(){
        AnimatorHelper.verticalShowFromBottom(contentView);
        floatView.postDelayed(new Runnable() {
            @Override
            public void run() {
                floatView.startRote();
            }
        },AnimatorHelper.DURATION_NORMAL);
    }

    public void hideToBottom(){
        AnimatorHelper.hideToBottom(contentView);
    }

    private synchronized void update(ArrayList<AppModel> updateList){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DbManager.updateAppModelList(mActivity,updateList);
            }
        }).start();
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
        mApplistAdLoader.preloadAd();
    }
}
