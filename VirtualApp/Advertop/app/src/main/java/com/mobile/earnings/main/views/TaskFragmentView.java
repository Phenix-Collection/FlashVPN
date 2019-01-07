package com.mobile.earnings.main.views;

import com.mobile.earnings.api.BaseView;
import com.mobile.earnings.api.data_models.AppModel;

import java.util.ArrayList;

public interface TaskFragmentView extends BaseView{

	void updateTasksFromServer(ArrayList<AppModel> data);

	void updateActiveTasks(ArrayList<AppModel> data);

	void showLoad();

	void hideLoad();
}
