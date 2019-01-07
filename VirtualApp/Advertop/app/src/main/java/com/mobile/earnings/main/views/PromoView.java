package com.mobile.earnings.main.views;

import com.mobile.earnings.api.responses.SendCopyAliPromoCode;



public interface PromoView{

	void informUser(int resourceId);

	void codeSubmitted(SendCopyAliPromoCode response);
}
