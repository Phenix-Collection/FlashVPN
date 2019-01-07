package com.mobile.earnings.api.responses;

import com.mobile.earnings.api.data_models.AppModel;
import com.google.gson.annotations.SerializedName;




public class UpdateTaskResponse{

	@SerializedName("task")
	public Task task;

	public class Task {

		@SerializedName("id")
		public int      id;
		@SerializedName("app")
		public AppModel app;

	}
}
