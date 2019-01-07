
package com.mobile.earnings.api.data_models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDataAfterRegistration{

    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("login")
    @Expose
    public String login;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("id")
    @Expose
    public int id;

}
