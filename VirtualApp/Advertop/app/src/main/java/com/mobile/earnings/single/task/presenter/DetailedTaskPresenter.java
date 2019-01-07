package com.mobile.earnings.single.task.presenter;


public interface DetailedTaskPresenter{

	void updateTask(int taskId);

	void markTaskWatched(int taskId);

	void getReviewAppModel(int appId);

	void sendScreenshotOnModeration(String imageUri, int appId);

}
