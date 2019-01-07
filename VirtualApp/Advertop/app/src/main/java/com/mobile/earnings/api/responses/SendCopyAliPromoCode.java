package com.mobile.earnings.api.responses;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;



public class SendCopyAliPromoCode extends BaseCopyAliResponse implements Serializable{

	@SerializedName("site")
	public String site;
	@SerializedName("user_id")
	public int userId;
	@SerializedName("offer_id")
	public int offerId;
	@SerializedName("promocode")
	public int promoCode;
	@SerializedName("reward")
	public String rewardPrice;
	@SerializedName("timer_end")
	public long timerTimeInMillis;
}
