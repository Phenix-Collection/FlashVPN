package com.mobile.earnings.main.presenterImpls;

import android.support.annotation.NonNull;
import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.data_models.AppModel;
import com.mobile.earnings.api.responses.BaseResponse;
import com.mobile.earnings.api.responses.DefaultTaskResponse;
import com.mobile.earnings.api.responses.MainTasksResponse;
import com.mobile.earnings.api.responses.UserFundsResponse;
import com.mobile.earnings.api.responses.UserResponse;
import com.mobile.earnings.api.responses.ApplicationResponse;
import com.mobile.earnings.api.responses.BaseCopyAliResponse;
import com.mobile.earnings.api.responses.SendCopyAliPromoCode;
import com.mobile.earnings.api.services.AliCopyService;
import com.mobile.earnings.api.services.FundsApi;
import com.mobile.earnings.api.services.ProfileApi;
import com.mobile.earnings.api.services.TasksApi;
import com.mobile.earnings.main.presenters.MainActPresenter;
import com.mobile.earnings.main.views.MainActView;
import com.mobile.earnings.utils.Constantaz;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.earnings.main.adapters.BaseTasksAdapter.VIEW_TYPE_ACTIVE_TASK;
import static com.mobile.earnings.utils.AppModelExtractor.extractAppModels;
import static com.mobile.earnings.utils.Constantaz.PREF_COMMENT_REWARD;
import static com.mobile.earnings.utils.Constantaz.PREF_REVIEW_REWARD;
import static com.mobile.earnings.utils.Constantaz.RANDOM_CONSTANT;
import static com.mobile.earnings.utils.Constantaz.TASKS_COUNT;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_APP_NOT_FOUND;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_REQUEST_LIMIT_ERROR;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_SUCCESS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_USER_NOT_FOUND;

public class MainActPresenterImpl implements MainActPresenter{

	private MainActView view;

	public MainActPresenterImpl(MainActView view){
		this.view = view;
	}

