package com.mobile.earnings.api.listeners;

import android.util.Log;

import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

public class VkPostRequestListener extends VKRequest.VKRequestListener{

	@Override
	public void onComplete(VKResponse response){
		super.onComplete(response);
		Log.e("VKShare", "POST id: " + response.responseString);
	}

	@Override
	public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts){
		super.attemptFailed(request, attemptNumber, totalAttempts);
		Log.e("VKShare", "Attemp failed: +" + request.methodName);
	}

	@Override
	public void onError(VKError error){
		super.onError(error);
		Log.e("VKShare", "Error: \n" + error.toString());
	}
}
