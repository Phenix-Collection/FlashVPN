package com.mobile.earnings.api.data_models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;



public class PaymentSystemResponse{


	@SerializedName("data")
	public ArrayList<PaymentSystem> data = null;
	@SerializedName("can_release_funds")
	public boolean canReleaseFunds;

}
