package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

public class UserTask extends TimeModel {
    public static final int USER_TASK_STATUS_PENDING = 0;
    public static final int USER_TASK_STATUS_PAID = 1;

    @SerializedName("id")
    public long mId;
    @SerializedName("payout")
    public float mPayout;
    @SerializedName("status")
    public int mStatus;

    public boolean isPending() {
        return mStatus == USER_TASK_STATUS_PENDING;
    }

    public float getPayout() {
        if (isPending()) {
            return 0;
        }
        return mPayout;
    }
}
