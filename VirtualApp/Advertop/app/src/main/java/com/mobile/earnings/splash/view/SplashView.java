package com.mobile.earnings.splash.view;

import android.support.annotation.NonNull;

import com.mobile.earnings.api.BaseView;

public interface SplashView extends BaseView{

	void startRegisterAct(@NonNull String cityName, float lat, float lon);

	void openMainAct();

	void openTutorialScreen();
}
