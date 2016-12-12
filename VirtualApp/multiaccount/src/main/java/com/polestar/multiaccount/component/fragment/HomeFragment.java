package com.polestar.multiaccount.component.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdUtils;
import com.polestar.ad.L;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAd;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.imageloader.widget.BasicLazyLoadImageView;
import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseFragment;
import com.polestar.multiaccount.component.activity.AppListActivity;
import com.polestar.multiaccount.component.activity.HomeActivity;
import com.polestar.multiaccount.constant.Constants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.BitmapUtils;
import com.polestar.multiaccount.utils.EventReportManager;
import com.polestar.multiaccount.utils.AnimatorHelper;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.utils.CloneHelper;
import com.polestar.multiaccount.utils.CommonUtils;
import com.polestar.multiaccount.utils.CustomDialogUtils;
import com.polestar.multiaccount.utils.CustomToastUtils;
import com.polestar.multiaccount.utils.ExplosionField;
import com.polestar.multiaccount.utils.LocalAdUtils;
import com.polestar.multiaccount.utils.Logs;
import com.polestar.multiaccount.utils.MTAManager;
import com.polestar.multiaccount.widgets.CustomFloatView;
import com.polestar.multiaccount.widgets.GifView;
import com.polestar.multiaccount.widgets.GridAppCell;
import com.polestar.multiaccount.widgets.dragdrop.DragController;
import com.polestar.multiaccount.widgets.dragdrop.DragImageView;
import com.polestar.multiaccount.widgets.dragdrop.DragLayer;
import com.polestar.multiaccount.widgets.dragdrop.DragSource;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by yxx on 2016/7/19.
 */
public class HomeFragment extends BaseFragment {
    private View contentView;
    private GridView pkgGridView;
    private PackageGridAdapter pkgGridAdapter;
    private List<AppModel> appInfos;
    private ImageView addBtn, removeBtn;
    private CustomFloatView floatView;
    private LinearLayout guideLayout;
    private GifView iconGifView;
    private ExplosionField mExplosionField;
    private DragController mDragController;
    private DragLayer mDragLayer;
    private LinearLayout nativeAdContainer;


    private FuseAdLoader mNativeAdLoader;
    private NativeExpressAdView mAdmobExpressView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        long time = System.currentTimeMillis();
        Logs.e("onCreateView time1 = " + time);
        contentView = inflater.inflate(R.layout.fragment_home, null);
        mExplosionField = ExplosionField.attachToWindow(mActivity);
        initView();
        initData();
        mDragController = new DragController(getActivity());
        mDragController.setDragListener(mDragListener);
        mDragController.setWindowToken(contentView.getWindowToken());
        mDragLayer.setDragController(mDragController);

        Logs.e("onCreateView time = " + (System.currentTimeMillis() - time));
        return contentView;
    }
    DragController.DragListener mDragListener = new DragController.DragListener() {
        @Override
        public void onDragStart(DragSource source, Object info, int dragAction) {
            Logs.d("onDragStart");
            floatView.animToExtands();
            mDragController.addDropTarget(floatView);
        }

        @Override
        public void onDragEnd(DragSource source, Object info, int action) {
            Logs.d("onDragEnd + " + floatView.getSelectedState());
            switch (floatView.getSelectedState()) {
                case CustomFloatView.SELECT_BTN_LEFT:
                    MTAManager.addShortCut(mActivity, ((AppModel) info).getPackageName());
                    EventReportManager.addShortCut(mActivity, ((AppModel) info).getPackageName());
                    CommonUtils.createShortCut(mActivity,((AppModel) info));
                    floatView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), R.string.toast_shortcut_added, Toast.LENGTH_SHORT).show();
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
            for(AppModel m: appInfos ) {
                if (m.getPackageName().equals(appModel.getPackageName())) {
                    return ret;
                }
                ret ++;
            }
            return  -1;
        }
        @Override
        public int getCount() {
            int size = appInfos == null ? 0 : appInfos.size();
            if ( size < 15 ) {
                size = 15;
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
            if (position < appInfos.size()) {
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
                    appModel.setCustomIcon(BitmapUtils.createCustomIcon(getActivity(), appModel.initDrawable(getActivity())));
                }

                if (appModel.getCustomIcon() != null) {
                    appIcon.setImageBitmap(appModel.getCustomIcon());
                }
                appName.setText(appModel.getName());
            }

            return view;
        }
    }
    private void initView() {
        nativeAdContainer = (LinearLayout) getActivity().findViewById(R.id.native_ad_container);
        mAdmobExpressView = (NativeExpressAdView) getActivity().findViewById(R.id.ab_native_express);
        mAdmobExpressView.setVisibility(View.GONE);

        mDragLayer = (DragLayer)contentView.findViewById(R.id.drag_layer);
        pkgGridView = (GridView) contentView.findViewById(R.id.grid_app);
        pkgGridAdapter = new PackageGridAdapter();
        pkgGridView.setAdapter(pkgGridAdapter);

        guideLayout = (LinearLayout) contentView.findViewById(R.id.guide_layout);

        floatView = (CustomFloatView) contentView.findViewById(R.id.addApp_btn);
        floatView.startBreath();

        floatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAppListActivity();
                MTAManager.homeAdd(getActivity());
                EventReportManager.homeAdd(getActivity());
            }
        });
