package com.polestar.task.network.responses;

import com.google.gson.annotations.SerializedName;
import com.polestar.task.network.datamodels.RegionServers;
import com.polestar.task.network.datamodels.VpnServer;

import java.util.ArrayList;

public class ServersResponse {
    @SerializedName("regionServers")
    public ArrayList<RegionServers> mVpnServers;

    public ServersResponse() {
        mVpnServers = new ArrayList<>();
    }
}
