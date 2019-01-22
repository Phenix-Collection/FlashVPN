package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

public class UserProduct extends TimeModel {
    @SerializedName("id")
    public long mId;

    @SerializedName("cost")
    public float mCost;
}
