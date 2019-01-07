package com.mobile.earnings.main.views;

import com.mobile.earnings.api.BaseView;
import com.mobile.earnings.api.data_models.AppModel;
import com.mobile.earnings.api.responses.DefaultTaskResponse;

import java.util.ArrayList;

public interface MainActView extends BaseView{

	void setDataFromServer(float ownBalance, float expectedBalance,
                           String currencyCode, int referralCount);

	void setToolbarTitle(String toolbarTitle);

	void setToolbarBalance();

	void onTimerFinished();

	boolean updateTimer();

	void informUser(int message);

	void informUser(String message);

	void initTaskList(ArrayList<AppModel> tasks);

	void initActiveList(ArrayList<AppModel> activeTasks);

	void initDefaultList(DefaultTaskResponse.Settings data);

	void openDetailedTaskActivity(AppModel model);

	void showLoading();

	void hideLoading();

}
