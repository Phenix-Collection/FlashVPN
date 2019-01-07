package com.mobile.earnings.api.responses;

import com.google.gson.annotations.SerializedName;



public class RouletteSettingsResponse{

	@SerializedName("settings")
	public Settings settings;

	public class Settings {

		@SerializedName("ratio")
		public float ratio;
		@SerializedName("first_bet_amount")
		public float firstBetAmount;
		@SerializedName("second_bet_amount")
		public float secondBetAmount;
		@SerializedName("third_bet_amount")
		public float thirdBetAmount;
		@SerializedName("currency")
		public String currency;

	}
}
