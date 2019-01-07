package com.mobile.earnings.api.responses;

import com.google.gson.annotations.SerializedName;

public class BaseResponse{

	@SerializedName("error")
	public String error;

	@SerializedName("result")
	public boolean result;
}
