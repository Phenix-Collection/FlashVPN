package com.polestar.task.network.services;

import com.polestar.task.network.datamodels.UserTask;
import com.polestar.task.network.responses.TasksResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface TasksApi {
    @Headers("Accept: application/json")
    @GET("api/v1/task/getAvailableTasks")
    Call<TasksResponse> getAvailableTasks();

    @Headers("Accept: application/json")
    @POST("api/v1/task/finishTask")
    @FormUrlEncoded
    Call<UserTask> finishTask(@Field("device_id") String deviceID,
                              @Field("task_id") long productId);
}
