package com.mobile.earnings.api.responses;

import com.google.gson.annotations.SerializedName;



public class RouletteResultResponse{

	@SerializedName("rouletteRoll")
	public RouletteRoll rouletteRoll;

	public class RouletteRoll {

		@SerializedName("id")
		public int                   id;
		@SerializedName("bet")
		public float                 bet;
		@SerializedName("amount")
		public float                 amount;
		@SerializedName("result")
		public boolean               result;
		@SerializedName("currency")
		public String                currency;
		@SerializedName("user")
		public UserResponse.UserInfo user;

	}
}
