package com.mobile.earnings.main.views;

import com.mobile.earnings.api.BaseView;
import com.mobile.earnings.api.data_models.PaymentSystem;

import java.util.ArrayList;

public interface FundsView extends BaseView{

	void setDataFromServer(float ownBalance, float referralBalance, float overallBalance, float expectedBalance,
						   String currencyCode);

	void showRefillDialog(String account, final float amount, float cleanAmount, String paymentSystemName);

	void informAboutLimit(float limit);

	void canReleaseFunds(boolean canReleaseFunds);

	void createPaymentSystemSheet(ArrayList<PaymentSystem> items);

	void showLoading();

	void hideLoading();
}
