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
import com.polestar.task.network.responses.UserProductResponse;
import com.polestar.task.network.responses.UserTaskResponse;
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
                if (listener != null) {
                    if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                        listener.onRegisterFailed(ADErrorCode.createFromAdErrMsg(t.getMessage()));
                    } else {
                        listener.onRegisterFailed(ADErrorCode.createServerDown());
                    }
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
                if (listener != null) {
                    if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                        listener.onGeneralError(ADErrorCode.createFromAdErrMsg(t.getMessage()));
                    } else {
                        listener.onGeneralError(ADErrorCode.createServerDown());
                    }
                }
            }
        });
    }

    public static void consumeProduct(String deviceId, final long id, final int amount, final IProductStatusListener listener) {
        ProductsApi service = RetrofitServiceFactory.createSimpleRetroFitService(ProductsApi.class);
        Call<UserProductResponse> call = service.consumeProduct(deviceId, id, amount);
        call.enqueue(new Callback<UserProductResponse>() {
            @Override
            public void onResponse(Call<UserProductResponse> call, Response<UserProductResponse> response) {
                Log.i(Configuration.HTTP_TAG, "onResponse: "+ response.toString());

                switch(response.code()){
                    case 200:
                        UserProductResponse ur = response.body();
                        Log.i(Configuration.HTTP_TAG, "onResponse cost: "+ ur.mUserProduct.mCost);
                        if (listener != null) {
                            listener.onConsumeSuccess(id, amount, ur.mUserProduct.mCost, ur.mUser.mBalance);
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
            public void onFailure(Call<UserProductResponse> call, Throwable t) {
                Log.e(Configuration.HTTP_TAG, "onFailure: " + t.getMessage());
                if (listener != null) {
                    if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                        listener.onConsumeFail(ADErrorCode.createFromAdErrMsg(t.getMessage()));
                    } else {
                        listener.onGeneralError(ADErrorCode.createServerDown());
                    }
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
                Log.e(Configuration.HTTP_TAG, "onFailure: " + t.getMessage());
                if (listener != null) {
                    if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                        listener.onGeneralError(ADErrorCode.createFromAdErrMsg(t.getMessage()));
                    } else {
                        listener.onGeneralError(ADErrorCode.createServerDown());
                    }
                }
            }
        });
    }

    public static void finishTask(String deviceId, final long id, final ITaskStatusListener listener) {
        TasksApi service = RetrofitServiceFactory.createSimpleRetroFitService(TasksApi.class);
        Call<UserTaskResponse> call = service.finishTask(deviceId, id, null);
        call.enqueue(new Callback<UserTaskResponse>() {
            @Override
            public void onResponse(Call<UserTaskResponse> call, Response<UserTaskResponse> response) {
                Log.i(Configuration.HTTP_TAG, "onResponse: "+ response.toString());

                switch(response.code()){
                    case 200:
                        UserTaskResponse ur = response.body();
                        Log.i(Configuration.HTTP_TAG, "onResponse paid: "+ ur.mUserTask.mPayout);
                        if (listener != null) {
                            listener.onTaskSuccess(id, ur.mUserTask.getPayout(), ur.mUser.mBalance);
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
            public void onFailure(Call<UserTaskResponse> call, Throwable t) {
                Log.e(Configuration.HTTP_TAG, "onFailure: " + t.getMessage());
                if (listener != null) {
                    if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                        listener.onTaskFail(id, ADErrorCode.createFromAdErrMsg(t.getMessage()));
                    } else {
                        listener.onGeneralError(ADErrorCode.createServerDown());
                    }
                }
            }
        });
    }
}
