package com.mobile.earnings.ads.fragments.presentations;

import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.responses.BaseResponse;
import com.mobile.earnings.api.services.FundsApi;
import com.mobile.earnings.api.services.TasksApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.earnings.utils.Constantaz.RANDOM_CONSTANT;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_REQUEST_LIMIT_ERROR;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_SUCCESS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_USER_NOT_FOUND;


public class AdsPresenter{

	private AdsView view;

	public AdsPresenter(AdsView view){
		this.view = view;
	}

	public void payForVideoAd(final int number){
		TasksApi service = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		Call<BaseResponse> call = service.finishVideoTask(App.getDeviceID());
		call.enqueue(new Callback<BaseResponse>(){
			@Override
			public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						if(number == RANDOM_CONSTANT){
							view.informUser(R.string.adMobRandomMessage);
						}else{
							view.informUser(R.string.videoAd_successFullText);
						}
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					case HTTP_REQUEST_LIMIT_ERROR:
						view.informUser(R.string.videoLimitMessage);
						break;
					default:
						break;
				}
			}

			@Override
			public void onFailure(Call<BaseResponse> call, Throwable t){
				Log.e("VIDEO_PAY", "onFailure: "+t.getMessage());
				view.informUser(R.string.videoAd_failText);
			}
		});
	}

	public void payForOfferWall(float reward, String type){
		FundsApi service = RetroFitServiceFactory.createSimpleRetroFitService(FundsApi.class);
		Call<BaseResponse> call = service.refillBalance(App.getDeviceID(), reward, type);
		call.enqueue(new Callback<BaseResponse>(){
			@Override
			public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						view.informUser(R.string.videoAd_successFullText);
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					default:
						break;
				}
			}

			@Override
			public void onFailure(Call<BaseResponse> call, Throwable t){
				Log.e("OFFERWALL_PAY", "onFailure: "+t.getMessage());
				view.informUser(R.string.videoAd_failText);
			}
		});
	}
}
