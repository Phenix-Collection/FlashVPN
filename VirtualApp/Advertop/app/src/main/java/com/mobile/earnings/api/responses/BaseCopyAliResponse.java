package com.mobile.earnings.api.responses;

import com.google.gson.annotations.SerializedName;




public class BaseCopyAliResponse{

	@SerializedName("code")
	public int resultCode;
	@SerializedName("descr")
	public String description;
}
