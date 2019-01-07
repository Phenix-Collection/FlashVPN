package com.mobile.earnings.single.task.presenter.impl;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.data_models.AppModel;
import com.mobile.earnings.api.data_models.ReviewAppModel;
import com.mobile.earnings.api.responses.BaseResponse;
import com.mobile.earnings.api.responses.UpdateTaskResponse;
import com.mobile.earnings.api.services.TasksApi;
import com.mobile.earnings.single.task.presenter.DetailedTaskPresenter;
import com.mobile.earnings.single.task.view.DetailedTaskView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_APP_NOT_FOUND;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_SUCCESS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_TASK_ALREADY_DONE;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_TASK_FAILED;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_TASK_NOT_AVAILABLE;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_USER_NOT_FOUND;

public class DetailedPresenterImpl implements DetailedTaskPresenter{

	private DetailedTaskView view;

	public DetailedPresenterImpl(DetailedTaskView view){
		this.view = view;
	}

	@Override
	public void updateTask(final int taskId){
		view.showLoading();
		TasksApi service = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		String deviceID = App.getDeviceID();
		Call<UpdateTaskResponse> call = service.updateTask(taskId, deviceID);
		call.enqueue(new Callback<UpdateTaskResponse>(){
			@Override
			public void onResponse(Call<UpdateTaskResponse> call, Response<UpdateTaskResponse> response){
				view.hideLoading();
				UpdateTaskResponse taskResponse = response.body();
				AppModel model = null;
				if(taskResponse != null) {
					if(taskResponse.task != null) {
						model = taskResponse.task.app;
					}
				}
				switch(response.code()){
					case HTTP_SUCCESS:
						if(model != null) {
							view.updateUi(model);
							view.updateNotEnabled(model.isAvailable);
						}
						break;
					case HTTP_TASK_ALREADY_DONE:
						view.informUser(R.string.single_task_finished);
						break;
					case HTTP_APP_NOT_FOUND:
						view.informUser(R.string.detailedAct_appNotFoundException);
						break;
					case HTTP_TASK_NOT_AVAILABLE:
						view.informUser(R.string.detailedTaskAct_taskNotReadyYetException);
						break;
					case HTTP_TASK_FAILED:
						view.informUser(R.string.detailedTaskAct_taskFailedException);
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
				}
			}

			@Override
			public void onFailure(Call<UpdateTaskResponse> call, Throwable t){
				Log.e("TASK_UPDATE", "onFailure: " + t.getMessage());
				view.hideLoading();
			}
		});
	}

	@Override
	public void markTaskWatched(int taskId){
		TasksApi api = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		Call<BaseResponse> call = api.markWatched(taskId, App.getDeviceID());
		call.enqueue(new Callback<BaseResponse>(){
			@Override
			public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						Log.e("WATCHED", "Success");
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
			public void onFailure(Call<BaseResponse> call, Throwable t){
				Log.e("WATCHED", "onFailure: " + t.getMessage());
			}
		});
	}

	@Override
	public void getReviewAppModel(int appId){
		TasksApi api = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		Call<ReviewAppModel> call = api.getReviewAppModel(appId, App.getDeviceID());
		call.enqueue(new Callback<ReviewAppModel>(){
			@Override
			public void onResponse(Call<ReviewAppModel> call, Response<ReviewAppModel> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						ReviewAppModel model = response.body();
						if(model != null) {
							view.setUpReviewAppTaskContainer(model.reviewModel);
						} else{
							view.informUser(R.string.someErrorText);
						}
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					case HTTP_APP_NOT_FOUND:
						view.informUser(R.string.detailedAct_appNotFoundException);
						break;
				}
			}

			@Override
			public void onFailure(Call<ReviewAppModel> call, Throwable t){
				Log.e("REVIEW", "onFailure: " + t.getMessage());
			}
		});
	}

	@Override
	public void sendScreenshotOnModeration(String imageUri, final int appId){
		long oneMByte = 1048576;
		TasksApi api = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		File imageFile = new File(imageUri);
		try{
			if(imageFile.length() >= oneMByte) {
				Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
				try{
					bitmap.compress(Bitmap.CompressFormat.JPEG, 2, new FileOutputStream(imageFile));
				} catch(FileNotFoundException e){
					Log.e("IMAGE_COMPRESSING", "Exception: " + e.getMessage());
				}
			}
		} catch(SecurityException e){
			Log.e("IMAGE_LENGTH", "Exception: " + e.getMessage());
		}
		RequestBody deviceIdPart = RequestBody.create(MediaType.parse("text/plain"), App.getDeviceID());
		RequestBody imagePart = RequestBody.create(MediaType.parse("image/*"), imageFile);
		RequestBody appIdPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(appId));
		Call<BaseResponse> call = api.sendScreenshot(deviceIdPart, imagePart, appIdPart);
		call.enqueue(new Callback<BaseResponse>(){
			@Override
			public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						BaseResponse model = response.body();
						if(model != null && model.result) {
							getReviewAppModel(appId);
						} else{
							view.informUser(R.string.someErrorText);
						}
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					case HTTP_APP_NOT_FOUND:
						view.informUser(R.string.detailedAct_appNotFoundException);
						break;
				}
			}

			@Override
			public void onFailure(Call<BaseResponse> call, Throwable t){
				Log.e("MODERATING", "onFailure: " + t.getMessage());
			}
		});
	}

	public void copyCodeToBuffer(ClipboardManager clipboard, String promoCode){
		ClipData clip = ClipData.newPlainText("promoCode", promoCode);
		clipboard.setPrimaryClip(clip);
		view.informUser(R.string.referralFrag_promoCopied);
	}

}
