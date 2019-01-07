package com.mobile.earnings.splash.presenter;

import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.responses.BaseResponse;
import com.mobile.earnings.api.services.AuthApi;
import com.mobile.earnings.fcm.TokenSender;
import com.mobile.earnings.splash.view.SplashView;
import com.mobile.earnings.utils.Constantaz;
import com.crashlytics.android.Crashlytics;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_SUCCESS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_USER_NOT_FOUND;

public class SplashPresenterImpl implements SplashPresenter{

	private SplashView view;

	public SplashPresenterImpl(SplashView view){
		this.view = view;
	}

	@Override
	public void loginUser(final String cityName, final float lat, final float lon){
		final AuthApi service = RetroFitServiceFactory.createSimpleRetroFitService(AuthApi.class);
		Call<BaseResponse> call = service.login(App.getDeviceID(), cityName, lat, lon);
		call.enqueue(new Callback<BaseResponse>(){
			@Override
			public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						TokenSender.sendToken();
						Crashlytics.setUserIdentifier(App.getDeviceID());
						Crashlytics.setUserName(App.getDeviceID());
						if(App.getPrefs().getBoolean(Constantaz.PREFS_TUTORIAL, false)) {
							view.openMainAct();
						} else{
							view.openTutorialScreen();
						}
						break;
					case HTTP_USER_NOT_FOUND:
						view.startRegisterAct(cityName, lat, lon);
						break;
				}
			}

			@Override
			public void onFailure(Call<BaseResponse> call, Throwable e){
				Log.e("LOGIN", "Failure: " + e.getMessage());
			}
		});
	}

}
