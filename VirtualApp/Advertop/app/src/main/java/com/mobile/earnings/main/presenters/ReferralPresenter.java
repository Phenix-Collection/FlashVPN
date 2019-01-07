package com.mobile.earnings.main.presenters;

import android.content.Context;

public interface ReferralPresenter{

	void getDataFromServer(boolean isActive);

	void copyCodeToBuffer(Context context, String promoCode);
}
