package com.polestar.task.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ErrorCodeInterceptor implements Interceptor {
    /**
     * 2019-01-22 使用retrofit的assumption以及规则
     *
     * 正常使用时
     * 1,只要服务器有响应，retrofit总是进入onResponse分支，哪怕服务器给出一个404, 500, 302等的http code
     * 2,进入onFailure的情况，目前我只看到服务器挂了，会进入onFailure
     *
     * 那么带来一个问题，在onResponse里头，我们怎么判别服务器可能给出的各种预定义的错误？
     * Adtop是通过使用不同的http status code来的，这个方式我目前不太认可；因为他改变了官方关于http status的定义
     *
     * 我们现在的处理方式是，加入一个interceptor，先看raw_response，如果raw_response里头
     * 包含了我们预定义的errCode errMsg，那么我们认为是服务器返回了预定义的错误。从而我们主动
     * throw一个IOException；
     * 使用者则会在onFailure里头获取到相应的errMsg，并且得知服务器返回了预定义的错误。
     *
     * 而我们的onResponse，抛开302不谈(现在还不清楚retrofit对于302是怎么处理的)，里面的http status应该
     * 都是200；并且raw_response的数据，始终是可以被deserialize成预定义的class的
     *
     * 假如没有这个errCode interceptor，那么服务器返回预定义的错误时，客户端根本不知道到底发生了什么错误。
     * 因为onResponse里那个期待的responseClass不包含任何东西,成员变量都是null。
     * Adtop是在onResponse里通过不同的http status code来区分错误
     * 我们是在onFailure里通过errMsg来区分错误
     *
     */


    public static boolean isAdError(Throwable throwable) {
        String errMsg = throwable.getMessage();
        if (errMsg != null && errMsg.startsWith(Configuration.ADERR_PREFIX)) {
            return true;
        }
        return false;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        okhttp3.Response response = chain.proceed(request);

        String responseBody = response.body().string();
        Log.i(Configuration.HTTP_TAG, "raw_response:" + responseBody);

        try {
            // 先看看是不是有服务器返回的错误，如果有，就throw出去，让onFailure分支去处理
            JSONObject jsonObject = new JSONObject(responseBody);
            int errCode = jsonObject.optInt("errCode");
            String errMsg = jsonObject.optString("errMsg");

            if (errCode > 0) {
                throw new IOException(errMsg);
            }
        } catch (JSONException e) {
            Log.i(Configuration.HTTP_TAG, "Invalid JSON response");
        }

        return response.newBuilder()
                .body(ResponseBody.create(response.body().contentType(), responseBody)).build();
    }
}
