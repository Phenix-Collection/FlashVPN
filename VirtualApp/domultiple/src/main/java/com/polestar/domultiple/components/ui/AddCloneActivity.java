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

import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.utils.RemoteConfig;
import com.polestar.domultiple.widget.SelectGridAppItem;
import com.polestar.domultiple.widget.SelectPkgGridAdapter;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by guojia on 2017/7/16.
 */

public class AddCloneActivity extends BaseActivity implements AdapterView.OnItemClickListener{
    private static final int APP_LIST_READY = 0;
    private static final String CONFIG_HOT_CLONE_LIST = "hot_clone_list";
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

    private Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case APP_LIST_READY:
                    selected = 0;
                    progressBar.setVisibility(View.GONE);
                    cloneButton.setVisibility(View.VISIBLE);
                    updateGrid();
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
                    if (CloneManager.isAppInstalled(pkgName)) {
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
        for (SelectGridAppItem item: hotAppList) {
            if( item.selected) {
                CloneModel model = new CloneModel(item.pkg, this);
                if (CommonUtils.isSocialApp(item.pkg)) {
                    model.setLockerState(AppConstants.AppLockState.ENABLED_FOR_CLONE);
                }
                CloneManager.getInstance(this).createClone(this, model);
                selected = true;
            }
        }
        for (SelectGridAppItem item: otherAppList) {
            if( item.selected) {
                CloneModel model = new CloneModel(item.pkg, this);
                if (CommonUtils.isSocialApp(item.pkg)) {
                    model.setLockerState(AppConstants.AppLockState.ENABLED_FOR_CLONE);
                }
                CloneManager.getInstance(this).createClone(this, model);
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
