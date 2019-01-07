
package com.mobile.earnings.api.responses;

import com.mobile.earnings.api.data_models.UserDataAfterRegistration;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AuthorizationResponse{

    @SerializedName("user")
    @Expose
    public UserDataAfterRegistration userDataAfterRegistration;

}
