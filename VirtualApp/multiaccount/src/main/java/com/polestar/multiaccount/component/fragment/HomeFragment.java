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

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseFragment;
import com.polestar.multiaccount.component.activity.AppListActivity;
import com.polestar.multiaccount.component.activity.HomeActivity;
import com.polestar.multiaccount.component.adapter.AppHomeAdapter;
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
import com.polestar.multiaccount.widgets.AppIconBgView;
import com.polestar.multiaccount.widgets.CustomDragableView;
import com.polestar.multiaccount.widgets.CustomFloatView;
import com.polestar.multiaccount.widgets.GifView;
import com.polestar.multiaccount.widgets.PageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yxx on 2016/7/19.
 */
public class HomeFragment extends BaseFragment {
    private View contentView;
    private CustomDragableView appGridView;
    private GridView pkgGridView;
    private PackageGridAdapter pkgGridAdapter;
    private PageIndicator indicator;
    private AppHomeAdapter adapter;
    private List<AppModel> appInfos;
    private ImageView addBtn, removeBtn;
    private CustomFloatView floatView;
    private LinearLayout guideLayout;
    private GifView iconGifView;
    private ExplosionField mExplosionField;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        long time = System.currentTimeMillis();
        Logs.e("onCreateView time1 = " + time);
        contentView = inflater.inflate(R.layout.fragment_home, null);
        mExplosionField = ExplosionField.attachToWindow(mActivity);
        initView();
        initData();
        Logs.e("onCreateView time = " + (System.currentTimeMillis() - time));
        return contentView;
    }

    private class PackageGridAdapter extends BaseAdapter {
        class ViewHolder {
            ImageView appIcon;
            TextView appName;
        }

        @Override
        public int getCount() {
            return appInfos == null ? 0 : appInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return appInfos == null ? null : appInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            PackageGridAdapter.ViewHolder holder = null;
//        if (view != null) {
//            holder = (ViewHolder) view.getTag();
//        }
//        if (view == null || holder == null) {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.item_app, null);
            holder = new PackageGridAdapter.ViewHolder();
            holder.appIcon = (ImageView) view.findViewById(R.id.app_icon);
            holder.appName = (TextView) view.findViewById(R.id.app_name);
            view.setTag(holder);
//        }

            AppModel appModel = appInfos.get(i);
            if (appModel.getCustomIcon() == null) {
                appModel.setCustomIcon(BitmapUtils.createCustomIcon(getActivity(), appModel.initDrawable(getActivity())));
            }

            if (appModel.getCustomIcon() != null) {
                holder.appIcon.setImageBitmap(appModel.getCustomIcon());
            } else {
//            holder.appIcon.setImageDrawable(appModel.initDrawable(mContext));
            }
            holder.appName.setText(appModel.getName());

            return view;
        }
    }
    private void initView() {
        appGridView = (CustomDragableView) contentView.findViewById(R.id.dragable_view);
        pkgGridView = (GridView) contentView.findViewById(R.id.grid_app);
        pkgGridAdapter = new PackageGridAdapter();
        pkgGridView.setAdapter(pkgGridAdapter);
        pkgGridView.setVisibility(View.GONE);
        indicator = (PageIndicator) contentView.findViewById(R.id.indicator);
        guideLayout = (LinearLayout) contentView.findViewById(R.id.guide_layout);
        iconGifView = (GifView) contentView.findViewById(R.id.icon_gif);
        floatView = (CustomFloatView) contentView.findViewById(R.id.addApp_btn);
        floatView.startBreath();

        floatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Logs.d("JJJJ Add onClick");
                startAppListActivity();
                MTAManager.homeAdd(getActivity());
                EventReportManager.homeAdd(getActivity());
            }
        });
        floatView.post(new Runnable() {
            @Override
            public void run() {
                appGridView.setLayoutPercent(1f - ((float) floatView.getHeight()) / ((float) appGridView.getHeight()));
            }
        });

        pkgGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                final int i = adapter.getNatureIndex(position);
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
                return true;
            }
        });

        appGridView.setOnDragListener(new CustomDragableView.OnDragListener() {
            @Override
            public void onDragStart(View view) {
                AppIconBgView bgView = (AppIconBgView) view.findViewById(R.id.app_icon_anim_bg);
                if(bgView != null){
                    bgView.startAnim();
                }
                floatView.animToExtands();
            }

            @Override
            public void onDragEnd(View view) {
                AppIconBgView bgView = (AppIconBgView) view.findViewById(R.id.app_icon_anim_bg);
                if(bgView != null){
                    bgView.reset();
                }
                floatView.animToIdel();
            }

            @Override
            public boolean onDragOutSide(int dragLocation) {
                switch (dragLocation) {
                    case CustomDragableView.DRAG_OUTSIDE_BOTTOM_LEFT:
                        floatView.selecteLeftBtn();
                        break;
                    case CustomDragableView.DRAG_OUTSIDE_BOTTOM_RIGHT:
                        floatView.selecteRightBtn();
                        break;
                }
                return false;
            }

            @Override
            public boolean completeDragOutSide(int dragLocation, int position) {
                final int appPosition = adapter.getNatureIndex(position);
                floatView.clearSelectedBtn();
                switch (dragLocation) {
                    case CustomDragableView.DRAG_OUTSIDE_BOTTOM_LEFT:
                        MTAManager.addShortCut(mActivity, appInfos.get(appPosition).getPackageName());
                        EventReportManager.addShortCut(mActivity, appInfos.get(appPosition).getPackageName());
                        CommonUtils.createShortCut(mActivity,appInfos.get(appPosition));
                        floatView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                CustomToastUtils.showImageWithMsg(mActivity, mActivity.getResources().getString(R.string.toast_shortcut_added), R.mipmap.icon_add_success);
                            }
                        },CustomFloatView.ANIM_DURATION / 2);
                        showFullScreenAd();                        break;
                    case CustomDragableView.DRAG_OUTSIDE_BOTTOM_RIGHT:
                        adapter.notifyDataSetChanged();
                        floatView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                showDeleteDialog(appPosition,position);
                            }
                        },CustomFloatView.ANIM_DURATION / 2);
                        return true;
                }
                return false;
            }

            @Override
            public boolean onCancleDragOutSide() {
                floatView.clearSelectedBtn();
                return false;
            }
        });
        appGridView.setOnPageChangeListener(new CustomDragableView.OnPageChangeListener() {
            @Override
            public void onPageCountChanged(int pageCount) {
//                if(appGridView.getPageCount() <= 1){
//                    indicator.setVisibility(View.INVISIBLE);
//                }else{
//                    indicator.setVisibility(View.VISIBLE);
//                    indicator.setTotalPageSize(appGridView.getPageCount());
//                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
              //  indicator.setCurrentPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        appGridView.setOnRearrangeListener(new CustomDragableView.OnRearrangeListener() {
            @Override
            public void onRearrange(int oldIndex, int newIndex) {
                if (oldIndex < 0 || newIndex < 0 || oldIndex >= appInfos.size() || newIndex >= appInfos.size()) {
                    return;
                }
                AppModel model = appInfos.get(oldIndex);
                appInfos.remove(oldIndex);
                appInfos.add(newIndex, model);
                adapter.notifyDataSetChanged();
                if (oldIndex > newIndex) {
                    updateModelIndex(newIndex, oldIndex);
                } else {
                    updateModelIndex(oldIndex, newIndex);
                }
            }
        });
        removeBtn = (ImageView) contentView.findViewById(R.id.remove);
        addBtn = (ImageView) contentView.findViewById(R.id.add);
    }

    private void initData(){
        CloneHelper.getInstance(mActivity).loadClonedApps(mActivity, new CloneHelper.OnClonedAppChangListener() {
            @Override
            public void onInstalled(List<AppModel> clonedApp) {
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
                needShowGuideLayout();
                appGridView.setCurrentPage(appGridView.getPageCount() - 1,false);
            }

            @Override
            public void onUnstalled(List<AppModel> clonedApp) {
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
                needShowGuideLayout();
            }

            @Override
            public void onLoaded(List<AppModel> clonedApp) {
                appInfos = clonedApp;
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
                adapter = new AppHomeAdapter(mActivity, appInfos,appGridView.getmPageSize());
                appGridView.setAdapter(adapter);
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

    private void showDeleteDialog(int appPosition,int itemPosition){
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
                                deleteAppWithAnim(appPosition,itemPosition);
                                break;
                        }
                    }
                });
    }

    private void deleteAppWithAnim(int appPosition,int itemPosition){
        if(appPosition < 0 || appPosition >= appInfos.size())
            return;
        if(itemPosition >= 0 && itemPosition < pkgGridView.getCount()) {
            AppModel appModel = appInfos.get(itemPosition);
            if(appModel != null){
                appModel.setUnEnable(true);
                adapter.onDelete();
            }
            View view = pkgGridView.getChildAt(itemPosition);
            mExplosionField.explode(view, new ExplosionField.OnExplodeFinishListener() {
                @Override
                public void onExplodeFinish(View v) {
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            deleteApp(appPosition);
                        }
                    },1000);
                }
            });
        }
    }

    private void deleteApp(int itemPosition){
        AppModel appModel = appInfos.remove(itemPosition);
        MTAManager.deleteClonedApp(mActivity, appModel.getPackageName());
        EventReportManager.deleteClonedApp(mActivity, appModel.getPackageName());
        updateModelIndex(itemPosition,appInfos.size() - 1);
        AppManager.uninstallApp(appModel.getPackageName());
        CommonUtils.removeShortCut(mActivity,appModel);
        DbManager.deleteAppModel(mActivity, appModel);
        DbManager.notifyChanged();
        adapter.notifyDataSetChanged();
        adapter.deleteComplete();
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
        View childView = appGridView.getChildAt(adapter.getFirstAppIndex());
        View iconView = childView.findViewById(R.id.app_icon);
        int[] location= new int[2];
        iconView.getLocationInWindow(location);
        if(mActivity instanceof  HomeActivity){
            ((HomeActivity)mActivity).showGuidePopWindow(location[0] + iconView.getWidth(),location[1] + iconView.getHeight());
        }
    }
}
