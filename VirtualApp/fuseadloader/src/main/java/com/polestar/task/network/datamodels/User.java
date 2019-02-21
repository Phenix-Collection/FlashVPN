package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

public class User extends TimeModel {
    @SerializedName("device_id")
    public String mDeviceId;
    @SerializedName("referral_code")
    public String mReferralCode;
    @SerializedName("balance")
    public float mBalance;
    @SerializedName("subscribe_status")
    public int mSubscribeStatus;
    @SerializedName("vpn_vip_left")
    public int mVpnVipLeft;
}
