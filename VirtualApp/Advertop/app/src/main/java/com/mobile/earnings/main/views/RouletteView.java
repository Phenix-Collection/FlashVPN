package com.mobile.earnings.main.views;

import com.mobile.earnings.api.BaseView;
import com.mobile.earnings.api.responses.RouletteSettingsResponse;



public interface RouletteView extends BaseView{

	void updateSettings(RouletteSettingsResponse.Settings settings);

	void setResultData(boolean result, float amount, float currentBalance);

	void startTimer();

	void showLoading();

	void hideLoading();
}
