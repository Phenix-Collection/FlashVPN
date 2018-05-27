package com.polestar.domultiple.components.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.polestar.clone.CloneAgent64;
import com.polestar.clone.CustomizeAppData;
import com.polestar.domultiple.AppConstants;
import com.polestar.domultiple.PolestarApp;
import com.polestar.domultiple.R;
import com.polestar.domultiple.clone.CloneManager;
import com.polestar.domultiple.db.CloneModel;
import com.polestar.domultiple.db.DBManager;
import com.polestar.domultiple.utils.PreferencesUtils;
import com.polestar.domultiple.widget.BlueSwitch;
import com.polestar.domultiple.widget.FixedListView;
import com.polestar.domultiple.widget.PackageSwitchListAdapter;

import java.util.List;

/**
 * Created by PolestarApp on 2017/7/18.
 */

public class NotificationActivity extends BaseActivity {

    private BlueSwitch mMasterSwitch;
    private FixedListView mListView;
    private PackageSwitchListAdapter mNotificationAdapter;
    private List<CloneModel> mClonedModels;
    private Context mContext;
    private TextView mPerAppTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity_layout);
        mContext = this;
        initModels();
        initView();
    }

    private void initModels() {
        mClonedModels = CloneManager.getInstance(this).getClonedApps();
    }

    private void initView() {
        setTitle(getString(R.string.notification));

        mMasterSwitch = (BlueSwitch) findViewById(R.id.switch_notification);
        mListView = (FixedListView) findViewById(R.id.switch_notifications_apps);
        mNotificationAdapter = new PackageSwitchListAdapter(mContext);
        mNotificationAdapter.setOnCheckStatusChangedListener(new PackageSwitchListAdapter.OnCheckStatusChangedListener() {
            @Override
            public void onCheckStatusChangedListener(CloneModel model, boolean status) {
                model.setNotificationEnable(status);
                DBManager.updateCloneModel(mContext, model);
                if (PolestarApp.isSupportPkgExist()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            CloneAgent64 agent64 = new CloneAgent64(NotificationActivity.this);
                            CustomizeAppData data = CustomizeAppData.loadFromPref(model.getPackageName(), model.getPkgUserId());
                            data.isNotificationEnable = status;
                            data.saveToPref();
                            agent64.syncPackageSetting(model.getPackageName(), model.getPkgUserId(),data);
                        }
                    }).start();
                }
//                DBManager.notifyChanged();
            }
        });
        mNotificationAdapter.setIsCheckedCallback(new PackageSwitchListAdapter.IsCheckedCallback() {
            @Override
            public boolean isCheckedCallback(CloneModel model) {
                return model == null? false: model.getNotificationEnable();
            }
        });
        mListView.setAdapter(mNotificationAdapter);
        mNotificationAdapter.setModels(mClonedModels);
        mMasterSwitch.setChecked(PreferencesUtils.getBoolean(mContext, AppConstants.PreferencesKey.NOTIFICATION_MASTER_SWITCH, true));
        mMasterSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pName = mContext.getPackageName();
                boolean val = ((BlueSwitch) v).isChecked();
                PreferencesUtils.putBoolean(mContext, AppConstants.PreferencesKey.NOTIFICATION_MASTER_SWITCH, val);
                if (val) {
                    mListView.setVisibility(View.VISIBLE);
                    if (mClonedModels.size() > 0) {
                        mPerAppTxt.setVisibility(View.VISIBLE);
                    }
                } else {
                    for (CloneModel model: mClonedModels) {
                        model.setNotificationEnable(false);
                    }
                    if (PolestarApp.isSupportPkgExist()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CloneAgent64 agent64 = new CloneAgent64(NotificationActivity.this);
                                for (CloneModel model: mClonedModels) {
                                    model.setNotificationEnable(false);
                                    CustomizeAppData data = CustomizeAppData.loadFromPref(model.getPackageName(), model.getPkgUserId());
                                    data.isNotificationEnable = false;
                                    data.saveToPref();
                                    agent64.syncPackageSetting(model.getPackageName(), model.getPkgUserId(),data);
                                }
                            }
                        }).start();
                    }
                    mNotificationAdapter.notifyDataSetChanged();
                    mListView.setVisibility(View.INVISIBLE);
                    mPerAppTxt.setVisibility(View.INVISIBLE);
                }
            }
        });
        mPerAppTxt = (TextView) findViewById(R.id.enable_per_app_txt);
        if (mClonedModels.size() == 0) {
            mPerAppTxt.setVisibility(View.INVISIBLE);
        }
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
