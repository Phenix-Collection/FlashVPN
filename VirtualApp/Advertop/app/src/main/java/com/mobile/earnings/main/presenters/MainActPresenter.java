package com.mobile.earnings.main.presenters;

import com.mobile.earnings.api.data_models.AppModel;
import com.mobile.earnings.api.responses.DefaultTaskResponse;
import com.mobile.earnings.api.responses.SendCopyAliPromoCode;

import java.util.ArrayList;

public interface MainActPresenter{

	void getTasks();

	void getActiveTasks();

	void getFundsFromServer();

	void disablePromoTask(SendCopyAliPromoCode response);

	void payForVideoAd(final int number);

	void updateUserData();

	void getDefaultTaskPrices();

	void getAppModel(int appId);

	String[] extractDefaultPrices(DefaultTaskResponse.Settings data);

	int getEnabledActiveTasksCount(ArrayList<AppModel> activeTasks);

	int getNewTasksCount(ArrayList<AppModel> tasks);
}
