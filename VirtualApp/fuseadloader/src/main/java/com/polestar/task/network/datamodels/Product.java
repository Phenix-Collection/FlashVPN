package com.polestar.task.network.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Product extends TimeModel implements Parcelable {
    public static final int PRODUCT_TYPE_REMOVE_AD_1DAY = 0;
    public static final int PRODUCT_TYPE_REMOVE_AD_7DAY = 1;
    public static final int PRODUCT_TYPE_REMOVE_AD_30DAY = 2;

    public static final int MONEY_PRODUCT_THRESHOLDER = 1000;

    public static final int PRODUCT_TYPE_AMAZON = 1001;
    public static final int PRODUCT_TYPE_PAYPAL = 1002;


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
    @SerializedName("name")
    public String mName;
    @SerializedName("icon_url")
    public String mIconUrl;

    protected Product(Parcel in) {
        mCreatedAt = in.readString();
        mUpdatedAt = in.readString();
        mId = in.readLong();
        mProductType = in.readInt();
        mDescription = in.readString();
        mStatus = in.readInt();
        mCost = in.readFloat();
        mDetail = in.readString();
        mName = in.readString();
        mIconUrl = in.readString();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public boolean isFunctionalProduct() {
        if (mProductType < MONEY_PRODUCT_THRESHOLDER) {
            return true;
        }
        return false;
    }

    public boolean isMoneyProduct() {
        return !isFunctionalProduct();
    }

    public boolean isPaypal() {
        return mProductType == PRODUCT_TYPE_PAYPAL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mCreatedAt);
        parcel.writeString(mUpdatedAt);
        parcel.writeLong(mId);
        parcel.writeInt(mProductType);
        parcel.writeString(mDescription);
        parcel.writeInt(mStatus);
        parcel.writeFloat(mCost);
        parcel.writeString(mDetail);
        parcel.writeString(mName);
        parcel.writeString(mIconUrl);
    }
}
