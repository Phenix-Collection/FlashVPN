package mochat.multiple.parallel.whatsclone.component.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.polestar.booster.BoosterSdk;
import com.polestar.ad.AdConfig;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import mochat.multiple.parallel.whatsclone.MApp;
import mochat.multiple.parallel.whatsclone.R;
import mochat.multiple.parallel.whatsclone.component.BaseFragment;
import mochat.multiple.parallel.whatsclone.component.activity.AppListActivity;
import mochat.multiple.parallel.whatsclone.component.activity.AppStartActivity;
import mochat.multiple.parallel.whatsclone.component.activity.CustomizeIconActivity;
import mochat.multiple.parallel.whatsclone.component.activity.HomeActivity;
import mochat.multiple.parallel.whatsclone.component.activity.NativeInterstitialActivity;
import mochat.multiple.parallel.whatsclone.constant.AppConstants;
import mochat.multiple.parallel.whatsclone.db.DbManager;
import mochat.multiple.parallel.whatsclone.model.AppModel;
import mochat.multiple.parallel.whatsclone.model.CustomizeAppData;
import mochat.multiple.parallel.whatsclone.utils.AnimatorHelper;
import mochat.multiple.parallel.whatsclone.utils.AppManager;
import mochat.multiple.parallel.whatsclone.utils.CloneHelper;
import mochat.multiple.parallel.whatsclone.utils.CommonUtils;
import mochat.multiple.parallel.whatsclone.utils.DisplayUtils;
import mochat.multiple.parallel.whatsclone.utils.ExplosionField;
import mochat.multiple.parallel.whatsclone.utils.MLogs;
import mochat.multiple.parallel.whatsclone.utils.EventReporter;
import mochat.multiple.parallel.whatsclone.utils.PreferencesUtils;
import mochat.multiple.parallel.whatsclone.utils.RemoteConfig;
import mochat.multiple.parallel.whatsclone.widgets.LeftRightDialog;
import mochat.multiple.parallel.whatsclone.widgets.GridAppCell;
import mochat.multiple.parallel.whatsclone.widgets.HeaderGridView;
import mochat.multiple.parallel.whatsclone.widgets.TutorialGuides;
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
    private PopupMenu menuPopup;
    private ExplosionField mExplosionField;
    private LinearLayout nativeAdContainer;

    private IAdAdapter nativeAd;

    private FuseAdLoader mNativeAdLoader;
    private FuseAdLoader mApplistAdLoader;
    private static final String CONFIG_HOME_NATIVE_PRIOR_TIME = "home_native_prior_time";
    private static final String CONFIG_HOME_SHOW_LUCKY_RATE = "home_show_lucky_rate";
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
                .mainMediaId(R.id.ad_cover_image)
                .iconImageId(R.id.ad_icon_image)
                .callToActionId(R.id.ad_cta_text)
                .privacyInformationId(R.id.ad_choices_image)
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
        return contentView;
    }
    private class ItemDetailListener implements View.OnClickListener {
        int idx;
        ItemDetailListener(int i) {
            idx = i;
        }

        @Override
        public void onClick(View more) {
            if (menuPopup == null) {
                menuPopup = new PopupMenu(getActivity(), more);
                menuPopup.inflate(R.menu.item_detail_menu);
            }
            //菜单项的监听
            menuPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    AppModel model = (AppModel) pkgGridAdapter.getItem(idx);
                    switch (menuItem.getItemId()) {
                        case R.id.item_shortcut:
                            if (model != null){
                                CommonUtils.createShortCut(mActivity,(model));
                                more.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mActivity, R.string.toast_shortcut_added, Toast.LENGTH_SHORT).show();
                                        //CustomToastUtils.showImageWithMsg(mActivity, mActivity.getResources().getString(R.string.toast_shortcut_added), R.mipmap.icon_add_success);
                                    }
                                },500);
                            }
                            break;
                        case R.id.item_customize:
                            if (model != null) {
                                CustomizeIconActivity.start(getActivity(), model.getPackageName(), model.getPkgUserId());
                            }
                            break;
                        case R.id.item_delete:
                            if (model != null){
                                showDeleteDialog( model);
                            }
                            break;
                    }
                    return true;
                }
            });
            try {
                MenuPopupHelper menuHelper = new MenuPopupHelper(getActivity(), (MenuBuilder) menuPopup.getMenu(), more);
                //menuHelper.setForceShowIcon(true);
                menuHelper.show();
//            Field field = menuPopup.getClass().getDeclaredField("mPopup");
//            field.setAccessible(true);
//            MenuPopupHelper mHelper = (MenuPopupHelper) field.get(menuPopup);
//            mHelper.setForceShowIcon(true);
            } catch (Exception e) {
                MLogs.logBug(MLogs.getStackTraceString(e));
            }

        }
    }
    private class PackageGridAdapter extends BaseAdapter {

        public int getPosition(AppModel appModel) {
            int ret = 0;
            if (appModel != null ) {
                for (AppModel m : appInfos) {
                    if (m.getPackageName().equals(appModel.getPackageName())
                            && m.getPkgUserId() == appModel.getPkgUserId()) {
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
            size ++ ;//add
            if (showBooster ) {
                size ++;
                //for booster icon
            }
            if (showLucky) {
                size ++;
            }
            if ( size < 12 ) {
                if (adShowed) {
                    size = 12;
                } else {
                    size = 15;
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
            ImageView detail = (ImageView) view.findViewById(R.id.item_detail);
            ImageView newDot = (ImageView) view.findViewById(R.id.new_dot);
            detail.setOnClickListener(new ItemDetailListener(i));

            AppModel appModel = (AppModel) getItem(i);
            if (appModel != null) {
                CustomizeAppData data = CustomizeAppData.loadFromPref(appModel.getPackageName(),
                        appModel.getPkgUserId());
                Bitmap bmp = data.getCustomIcon();
                appIcon.setImageBitmap(bmp);
                appModel.setCustomIcon(bmp);
                appName.setText(data.customized? data.label: appModel.getName());
                if (CustomizeAppData.hasLaunched(appModel.getPackageName(), appModel.getPkgUserId())) {
                    newDot.setVisibility(View.INVISIBLE);
                }
            } else {
                detail.setVisibility(View.GONE);
                newDot.setVisibility(View.INVISIBLE);
                int luckIdx = showBooster? appInfos.size() + 1: appInfos.size();
                int boosterIdx = appInfos.size();
                int addIdx = showLucky? luckIdx + 1: luckIdx;
                if (showLucky && i == luckIdx) {
                    appIcon.setImageResource(R.drawable.icon_feel_lucky);
                    appName.setText(R.string.feel_lucky);
                    appName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    appName.setTextColor(getResources().getColor(R.color.lucky_red));
                    newDot.setBackgroundResource(R.color.lucky_red);
                } else if(showBooster && i == boosterIdx) {
                    appIcon.setImageResource(R.drawable.boost_icon);
                    appName.setText(R.string.booster_title);
                } else if (i == addIdx) {
                    appIcon.setImageResource(R.mipmap.icon_add_more);
                    appName.setText(R.string.add_app);
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

        pkgGridView = (HeaderGridView) contentView.findViewById(R.id.grid_app);
        pkgGridView.addHeaderView(nativeAdContainer);
        pkgGridAdapter = new PackageGridAdapter();
//        pkgGridView.setLayoutAnimation(getGridLayoutAnimController());
        pkgGridView.setAdapter(pkgGridAdapter);

        pkgGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                //final int i = pkgGridAdapter.getNatureIndex(position);
                int i =pos - pkgGridView.getGridItemStartOffset();
                MLogs.d("onItemClick " + i);
                if(i >= 0 && i < appInfos.size()){
                    AppModel model = appInfos.get(i);
                    startAppLaunchActivity(model.getPackageName(), model.getPkgUserId());
                }else{
                    int luckIdx = showBooster? appInfos.size() + 1: appInfos.size();
                    int boosterIdx = appInfos.size();
                    int addIdx = showLucky? luckIdx + 1: luckIdx;
                    if(showLucky && i == luckIdx){
                        MLogs.d("Show lucky");
                        Intent intent = new Intent(mActivity, NativeInterstitialActivity.class);
                        startActivity(intent);
                        EventReporter.homeGiftClick(mActivity, "lucky_item");
                    } else if (showBooster && i==boosterIdx) {
                        MLogs.d("Start booster");
                        BoosterSdk.startClean(mActivity, "home_item");
                    } else if (i == addIdx) {
                        startAppListActivity();
                        PreferencesUtils.setCloneGuideShowed(mActivity);
                        EventReporter.homeAdd(mActivity);
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
                    ImageView more = (ImageView)view.findViewById(R.id.item_detail);
                    if (more != null) {
                        more.performClick();
                    }
                    return true;
                } else {
                    int luckIdx = showBooster? appInfos.size() + 1: appInfos.size();
                    int boosterIdx = appInfos.size();
                    int addIdx = showLucky? luckIdx + 1: luckIdx;
                    if(showLucky && i == luckIdx){
                        MLogs.d("Show lucky");
                        Intent intent = new Intent(mActivity, NativeInterstitialActivity.class);
                        startActivity(intent);
                        EventReporter.homeGiftClick(mActivity, "lucky_item");
                    } else if (showBooster && i==boosterIdx) {
                        MLogs.d("Start booster");
                        BoosterSdk.startClean(mActivity, "home_item");
                    } else if (i == addIdx) {
                        startAppListActivity();
                        PreferencesUtils.setCloneGuideShowed(mActivity);
                        EventReporter.homeAdd(mActivity);
                    }
                    return true;
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
        //GreyAttribute.checkAndClick(getActivity(),"com.kamagames.pokerist");
        if (menuPopup != null) {
            menuPopup.dismiss();
        }
        if (PreferencesUtils.isAdFree()) {
            hideAd();
        }
        if (pkgGridAdapter != null) {
            pkgGridAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (menuPopup != null) {
            menuPopup.dismiss();
        }
    }

    private static final String KEY_HOME_SHOW_HEADER_AD = "home_show_header_ad";
    public static final String SLOT_HOME_HEADER_NATIVE = "slot_home_header_native";
    private List<AdConfig> headerNativeAdConfigs ;

    private void initData(){
        showBooster = RemoteConfig.getBoolean(CONFIG_SHOW_BOOSTER) && PreferencesUtils.hasCloned();
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
                        showLucky = (!PreferencesUtils.isAdFree())
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
        MLogs.d("Position: "+ pkgGridAdapter.getPosition(appModel) + " offset: "+ pkgGridView.getGridItemStartOffset());
        View view = pkgGridView.getChildAt(pkgGridAdapter.getPosition(appModel) + pkgGridView.getGridItemStartOffset());
        if(view != null) {
            mExplosionField.explode(view, new ExplosionField.OnExplodeFinishListener() {
                @Override
                public void onExplodeFinish(View v) {
                }
            });
        }
        pkgGridView.postDelayed(new Runnable() {
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
        AppManager.uninstallApp(appModel.getPackageName(), appModel.getPkgUserId());
        CommonUtils.removeShortCut(mActivity,appModel);
        CustomizeAppData.removePerf(appModel.getPackageName(), appModel.getPkgUserId());
        PreferencesUtils.resetStarted(appModel.getName());
        DbManager.deleteAppModel(mActivity, appModel);
        DbManager.notifyChanged();
        pkgGridAdapter.notifyDataSetChanged();
        //adapter.deleteComplete();
    }

    private void startAppLaunchActivity(String packageName, int userId){
        if(mActivity instanceof  HomeActivity){
            ((HomeActivity)mActivity).startAppLaunchActivity(packageName, userId);
        }
    }

    public void showFromBottom(){
        AnimatorHelper.verticalShowFromBottom(contentView);
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
