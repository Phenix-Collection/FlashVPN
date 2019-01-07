package com.mobile.earnings.api.modules;

import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.responses.AuthorizationResponse;
import com.mobile.earnings.api.listeners.OnRegisterListener;
import com.mobile.earnings.api.services.AuthApi;
import com.mobile.earnings.fcm.TokenSender;
import com.mobile.earnings.utils.ReportEvents;
import com.crashlytics.android.Crashlytics;
import com.yandex.metrica.YandexMetrica;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.earnings.utils.Constantaz.DEVICE_TYPE;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_REFERRAL_CODE_NOT_EXISTS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_SUCCESS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_USER_ALREADY_EXISTS;

public class RegisterModule{

	private OnRegisterListener listener;

	public void registerUser(final String promoCode, String name, String login, String cityName, float lat, float lon, String email, String phoneNumber, int age, String sex){
		final String deviceId = App.getDeviceID();
		AuthApi service = RetroFitServiceFactory.createSimpleRetroFitService(AuthApi.class);
		Call<AuthorizationResponse> call = service.register(DEVICE_TYPE, deviceId, promoCode, name, login, cityName, lat, lon, age, sex, email, phoneNumber);
		call.enqueue(new Callback<AuthorizationResponse>(){
			@Override
			public void onResponse(Call<AuthorizationResponse> call, Response<AuthorizationResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						TokenSender.sendToken();
						if(promoCode != null && !promoCode.isEmpty()) {
							YandexMetrica.reportEvent(ReportEvents.REPORT_EVENT_PROMO_SIGN_IN);
						}
						if(listener != null) {
							listener.onSuccess(response.body());
						}
						Crashlytics.setUserIdentifier(deviceId);
						break;
					case HTTP_REFERRAL_CODE_NOT_EXISTS:
						if(listener != null) {
							listener.onError(R.string.registerDialogWrongPromoTitle);
						}
						break;
					case HTTP_USER_ALREADY_EXISTS:
						Log.d("REGISTER", "onResponse: cannot create user");
						break;
					default:
						listener.onError(R.string.someErrorText);
						break;
				}
			}

			@Override
			public void onFailure(Call<AuthorizationResponse> call, Throwable e){
				Log.e("REGISTER", "Error: " + e.toString());
				listener.onError(R.string.someErrorText);
			}
		});
	}

	public void setOnRegisterSuccessListener(OnRegisterListener listener){
		this.listener = listener;
	}

}
