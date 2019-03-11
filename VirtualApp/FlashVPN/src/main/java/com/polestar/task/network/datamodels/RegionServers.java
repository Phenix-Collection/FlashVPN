package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RegionServers {
    public static final int NONE_ID = -111;
    @SerializedName("region")
    public Region mRegion;
    @SerializedName("servers")
    public ArrayList<VpnServer> mServers;

    public VpnServer getFirstServer() {
        //应该总是不空的
        return mServers.get(0);
    }

    public int getId() {
        if (mRegion != null) {
            return mRegion.getId();
        }
        return NONE_ID;
    }

    public void dump() {
        mRegion.dump();
        for (VpnServer vpnServer : mServers) {
            vpnServer.dump();
        }
    }

    public boolean hasValidId() {
        return mRegion != null;
    }
}
