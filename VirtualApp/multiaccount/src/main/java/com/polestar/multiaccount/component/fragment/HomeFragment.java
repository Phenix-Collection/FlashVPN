package com.polestar.multiaccount.component.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdLog;
import com.polestar.ad.AdUtils;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseFragment;
import com.polestar.multiaccount.component.activity.AppListActivity;
import com.polestar.multiaccount.component.activity.HomeActivity;
import com.polestar.multiaccount.component.activity.NativeInterstitialActivity;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.BitmapUtils;
import com.polestar.multiaccount.utils.AnimatorHelper;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.DisplayUtils;
import com.polestar.multiaccount.utils.ExplosionField;
import com.polestar.multiaccount.utils.MLogs;
import com.polestar.multiaccount.utils.MTAManager;
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
    private NativeExpressAdView mAdmobExpressView;
    private View mLockSettingIcon;

    private static boolean burstLoad = true;
    private static long nativePriorTime = 2*1000;
    private static final String CONFIG_HOME_BURST_LOAD = "home_burst_load";
    private static final String CONFIG_HOME_NATIVE_PRIOR_TIME = "home_native_prior_time";
    private static final String CONFIG_HOME_SHOW_LUCKY_RATE = "home_show_lucky_rate";
    private static final String CONFIG_HOME_SHOW_LUCKY_GATE= "home_show_lucky_gate";
    private long adLoadStartTime = 0;
    private static final int NATIVE_AD_READY = 0;
    private static final int BANNER_AD_READY = 1;
    private boolean showLucky;
    public static String UNIT_ID = "8439";

    private boolean adShowed = false;

    private Handler adHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if (adShowed){
                return;
            }
            adShowed = true;
            switch (msg.what) {
                case NATIVE_AD_READY:
                    IAdAdapter ad = (IAdAdapter) msg.obj;
                    inflateNativeAd(ad);
                    break;
                case BANNER_AD_READY:
                    showBannerAd();
                    break;
            }
        }
    };

    public void inflateNativeAd(IAdAdapter ad) {
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
        burstLoad = RemoteConfig.getBoolean(CONFIG_HOME_BURST_LOAD);
        nativePriorTime = RemoteConfig.getLong(CONFIG_HOME_NATIVE_PRIOR_TIME);
        initView();
        initData();
        boolean showHeaderAd = RemoteConfig.getBoolean(KEY_HOME_SHOW_HEADER_AD);
        MLogs.d(KEY_HOME_SHOW_HEADER_AD + showHeaderAd);
        headerNativeAdConfigs = RemoteConfig.getAdConfigList(SLOT_HOME_HEADER_NATIVE);
        if (showHeaderAd && headerNativeAdConfigs.size() > 0
                && (!PreferencesUtils.isAdFree())) {
            initAdmobBannerView();
            loadHeadNativeAd();
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
                    MTAManager.addShortCut(mActivity, ((AppModel) info).getPackageName());
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
                if (appModel.getCustomIcon() == null) {
                    appModel.setCustomIcon(BitmapUtils.createCustomIcon(mActivity, appModel.initDrawable(mActivity)));
                }

                if (appModel.getCustomIcon() != null) {
                    appIcon.setImageBitmap(appModel.getCustomIcon());
                }
                appName.setText(appModel.getName());
            } else {
                if (showLucky && i == appInfos.size()) {
                    appIcon.setImageResource(R.drawable.icon_feel_lucky);
                    appName.setText(R.string.feel_lucky);
                    appName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    appName.setTextColor(getResources().getColor(R.color.lucky_red));
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
                MTAManager.homeAdd(mActivity);
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
                    if(showLucky && i == appInfos.size()){
                        MLogs.d("Show lucky");
                        Intent intent = new Intent(mActivity, NativeInterstitialActivity.class);
                        startActivity(intent);
                        MTAManager.homeGiftClick(mActivity, "lucky");
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
                    if(showLucky && i == appInfos.size()){
                        MLogs.d("Show lucky");
                        Intent intent = new Intent(mActivity, NativeInterstitialActivity.class);
                        startActivity(intent);
                        MTAManager.homeGiftClick(mActivity, "lucky");
                    }
                    return  false;
                }

            }
        });
    }

    private void initAdmobBannerView() {
        mAdmobExpressView = new NativeExpressAdView(mActivity);
        String adunit  = null;
        if (headerNativeAdConfigs != null) {
            for (AdConfig adConfig: headerNativeAdConfigs) {
                if (adConfig.source != null && adConfig.source.equals(AdConstants.NativeAdType.AD_SOURCE_ADMOB_NAVTIVE_BANNER)){
                    adunit = adConfig.key;
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(adunit)) {
            mAdmobExpressView = null;
            return;
        }
        int dpWidth = DisplayUtils.px2dip(mActivity, DisplayUtils.getScreenWidth(mActivity));
        dpWidth = dpWidth < 290 ? dpWidth : dpWidth-10;
        mAdmobExpressView.setAdSize(new AdSize(dpWidth, 135));
//        mAdmobExpressView.setAdUnitId("ca-app-pub-5490912237269284/2431070657");
        mAdmobExpressView.setAdUnitId(adunit);
        mAdmobExpressView.setVisibility(View.GONE);
        mAdmobExpressView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                AdLog.d("onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                AdLog.d("onAdFailedToLoad " + i);
                mAdmobExpressView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                long delay = nativePriorTime - (System.currentTimeMillis() - adLoadStartTime);
                adHandler.sendMessageDelayed(adHandler.obtainMessage(BANNER_AD_READY),delay );
                AdLog.d("on Banner AdLoaded ");
            }
        });
    }

    private void showBannerAd(){
        nativeAdContainer.removeAllViews();
        mAdmobExpressView.setVisibility(View.VISIBLE);
        nativeAdContainer.addView(mAdmobExpressView);
        pkgGridAdapter.notifyDataSetChanged();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mAdmobExpressView, "scaleX", 0.7f, 1.0f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mAdmobExpressView, "scaleY", 0.7f, 1.0f, 1.0f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY);
        animSet.setInterpolator(new BounceInterpolator());
        animSet.setDuration(800).start();
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

            }
        });
        dismissLongClickGuide();
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

    private void loadAdmobNativeExpress(){
        if (mAdmobExpressView == null) {
            return;
        }
        MLogs.d("Home loadAdmobNativeExpress");
        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID(mActivity);
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            boolean isTestDevice = request.isTestDevice(mActivity);
            AdLog.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
            AdLog.d( "Admob unit id "+ mAdmobExpressView.getAdUnitId());
            mAdmobExpressView.loadAd(request );
        } else {
            mAdmobExpressView.loadAd(new AdRequest.Builder().build());
        }
    }

    private void loadHeadNativeAd() {
        if (mNativeAdLoader == null) {
            mNativeAdLoader = FuseAdLoader.get(SLOT_HOME_HEADER_NATIVE, getActivity());
        }
//        mNativeAdLoader.addAdConfig(new AdConfig(AdConstants.NativeAdType.AD_SOURCE_FACEBOOK, "1713507248906238_1787756514814644", -1));
//        mNativeAdLoader.addAdConfig(new AdConfig(AdConstants.NativeAdType.AD_SOURCE_MOPUB, "ea31e844abf44e3690e934daad125451", -1));

        if (burstLoad) {
            adLoadStartTime = System.currentTimeMillis();
            loadAdmobNativeExpress();
        }
        if ( mNativeAdLoader.hasValidAdSource()) {
            mNativeAdLoader.loadAd(1, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    adHandler.sendMessage(adHandler.obtainMessage(NATIVE_AD_READY, ad ));
                    nativeAd = ad;
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {
                    adLoadStartTime = 0;
                    if (!burstLoad) {
                        loadAdmobNativeExpress();
                    }
                }
            });
        } else {
            adLoadStartTime = 0;
            if (!burstLoad) {
                loadAdmobNativeExpress();
            }
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                CloneHelper.getInstance(mActivity).loadClonedApps(mActivity, new CloneHelper.OnClonedAppChangListener() {
                    @Override
                    public void onInstalled(List<AppModel> clonedApp) {
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
                        if (appInfos.size() == 0) {
                            FuseAdLoader.get(AppListActivity.SLOT_APPLIST_NATIVE, mActivity).loadAd(1, null);
                        }
                    }

                    @Override
                    public void onLoaded(List<AppModel> clonedApp) {
                        appInfos = clonedApp;
                        if (appInfos.size() == 0) {
                            FuseAdLoader.get(AppListActivity.SLOT_APPLIST_NATIVE, mActivity).loadAd(1, null);
                        }
                        MLogs.d("onLoaded applist");
                        long luckyRate = RemoteConfig.getLong(CONFIG_HOME_SHOW_LUCKY_RATE);
                        long gate = RemoteConfig.getLong(CONFIG_HOME_SHOW_LUCKY_GATE);
                        showLucky = (!PreferencesUtils.isAdFree()) && appInfos.size() >= gate && new Random().nextInt(100) < luckyRate ;
                        if (!showLucky) {
                            MLogs.d("Not show lucky. Rate: " + luckyRate);
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
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            deleteApp(appModel);
                        }
                    }, 1000);
                }
            });
        } else {
            deleteApp(appModel);
        }
    }

    private void deleteApp(AppModel appModel){
        appInfos.remove(appModel);
        MTAManager.deleteClonedApp(mActivity, appModel.getPackageName());
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
}
