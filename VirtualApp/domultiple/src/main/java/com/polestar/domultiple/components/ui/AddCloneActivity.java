package com.polestar.domultiple.components.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdSize;
import com.lody.virtual.client.core.VirtualCore;
import com.polestar.ad.AdConstants;
import com.polestar.ad.AdViewBinder;
import com.polestar.ad.adapters.FuseAdLoader;
import com.polestar.ad.adapters.IAdAdapter;
import com.polestar.ad.adapters.IAdLoadListener;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.DisplayUtils;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;
import com.polestar.domultiple.widget.SelectGridAppItem;
import com.polestar.domultiple.widget.SelectPkgGridAdapter;
import com.polestar.grey.GreyAttribute;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by PolestarApp on 2017/7/16.
 */

public class AddCloneActivity extends BaseActivity implements AdapterView.OnItemClickListener{
    private static final int APP_LIST_READY = 0;
    private static final String CONFIG_HOT_CLONE_LIST = "hot_clone_list";
    public static final String SLOT_ADD_CLONE_AD = "slot_add_clone_native";
    private List<SelectGridAppItem> hotAppList = new ArrayList<>();
    private List<SelectGridAppItem> otherAppList = new ArrayList<>();
    private LinearLayout hotAppLayout;
    private LinearLayout adContainer;
    private LinearLayout otherAppLayout;
    private GridView hotAppGridView;
    private GridView otherAppGridView;
    private TextView cloneButton;
    private int selected;
    private ProgressBar progressBar;
    private FuseAdLoader adLoader;
    private IAdAdapter mAd;
    private boolean appListReady;

    private void inflateNativeAd() {
        if (mAd == null || !appListReady) {
            return;
        }
        if (hotAppList.size() == 0) {
            adContainer = (LinearLayout) findViewById(R.id.ad_container_2);
        } else {
            adContainer = (LinearLayout) findViewById(R.id.ad_container_1);
        }
        final AdViewBinder viewBinder;
        switch (mAd.getAdType()) {
            case AdConstants.NativeAdType.AD_SOURCE_FACEBOOK:
                viewBinder =  new AdViewBinder.Builder(R.layout.add_clone_native_ad_fb)
                        .titleId(R.id.ad_title)
                        .textId(R.id.ad_subtitle_text)
                        .mainMediaId(R.id.ad_cover_image)
                        .callToActionId(R.id.ad_cta_text)
                        .privacyInformationId(R.id.ad_choices_container)
                        .build();
                break;
            default:
                viewBinder =  new AdViewBinder.Builder(R.layout.add_clone_native_ad_default)
                        .titleId(R.id.ad_title)
                        .textId(R.id.ad_subtitle_text)
                        .mainMediaId(R.id.ad_cover_image)
                        .callToActionId(R.id.ad_cta_text)
                        .privacyInformationId(R.id.ad_choices_image)
                        .build();
                break;
        }

        View adView = mAd.getAdView(viewBinder);
        if (adView != null) {
            try {
                adContainer.removeAllViews();
                adContainer.addView(adView);
                adContainer.setVisibility(View.VISIBLE);
            }catch (Exception ex){
                //possiblely inflateNativeAd called twice
//                #2799 java.lang.IllegalStateException
//                The specified child already has a parent. You must call removeView() on the child's parent first.
//
//                com.polestar.domultiple.components.ui.AddCloneActivity.inflateNativeAd(AddCloneActivity.java)
            }
        }
    }

    public static AdSize getBannerAdSize() {
        int dpWidth = DisplayUtils.px2dip(PolestarApp.getApp(), DisplayUtils.getScreenWidth(PolestarApp.getApp()));
        return new AdSize(dpWidth, 140);
    }

    private void loadAd() {
        if (adLoader == null) {
            adLoader = FuseAdLoader.get(SLOT_ADD_CLONE_AD, this);
        }
        adLoader.setBannerAdSize(getBannerAdSize());
        if (adLoader.hasValidAdSource()) {
            mAd = null;
            adLoader.loadAd(2,1000, new IAdLoadListener() {
                @Override
                public void onAdLoaded(IAdAdapter ad) {
                    mAd = ad;
                    if(appListReady) {
                        inflateNativeAd();
                    }
                }

                @Override
                public void onAdListLoaded(List<IAdAdapter> ads) {

                }

                @Override
                public void onError(String error) {

                }
            });
        }
    }

