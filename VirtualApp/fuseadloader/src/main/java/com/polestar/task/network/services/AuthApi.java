package com.polestar.task.network.services;

import com.polestar.task.network.datamodels.User;
import com.polestar.task.network.responses.UserResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthApi {

    @Headers("Accept: application/json")
    @POST("api/v1/user/registerAnonymous")
    @FormUrlEncoded
    Call<UserResponse> registerAnonymous(@Field("device_id") String deviceID);
}
