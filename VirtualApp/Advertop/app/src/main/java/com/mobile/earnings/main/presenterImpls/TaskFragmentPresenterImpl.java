package com.mobile.earnings.main.presenterImpls;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.responses.BaseResponse;
import com.mobile.earnings.api.responses.MainTasksResponse;
import com.mobile.earnings.api.services.TasksApi;
import com.mobile.earnings.main.presenters.TaskFragmentPresenter;
import com.mobile.earnings.main.views.TaskFragmentView;
import com.mobile.earnings.utils.Constantaz;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.earnings.utils.AppModelExtractor.extractAppModels;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_APP_NOT_FOUND;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_SUCCESS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_TASK_ALREADY_DONE;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_USER_NOT_FOUND;

public class TaskFragmentPresenterImpl implements TaskFragmentPresenter{

	private TaskFragmentView view;

	public TaskFragmentPresenterImpl(TaskFragmentView view){
		this.view = view;
	}

	@Override
	public void getTasksFromServer(final int skip, int take){
		view.showLoad();
		String deviceID = App.getDeviceID();
		TasksApi service = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		Call<MainTasksResponse> call = service.getTasks(Constantaz.DEVICE_TYPE, deviceID, 0, 0, skip, take);
		call.enqueue(new Callback<MainTasksResponse>(){
			@Override
			public void onResponse(Call<MainTasksResponse> call, Response<MainTasksResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						MainTasksResponse mainTasksResponse = response.body();
						view.updateTasksFromServer(extractAppModels(mainTasksResponse));
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
				}
				view.hideLoad();
			}

			@Override
			public void onFailure(Call<MainTasksResponse> call, Throwable t){
				Log.e("TASKS", "Error downloading: " + t.getMessage());
				view.hideLoad();
			}
		});
	}

	@Override
	public void getActiveTasks(final int skip, int take){
		view.showLoad();
		String deviceId = App.getDeviceID();
		TasksApi service = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		Call<MainTasksResponse> call = service.getActiveTasks(deviceId, skip, take);
		call.enqueue(new Callback<MainTasksResponse>(){
			@Override
			public void onResponse(Call<MainTasksResponse> call, Response<MainTasksResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						MainTasksResponse mainTasksResponse = response.body();
						view.updateActiveTasks(extractAppModels(mainTasksResponse));
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
				}
				view.hideLoad();
			}

			@Override
			public void onFailure(Call<MainTasksResponse> call, Throwable t){
				Log.e("TASKS", "Error downloading: " + t.getMessage());
				view.hideLoad();
			}
		});
	}

	@Override
	public void finishVKTask(){
		TasksApi service = RetroFitServiceFactory.createSimpleRetroFitService(TasksApi.class);
		Call<BaseResponse> call = service.finishVkTask(App.getDeviceID());
		call.enqueue(new Callback<BaseResponse>(){
			@Override
			public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response){
				switch(response.code()){
					case HTTP_SUCCESS:
						view.informUser(R.string.taskFrag_vkTaskFinished);
						break;
					case HTTP_USER_NOT_FOUND:
						view.informUser(R.string.refillRequestUserNotFound);
						break;
					case HTTP_TASK_ALREADY_DONE:
						view.informUser(R.string.single_task_finished);
						break;
					default:
						break;
				}
			}

			@Override
			public void onFailure(Call<BaseResponse> call, Throwable t){
				Log.e("VK_PAY", "onFailure: " + t.getMessage());
			}
		});
	}

	@Override
	public void sendScreenshotOnModeration(@NonNull String imageUri, final int appId){
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
						if(model == null && !model.result) {
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

}
