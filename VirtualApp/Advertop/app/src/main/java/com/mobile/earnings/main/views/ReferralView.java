package com.mobile.earnings.main.views;

import com.mobile.earnings.api.BaseView;

public interface ReferralView extends BaseView{

	void setData(int activeRefCount, int referralCount, String referralsBalance, String currency);

}
