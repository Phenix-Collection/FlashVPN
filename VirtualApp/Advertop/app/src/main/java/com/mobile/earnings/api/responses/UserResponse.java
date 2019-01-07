package com.mobile.earnings.api.responses;

import com.google.gson.annotations.SerializedName;

public class UserResponse{

	@SerializedName("user")
	public UserInfo userInfo;

	public class UserInfo {

		@SerializedName("id")
		public int id;
		@SerializedName("name")
		public String name;
		@SerializedName("email")
		public String email;
		@SerializedName("login")
		public String login;
		@SerializedName("age")
		public int age;
		@SerializedName("gender")
		public String gender;
		@SerializedName("phone_number")
		public String phoneNumber;
		@SerializedName("referral_first_code")
		public String referralFirstCode;
		@SerializedName("referral_second_code")
		public String referralSecondCode;
		@SerializedName("balance")
		public float balance;
		@SerializedName("referral_first_balance")
		public float referralFirstBalance;
		@SerializedName("referral_second_balance")
		public float referralSecondBalance;
		@SerializedName("currency")
		public String currency;
		@SerializedName("referral_first_count")
		public int referralFirstCount;
		@SerializedName("referral_second_count")
		public int referralSecondCount;
		@SerializedName("referral_second_active_count")
		public int referralActiveCount;

	}
}
