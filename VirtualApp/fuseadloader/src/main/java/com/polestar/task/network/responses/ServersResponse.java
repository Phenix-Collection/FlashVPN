package com.polestar.task.network.responses;

import com.google.gson.annotations.SerializedName;
import com.polestar.task.network.datamodels.VpnServer;

import java.util.ArrayList;

public class ServersResponse {
    @SerializedName("servers")
    public ArrayList<VpnServer> mVpnServers;
}
