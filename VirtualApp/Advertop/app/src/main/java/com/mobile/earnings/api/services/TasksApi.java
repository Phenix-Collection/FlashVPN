package com.mobile.earnings.api.services;

import com.mobile.earnings.api.data_models.ReviewAppModel;
import com.mobile.earnings.api.responses.ApplicationResponse;
import com.mobile.earnings.api.responses.BaseResponse;
import com.mobile.earnings.api.responses.DefaultTaskResponse;
import com.mobile.earnings.api.responses.MainTasksResponse;
import com.mobile.earnings.api.responses.UpdateTaskResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TasksApi{

	@GET("tasks")
	@Headers("accept:application/json")
	Call<MainTasksResponse> getTasks(@Query("device_type") String deviceType,
									 @Query("device_token") String deviceID,
									 @Query("lat") float userLat,
									 @Query("long") float userLon,
									 @Query("skip") int skip,
									 @Query("take") int take);

	@Headers("Accept: application/json")
	@GET("tasks/active")
	Call<MainTasksResponse> getActiveTasks(@Query("device_token") String deviceId,
										   @Query("skip") int skip,
										   @Query("take") int take);

	@Headers("Accept: application/json")
	@GET("tasks/settings")
	Call<DefaultTaskResponse> getDefaultTasks(@Query("device_token") String deviceId);

	@Headers("Accept: application/json")
	@POST("tasks/update")
	@FormUrlEncoded
	Call<UpdateTaskResponse> updateTask(@Field("app_id") int id,
										@Field("device_token") String deviceID);

	@Headers("Accept: application/json")
	@GET("apps/{app_id}")
	Call<ApplicationResponse> getTaskData(@Path("app_id") int taskId,
										  @Query("device_token") String deviceId);

	@Headers("Accept: application/json")
	@PUT("apps/{app_id}/read")
	@FormUrlEncoded
	Call<BaseResponse> markWatched(@Path("app_id") int appId,
								   @Field("device_token") String deviceId);

	@Headers("Accept: application/json")
	@POST("tasks/vk-group")
	@FormUrlEncoded
	Call<BaseResponse> finishVkTask(@Field("device_token") String deviceId);

	@Headers("Accept: application/json")
	@POST("tasks/video")
	@FormUrlEncoded
	Call<BaseResponse> finishVideoTask(@Field("device_token") String deviceId);

	@Headers("Accept: application/json")
	@GET("apps/{app_id}")
	Call<ReviewAppModel> getReviewAppModel(@Path("app_id") int appId,
										   @Query("device_token") String deviceToken);
	@Headers("Accept: application/json")
	@POST("tasks/review")
	@Multipart
	Call<BaseResponse> sendScreenshot(@Part("device_token") RequestBody deviceId,
									  @Part("image\"; filename=\"pp.png") RequestBody image,
									  @Part("app_id") RequestBody appId);

}
