package com.polestar.superclone.component.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.polestar.clone.CloneAgent64;
import com.polestar.clone.CustomizeAppData;
import com.polestar.superclone.MApp;
import com.polestar.superclone.R;
import com.polestar.superclone.component.BaseActivity;
import com.polestar.superclone.component.adapter.BasicPackageSwitchAdapter;
import com.polestar.superclone.constant.AppConstants;
import com.polestar.superclone.db.DbManager;
import com.polestar.superclone.model.AppModel;
import com.polestar.superclone.utils.PreferencesUtils;
import com.polestar.superclone.widgets.BlueSwitch;
import com.polestar.superclone.widgets.FixedListView;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends BaseActivity {

    private BlueSwitch mMasterSwitch;
    private FixedListView mListView;
    private BasicPackageSwitchAdapter mNotificationAdapter;
    private List<AppModel> mClonedModels;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        mContext = this;
        initModels();
        initView();
    }

    private void initModels() {
        mClonedModels = DbManager.queryAppList(mContext);
        for (AppModel model : mClonedModels) {
            model.setIcon(model.initDrawable(this));
        }
    }

    private void initView() {
        setTitle(getString(R.string.notifications));

        mMasterSwitch = (BlueSwitch) findViewById(R.id.switch_notification_dotspace);
        mListView = (FixedListView) findViewById(R.id.switch_notifications_apps);
        mNotificationAdapter = new BasicPackageSwitchAdapter(mContext);
        mNotificationAdapter.setOnCheckStatusChangedListener(new BasicPackageSwitchAdapter.OnCheckStatusChangedListener() {
            @Override
            public void onCheckStatusChangedListener(AppModel model, boolean status) {
                model.setNotificationEnable(status);
                DbManager.updateAppModel(mContext, model);
                CustomizeAppData data = CustomizeAppData.loadFromPref(model.getPackageName(), model.getPkgUserId());
                data.isNotificationEnable = status;
                data.saveToPref();
                if (MApp.isSupportPkgExist()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            CloneAgent64 agent64 = new CloneAgent64(NotificationActivity.this);
                            agent64.syncPackageSetting(model.getPackageName(), model.getPkgUserId(),data);
                        }
                    }).start();
                }
//                DbManager.notifyChanged();
            }
        });
        mNotificationAdapter.setIsCheckedCallback(new BasicPackageSwitchAdapter.IsCheckedCallback() {
            @Override
            public boolean isCheckedCallback(AppModel model) {
                return model == null? false: model.getNotificationEnable();
            }
        });
        mListView.setAdapter(mNotificationAdapter);
        mNotificationAdapter.setModels(mClonedModels);
        mMasterSwitch.setChecked(PreferencesUtils.isGlobalNotificationEnabled());
        mMasterSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean val = ((BlueSwitch) v).isChecked();
                PreferencesUtils.setGlobalNotification(val);
                //sync with arm64 package as only one syncPackageSetting api
                if (val) {
                    mListView.setVisibility(View.VISIBLE);
                    ArrayList<CustomizeAppData> changeList = new ArrayList<>();
                    for (AppModel model: mClonedModels) {
                        CustomizeAppData data = CustomizeAppData.loadFromPref(model.getPackageName(), model.getPkgUserId());
                        changeList.add(data);
                    }
                    if (MApp.isSupportPkgExist()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CloneAgent64 agent64 = new CloneAgent64(NotificationActivity.this);
                                for (CustomizeAppData data: changeList) {
                                    agent64.syncPackageSetting(data.pkg, data.userId,data);
                                }
                            }
                        }).start();
                    }
                } else {
                    ArrayList<CustomizeAppData> changeList = new ArrayList<>();
                    for (AppModel model: mClonedModels) {
//                        model.setNotificationEnable(false);
                        CustomizeAppData data = CustomizeAppData.loadFromPref(model.getPackageName(), model.getPkgUserId());
                        data.isNotificationEnable = false;
//                        data.saveToPref();
                        changeList.add(data);
                    }
                    if (MApp.isSupportPkgExist()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CloneAgent64 agent64 = new CloneAgent64(NotificationActivity.this);
                                for (CustomizeAppData data: changeList) {
                                    agent64.syncPackageSetting(data.pkg, data.userId,data);
                                }
                            }
                        }).start();
                    }
                    mNotificationAdapter.notifyDataSetChanged();
                    mListView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