    private Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case APP_LIST_READY:
                    selected = 0;
                    progressBar.setVisibility(View.GONE);
                    cloneButton.setVisibility(View.VISIBLE);
                    updateGrid();
                    appListReady = true;
                    if (mAd != null) {
                        inflateNativeAd();
                    }
                    break;
            }
        }
    };

    private void updateGrid() {
        if (hotAppList == null || hotAppList.size() == 0) {
            hotAppLayout.setVisibility(View.GONE);
            View otherTitle = findViewById(R.id.other_clone_title);
            View otherDetail = findViewById(R.id.other_clone_detail);
            View noHotTitle = findViewById(R.id.no_hot_title);
            otherDetail.setVisibility(View.GONE);
            otherTitle.setVisibility(View.GONE);
            noHotTitle.setVisibility(View.VISIBLE);
        } else {
            MLogs.d("Hot app size: " + hotAppList.size());
            hotAppLayout.setVisibility(View.VISIBLE);
            SelectPkgGridAdapter adapter = new SelectPkgGridAdapter(this,hotAppList);
            hotAppGridView.setAdapter(adapter);
            hotAppGridView.setOnItemClickListener(this);
            adapter.notifyDataSetChanged();
            //setGrideViewHeightBasedOnChildren(hotAppGridView);
        }
        if (otherAppList == null || otherAppList.size() == 0) {
            otherAppLayout.setVisibility(View.GONE);
        } else {
            MLogs.d("Other app size: " + otherAppList.size());
            otherAppLayout.setVisibility(View.VISIBLE);
            SelectPkgGridAdapter adapter = new SelectPkgGridAdapter(this,otherAppList);
            otherAppGridView.setAdapter(adapter);
            otherAppGridView.setOnItemClickListener(this);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        if(!PreferencesUtils.isAdFree()) {
            loadAd();
        }
    }

    private void initData() {
        loadAppListAsync();
    }

    private void initView() {
        setContentView(R.layout.add_clone_activity_layout);
        setTitle(getString(R.string.add_clone_title));
        hotAppLayout = (LinearLayout) findViewById(R.id.hot_clone_layout);
        hotAppGridView = (GridView) findViewById(R.id.hot_clone_grid);
        otherAppLayout = (LinearLayout) findViewById(R.id.other_clone_layout);
        otherAppGridView = (GridView) findViewById(R.id.other_clone_grid);
        cloneButton = (TextView)findViewById(R.id.clone_button);
        cloneButton.setText(String.format(getString(R.string.clone_action_txt), ""));
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
    }

    private void loadAppListAsync() {
        // NOT include host APP itself, already cloned APP in core and popular APP.
        appListReady = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String hotCloneConf = RemoteConfig.getString(CONFIG_HOT_CLONE_LIST);
                HashSet<String> hotCloneSet = new HashSet<>();
                if(!TextUtils.isEmpty(hotCloneConf)) {
                    String[] arr = hotCloneConf.split(":");
                    for (String s: arr) {
                        hotCloneSet.add(s);
                    }
                }
                PackageManager pm = getPackageManager();
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
                String hostPkg = getPackageName();

                for (ResolveInfo resolveInfo : resolveInfos) {
                    String pkgName = resolveInfo.activityInfo.packageName;
                    if (hostPkg.equals(pkgName)) {
                        continue;
                    }
                    if (!CloneManager.getInstance(AddCloneActivity.this).isAllowedToClone(pkgName)) {
                        continue;
                    }
                    if (!CloneManager.getInstance(AddCloneActivity.this).isClonable(pkgName)) {
                        MLogs.d("package: " + pkgName + " not clonable!");
                        continue;
                    }
                    SelectGridAppItem item = new SelectGridAppItem();
                    item.icon = resolveInfo.activityInfo.loadIcon(pm);
                    item.name = resolveInfo.activityInfo.loadLabel(pm);
                    item.selected = false;
                    item.pkg = pkgName;
                    if (hotCloneSet.contains(pkgName) && hotAppList.size() < 6) {
                        hotAppList.add(item);
                    } else{
                        otherAppList.add(item);
                    }
                }
                mHandler.sendEmptyMessage(APP_LIST_READY);
            }
        }).start();
    }

    public void onCloneClick(View view) {
        boolean selected = false;
        boolean hasLock = false;
        CloneManager cm = CloneManager.getInstance(this);
        cm.clearPendingClones();
        for (SelectGridAppItem item: hotAppList) {
            if( item.selected) {
                CloneModel model = new CloneModel(item.pkg, this);
                if (CommonUtils.isSocialApp(item.pkg)) {
                    model.setLockerState(AppConstants.AppLockState.ENABLED_FOR_CLONE);
                    model.setNotificationEnable(true);
                }
                //model.setName(cm.getDefaultName(item.pkg));
                int userId = cm.getNextAvailableUserId(item.pkg);
                PackageManager pm = getPackageManager();
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(item.pkg, 0);
                    CharSequence label = pm.getApplicationLabel(ai);
                    model.setName(VirtualCore.getCompatibleName(""+label, userId));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                GreyAttribute.checkAndClick(AddCloneActivity.this, model.getPackageName());
                cm.createClone(this, model, userId);
                selected = true;
            }
        }
        for (SelectGridAppItem item: otherAppList) {
            if( item.selected) {
                CloneModel model = new CloneModel(item.pkg, this);
                if (CommonUtils.isSocialApp(item.pkg)) {
                    model.setLockerState(AppConstants.AppLockState.ENABLED_FOR_CLONE);
                }
                int userId = cm.getNextAvailableUserId(item.pkg);
                PackageManager pm = getPackageManager();
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(item.pkg, 0);
                    CharSequence label = pm.getApplicationLabel(ai);
                    model.setName(VirtualCore.getCompatibleName(""+label, userId));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                GreyAttribute.checkAndClick(AddCloneActivity.this, model.getPackageName());
                cm.createClone(this, model, userId);
                selected = true;
            }
        }
        MLogs.d("on clone button click");
        if (!selected) {
            Toast.makeText(this, R.string.no_selection_for_clone, Toast.LENGTH_LONG).show();
        } else {
            PreferencesUtils.setHasCloned();
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SelectGridAppItem item = (SelectGridAppItem) view.getTag();
        if (item != null) {
            ImageView cbox = (ImageView) view.findViewById(R.id.select_cb_img);
            View cover = view.findViewById(R.id.cover);
            if (cbox != null) {
                item.selected = !item.selected;
                if (item.selected) {
                    selected ++;
                    cbox.setImageResource(R.drawable.selectd);
                    cover.setVisibility(View.INVISIBLE);
                } else {
                    selected --;
                    cbox.setImageResource(R.drawable.not_select);
                    cover.setVisibility(View.VISIBLE);
                }
                if (selected > 0) {
                    cloneButton.setText(String.format(getString(R.string.clone_action_txt), "(" + selected + ")"));
                    cloneButton.setEnabled(true);

                } else {
                    cloneButton.setText(String.format(getString(R.string.clone_action_txt), ""));
                    cloneButton.setEnabled(false);

                }
            }
        }
    }
}
