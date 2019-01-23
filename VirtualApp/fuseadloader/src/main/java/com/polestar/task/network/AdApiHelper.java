package com.polestar.task.network;

import android.util.Log;

import com.polestar.task.ADErrorCode;
import com.polestar.task.IProductStatusListener;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.IUserStatusListener;
import com.polestar.task.network.datamodels.User;
import com.polestar.task.network.datamodels.UserProduct;
import com.polestar.task.network.datamodels.UserTask;
import com.polestar.task.network.responses.ProductsResponse;
import com.polestar.task.network.responses.TasksResponse;
import com.polestar.task.network.services.AuthApi;
import com.polestar.task.network.services.ProductsApi;
import com.polestar.task.network.services.TasksApi;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdApiHelper {
    private static <T> ADErrorCode createADErrorFromResponse(Response<T> response) {
        try {
            String rawErrorBody = response.errorBody().string();
            return new ADErrorCode(response.code(), rawErrorBody);
        } catch (IOException e) {
            e.printStackTrace();
            return new ADErrorCode(response.code(), response.message());
        }
    }

    public static void register(String deviceId, final IUserStatusListener listener) {
        AuthApi service = RetrofitServiceFactory.createSimpleRetroFitService(AuthApi.class);
        Call<User> call = service.registerAnonymous(deviceId);
        call.enqueue(new Callback<User>(){
            @Override
            public void onResponse(Call<User> call, Response<User> response){
                Log.i(Configuration.HTTP_TAG, "onResponse: "+ response.toString());

                switch(response.code()){
                    case 200:
                        User ur = response.body();
                        Log.i(Configuration.HTTP_TAG, "onResponse: "+ ur.mReferralCode);
                        if (listener != null) {
                            listener.onRegisterSuccess(ur);
                        }
                        break;
                    default:
                        if (listener != null) {
                            listener.onGeneralError(createADErrorFromResponse(response));
                        }
                        break;
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t){
                Log.e(Configuration.HTTP_TAG, "onFailure: " + t.getMessage());
                if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                    listener.onRegisterFailed(ADErrorCode.createFromAdErrMsg(t.getMessage()));
                } else {
                    listener.onRegisterFailed(ADErrorCode.createServerDown());
                }
            }
        });
    }

    public static void getAvailableProducts(final IProductStatusListener listener) {
        ProductsApi service = RetrofitServiceFactory.createSimpleRetroFitService(ProductsApi.class);
        Call<ProductsResponse> call = service.getAvailableProducts();
        call.enqueue(new Callback<ProductsResponse>() {
            @Override
            public void onResponse(Call<ProductsResponse> call, Response<ProductsResponse> response) {
                Log.i(Configuration.HTTP_TAG, "onResponse: "+ response.toString());

                switch(response.code()){
                    case 200:
                        ProductsResponse ur = response.body();
                        Log.i(Configuration.HTTP_TAG, "onResponse product count: "+ ur.mProducts.size());
                        if (listener != null) {
                            listener.onGetAllAvailableProducts(ur.mProducts);
                        }
                        break;
                    default:
                        if (listener != null) {
                            listener.onGeneralError(createADErrorFromResponse(response));
                        }
                        break;
                }
            }

            @Override
            public void onFailure(Call<ProductsResponse> call, Throwable t) {
                Log.e(Configuration.HTTP_TAG, "onFailure: "+ t.getMessage());
                // getProducts has no predefined err, so this must be server error
                if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                    listener.onGeneralError(ADErrorCode.createFromAdErrMsg(t.getMessage()));
                } else {
                    listener.onGeneralError(ADErrorCode.createServerDown());
                }
            }
        });
    }

    public static void consumeProduct(String deviceId, long id, final IProductStatusListener listener) {
        ProductsApi service = RetrofitServiceFactory.createSimpleRetroFitService(ProductsApi.class);
        Call<UserProduct> call = service.consumeProduct(deviceId, id);
        call.enqueue(new Callback<UserProduct>() {
            @Override
            public void onResponse(Call<UserProduct> call, Response<UserProduct> response) {
                Log.i(Configuration.HTTP_TAG, "onResponse: "+ response.toString());

                switch(response.code()){
                    case 200:
                        UserProduct ur = response.body();
                        Log.i(Configuration.HTTP_TAG, "onResponse cost: "+ ur.mCost);
                        //TODO
                        break;
                    default:
                        if (listener != null) {
                            listener.onGeneralError(createADErrorFromResponse(response));
                        }
                        break;
                }
            }

            @Override
            public void onFailure(Call<UserProduct> call, Throwable t) {
                Log.e(Configuration.HTTP_TAG, "onFailure: " + t.getMessage());
                if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                    listener.onGeneralError(ADErrorCode.createFromAdErrMsg(t.getMessage()));
                } else {
                    listener.onGeneralError(ADErrorCode.createServerDown());
                }
            }
        });
    }

    public static void getAvailableTasks(final ITaskStatusListener listener) {
        TasksApi service = RetrofitServiceFactory.createSimpleRetroFitService(TasksApi.class);
        Call<TasksResponse> call = service.getAvailableTasks();
        call.enqueue(new Callback<TasksResponse>() {
            @Override
            public void onResponse(Call<TasksResponse> call, Response<TasksResponse> response) {
                Log.i(Configuration.HTTP_TAG, "onResponse: "+ response.toString());

                switch(response.code()){
                    case 200:
                        TasksResponse ur = response.body();
                        Log.i(Configuration.HTTP_TAG, "onResponse task count "+ ur.mTasks.size());
                        if (listener != null) {
                            listener.onGetAllAvailableTasks(ur.mTasks);
                        }
                        break;
                    default:
                        if (listener != null) {
                            listener.onGeneralError(createADErrorFromResponse(response));
                        }
                        break;
                }
            }

            @Override
            public void onFailure(Call<TasksResponse> call, Throwable t) {
                Log.e(Configuration.HTTP_TAG, "onFailure: "+t.getMessage());
                if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                    listener.onGeneralError(ADErrorCode.createFromAdErrMsg(t.getMessage()));
                } else {
                    listener.onGeneralError(ADErrorCode.createServerDown());
                }
            }
        });
    }

    public static void finishTask(String deviceId, long id, final ITaskStatusListener listener) {
        TasksApi service = RetrofitServiceFactory.createSimpleRetroFitService(TasksApi.class);
        Call<UserTask> call = service.finishTask(deviceId, id);
        call.enqueue(new Callback<UserTask>() {
            @Override
            public void onResponse(Call<UserTask> call, Response<UserTask> response) {
                Log.i(Configuration.HTTP_TAG, "onResponse: "+ response.toString());

                switch(response.code()){
                    case 200:
                        UserTask ur = response.body();
                        Log.i(Configuration.HTTP_TAG, "onResponse paid: "+ ur.mPayout);
                        //TODO
                        break;
                    default:
                        if (listener != null) {
                            listener.onGeneralError(createADErrorFromResponse(response));
                        }
                        break;
                }
            }

            @Override
            public void onFailure(Call<UserTask> call, Throwable t) {
                Log.e(Configuration.HTTP_TAG, "onFailure: " + t.getMessage());
                if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                    listener.onGeneralError(ADErrorCode.createFromAdErrMsg(t.getMessage()));
                } else {
                    listener.onGeneralError(ADErrorCode.createServerDown());
                }
            }
        });
    }
}