//        floatView.post(new Runnable() {
//            @Override
//            public void run() {
//                appGridView.setLayoutPercent(1f - ((float) floatView.getHeight()) / ((float) appGridView.getHeight()));
//            }
//        });

        pkgGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //final int i = pkgGridAdapter.getNatureIndex(position);
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
                }
            }
        });
        pkgGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                DragImageView iv = (DragImageView)view.findViewById(R.id.app_icon);
                mDragController.startDrag(iv, iv, pkgGridAdapter.getItem(i),DragController.DRAG_ACTION_COPY);
                return true;
            }
        });

//        appGridView.setOnDragListener(new CustomDragableView.OnDragListener() {
//            @Override
//            public void onDragStart(View view) {
//                AppIconBgView bgView = (AppIconBgView) view.findViewById(R.id.app_icon_anim_bg);
//                if(bgView != null){
//                    bgView.startAnim();
//                }
//                floatView.animToExtands();
//            }
//
//            @Override
//            public void onDragEnd(View view) {
//                AppIconBgView bgView = (AppIconBgView) view.findViewById(R.id.app_icon_anim_bg);
//                if(bgView != null){
//                    bgView.reset();
//                }
//                floatView.animToIdel();
//            }
//
//            @Override
//            public boolean onDragOutSide(int dragLocation) {
//                switch (dragLocation) {
//                    case CustomDragableView.DRAG_OUTSIDE_BOTTOM_LEFT:
//                        floatView.selecteLeftBtn();
//                        break;
//                    case CustomDragableView.DRAG_OUTSIDE_BOTTOM_RIGHT:
//                        floatView.selecteRightBtn();
//                        break;
//                }
//                return false;
//            }
//
//            @Override
//            public boolean completeDragOutSide(int dragLocation, int position) {
//                final int appPosition = adapter.getNatureIndex(position);
//                floatView.clearSelectedBtn();
//                switch (dragLocation) {
//                    case CustomDragableView.DRAG_OUTSIDE_BOTTOM_LEFT:
//                        MTAManager.addShortCut(mActivity, appInfos.get(appPosition).getPackageName());
//                        EventReportManager.addShortCut(mActivity, appInfos.get(appPosition).getPackageName());
//                        CommonUtils.createShortCut(mActivity,appInfos.get(appPosition));
//                        floatView.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                CustomToastUtils.showImageWithMsg(mActivity, mActivity.getResources().getString(R.string.toast_shortcut_added), R.mipmap.icon_add_success);
//                            }
//                        },CustomFloatView.ANIM_DURATION / 2);
//                        showFullScreenAd();                        break;
//                    case CustomDragableView.DRAG_OUTSIDE_BOTTOM_RIGHT:
//                        adapter.notifyDataSetChanged();
//                        floatView.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                showDeleteDialog(appPosition,position);
//                            }
//                        },CustomFloatView.ANIM_DURATION / 2);
//                        return true;
//                }
//                return false;
//            }
//
//            @Override
//            public boolean onCancleDragOutSide() {
//                floatView.clearSelectedBtn();
//                return false;
//            }
//        });
//        appGridView.setOnPageChangeListener(new CustomDragableView.OnPageChangeListener() {
//            @Override
//            public void onPageCountChanged(int pageCount) {
////                if(appGridView.getPageCount() <= 1){
////                    indicator.setVisibility(View.INVISIBLE);
////                }else{
////                    indicator.setVisibility(View.VISIBLE);
////                    indicator.setTotalPageSize(appGridView.getPageCount());
////                }
//            }
//
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//              //  indicator.setCurrentPage(position);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
//        appGridView.setOnRearrangeListener(new CustomDragableView.OnRearrangeListener() {
//            @Override
//            public void onRearrange(int oldIndex, int newIndex) {
//                if (oldIndex < 0 || newIndex < 0 || oldIndex >= appInfos.size() || newIndex >= appInfos.size()) {
//                    return;
//                }
//                AppModel model = appInfos.get(oldIndex);
//                appInfos.remove(oldIndex);
//                appInfos.add(newIndex, model);
//                adapter.notifyDataSetChanged();
//                if (oldIndex > newIndex) {
//                    updateModelIndex(newIndex, oldIndex);
//                } else {
//                    updateModelIndex(oldIndex, newIndex);
//                }
//            }
//        });
    }
    private void inflateFbNativeAdView(IAd ad) {
        View adView = LayoutInflater.from(getActivity()).inflate(R.layout.front_page_native_ad, null);
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        adView.setLayoutParams(params);
        if (ad != null && adView != null) {
            BasicLazyLoadImageView coverView = (BasicLazyLoadImageView) adView.findViewById(R.id.ad_cover_image);
            coverView.setDefaultResource(0);
            coverView.requestDisplayURL(ad.getCoverImageUrl());
            BasicLazyLoadImageView iconView = (BasicLazyLoadImageView) adView.findViewById(R.id.ad_icon_image);
            iconView.setDefaultResource(0);
            iconView.requestDisplayURL(ad.getIconImageUrl());
            TextView titleView = (TextView) adView.findViewById(R.id.ad_title);
            titleView.setText(ad.getTitle());
            TextView subtitleView = (TextView) adView.findViewById(R.id.ad_subtitle_text);
            subtitleView.setText(ad.getBody());
            TextView ctaView = (TextView) adView.findViewById(R.id.ad_cta_text);
            ctaView.setText(ad.getCallToActionText());


            nativeAdContainer.addView(adView);
            ad.registerViewForInteraction(nativeAdContainer);
            if (ad.getPrivacyIconUrl() != null) {
                BasicLazyLoadImageView choiceIconImage = (BasicLazyLoadImageView) adView.findViewById(R.id.ad_choices_image);
                choiceIconImage.setDefaultResource(0);
                choiceIconImage.requestDisplayURL(ad.getPrivacyIconUrl());
                ad.registerPrivacyIconView(choiceIconImage);
            }
        }
    }

    private void loadAdmobNativeExpress(){
        mAdmobExpressView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                L.d("onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                L.d("onAdFailedToLoad " + i);
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
                mAdmobExpressView.setVisibility(View.VISIBLE);
                L.d("onAdLoaded ");
            }
        });
        if (AdConstants.DEBUG) {
            String android_id = AdUtils.getAndroidID(getActivity());
            String deviceId = AdUtils.MD5(android_id).toUpperCase();
            AdRequest request = new AdRequest.Builder().addTestDevice(deviceId).build();
            boolean isTestDevice = request.isTestDevice(getActivity());
            L.d( "is Admob Test Device ? "+deviceId+" "+isTestDevice);
            mAdmobExpressView.loadAd(request );
        } else {
            mAdmobExpressView.loadAd(new AdRequest.Builder().build());
        }
    }
    private void loadNativeAd() {
        if (mNativeAdLoader == null) {
            mNativeAdLoader = new FuseAdLoader(getActivity());
            mNativeAdLoader.addAdSource(AdConstants.NativeAdType.AD_SOURCE_FACEBOOK, "1700354860278115_1702636763383258", -1);
        }
        mNativeAdLoader.loadAd(1, new IAdLoadListener() {
            @Override
            public void onAdLoaded(IAd ad) {
                if (ad.getAdType().equals(AdConstants.NativeAdType.AD_SOURCE_FACEBOOK)
                        || ad.getAdType().equals(AdConstants.NativeAdType.AD_SOURCE_VK)) {
                    inflateFbNativeAdView(ad);
                }
            }

            @Override
            public void onAdListLoaded(List<IAd> ads) {

            }

            @Override
            public void onError(String error) {
                loadAdmobNativeExpress();
            }
        });
    }

    private void initData(){
        loadNativeAd();
        CloneHelper.getInstance(mActivity).loadClonedApps(mActivity, new CloneHelper.OnClonedAppChangListener() {
            @Override
            public void onInstalled(List<AppModel> clonedApp) {
                if(pkgGridAdapter != null){
                    pkgGridAdapter.notifyDataSetChanged();
                }
                needShowGuideLayout();
            }

            @Override
            public void onUnstalled(List<AppModel> clonedApp) {
                if(pkgGridAdapter != null){
                    pkgGridAdapter.notifyDataSetChanged();
                }
                needShowGuideLayout();
            }

            @Override
            public void onLoaded(List<AppModel> clonedApp) {
                appInfos = clonedApp;
                if(pkgGridAdapter != null){
                    pkgGridAdapter.notifyDataSetChanged();
                }
                //adapter = new AppHomeAdapter(mActivity, appInfos,appGridView.getmPageSize());
                //appGridView.setAdapter(adapter);
                needShowGuideLayout();
            }
        });
    }

    private void needShowGuideLayout(){
//        if(appInfos == null || appInfos.size() <= 0){
//            iconGifView.setGifResource(R.mipmap.logo);
//            iconGifView.play();
//            guideLayout.setVisibility(View.VISIBLE);
//        }else{
//            iconGifView.pause();
//            guideLayout.setVisibility(View.GONE);
//        }
        guideLayout.setVisibility(View.GONE);
    }

    private void showDeleteDialog(AppModel appModel){
        CustomDialogUtils.showCustomDialog(mActivity,mActivity.getResources().getString(R.string.delete_dialog_title),
                mActivity.getResources().getString(R.string.delete_dialog_content),
                mActivity.getResources().getString(R.string.delete_dialog_left),mActivity.getResources().getString(R.string.delete_dialog_right),
                new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case CustomDialogUtils.LEFT_BUTTON:
                                break;
                            case CustomDialogUtils.RIGHT_BUTTON:
                                deleteAppWithAnim(appModel);
                                break;
                        }
                    }
                });
    }

    private void deleteAppWithAnim(AppModel appModel){
        if(appModel != null){
            appModel.setUnEnable(true);
            pkgGridAdapter.notifyDataSetChanged();
            //adapter.onDelete();
        }
        View view = pkgGridView.getChildAt(pkgGridAdapter.getPosition(appModel));
        mExplosionField.explode(view, new ExplosionField.OnExplodeFinishListener() {
            @Override
            public void onExplodeFinish(View v) {
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        deleteApp(appModel);
                    }
                },1000);
            }
        });
    }

    private void deleteApp(AppModel appModel){
        appInfos.remove(appModel);
        MTAManager.deleteClonedApp(mActivity, appModel.getPackageName());
        EventReportManager.deleteClonedApp(mActivity, appModel.getPackageName());
//        updateModelIndex(itemPosition,appInfos.size() - 1);
        AppManager.uninstallApp(appModel.getPackageName());
        CommonUtils.removeShortCut(mActivity,appModel);
        DbManager.deleteAppModel(mActivity, appModel);
        DbManager.notifyChanged();
        pkgGridAdapter.notifyDataSetChanged();
        //adapter.deleteComplete();
        needShowGuideLayout();
        showFullScreenAd();
    }

    private void startAppLaunchActivity(String packageName){
        if(mActivity instanceof  HomeActivity){
            ((HomeActivity)mActivity).startAppLaunchActivity(packageName);
        }
    }

    private void showFullScreenAd(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                LocalAdUtils.showFullScreenAd(mActivity,false);
            }
        },AnimatorHelper.DURATION_NORMAL);
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

    private void updateModelIndex(int startIndex, int endIndex) {
        ArrayList<AppModel> updateList = new ArrayList<>();
        for (int i = startIndex; i <= endIndex; i++) {
            AppModel model = appInfos.get(i);
            model.setIndex(i);
            updateList.add(model);
        }

        update(updateList);
    }

    private synchronized void update(ArrayList<AppModel> updateList){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DbManager.updateAppModelList(mActivity,updateList);
//                DbManager.notifyChanged();
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
            mActivity.startActivityForResult(i, Constants.REQUEST_SELECT_APP);
            mActivity.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }
    }
    public void showGuidePopWindow(){
//        View childView = appGridView.getChildAt(adapter.getFirstAppIndex());
//        View iconView = childView.findViewById(R.id.app_icon);
//        int[] location= new int[2];
//        iconView.getLocationInWindow(location);
//        if(mActivity instanceof  HomeActivity){
//            ((HomeActivity)mActivity).showGuidePopWindow(location[0] + iconView.getWidth(),location[1] + iconView.getHeight());
//        }
    }
}
