package com.mobile.earnings.api.responses;

import com.google.gson.annotations.SerializedName;

public class UserFundsResponse{

	@SerializedName("balance")
	public Balance balance;

	public class Balance{

		@SerializedName("user_balance")
		public float ownBalance;

		@SerializedName("referral_first_balance")
		public float firstReferralBalance;

		@SerializedName("referral_second_balance")
		public float secondReferralBalance;

		@SerializedName("referral_first_count")
        public int referralCount;

		@SerializedName("total_balance")
		public float totalBalance;

		@SerializedName("expected_profit")
		public float expectedFunds;

		@SerializedName("currency")
		public String currencySign;
	}
}
