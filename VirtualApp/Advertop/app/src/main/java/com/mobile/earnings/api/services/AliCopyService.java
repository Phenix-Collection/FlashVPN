package com.mobile.earnings.api.services;

import com.mobile.earnings.api.responses.BaseCopyAliResponse;
import com.mobile.earnings.api.responses.SendCopyAliPromoCode;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;




public interface AliCopyService{

	@POST("index.php?r=api.promoActivation")
	@FormUrlEncoded
	Call<SendCopyAliPromoCode> registerPromo(@Field("offer_id") int offerId,
												   @Field("code") long promoCode);
	@POST("index.php?r=api.promoDisactivation")
	@FormUrlEncoded
	Call<BaseCopyAliResponse> disActivatePromo(@Field("user_id") int userId,
											   @Field("offer_id") int offerId,
											   @Field("code") int promoCode);

}
