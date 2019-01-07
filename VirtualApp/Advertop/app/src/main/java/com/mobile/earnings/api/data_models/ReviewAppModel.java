package com.mobile.earnings.api.data_models;

import com.google.gson.annotations.SerializedName;

import java.util.List;




public class ReviewAppModel{

	@SerializedName("application")
	public ReviewModel reviewModel;

	public class ReviewModel{
		@SerializedName("review_exists")
		public boolean isReviewTaskExists;
		@SerializedName("review_available")
		public boolean isReviewAvailable;
		@SerializedName("review_type")
		public String  type;
		@SerializedName("review_state")
		public String  state;
		@SerializedName("review_stars")
		public int     stars;
		@SerializedName("review_keywords")
		public List<String> keywords = null;
	}

}
