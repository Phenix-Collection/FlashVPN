package com.polestar.task.network.datamodels;

import com.google.gson.annotations.SerializedName;

public class TimeModel {
    @SerializedName("created_at")
    public String mCreatedAt;
    @SerializedName("updated_at")
    public String mUpdatedAt;
}
