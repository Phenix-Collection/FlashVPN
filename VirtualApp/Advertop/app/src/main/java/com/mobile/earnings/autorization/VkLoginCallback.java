package com.mobile.earnings.autorization;

import android.support.annotation.NonNull;
import android.util.Log;

import com.mobile.earnings.api.modules.RegisterModule;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;

public class VkLoginCallback implements VKCallback<VKAccessToken>{

	private RegisterModule registerModule;
	private String         promoCode;
	private String         cityName;
	private float lat, lon;

	/**
	 * Use if you need just login to Vk
	 */
	public VkLoginCallback(){
	}

	/**
	 * Use if you need to get user data after login
	 *
	 * @param registerModule module for registering user
	 * @param promoCode      given promo code
	 * @param cityName       user's city
	 */
	public VkLoginCallback(@NonNull RegisterModule registerModule, String promoCode,
						   String cityName, float lat, float lon){
		this.registerModule = registerModule;
		this.promoCode = promoCode;
		this.cityName = cityName;
		this.lat = lat;
		this.lon = lon;
	}

	@Override
	public void onResult(VKAccessToken res){
		VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.NAME_CASE, VKApiConst.USER_IDS, VKApiConst.HTTPS));
		request.secure = true;
		VkLoginRequestListener requestListener = new VkLoginRequestListener();
		request.executeWithListener(requestListener);
	}

	@Override
	public void onError(VKError error){
		if(error != null) {
			Log.e("VK", "Error: \n" + error.toString());
		}
	}

	private class VkLoginRequestListener extends VKRequest.VKRequestListener{

		@Override
		public void onComplete(VKResponse response){
			super.onComplete(response);
			@SuppressWarnings("unchecked")
			VKApiUser user = ((VKList<VKApiUser>) response.parsedModel).get(0);
			String login = null;
			Log.d("VK_RESPONSE", "onComplete: " + response.json.toString());
			try{
				login = response.json.getJSONArray("response").getJSONObject(0).getString("id");
			} catch(JSONException e){
				Log.e("VK_RESPONSE", "Response parsing exception: " + e.getMessage());
			}
			if(registerModule != null) {
				registerModule.registerUser(promoCode.isEmpty() ? null : promoCode, user.first_name, login, cityName, lat, lon, null, null, 0, "");
			}
		}

		@Override
		public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts){
			super.attemptFailed(request, attemptNumber, totalAttempts);
			Log.e("VK_RESPONSE", "Attempt failed: +" + request.methodName);
		}

		@Override
		public void onError(VKError error){
			super.onError(error);
			Log.e("VK_RESPONSE", "Error: \n" + error.toString());
		}
	}
}
