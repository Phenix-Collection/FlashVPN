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
//ss://aes-256-cfb:passwd@95.179.225.74:28388

    //本地的性能数据如下
    @SerializedName("ping")
    public int mPingDelayMilli;
    @SerializedName("connect")
    public int mConnectDelayMilli;
    @SerializedName("downloadSpeed")
    public int mByteDownPs;
    @SerializedName("uploadSpeed")
    public int mByteUpPs;
}
