package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

public class Product extends TimeModel {
    public static final int MONEY_PRODUCT_THRESHOLDER = 1000;

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

    public boolean isFunctionalProduct() {
        if (mProductType < MONEY_PRODUCT_THRESHOLDER) {
            return true;
        }
        return false;
    }

    public boolean isMoneyProduct() {
        return !isFunctionalProduct();
    }
}
