package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RegionServers {
    public static final int NONE_ID = -111;
    @SerializedName("region")
    public Region mRegion;
    @SerializedName("servers")
    public ArrayList<VpnServer> mServers;

    public int getId() {
        if (mRegion != null) {
            return mRegion.getId();
        }
        return NONE_ID;
    }

    public boolean hasValidId() {
        return mRegion != null;
    }
}
