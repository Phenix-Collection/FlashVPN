package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

public class VpnServer  {

    @SerializedName("public_ip")
    public String mPublicIp;

    @SerializedName("geo")
    public String mGeo;

    @SerializedName("city")
    public String mCity;

    @SerializedName("pri")
    public int mPri;

    @SerializedName("vip")
    public int mVip;

    @SerializedName("is_online")
    public int mIsOnline;
}
