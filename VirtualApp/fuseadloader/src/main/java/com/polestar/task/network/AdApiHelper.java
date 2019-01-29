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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

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

    public static final int REQUEST_SUCCEED = 0;
    public static final int ERR_REQUEST_TOO_FREQUENT = 1;

    private static final long API_COMMON_INTERVAL = 60 * 1000; //60 seconds

    private static String KEY_REGISTER = "register";
    private static String KEY_GET_PRODUCTS = "getProducts";
    private static String KEY_GET_TASKS = "getTasks";
    private static String KEY_CONSUME_PRODUCT = "consumeProduct";
    private static String KEY_FINISH_TASK = "finishTask";

    private static final HashMap<String, Date> sTimeMapping = new HashMap<>();


    private static boolean canDoRequest(String requestKey, long thresholder) {
        Date lastTime = sTimeMapping.get(requestKey);
        if (lastTime == null) {
            sTimeMapping.put(requestKey, Calendar.getInstance().getTime());
            return true;
        }

        if (MiscUtils.tooCloseWithNow(lastTime, thresholder)) {
            Log.w(Configuration.HTTP_TAG, "Too close with last http request time " + lastTime.toString() + " for " + requestKey);
            return false;
        } else {
            sTimeMapping.put(requestKey, Calendar.getInstance().getTime());
            return true;
        }
    }

    public static int register(String deviceId, final IUserStatusListener listener) {
        return register(deviceId, listener, false);
    }

    public static int register(String deviceId, final IUserStatusListener listener, boolean force) {
        if (!force && !canDoRequest(KEY_REGISTER, API_COMMON_INTERVAL)) {
            return ERR_REQUEST_TOO_FREQUENT;
        }

        AuthApi service = RetrofitServiceFactory.createSimpleRetroFitService(AuthApi.class);
        Call<User> call = service.registerAnonymous(deviceId, null, null, null);
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

        return REQUEST_SUCCEED;
    }

    public static int getAvailableProducts(final IProductStatusListener listener) {
        return getAvailableProducts(listener, false);
    }

    public static int getAvailableProducts(final IProductStatusListener listener, boolean force) {
        if (!force && !canDoRequest(KEY_GET_PRODUCTS, API_COMMON_INTERVAL)) {
            return ERR_REQUEST_TOO_FREQUENT;
        }

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

        return REQUEST_SUCCEED;
    }

    public static int consumeProduct(String deviceId, final long id, final int amount, final IProductStatusListener listener) {
        return consumeProduct(deviceId, id, amount, listener, false);
    }

    public static int consumeProduct(String deviceId, final long id, final int amount, final IProductStatusListener listener,
                                        boolean force) {
        if (!force && !canDoRequest(KEY_CONSUME_PRODUCT, API_COMMON_INTERVAL)) {
            return ERR_REQUEST_TOO_FREQUENT;
        }

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

        return REQUEST_SUCCEED;
    }

    public static int getAvailableTasks(final ITaskStatusListener listener) {
        return getAvailableTasks(listener, false);
    }

    public static int getAvailableTasks(final ITaskStatusListener listener, boolean force) {
        if (!force && !canDoRequest(KEY_GET_TASKS, API_COMMON_INTERVAL)) {
            return ERR_REQUEST_TOO_FREQUENT;
        }

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

        return REQUEST_SUCCEED;
    }

    public static int finishTask(String deviceId, final long id, String referralCode, final ITaskStatusListener listener) {
        return finishTask(deviceId, id, referralCode, listener, false);
    }

    public static int finishTask(String deviceId, final long id, String referralCode, final ITaskStatusListener listener, boolean force) {
        if (!force && !canDoRequest(KEY_FINISH_TASK, API_COMMON_INTERVAL)) {
            return ERR_REQUEST_TOO_FREQUENT;
        }

        TasksApi service = RetrofitServiceFactory.createSimpleRetroFitService(TasksApi.class);
        Call<UserTaskResponse> call = service.finishTask(deviceId, id, referralCode);
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

        return REQUEST_SUCCEED;
    }
}
