package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

public class Product extends TimeModel {
    @SerializedName("id")
    public long mId;

    @SerializedName("product_type")
    public int mProductType;
    @SerializedName("description")
    public String mDescription;
    @SerializedName("status")
    public int mStatus;
    @SerializedName("cost")
    public float mCost;
    @SerializedName("detail")
    public String mDetail;
}
