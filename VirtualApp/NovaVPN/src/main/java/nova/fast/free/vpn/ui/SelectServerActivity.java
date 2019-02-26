package nova.fast.free.vpn.ui;

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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nova.fast.free.vpn.R;
import nova.fast.free.vpn.core.LocalVpnService;
import nova.fast.free.vpn.network.ServerInfo;
import nova.fast.free.vpn.network.VPNServerManager;
import nova.fast.free.vpn.utils.MLogs;
import nova.fast.free.vpn.utils.PreferenceUtils;

public class SelectServerActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener{
    private RadioButton autoCheckBox;
    private ListView serverListView;
    private CompoundButton currentSelected;
    private ImageView autoSignalImg;
    private VPNServerManager vpnServerManager;
    private ServerListAdapter listAdapter;
    private ProgressBar refreshProgress;
    private ImageView refreshIcon;

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
        vpnServerManager = VPNServerManager.getInstance(this);
        initView();
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
        autoSignalImg.setImageResource(vpnServerManager.getBestServer().getSignalResId());
        int prefered = PreferenceUtils.getPreferServer();
        if (prefered == ServerInfo.SERVER_ID_AUTO) {
            currentSelected = autoCheckBox;
            currentSelected.setChecked(true);
        }
        View view = findViewById(R.id.content);
        setImmerseLayout(view);
    }

    private void updateSelected(CompoundButton compoundButton) {
        MLogs.d("updateSelected : " + PreferenceUtils.getPreferServer());
        if (compoundButton.getId() == R.id.auto_checkbox) {
            PreferenceUtils.setPreferServer(ServerInfo.SERVER_ID_AUTO);
        } else {
            ServerInfo si = (ServerInfo)compoundButton.getTag();
            if (si != null) {
                PreferenceUtils.setPreferServer(si.id);
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
        vpnServerManager.asyncUpdatePing(new VPNServerManager.OnUpdatePingListener() {
            @Override
            public void onPingUpdated(boolean res, List<ServerInfo> serverInfos) {
                listAdapter.updateServers();
                listAdapter.notifyDataSetChanged();
                refreshProgress.setVisibility(View.INVISIBLE);
                autoSignalImg.setImageResource(vpnServerManager.getBestServer().getSignalResId());
                refreshIcon.setClickable(true);
            }
        }, true);
    }

    private class ServerListAdapter extends BaseAdapter {
        private List<ServerInfo> servers;
        public ServerListAdapter() {
           updateServers();
        }

        public void updateServers(){
            if (servers != null) {
                servers.clear();
            }
            servers = new ArrayList<>( VPNServerManager.getInstance(SelectServerActivity.this).getActiveServers());
            Collections.sort(servers, new Comparator<ServerInfo>() {
                @Override
                public int compare(ServerInfo serverInfo, ServerInfo t1) {
                    int country = serverInfo.country.compareToIgnoreCase(t1.country);
                    return  country == 0?serverInfo.id - t1.id: country;
                }
            });
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
            RadioButton checkbox = convertView.findViewById(R.id.checkbox);
            checkbox.setOnCheckedChangeListener(SelectServerActivity.this);
            ServerInfo si = (ServerInfo)getItem(i);
            checkbox.setTag(si);
            flag.setImageResource(si.getFlagResId());
            city.setText(si.city);
            signal.setImageResource(si.getSignalResId());
            checkbox.setChecked(si.id == PreferenceUtils.getPreferServer());
            return convertView;
        }
    }
}
