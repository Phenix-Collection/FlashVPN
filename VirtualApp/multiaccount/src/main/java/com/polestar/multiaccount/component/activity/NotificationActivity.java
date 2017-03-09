package com.polestar.multiaccount.component.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.component.adapter.BasicPackageSwitchAdapter;
import com.polestar.multiaccount.constant.AppConstants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.AppManager;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.widgets.BlueSwitch;
import com.polestar.multiaccount.widgets.FixedListView;

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
//                DbManager.notifyChanged();
                AppManager.setAppNotificationFlag(model.getPackageName(), status);
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
        mMasterSwitch.setChecked(PreferencesUtils.getBoolean(mContext, AppConstants.KEY_SERVER_PUSH, true));
        mMasterSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pName = mContext.getPackageName();
                boolean val = ((BlueSwitch) v).isChecked();
                PreferencesUtils.putBoolean(mContext, AppConstants.KEY_SERVER_PUSH, val);
                if (val) {
                    mListView.setVisibility(View.VISIBLE);
                } else {
                    for (AppModel model: mClonedModels) {
                        model.setNotificationEnable(false);
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
