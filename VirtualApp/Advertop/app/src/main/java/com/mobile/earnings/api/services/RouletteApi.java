package com.mobile.earnings.api.services;

import com.mobile.earnings.api.responses.RouletteResultResponse;
import com.mobile.earnings.api.responses.RouletteSettingsResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;




public interface RouletteApi{

	@Headers("Accept: application/json")
	@GET("tasks/roulette/settings")
	Call<RouletteSettingsResponse> getBets(@Query("device_token") String deviceId);

	@Headers("Accept: application/json")
	@POST("tasks/roulette/roll")
	@FormUrlEncoded
	Call<RouletteResultResponse> rollIt(@Field("device_token") String deviceID,
										@Field("bet") float bet);
}
