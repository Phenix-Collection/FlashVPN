package com.polestar.domultiple.components.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.utils.CommonUtils;
import com.polestar.domultiple.utils.MLogs;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.widget.HomeGridAdapter;

import java.util.List;

/**
 * Created by guojia on 2017/7/15.
 */

public class HomeActivity extends BaseActivity implements CloneManager.OnClonedAppChangListener{
    private List<CloneModel> mClonedList;
    private CloneManager cm;
    private GridView cloneGridView;
    private HomeGridAdapter gridAdapter;
    boolean showLucky;

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    private void initData() {
        cm = CloneManager.getInstance(this);
        cm.loadClonedApps(this, this);
        showLucky = PreferencesUtils.hasCloned() && !PreferencesUtils.isAdFree();
        gridAdapter.setShowLucky(showLucky );
    }


    private void initView() {
        setContentView(R.layout.home_activity_layout);
        cloneGridView = (GridView) findViewById(R.id.clone_grid_view);
        gridAdapter = new HomeGridAdapter(this);
        cloneGridView.setAdapter(gridAdapter);
        cloneGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int luckyIdx = mClonedList.size();
                int addIdx = showLucky? luckyIdx + 1 : luckyIdx;
                if (i < mClonedList.size()) {

                } else if (showLucky && i == luckyIdx) {
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
                if (i >= mClonedList.size()) {
                    return false;
                }
                return false;
            }
        });
    }

    @Override
    public void onInstalled(CloneModel clonedApp, boolean result) {
        mClonedList = cm.getClonedApps();
        if (result && PreferencesUtils.getBoolean(this, AppConstants.KEY_AUTO_CREATE_SHORTCUT, false)) {
            CommonUtils.createShortCut(this, clonedApp);
        }
        gridAdapter.notifyDataSetChanged(mClonedList);
    }

    @Override
    public void onUnstalled(CloneModel clonedApp, boolean result) {
        mClonedList = cm.getClonedApps();
        if (result) {
            CommonUtils.removeShortCut(this, clonedApp);
        }
        gridAdapter.notifyDataSetChanged(mClonedList);
    }

    @Override
    public void onLoaded(List<CloneModel> clonedApp) {
        mClonedList = cm.getClonedApps();
        gridAdapter.notifyDataSetChanged(mClonedList);
    }
}
