package com.mobile.earnings.profile.presentation;

import android.util.Log;

import com.mobile.earnings.App;
import com.mobile.earnings.R;
import com.mobile.earnings.api.RetroFitServiceFactory;
import com.mobile.earnings.api.responses.UserResponse;
import com.mobile.earnings.api.services.ProfileApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mobile.earnings.utils.Constantaz.DEVICE_TYPE;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_EMAIL_EXISTS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_SUCCESS;
import static com.mobile.earnings.utils.HttpResponseCodes.HTTP_USER_NOT_FOUND;




public class ProfilePresenter{

	private ProfileView view;
	private ProfileApi  service;

	public ProfilePresenter(ProfileView view){
		this.view = view;
		service = RetroFitServiceFactory.createSimpleRetroFitService(ProfileApi.class);
	}

	public void getUserData(){
		service.getUserInfo(App.getDeviceID(), DEVICE_TYPE)
				.enqueue(new Callback<UserResponse>(){
					@Override
					public void onResponse(Call<UserResponse> call, Response<UserResponse> response){
						switch(response.code()){
							case HTTP_SUCCESS:
								view.setProfileData(response.body().userInfo);
								break;
							case HTTP_USER_NOT_FOUND:
								view.informUser(R.string.refillRequestUserNotFound);
								break;
						}
					}

					@Override
					public void onFailure(Call<UserResponse> call, Throwable t){
						Log.e("PROFILE", "onFailure: "+t.getMessage());
					}
				});
	}

	public void updateProfileData(String name, String email, String phoneNumber, int year, String gender){
		service.updateUserProfile(App.getDeviceID(), name, email, phoneNumber, year, gender)
				.enqueue(new Callback<UserResponse>(){
					@Override
					public void onResponse(Call<UserResponse> call, Response<UserResponse> response){
						switch(response.code()){
							case HTTP_SUCCESS:
								view.informUser(R.string.profileAct_profileDataUpdated);
								break;
							case HTTP_USER_NOT_FOUND:
								view.informUser(R.string.refillRequestUserNotFound);
								break;
							case HTTP_EMAIL_EXISTS:
								view.informUser(R.string.profileAct_emailAlreadyExistsError);
								break;
						}
					}

					@Override
					public void onFailure(Call<UserResponse> call, Throwable t){
						Log.e("PROFILE", "onFailure: "+t.getMessage());
					}
				});
	}
}
