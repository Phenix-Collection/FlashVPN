package com.mobile.earnings.main.presenterImpls;

import android.support.annotation.NonNull;
import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.responses.RouletteResultResponse;
import com.mobile.earnings.api.responses.RouletteSettingsResponse;
import com.mobile.earnings.api.services.RouletteApi;
import com.mobile.earnings.main.views.RouletteView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_NOT_ENOUGH_FUNDS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_SUCCESS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_USER_NOT_FOUND;



public class RoulettePresenter{

	private RouletteView view;

	public RoulettePresenter(RouletteView view){
		this.view = view;
	}

	public void getBets(){
		view.showLoading();
		RouletteApi service = RetroFitServiceFactory.createSimpleRetroFitService(RouletteApi.class);
		Call<RouletteSettingsResponse> call = service.getBets(App.getDeviceID());
		call.enqueue(new Callback<RouletteSettingsResponse>(){
			@Override
			public void onResponse(@NonNull Call<RouletteSettingsResponse> call, @NonNull Response<RouletteSettingsResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						RouletteSettingsResponse settings = response.body();
						if(settings != null) {
							view.updateSettings(settings.settings);
						} else {
							view.informUser(R.string.someErrorText);
						}
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
				}
				view.hideLoading();
			}

			@Override
			public void onFailure(@NonNull Call<RouletteSettingsResponse> call, @NonNull Throwable t){
				Log.e("ROULETTE", "onFailure: "+t.getMessage());
				view.hideLoading();
			}
		});
	}

	public void startRolling(float bet){
		RouletteApi service = RetroFitServiceFactory.createSimpleRetroFitService(RouletteApi.class);
		Call<RouletteResultResponse> call = service.rollIt(App.getDeviceID(), bet);
		call.enqueue(new Callback<RouletteResultResponse>(){
			@Override
			public void onResponse(@NonNull Call<RouletteResultResponse> call, @NonNull Response<RouletteResultResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						RouletteResultResponse resultData = response.body();
						if(resultData != null) {
							view.startTimer();
							view.setResultData(resultData.rouletteRoll.result, resultData.rouletteRoll.amount, resultData.rouletteRoll.user.balance);
						} else {
							view.informUser(R.string.someErrorText);
						}
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					case HTTP_NOT_ENOUGH_FUNDS:
						view.informUser(R.string.roulette_NoFundsError);
						break;
					default:
						break;
				}
			}

			@Override
			public void onFailure(@NonNull Call<RouletteResultResponse> call, @NonNull Throwable t){
				Log.e("TAGA", "onFailure: "+t.getMessage());
			}
		});
	}

}
