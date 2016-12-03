package com.polestar.multiaccount.component.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.polestar.multiaccount.R;
import com.polestar.multiaccount.component.BaseActivity;
import com.polestar.multiaccount.component.adapter.NotificationAdapter;
import com.polestar.multiaccount.constant.Constants;
import com.polestar.multiaccount.db.DbManager;
import com.polestar.multiaccount.model.AppModel;
import com.polestar.multiaccount.utils.PreferencesUtils;
import com.polestar.multiaccount.widgets.BlueSwitch;
import com.polestar.multiaccount.widgets.FixedListView;

import java.util.List;

public class NotificationActivity extends BaseActivity {

    private BlueSwitch mMasterSwitch;
    private FixedListView mListView;
    private NotificationAdapter mNotificationAdapter;
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
        setTitle(Constants.TITLE_NOTIFICATION);

        mMasterSwitch = (BlueSwitch) findViewById(R.id.switch_notification_dotspace);
        mListView = (FixedListView) findViewById(R.id.switch_notifications_apps);
        mNotificationAdapter = new NotificationAdapter(mContext);
        mListView.setAdapter(mNotificationAdapter);
        mNotificationAdapter.setModels(mClonedModels);
        mMasterSwitch.setChecked(PreferencesUtils.getBoolean(mContext, Constants.KEY_SERVER_PUSH, true));
        mMasterSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pName = mContext.getPackageName();
                boolean val = ((BlueSwitch) v).isChecked();
                PreferencesUtils.putBoolean(mContext, Constants.KEY_SERVER_PUSH, val);
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