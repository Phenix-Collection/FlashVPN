package com.mobile.earnings.main.presenters;


public interface TaskFragmentPresenter{

	void getTasksFromServer(int skip, int take);

	void getActiveTasks(final int skip, int take);

	void finishVKTask();

	void sendScreenshotOnModeration(String imageUri, final int appId);

}
