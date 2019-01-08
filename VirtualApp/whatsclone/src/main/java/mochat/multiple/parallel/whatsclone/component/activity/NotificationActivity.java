package mochat.multiple.parallel.whatsclone.component.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.polestar.clone.CloneAgent64;
import com.polestar.clone.CustomizeAppData;

import mochat.multiple.parallel.whatsclone.MApp;
import mochat.multiple.parallel.whatsclone.R;
import mochat.multiple.parallel.whatsclone.component.BaseActivity;
import mochat.multiple.parallel.whatsclone.component.adapter.BasicPackageSwitchAdapter;
import mochat.multiple.parallel.whatsclone.constant.AppConstants;
import mochat.multiple.parallel.whatsclone.db.DbManager;
import mochat.multiple.parallel.whatsclone.model.AppModel;
import mochat.multiple.parallel.whatsclone.utils.PreferencesUtils;
import mochat.multiple.parallel.whatsclone.widgets.RoundSwitch;
import mochat.multiple.parallel.whatsclone.widgets.FixedListView;

import java.util.List;

public class NotificationActivity extends BaseActivity {

    private RoundSwitch mMasterSwitch;
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

        mMasterSwitch = (RoundSwitch) findViewById(R.id.switch_notification_dotspace);
        mListView = (FixedListView) findViewById(R.id.switch_notifications_apps);
        mNotificationAdapter = new BasicPackageSwitchAdapter(mContext);
        mNotificationAdapter.setOnCheckStatusChangedListener(new BasicPackageSwitchAdapter.OnCheckStatusChangedListener() {
            @Override
            public void onCheckStatusChangedListener(AppModel model, boolean status) {
                model.setNotificationEnable(status);
                DbManager.updateAppModel(mContext, model);
//                DbManager.notifyChanged();
                if (MApp.isSupportPkgExist()) {
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
                boolean val = ((RoundSwitch) v).isChecked();
                PreferencesUtils.putBoolean(mContext, AppConstants.KEY_SERVER_PUSH, val);
                if (val) {
                    mListView.setVisibility(View.VISIBLE);
                } else {
                    for (AppModel model: mClonedModels) {
                        model.setNotificationEnable(false);
                    }
                    mNotificationAdapter.notifyDataSetChanged();
                    mListView.setVisibility(View.INVISIBLE);
                    if (MApp.isSupportPkgExist()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CloneAgent64 agent64 = new CloneAgent64(NotificationActivity.this);
                                for (AppModel model: mClonedModels) {
                                    model.setNotificationEnable(false);
                                    CustomizeAppData data = CustomizeAppData.loadFromPref(model.getPackageName(), model.getPkgUserId());
                                    data.isNotificationEnable = false;
                                    data.saveToPref();
                                    agent64.syncPackageSetting(model.getPackageName(), model.getPkgUserId(),data);
                                }
                            }
                        }).start();
                    }
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
