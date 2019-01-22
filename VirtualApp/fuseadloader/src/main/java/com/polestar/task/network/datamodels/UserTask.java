package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

public class UserTask extends TimeModel {
    @SerializedName("id")
    public long mId;
    @SerializedName("payout")
    public float mPayout;
}
