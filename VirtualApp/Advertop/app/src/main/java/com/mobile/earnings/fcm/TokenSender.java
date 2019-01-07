package com.mobile.earnings.fcm;

import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.responses.BaseResponse;
import com.mobile.earnings.api.services.AuthApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscriber;
import rx_fcm.internal.RxFcm;

public class TokenSender{

	public static void sendToken(){
		final AuthApi service = RetroFitServiceFactory.createSimpleRetroFitService(AuthApi.class);
		RxFcm.Notifications.currentToken().subscribe(new Subscriber<String>(){
			@Override
			public void onCompleted(){

			}

			@Override
			public void onError(Throwable e){
				Log.e("FCM", "CurrentTokenError: " + e.getMessage());
			}

			@Override
			public void onNext(String s){
				Call<BaseResponse> call = service.addPushToken(App.getDeviceID(), s);
				call.enqueue(new Callback<BaseResponse>(){
					@Override
					public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response){
						Log.e("TokenSender:", "TokenSent");
					}

					@Override
					public void onFailure(Call<BaseResponse> call, Throwable throwable){
						Log.e("TokenSender:", "Error: " + throwable.getMessage());
					}
				});
			}
		});
	}
}
