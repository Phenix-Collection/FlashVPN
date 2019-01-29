package com.polestar.task.network.services;

import com.polestar.task.network.responses.TasksResponse;
import com.polestar.task.network.responses.UserTaskResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TasksApi {
    @Headers("Accept: application/json")
    @GET("api/v1/task/getAvailableTasks")
    Call<TasksResponse> getAvailableTasks(@Query("device_id") String deviceID);

    @Headers("Accept: application/json")
    @POST("api/v1/task/finishTask")
    @FormUrlEncoded
    Call<UserTaskResponse> finishTask(@Field("device_id") String deviceID,
                                      @Field("task_id") long productId,
                                      @Field("referral_code") String referralCode);
}
