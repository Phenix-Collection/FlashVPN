package com.mobile.earnings.api.services;

import com.mobile.earnings.api.data_models.PaymentSystemResponse;
import com.mobile.earnings.api.responses.BaseResponse;
import com.mobile.earnings.api.responses.UserFundsResponse;
import com.mobile.earnings.api.responses.ReplenishmentResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface FundsApi{

	@Headers("Accept: application/json")
	@GET("user/money")
	Call<UserFundsResponse> getFunds(@Query("device_token") String deviceID);

	@Headers("Accept: application/json")
	@POST("user/money/transfer")
	@FormUrlEncoded
	Call<UserFundsResponse> transfer(@Field("device_token") String deviceId);

	@Headers("Accept: application/json")
	@POST("user/money/refill")
	@FormUrlEncoded
	Call<BaseResponse> refillBalance(@Field("device_token") String deviceId,
									 @Field("amt") float amount,
									 @Field("type") String type);

	@Headers("Accept: application/json")
	@PUT("user/replenishment/check")
	@FormUrlEncoded
	Call<ReplenishmentResponse> check(@Field("device_token") String deviceId,
									  @Field("amt") float amount);

	@Headers("Accept: application/json")
	@POST("user/replenishment/request")
	@FormUrlEncoded
	Call<ReplenishmentResponse> refillBalance(@Field("device_token") String deviceId,
											  @Field("phone") String userAccount,
											  @Field("amt") float amount,
											  @Field("method") String paymentSystemName);

	@Headers("Accept: application/json")
	@GET("/api/v2/payment-systems")
	Call<PaymentSystemResponse> getPaymentSystems(@Query("device_token") String deviceId);
}
