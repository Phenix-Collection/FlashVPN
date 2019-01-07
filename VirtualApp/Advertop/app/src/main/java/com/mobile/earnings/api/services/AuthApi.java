package com.mobile.earnings.api.services;

import com.mobile.earnings.api.responses.AuthorizationResponse;
import com.mobile.earnings.api.responses.BaseResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;



public interface AuthApi{

	@Headers("Accept: application/json")
	@PATCH("user/push-device-token")
	Call<BaseResponse> addPushToken(@Query("device_token") String deviceID, @Query("push_device_token") String deviceToken);

	@Headers("Accept: application/json")
	@POST("auth/login")
	@FormUrlEncoded
	Call<BaseResponse> login(@Field("device_token") String deviceId,
							 @Field("city") String cityName,
							 @Field("lat") float lat,
							 @Field("lng") float lon);

	@Headers("Accept: application/json")
	@POST("auth/register")
	@FormUrlEncoded
	Call<AuthorizationResponse> register(@Field("device_type") String deviceType,
										 @Field("device_token") String deviceID,
										 @Field("referral_code") String promoCode,
										 @Field("name") String name,
										 @Field("login") String login,
										 @Field("city") String cityName,
										 @Field("lat") float lat,
										 @Field("lng") float lon,
										 @Field("age") int age,
										 @Field("gender") String gender,
										 @Field("email") String email,
										 @Field("phone_number") String phoneNumber);
}
