package com.polestar.task.network.responses;

import com.google.gson.annotations.SerializedName;

public class SucceedResponse {
    @SerializedName("errCode")
    public int mErrCode;
    @SerializedName("errMsg")
    public String mErrMsg;
}
