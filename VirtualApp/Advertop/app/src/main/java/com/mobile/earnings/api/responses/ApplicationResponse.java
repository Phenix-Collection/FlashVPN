package com.mobile.earnings.api.responses;

import com.mobile.earnings.api.data_models.AppModel;
import com.google.gson.annotations.SerializedName;



public class ApplicationResponse {

	@SerializedName("application")
	public AppModel application;

}
