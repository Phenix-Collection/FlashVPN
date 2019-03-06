package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

public class VpnRequirement extends TimeModel {
    /**
     * "created_at": "2019-02-20 02:33:51",
     "updated_at": "2019-02-20 02:33:51",
     "from_ip": "183.194.166.134",
     "public_ip": "51.15.118.154",
     "private_ip": "",
     "device_id": "nova1_nova.fast.free.vpn",
     "port": 26312,
     "password": "hzouve9N",
     "limit_speed_normal": -1,
     "limit_speed_vip": -1
     */
    @SerializedName("public_ip")
    public String mPublicIp;
    @SerializedName("port")
    public int mPort;
    @SerializedName("password")
    public int mPassword;
    @SerializedName("limit_speed_normal")
    public int mLimitSpeedNormal;
    @SerializedName("limit_speed_vip")
    public int mLimitSpeedVip;
}
