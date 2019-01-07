package com.mobile.earnings.autorization;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mobile.earnings.api.modules.RegisterModule;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

public class FacebookResultCallback implements FacebookCallback<LoginResult>{

	private RegisterModule registerModule;
	private String         promoCode;
	private String         cityName;
	private float lat, lon;

	public FacebookResultCallback(@NonNull RegisterModule registerer, String promoCode,
								  String cityName, float lat, float lon){
		this.registerModule = registerer;
		this.promoCode = promoCode;
		this.cityName = cityName;
		this.lat = lat;
		this.lon = lon;
	}

	@Override
	public void onSuccess(LoginResult loginResult){
		GraphRequest request = getUserData(loginResult.getAccessToken());
		Bundle parameters = new Bundle();
		String permissions = "id,name,link,email";
		parameters.putString("fields", permissions);
		request.setParameters(parameters);
		request.executeAsync();
	}

	@Override
	public void onCancel(){
		Log.e("FB_RESPONSE", "Facebook send cancel");
	}

	@Override
	public void onError(FacebookException error){
		Log.e("FB_RESPONSE", "Facebook onError: \n" + error.toString());
	}

	private GraphRequest getUserData(AccessToken accessToken){
		return GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback(){
			@Override
			public void onCompleted(JSONObject object, GraphResponse response){
				String name = null;
				try{
					if(object != null) {
						name = object.getString("name");
					}
				} catch(JSONException e){
					Crashlytics.logException(e);
				}
				registerModule.registerUser(promoCode.isEmpty() ? null : promoCode, name, null, cityName, lat, lon, null, null, 0, "");
			}
		});
	}
}
