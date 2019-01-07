package com.mobile.earnings.api.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;



public class DefaultTaskResponse{

	@SerializedName("settings")
	public Settings settings;

	public class Settings {

		@SerializedName("video")
		public float video;
		@SerializedName("vk_group")
		public float vkGroup;
		@SerializedName("referral_first_reward_percentage")
		public String referralFirstRewardPercentage;
		@SerializedName("referral_second_reward_percentage")
		public String referralSecondRewardPercentage;
		@SerializedName("offer_wall")
		public float offerWall;
		@SerializedName("roulette")
		public float roulette;
		@SerializedName("video_zone")
		public float videoZone;
		@SerializedName("bonus")
		public String bonus;
		@SerializedName("currency")
		public String currency;
		//Review task
		@SerializedName("standard_review_available")
		public boolean isDefaultAppReviewAvailable;
		@SerializedName("review_award")
		public float reviewAward;
		@SerializedName("comment_award")
		public float commentAward;
		@SerializedName("comment_keywords")
		public List<String> commentKeywords;

	}
}
