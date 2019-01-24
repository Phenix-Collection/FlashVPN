package com.polestar.task.network.responses;

import com.google.gson.annotations.SerializedName;
import com.polestar.task.network.datamodels.User;
import com.polestar.task.network.datamodels.UserProduct;

public class UserProductResponse {
    @SerializedName("user")
    public User mUser;
    @SerializedName("user_product")
    public UserProduct mUserProduct;
}
