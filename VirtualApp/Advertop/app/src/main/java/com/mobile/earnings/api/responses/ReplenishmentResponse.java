package com.mobile.earnings.api.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;




public class ReplenishmentResponse{
	@SerializedName("status")
	@Expose
	private boolean status;
	@SerializedName("amount")
	@Expose
	private float amount;
	@SerializedName("amount_clean")
	@Expose
	private float amountClean;
	@SerializedName("limit")
	@Expose
	private float limit;

	public float getAmountClean(){
		return amountClean;
	}

	public boolean getStatus() {
		return status;
	}

	public float getAmount() {
		return amount;
	}

	public float getLimit() {
		return limit;
	}

}
