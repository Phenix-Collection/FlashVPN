package com.mobile.earnings.single.task.view;

import android.support.annotation.NonNull;

import com.mobile.earnings.api.BaseView;
import com.mobile.earnings.api.data_models.AppModel;
import com.mobile.earnings.api.data_models.ReviewAppModel;

public interface DetailedTaskView extends BaseView{

	void updateUi(@NonNull AppModel model);

	void updateNotEnabled(boolean isEnabled);

	void setUpReviewAppTaskContainer(@NonNull ReviewAppModel.ReviewModel model);

	void showLoading();

	void hideLoading();
}
