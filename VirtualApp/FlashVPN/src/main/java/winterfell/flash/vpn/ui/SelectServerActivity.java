package winterfell.flash.vpn.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import winterfell.flash.vpn.reward.network.datamodels.RegionServers;
import winterfell.flash.vpn.reward.network.datamodels.VpnServer;

import java.util.List;

import winterfell.flash.vpn.R;
import winterfell.flash.vpn.network.VPNServerIntermediaManager;
import winterfell.flash.vpn.utils.MLogs;
import winterfell.flash.vpn.utils.PreferenceUtils;

public class SelectServerActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener{
    private RadioButton autoCheckBox;
    private ListView serverListView;
    private CompoundButton currentSelected;
    private ImageView autoSignalImg;
    private VPNServerIntermediaManager vpnServerIntermediaManagerManager;
    private ServerListAdapter listAdapter;
    private ProgressBar refreshProgress;
    private ImageView refreshIcon;
    private int originPrefer;

    public static void start(Activity activity, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(activity, SelectServerActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected boolean useCustomTitleBar() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vpnServerIntermediaManagerManager = VPNServerIntermediaManager.getInstance(this);
        initView();
        originPrefer = PreferenceUtils.getPreferServer();
    }

    private void initView(){

        setContentView(R.layout.activity_select_server);
        refreshProgress = findViewById(R.id.refresh_status);
        refreshIcon = findViewById(R.id.refresh_icon);
        autoCheckBox = findViewById(R.id.auto_checkbox);
        autoCheckBox.setOnCheckedChangeListener(this);
        serverListView = findViewById(R.id.server_list_view);
        listAdapter = new ServerListAdapter();
        listAdapter.updateServers();
        serverListView.setAdapter(listAdapter);
        autoSignalImg = findViewById(R.id.best_server_signal);
        if (vpnServerIntermediaManagerManager.getBestServer() != null) {//如果网络不好，可能什么数据都没拿到呢
            autoSignalImg.setImageResource(vpnServerIntermediaManagerManager.getBestServer().getSignalResId());
        }
        int prefered = PreferenceUtils.getPreferServer();
        if (prefered == VpnServer.SERVER_ID_AUTO) {
            currentSelected = autoCheckBox;
            currentSelected.setChecked(true);
        }
        View view = findViewById(R.id.content);
        setImmerseLayout(view);

        LinearLayout linearLayout = findViewById(R.id.auto_checkbox_layout);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoCheckBox.setChecked(true);
            }
        });
    }

    private void updateSelected(CompoundButton compoundButton) {
        MLogs.d("updateSelected : " + PreferenceUtils.getPreferServer());
        if (compoundButton.getId() == R.id.auto_checkbox) {
            PreferenceUtils.setPreferServer(VpnServer.SERVER_ID_AUTO);
        } else {
            RegionServers si = (RegionServers)compoundButton.getTag();
            if (si != null) {
                PreferenceUtils.setPreferServer(si.getId());
            }
            autoCheckBox.setChecked(false);
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            updateSelected(compoundButton);
        }
    }

    public void onRefreshClick(View view) {
        refreshIcon.setClickable(false);
        refreshProgress.setVisibility(View.VISIBLE);
        refreshProgress.setIndeterminate(true);
        vpnServerIntermediaManagerManager.asyncUpdatePing(new VPNServerIntermediaManager.OnUpdatePingListener() {
            @Override
            public void onPingUpdated(boolean res) {
                listAdapter.updateServers();
                listAdapter.notifyDataSetChanged();
                refreshProgress.setVisibility(View.INVISIBLE);
                if (vpnServerIntermediaManagerManager.getBestServer() != null) {
                    autoSignalImg.setImageResource(vpnServerIntermediaManagerManager.getBestServer().getSignalResId());
                }
                refreshIcon.setClickable(true);
            }
        }, true);
    }

    private class ServerListAdapter extends BaseAdapter {
        private List<RegionServers> servers;
        public ServerListAdapter() {
           updateServers();
        }

        public void updateServers(){
            if (servers != null) {
                servers.clear();
            }
            servers = VPNServerIntermediaManager.getInstance(SelectServerActivity.this).getDupInterRegionServers();

            MLogs.d("Get Servers num: " + servers.size());
        }

        @Override
        public int getCount() {
            return servers.size();
        }

        @Override
        public Object getItem(int i) {
            return servers.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView =  LayoutInflater.from(SelectServerActivity.this).inflate(R.layout.server_list_item, viewGroup, false);
            }
            ImageView flag = convertView.findViewById(R.id.img_flag);
            ImageView signal = convertView.findViewById(R.id.img_signal);
            TextView city = convertView.findViewById(R.id.txt_city);
            final RadioButton checkbox = convertView.findViewById(R.id.checkbox);
            checkbox.setOnCheckedChangeListener(SelectServerActivity.this);

            convertView.setClickable(true);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkbox.setChecked(true);
                }
            });

            RegionServers si = (RegionServers)getItem(i);
            checkbox.setTag(si);
            flag.setImageResource(si.getFirstServer().getFlagResId());
            city.setText(si.getFirstServer().mCity);
            signal.setImageResource(si.getFirstServer().getSignalResId());
            checkbox.setChecked(si.getId() == PreferenceUtils.getPreferServer());
            return convertView;
        }
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        if (PreferenceUtils.getPreferServer() == originPrefer) {
//            setResult(RESULT_CANCELED);
//        } else {
//            setResult(RESULT_OK);
//            MLogs.d("server changed");
//        }
//        finish();
//    }

    @Override
    public void onBackPressed() {
        if (PreferenceUtils.getPreferServer() == originPrefer) {
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_OK);
            MLogs.d("server changed on back");
        }
        super.onBackPressed();

//        finish();
    }
}