	@Override
	public void getTasks(){
		String deviceID = App.getDeviceID();
		TasksApi service = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		Call<MainTasksResponse> call = service.getTasks(Constantaz.DEVICE_TYPE, deviceID, 0, 0, 0, TASKS_COUNT);
		call.enqueue(new Callback<MainTasksResponse>(){
			@Override
			public void onResponse(Call<MainTasksResponse> call, Response<MainTasksResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						MainTasksResponse mainTasksResponse = response.body();
						view.initTaskList(extractAppModels(mainTasksResponse));
						break;
					case HTTP_USER_NOT_FOUND:
						view.hideLoading();
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					default:
						view.hideLoading();
						break;
				}
			}

			@Override
			public void onFailure(Call<MainTasksResponse> call, Throwable t){
				Log.e("TASKS", "Error downloading: " + t.getMessage());
				view.hideLoading();
			}
		});
	}

	@Override
	public void getActiveTasks(){
		String deviceId = App.getDeviceID();
		TasksApi service = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		Call<MainTasksResponse> call = service.getActiveTasks(deviceId, 0, TASKS_COUNT);
		call.enqueue(new Callback<MainTasksResponse>(){
			@Override
			public void onResponse(Call<MainTasksResponse> call, Response<MainTasksResponse> response){
				view.hideLoading();
				switch(response.code()){
					case HTTP_SUCCESS:
						MainTasksResponse mainTasksResponse = response.body();
						if(mainTasksResponse != null) {
							ArrayList<AppModel> activeTaskList = extractAppModels(mainTasksResponse);
							for(AppModel model : activeTaskList){
								model.viewType = VIEW_TYPE_ACTIVE_TASK;
							}
							view.initActiveList(activeTaskList);
						} else{
							view.informUser(R.string.someErrorText);
						}
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					default:
						break;
				}
			}

			@Override
			public void onFailure(Call<MainTasksResponse> call, Throwable t){
				Log.e("TASKS", "Error downloading: " + t.getMessage());
				view.hideLoading();
			}
		});
	}

	@Override
	public void getDefaultTaskPrices(){
		TasksApi service = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		Call<DefaultTaskResponse> call = service.getDefaultTasks(App.getDeviceID());
		call.enqueue(new Callback<DefaultTaskResponse>(){
			@Override
			public void onResponse(Call<DefaultTaskResponse> call, Response<DefaultTaskResponse> response){
				view.hideLoading();
				switch(response.code()){
					case HTTP_SUCCESS:
						DefaultTaskResponse defaultTaskResponseResponse = response.body();
						view.initDefaultList(defaultTaskResponseResponse.settings);
						saveReviewSettingsData(defaultTaskResponseResponse.settings);
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
				}
			}

			@Override
			public void onFailure(Call<DefaultTaskResponse> call, Throwable t){
				Log.e("DEFAULT_TASKS", "Error: " + t.getMessage());
				view.hideLoading();
			}
		});
	}

	@Override
	public void getAppModel(int appId){
		view.showLoading();
		TasksApi api = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		Call<ApplicationResponse> call = api.getTaskData(appId, App.getDeviceID());
		call.enqueue(new Callback<ApplicationResponse>(){
			@Override
			public void onResponse(Call<ApplicationResponse> call, Response<ApplicationResponse> response){
				view.hideLoading();
				switch(response.code()){
					case HTTP_SUCCESS:
						view.openDetailedTaskActivity(response.body().application);
						break;
					case HTTP_APP_NOT_FOUND:
						view.informUser(R.string.detailedAct_appNotFoundException);
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
				}
			}

			@Override
			public void onFailure(Call<ApplicationResponse> call, Throwable t){
				Log.e("TASK_DATA", "onFailure: " + t.getMessage());
				view.hideLoading();
			}
		});
	}

	@Override
	public String[] extractDefaultPrices(DefaultTaskResponse.Settings data){
		String[] tempArray = new String[8];
		tempArray[0] = String.valueOf(data.video);
		tempArray[1] = String.valueOf(data.offerWall);
		tempArray[2] = String.valueOf(data.roulette);
		tempArray[3] = String.valueOf(data.videoZone);
		tempArray[4] = data.referralSecondRewardPercentage;
		tempArray[5] = String.valueOf(data.vkGroup);
		tempArray[6] = String.valueOf(data.commentAward);
		tempArray[7] = data.bonus;
		return tempArray;
	}

	@Override
	public void getFundsFromServer(){
		view.showLoading();
		FundsApi service = RetroFitServiceFactory.createSimpleRetroFitService(FundsApi.class);
		Call<UserFundsResponse> call = service.getFunds(App.getDeviceID());
		call.enqueue(new Callback<UserFundsResponse>(){
			@Override
			public void onResponse(Call<UserFundsResponse> call, Response<UserFundsResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						UserFundsResponse userFunds = response.body();
						view.setDataFromServer(userFunds.balance.ownBalance,
                                userFunds.balance.expectedFunds,
                                userFunds.balance.currencySign,
                                userFunds.balance.referralCount);
						break;
					case HTTP_USER_NOT_FOUND:
						view.hideLoading();
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					default:
						view.hideLoading();
						break;
				}
			}

			@Override
			public void onFailure(Call<UserFundsResponse> call, Throwable t){
				Log.e("BALANCE", "onFailure " + t.getMessage());
				view.hideLoading();
			}
		});
	}

	@Override
	public void updateUserData(){
		String deviceId = App.getDeviceID();
		ProfileApi service = RetroFitServiceFactory.createSimpleRetroFitService(ProfileApi.class);
		service.getUserInfo(deviceId, Constantaz.DEVICE_TYPE).enqueue(new Callback<UserResponse>(){
			@Override
			public void onResponse(Call<UserResponse> call, Response<UserResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						UserResponse userResponse = response.body();
						if(userResponse != null) {
							App.getPrefs().edit().putString(Constantaz.PREFS_FIRST_PROMO, userResponse.userInfo.referralFirstCode).apply();
							App.getPrefs().edit().putString(Constantaz.PREFS_SECOND_PROMO, userResponse.userInfo.referralSecondCode).apply();
						}
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
				}
			}

			@Override
			public void onFailure(Call<UserResponse> call, Throwable t){
				Log.e("USER", "onFailure: " + t.getMessage());
			}
		});
	}

	@Override
	public void disablePromoTask(SendCopyAliPromoCode response){
		String endPoint = response.site;
		if(endPoint.charAt(response.site.length() - 1) != '/')
			endPoint = endPoint.concat("/");
		AliCopyService service = RetroFitServiceFactory.createCustomEndPointService(AliCopyService.class, endPoint);
		Call<BaseCopyAliResponse> call = service.disActivatePromo(response.userId, response.offerId, response.promoCode);
		call.enqueue(new Callback<BaseCopyAliResponse>(){
			@Override
			public void onResponse(Call<BaseCopyAliResponse> call, Response<BaseCopyAliResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						BaseCopyAliResponse data = response.body();
						if(data != null) {
							view.informUser(data.description);
						}
						break;
					default:
						break;
				}
			}

			@Override
			public void onFailure(Call<BaseCopyAliResponse> call, Throwable t){
				Log.e("ALI_COPY", "onFailure: " + t.getMessage());
			}
		});
	}

	@Override
	public void payForVideoAd(final int number){
		TasksApi service = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		Call<BaseResponse> call = service.finishVideoTask(App.getDeviceID());
		call.enqueue(new Callback<BaseResponse>(){
			@Override
			public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						if(number == RANDOM_CONSTANT) {
							view.informUser(R.string.adMobRandomMessage);
						} else{
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
				Log.e("VIDEO_PAY", "onFailure: " + t.getMessage());
				view.informUser(R.string.videoAd_failText);
			}
		});
	}

	@Override
	public int getEnabledActiveTasksCount(@NonNull ArrayList<AppModel> activeTasks){
		int counter = 0;
		for(AppModel model : activeTasks){
			if(model != null && model.isAvailable != null && model.isAvailable) {
				counter++;
			}
		}
		return counter;
	}

	@Override
	public int getNewTasksCount(ArrayList<AppModel> tasks){
		int counter = 0;
		for(AppModel model : tasks){
			if(model != null && !model.isTaskWatched) {
				counter++;
			}
		}
		return counter;
	}

	private void saveReviewSettingsData(DefaultTaskResponse.Settings data){
		App.getPrefs().edit().putFloat(PREF_REVIEW_REWARD, data.reviewAward).apply();
		App.getPrefs().edit().putFloat(PREF_COMMENT_REWARD, data.commentAward).apply();
	}

}
