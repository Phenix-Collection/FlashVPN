package com.mobile.earnings.api.services;

import com.mobile.earnings.api.responses.UserResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ProfileApi{

	@Headers("Accept: application/json")
	@GET("user")
	Call<UserResponse> getUserInfo(@Query("device_token") String deviceID,
										 @Query("device_type") String type);

	@Headers("Accept: application/json")
	@POST("user")
	@FormUrlEncoded
	Call<UserResponse> updateUserProfile(@Field("device_token") String deviceId,
										 @Field("name") String name,
										 @Field("email") String email,
										 @Field("phone_number") String phoneNumber,
										 @Field("year") int year,
										 @Field("gender") String gender);
}
